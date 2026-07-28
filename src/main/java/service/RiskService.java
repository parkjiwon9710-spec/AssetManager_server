package service;

import Market.MarketSpec;
import Market.MarketSpecCache;
import db.DBUtil;
import model.OrderSide;
import model.Position;
import model.SystemTradeMode;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Set;

//로스컷 로직 제외한 트리밍 버전
public class RiskService {

    private final UserService userService = new UserService();
    private final PositionService positionService = new PositionService();
    private final SystemTradeModeDAO modeDAO = new SystemTradeModeDAO();
    private final SymbolTradeSettingDAO settingDAO = new SymbolTradeSettingDAO();
    private final Set<Integer> liquidatingUsers = java.util.concurrent.ConcurrentHashMap.newKeySet();

    private int getMaxQtyFromDB(int userId, String symbol, OrderSide side) {

        MarketSpec spec = MarketSpecCache.get(symbol);

        if ("OVERSEAS_FUTURES".equals(spec.getMarketType())) {
            // 기존 그대로, side 안 씀 (해외선물은 매수/매도 구분 없다고 가정)
            String sql = "SELECT max_qty FROM user_overseas_qty_limits WHERE user_id=? AND symbol=?";
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setString(2, symbol);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int qty = rs.getInt("max_qty");
                    if (rs.wasNull()) return getDefaultOverseasQty(userId);
                    return qty;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return getDefaultOverseasQty(userId);
        }

        if ("DOMESTIC_FUTURES".equals(spec.getMarketType())) {
            return getDefaultFutureQty(userId);
        }

        // 🔥 옵션은 매수/매도 컬럼을 구분해서 읽기
        if ("OPTIONS".equals(spec.getMarketType())) {
            return side == OrderSide.BUY
                    ? getDefaultOptionBuyQty(userId)
                    : getDefaultOptionSellQty(userId);
        }

        return 0;
    }
/// ////////옵션/////////
    private int getDefaultOptionBuyQty(int userId) {
        String sql = "SELECT max_options_buy_qty FROM user_qty_limits WHERE user_id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("max_options_buy_qty");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getDefaultOptionSellQty(int userId) {
        String sql = "SELECT max_options_sell_qty FROM user_qty_limits WHERE user_id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("max_options_sell_qty");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    /// ////////국선/////////
    private int getDefaultFutureQty(int userId) {

        String sql = "SELECT max_futures_qty FROM user_qty_limits WHERE user_id=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("max_futures_qty");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
    /// ////////해선/////////
    private int getDefaultOverseasQty(int userId) {

        String sql = "SELECT max_overseas_qty FROM user_qty_limits WHERE user_id=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("max_overseas_qty");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public int calcMaxBuyQty(int userId, String symbol, int pendingBuyQty) {

        Position pos = positionService.getPosition(userId, symbol);
        int qty = calcMaxOrderQty(userId, symbol, OrderSide.BUY);   // 🔥 side 추가

        if (pos != null && !pos.isLong()) {
            qty += Math.abs(pos.getQty());
        }

        return Math.max(qty - pendingBuyQty, 0);
    }

    public int calcMaxSellQty(int userId, String symbol, int pendingSellQty) {

        Position pos = positionService.getPosition(userId, symbol);
        int qty = calcMaxOrderQty(userId, symbol, OrderSide.SELL);   // 🔥 side 추가

        if (pos != null && pos.isLong()) {
            qty += Math.abs(pos.getQty());
        }

        return Math.max(qty - pendingSellQty, 0);
    }

    public int calcMaxOrderQty(int userId, String symbol, OrderSide side) {   // 🔥 파라미터 추가

        User user = userService.getUserById(userId);
        if (user == null) return 0;

        long balance = (long) user.getBalance();

        long usedMargin = positionService
                .getAllPositions(userId)
                .stream()
                .mapToLong(p -> Math.abs(p.getQty()) * getEntryMargin(p.getSymbol()))
                .sum();

        long availableMargin = balance - usedMargin;
        if (availableMargin <= 0) return 0;

        long entryMargin = getEntryMargin(symbol);

        if (entryMargin <= 0) {
            System.out.println("entryMargin 설정 오류 : " + symbol);
            return 0;
        }

        int maxByMargin = (int) (availableMargin / entryMargin);
        int maxByDB = getMaxQtyFromDB(userId, symbol, side);   // 🔥 side 추가

        Position pos = positionService.getPosition(userId, symbol);
        if (pos != null) {
            maxByDB -= Math.abs(pos.getQty());
        }

        return Math.max(Math.min(maxByMargin, maxByDB), 0);
    }

    // 서버 측 최종 검증용 - 주문 요청이 실제로 가능한지 확인
    public boolean canPlaceOrder(int userId, String symbol, OrderSide side, int orderQty) {
        if (orderQty <= 0) return false;

        Position pos = positionService.getPosition(userId, symbol);

        int exitQty = 0;
        int entryQty = orderQty;

        if (pos != null && pos.getQty() != 0) {
            boolean opposite =
                    (pos.isLong() && side == OrderSide.SELL) ||
                            (!pos.isLong() && side == OrderSide.BUY);
            if (opposite) {
                exitQty = Math.min(Math.abs(pos.getQty()), orderQty);
                entryQty = orderQty - exitQty;
            }
        }

        if (entryQty <= 0) return true;

        int maxQty = calcMaxOrderQty(userId, symbol, side);   // 🔥 side 추가
        return entryQty <= maxQty;
    }

    private long getEntryMargin(String symbol) {

        SystemTradeMode mode = modeDAO.getSettings();

        if (mode != null && "PER_SYMBOL".equals(mode.getOverseasEntryMarginMode())) {
            return settingDAO.getEntryMargin(symbol);
        }

        MarketSpec spec = MarketSpecCache.get(symbol);
        return spec.getEntryMargin();
    }




    /// ////////로스컷관련메소드추가////////////
    /// //가담보금이 유지증거금 이하인지 판단
    public boolean shouldForceLiquidate(int userId) {

        User user = userService.getUserById(userId);
        if (user == null) return false;

        double unrealized = positionService.getTotalRealtimePnl(userId);
        long evaluatedCollateral = (long) (user.getBalance() + unrealized);

        long maintenanceMargin = positionService
                .getAllPositions(userId)
                .stream()
                .mapToLong(p -> Math.abs(p.getQty()) * getMaintMargin(p.getSymbol()))
                .sum();

        if (maintenanceMargin == 0) return false;

        return evaluatedCollateral <= maintenanceMargin;
    }
/// /////////////중복청산방지////////////
    public boolean tryStartLiquidation(int userId) {
        return liquidatingUsers.add(userId);
    }

    public void finishLiquidation(int userId) {
        liquidatingUsers.remove(userId);
    }

    public boolean isLiquidating(int userId) {
        return liquidatingUsers.contains(userId);
    }
    /// /////////////중복청산방지////////////

    private long getMaintMargin(String symbol) {

        SystemTradeMode mode = modeDAO.getSettings();

        if (mode != null && "PER_SYMBOL".equals(mode.getOverseasMaintMarginMode())) {
            return settingDAO.getMaintMargin(symbol);
        }

        MarketSpec spec = MarketSpecCache.get(symbol);
        return spec.getMaintMargin();
    }
    ///
    ///   /// /////////////////////////////////////////////////////////////////////////////////로스컷관련메소드추가끝
}