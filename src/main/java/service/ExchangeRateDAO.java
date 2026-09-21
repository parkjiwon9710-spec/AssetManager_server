package service;

import db.DBUtil;
import model.ExchangeRateDto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

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

    // ★★★ 아래부터 신규 추가 ★★★

    public List<ExchangeRateDto> findAll() {
        String sql = "SELECT currency, rate_to_krw, updated_at FROM exchange_rates";
        List<ExchangeRateDto> result = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(new ExchangeRateDto(
                        rs.getString("currency"),
                        rs.getDouble("rate_to_krw"),
                        rs.getTimestamp("updated_at").toString()
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean updateRate(String currency, double rate) {
        String sql = "UPDATE exchange_rates SET rate_to_krw = ?, updated_at = NOW() WHERE currency = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, rate);
            ps.setString(2, currency);
            int affected = ps.executeUpdate();
            System.out.println("[DEBUG] updateRate currency=" + currency + " rate=" + rate + " affected=" + affected);
            return true;

        } catch (Exception e) {
            System.out.println("[DEBUG] updateRate 예외 발생: currency=" + currency);
            e.printStackTrace();
            return false;
        }
    }
}