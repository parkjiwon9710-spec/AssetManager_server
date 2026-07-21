package service;

import model.RealtimePnlRow;
import db.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RealtimePnlService {

    private final TradeHistoryDAO tradeHistoryDAO = new TradeHistoryDAO();
    private final PositionService positionService = new PositionService();

    public List<RealtimePnlRow> compute() {

        List<RealtimePnlRow> result = new ArrayList<>();

        double totalRealtime = 0, totalNetPnl = 0, totalFee = 0, totalCollateral = 0;
        int totalCount = 0;

        String sql =
                "SELECT u.id, u.name, u.username, " +
                        "       a.customer_grade, p.recommender, a.server, u.balance " +
                        "FROM users u " +
                        "LEFT JOIN user_account_status a ON a.user_id = u.id " +
                        "LEFT JOIN user_profiles p       ON p.user_id = u.id " +
                        "LEFT JOIN user_status s         ON s.user_id = u.id " +
                        "WHERE u.role = 'USER' " +
                        "AND u.account_type = 'REAL' " +
                        "AND s.last_login IS NOT NULL " +
                        "AND DATE(DATE_SUB(s.last_login, INTERVAL 7 HOUR)) = " +
                        "    DATE(DATE_SUB(NOW(), INTERVAL 7 HOUR)) " +
                        "ORDER BY u.name";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int uid = rs.getInt("id");

                double netPnl = tradeHistoryDAO.getTodayNetPnl(uid);
                double fee = tradeHistoryDAO.getTodayFee(uid);
                double unrealized = positionService.getTotalRealtimePnl(uid);
                double realtime = netPnl + unrealized;
                double winRate = tradeHistoryDAO.getTodayWinRate(uid);
                double collateral = rs.getLong("balance") + unrealized;

                totalRealtime += realtime;
                totalNetPnl += netPnl;
                totalFee += fee;
                totalCollateral += collateral;
                totalCount++;

                RealtimePnlRow row = new RealtimePnlRow();
                row.setName(rs.getString("name"));
                row.setUsername(rs.getString("username"));
                row.setGrade(rs.getString("customer_grade"));
                row.setRecommender(rs.getString("recommender"));
                row.setRealtime(realtime);
                row.setNetPnl(netPnl);
                row.setFee(fee);
                row.setCollateral(collateral);
                row.setWinRate(winRate);
                row.setServer(rs.getString("server"));
                result.add(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        RealtimePnlRow total = new RealtimePnlRow();
        total.setTotal(true);
        total.setName("오늘접속 " + totalCount + "명");
        total.setRealtime(totalRealtime);
        total.setNetPnl(totalNetPnl);
        total.setFee(totalFee);
        total.setCollateral(totalCollateral);
        result.add(0, total);

        return result;
    }
}