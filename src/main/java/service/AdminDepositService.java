package service;

import model.AdminDepositMonitoring;
import db.DBUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AdminDepositService {

    // 기존 AdminService.getMonitoring 그대로 (변경 없음)
    public List<AdminDepositMonitoring> getMonitoring(String type) {

        List<AdminDepositMonitoring> list = new ArrayList<>();

        String sql = """
SELECT
    d.id,
    d.status,
    d.amount,
    d.request_note,
    d.admin_memo,
    d.created_at,
    d.processed_at,

    CONCAT(u.name, '(', u.username, ')') AS name_id,
    u.account_type,

    d.request_source,

    d.snapshot_account_holder AS account_holder,
    d.snapshot_bank AS deposit_bank,
    d.snapshot_account_number AS deposit_account_number,
    d.snapshot_deposit_account_bank AS deposit_account,
    d.snapshot_recommender AS recommender,
    d.snapshot_customer_grade AS customer_grade

FROM deposit_requests d
JOIN users u ON d.user_id = u.id

WHERE d.type = ?
AND d.created_at >= ?
AND d.created_at < ?
ORDER BY d.id DESC
""";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, type);
            LocalDateTime now = LocalDateTime.now();

            LocalDateTime start;

            if (now.getHour() >= 7) {
                start = now.toLocalDate().atTime(7, 0);
            } else {
                start = now.toLocalDate().minusDays(1).atTime(7, 0);
            }

            LocalDateTime end = start.plusDays(1);

            ps.setTimestamp(2, Timestamp.valueOf(start));
            ps.setTimestamp(3, Timestamp.valueOf(end));


            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                AdminDepositMonitoring m = new AdminDepositMonitoring();

                m.setId(rs.getInt("id"));
                String status = rs.getString("status");

                switch (status) {
                    case "PENDING":
                        status = "승인대기";
                        break;
                    case "APPROVED":
                        status = "승인완료";
                        break;
                    case "REJECTED":
                        status = "승인거부";
                        break;
                }

                m.setStatus(status);

                m.setNameId(rs.getString("name_id"));
                m.setCustomerGrade(rs.getString("customer_grade"));

                m.setAmount(rs.getDouble("amount"));

                m.setAccountHolder(rs.getString("account_holder"));
                m.setDepositAccount(rs.getString("deposit_account"));

                String source = rs.getString("request_source");

                switch (source) {
                    case "USER":
                        source = "고객";
                        break;
                    case "ADMIN":
                        source = "매니저";
                        break;
                }

                m.setRequestSource(source);
                m.setRequestNote(rs.getString("request_note"));
                m.setAdminMemo(rs.getString("admin_memo"));

                String accountType = rs.getString("account_type");

                switch (accountType) {
                    case "REAL":
                        accountType = "실거래";
                        break;
                    case "DEMO":
                        accountType = "모의";
                        break;
                    case "PARTNER":
                        accountType = "파트너";
                        break;
                }

                m.setAccountType(accountType);
                m.setRecommender(rs.getString("recommender"));

                m.setCreatedAt(rs.getTimestamp("created_at"));
                m.setProcessedAt(rs.getTimestamp("processed_at"));

                m.setServer("-");
                list.add(m);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // 🔥 변경: 승인된 건들의 userId 목록을 반환 (TOP_INFO push 대상)
    public List<ApprovedInfo> approveRequests(List<Integer> requestIds, int adminId) {

        List<ApprovedInfo> affectedUserIds = new ArrayList<>();

        String selectSql = "SELECT user_id, type, amount FROM deposit_requests WHERE id=?";
        String updateDepositSql = "UPDATE users SET balance = balance + ? WHERE id=?";
        String approveSql = """
        UPDATE deposit_requests 
        SET status='APPROVED', admin_id=?, processed_at=NOW(), processed_amount=amount
        WHERE id=?
    """;

        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);

            for (int requestId : requestIds) {

                PreparedStatement selectStmt = conn.prepareStatement(selectSql);
                selectStmt.setInt(1, requestId);
                ResultSet rs = selectStmt.executeQuery();

                if (!rs.next()) continue;

                int userId = rs.getInt("user_id");
                String reqType = rs.getString("type");
                double amount = rs.getDouble("amount");

                // 🔥 입금만 승인 시 잔액 반영. 출금은 이미 신청 시점에 차감됐으니 여기선 상태만 변경
                if ("DEPOSIT".equals(reqType)) {
                    PreparedStatement balanceStmt = conn.prepareStatement(updateDepositSql);
                    balanceStmt.setDouble(1, amount);
                    balanceStmt.setInt(2, userId);
                    balanceStmt.executeUpdate();
                }

                PreparedStatement approveStmt = conn.prepareStatement(approveSql);
                approveStmt.setInt(1, adminId);
                approveStmt.setInt(2, requestId);
                approveStmt.executeUpdate();

                affectedUserIds.add(new ApprovedInfo(userId, reqType));
            }

            conn.commit();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return affectedUserIds;
    }

    public List<ApprovedInfo> rejectRequests(List<Integer> requestIds, int adminId) {

        List<ApprovedInfo> affectedUserIds = new ArrayList<>();

        String selectSql = "SELECT user_id, type, amount FROM deposit_requests WHERE id=?";
        String refundSql = "UPDATE users SET balance = balance + ? WHERE id=?";
        String rejectSql = """
        UPDATE deposit_requests 
        SET status='REJECTED', admin_id=?, processed_at=NOW(), processed_amount=0
        WHERE id=?
    """;

        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);

            for (int requestId : requestIds) {

                PreparedStatement selectStmt = conn.prepareStatement(selectSql);
                selectStmt.setInt(1, requestId);
                ResultSet rs = selectStmt.executeQuery();

                if (!rs.next()) continue;

                int userId = rs.getInt("user_id");
                String reqType = rs.getString("type");
                double amount = rs.getDouble("amount");

                // 🔥 출금 거절이면 신청 시 차감했던 금액을 환불
                if ("WITHDRAW".equals(reqType)) {
                    PreparedStatement refundStmt = conn.prepareStatement(refundSql);
                    refundStmt.setDouble(1, amount);
                    refundStmt.setInt(2, userId);
                    refundStmt.executeUpdate();
                }

                PreparedStatement rejectStmt = conn.prepareStatement(rejectSql);
                rejectStmt.setInt(1, adminId);
                rejectStmt.setInt(2, requestId);
                rejectStmt.executeUpdate();

                affectedUserIds.add(new ApprovedInfo(userId, reqType));
            }

            conn.commit();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return affectedUserIds;
    }

    public static class ApprovedInfo {
        public int userId;
        public String type;

        public ApprovedInfo(int userId, String type) {
            this.userId = userId;
            this.type = type;
        }
    }
}
