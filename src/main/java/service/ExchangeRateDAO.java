package service;

import db.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ExchangeRateDAO {

    public double getRate(String currency) {

        String sql =
                "SELECT rate_to_krw FROM exchange_rates WHERE currency=?";

        try(Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, currency);

            ResultSet rs = ps.executeQuery();

            if(rs.next()) {
                return rs.getDouble("rate_to_krw");
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return 1.0;
    }

}
