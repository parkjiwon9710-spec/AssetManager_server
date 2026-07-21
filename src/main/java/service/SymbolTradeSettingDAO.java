package service;

import db.DBUtil;
import model.SymbolTradeSetting;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SymbolTradeSettingDAO {

    public SymbolTradeSetting getBySymbol(String symbol){

        String sql =
                "SELECT * FROM symbol_trade_settings WHERE symbol=?";

        try(Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setString(1, symbol);

            ResultSet rs = ps.executeQuery();

            if(rs.next()){

                SymbolTradeSetting setting =
                        new SymbolTradeSetting();

                setting.setSymbol(
                        rs.getString("symbol")
                );

                setting.setEntryMargin(
                        rs.getLong("entry_margin")
                );

                setting.setMaintMargin(
                        rs.getLong("maint_margin")
                );

                setting.setOvernightMargin(
                        rs.getLong("overnight_margin")
                );

                setting.setOvernightEnabled(
                        rs.getBoolean("overnight_enabled")
                );

                return setting;
            }

        }catch(Exception e){

            e.printStackTrace();

        }

        return null;
    }


    public void saveEntryMargin(
            String symbol,
            long value
    ){

        String sql =
                """
                INSERT INTO symbol_trade_settings
                (symbol, entry_margin)
                VALUES(?,?)
                ON DUPLICATE KEY UPDATE
                entry_margin=?
                """;

        try(Connection conn = DBUtil.getConnection();
            PreparedStatement ps =
                    conn.prepareStatement(sql)){

            ps.setString(1, symbol);
            ps.setLong(2, value);
            ps.setLong(3, value);

            ps.executeUpdate();

        }catch(Exception e){

            e.printStackTrace();

        }




    }





    public void saveMaintMargin(
            String symbol,
            long value
    ){

        String sql =
                """
                INSERT INTO symbol_trade_settings
                (symbol, maint_margin)
                VALUES(?,?)
                ON DUPLICATE KEY UPDATE
                maint_margin=?
                """;

        try(Connection conn = DBUtil.getConnection();
            PreparedStatement ps =
                    conn.prepareStatement(sql)){

            ps.setString(1, symbol);
            ps.setLong(2, value);
            ps.setLong(3, value);

            ps.executeUpdate();

        }catch(Exception e){

            e.printStackTrace();

        }

    }



    public void saveOvernightMargin(
            String symbol,
            long value
    ){

        String sql =
                """
                INSERT INTO symbol_trade_settings
                (symbol, overnight_margin)
                VALUES(?,?)
                ON DUPLICATE KEY UPDATE
                overnight_margin=?
                """;

        try(Connection conn = DBUtil.getConnection();
            PreparedStatement ps =
                    conn.prepareStatement(sql)){

            ps.setString(1, symbol);
            ps.setLong(2, value);
            ps.setLong(3, value);

            ps.executeUpdate();

        }catch(Exception e){

            e.printStackTrace();

        }

    }

    public void saveOvernightEnabled(
            String symbol,
            boolean enabled
    ){

        String sql =
                """
                INSERT INTO symbol_trade_settings
                (symbol, overnight_enabled)
                VALUES(?,?)
                ON DUPLICATE KEY UPDATE
                overnight_enabled=?
                """;

        try(Connection conn = DBUtil.getConnection();
            PreparedStatement ps =
                    conn.prepareStatement(sql)){

            ps.setString(1, symbol);
            ps.setBoolean(2, enabled);
            ps.setBoolean(3, enabled);

            ps.executeUpdate();

        }catch(Exception e){

            e.printStackTrace();

        }

    }


    public long getEntryMargin(String symbol){

        String sql =
                "SELECT entry_margin " +
                        "FROM symbol_trade_settings " +
                        "WHERE symbol=?";

        try(Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setString(1, symbol);

            ResultSet rs =
                    ps.executeQuery();

            if(rs.next()){

                return rs.getLong(
                        "entry_margin"
                );

            }

        }catch(Exception e){

            e.printStackTrace();

        }

        return 0;
    }

    public long getMaintMargin(String symbol){

        String sql =
                "SELECT maint_margin " +
                        "FROM symbol_trade_settings " +
                        "WHERE symbol=?";

        try(Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setString(1, symbol);

            ResultSet rs =
                    ps.executeQuery();

            if(rs.next()){

                return rs.getLong(
                        "maint_margin"
                );

            }

        }catch(Exception e){

            e.printStackTrace();

        }

        return 0;
    }


    public long getOvernightMargin(String symbol){

        String sql =
                "SELECT overnight_margin " +
                        "FROM symbol_trade_settings " +
                        "WHERE symbol=?";

        try(Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setString(1, symbol);

            ResultSet rs =
                    ps.executeQuery();

            if(rs.next()){

                return rs.getLong(
                        "overnight_margin"
                );

            }

        }catch(Exception e){

            e.printStackTrace();

        }

        return 0;
    }


    public boolean getOvernightEnabled(
            String symbol
    ){
        String sql =
                "SELECT overnight_enabled " +
                        "FROM symbol_trade_settings " +
                        "WHERE symbol=?";

        try(Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setString(1, symbol);

            ResultSet rs =
                    ps.executeQuery();

            if(rs.next()){

                return rs.getBoolean(
                        "overnight_enabled"
                );

            }

        }catch(Exception e){

            e.printStackTrace();

        }

        return true;
    }




}