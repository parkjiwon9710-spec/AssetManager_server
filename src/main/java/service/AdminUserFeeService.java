package service;

import model.AdminUserFeeData;
import model.OverseasFeeRow;
import db.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminUserFeeService {

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

    public AdminUserFeeData loadFeeSettings(String username) {

        AdminUserFeeData data = new AdminUserFeeData();

        String sql1 = """
            SELECT futures_fee, night_futures_fee, options_fee, night_options_fee
            FROM user_fee_settings
            WHERE user_id = (SELECT id FROM users WHERE username = ?)
        """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql1)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                data.setFuturesFee(String.valueOf(rs.getDouble("futures_fee")));
                data.setNightFuturesFee(String.valueOf(rs.getDouble("night_futures_fee")));
                data.setOptionsFee(String.valueOf(rs.getDouble("options_fee")));
                data.setNightOptionsFee(String.valueOf(rs.getDouble("night_options_fee")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
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

        List<OverseasFeeRow> overseasFees = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql2)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                overseasFees.add(new OverseasFeeRow(
                        symbolToKor(rs.getString("symbol")),
                        rs.getDouble("fee")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        data.setOverseasFees(overseasFees);
        return data;
    }

    public String saveDomesticFee(String username, String futuresFee, String nightFuturesFee,
                                  String optionsFee, String nightOptionsFee) {

        String sql = """
            UPDATE user_fee_settings
            SET futures_fee = ?, night_futures_fee = ?, options_fee = ?, night_options_fee = ?
            WHERE user_id = (SELECT id FROM users WHERE username = ?)
        """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, Double.parseDouble(futuresFee.isEmpty() ? "0" : futuresFee));
            ps.setDouble(2, Double.parseDouble(nightFuturesFee.isEmpty() ? "0" : nightFuturesFee));
            ps.setDouble(3, Double.parseDouble(optionsFee.isEmpty() ? "0" : optionsFee));
            ps.setDouble(4, Double.parseDouble(nightOptionsFee.isEmpty() ? "0" : nightOptionsFee));
            ps.setString(5, username);

            ps.executeUpdate();
            return null;

        } catch (SQLException e) {
            e.printStackTrace();
            return "국내 수수료 저장 실패";
        } catch (NumberFormatException e) {
            return "숫자만 입력하세요";
        }
    }

    public String saveOverseasFee(String username, List<OverseasFeeRow> rows) {

        String sql = """
            UPDATE user_overseas_fees
            SET fee = ?
            WHERE user_id = (SELECT id FROM users WHERE username = ?)
            AND symbol = ?
        """;

        try (Connection conn = DBUtil.getConnection()) {

            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (OverseasFeeRow row : rows) {
                    String symbol = KOR_TO_SYMBOL.getOrDefault(row.getSymbolKor(), row.getSymbolKor());

                    ps.setDouble(1, row.getFee());
                    ps.setString(2, username);
                    ps.setString(3, symbol);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
            return null;

        } catch (SQLException e) {
            e.printStackTrace();
            return "해외 수수료 저장 실패";
        }
    }
}
