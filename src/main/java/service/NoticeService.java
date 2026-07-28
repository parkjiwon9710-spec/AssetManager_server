package service;

import db.DBUtil;
import model.Notice;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NoticeService {

    public boolean addNotice(String title, String type, String contentRtf) {
        String sql = "INSERT INTO notices(title, content, type, created_at) VALUES(?, ?, ?, NOW())";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setString(2, contentRtf);
            stmt.setString(3, type);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Notice> getNotices() {
        List<Notice> list = new ArrayList<>();
        String sql =
                "SELECT id, title, content, type, created_at FROM notices ORDER BY " +
                        "CASE type WHEN '이벤트' THEN 1 WHEN '필독' THEN 2 WHEN '최상위' THEN 3 ELSE 4 END, " +
                        "created_at DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new Notice(
                        rs.getInt("id"), rs.getString("title"), rs.getString("content"),
                        rs.getString("type"), rs.getTimestamp("created_at")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateNotice(int id, String title, String contentRtf, String type) {
        String sql = "UPDATE notices SET title=?, content=?, type=? WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, contentRtf);
            pstmt.setString(3, type);
            pstmt.setInt(4, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteNotice(int id) {
        String sql = "DELETE FROM notices WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    ///필독공지관련
    public List<Notice> getMustReadNotices(int userId) {
        List<Notice> list = new ArrayList<>();
        String sql =
                "SELECT n.id, n.title, n.content, n.type, n.created_at " +
                        "FROM notices n " +
                        "WHERE n.type='필독' " +
                        "AND NOT EXISTS (SELECT 1 FROM notice_reads r WHERE r.notice_id = n.id AND r.user_id = ?) " +
                        "ORDER BY n.created_at DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Notice(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("content"),
                            rs.getString("type"),
                            rs.getTimestamp("created_at")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean markNoticeRead(int userId, int noticeId) {
        String sql = "INSERT IGNORE INTO notice_reads (notice_id, user_id) VALUES (?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, noticeId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
/// //////
