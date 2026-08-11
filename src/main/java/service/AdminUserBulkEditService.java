package service;

import model.OverseasFeeRow;
import model.OverseasQtyRow;
import db.DBUtil;
import server.AdminUserBulkEditRequest;
import server.AdminUserBulkEditResult;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminUserBulkEditService {

    // AdminUserFullService와 동일한 매핑 재사용 (같은 걸 두 곳에 중복 정의 중 -
    // 나중에 공통 클래스로 뽑는 걸 추천드립니다)
    private static final Map<String, String> KOR_TO_SYMBOL = new HashMap<>();
    static {
        KOR_TO_SYMBOL.put("금", "GOLD");
        KOR_TO_SYMBOL.put("은", "SILVER");
        KOR_TO_SYMBOL.put("크루드오일", "CRUDE_OIL");
        KOR_TO_SYMBOL.put("S&P500", "SP500");
        KOR_TO_SYMBOL.put("구리", "COPPER");
        KOR_TO_SYMBOL.put("나스닥", "NASDAQ");
        KOR_TO_SYMBOL.put("다우", "DOW");
        KOR_TO_SYMBOL.put("미국채10년", "US_BOND_10Y");
        KOR_TO_SYMBOL.put("영국파운드", "GBP");
        KOR_TO_SYMBOL.put("유로FX", "EURO_FX");
        KOR_TO_SYMBOL.put("일본엔", "JPY");
        KOR_TO_SYMBOL.put("천연가스", "NATURAL_GAS");
        KOR_TO_SYMBOL.put("캐나다달러", "CAD");
        KOR_TO_SYMBOL.put("항셍지수", "HSI");
        KOR_TO_SYMBOL.put("호주달러", "AUD");
    }

    public AdminUserBulkEditResult saveBulk(AdminUserBulkEditRequest req) {

        if (req.usernames == null || req.usernames.isEmpty()) {
            return new AdminUserBulkEditResult(false, "선택된 고객이 없습니다.", 0, 0);
        }

        // ---- 검증 ----
        if (req.updateFee) {
            try {
                Double.parseDouble(req.futuresFee.isEmpty() ? "0" : req.futuresFee);
                Double.parseDouble(req.nightFuturesFee.isEmpty() ? "0" : req.nightFuturesFee);
                Double.parseDouble(req.optionsFee.isEmpty() ? "0" : req.optionsFee);
                Double.parseDouble(req.nightOptionsFee.isEmpty() ? "0" : req.nightOptionsFee);
            } catch (NumberFormatException e) {
                return new AdminUserBulkEditResult(false, "국내 수수료는 숫자만 입력하세요", 0, 0);
            }
            if (req.overseasFees != null) {
                for (OverseasFeeRow row : req.overseasFees) {
                    if (row.getFee() < 0) return new AdminUserBulkEditResult(false, "해외 수수료는 0 이상이어야 합니다", 0, 0);
                }
            }
        }

        if (req.updateQty) {
            model.SystemQtyLimit systemLimit = new SystemQtyLimitDAO().getSettings();
            if (systemLimit == null) return new AdminUserBulkEditResult(false, "시스템 설정 조회 실패", 0, 0);

            if (req.maxFuturesQty != null && req.maxFuturesQty > systemLimit.getMaxFuturesQty())
                return new AdminUserBulkEditResult(false,
                        "시스템 국내선물 최대계약수(" + systemLimit.getMaxFuturesQty() + ")를 초과할 수 없습니다.", 0, 0);
            if (req.maxOptionsQty != null && req.maxOptionsQty > systemLimit.getMaxOptionsQty())
                return new AdminUserBulkEditResult(false,
                        "시스템 옵션 최대계약수(" + systemLimit.getMaxOptionsQty() + ")를 초과할 수 없습니다.", 0, 0);
            if (req.maxOverseasQty != null && req.maxOverseasQty > systemLimit.getMaxOverseasQty())
                return new AdminUserBulkEditResult(false,
                        "시스템 해외선물 최대계약수(" + systemLimit.getMaxOverseasQty() + ")를 초과할 수 없습니다.", 0, 0);

            if (req.overseasQtyRows != null) {
                int cap = req.maxOverseasQty != null ? req.maxOverseasQty : Integer.MAX_VALUE;
                for (OverseasQtyRow row : req.overseasQtyRows) {
                    if (row.getMaxQty() == null) continue;
                    if (row.getMaxQty() < 0) return new AdminUserBulkEditResult(false, "개별 계약수는 0 이상이어야 합니다", 0, 0);
                    if (row.getMaxQty() > cap)
                        return new AdminUserBulkEditResult(false,
                                "개별 계약수는 해외선물 최대계약수(" + cap + ")를 초과할 수 없습니다.", 0, 0);
                }
            }
        }

        int successCount = 0;
        int failCount = 0;

        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);

            for (String username : req.usernames) {
                try {
                    applyToUser(conn, req, username);
                    successCount++;
                } catch (SQLException e) {
                    e.printStackTrace();
                    failCount++;
                }
            }

            conn.commit();

        } catch (SQLException e) {
            e.printStackTrace();
            return new AdminUserBulkEditResult(false, "저장 중 오류가 발생했습니다", 0, req.usernames.size());
        }

        boolean success = failCount == 0;
        String message = success
                ? successCount + "명 저장되었습니다."
                : successCount + "명 성공, " + failCount + "명 실패했습니다.";

        return new AdminUserBulkEditResult(success, message, successCount, failCount);
    }

    private void applyToUser(Connection conn, AdminUserBulkEditRequest req, String username) throws SQLException {

        // users
        if (req.updatePassword) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE users SET password=? WHERE username=?")) {
                ps.setString(1, req.password);
                ps.setString(2, username);
                ps.executeUpdate();
            }
        }

        // user_profiles (동적 SET절 - 체크된 것만)
        Map<String, Object> profileSets = new HashMap<>();
        if (req.updateRecommender) profileSets.put("recommender", req.recommender);
        if (req.updatePartnerMemo) profileSets.put("memo_partner", req.partnerMemo);
        if (req.updateDepositAccount) profileSets.put("deposit_account", req.depositAccount);
        if (req.updateMemoCustomer) profileSets.put("memo_customer", req.memoCustomer);
        updateByUsername(conn, "user_profiles", profileSets, username);

        // user_account_status
        Map<String, Object> statusSets = new HashMap<>();
        if (req.updateGrade) statusSets.put("customer_grade", req.grade);
        if (req.updateOvernight) statusSets.put("overnight_setting", "허용".equals(req.overnight));
        if (req.updateAccountStatus) statusSets.put("account_status", req.accountStatus);
        updateByUsername(conn, "user_account_status", statusSets, username);

        // 추천인 이력 (추천인 변경 시에만)
        if (req.updateRecommender) {
            int userId = resolveUserId(conn, username);
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE user_partner_history SET end_time = NOW() WHERE user_id=? AND end_time IS NULL")) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO user_partner_history (user_id, partner_username, start_time, end_time) VALUES (?, ?, NOW(), NULL)")) {
                ps.setInt(1, userId);
                ps.setString(2, req.recommender == null || req.recommender.isEmpty() ? null : req.recommender);
                ps.executeUpdate();
            }
        }

        // 수수료
        if (req.updateFee) {
            try (PreparedStatement ps = conn.prepareStatement("""
                UPDATE user_fee_settings
                SET futures_fee=?, night_futures_fee=?, options_fee=?, night_options_fee=?
                WHERE user_id = (SELECT id FROM users WHERE username=?)
            """)) {
                ps.setDouble(1, Double.parseDouble(req.futuresFee.isEmpty() ? "0" : req.futuresFee));
                ps.setDouble(2, Double.parseDouble(req.nightFuturesFee.isEmpty() ? "0" : req.nightFuturesFee));
                ps.setDouble(3, Double.parseDouble(req.optionsFee.isEmpty() ? "0" : req.optionsFee));
                ps.setDouble(4, Double.parseDouble(req.nightOptionsFee.isEmpty() ? "0" : req.nightOptionsFee));
                ps.setString(5, username);
                ps.executeUpdate();
            }

            if (req.overseasFees != null && !req.overseasFees.isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement("""
                    UPDATE user_overseas_fees SET fee=?
                    WHERE user_id=(SELECT id FROM users WHERE username=?) AND symbol=?
                """)) {
                    for (OverseasFeeRow row : req.overseasFees) {
                        String symbol = KOR_TO_SYMBOL.getOrDefault(row.getSymbolKor(), row.getSymbolKor());
                        ps.setDouble(1, row.getFee());
                        ps.setString(2, username);
                        ps.setString(3, symbol);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }
        }

        // 계약수
        if (req.updateQty) {
            Map<String, Object> qtySets = new HashMap<>();
            if (req.maxFuturesQty != null) qtySets.put("max_futures_qty", req.maxFuturesQty);
            if (req.maxOptionsQty != null) qtySets.put("max_options_qty", req.maxOptionsQty);
            if (req.maxOverseasQty != null) qtySets.put("max_overseas_qty", req.maxOverseasQty);
            updateByUsername(conn, "user_qty_limits", qtySets, username);


            if (req.overseasQtyRows != null && !req.overseasQtyRows.isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement("""
                    UPDATE user_overseas_qty_limits SET max_qty=?
                    WHERE user_id=(SELECT id FROM users WHERE username=?) AND symbol=?
                """)) {
                    for (OverseasQtyRow row : req.overseasQtyRows) {
                        if (row.getMaxQty() == null) continue; // 값 없는 종목은 건드리지 않음
                        ps.setInt(1, row.getMaxQty());
                        ps.setString(2, username);
                        ps.setString(3, row.getSymbol());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }
        }
    }

    private void updateByUsername(Connection conn, String table, Map<String, Object> sets, String username) throws SQLException {
        if (sets.isEmpty()) return;

        StringBuilder sb = new StringBuilder("UPDATE ").append(table).append(" SET ");
        java.util.List<Object> params = new java.util.ArrayList<>();
        boolean first = true;
        for (Map.Entry<String, Object> e : sets.entrySet()) {
            if (!first) sb.append(", ");
            sb.append(e.getKey()).append("=?");
            params.add(e.getValue());
            first = false;
        }
        sb.append(" WHERE user_id = (SELECT id FROM users WHERE username=?)");
        params.add(username);

        try (PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ps.executeUpdate();
        }
    }

    private int resolveUserId(Connection conn, String username) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM users WHERE username=?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        }
        throw new SQLException("유저를 찾을 수 없습니다: " + username);
    }
}