package service;

import model.AdminUserListRow;
import db.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminUserListService {

    public List<AdminUserListRow> loadCustomers(String keyword) {

        List<AdminUserListRow> list = new ArrayList<>();

        String sql = """
            SELECT
                u.id,
                u.created_at, u.username, u.name,
                up.phone, up.recommender,
                u.balance,
                us.total_pnl, us.total_fee, us.total_winrate, us.is_online,
                u.account_type,
                uas.customer_grade, uas.account_status,
                u.password,
                up.email,
                uas.server,
                uas.overnight_setting,
                us.trade_count, us.trade_days,
                up.bank, up.account_number, up.account_holder, up.deposit_account,
                us.last_trade_time,
                ufs.futures_fee, ufs.options_fee, ufs.night_futures_fee, ufs.night_options_fee,
                uql.max_futures_qty, uql.max_options_buy_qty, uql.max_options_sell_qty, uql.max_overseas_qty,
                up.memo_customer, up.memo_partner,
                us.last_login, us.login_fail_count,
                up.join_ip, up.join_mac,
                upe.overnight_permission, upe.chat_permission
            FROM users u
            LEFT JOIN user_profiles up ON u.id = up.user_id
            LEFT JOIN user_account_status uas ON u.id = uas.user_id
            LEFT JOIN user_fee_settings ufs ON u.id = ufs.user_id
            LEFT JOIN user_qty_limits uql ON u.id = uql.user_id
            LEFT JOIN user_status us ON u.id = us.user_id
            LEFT JOIN user_permissions upe ON u.id = upe.user_id
            WHERE u.role = 'USER'
            AND (u.username LIKE ? OR u.name LIKE ?)
            ORDER BY u.created_at DESC
        """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String kw = "%" + (keyword == null ? "" : keyword) + "%";
            ps.setString(1, kw);
            ps.setString(2, kw);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                AdminUserListRow row = new AdminUserListRow();
                int userId = rs.getInt("id");   // 🔥 이 줄이 있는지 확인
                row.setCreatedAt(rs.getTimestamp("created_at"));
                row.setUsername(rs.getString("username"));
                row.setName(rs.getString("name"));
                row.setPhone(rs.getString("phone"));
                row.setRecommender(rs.getString("recommender"));
                row.setBalance(rs.getLong("balance"));
                row.setTotalPnl(rs.getDouble("total_pnl"));
                row.setTotalFee(rs.getDouble("total_fee"));
                row.setTotalWinrate(rs.getInt("total_winrate") + "%");
                row.setOnlineStatus(rs.getBoolean("is_online") ? "온라인" : "오프라인");
                row.setAccountType(rs.getString("account_type"));
                row.setCustomerGrade(rs.getString("customer_grade"));
                row.setAccountStatus(rs.getString("account_status"));
                row.setPassword(rs.getString("password"));
                row.setEmail(rs.getString("email"));
                row.setServer(rs.getString("server"));
                row.setOvernightSetting(rs.getBoolean("overnight_setting") ? "허용" : "미허용");
                row.setTradeCount(rs.getInt("trade_count"));
                row.setTradeDays(rs.getInt("trade_days"));
                row.setBank(rs.getString("bank"));
                row.setAccountNumber(rs.getString("account_number"));
                row.setAccountHolder(rs.getString("account_holder"));
                row.setDepositAccount(rs.getString("deposit_account"));
                row.setLastTradeTime(rs.getTimestamp("last_trade_time") == null ? "-" : rs.getTimestamp("last_trade_time"));
                row.setFuturesFee(rs.getDouble("futures_fee"));
                row.setOptionsFee(rs.getDouble("options_fee"));
                row.setNightFuturesFee(rs.getDouble("night_futures_fee"));
                row.setNightOptionsFee(rs.getDouble("night_options_fee"));
                row.setMaxFuturesQty(rs.getInt("max_futures_qty"));
                row.setMaxOptionsBuyQty(rs.getInt("max_options_buy_qty"));
                row.setMaxOptionsSellQty(rs.getInt("max_options_sell_qty"));
                row.setMaxOverseasQty(rs.getInt("max_overseas_qty"));
                row.setOverseasLimitSummary(              // ← 이게 있어야 함
                        buildOverseasLimitSummary(conn, userId, rs.getInt("max_overseas_qty"))
                );
                row.setMemoCustomer(rs.getString("memo_customer"));
                row.setMemoPartner(rs.getString("memo_partner"));
                row.setLastLogin(rs.getTimestamp("last_login") == null ? "-" : rs.getTimestamp("last_login"));
                row.setLoginFailCount(rs.getInt("login_fail_count"));
                row.setJoinIp(rs.getString("join_ip"));
                row.setJoinMac(rs.getString("join_mac"));
                row.setOvernightPermission(rs.getBoolean("overnight_permission") ? "허용" : "미허용");
                row.setChatPermission(rs.getBoolean("chat_permission") ? "허용" : "미허용");

                list.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    private String buildOverseasLimitSummary(Connection conn, int userId, int defaultQty) throws SQLException {

        String sql = """
        SELECT symbol, max_qty
        FROM user_overseas_qty_limits
        WHERE user_id = ?
        ORDER BY symbol
    """;

        List<String> parts = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String kor = symbolToKor(rs.getString("symbol"));
                Object maxQtyObj = rs.getObject("max_qty");

                if (maxQtyObj == null) {
                    parts.add(kor + " : " + defaultQty + "(기본값)");
                } else {
                    parts.add(kor + " : " + rs.getInt("max_qty"));
                }
            }
        }

        return parts.isEmpty() ? "-" : String.join(" / ", parts);
    }


    private String symbolToKor(String symbol) {
        return switch (symbol) {
            case "GOLD" -> "금";
            case "SILVER" -> "은";
            case "CRUDE_OIL" -> "크루드오일";
            case "SP500" -> "S&P500";
            case "COPPER" -> "구리";
            case "NASDAQ" -> "나스닥";
            case "DOW" -> "다우";
            case "US_BOND_10Y" -> "미국채10년";
            case "GBP" -> "영국파운드";
            case "EURO_FX" -> "유로FX";
            case "JPY" -> "일본엔";
            case "NATURAL_GAS" -> "천연가스";
            case "CAD" -> "캐나다달러";
            case "HSI" -> "항셍지수";
            case "AUD" -> "호주달러";
            default -> symbol;
        };
    }

}
