package service;

import Market.MarketSpecCache;
import db.DBUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class UserDataDAO {

    public void loadCustomers(String keyword, DefaultTableModel model) {

        model.setRowCount(0);

        String sql = """
    SELECT
        u.created_at,
        u.username,
        u.name,

        up.phone,
        up.recommender,

        u.balance,

        us.total_pnl,
        us.total_fee,
        us.total_winrate,
        us.is_online,

        u.account_type,

        uas.customer_grade,
        uas.account_status,

        u.password,

        up.email,

        uas.server,

      uas.overnight_setting,

        us.trade_count,
        us.trade_days,

        up.bank,
        up.account_number,
        up.account_holder,
        up.deposit_account,

        us.last_trade_time,

        ufs.futures_fee,
        ufs.options_fee,
        ufs.night_futures_fee,
        ufs.night_options_fee,

        uql.max_futures_qty,
        uql.max_options_buy_qty,
        uql.max_options_sell_qty,
        uql.max_overseas_qty,

        up.memo_customer,
        up.memo_partner,

        us.last_login,
        us.login_fail_count,

        up.join_ip,
        up.join_mac,

        upe.overnight_permission,
        upe.chat_permission

    FROM users u

    LEFT JOIN user_profiles up
        ON u.id = up.user_id

    LEFT JOIN user_account_status uas
        ON u.id = uas.user_id

    LEFT JOIN user_fee_settings ufs
        ON u.id = ufs.user_id

    LEFT JOIN user_qty_limits uql
        ON u.id = uql.user_id

    LEFT JOIN user_status us
        ON u.id = us.user_id

    LEFT JOIN user_permissions upe
        ON u.id = upe.user_id

    WHERE u.role = 'USER'
    AND (u.username LIKE ? OR u.name LIKE ?)

    ORDER BY u.created_at DESC
""";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String kw = "%" + keyword + "%";
            ps.setString(1, kw);
            ps.setString(2, kw);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getTimestamp("created_at"),
                        rs.getString("username"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("recommender"),
                        rs.getLong("balance"),
                        rs.getDouble("total_pnl"),
                        rs.getDouble("total_fee"),
                        rs.getInt("total_winrate") + "%",
                        rs.getBoolean("is_online") ? "온라인" : "오프라인",
                        rs.getString("account_type"),
                        rs.getString("customer_grade"),
                        rs.getString("account_status"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getString("server"),
                        rs.getBoolean("overnight_setting") ? "허용" : "미허용",
                        rs.getInt("trade_count"),
                        rs.getInt("trade_days"),
                        rs.getString("bank"),
                        rs.getString("account_number"),
                        rs.getString("account_holder"),
                        rs.getString("deposit_account"),
                        rs.getTimestamp("last_trade_time") == null ? "-" : rs.getTimestamp("last_trade_time"),
                        rs.getDouble("futures_fee"),
                        rs.getDouble("options_fee"),
                        rs.getDouble("night_futures_fee"),
                        rs.getDouble("night_options_fee"),
//                        rs.getDouble("overseas_futures_fee"),
                        rs.getInt("max_futures_qty"),
                        rs.getInt("max_options_buy_qty"),
                        rs.getInt("max_options_sell_qty"),
                        rs.getInt("max_overseas_qty"),
//                        rs.getInt("overseas_limit_qty"),
                        rs.getString("memo_customer"),
                        rs.getString("memo_partner"),
                        rs.getTimestamp("last_login") == null ? "-" : rs.getTimestamp("last_login"),
                        rs.getInt("login_fail_count"),
                        rs.getString("join_ip"),
                        rs.getString("join_mac"),
                        rs.getBoolean("overnight_permission") ? "허용" : "미허용",
                        rs.getBoolean("chat_permission") ? "허용" : "미허용"
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "고객 정보 조회 중 오류가 발생했습니다.");
        }
    }


    public void loadBasicInfo(String username, java.util.function.Consumer<String[]> callback) {

        String sql = """
        SELECT
            u.created_at, u.username, u.name, u.password, up.email,
            up.phone, up.recommender, u.role, uas.customer_grade,
          up.memo_partner, up.bank, up.account_number, up.account_holder,
                      up.deposit_account, uas.overnight_setting
        FROM users u
       LEFT JOIN user_profiles up
                       ON u.id = up.user_id
                
                   LEFT JOIN user_account_status uas
                       ON u.id = uas.user_id
                
                   LEFT JOIN user_fee_settings ufs
                       ON u.id = ufs.user_id
                
                   LEFT JOIN user_qty_limits uql
                       ON u.id = uql.user_id
                
                   WHERE u.username = ?
    """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String[] data = {
                        rs.getTimestamp("created_at") == null ? "-" : rs.getTimestamp("created_at").toString(),
                        rs.getString("username"),
                        rs.getString("name") == null ? "" : rs.getString("name"),
                        rs.getString("password") == null ? "" : rs.getString("password"),
                        rs.getString("email") == null ? "" : rs.getString("email"),
                        rs.getString("phone") == null ? "" : rs.getString("phone"),
                        rs.getString("recommender") == null ? "" : rs.getString("recommender"),
                        rs.getString("role") == null ? "" : rs.getString("role"),
                        rs.getString("customer_grade") == null ? "" : rs.getString("customer_grade"),
                        rs.getString("memo_partner") == null ? "" : rs.getString("memo_partner"),
                        rs.getString("bank") == null ? "" : rs.getString("bank"),
                        rs.getString("account_number") == null ? "" : rs.getString("account_number"),
                        rs.getString("account_holder") == null ? "" : rs.getString("account_holder"),
                        rs.getString("deposit_account") == null ? "" : rs.getString("deposit_account"),
                        rs.getBoolean("overnight_setting") ? "허용" : "미허용",
                        "미허용" // 리모트컨트롤 - DB컬럼 추가 전까지 기본값
                };
                callback.accept(data);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "기본정보 조회 중 오류가 발생했습니다.");
        }
    }

    public void saveBasicInfo(String username, String name, String password,
                              String email, String phone, String recommender,
                              String grade, String partnerMemo, String bank,
                              String accountNumber, String accountHolder,
                              String depositAccount, String overnight, String remote) {

        String sql1 = "UPDATE users SET name=?, password=? WHERE username=?";
        String sql2 = """
        UPDATE user_profiles SET email=?, phone=?, recommender=?,
        memo_partner=?, bank=?, account_number=?, account_holder=?, deposit_account=?
        WHERE user_id = (SELECT id FROM users WHERE username=?)
    """;
        String sql3 = """
    UPDATE user_account_status
    SET customer_grade=?, overnight_setting=?
    WHERE user_id = (
        SELECT id FROM users WHERE username=?
    )
""";

        try (Connection conn = DBUtil.getConnection()) {

            // users 테이블 업데이트
            PreparedStatement ps1 = conn.prepareStatement(sql1);
            ps1.setString(1, name);
            ps1.setString(2, password);
            ps1.setString(3, username);
            ps1.executeUpdate();

            // user_profiles 테이블 업데이트
            PreparedStatement ps2 = conn.prepareStatement(sql2);
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

            // user_trading_settings 테이블 업데이트
            PreparedStatement ps3 = conn.prepareStatement(sql3);
            ps3.setString(1, grade);
            ps3.setBoolean(2, overnight.equals("허용"));
            ps3.setString(3, username);
            ps3.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "저장 중 오류가 발생했습니다.");
        }
    }



    public void loadTradeInfo(String username, java.util.function.Consumer<String[]> callback) {

        String sql = """
    SELECT
        uas.account_status,
        uas.server,

        upe.overnight_permission,
        upe.chat_permission,

        uql.max_futures_qty,
        uql.max_options_buy_qty,
        uql.max_options_sell_qty,
        uql.max_overseas_qty

    FROM users u

    LEFT JOIN user_account_status uas
        ON u.id = uas.user_id

    LEFT JOIN user_qty_limits uql
        ON u.id = uql.user_id

    LEFT JOIN user_permissions upe
        ON u.id = upe.user_id

    WHERE u.username = ?
""";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String[] data = {
                        rs.getString("account_status") == null ? "ACTIVE" : rs.getString("account_status"),
                        rs.getString("server") == null ? "" : rs.getString("server"),
                        rs.getBoolean("overnight_permission") ? "허용" : "미허용",
                        rs.getBoolean("chat_permission") ? "허용" : "미허용",
                        String.valueOf(rs.getInt("max_futures_qty")),
                        String.valueOf(rs.getInt("max_options_buy_qty")),
                        String.valueOf(rs.getInt("max_options_sell_qty")),
                        String.valueOf(rs.getInt("max_overseas_qty"))
                };
                callback.accept(data);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "거래설정 조회 중 오류가 발생했습니다.");
        }
    }
