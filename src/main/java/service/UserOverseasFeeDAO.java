package service;

import Market.MarketSpecCache;
import db.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserOverseasFeeDAO {

    public double getFee(int userId, String symbol) {

        String sql =
                "SELECT fee FROM user_overseas_fees WHERE user_id=? AND symbol=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, symbol);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getDouble("fee");   // 고객 개별
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }


        // 고객 설정 없으면 마켓 기본값
        return MarketSpecCache.get(symbol).getFeePerContract();
    }

    public void setFee(int userId, String symbol, double fee) {
        String sql = """
            INSERT INTO user_overseas_fees (user_id, symbol, fee)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE fee = ?
            """;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, symbol);
            ps.setDouble(3, fee);
            ps.setDouble(4, fee);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}