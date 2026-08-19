
package service;

import model.*;
import server.SessionManager;
import service.UserDataDAO;
import db.DBUtil;

import java.sql.*;
import java.util.List;

public class CustomerDepositService {

    public String createRequest(int userId, String type, double amount, String requestNote) {

        if ("WITHDRAW".equals(type)) {
            PositionService positionService = new PositionService();
            List<Position> positions = positionService.getAllPositions(userId);
            if (!positions.isEmpty()) {
                return "보유 포지션이 있어 출금 신청이 불가합니다";
            }
        }

        String partnerUsername = new UserDataDAO().getPartnerUsername(userId);

        // 🔥 신청 시점 계좌정보 스냅샷 조회
        String bank = null, accountNumber = null, accountHolder = null, depositAccountBank = null;
        String recommender = null, customerGrade = null, accountStatus = null;   // 🔥 추가

        String profileSql = "SELECT bank, account_number, account_holder, deposit_account, recommender FROM user_profiles WHERE user_id=?";
        String companyAccountSql = "SELECT bank FROM company_accounts WHERE id=?";
        String statusSql = "SELECT customer_grade, account_status FROM user_account_status WHERE user_id=?";   // 🔥 추가

        String selectSql = "SELECT balance FROM users WHERE id=?";
        String deductBalanceSql = "UPDATE users SET balance = balance - ? WHERE id=?";
        String insertSql = """
        INSERT INTO deposit_requests
            (user_id, type, amount, status, request_note, remark, partner_username, created_at,
             snapshot_bank, snapshot_account_number, snapshot_account_holder, snapshot_deposit_account_bank,
             snapshot_recommender, snapshot_customer_grade, snapshot_account_status)
        VALUES
            (?, ?, ?, 'PENDING', ?, ?, ?, NOW(), ?, ?, ?, ?, ?, ?, ?)
    """;
        try (Connection conn = DBUtil.getConnection()) {

            conn.setAutoCommit(false);

            // 프로필 스냅샷 조회
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
                                    if (caRs.next()) {
                                        depositAccountBank = caRs.getString("bank");
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // 🔥 계정상태 스냅샷 조회
            try (PreparedStatement ps = conn.prepareStatement(statusSql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        customerGrade = rs.getString("customer_grade");
                        accountStatus = rs.getString("account_status");
                    }
                }
            }
            if ("WITHDRAW".equals(type)) {
                try (PreparedStatement checkStmt = conn.prepareStatement(selectSql)) {
                    checkStmt.setInt(1, userId);
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next() && rs.getDouble("balance") < amount) {
                            conn.rollback();
                            return "잔액 부족으로 출금 신청이 불가합니다";
                        }
                    }
                }
                //출금신청 즉시 담보금 차감(홀드)
                try (PreparedStatement deductStmt = conn.prepareStatement(deductBalanceSql)) {
                    deductStmt.setDouble(1, amount);
                    deductStmt.setInt(2, userId);
                    deductStmt.executeUpdate();
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setInt(1, userId);
                stmt.setString(2, type);
                stmt.setDouble(3, amount);
                stmt.setString(4, requestNote == null || requestNote.isEmpty() ? null : requestNote);
                stmt.setString(5, "고객");
                stmt.setString(6, partnerUsername);
                stmt.setString(7, bank);
                stmt.setString(8, accountNumber);
                stmt.setString(9, accountHolder);
                stmt.setString(10, depositAccountBank);
                stmt.setString(11, recommender);      // 🔥
                stmt.setString(12, customerGrade);    // 🔥
                stmt.setString(13, accountStatus);  // 🔥

                int rows = stmt.executeUpdate();

                if (rows > 0) {
                    conn.commit();
                    return null;
                } else {
                    conn.rollback();
                    return "신청 실패";
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "신청 실패";
        }
    }

    public void pushBalanceToUser(int userId) {
        User user = new UserDAO().getUserById(userId);  // 서버에 이미 있는 UserDAO 재사용
        if (user != null) {
            SessionManager.sendToCustomer(userId, new DwBalanceUpdate(user.getBalance()));
        }
    }

    public void pushAccountInfoToUser(int userId) {
        System.out.println("[DEBUG] pushAccountInfoToUser 호출됨, userId=" + userId);

        DwAccountInfo info = loadAccountInfo(userId);

        System.out.println(
                "[DEBUG] loadAccountInfo 결과: " +
                        "bank=" + info.getBank() +
                        ", accountNumber=" + info.getAccountNumber() +
                        ", accountHolder=" + info.getAccountHolder() +
                        ", alias=" + info.getAlias()
        );

        DwAccountInfoUpdate update = new DwAccountInfoUpdate();
        update.setBank(info.getBank());
        update.setAccountNumber(info.getAccountNumber());
        update.setAccountHolder(info.getAccountHolder());
        update.setAlias(info.getAlias());

        System.out.println(
                "[DEBUG] push할 계좌정보: " +
                        "bank=" + update.getBank() +
                        ", accountNumber=" + update.getAccountNumber() +
                        ", accountHolder=" + update.getAccountHolder() +
                        ", alias=" + update.getAlias()
        );

        SessionManager.sendToCustomer(userId, update);
    }


    public DwAccountInfo loadAccountInfo(int userId) {

        String profileSql = "SELECT deposit_account FROM user_profiles WHERE user_id=?";
        String companyAccountSql = "SELECT bank, account_number, account_holder FROM company_accounts WHERE id=?";

        DwAccountInfo info = new DwAccountInfo();

        try (Connection conn = DBUtil.getConnection()) {

            String depositAccountId = null;

            try (PreparedStatement ps = conn.prepareStatement(profileSql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        depositAccountId = rs.getString("deposit_account");
                    }
                }
            }

            if (depositAccountId != null && !depositAccountId.isBlank()) {
                try (PreparedStatement ps = conn.prepareStatement(companyAccountSql)) {
                    ps.setInt(1, Integer.parseInt(depositAccountId));
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            info.setBank(rs.getString("bank"));
                            info.setAccountNumber(rs.getString("account_number"));
                            info.setAccountHolder(rs.getString("account_holder"));
                            return info;
                        }
                    }
                }
            }

        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
        }

        // 계좌 미배정 등으로 못 찾은 경우 기본값
        info.setBank("-");
        info.setAccountNumber("-");
        info.setAccountHolder("-");
        return info;
    }

}