//기존회원수정
public void saveTradeInfo(String username, String accountStatus, String server,
                          String overnightPerm, String chatPerm,
                          String maxFuturesQty, String maxOptionsBuyQty,
                          String maxOptionsSellQty, String maxOverseasQty,
                          String overseasLimitQty) {

    String sql1 = """
    UPDATE user_account_status
    SET account_status=?, server=?
    WHERE user_id = (SELECT id FROM users WHERE username=?)
    """;

    String sql2 = """
    UPDATE user_qty_limits
    SET
        max_futures_qty=?,
        max_options_buy_qty=?,
        max_options_sell_qty=?,
        max_overseas_qty=?
    WHERE user_id = (SELECT id FROM users WHERE username=?)
    """;

    String sql3 = """
    UPDATE user_permissions
    SET
        overnight_permission=?,
        chat_permission=?
    WHERE user_id = (SELECT id FROM users WHERE username=?)
    """;

    try (Connection conn = DBUtil.getConnection()) {

        conn.setAutoCommit(true);

        try (PreparedStatement ps1 = conn.prepareStatement(sql1)) {
            ps1.setString(1, accountStatus);
            ps1.setString(2, server);
            ps1.setString(3, username);
            ps1.executeUpdate();
        }

        try (PreparedStatement ps2 = conn.prepareStatement(sql2)) {
            ps2.setInt(1, parseInt(maxFuturesQty));
            ps2.setInt(2, parseInt(maxOptionsBuyQty));
            ps2.setInt(3, parseInt(maxOptionsSellQty));
            ps2.setInt(4, parseInt(maxOverseasQty));
            ps2.setString(5, username); // ✔ 여기
            ps2.executeUpdate();
        }

        try (PreparedStatement ps3 = conn.prepareStatement(sql3)) {
            ps3.setBoolean(1, overnightPerm.equals("허용"));
            ps3.setBoolean(2, chatPerm.equals("허용"));
            ps3.setString(3, username);
            ps3.executeUpdate();
        }

    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "저장 중 오류가 발생했습니다.");
    }
}



    public void loadAccountData(String username, java.util.function.Consumer<String[]> callback) {

        String sql = """
    SELECT
        uas.account_status,
        uas.server,

        us.is_online,
        us.last_login,

        up.join_ip,
        up.join_mac,

        us.login_fail_count,
        us.last_trade_time,

        us.trade_count,
        us.trade_days,

        us.total_pnl,
        us.total_fee,
        us.total_winrate,

        us.mileage,

        up.memo_customer

    FROM users u

    LEFT JOIN user_account_status uas
        ON u.id = uas.user_id

    LEFT JOIN user_status us
        ON u.id = us.user_id

    LEFT JOIN user_profiles up
        ON u.id = up.user_id

    WHERE u.username = ?
""";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String[] data = {
                        rs.getString("account_status") == null ? "정상" : rs.getString("account_status"),
                        rs.getString("server") == null ? "" : rs.getString("server"),
                        rs.getBoolean("is_online") ? "온라인" : "오프라인",
                        rs.getTimestamp("last_login") == null ? "-" : rs.getTimestamp("last_login").toString(),
                        rs.getString("join_ip") == null ? "" : rs.getString("join_ip"),
                        rs.getString("join_mac") == null ? "" : rs.getString("join_mac"),
                        String.valueOf(rs.getInt("login_fail_count")),
                        rs.getTimestamp("last_trade_time") == null ? "-" : rs.getTimestamp("last_trade_time").toString(),
                        String.valueOf(rs.getInt("trade_count")),
                        String.valueOf(rs.getInt("trade_days")),
                        String.valueOf(rs.getDouble("total_pnl")),
                        String.valueOf(rs.getDouble("total_fee")),
                        String.valueOf(rs.getInt("total_winrate")),
                        String.valueOf(rs.getLong("mileage")),
                        rs.getString("memo_customer") == null ? "" : rs.getString("memo_customer")
                };
                callback.accept(data);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "계정정보 조회 중 오류가 발생했습니다.");
        }
    }

    public void saveAccountData(String username, String accountStatus, String server,
                                String mileage, String memo) {

        String sql1 = """
    UPDATE user_account_status
    SET account_status=?, server=?
    WHERE user_id = (
        SELECT id FROM users WHERE username=?
    )
""";

        String sql2 = """
        UPDATE user_status SET mileage=?
        WHERE user_id = (SELECT id FROM users WHERE username=?)
    """;

        String sql3 = """
        UPDATE user_profiles SET memo_customer=?
        WHERE user_id = (SELECT id FROM users WHERE username=?)
    """;

        try (Connection conn = DBUtil.getConnection()) {

            PreparedStatement ps1 = conn.prepareStatement(sql1);
            ps1.setString(1, accountStatus);
            ps1.setString(2, server);
            ps1.setString(3, username);
            ps1.executeUpdate();

            PreparedStatement ps2 = conn.prepareStatement(sql2);
            ps2.setLong(1, mileage.isEmpty() ? 0 : Long.parseLong(mileage));
            ps2.setString(2, username);
            ps2.executeUpdate();

            PreparedStatement ps3 = conn.prepareStatement(sql3);
            ps3.setString(1, memo);
            ps3.setString(2, username);
            ps3.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "저장 중 오류가 발생했습니다.");
        }
    }





    //관리자입출금패널관련
    public void loadBalance(String username, java.util.function.Consumer<Long> callback) {

        String sql = "SELECT balance FROM users WHERE username = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                callback.accept(rs.getLong("balance"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "담보금 조회 중 오류가 발생했습니다.");
        }
    }

    public void updateBalance(String username, long delta, String memo) {



        String sql1 =
                "UPDATE users SET balance = balance + ? WHERE username = ?";

        String sql2 = """
        UPDATE user_profiles
        SET memo_customer = ?
        WHERE user_id = (
            SELECT id
            FROM users
            WHERE username = ?
        )
    """;

        String userSql =
                "SELECT id FROM users WHERE username = ?";

        String depositSql = """
     INSERT INTO deposit_requests
                     (
                         user_id,
                         type,
                         amount,
                         status,
                         processed_at,
                         request_source,
                         remark,
                         admin_memo,
                         partner_username
                     )

VALUES
(
    ?, ?, ?, 'APPROVED', NOW(),
    'ADMIN', ?, ?, ?
)
""";

        try (Connection conn = DBUtil.getConnection()) {

            conn.setAutoCommit(false);

            // 잔액 변경
            PreparedStatement ps1 = conn.prepareStatement(sql1);
            ps1.setLong(1, delta);
            ps1.setString(2, username);
            ps1.executeUpdate();

            // user_id 조회
            int userId = 0;

            PreparedStatement userPs =
                    conn.prepareStatement(userSql);

            userPs.setString(1, username);

            ResultSet rs = userPs.executeQuery();

            if (rs.next()) {
                userId = rs.getInt("id");
            }

            String partnerUsername =
                    getPartnerUsername(userId);

            // 입출금 기록 저장
            PreparedStatement depositPs =
                    conn.prepareStatement(depositSql);

            depositPs.setInt(1, userId);
            depositPs.setString(2, delta > 0 ? "DEPOSIT" : "WITHDRAW");
            depositPs.setLong(3, Math.abs(delta));
            depositPs.setString(4, "관리자");
            depositPs.setString(5, memo);
            depositPs.setString(6, partnerUsername);
            depositPs.executeUpdate();

            // 메모 저장
            if (memo != null && !memo.isEmpty()) {

                PreparedStatement ps2 =
                        conn.prepareStatement(sql2);

                ps2.setString(1, memo);
                ps2.setString(2, username);

                ps2.executeUpdate();
            }

            conn.commit();

        } catch (SQLException e) {

            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    null,
                    "담보금 업데이트 중 오류가 발생했습니다."
            );
        }
    }

    public double getAvailableMargin(int userId){


        String sql =
                "SELECT balance " +
                        "FROM users " +
                        "WHERE id=?";


        try(
                Connection conn = DBUtil.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ){

            ps.setInt(1, userId);


            ResultSet rs = ps.executeQuery();


            if(rs.next()){

                return rs.getDouble("balance");

            }


        }catch(Exception e){

            e.printStackTrace();

        }


        return 0;

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

    public void loadFeeSettings(String username,
                                java.util.function.Consumer<String[]> domesticCallback,
                                DefaultTableModel overseasModel) {

        String sql1 = """
        SELECT
            futures_fee,
            night_futures_fee,
            options_fee,
            night_options_fee
        FROM user_fee_settings
        WHERE user_id = (
            SELECT id FROM users WHERE username = ?
        )
    """;

        try (
                Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql1)
        ) {

            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                String[] data = {
                        String.valueOf(rs.getDouble("futures_fee")),
                        String.valueOf(rs.getDouble("night_futures_fee")),
                        String.valueOf(rs.getDouble("options_fee")),
                        String.valueOf(rs.getDouble("night_options_fee"))
                };

                domesticCallback.accept(data);
            }

        } catch (SQLException e) {

            e.printStackTrace();

            JOptionPane.showMessageDialog(null, "국내 수수료 조회 실패");
        }

        String sql2 = """
    SELECT ms.symbol, COALESCE(uof.fee, ms.fee_per_contract) AS fee
    FROM market_specs ms
    LEFT JOIN user_overseas_fees uof
        ON ms.symbol = uof.symbol
        AND uof.user_id = (SELECT id FROM users WHERE username = ?)
    WHERE ms.market_type = 'OVERSEAS_FUTURES'
    ORDER BY ms.sort_order
""";

        try (
                Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql2)
        ) {

            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();

            overseasModel.setRowCount(0);

            while (rs.next()) {

                overseasModel.addRow(new Object[]{
                        symbolToKor(rs.getString("symbol")),
                        rs.getDouble("fee")
                });
            }

        } catch (SQLException e) {

            e.printStackTrace();

            JOptionPane.showMessageDialog(null, "해외 수수료 조회 실패");
        }
    }


    public void saveDomesticFee(String username,
                                String futuresFee,
                                String nightFuturesFee,
                                String optionsFee,
                                String nightOptionsFee) {

        String sql = """
        UPDATE user_fee_settings
        SET
            futures_fee = ?,
            night_futures_fee = ?,
            options_fee = ?,
            night_options_fee = ?
        WHERE user_id = (
            SELECT id FROM users WHERE username = ?
        )
    """;

        try (
                Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setDouble(1, Double.parseDouble(
                    futuresFee.isEmpty() ? "0" : futuresFee
            ));

            ps.setDouble(2, Double.parseDouble(
                    nightFuturesFee.isEmpty() ? "0" : nightFuturesFee
            ));

            ps.setDouble(3, Double.parseDouble(
                    optionsFee.isEmpty() ? "0" : optionsFee
            ));

            ps.setDouble(4, Double.parseDouble(
                    nightOptionsFee.isEmpty() ? "0" : nightOptionsFee
            ));

            ps.setString(5, username);

            ps.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();

            JOptionPane.showMessageDialog(null, "국내 수수료 저장 실패");
        }
    }

    public void saveOverseasFee(String username,
                                DefaultTableModel model) {

        String sql = """
        UPDATE user_overseas_fees
        SET fee = ?
        WHERE user_id = (
            SELECT id FROM users WHERE username = ?
        )
        AND symbol = ?
    """;

        java.util.Map<String, String> korToSymbol = new java.util.HashMap<>();

        korToSymbol.put("금", "GOLD");
        korToSymbol.put("은", "SILVER");
        korToSymbol.put("크루드오일", "CRUDE_OIL");
        korToSymbol.put("S&P500", "SP500");
        korToSymbol.put("구리", "COPPER");
        korToSymbol.put("나스닥", "NASDAQ");
        korToSymbol.put("다우", "DOW");
        korToSymbol.put("미국채10년", "US_BOND_10Y");
        korToSymbol.put("영국파운드", "GBP");
        korToSymbol.put("유로FX", "EURO_FX");
        korToSymbol.put("일본엔", "JPY");
        korToSymbol.put("천연가스", "NATURAL_GAS");
        korToSymbol.put("캐나다달러", "CAD");
        korToSymbol.put("항셍지수", "HSI");
        korToSymbol.put("호주달러", "AUD");

        try (Connection conn = DBUtil.getConnection()) {

            for (int i = 0; i < model.getRowCount(); i++) {

                String kor = model.getValueAt(i, 0).toString();

                String symbol = korToSymbol.getOrDefault(kor, kor);

                double fee = Double.parseDouble(
                        model.getValueAt(i, 1).toString()
                );

                PreparedStatement ps = conn.prepareStatement(sql);

                ps.setDouble(1, fee);
                ps.setString(2, username);
                ps.setString(3, symbol);

                ps.executeUpdate();
            }

        } catch (SQLException e) {

            e.printStackTrace();

            JOptionPane.showMessageDialog(null, "해외 수수료 저장 실패");
        }
    }




    //신규가입할때
    public boolean insertNewUser(
            String username, String name, String password,
            String email, String phone, String recommender, String accountType,
            String grade, String partnerMemo, String customerMemo,
            String bank, String accountNumber, String accountHolder, String depositAccount,
            String overnight, String remote,
            String accountStatus, String server, String mileage, String memo,
            String balance,
            String futuresFee, String nightFuturesFee, String optionsFee, String nightOptionsFee,
            DefaultTableModel overseasFeeModel,
            DefaultTableModel qtyModel  // ← 변경
    ) {

        System.out.println("INSERT accountType FINAL = [" + accountType + "]");
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // 1. users
            int userId;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO users (username, name, password, role, balance, account_type) VALUES (?, ?, ?, 'USER', ?, ?) ",
                    Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, username);
                ps.setString(2, name);
                ps.setString(3, password);
                ps.setLong(4, parseLong(balance));

                ps.setString(5, accountType); // ★ 핵심 추가

                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                keys.next();
                userId = keys.getInt(1);
            }

            // 2. user_profiles
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO user_profiles (user_id, phone, email, recommender, bank, account_number, account_holder, deposit_account, memo_partner, memo_customer) VALUES (?,?,?,?,?,?,?,?,?,?)")) {
                ps.setInt(1, userId);
                ps.setString(2, phone);
                ps.setString(3, email);
                ps.setString(4, recommender);
                ps.setString(5, bank);
                ps.setString(6, accountNumber);
                ps.setString(7, accountHolder);
                ps.setString(8, depositAccount);
                ps.setString(9, partnerMemo);
                ps.setString(10, customerMemo);
                ps.executeUpdate();
            }

            // 3. user_account_status
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO user_account_status (user_id, account_status, customer_grade, server, overnight_setting) VALUES (?,?,?,?,?)")) {
                ps.setInt(1, userId);
                ps.setString(2, accountStatus);
                ps.setString(3, grade);
                ps.setString(4, server);
                ps.setBoolean(5, overnight.equals("허용"));
                ps.executeUpdate();
            }

            // 4. user_fee_settings
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO user_fee_settings (user_id, futures_fee, night_futures_fee, options_fee, night_options_fee) VALUES (?,?,?,?,?)")) {
                ps.setInt(1, userId);
                ps.setDouble(2, parseDouble(futuresFee));
                ps.setDouble(3, parseDouble(nightFuturesFee));
                ps.setDouble(4, parseDouble(optionsFee));
                ps.setDouble(5, parseDouble(nightOptionsFee));
                ps.executeUpdate();
            }


            // 5. user_overseas_fees
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO user_overseas_fees (user_id, symbol, fee) VALUES (?,?,?)")) {

                for (int i = 0; i < overseasFeeModel.getRowCount(); i++) {

                    String symbol =
                            overseasFeeModel.getValueAt(i, 0).toString();

                    ps.setInt(1, userId);
                    ps.setString(2, symbol);


                    double fee =
                            parseDouble(
                                    overseasFeeModel.getValueAt(i, 1).toString()
                            );


                    // 입력 안 했으면 market_specs 기본 수수료 사용
                    if (fee == 0) {
                        fee =
                                MarketSpecCache.get(symbol)
                                        .getFeePerContract();
                    }


                    ps.setDouble(3, fee);

                    ps.addBatch();
                }

                ps.executeBatch();
            }

            // 6. user_qty_limits (국내 3개: 국내선물, 옵션매수, 옵션매도)
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO user_qty_limits (user_id, max_futures_qty, max_options_buy_qty, max_options_sell_qty) VALUES (?,?,?,?)")) {
                ps.setInt(1, userId);
                ps.setInt(2, parseInt(qtyModel.getValueAt(0, 1).toString())); // 국내선물
                ps.setInt(3, parseInt(qtyModel.getValueAt(1, 1).toString())); // 옵션매수
                ps.setInt(4, parseInt(qtyModel.getValueAt(2, 1).toString())); // 옵션매도
                ps.executeUpdate();
            }

            // 7. user_overseas_qty_limits (해외 종목별, index 3부터)
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO user_overseas_qty_limits (user_id, symbol, max_qty) VALUES (?,?,?)")) {
                for (int i = 3; i < qtyModel.getRowCount(); i++) {
                    ps.setInt(1, userId);
                    ps.setString(2, qtyModel.getValueAt(i, 0).toString());
                    int qty = parseInt(qtyModel.getValueAt(i, 1).toString());
                    ps.setInt(3, qty > 0 ? qty : 10); // 0이면 기본값 10
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // 8. user_permissions
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO user_permissions (user_id, overnight_permission, chat_permission) VALUES (?,?,?)")) {
                ps.setInt(1, userId);
                ps.setBoolean(2, overnight.equals("허용"));
                ps.setBoolean(3, remote.equals("허용"));
                ps.executeUpdate();
            }

            // 9. user_status
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO user_status (user_id, mileage) VALUES (?,?)")) {
                ps.setInt(1, userId);
                ps.setLong(2, parseLong(mileage));
                ps.executeUpdate();
            }

            // 10. user_trade_permissions (기본값으로 INSERT)
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO user_trade_permissions (user_id) VALUES (?)")) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            // 11. user_overseas_permissions (기본값 enabled=true로 INSERT)
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO user_overseas_permissions (user_id, symbol, enabled, max_qty) VALUES (?,?,true,0)")) {
                for (int i = 3; i < qtyModel.getRowCount(); i++) {
                    ps.setInt(1, userId);
                    ps.setString(2, qtyModel.getValueAt(i, 0).toString());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            return false;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
        }
    }


