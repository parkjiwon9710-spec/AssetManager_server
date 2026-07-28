package service;

import Market.MarketSpec;
import Market.MarketSpecCache;
import db.DBUtil;
import model.OrderSide;
import model.Position;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PositionService {

    private final PositionDAO positionDAO = new PositionDAO();
    private final FeeService feeService = new FeeService();
    private final UserDAO userDAO = new UserDAO();
    private final UserService userService = new UserService();
    private final TradeHistoryDAO tradeHistoryDAO = new TradeHistoryDAO();
    private final UserStatusDAO userStatusDAO = new UserStatusDAO();
    private final ExchangeRateDAO exchangeRateDAO = new ExchangeRateDAO();
    private final TopInfoService topInfoService = new TopInfoService();

    public void applyTrade(
            int orderId, int userId, String symbol, OrderSide side, double price, int qty,
            boolean tpEnabled, int tpTicks, boolean slEnabled, int slTicks
    ){

        Position p = positionDAO.findByUserAndSymbol(userId, symbol);
        MarketSpec spec = MarketSpecCache.get(symbol);

        // ===== 신규 포지션 =====
        if (p == null) {

            double entryFee = feeService.getFeeKRW(userId, symbol, price, qty);
            String partnerUsername = userDAO.getPartnerUsername(userId);

            tradeHistoryDAO.insert(orderId, userId, partnerUsername, symbol, side.name(), price, qty, 0, entryFee);
            userStatusDAO.updateTradeStats(userId, -entryFee, entryFee, Timestamp.valueOf(LocalDateTime.now()));

            Position np = new Position();
            np.setUserId(userId);
            np.setSymbol(symbol);
            np.setDirection(side == OrderSide.BUY ? "LONG" : "SHORT");
            np.setQty(qty);
            np.setAvgPrice(price);
            np.setRealizedPnl(-entryFee);
            np.setOrderId(orderId);


            // 🔥 TP/SL 반영
            applyTpSl(np, tpEnabled, tpTicks, slEnabled, slTicks, spec.getTickSize());

            positionDAO.insert(np);
            userService.applyBalanceChange(userId, -entryFee);


//            /// ////////////탑인포패널에서 1초스케줄려말고 변화시점에 푸쉬해서 탑인포패널 업데이트/////////////
//            topInfoService.pushToUser(userId);
//            /// /////////////////////
            return;
        }

        boolean sameDirection =
                (p.getDirection().equals("LONG") && side == OrderSide.BUY) ||
                        (p.getDirection().equals("SHORT") && side == OrderSide.SELL);

        // ===== 같은 방향 =====
        if (sameDirection) {

            double entryFee = feeService.getFeeKRW(userId, symbol, price, qty);
            String partnerUsername = userDAO.getPartnerUsername(userId);

            tradeHistoryDAO.insert(orderId,userId, partnerUsername, symbol, side.name(), price, qty, 0, entryFee);
            userStatusDAO.updateTradeStats(userId, -entryFee, entryFee, Timestamp.valueOf(LocalDateTime.now()));

            p.setRealizedPnl(p.getRealizedPnl() - entryFee);
            userService.applyBalanceChange(userId, -entryFee);

            double totalCost = p.getAvgPrice() * p.getQty() + price * qty;
            int newQty = p.getQty() + qty;

            p.setQty(newQty);
            p.setAvgPrice(totalCost / newQty);
            p.setOrderId(orderId);

            // 🔥 TP/SL 반영 (평단가가 바뀌었으니 새 평단가 기준으로 재계산)
            applyTpSl(p, tpEnabled, tpTicks, slEnabled, slTicks, spec.getTickSize());

            positionDAO.update(p);

//            /// ////////////탑인포패널에서 1초스케줄려말고 변화시점에 푸쉬해서 탑인포패널 업데이트/////////////
//            topInfoService.pushToUser(userId);
//            /// /////////////////////
            return;
        }

        // ===== 반대 방향 → 청산 =====
        int closeQty = Math.min(p.getQty(), qty);



        double priceDiff =
                p.getDirection().equals("LONG")
                        ? price - p.getAvgPrice()
                        : p.getAvgPrice() - price;

        double ticks = Math.round(priceDiff / spec.getTickSize());
        double rate = Store.ExchangeRateCache.getRate(spec.getCurrency());
        double tradingProfit = ticks * spec.getTickValue() * rate * closeQty;

        double fee = feeService.getFeeKRW(userId, symbol, price, closeQty);
        double finalProfit = tradingProfit - fee;
        boolean isWin = tradingProfit > 0;

        userService.applyBalanceChange(userId, finalProfit);

        String partnerUsername = userDAO.getPartnerUsername(userId);

        tradeHistoryDAO.insert(orderId,userId, partnerUsername, symbol, side.name(), price, closeQty, tradingProfit, fee);
        userStatusDAO.updateTradeStats(userId, finalProfit, fee, Timestamp.valueOf(LocalDateTime.now()));

        if (tradingProfit != 0) {
            userStatusDAO.updateWinRate(userId, isWin);
        }

        p.setRealizedPnl(p.getRealizedPnl() + finalProfit);

        int remainQty = Math.max(0, p.getQty() - closeQty);

        if (remainQty > 0) {
            p.setQty(remainQty);
            // 부분 청산이라 포지션은 유지됨 - 기존 TP/SL 설정 유지 (건드리지 않음)
            positionDAO.update(p);


//            /// ////////////탑인포패널에서 1초스케줄려말고 변화시점에 푸쉬해서 탑인포패널 업데이트/////////////
//            topInfoService.pushToUser(userId);;
//            /// /////////////////////

            return;
        }

        positionDAO.update(p);
        positionDAO.delete(p.getId());

        // ===== 포지션 전환 =====
        int openQty = qty - closeQty;
        if (openQty > 0) {

            double entryFee = feeService.getFeeKRW(userId, symbol, price, openQty);

            tradeHistoryDAO.insert(orderId,userId, partnerUsername, symbol, side.name(), price, openQty, 0, entryFee);
            userStatusDAO.updateTradeStats(userId, -entryFee, entryFee, Timestamp.valueOf(LocalDateTime.now()));
            userService.applyBalanceChange(userId, -entryFee);

            Position np = new Position();
            np.setUserId(userId);
            np.setSymbol(symbol);
            np.setDirection(side == OrderSide.BUY ? "LONG" : "SHORT");
            np.setQty(openQty);
            np.setAvgPrice(price);
            np.setRealizedPnl(-entryFee);
            np.setOrderId(orderId);

            // 🔥 TP/SL 반영
            applyTpSl(np, tpEnabled, tpTicks, slEnabled, slTicks, spec.getTickSize());

            positionDAO.insert(np);


//            /// ////////////탑인포패널에서 1초스케줄려말고 변화시점에 푸쉬해서 탑인포패널 업데이트/////////////
//            topInfoService.pushToUser(userId);
//            /// /////////////////////
        }
    }


    // 🔥 TP/SL 계산 헬퍼
    private void applyTpSl(Position pos, boolean tpEnabled, int tpTicks, boolean slEnabled, int slTicks, double tickSize) {

        boolean isLong = "LONG".equals(pos.getDirection());

        if (tpEnabled && tpTicks > 0) {
            pos.setTpEnabled(true);
            pos.setTpPrice(isLong
                    ? pos.getAvgPrice() + tpTicks * tickSize
                    : pos.getAvgPrice() - tpTicks * tickSize);
            pos.setTpTicks(tpTicks);   // 🔥 추가
        } else {
            pos.setTpEnabled(false);
            pos.setTpTicks(0);         // 🔥 추가 (꺼지면 리셋)
        }

        if (slEnabled && slTicks > 0) {
            pos.setSlEnabled(true);
            pos.setSlPrice(isLong
                    ? pos.getAvgPrice() - slTicks * tickSize
                    : pos.getAvgPrice() + slTicks * tickSize);
            pos.setSlTicks(slTicks);   // 🔥 추가
        } else {
            pos.setSlEnabled(false);
            pos.setSlTicks(0);         // 🔥 추가
        }
    }

    //손익절 즉시갱신메서드   포지션은 있는데 체크박스가 꺼져있다가, 처음으로 체크박스를 켤 때 && 이미 체크박스가 켜져있는 상태에서, 틱수 스피너나 드롭다운 값만 바꿀 때
    public boolean updateTpSl(int userId, String symbol, boolean tpEnabled, int tpTicks, boolean slEnabled, int slTicks) {

        Position pos = positionDAO.findByUserAndSymbol(userId, symbol);
        if (pos == null) return false;   // 포지션 없으면 반영 대상 없음

        MarketSpec spec = MarketSpecCache.get(symbol);
        applyTpSl(pos, tpEnabled, tpTicks, slEnabled, slTicks, spec.getTickSize());

        positionDAO.update(pos);
        return true;
    }




    public Position getPosition(int userId, String symbol) {
        return positionDAO.findByUserAndSymbol(userId, symbol);
    }

    public List<Position> getAllPositions(int userId) {  // 유저별 (로스컷 등에서 이미 사용 중)
        return positionDAO.findAllByUser(userId);
    }
    public Position getPositionById(int id) {
        return positionDAO.findById(id);
    }



    public List<Position> findPositionsBySymbol(String symbol){

        List<Position> list = new ArrayList<>();

        String sql =
                "SELECT user_id, symbol, direction, qty " +
                        "FROM positions " +
                        "WHERE symbol=?";

        try(
                Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ){

            ps.setString(1, symbol);

            ResultSet rs = ps.executeQuery();

            while(rs.next()){

                Position p = new Position();

                p.setUserId(rs.getInt("user_id"));
                p.setSymbol(rs.getString("symbol"));
                p.setDirection(rs.getString("direction"));
                p.setQty(rs.getInt("qty"));

                list.add(p);
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        return list;
    }

    public List<Position> findPositionsBySymbolAndUser(int userId, String symbol) {

        List<Position> list = new ArrayList<>();

        String sql =
                "SELECT user_id, symbol, direction, qty " +
                        "FROM positions " +
                        "WHERE user_id=? AND symbol=?";

        try (
                Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setInt(1, userId);
            ps.setString(2, symbol);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Position p = new Position();

                    p.setUserId(rs.getInt("user_id"));
                    p.setSymbol(rs.getString("symbol"));
                    p.setDirection(rs.getString("direction"));
                    p.setQty(rs.getInt("qty"));

                    list.add(p);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }


//    //public List<Position> getAllPositions() {           // 전체 (checkTpSl용) 이었지만 지금 안씀
//        return positionDAO.findAll();
//    }
    public List<Position> getActiveTpSlPositionsBySymbol(String symbol) {
        return positionDAO.findActiveTpSlPositionsBySymbol(symbol);
    }


    //실시간 평가손익 계산
    public double getTotalRealtimePnl(int userId) {

        double total = 0;

        String sql = "SELECT symbol, qty, avg_price, direction FROM positions WHERE user_id=?";

        try (java.sql.Connection conn = db.DBUtil.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            java.sql.ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String symbol = rs.getString("symbol");
                int qty = rs.getInt("qty");
                double avg = rs.getDouble("avg_price");
                String dir = rs.getString("direction");

                double currentPrice = Store.PriceStore.getLast(symbol);
                Market.MarketSpec spec = Market.MarketSpecCache.get(symbol);

                double priceDiff = "LONG".equals(dir) ? currentPrice - avg : avg - currentPrice;
                double ticks = Math.round(priceDiff / spec.getTickSize());
                double rate = Store.ExchangeRateCache.getRate(spec.getCurrency());

                total += ticks * spec.getTickValue() * rate * qty;
            }

        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }

        return total;
    }
//그 종목 보유 중인 유저 목록
    public List<Integer> getUsersBySymbol(String symbol) {
        return positionDAO.findUserIdsBySymbol(symbol);
    }
//강제 전종목청산=로스컷
    public void forcecloseAllPositions(int userId) {

        List<Position> positions = positionDAO.findAllByUser(userId);

        for (Position p : positions) {
            String symbol = p.getSymbol();

            model.OrderSide closeSide = p.isLong() ? model.OrderSide.SELL : model.OrderSide.BUY;

            double executionPrice = closeSide == model.OrderSide.BUY
                    ? Store.PriceStore.getBestAsk(symbol)
                    : Store.PriceStore.getBestBid(symbol);

            if (Double.isNaN(executionPrice) || executionPrice <= 0) continue;

            // executeMarket 재사용 (OrderExecutionService 순환참조 피하려고 직접 처리)
            int orderId = new OrderDAO().insertFilled(
                    userId,
                    symbol,
                    closeSide.name(),
                    executionPrice,
                    p.getQty(),
                    0,
                    p.getAvgPrice(),
                    true,
                    "LIQUIDATION",
                    null   // 🔥 로스컷은 틱수 개념 없음
            );

            if (orderId > 0) {
                applyTrade(orderId, userId, symbol, closeSide, executionPrice, p.getQty(),
                        false, 0, false, 0);

                server.ClientEventMessage event = new server.ClientEventMessage(
                        "TRADE_EXECUTED", symbol,
                        closeSide == model.OrderSide.BUY ? "BUY_EXECUTED" : "SELL_EXECUTED"
                );
                //사운드/알림용 이벤트 전송
                server.SessionManager.sendEventToCustomer(userId, event);
            }

            System.out.println("[서버] 로스컷 강제청산 - userId: " + userId + ", symbol: " + symbol + ", qty: " + p.getQty());
        }
//for문 밖(전체종목청산끝나고) 포지션변화일어나면 그 고객 주문창 탑인포패널 즉시 푸쉬
        topInfoService.pushToUser(userId);
    }


//TPSL 청산용도 / 직접처리방식임
public void closePosition(Position pos, double price, String reason) {

    OrderSide side = pos.isLong() ? OrderSide.SELL : OrderSide.BUY;

    double triggerPrice = "TP".equals(reason) ? pos.getTpPrice() : pos.getSlPrice();
    Integer tickCount = "TP".equals(reason) ? pos.getTpTicks() : pos.getSlTicks();   // 🔥 추가

    int orderId = new OrderDAO().insertFilled(
            pos.getUserId(),
            pos.getSymbol(),
            side.name(),
            price,
            pos.getQty(),
            triggerPrice,
            pos.getAvgPrice(),
            true,
            reason,
            tickCount   // 🔥 추가
    );

    if (orderId > 0) {
        applyTrade(orderId, pos.getUserId(), pos.getSymbol(), side, price, pos.getQty(),
                false, 0, false, 0);

        server.ClientEventMessage event = new server.ClientEventMessage(
                "TRADE_EXECUTED", pos.getSymbol(),
                side == model.OrderSide.BUY ? "BUY_EXECUTED" : "SELL_EXECUTED"
        );
        server.SessionManager.sendEventToCustomer(pos.getUserId(), event);

        //포지션변화일어나면 그 고객 주문창 탑인포패널 즉시 푸쉬
        topInfoService.pushToUser(pos.getUserId());

        System.out.println("[서버] " + reason + " 자동청산 - userId: " + pos.getUserId()
                + ", symbol: " + pos.getSymbol() + ", price: " + price + ", qty: " + pos.getQty()
                + ", ticks: " + tickCount);
    }
}

    public List<model.PositionRow> loadPositionRows(int userId) {
        List<model.PositionRow> result = new ArrayList<>();

        String sql = "SELECT id, symbol, qty, avg_price, direction FROM positions WHERE user_id=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String symbol = rs.getString("symbol");
                double currentPrice = Store.PriceStore.getLast(symbol);
                int qty = rs.getInt("qty");
                double avg = rs.getDouble("avg_price");
                String dir = rs.getString("direction");
                String displaySide = "LONG".equals(dir) ? "매수" : "매도";

                Market.MarketSpec spec = Market.MarketSpecCache.get(symbol);

                double priceDiff = "LONG".equals(dir) ? currentPrice - avg : avg - currentPrice;
                double ticks = Math.round(priceDiff / spec.getTickSize());
                double rate = Store.ExchangeRateCache.getRate(spec.getCurrency());
                double pnl = ticks * spec.getTickValue() * rate * qty;

                result.add(new model.PositionRow(
                        rs.getInt("id"), symbol, avg, currentPrice,
                        displaySide, qty, String.format("%.2f", pnl)
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

}