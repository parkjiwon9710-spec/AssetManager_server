package service;

import model.AdminUserAccountData;
import db.DBUtil;

import java.sql.*;

public class AdminUserAccountService {

    public AdminUserAccountData loadAccountData(String username) {

        String sql = """
            SELECT
                uas.account_status, uas.server,
                us.is_online, us.last_login,
                up.join_ip, up.join_mac,
                us.login_fail_count, us.last_trade_time,
                us.trade_count, us.trade_days,
                us.total_pnl, us.total_fee, us.total_winrate,
                us.mileage,
                up.memo_customer
            FROM users u
            LEFT JOIN user_account_status uas ON u.id = uas.user_id
            LEFT JOIN user_status us ON u.id = us.user_id
            LEFT JOIN user_profiles up ON u.id = up.user_id
            WHERE u.username = ?
        """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                AdminUserAccountData data = new AdminUserAccountData();
                data.setAccountStatus(rs.getString("account_status") == null ? "정상" : rs.getString("account_status"));
                data.setServer(rs.getString("server") == null ? "" : rs.getString("server"));
                data.setOnlineStatus(rs.getBoolean("is_online") ? "온라인" : "오프라인");
                data.setLastLogin(rs.getTimestamp("last_login") == null ? "-" : rs.getTimestamp("last_login").toString());
                data.setJoinIp(rs.getString("join_ip") == null ? "" : rs.getString("join_ip"));
                data.setJoinMac(rs.getString("join_mac") == null ? "" : rs.getString("join_mac"));
                data.setLoginFailCount(String.valueOf(rs.getInt("login_fail_count")));
                data.setLastTradeTime(rs.getTimestamp("last_trade_time") == null ? "-" : rs.getTimestamp("last_trade_time").toString());
                data.setTradeCount(String.valueOf(rs.getInt("trade_count")));
                data.setTradeDays(String.valueOf(rs.getInt("trade_days")));
                data.setTotalPnl(String.valueOf(rs.getDouble("total_pnl")));
                data.setTotalFee(String.valueOf(rs.getDouble("total_fee")));
                data.setTotalWinrate(String.valueOf(rs.getInt("total_winrate")));
                data.setMileage(String.valueOf(rs.getLong("mileage")));
                data.setMemoCustomer(rs.getString("memo_customer") == null ? "" : rs.getString("memo_customer"));

                return data;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String saveAccountData(String username, String accountStatus, String server,
                                  String mileage, String memo) {

        String sql1 = """
            UPDATE user_account_status
            SET account_status=?, server=?
            WHERE user_id = (SELECT id FROM users WHERE username=?)
        """;

        String sql2 = """
            UPDATE user_status SET mileage=?
            WHERE user_id = (SELECT id FROM users WHERE username=?)
        """;

        String sql3 = """
            UPDATE user_profiles SET memo_customer=?
            WHERE user_id = (SELECT id FROM users WHERE username=?)
        """;

        try (Connection conn = DBUtil.getConnection()) {

            conn.setAutoCommit(false);

            try (PreparedStatement ps1 = conn.prepareStatement(sql1)) {
                ps1.setString(1, accountStatus);
                ps1.setString(2, server);
                ps1.setString(3, username);
                ps1.executeUpdate();
            }

            try (PreparedStatement ps2 = conn.prepareStatement(sql2)) {
                ps2.setLong(1, mileage == null || mileage.isEmpty() ? 0 : Long.parseLong(mileage));
                ps2.setString(2, username);
                ps2.executeUpdate();
            }

            try (PreparedStatement ps3 = conn.prepareStatement(sql3)) {
                ps3.setString(1, memo);
                ps3.setString(2, username);
                ps3.executeUpdate();
            }

            conn.commit();
            return null; // 성공

        } catch (SQLException e) {
            e.printStackTrace();
            return "저장 중 오류가 발생했습니다";
        } catch (NumberFormatException e) {
            return "마일리지는 숫자만 입력하세요";
        }
    }
}
