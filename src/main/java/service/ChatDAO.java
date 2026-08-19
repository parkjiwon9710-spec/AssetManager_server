package service;

import db.DBUtil;
import model.ChatMessageRow;
import model.ChatListRow;
import java.sql.*;
import java.util.*;

public class ChatDAO {

    public List<ChatMessageRow> loadMessages(int userId) {
        List<ChatMessageRow> list = new ArrayList<>();
        String sql = """
            SELECT cm.sender_type, cm.sender_id, u.username, cm.message, cm.created_at,
                   cm.is_read_by_user, cm.is_read_by_admin
            FROM chat_messages cm
            JOIN users u ON cm.sender_id = u.id
            WHERE cm.user_id = ?
            ORDER BY cm.created_at ASC
        """;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new ChatMessageRow(
                        rs.getString("sender_type"),
                        rs.getInt("sender_id"),
                        rs.getString("username"),
                        rs.getString("message"),
                        rs.getString("created_at"),
                        rs.getBoolean("is_read_by_user"),
                        rs.getBoolean("is_read_by_admin")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // 방금 보낸 메시지 1건만 조회 (push용 - INSERT 직후 그 행을 다시 읽어옴)
    public ChatMessageRow findLatestMessage(int userId) {
        List<ChatMessageRow> all = loadMessages(userId);
        return all.isEmpty() ? null : all.get(all.size() - 1);
    }

    public boolean sendMessage(int userId, String senderType, int senderId, String msg) {
        String sql = """
            INSERT INTO chat_messages(user_id, sender_type, sender_id, message, created_at)
            VALUES(?,?,?,?,NOW())
        """;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, senderType);
            ps.setInt(3, senderId);
            ps.setString(4, msg);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public void markAsReadByUser(int userId) {
        String sql = "UPDATE chat_messages SET is_read_by_user = 1 WHERE user_id = ? AND sender_type = 'ADMIN' AND is_read_by_user = 0";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void markAsReadByAdmin(int userId) {
        String sql = "UPDATE chat_messages SET is_read_by_admin = 1 WHERE user_id = ? AND sender_type = 'USER' AND is_read_by_admin = 0";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public List<ChatListRow> getChatList() {
        List<ChatListRow> list = new ArrayList<>();
        String sql = """
            SELECT u.id as user_id, u.username, m.last_msg, m.last_time, m.unread
            FROM users u
            JOIN (
                SELECT user_id, MAX(created_at) as last_time,
                       SUBSTRING_INDEX(GROUP_CONCAT(message ORDER BY created_at DESC), ',', 1) as last_msg,
                       SUM(CASE WHEN is_read_by_admin = 0 AND sender_type = 'USER' THEN 1 ELSE 0 END) as unread
                FROM chat_messages
                GROUP BY user_id
            ) m ON u.id = m.user_id
            ORDER BY m.last_time DESC
        """;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new ChatListRow(
                        rs.getInt("user_id"), rs.getString("username"),
                        rs.getString("last_msg"), rs.getString("last_time"), rs.getInt("unread")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }



    public boolean deleteChatRooms(List<Integer> userIds) {
        if (userIds == null || userIds.isEmpty()) return false;

        String placeholders = String.join(",", java.util.Collections.nCopies(userIds.size(), "?"));
        String sql = "DELETE FROM chat_messages WHERE user_id IN (" + placeholders + ")";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < userIds.size(); i++) {
                ps.setInt(i + 1, userIds.get(i));
            }
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}