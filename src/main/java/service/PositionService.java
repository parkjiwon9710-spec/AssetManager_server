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
    private final OrderAuditDAO orderAuditDAO = new service.OrderAuditDAO();


    public model.TradeResult applyTrade(
            int orderId, int userId, String symbol, OrderSide side, double price, int qty,
            boolean tpEnabled, int tpTicks, boolean slEnabled, int slTicks
    ){
        model.TradeResult result = new model.TradeResult();

        Position p = positionDAO.findByUserAndSymbol(userId, symbol);
        MarketSpec spec = MarketSpecCache.get(symbol);

        // ===== 신규 포지션 =====
        if (p == null) {

            double entryFeeRaw = feeService.getFeeKRW(userId, symbol, price, qty);
            long entryFee = Math.round(entryFeeRaw);
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

            applyTpSl(np, tpEnabled, tpTicks, slEnabled, slTicks, spec.getTickSize());

            positionDAO.insert(np);
            userService.applyBalanceChange(userId, -entryFee);

            result.fee = entryFee;
            result.realizedPnl = 0;   // 🔥 -entryFee → 0
            result.positionQty = qty;
            result.positionAvgPrice = price;
            result.positionDirection = np.getDirection();
            return result;
        }

        boolean sameDirection =
                (p.getDirection().equals("LONG") && side == OrderSide.BUY) ||
                        (p.getDirection().equals("SHORT") && side == OrderSide.SELL);

        // ===== 같은 방향 =====
        if (sameDirection) {

            double entryFeeRaw = feeService.getFeeKRW(userId, symbol, price, qty);
            long entryFee = Math.round(entryFeeRaw);

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

            applyTpSl(p, tpEnabled, tpTicks, slEnabled, slTicks, spec.getTickSize());

            positionDAO.update(p);

            result.fee = entryFee;
            result.realizedPnl = 0;   // 🔥 -entryFee → 0
            result.positionQty = p.getQty();
            result.positionAvgPrice = p.getAvgPrice();
            result.positionDirection = p.getDirection();
            return result;
        }

        // ===== 반대 방향 → 청산 =====
        int closeQty = Math.min(p.getQty(), qty);

        double priceDiff =
                p.getDirection().equals("LONG")
                        ? price - p.getAvgPrice()
                        : p.getAvgPrice() - price;

        double rate = Store.ExchangeRateCache.getRate(spec.getCurrency());   // 기존에 이미 있는 줄
        long ticks = Math.round(priceDiff / spec.getTickSize());
        long tradingProfit = ticks * spec.getTickValueKrw(rate) * closeQty;

        double feeRaw = feeService.getFeeKRW(userId, symbol, price, closeQty);
        long fee = Math.round(feeRaw);

        long finalProfit = tradingProfit - fee;
        boolean isWin = tradingProfit > 0;

        userService.applyBalanceChange(userId, finalProfit);

        String partnerUsername = userDAO.getPartnerUsername(userId);

        tradeHistoryDAO.insert(orderId, userId, partnerUsername, symbol, side.name(), price, closeQty, tradingProfit, fee);
        userStatusDAO.updateTradeStats(userId, finalProfit, fee, Timestamp.valueOf(LocalDateTime.now()));

        if (tradingProfit != 0) {
            userStatusDAO.updateWinRate(userId, isWin);
        }

        p.setRealizedPnl(p.getRealizedPnl() + finalProfit);

        int remainQty = Math.max(0, p.getQty() - closeQty);

        result.fee = fee;
        result.realizedPnl = tradingProfit;   // 🔥 finalProfit → tradingProfit (순손익)

        if (remainQty > 0) {
            p.setQty(remainQty);
            positionDAO.update(p);

            result.positionQty = remainQty;
            result.positionAvgPrice = p.getAvgPrice();
            result.positionDirection = p.getDirection();
            return result;
        }

        positionDAO.update(p);
        positionDAO.delete(p.getId());

        // ===== 포지션 전환 =====
        int openQty = qty - closeQty;
        if (openQty > 0) {

            double entryFeeRaw = feeService.getFeeKRW(userId, symbol, price, openQty);
            long entryFee = Math.round(entryFeeRaw);

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

            applyTpSl(np, tpEnabled, tpTicks, slEnabled, slTicks, spec.getTickSize());

            positionDAO.insert(np);

            // 🔥 전환된 경우: 수수료/손익은 청산분+신규진입분 합산, 포지션은 새로 열린 쪽
            result.fee = fee + entryFee;
            result.realizedPnl = tradingProfit;   // 🔥 finalProfit - entryFee → tradingProfit (청산분 순손익만, 신규진입은 실현손익 없음)
            result.positionQty = openQty;
            result.positionAvgPrice = price;
            result.positionDirection = np.getDirection();
            return result;
        }

        // 완전 청산 (전환 없음) - 포지션 없음
        result.positionQty = 0;
        result.positionAvgPrice = 0;
        result.positionDirection = null;
        return result;
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
                long ticks = Math.round(priceDiff / spec.getTickSize());
                double rate = Store.ExchangeRateCache.getRate(spec.getCurrency());

                total += ticks * spec.getTickValueKrw(rate) * qty;
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

        double orderPrice = closeSide == model.OrderSide.BUY
                ? Store.PriceStore.getBestAsk(symbol)
                : Store.PriceStore.getBestBid(symbol);

        LocalDateTime signalTime = LocalDateTime.now().withNano(0);

        if (Double.isNaN(orderPrice) || orderPrice <= 0) continue;

        double filledPrice = closeSide == model.OrderSide.BUY
                ? Store.PriceStore.getBestAsk(symbol)
                : Store.PriceStore.getBestBid(symbol);

        LocalDateTime fillTime = LocalDateTime.now().withNano(0);

        int orderId = new OrderDAO().insertFilled(
                userId, symbol, closeSide.name(),
                orderPrice, filledPrice,
                p.getQty(), 0, p.getAvgPrice(), true, "LIQUIDATION", null
        );

        if (orderId > 0) {
            model.TradeResult result = applyTrade(orderId, userId, symbol, closeSide, filledPrice, p.getQty(),
                    false, 0, false, 0);   // 🔥 반환값 받음

            double margin = new service.UserDataDAO().getAvailableMargin(userId);   // 🔥 조회

            logSignalAndFill(userId, symbol, closeSide, String.valueOf(orderId),
                    "강제청산(로스컷)", orderPrice, filledPrice, p.getQty(), signalTime, fillTime, result, margin);   // 🔥 전달

            server.ClientEventMessage event = new server.ClientEventMessage(
                    "TRADE_EXECUTED", symbol,
                    closeSide == model.OrderSide.BUY ? "BUY_EXECUTED" : "SELL_EXECUTED"
            );
            server.SessionManager.sendEventToCustomer(userId, event);
        }

        System.out.println("[서버] 로스컷 강제청산 - userId: " + userId + ", symbol: " + symbol + ", qty: " + p.getQty());
    }
    topInfoService.pushToUser(userId);
}

