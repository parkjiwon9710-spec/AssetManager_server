package service;

import model.AdminUserBasicInfo;
import db.DBUtil;

import java.sql.*;

public class AdminUserBasicService {

    public AdminUserBasicInfo loadBasicInfo(String username) {

        String sql = """
            SELECT
                u.created_at, u.username, u.name, u.password, up.email,
                up.phone, up.recommender, u.role, uas.customer_grade,
                up.memo_partner, up.bank, up.account_number, up.account_holder,
                up.deposit_account, uas.overnight_setting
            FROM users u
            LEFT JOIN user_profiles up ON u.id = up.user_id
            LEFT JOIN user_account_status uas ON u.id = uas.user_id
            LEFT JOIN user_fee_settings ufs ON u.id = ufs.user_id
            LEFT JOIN user_qty_limits uql ON u.id = uql.user_id
            WHERE u.username = ?
        """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                AdminUserBasicInfo info = new AdminUserBasicInfo();
                info.setCreatedAt(rs.getTimestamp("created_at") == null ? "-" : rs.getTimestamp("created_at").toString());
                info.setUsername(rs.getString("username"));
                info.setName(rs.getString("name") == null ? "" : rs.getString("name"));
                info.setPassword(rs.getString("password") == null ? "" : rs.getString("password"));
                info.setEmail(rs.getString("email") == null ? "" : rs.getString("email"));
                info.setPhone(rs.getString("phone") == null ? "" : rs.getString("phone"));
                info.setRecommender(rs.getString("recommender") == null ? "" : rs.getString("recommender"));
                info.setRole(rs.getString("role") == null ? "" : rs.getString("role"));
                info.setCustomerGrade(rs.getString("customer_grade") == null ? "" : rs.getString("customer_grade"));
                info.setMemoPartner(rs.getString("memo_partner") == null ? "" : rs.getString("memo_partner"));
                info.setBank(rs.getString("bank") == null ? "" : rs.getString("bank"));
                info.setAccountNumber(rs.getString("account_number") == null ? "" : rs.getString("account_number"));
                info.setAccountHolder(rs.getString("account_holder") == null ? "" : rs.getString("account_holder"));
                info.setDepositAccount(rs.getString("deposit_account") == null ? "" : rs.getString("deposit_account"));
                info.setOvernightSetting(rs.getBoolean("overnight_setting") ? "허용" : "미허용");
                info.setRemoteControl("미허용"); // DB컬럼 추가 전까지 기본값

                return info;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String saveBasicInfo(String username, String name, String password, String email, String phone,
                                String recommender, String grade, String partnerMemo, String bank,
                                String accountNumber, String accountHolder, String depositAccount,
                                String overnight, String remote) {

        String sql1 = "UPDATE users SET name=?, password=? WHERE username=?";
        String sql2 = """
            UPDATE user_profiles SET email=?, phone=?, recommender=?,
            memo_partner=?, bank=?, account_number=?, account_holder=?, deposit_account=?
            WHERE user_id = (SELECT id FROM users WHERE username=?)
        """;
        String sql3 = """
            UPDATE user_account_status
            SET customer_grade=?, overnight_setting=?
            WHERE user_id = (SELECT id FROM users WHERE username=?)
        """;
        String getUserIdSql = "SELECT id FROM users WHERE username=?";
        String closeHistorySql = "UPDATE user_partner_history SET end_time = NOW() WHERE user_id=? AND end_time IS NULL";
        String insertHistorySql = "INSERT INTO user_partner_history (user_id, partner_username, start_time, end_time) VALUES (?, ?, NOW(), NULL)";

        try (Connection conn = DBUtil.getConnection()) {

            conn.setAutoCommit(false);

            try (PreparedStatement ps1 = conn.prepareStatement(sql1)) {
                ps1.setString(1, name);
                ps1.setString(2, password);
                ps1.setString(3, username);
                ps1.executeUpdate();
            }

            try (PreparedStatement ps2 = conn.prepareStatement(sql2)) {
                ps2.setString(1, email);
                ps2.setString(2, phone);
                ps2.setString(3, recommender);
                ps2.setString(4, partnerMemo);
                ps2.setString(5, bank);
                ps2.setString(6, accountNumber);
                ps2.setString(7, accountHolder);
                ps2.setString(8, depositAccount);
                ps2.setString(9, username);
                ps2.executeUpdate();
            }

            try (PreparedStatement ps3 = conn.prepareStatement(sql3)) {
                ps3.setString(1, grade);
                ps3.setBoolean(2, overnight.equals("허용"));
                ps3.setString(3, username);
                ps3.executeUpdate();
            }

            // 🔥 userId 조회 (같은 커넥션/트랜잭션 내에서)
            int userId = -1;
            try (PreparedStatement psId = conn.prepareStatement(getUserIdSql)) {
                psId.setString(1, username);
                try (ResultSet rs = psId.executeQuery()) {
                    if (rs.next()) {
                        userId = rs.getInt("id");
                    }
                }
            }

            if (userId == -1) {
                conn.rollback();
                return "유저를 찾을 수 없습니다";
            }

            // 🔥 추천인 이력 갱신
            try (PreparedStatement ps4 = conn.prepareStatement(closeHistorySql)) {
                ps4.setInt(1, userId);
                ps4.executeUpdate();
            }

            try (PreparedStatement ps5 = conn.prepareStatement(insertHistorySql)) {
                ps5.setInt(1, userId);
                ps5.setString(2, recommender == null || recommender.isEmpty() ? null : recommender);
                ps5.executeUpdate();
            }

            conn.commit();
            return null; // 성공

        } catch (SQLException e) {
            e.printStackTrace();
            return "저장 중 오류가 발생했습니다";
        }
    }
}
