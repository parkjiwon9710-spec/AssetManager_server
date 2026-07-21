package service;

import model.AdminUserDepositData;
import service.UserDataDAO;
import db.DBUtil;

import java.sql.*;

public class AdminUserDepositService {

    public AdminUserDepositData loadBalance(String username) {

        String sql = "SELECT balance FROM users WHERE username = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                AdminUserDepositData data = new AdminUserDepositData();
                data.setBalance(rs.getLong("balance"));
                return data;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * @return 실패 사유(null이면 성공), 성공 시 out 파라미터로 영향받은 userId를 채워줌
     */
    public String updateBalance(String username, long delta, String memo, int adminId, int[] outUserId) {

        String sql1 = "UPDATE users SET balance = balance + ? WHERE username = ?";
        String userSql = "SELECT id FROM users WHERE username = ?";
        String profileSql = "SELECT bank, account_number, account_holder, deposit_account, recommender FROM user_profiles WHERE user_id=?";
        String companyAccountSql = "SELECT bank FROM company_accounts WHERE id=?";
        String statusSql = "SELECT customer_grade, account_status FROM user_account_status WHERE user_id=?";

        String depositSql = """
        INSERT INTO deposit_requests
            (user_id, type, amount, status, processed_at, request_source, remark, admin_memo,
             partner_username, admin_id,
             snapshot_bank, snapshot_account_number, snapshot_account_holder, snapshot_deposit_account_bank,
             snapshot_recommender, snapshot_customer_grade, snapshot_account_status)
        VALUES
            (?, ?, ?, 'APPROVED', NOW(), 'ADMIN', ?, ?,
             ?, ?,
             ?, ?, ?, ?, ?, ?, ?)
    """;

        try (Connection conn = DBUtil.getConnection()) {

            conn.setAutoCommit(false);

            try (PreparedStatement ps1 = conn.prepareStatement(sql1)) {
                ps1.setLong(1, delta);
                ps1.setString(2, username);
                ps1.executeUpdate();
            }

            int userId = 0;
            try (PreparedStatement userPs = conn.prepareStatement(userSql)) {
                userPs.setString(1, username);
                try (ResultSet rs = userPs.executeQuery()) {
                    if (rs.next()) userId = rs.getInt("id");
                }
            }

            if (userId == 0) {
                conn.rollback();
                return "유저를 찾을 수 없습니다";
            }

            String partnerUsername = new UserDataDAO().getPartnerUsername(userId);

            // 🔥 스냅샷 조회
            String bank = null, accountNumber = null, accountHolder = null, depositAccountBank = null, recommender = null;
            try (PreparedStatement ps = conn.prepareStatement(profileSql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        bank = rs.getString("bank");
                        accountNumber = rs.getString("account_number");
                        accountHolder = rs.getString("account_holder");
                        recommender = rs.getString("recommender");

                        String depositAccountId = rs.getString("deposit_account");
                        if (depositAccountId != null && !depositAccountId.isBlank()) {
                            try (PreparedStatement caPs = conn.prepareStatement(companyAccountSql)) {
                                caPs.setInt(1, Integer.parseInt(depositAccountId));
                                try (ResultSet caRs = caPs.executeQuery()) {
                                    if (caRs.next()) depositAccountBank = caRs.getString("bank");
                                }
                            }
                        }
                    }
                }
            }

            String customerGrade = null, accountStatus = null;
            try (PreparedStatement ps = conn.prepareStatement(statusSql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        customerGrade = rs.getString("customer_grade");
                        accountStatus = rs.getString("account_status");
                    }
                }
            }

            try (PreparedStatement depositPs = conn.prepareStatement(depositSql)) {
                depositPs.setInt(1, userId);
                depositPs.setString(2, delta > 0 ? "DEPOSIT" : "WITHDRAW");
                depositPs.setLong(3, Math.abs(delta));
                depositPs.setString(4, "관리자");        // remark - 그대로 유지
                depositPs.setString(5, memo);             // 🔥 admin_memo = 관리용메모
                depositPs.setString(6, partnerUsername);
                depositPs.setInt(7, adminId);             // 🔥 admin_id 추가
                depositPs.setString(8, bank);
                depositPs.setString(9, accountNumber);
                depositPs.setString(10, accountHolder);
                depositPs.setString(11, depositAccountBank);
                depositPs.setString(12, recommender);
                depositPs.setString(13, customerGrade);
                depositPs.setString(14, accountStatus);
                depositPs.executeUpdate();
            }

            conn.commit();
            outUserId[0] = userId;
            return null;

        } catch (SQLException e) {
            e.printStackTrace();
            return "담보금 업데이트 중 오류가 발생했습니다";
        }
    }
}