package service;

import db.DBUtil;
import model.FeeOverrideRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SymbolFeeOverrideDAO {

    public boolean isOverrideEnabled(String symbol) {
        String sql = "SELECT override_enabled FROM symbol_fee_override WHERE symbol=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, symbol);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBoolean("override_enabled");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public double getOverrideFee(String symbol) {
        String sql = "SELECT override_fee FROM symbol_fee_override WHERE symbol=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, symbol);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("override_fee");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 2.5;
    }

    public void setOverride(String symbol, boolean enabled, double fee) {
        String sql = """
            INSERT INTO symbol_fee_override (symbol, override_enabled, override_fee)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE override_enabled=?, override_fee=?
            """;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, symbol);
            ps.setBoolean(2, enabled);
            ps.setDouble(3, fee);
            ps.setBoolean(4, enabled);
            ps.setDouble(5, fee);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 전체 종목의 현재 오버라이드 상태 한 번에 조회
    public List<FeeOverrideRow> getAllOverrides(List<String> symbols) {
        List<model.FeeOverrideRow> list = new ArrayList<>();
        for (String symbol : symbols) {
            boolean enabled = isOverrideEnabled(symbol);
            double fee = getOverrideFee(symbol);
            list.add(new model.FeeOverrideRow(symbol, enabled, fee));
        }
        return list;
    }

    // 여러 종목을 한 번에 저장
    public void saveAllOverrides(List<model.FeeOverrideRow> rows) {
        for (model.FeeOverrideRow row : rows) {
            setOverride(row.getSymbol(), row.isEnabled(), row.getFee());
        }
    }
}
