package service;



import model.*;
import db.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import Market.MarketSpec;
import Market.MarketSpecCache;
import server.AdminOverseasSymbolScaffoldResponse;
import server.AdminUserFullResponse;
import server.AdminUserFullSaveRequest;
import service.SystemQtyLimitDAO;

public class AdminUserFullService {

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

    // =========================================================
    // 통합 조회
    // =========================================================
    public AdminUserFullResponse loadAll(String username) {

        AdminUserFullResponse response = new AdminUserFullResponse(true, null);

        // ---- 기본정보 ----
        String sqlBasic = """
            SELECT
                u.created_at, u.username, u.name, u.password, up.email,
                up.phone, up.recommender, u.role, uas.customer_grade,
                up.memo_partner, up.bank, up.account_number, up.account_holder,
                up.deposit_account, uas.overnight_setting
            FROM users u
            LEFT JOIN user_profiles up ON u.id = up.user_id
            LEFT JOIN user_account_status uas ON u.id = uas.user_id
            WHERE u.username = ?
        """;

        // ---- 계정정보 ----
        String sqlAccount = """
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

        // ---- 국내수수료 ----
        String sqlDomesticFee = """
            SELECT futures_fee, night_futures_fee, options_fee, night_options_fee
            FROM user_fee_settings
            WHERE user_id = (SELECT id FROM users WHERE username = ?)
        """;

        // ---- 해외수수료 ----
        String sqlOverseasFee = """
            SELECT ms.symbol, COALESCE(uof.fee, ms.fee_per_contract) AS fee
            FROM market_specs ms
            LEFT JOIN user_overseas_fees uof
                ON ms.symbol = uof.symbol
                AND uof.user_id = (SELECT id FROM users WHERE username = ?)
            WHERE ms.market_type = 'OVERSEAS_FUTURES'
            ORDER BY ms.sort_order
        """;

        // ---- 계약수 ----
        String sqlQty = """
                SELECT max_futures_qty, max_options_qty, max_overseas_qty
                FROM user_qty_limits
                WHERE user_id = (SELECT id FROM users WHERE username = ?)
            """;

        // ---- 해외 개별계약수 ----
        String sqlOverseasQty = """
            SELECT symbol, max_qty
            FROM user_overseas_qty_limits
            WHERE user_id = (SELECT id FROM users WHERE username = ?)
        """;

        try (Connection conn = DBUtil.getConnection()) {

            try (PreparedStatement ps = conn.prepareStatement(sqlBasic)) {
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
                    info.setRemoteControl("미허용");
                    response.basicInfo = info;
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlAccount)) {
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
                    response.accountData = data;
                }
            }

            AdminUserFeeData feeData = new AdminUserFeeData();
            try (PreparedStatement ps = conn.prepareStatement(sqlDomesticFee)) {
                ps.setString(1, username);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    feeData.setFuturesFee(String.valueOf(rs.getDouble("futures_fee")));
                    feeData.setNightFuturesFee(String.valueOf(rs.getDouble("night_futures_fee")));
                    feeData.setOptionsFee(String.valueOf(rs.getDouble("options_fee")));
                    feeData.setNightOptionsFee(String.valueOf(rs.getDouble("night_options_fee")));
                }
            }

            List<OverseasFeeRow> overseasFees = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(sqlOverseasFee)) {
                ps.setString(1, username);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    overseasFees.add(new OverseasFeeRow(symbolToKor(rs.getString("symbol")), rs.getDouble("fee")));
                }
            }
            feeData.setOverseasFees(overseasFees);
            response.feeData = feeData;

            try (PreparedStatement ps = conn.prepareStatement(sqlQty)) {
                ps.setString(1, username);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    AdminUserQtyLimitData qtyData = new AdminUserQtyLimitData();
                    qtyData.setMaxFuturesQty(rs.getInt("max_futures_qty"));
                    qtyData.setMaxOptionsQty(rs.getInt("max_options_qty"));
                    qtyData.setMaxOverseasQty(rs.getInt("max_overseas_qty"));
                    response.qtyLimitData = qtyData;
                }
            }

            Map<String, Integer> savedOverseasQty = new HashMap<>();
            try (PreparedStatement ps = conn.prepareStatement(sqlOverseasQty)) {
                ps.setString(1, username);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    savedOverseasQty.put(rs.getString("symbol"), rs.getInt("max_qty"));
                }
            }

            List<OverseasQtyRow> overseasQtyRows = new ArrayList<>();
            for (MarketSpec spec : MarketSpecCache.getAll()) {
                if (!"OVERSEAS_FUTURES".equalsIgnoreCase(spec.getMarketType())) continue;
                overseasQtyRows.add(new OverseasQtyRow(spec.getSymbol(), savedOverseasQty.get(spec.getSymbol())));
            }
            response.overseasQtyRows = overseasQtyRows;

            response.systemMaxOverseasQty = new SystemQtyLimitDAO().getSettings() != null
                    ? new SystemQtyLimitDAO().getSettings().getMaxOverseasQty()
                    : Integer.MAX_VALUE;

        } catch (SQLException e) {
            e.printStackTrace();
            return new AdminUserFullResponse(false, "조회 중 오류가 발생했습니다");
        }

        return response;
    }







    public AdminOverseasSymbolScaffoldResponse getOverseasSymbolScaffold() {

        List<OverseasQtyRow> overseasQtyRows = new ArrayList<>();
        List<String> symbolKorList = new ArrayList<>();

        for (MarketSpec spec : MarketSpecCache.getAll()) {
            if (!"OVERSEAS_FUTURES".equalsIgnoreCase(spec.getMarketType())) continue;
            overseasQtyRows.add(new OverseasQtyRow(spec.getSymbol(), null));
            symbolKorList.add(symbolToKor(spec.getSymbol())); // 기존 메서드 그대로 재사용
        }

        model.SystemQtyLimit systemLimit = new SystemQtyLimitDAO().getSettings();
        int systemMax = systemLimit != null ? systemLimit.getMaxOverseasQty() : Integer.MAX_VALUE;

        return new AdminOverseasSymbolScaffoldResponse(overseasQtyRows, symbolKorList, systemMax);
    }





    // =========================================================
    // 통합 저장 - 하나의 트랜잭션
    // =========================================================
    public String saveAll(AdminUserFullSaveRequest req) {

        // ---- 1. 저장 전 검증 (숫자 파싱 + 시스템 한도) ----
        try {
            Double.parseDouble(req.futuresFee.isEmpty() ? "0" : req.futuresFee);
            Double.parseDouble(req.nightFuturesFee.isEmpty() ? "0" : req.nightFuturesFee);
            Double.parseDouble(req.optionsFee.isEmpty() ? "0" : req.optionsFee);
            Double.parseDouble(req.nightOptionsFee.isEmpty() ? "0" : req.nightOptionsFee);
        } catch (NumberFormatException e) {
            return "국내 수수료는 숫자만 입력하세요";
        }

        if (req.overseasFees != null) {
            for (OverseasFeeRow row : req.overseasFees) {
                if (row.getFee() < 0) return "해외 수수료는 0 이상이어야 합니다";
            }
        }

        model.SystemQtyLimit systemLimit = new SystemQtyLimitDAO().getSettings();
        if (systemLimit == null) return "시스템 설정 조회 실패";

        if (req.maxFuturesQty > systemLimit.getMaxFuturesQty())
            return "시스템 국내선물 최대계약수(" + systemLimit.getMaxFuturesQty() + ")를 초과할 수 없습니다.";
        if (req.maxOptionsQty > systemLimit.getMaxOptionsQty())
            return "시스템 옵션 최대계약수(" + systemLimit.getMaxOptionsQty() + ")를 초과할 수 없습니다.";
        if (req.maxOverseasQty > systemLimit.getMaxOverseasQty())
            return "시스템 해외선물 최대계약수(" + systemLimit.getMaxOverseasQty() + ")를 초과할 수 없습니다.";

        if (req.overseasQtyRows != null) {
            for (OverseasQtyRow row : req.overseasQtyRows) {
                if (row.getMaxQty() == null) continue;
                if (row.getMaxQty() < 0) return "개별 계약수는 0 이상이어야 합니다";
                if (row.getMaxQty() > req.maxOverseasQty)
                    return "개별 계약수는 해외선물 최대계약수(" + req.maxOverseasQty + ")를 초과할 수 없습니다.";
            }
        }

        // ---- 2. 하나의 트랜잭션으로 전부 저장 ----
        try (Connection conn = DBUtil.getConnection()) {

            conn.setAutoCommit(false);

            // 2-1. 기본정보
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE users SET name=?, password=? WHERE username=?")) {
                ps.setString(1, req.name);
                ps.setString(2, req.password);
                ps.setString(3, req.username);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement("""
    UPDATE user_profiles SET email=?, phone=?, recommender=?,
    memo_partner=?, bank=?, account_number=?, account_holder=?, deposit_account=?,
    memo_customer=?
    WHERE user_id = (SELECT id FROM users WHERE username=?)
""")) {
                ps.setString(1, req.email);
                ps.setString(2, req.phone);
                ps.setString(3, req.recommender);
                ps.setString(4, req.partnerMemo);
                ps.setString(5, req.bank);
                ps.setString(6, req.accountNumber);
                ps.setString(7, req.accountHolder);
                ps.setString(8, req.depositAccount);
                ps.setString(9, req.memoCustomer);
                ps.setString(10, req.username);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement("""
                UPDATE user_account_status
                SET customer_grade=?, overnight_setting=?, account_status=?, server=?
                WHERE user_id = (SELECT id FROM users WHERE username=?)
            """)) {
                ps.setString(1, req.grade);
                ps.setBoolean(2, "허용".equals(req.overnight));
                ps.setString(3, req.accountStatus);
                ps.setString(4, req.server);
                ps.setString(5, req.username);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE user_status SET mileage=? WHERE user_id = (SELECT id FROM users WHERE username=?)")) {
                ps.setLong(1, req.mileage == null || req.mileage.isEmpty() ? 0 : Long.parseLong(req.mileage));
                ps.setString(2, req.username);
                ps.executeUpdate();
            }

            // 2-2. userId 조회 + 추천인 이력
            int userId = -1;
            try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM users WHERE username=?")) {
                ps.setString(1, req.username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) userId = rs.getInt("id");
                }
            }

            if (userId == -1) {
                conn.rollback();
                return "유저를 찾을 수 없습니다";
            }

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

            // 2-3. 수수료
            try (PreparedStatement ps = conn.prepareStatement("""
                UPDATE user_fee_settings
                SET futures_fee=?, night_futures_fee=?, options_fee=?, night_options_fee=?
                WHERE user_id = (SELECT id FROM users WHERE username=?)
            """)) {
                ps.setDouble(1, Double.parseDouble(req.futuresFee.isEmpty() ? "0" : req.futuresFee));
                ps.setDouble(2, Double.parseDouble(req.nightFuturesFee.isEmpty() ? "0" : req.nightFuturesFee));
                ps.setDouble(3, Double.parseDouble(req.optionsFee.isEmpty() ? "0" : req.optionsFee));
                ps.setDouble(4, Double.parseDouble(req.nightOptionsFee.isEmpty() ? "0" : req.nightOptionsFee));
                ps.setString(5, req.username);
                ps.executeUpdate();
            }

            if (req.overseasFees != null) {
                try (PreparedStatement ps = conn.prepareStatement("""
                    UPDATE user_overseas_fees SET fee=?
                    WHERE user_id=(SELECT id FROM users WHERE username=?) AND symbol=?
                """)) {
                    for (OverseasFeeRow row : req.overseasFees) {
                        String symbol = KOR_TO_SYMBOL.getOrDefault(row.getSymbolKor(), row.getSymbolKor());
                        ps.setDouble(1, row.getFee());
                        ps.setString(2, req.username);
                        ps.setString(3, symbol);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }

            // 2-4. 계약수
            try (PreparedStatement ps = conn.prepareStatement("""
                UPDATE user_qty_limits
                SET max_futures_qty=?, max_options_qty=?, max_overseas_qty=?
                WHERE user_id=(SELECT id FROM users WHERE username=?)
            """)) {
                ps.setInt(1, req.maxFuturesQty);
                ps.setInt(2, req.maxOptionsQty);
                ps.setInt(3, req.maxOverseasQty);
                ps.setString(4, req.username);
                ps.executeUpdate();
            }

            if (req.overseasQtyRows != null) {
                try (PreparedStatement ps = conn.prepareStatement("""
                    UPDATE user_overseas_qty_limits SET max_qty=?
                    WHERE user_id=(SELECT id FROM users WHERE username=?) AND symbol=?
                """)) {
                    for (OverseasQtyRow row : req.overseasQtyRows) {
                        if (row.getMaxQty() == null) {
                            ps.setNull(1, Types.INTEGER);
                        } else {
                            ps.setInt(1, row.getMaxQty());
                        }
                        ps.setString(2, req.username);
                        ps.setString(3, row.getSymbol());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }




            conn.commit();
            new service.CustomerDepositService().pushAccountInfoToUser(userId);
            return null; // 성공

        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            return "저장 중 오류가 발생했습니다";
        }
    }
}
