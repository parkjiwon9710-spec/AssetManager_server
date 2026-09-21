package service;

import db.DBUtil;
import model.OrderAuditLog;
import model.OrderAuditRow;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderAuditDAO {

    public void insertLog(OrderAuditLog log) {

        String sql = """
            INSERT INTO order_audit_log
            (time,event_type,user_id,user_name,operator,exchange_name,server_name,
             order_id,symbol,order_price,filled_price,side,qty,fee,pnl,leverage,margin,
             position_snapshot,open_order_snapshot)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
            """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(log.time));
            ps.setString(2, log.eventType);
            ps.setInt(3, log.userId);
            ps.setString(4, log.userName);
            ps.setString(5, log.operator);
            ps.setString(6, log.exchange);
            ps.setString(7, log.server);
            ps.setString(8, log.orderId);
            ps.setString(9, log.symbol);
            ps.setDouble(10, log.orderPrice);
            ps.setDouble(11, log.filledPrice);
            ps.setString(12, log.side);
            ps.setInt(13, log.qty);
            ps.setDouble(14, log.fee);
            ps.setDouble(15, log.pnl);
            ps.setDouble(16, log.leverage);
            ps.setDouble(17, log.margin);
            ps.setString(18, log.positionSnapshot);
            ps.setString(19, log.openOrderSnapshot);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 거래소 매핑 - 심볼 기준
    public static String resolveExchange(String symbol) {
        if ("HSI".equals(symbol)) return "홍콩거래소";
        if ("KOSPI200".equals(symbol)) return "한국거래소";
        return "CME";
    }

    // 🔥 신규: 영업일 기준 조회
    public List<OrderAuditRow> loadLogsByTradingDay(int userId, LocalDate date) {

        List<OrderAuditRow> result = new ArrayList<>();

        String sql = """
            SELECT *
            FROM order_audit_log
            WHERE user_id = ?
            AND time >= ?
            AND time < ?
            ORDER BY time DESC
        """;

        LocalDateTime start = date.atTime(7, 0);
        LocalDateTime end = date.plusDays(1).atTime(7, 0);

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setTimestamp(2, Timestamp.valueOf(start));
            ps.setTimestamp(3, Timestamp.valueOf(end));

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                OrderAuditRow row = new OrderAuditRow();
                row.time = rs.getTimestamp("time").toLocalDateTime().toString();   // 🔥 문자열로 변환
                row.eventType = rs.getString("event_type");
                row.userName = rs.getString("user_name");
                row.operator = rs.getString("operator");
                row.exchange = rs.getString("exchange_name");
                row.server = rs.getString("server_name");
                row.orderId = rs.getString("order_id");
                row.symbol = rs.getString("symbol");
                row.orderPrice = rs.getDouble("order_price");
                row.filledPrice = rs.getDouble("filled_price");
                row.side = rs.getString("side");
                row.qty = rs.getInt("qty");
                row.fee = rs.getDouble("fee");
                row.pnl = rs.getDouble("pnl");
                row.leverage = rs.getDouble("leverage");
                row.margin = rs.getDouble("margin");
                row.positionSnapshot = rs.getString("position_snapshot");
                row.openOrderSnapshot = rs.getString("open_order_snapshot");
                result.add(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
//주문기록 중 유저의 미체결포지션 조회메소드
public static String buildOpenOrderSnapshot(int userId) {
    List<model.Order> pending = new OrderDAO().findPendingByUser(userId);   // 🔥 기존 메서드 재사용

    java.util.Map<String, java.util.Map<Double, Integer>> buyBySymbolPrice = new java.util.TreeMap<>();
    java.util.Map<String, java.util.Map<Double, Integer>> sellBySymbolPrice = new java.util.TreeMap<>();

    for (model.Order o : pending) {
        double price = "LIMIT".equals(o.getOrderType()) ? o.getOrderPrice() : o.getTriggerPrice();
        var target = "BUY".equals(o.getSide()) ? buyBySymbolPrice : sellBySymbolPrice;
        target.computeIfAbsent(o.getSymbol(), k -> new java.util.TreeMap<>())
                .merge(price, o.getQty(), Integer::sum);
    }

    StringBuilder sb = new StringBuilder();

    for (var symbolEntry : buyBySymbolPrice.entrySet()) {
        String symbolKor = UserDataDAO.symbolToKor(symbolEntry.getKey());
        for (var priceEntry : symbolEntry.getValue().entrySet()) {
            if (sb.length() > 0) sb.append(" / ");
            sb.append(symbolKor).append(":매수").append(priceEntry.getValue()).append("(").append(priceEntry.getKey()).append(")");
        }
    }
    for (var symbolEntry : sellBySymbolPrice.entrySet()) {
        String symbolKor = UserDataDAO.symbolToKor(symbolEntry.getKey());
        for (var priceEntry : symbolEntry.getValue().entrySet()) {
            if (sb.length() > 0) sb.append(" / ");
            sb.append(symbolKor).append(":매도").append(priceEntry.getValue()).append("(").append(priceEntry.getKey()).append(")");
        }
    }

    return sb.toString();
}


//주문기록 중 유저의 체결포지션 조회메소드
    public static String buildPositionSnapshot(int userId) {
        List<model.Position> positions = new service.PositionDAO().findAllByUser(userId);

        StringBuilder sb = new StringBuilder();
        for (model.Position p : positions) {
            if (p.getQty() <= 0) continue;
            String symbolKor = UserDataDAO.symbolToKor(p.getSymbol());
            String sideKor = "LONG".equals(p.getDirection()) ? "매수" : "매도";
            if (sb.length() > 0) sb.append(" / ");
            sb.append(symbolKor).append(":").append(sideKor).append(p.getQty())
                    .append("(").append(round4(p.getAvgPrice())).append(")");
        }
        return sb.toString();
    }

    // round4가 OrderAuditDAO에 없으면 추가 필요
    private static double round4(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }


    //취소로그 하나 남기는 메소드 공통헬퍼임
    public static void logCancel(model.Order o) {
        if (o == null) return;

        String orderTypeKor = "LIMIT".equals(o.getOrderType()) ? "지정가" : "MIT";
        String sideKor = "BUY".equals(o.getSide()) ? "매수" : "매도";
        double price = "LIMIT".equals(o.getOrderType()) ? o.getOrderPrice() : o.getTriggerPrice();

        model.OrderAuditLog cancelLog = new model.OrderAuditLog();
        cancelLog.time = java.time.LocalDateTime.now().withNano(0);
        cancelLog.eventType = orderTypeKor + " " + sideKor;
        cancelLog.userId = o.getUserId();
        cancelLog.exchange = resolveExchange(o.getSymbol());
        cancelLog.server = "취소";
        cancelLog.orderId = String.valueOf(o.getId());
        cancelLog.symbol = o.getSymbol();
        cancelLog.orderPrice = "LIMIT".equals(o.getOrderType()) ? o.getOrderPrice() : 0;   // 🔥 MIT는 항상 0
        cancelLog.side = o.getSide();
        cancelLog.qty = o.getQty();
        // 🔥 취소 이후 남은 미체결 합계 (단, 아직 실제 DB에서 취소 반영 전에 호출하면 이 건까지 포함되니 주의)
        cancelLog.openOrderSnapshot = buildOpenOrderSnapshot(o.getUserId());
        new OrderAuditDAO().insertLog(cancelLog);
    }

/// ///입출금관련로그찍는거
    public static void insertDwAuditLog(int userId, String userName, String operator, String eventType, double margin) {
        model.OrderAuditLog log = new model.OrderAuditLog();
        log.time = java.time.LocalDateTime.now().withNano(0);
        log.userId = userId;
        log.userName = userName;
        log.operator = operator;
        log.eventType = eventType;
        log.margin = margin;
        new OrderAuditDAO().insertLog(log);
    }

    public static String formatAmount(double amount) {
        return String.format("%,.0f", amount);
    }
    /// /////////////////////////


}