package service;

import db.DBUtil;
import model.LoginHistoryRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class LoginHistoryDAO {

    public void insert(int userId, String username, String name, String ip, String mac, String eventType) {
        String sql = "INSERT INTO login_history (user_id, username, name, ip, mac, event_type, event_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, NOW())";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, username);
            pstmt.setString(3, name);
            pstmt.setString(4, ip);
            pstmt.setString(5, mac);
            pstmt.setString(6, eventType);

            pstmt.executeUpdate();

        } catch (Exception e) {
            System.err.println("[LoginHistoryDAO] insert 실패 - userId: " + userId + ", eventType: " + eventType);
            e.printStackTrace();
        }
    }

    public List<LoginHistoryRow> loadHistory(Timestamp start, Timestamp end, String keyword) {
        List<LoginHistoryRow> rows = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT user_id, username, name, ip, mac, event_type, event_time " +
                        "FROM login_history " +
                        "WHERE event_time BETWEEN ? AND ? "
        );

        boolean hasKeyword = (keyword != null && !keyword.trim().isEmpty());
        if (hasKeyword) {
            sql.append("AND (username LIKE ? OR name LIKE ?) ");
        }
        sql.append("ORDER BY event_time DESC");

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            pstmt.setTimestamp(idx++, start);
            pstmt.setTimestamp(idx++, end);
            if (hasKeyword) {
                String likeKeyword = "%" + keyword.trim() + "%";
                pstmt.setString(idx++, likeKeyword);
                pstmt.setString(idx++, likeKeyword);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    rows.add(new LoginHistoryRow(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("name"),
                            rs.getString("ip"),
                            rs.getString("mac"),
                            rs.getString("event_type"),
                            rs.getTimestamp("event_time").getTime()
                    ));
                }
            }

        } catch (Exception e) {
            System.err.println("[LoginHistoryDAO] loadHistory 실패");
            e.printStackTrace();
        }

        return rows;
    }
}