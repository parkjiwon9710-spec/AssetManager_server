package service;

import db.DBUtil;
import model.User;
import model.UserSearchRow;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setBalance(rs.getInt("balance"));
                user.setRole(rs.getString("role"));
                user.setName(rs.getString("name"));
                return user;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setBalance(rs.getInt("balance"));
                user.setRole(rs.getString("role"));
                return user;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User login(String username, String password) {
        String sql = "SELECT * FROM users WHERE username=? AND password=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));

                user.setPassword(rs.getString("password"));
                user.setBalance(rs.getInt("balance"));
                user.setRole(rs.getString("role"));
                user.setName(rs.getString("name"));
                return user;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addBalance(int userId, double delta) {
        String sql = "UPDATE users SET balance = balance + ? WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, delta);
            ps.setInt(2, userId);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean checkPassword(int userId, String currentPw) {

        String sql = "SELECT password FROM users WHERE id=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {   // ⭐ 먼저 이동

                String dbPw = rs.getString("password");


                System.out.println("DB PW RAW = [" + dbPw + "]");  // ← 추가
                System.out.println("INPUT PW = [" + currentPw + "]");  // ← 추가

                return dbPw.equals(currentPw);
            }else {
                System.out.println("userId=" + userId + " 조회결과 없음");  // ← 추가
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void updatePassword(int userId, String newPw) {

        String sql = "UPDATE users SET password=? WHERE id=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newPw);
            ps.setInt(2, userId);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    //일별손익 검색할 때 기준
    public List<UserSearchRow> searchUsers(String keyword, String searchType) {
        String column = switch (searchType) {
            case "아이디"  -> "u.username";
            case "이름"   -> "u.name";
            case "휴대폰" -> "p.phone";
            case "이메일" -> "p.email";
            case "추천인" -> "p.recommender";
            case "메모"   -> "p.memo_customer";
            default       -> "u.name";
        };

        String sql = "SELECT u.id, u.username, u.name, a.server " +
                "FROM users u " +
                "LEFT JOIN user_profiles p ON p.user_id = u.id " +
                "LEFT JOIN user_account_status a ON a.user_id = u.id " +
                "WHERE " + column + " LIKE ? " +
                "AND u.role = 'USER' " +
                "AND u.account_type = 'REAL' " +
                "ORDER BY u.name";

        List<UserSearchRow> list = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new UserSearchRow(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("name"),
                        rs.getString("server")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public String getPartnerUsername(int userId) {

        String sql =
                """
                SELECT recommender
                FROM user_profiles
                WHERE user_id=?
                """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("recommender");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

//해지 조회용 유저어카운트스타투스테이블의
    public String getAccountStatus(int userId) {
        String sql = "SELECT account_status FROM user_account_status WHERE user_id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("account_status");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "정상"; // 값이 없으면 기본값 정상 취급
    }




}

