package service;

import db.DBUtil;
import model.TradeHistoryRow;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class  TradeHistoryDAO {

    public void insert(
            int orderId,
            int userId,
            String partnerUsername,
            String symbol,
            String side,
            double price,
            int qty,
            double realizedPnl,
            double fee
    ) {

        String sql =
                "INSERT INTO trade_history " +
                        "(order_id, user_id, partner_username, symbol, side, price, qty, realized_pnl, fee) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ps.setInt(2, userId);
            ps.setString(3, partnerUsername);
            ps.setString(4, symbol);
            ps.setString(5, side);
            ps.setDouble(6, price);
            ps.setInt(7, qty);
            ps.setDouble(8, realizedPnl);
            ps.setDouble(9, fee);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public List<TradeHistoryRow> loadTradeHistory(
            int userId,
            Timestamp start,
            Timestamp end,
            String symbol
    ) {

        List<TradeHistoryRow> list = new ArrayList<>();


        StringBuilder sql = new StringBuilder(

                "SELECT " +

                        "DATE(o.created_at) AS trade_date, " +
                        "TIME(o.created_at) AS trade_time, " +

                        "o.symbol, " +
                        "o.side, " +
                        "o.status, " +
                        "o.order_type, " +

                        "o.qty AS order_qty, " +
                        "t.qty AS filled_qty, " +

                        "o.price AS order_price, " +
                        "t.price AS filled_price, " +

                        "t.realized_pnl, " +
                        "t.fee, " +

                        "o.tp_sl_type, " +
                        "o.trigger_price," +
                        "o.position_price, " +
                        "o.tick_count " +

                        "FROM orders o " +

                        "LEFT JOIN trade_history t " +
                        "ON o.id = t.order_id " +

                        "WHERE o.user_id=? "
        );


        if(symbol != null && !symbol.equals("전체")){
            sql.append(" AND o.symbol=? ");
        }


        sql.append(
                " AND o.created_at >= ? " +
                        " AND o.created_at < ? " +
                        " ORDER BY o.created_at DESC"
        );



        try(
                Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())
        ){

            int idx = 1;


            ps.setInt(idx++, userId);



            if(symbol != null && !symbol.equals("전체")){
                ps.setString(idx++, symbol);
            }


            /*
             * 한국시간 보정
             *
             * UI 선택:
             * 2026-07-14 00:00 ~ 23:59
             *
             * DB:
             * UTC 기준이면 -9시간
             *
             * 그래서 start/end millis는
             * 이미 KST 기준으로 보내고
             * 여기서 Timestamp 변환하면 됨
             */

            ps.setTimestamp(idx++, start);
            ps.setTimestamp(idx++, end);



            ResultSet rs = ps.executeQuery();


            while(rs.next()){


                TradeHistoryRow row = new TradeHistoryRow();


                row.setDate(
                        rs.getString("trade_date")
                );

                row.setTime(
                        rs.getString("trade_time")
                );


                row.setSymbol(
                        rs.getString("symbol")
                );


                row.setSide(
                        convertSide(rs.getString("side"))
                );

                row.setStatus(
                        convertStatus(rs.getString("status"))
                );

                row.setType(
                        convertType(rs.getString("order_type"))
                );

                row.setOrderQty(
                        rs.getInt("order_qty")
                );


                Integer filledQty = rs.getObject("filled_qty", Integer.class);

                row.setFilledQty(
                        filledQty
                );



                row.setOrderPrice(
                        rs.getDouble("order_price")
                );


                row.setFilledPrice(
                        rs.getDouble("filled_price")
                );


                row.setPnl(
                        rs.getDouble("realized_pnl")
                );


                row.setFee(
                        rs.getDouble("fee")
                );


                Double pnl = row.getPnl();
                Double fee = row.getFee();


                if(pnl != null && fee != null){
                    row.setTotalPnl(
                            pnl - fee
                    );
                }



                row.setRemark(
                        createRemark(
                                rs.getString("order_type"),
                                rs.getString("tp_sl_type"),
                                rs.getObject("trigger_price", Double.class),
                                rs.getObject("position_price", Double.class),
                                rs.getObject("tick_count", Integer.class)
                        )
                );


                list.add(row);
            }


        }catch(Exception e){

            e.printStackTrace();

        }


        return list;
    }

    //매수/매도
    private String convertSide(String side){

        if("BUY".equals(side))
            return "매수";

        if("SELL".equals(side))
            return "매도";

        return side;
    }
    //상태
    private String convertStatus(String status){

        return switch(status){

            case "FILLED" -> "체결";

            case "CANCELED" -> "취소";

            case "PENDING" -> "미체결";

            default -> status;

        };
    }

    //유형
    private String convertType(String type){

        return switch(type){

            case "MARKET" -> "시장가";

            case "LIMIT" -> "지정가";

            case "MIT" -> "시장가";

            case "STOP" -> "STOP";

            default -> type;

        };
    }

    private String createRemark(
            String orderType,
            String tpSlType,
            Double triggerPrice,
            Double positionPrice,
            Integer tickCount
    ){

        // MIT
        if ("MIT".equals(orderType)) {
            return "MIT(예약가:" + triggerPrice + ")";
        }

        // TP
        if ("TP".equals(tpSlType)) {
            return "익절(틱:" + tickCount
                    + " / 진입평단가:" + positionPrice + ")";
        }

        // SL
        if ("SL".equals(tpSlType)) {
            return "손절(틱:" + tickCount
                    + " / 진입평단가:" + positionPrice + ")";
        }

        // 로스컷
        if ("LIQUIDATION".equals(tpSlType)) {
            return "로스컷";
        }

        return "-";
    }

    public double getTotalRealizedPnl(int userId) {
        String sql =
                "SELECT IFNULL(SUM(realized_pnl),0) " +
                        "FROM trade_history WHERE user_id=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double getTotalNetPnl(int userId) {

        String sql =
                "SELECT IFNULL(SUM(realized_pnl - fee),0) " +
                        "FROM trade_history WHERE user_id=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next())
                return rs.getDouble(1);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    // 당일 실현손익 (realized_pnl - fee)
    public double getTodayNetPnl(int userId) {
        String sql =
                "SELECT IFNULL(SUM(realized_pnl - fee), 0) " +
                        "FROM trade_history " +
                        "WHERE user_id = ? " +
                        "AND DATE(DATE_SUB(created_at, INTERVAL 7 HOUR)) = " +
                        "    DATE(DATE_SUB(NOW(), INTERVAL 7 HOUR))";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return rs.getDouble(1);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 당일 수수료
    public double getTodayFee(int userId) {
        String sql =
                "SELECT IFNULL(SUM(fee), 0) " +
                        "FROM trade_history " +
                        "WHERE user_id = ? " +
                        "AND DATE(DATE_SUB(created_at, INTERVAL 7 HOUR)) = " +
                        "    DATE(DATE_SUB(NOW(), INTERVAL 7 HOUR))";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return rs.getDouble(1);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
//당일승률  실시간손익현황용
    public double getTodayWinRate(int userId) {

        String sql =
                "SELECT " +
                        "SUM(CASE WHEN realized_pnl > 0 THEN 1 ELSE 0 END) win_count, " +
                        "COUNT(*) total_count " +
                        "FROM trade_history " +
                        "WHERE user_id=? " +
                        "AND realized_pnl <> 0 " +
                        "AND DATE(created_at)=CURDATE()";


        try(Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {


            ps.setInt(1,userId);


            ResultSet rs = ps.executeQuery();


            if(rs.next()) {

                int win =
                        rs.getInt("win_count");

                int total =
                        rs.getInt("total_count");


                if(total == 0)
                    return 0;


                return Math.round(((double) win / total) * 100);
            }


        } catch(Exception e){
            e.printStackTrace();
        }


        return 0;
    }

}
