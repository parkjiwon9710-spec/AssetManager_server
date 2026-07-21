package service;

import db.DBUtil;
import model.SystemQtyLimit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SystemQtyLimitDAO {

    public SystemQtyLimit getSettings() {

        String sql =
                "SELECT * FROM system_qty_limits WHERE id = 1";

        try (
                Connection conn =
                        DBUtil.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            ResultSet rs =
                    stmt.executeQuery();

            if (rs.next()) {

                SystemQtyLimit setting =
                        new SystemQtyLimit();

                setting.setMaxFuturesQty(
                        rs.getInt("max_futures_qty")
                );

                setting.setMaxOptionsBuyQty(
                        rs.getInt("max_options_buy_qty")
                );

                setting.setMaxOptionsSellQty(
                        rs.getInt("max_options_sell_qty")
                );

                setting.setMaxOverseasQty(
                        rs.getInt("max_overseas_qty")
                );

                return setting;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void updateSettings(
            int futuresQty,
            int optionBuyQty,
            int optionSellQty,
            int overseasQty
    ) {

        String sql =
                "UPDATE system_qty_limits " +
                        "SET max_futures_qty=?, " +
                        "max_options_buy_qty=?, " +
                        "max_options_sell_qty=?, " +
                        "max_overseas_qty=? " +
                        "WHERE id=1";

        try (
                Connection conn =
                        DBUtil.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, futuresQty);
            stmt.setInt(2, optionBuyQty);
            stmt.setInt(3, optionSellQty);
            stmt.setInt(4, overseasQty);

            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}