//업체입출금계좌 id받아오는 메소드
    public String getDepositAccountId(int userId) {

        String sql = """
        SELECT deposit_account
        FROM user_profiles
        WHERE user_id = ?
        """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("deposit_account");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
//추천인 업데이트 메소드
    public void updateRecommenderWithHistory(int userId, String newRecommender) {

        String closeSql =
                "UPDATE user_partner_history " +
                        "SET end_time = NOW() " +
                        "WHERE user_id=? AND end_time IS NULL";

        String insertSql =
                "INSERT INTO user_partner_history " +
                        "(user_id, partner_username, start_time, end_time) " +
                        "VALUES (?, ?, NOW(), NULL)";

        try (Connection conn = DBUtil.getConnection()) {

            conn.setAutoCommit(false);

            try (PreparedStatement ps1 = conn.prepareStatement(closeSql)) {
                ps1.setInt(1, userId);
                ps1.executeUpdate();
            }

            try (PreparedStatement ps2 = conn.prepareStatement(insertSql)) {
                ps2.setInt(1, userId);
                ps2.setString(2, newRecommender.isEmpty() ? null : newRecommender);
                ps2.executeUpdate();
            }

            conn.commit();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//처음 신규가입자 추가할 때 추천인
    public void insertInitialPartnerHistory(int userId, String recommender) {

        String sql =
                "INSERT INTO user_partner_history " +
                        "(user_id, partner_username, start_time, end_time) " +
                        "VALUES (?, ?, NOW(), NULL)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, recommender.isEmpty() ? null : recommender);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getUserId(String username) {

        String sql = "SELECT id FROM users WHERE username=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    public String getPartnerUsername(int userId) {

        String sql =
                """
                SELECT recommender
                FROM user_profiles
                WHERE user_id=?
                """;

        try (
                Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("recommender");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }


    private double parseDouble(String s) {
        try { return Double.parseDouble(s); } catch (Exception e) { return 0; }
    }

    private long parseLong(String s) {
        try { return Long.parseLong(s); } catch (Exception e) { return 0; }
    }

    private int parseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }





}