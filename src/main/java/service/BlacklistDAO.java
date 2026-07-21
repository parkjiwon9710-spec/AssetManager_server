package service;

import db.DBUtil;

import java.sql.*;

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
}