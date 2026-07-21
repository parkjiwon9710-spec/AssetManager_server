package service;

import Market.MarketSpec;
import Market.MarketSpecCache;
import model.AdminUserQtyLimitData;
import model.OverseasQtyRow;
import db.DBUtil;
import model.SystemQtyLimit;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminUserQtyLimitService {

    public AdminUserQtyLimitData loadQtyLimits(String username) {

        String sql = """
            SELECT max_futures_qty, max_options_buy_qty, max_options_sell_qty, max_overseas_qty
            FROM user_qty_limits
            WHERE user_id = (SELECT id FROM users WHERE username = ?)
        """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                AdminUserQtyLimitData data = new AdminUserQtyLimitData();
                data.setMaxFuturesQty(rs.getInt("max_futures_qty"));
                data.setMaxOptionsBuyQty(rs.getInt("max_options_buy_qty"));
                data.setMaxOptionsSellQty(rs.getInt("max_options_sell_qty"));
                data.setMaxOverseasQty(rs.getInt("max_overseas_qty"));
                return data;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // 🔥 시스템 전체 최대 해외선물 계약수 (SystemQtyLimitDAO 서버 버전)
    public int getSystemMaxOverseasQty() {

        String sql = "SELECT max_overseas_qty FROM system_qty_limits WHERE id = 1";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("max_overseas_qty");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Integer.MAX_VALUE;
    }

    public String saveQtyLimits(String username, int maxFuturesQty, int maxOptionsBuyQty,
                                int maxOptionsSellQty, int maxOverseasQty) {

        SystemQtyLimit systemLimit = new SystemQtyLimitDAO().getSettings();

        if (systemLimit == null) {
            return "시스템 설정 조회 실패";
        }

        if (maxFuturesQty > systemLimit.getMaxFuturesQty()) {
            return "시스템 국내선물 최대계약수(" + systemLimit.getMaxFuturesQty() + ")를 초과할 수 없습니다.";
        }

        if (maxOptionsBuyQty > systemLimit.getMaxOptionsBuyQty()) {
            return "시스템 옵션매수 최대계약수(" + systemLimit.getMaxOptionsBuyQty() + ")를 초과할 수 없습니다.";
        }

        if (maxOptionsSellQty > systemLimit.getMaxOptionsSellQty()) {
            return "시스템 옵션매도 최대계약수(" + systemLimit.getMaxOptionsSellQty() + ")를 초과할 수 없습니다.";
        }

        if (maxOverseasQty > systemLimit.getMaxOverseasQty()) {
            return "시스템 해외선물 최대계약수(" + systemLimit.getMaxOverseasQty() + ")를 초과할 수 없습니다.";
        }

        String sql = """
        UPDATE user_qty_limits
        SET max_futures_qty=?, max_options_buy_qty=?, max_options_sell_qty=?, max_overseas_qty=?
        WHERE user_id=(SELECT id FROM users WHERE username=?)
    """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maxFuturesQty);
            ps.setInt(2, maxOptionsBuyQty);
            ps.setInt(3, maxOptionsSellQty);
            ps.setInt(4, maxOverseasQty);
            ps.setString(5, username);
            ps.executeUpdate();

            return null;

        } catch (SQLException e) {
            e.printStackTrace();
            return "계약수 저장 실패";
        }
    }

    // 🔥 해외 종목별 계약수 - 전체 해외선물 종목 목록 + 저장된 개별값 합쳐서 반환
    public List<OverseasQtyRow> loadOverseasQtyLimits(String username) {

        Map<String, Integer> savedMap = new HashMap<>();

        String sql = """
            SELECT symbol, max_qty
            FROM user_overseas_qty_limits
            WHERE user_id = (SELECT id FROM users WHERE username = ?)
        """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                savedMap.put(rs.getString("symbol"), rs.getInt("max_qty"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        List<OverseasQtyRow> result = new ArrayList<>();

        for (MarketSpec spec : MarketSpecCache.getAll()) {
            if (!"OVERSEAS_FUTURES".equalsIgnoreCase(spec.getMarketType())) {
                continue;
            }
            Integer qty = savedMap.get(spec.getSymbol());
            result.add(new OverseasQtyRow(spec.getSymbol(), qty)); // qty가 없으면 null
        }

        return result;
    }

    public String saveOverseasQtyLimits(String username, List<OverseasQtyRow> rows, int overseasMax) {

        // 🔥 검증 - 클라이언트에서도 하지만 서버에서도 재검증
        for (OverseasQtyRow row : rows) {
            if (row.getMaxQty() == null) continue;
            if (row.getMaxQty() < 0) {
                return "0 이상만 입력 가능합니다.";
            }
            if (row.getMaxQty() > overseasMax) {
                return "개별 계약수는 해외선물 최대계약수(" + overseasMax + ")를 초과할 수 없습니다.";
            }
        }

        String sql = """
            UPDATE user_overseas_qty_limits
            SET max_qty=?
            WHERE user_id=(SELECT id FROM users WHERE username=?)
            AND symbol=?
        """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            for (OverseasQtyRow row : rows) {
                if (row.getMaxQty() == null) {
                    ps.setNull(1, Types.INTEGER);
                } else {
                    ps.setInt(1, row.getMaxQty());
                }
                ps.setString(2, username);
                ps.setString(3, row.getSymbol());
                ps.addBatch();
            }

            ps.executeBatch();
            conn.commit();

            return null;

        } catch (SQLException e) {
            e.printStackTrace();
            return "해외 종목 계약수 저장 실패";
        }
    }
}