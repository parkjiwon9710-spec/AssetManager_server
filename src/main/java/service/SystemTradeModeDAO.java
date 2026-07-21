package service;

import db.DBUtil;
import model.SystemTradeMode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


public class SystemTradeModeDAO {


    public SystemTradeMode getSettings(){


        String sql =
                "SELECT * FROM system_trade_modes WHERE id=1";


        try(Connection conn = DBUtil.getConnection();
            PreparedStatement ps =
                    conn.prepareStatement(sql)){


            ResultSet rs = ps.executeQuery();


            if(rs.next()){


                SystemTradeMode mode =
                        new SystemTradeMode();


                mode.setOverseasEntryMarginMode(
                        rs.getString("overseas_entry_margin_mode")
                );


                mode.setOverseasMaintMarginMode(
                        rs.getString("overseas_maint_margin_mode")
                );


                mode.setOverseasOvernightMarginMode(
                        rs.getString("overseas_overnight_margin_mode")
                );


                mode.setOverseasPermissionMode(
                        rs.getString("overseas_permission_mode")
                );


                return mode;

            }


        }catch(Exception e){

            e.printStackTrace();

        }


        return null;
    }



    public void save(SystemTradeMode mode){


        String sql =
                """
                UPDATE system_trade_modes SET
        
                overseas_entry_margin_mode=?,
        
                overseas_maint_margin_mode=?,
        
                overseas_overnight_margin_mode=?,
        
                overseas_permission_mode=?
        
                WHERE id=1
                """;



        try(Connection conn = DBUtil.getConnection();
            PreparedStatement ps =
                    conn.prepareStatement(sql)){


            ps.setString(1,
                    mode.getOverseasEntryMarginMode());


            ps.setString(2,
                    mode.getOverseasMaintMarginMode());


            ps.setString(3,
                    mode.getOverseasOvernightMarginMode());


            ps.setString(4,
                    mode.getOverseasPermissionMode());


            ps.executeUpdate();


        }catch(Exception e){

            e.printStackTrace();

        }

    }

}