//TPSL 청산용도 / 직접처리방식임
public void closePosition(Position pos, double orderPrice, String reason) {

    LocalDateTime signalTime = LocalDateTime.now().withNano(0);

    OrderSide side = pos.isLong() ? OrderSide.SELL : OrderSide.BUY;

    double triggerPrice = "TP".equals(reason) ? pos.getTpPrice() : pos.getSlPrice();
    Integer tickCount = "TP".equals(reason) ? pos.getTpTicks() : pos.getSlTicks();

    double filledPrice = side == OrderSide.SELL
            ? Store.PriceStore.getBestBid(pos.getSymbol())
            : Store.PriceStore.getBestAsk(pos.getSymbol());

    LocalDateTime fillTime = LocalDateTime.now().withNano(0);

    int orderId = new OrderDAO().insertFilled(
            pos.getUserId(), pos.getSymbol(), side.name(),
            orderPrice, filledPrice,
            pos.getQty(), triggerPrice, pos.getAvgPrice(), true, reason, tickCount
    );

    if (orderId > 0) {
        model.TradeResult result = applyTrade(orderId, pos.getUserId(), pos.getSymbol(), side, filledPrice, pos.getQty(),
                false, 0, false, 0);   // 🔥 반환값 받음

        double margin = new service.UserDataDAO().getAvailableMargin(pos.getUserId());   // 🔥 조회

        String eventType = "TP".equals(reason) ? "익절(TP) 청산" : "손절(SL) 청산";
        logSignalAndFill(pos.getUserId(), pos.getSymbol(), side, String.valueOf(orderId),
                eventType, orderPrice, filledPrice, pos.getQty(), signalTime, fillTime, result, margin);   // 🔥 전달

        server.ClientEventMessage event = new server.ClientEventMessage(
                "TRADE_EXECUTED", pos.getSymbol(),
                side == model.OrderSide.BUY ? "BUY_EXECUTED" : "SELL_EXECUTED"
        );
        server.SessionManager.sendEventToCustomer(pos.getUserId(), event);

        topInfoService.pushToUser(pos.getUserId());

        System.out.println("[서버] " + reason + " 자동청산 - userId: " + pos.getUserId()
                + ", symbol: " + pos.getSymbol() + ", orderPrice: " + orderPrice + ", filledPrice: " + filledPrice
                + ", qty: " + pos.getQty() + ", ticks: " + tickCount);
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
                long ticks = Math.round(priceDiff / spec.getTickSize());
                double rate = Store.ExchangeRateCache.getRate(spec.getCurrency());
                long pnl = ticks * spec.getTickValueKrw(rate) * qty;

                result.add(new model.PositionRow(
                        rs.getInt("id"), symbol, avg, currentPrice,
                        displaySide, qty, String.format("%.2f", (double) pnl)
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }




    // 🔥 이 클래스 전용 (OrderExecutionService와 중복 구현)
    //TP 익절 청산될 때
    //SL 손절 청산될 때
    //로스컷(강제청산)될 때
    private void logSignalAndFill(int userId, String symbol, OrderSide side, String orderId,
                                  String eventType, double orderPrice, double filledPrice, int qty,
                                  LocalDateTime signalTime, LocalDateTime fillTime,
                                  model.TradeResult result, double margin) {

        String exchange = service.OrderAuditDAO.resolveExchange(symbol);
        String symbolKor = service.UserDataDAO.symbolToKor(symbol);
        String sideKor = (side == OrderSide.BUY) ? "매수" : "매도";

        model.OrderAuditLog signalLog = new model.OrderAuditLog();
        signalLog.time = signalTime;
        signalLog.eventType = eventType;
        signalLog.userId = userId;
        signalLog.exchange = exchange;
        signalLog.server = "신호";
        signalLog.orderId = orderId;
        signalLog.symbol = symbol;
        signalLog.orderPrice = orderPrice;
        signalLog.filledPrice = 0;
        signalLog.side = side.name();
        signalLog.qty = qty;

        // 🔥 DB상 실제 미체결 + 지금 막 트리거된 이 청산 자신을 합쳐서 스냅샷 구성
        String existingSnapshot = service.OrderAuditDAO.buildOpenOrderSnapshot(userId);
        String selfEntry = symbolKor + ":" + sideKor + qty + "(" + orderPrice + ")";
        signalLog.openOrderSnapshot = existingSnapshot.isEmpty()
                ? selfEntry
                : existingSnapshot + ", " + selfEntry;

        orderAuditDAO.insertLog(signalLog);

        String positionSnapshot = service.OrderAuditDAO.buildPositionSnapshot(userId);

        model.OrderAuditLog fillLog = new model.OrderAuditLog();
        fillLog.time = fillTime;
        fillLog.eventType = eventType;
        fillLog.userId = userId;
        fillLog.exchange = exchange;
        fillLog.server = "체결";
        fillLog.orderId = orderId;
        fillLog.symbol = symbol;
        fillLog.orderPrice = 0;
        fillLog.filledPrice = filledPrice;
        fillLog.side = side.name();
        fillLog.qty = qty;
        fillLog.fee = result.fee;
        fillLog.pnl = result.realizedPnl;
        fillLog.margin = margin;
        fillLog.positionSnapshot = positionSnapshot;
        orderAuditDAO.insertLog(fillLog);
    }

    // 🔥 소수점 4째자리에서 반올림
    private double round4(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }


}