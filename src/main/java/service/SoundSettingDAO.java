package service;

import db.DBUtil;
import model.SoundSetting;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SoundSettingDAO {

    public SoundSetting load(int userId) {

        String sql =
                "SELECT buy_executed, sell_executed, " +
                        "buy_reserved, sell_reserved, " +
                        "order_modified, order_cancelled " +
                        "FROM user_sound_setting " +
                        "WHERE user_id=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                SoundSetting s = new SoundSetting();
                s.setBuyExecuted(rs.getBoolean("buy_executed"));
                s.setSellExecuted(rs.getBoolean("sell_executed"));
                s.setBuyReserved(rs.getBoolean("buy_reserved"));
                s.setSellReserved(rs.getBoolean("sell_reserved"));
                s.setOrderModified(rs.getBoolean("order_modified"));
                s.setOrderCancelled(rs.getBoolean("order_cancelled"));
                return s;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return createDefault();
    }

    public boolean save(int userId, SoundSetting s) {

        String sql =
                "INSERT INTO user_sound_setting " +
                        "(user_id, buy_executed, sell_executed, buy_reserved, sell_reserved, order_modified, order_cancelled) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "buy_executed=VALUES(buy_executed), " +
                        "sell_executed=VALUES(sell_executed), " +
                        "buy_reserved=VALUES(buy_reserved), " +
                        "sell_reserved=VALUES(sell_reserved), " +
                        "order_modified=VALUES(order_modified), " +
                        "order_cancelled=VALUES(order_cancelled)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setBoolean(2, s.isBuyExecuted());
            ps.setBoolean(3, s.isSellExecuted());
            ps.setBoolean(4, s.isBuyReserved());
            ps.setBoolean(5, s.isSellReserved());
            ps.setBoolean(6, s.isOrderModified());
            ps.setBoolean(7, s.isOrderCancelled());

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    private SoundSetting createDefault() {
        SoundSetting s = new SoundSetting();
        s.setBuyExecuted(true);
        s.setSellExecuted(true);
        s.setBuyReserved(true);
        s.setSellReserved(true);
        s.setOrderModified(true);
        s.setOrderCancelled(true);
        return s;
    }
}