package service;

import Market.MarketSpecCache;
import server.AdminUserRegisterRequest;
import model.OverseasFeeRow;
import model.OverseasQtyRow;
import db.DBUtil;

import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class AdminUserRegisterService {

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

    private double parseDouble(String s) {
        try { return s == null || s.isEmpty() ? 0 : Double.parseDouble(s); }
        catch (NumberFormatException e) { return 0; }
    }

    private long parseLong(String s) {
        try { return s == null || s.isEmpty() ? 0 : Long.parseLong(s); }
        catch (NumberFormatException e) { return 0; }
    }

    private int parseInt(String s) {
        try { return s == null || s.isEmpty() ? 0 : Integer.parseInt(s); }
        catch (NumberFormatException e) { return 0; }
    }

    public String insertNewUser(AdminUserRegisterRequest req) {

        Connection conn = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // 1. users
            int userId;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO users (username, name, password, role, balance, account_type) VALUES (?, ?, ?, 'USER', ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, req.username);
                ps.setString(2, req.name);
                ps.setString(3, req.password);
                ps.setLong(4, parseLong(req.balance));
                ps.setString(5, req.accountType);
                ps.executeUpdate();

                ResultSet keys = ps.getGeneratedKeys();
                if (!keys.next()) {
                    conn.rollback();
                    return "아이디 중복 또는 DB 오류입니다.";
                }
                userId = keys.getInt(1);
            }

            // 2. user_profiles
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO user_profiles (user_id, phone, email, recommender, bank, account_number, account_holder, deposit_account, memo_partner, memo_customer) VALUES (?,?,?,?,?,?,?,?,?,?)")) {
                ps.setInt(1, userId);
                ps.setString(2, req.phone);
                ps.setString(3, req.email);
                ps.setString(4, req.recommender);
                ps.setString(5, req.bank);
                ps.setString(6, req.accountNumber);
                ps.setString(7, req.accountHolder);
                ps.setString(8, req.depositAccount);
                ps.setString(9, req.partnerMemo);
                ps.setString(10, req.customerMemo);
                ps.executeUpdate();
            }

            // 3. user_account_status
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO user_account_status (user_id, account_status, customer_grade, server, overnight_setting) VALUES (?,?,?,?,?)")) {
                ps.setInt(1, userId);
                ps.setString(2, req.accountStatus);
                ps.setString(3, req.grade);
                ps.setString(4, req.server);
                ps.setBoolean(5, "허용".equals(req.overnight));
                ps.executeUpdate();
            }

            // 4. user_fee_settings
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO user_fee_settings (user_id, futures_fee, night_futures_fee, options_fee, night_options_fee) VALUES (?,?,?,?,?)")) {
                ps.setInt(1, userId);
                ps.setDouble(2, parseDouble(req.futuresFee));
                ps.setDouble(3, parseDouble(req.nightFuturesFee));
                ps.setDouble(4, parseDouble(req.optionsFee));
                ps.setDouble(5, parseDouble(req.nightOptionsFee));
                ps.executeUpdate();
            }

            // 5. user_overseas_fees
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO user_overseas_fees (user_id, symbol, fee) VALUES (?,?,?)")) {

                for (OverseasFeeRow row : req.overseasFees) {
                    String symbol = KOR_TO_SYMBOL.getOrDefault(row.getSymbolKor(), row.getSymbolKor());   // 🔥 변환 추가

                    double fee = row.getFee();
                    if (fee == 0) {
                        fee = MarketSpecCache.get(symbol).getFeePerContract();
                    }

                    ps.setInt(1, userId);
                    ps.setString(2, symbol);
                    ps.setDouble(3, fee);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // 6. user_qty_limits (국내 3개)
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO user_qty_limits (user_id, max_futures_qty, max_options_buy_qty, max_options_sell_qty) VALUES (?,?,?,?)")) {
                ps.setInt(1, userId);
                ps.setInt(2, req.maxFuturesQty);
                ps.setInt(3, req.maxOptionsBuyQty);
                ps.setInt(4, req.maxOptionsSellQty);
                ps.executeUpdate();
            }

            // 7. user_overseas_qty_limits (해외 종목별)
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO user_overseas_qty_limits (user_id, symbol, max_qty) VALUES (?,?,?)")) {
                for (OverseasQtyRow row : req.overseasQtyRows) {
                    String symbol = KOR_TO_SYMBOL.getOrDefault(row.getSymbol(), row.getSymbol());
                    int qty = row.getMaxQty() != null ? row.getMaxQty() : req.maxOverseasQty;   // 🔥 null이면 요약값 사용
                    ps.setInt(1, userId);
                    ps.setString(2, symbol);
                    ps.setInt(3, qty > 0 ? qty : 10);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // 8. user_permissions
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO user_permissions (user_id, overnight_permission, chat_permission) VALUES (?,?,?)")) {
                ps.setInt(1, userId);
                ps.setBoolean(2, "허용".equals(req.overnight));
                ps.setBoolean(3, "허용".equals(req.remote));
                ps.executeUpdate();
            }

            // 9. user_status
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO user_status (user_id, mileage) VALUES (?,?)")) {
                ps.setInt(1, userId);
                ps.setLong(2, parseLong(req.mileage));
                ps.executeUpdate();
            }

            // 10. user_trade_permissions (기본값)
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO user_trade_permissions (user_id) VALUES (?)")) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            // 11. user_overseas_permissions (기본값)
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO user_overseas_permissions (user_id, symbol, enabled, max_qty) VALUES (?,?,true,0)")) {
                for (OverseasQtyRow row : req.overseasQtyRows) {
                    String symbol = KOR_TO_SYMBOL.getOrDefault(row.getSymbol(), row.getSymbol());   // 🔥 여기도 동일하게 변환
                    ps.setInt(1, userId);
                    ps.setString(2, symbol);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // 12. 추천인 초기 이력
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO user_partner_history (user_id, partner_username, start_time, end_time) VALUES (?, ?, NOW(), NULL)")) {
                ps.setInt(1, userId);
                ps.setString(2, req.recommender == null || req.recommender.isEmpty() ? null : req.recommender);
                ps.executeUpdate();
            }

            conn.commit();
            return null; // 성공

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            return "아이디 중복 또는 DB 오류입니다.";
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
        }
    }
}