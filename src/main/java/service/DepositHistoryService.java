// server/service/DepositHistoryService.java
package service;

import model.AdminDepositHistoryRow;
import model.CustomerDepositHistoryRow;
import db.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DepositHistoryService {

    private String getTypeStatusText(String type, String status) {

        boolean isDeposit = "DEPOSIT".equals(type);

        if (isDeposit) {
            switch (status) {
                case "PENDING": return "입금대기";
                case "APPROVED": return "입금완료";
                case "REJECTED": return "입금거부";
            }
        } else {
            switch (status) {
                case "PENDING": return "출금대기";
                case "APPROVED": return "출금완료";
                case "REJECTED": return "출금거부";
            }
        }

        return "-";
    }

    private String nvl(String val) {
        return val == null ? "-" : val;
    }

    public List<AdminDepositHistoryRow> getAdminHistory(String nameKeyword, Timestamp start, Timestamp end) {

        List<AdminDepositHistoryRow> list = new ArrayList<>();

        String baseSql = """
        SELECT
            u.id,
            u.username,
            u.name,
            dr.type,
            dr.created_at,
            dr.processed_at,
            dr.amount,
            dr.processed_amount,
            dr.status,
            adm.name AS admin_name,
            dr.snapshot_bank,
            dr.snapshot_account_number,
            dr.snapshot_account_holder,
            dr.snapshot_deposit_account_bank,
            dr.snapshot_recommender,
            dr.snapshot_customer_grade,
            dr.snapshot_account_status,
            dr.request_note,
            dr.admin_memo,
            dr.remark
        FROM deposit_requests dr
        JOIN users u ON dr.user_id = u.id
        LEFT JOIN users adm ON dr.admin_id = adm.id
        WHERE dr.created_at BETWEEN ? AND ?
    """;

        String sql = (nameKeyword != null && !nameKeyword.isBlank())
                ? baseSql + " AND u.name LIKE ? ORDER BY dr.id DESC"
                : baseSql + " ORDER BY dr.id DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, start);
            ps.setTimestamp(2, end);
            if (nameKeyword != null && !nameKeyword.isBlank()) {
                ps.setString(3, "%" + nameKeyword + "%");
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String typeKor = getTypeStatusText(rs.getString("type"), rs.getString("status"));
                String status = rs.getString("status");

                double processedAmount;
                if (rs.getTimestamp("processed_at") == null) {   // 🔥 수정: PENDING 상태(미처리) 우선 체크
                    processedAmount = 0;
                } else if ("REJECTED".equals(status)) {
                    processedAmount = 0;
                } else {
                    Object pa = rs.getObject("processed_amount");
                    processedAmount = (pa != null) ? rs.getDouble("processed_amount") : rs.getDouble("amount");
                }

                AdminDepositHistoryRow row = new AdminDepositHistoryRow();
                row.setUsername(rs.getString("username"));
                row.setName(rs.getString("name"));
                row.setCustomerGrade(nvl(rs.getString("snapshot_customer_grade")));       // 🔥 스냅샷
                row.setTypeKor(typeKor);
                row.setCreatedAt(rs.getTimestamp("created_at"));
                row.setProcessedAt(rs.getTimestamp("processed_at") == null ? "-" : rs.getTimestamp("processed_at"));
                row.setAmount(rs.getDouble("amount"));
                row.setProcessedAmount(processedAmount);
                row.setAdminName(nvl(rs.getString("admin_name")));
                row.setBank(nvl(rs.getString("snapshot_bank")));
                row.setAccountNumber(nvl(rs.getString("snapshot_account_number")));
                row.setAccountHolder(nvl(rs.getString("snapshot_account_holder")));
                row.setDepositAccountBank(nvl(rs.getString("snapshot_deposit_account_bank")));
                row.setRequestNote(nvl(rs.getString("request_note")));
                row.setAdminMemo(nvl(rs.getString("admin_memo")));
                row.setRemark(nvl(rs.getString("remark")));
                row.setAccountStatus(nvl(rs.getString("snapshot_account_status")));       // 🔥 스냅샷
                row.setRecommender(nvl(rs.getString("snapshot_recommender")));            // 🔥 스냅샷

                list.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<CustomerDepositHistoryRow> getCustomerHistory(int userId, Timestamp start, Timestamp end) {

        List<CustomerDepositHistoryRow> list = new ArrayList<>();

        String sql = """
        SELECT
            dr.created_at,
            dr.type,
            dr.status,
            dr.amount,
            dr.processed_at,
            dr.processed_amount,
            dr.request_note,
            dr.remark,
            dr.snapshot_bank,
            dr.snapshot_account_number,
            dr.snapshot_account_holder
        FROM deposit_requests dr
        WHERE dr.user_id = ?
          AND dr.created_at BETWEEN ? AND ?
        ORDER BY dr.id DESC
    """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setTimestamp(2, start);
            stmt.setTimestamp(3, end);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                String typeKor = getTypeStatusText(rs.getString("type"), rs.getString("status"));

                double processedAmount;
                if (rs.getTimestamp("processed_at") == null) {
                    processedAmount = 0;
                } else {
                    Object pa = rs.getObject("processed_amount");
                    processedAmount = (pa != null) ? rs.getDouble("processed_amount") : rs.getDouble("amount");
                }

                CustomerDepositHistoryRow row = new CustomerDepositHistoryRow();
                row.setRemark(nvl(rs.getString("remark")));
                row.setTypeKor(typeKor);
                row.setCreatedAt(rs.getTimestamp("created_at"));
                row.setAmount(rs.getDouble("amount"));
                row.setProcessedAt(rs.getTimestamp("processed_at") == null ? "-" : rs.getTimestamp("processed_at"));
                row.setProcessedAmount(processedAmount);
                row.setRequestNote(nvl(rs.getString("request_note")));
                row.setBank(nvl(rs.getString("snapshot_bank")));                   // 🔥
                row.setAccountNumber(nvl(rs.getString("snapshot_account_number"))); // 🔥
                row.setAccountHolder(nvl(rs.getString("snapshot_account_holder"))); // 🔥

                list.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
}