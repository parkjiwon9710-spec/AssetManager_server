package service;

import db.DBUtil;

import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserStatusDAO {

    public void updateLastLogin(int userId) {

        String sql =
                "UPDATE user_status " +
                        "SET last_login = NOW(), " +
                        "    is_online = 1 " +
                        "WHERE user_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // 로그인 실패 시 카운트 증가 (username 기준 - 인증 전이라 userId를 모름)
    public void incrementLoginFailCount(String username) {
        String sql = """
        UPDATE user_status
        SET login_fail_count = login_fail_count + 1
        WHERE user_id = (SELECT id FROM users WHERE username = ?)
    """;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // 로그인 성공 시 카운트 리셋
    public void resetLoginFailCount(int userId) {
        String sql = "UPDATE user_status SET login_fail_count = 0 WHERE user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void updateTradeStats(
            int userId,
            double pnl,
            double fee,
            Timestamp tradeTime
    ) {

        String sql =
                """
                UPDATE user_status
                SET
                    total_pnl = total_pnl + ?,
                    total_fee = total_fee + ?,
                    trade_count = trade_count + 1,

                    trade_days =
                        CASE
                            WHEN last_trade_time IS NULL
                                THEN trade_days + 1

                            WHEN DATE(DATE_SUB(last_trade_time, INTERVAL 7 HOUR))
                                 <>
                                 DATE(DATE_SUB(?, INTERVAL 7 HOUR))
                                THEN trade_days + 1

                            ELSE trade_days
                        END,

                    last_trade_time = ?

                WHERE user_id = ?
                """;

        try(Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, pnl);
            ps.setDouble(2, fee);
            ps.setTimestamp(3, tradeTime);
            ps.setTimestamp(4, tradeTime);
            ps.setInt(5, userId);

            ps.executeUpdate();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void updateWinRate(int userId, boolean win) {

        String sql1 =
                """
                UPDATE user_status
                SET
                    win_count = win_count + ?,
                    lose_count = lose_count + ?
                WHERE user_id = ?
                """;

        String sql2 =
                """
                UPDATE user_status
                SET total_winrate =
                    ROUND(
                        win_count * 100.0 /
                        NULLIF(win_count + lose_count, 0)
                    )
                WHERE user_id = ?
                """;

        try (Connection conn = DBUtil.getConnection()) {

            int winInc = win ? 1 : 0;
            int loseInc = win ? 0 : 1;

            PreparedStatement ps1 = conn.prepareStatement(sql1);
            ps1.setInt(1, winInc);
            ps1.setInt(2, loseInc);
            ps1.setInt(3, userId);
            ps1.executeUpdate();

            PreparedStatement ps2 = conn.prepareStatement(sql2);
            ps2.setInt(1, userId);
            ps2.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isAutoOvernight(int userId) {

        String sql =
                "SELECT auto_overnight " +
                        "FROM user_account_status " +
                        "WHERE user_id = ?";

        try (
                Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getBoolean("auto_overnight");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean isOvernightPermitted(int userId) {

        String sql =
                "SELECT overnight_setting " +
                        "FROM user_account_status " +
                        "WHERE user_id = ?";

        try (
                Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getBoolean("overnight_setting");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 없으면 기본 미허용(안전 쪽으로)
        return false;
    }


    public void updateAutoOvernight(
            int userId,
            boolean enabled
    ) {

        String sql =
                "UPDATE user_account_status " +
                        "SET auto_overnight = ? " +
                        "WHERE user_id = ?";

        try (
                Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setBoolean(1, enabled);
            ps.setInt(2, userId);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}