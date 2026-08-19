package service;

import db.DBUtil;
import model.BlacklistRow;

import java.sql.*;
import java.util.List;

public class BlacklistDAO {

    public boolean isBlacklisted(String ip, String mac) {
        String sql = "SELECT COUNT(*) FROM blacklist WHERE (type='IP' AND value=?) OR (type='MAC' AND value=?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ip);
            ps.setString(2, mac);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addBlacklist(String type, String value, String reason) {
        String sql = "INSERT INTO blacklist (type, value, reason) VALUES (?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, type);
            ps.setString(2, value);
            ps.setString(3, reason);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public List<model.BlacklistRow> loadAll(String filterType) {

        List<BlacklistRow> rows = new java.util.ArrayList<>();

        String sql = (filterType == null)
                ? "SELECT id, type, value, reason, created_at FROM blacklist ORDER BY id DESC"
                : "SELECT id, type, value, reason, created_at FROM blacklist WHERE type = ? ORDER BY id DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (filterType != null) {
                ps.setString(1, filterType);
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                rows.add(new model.BlacklistRow(
                        rs.getInt("id"),
                        rs.getString("type"),
                        rs.getString("value"),
                        rs.getString("reason"),
                        rs.getTimestamp("created_at") != null
                                ? rs.getTimestamp("created_at").toString()
                                : ""
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rows;
    }

    public boolean delete(int id) {

        String sql = "DELETE FROM blacklist WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int affected = ps.executeUpdate();
            return affected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(int id, String value, String reason) {

        String sql = "UPDATE blacklist SET value = ?, reason = ? WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, value);
            ps.setString(2, reason);
            ps.setInt(3, id);

            int affected = ps.executeUpdate();
            return affected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}