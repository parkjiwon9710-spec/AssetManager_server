package service;

import db.DBUtil;
import model.CompanyAccount;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CompanyAccountDAO {

    public List<Object[]> getAll() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT id, bank, account_number, account_holder, alias FROM company_accounts ORDER BY id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{
                        rs.getInt("id"),
                        rs.getString("bank"),
                        rs.getString("account_number"),
                        rs.getString("account_holder"),
                        rs.getString("alias")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void insert(String bank, String accountNumber, String accountHolder, String alias) {
        String sql = "INSERT INTO company_accounts (bank, account_number, account_holder, alias) VALUES (?,?,?,?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bank);
            ps.setString(2, accountNumber);
            ps.setString(3, accountHolder);
            ps.setString(4, alias.isEmpty() ? null : alias);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(int id, String bank, String accountNumber, String accountHolder, String alias) {
        String sql = "UPDATE company_accounts SET bank=?, account_number=?, account_holder=?, alias=? WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bank);
            ps.setString(2, accountNumber);
            ps.setString(3, accountHolder);
            ps.setString(4, alias.isEmpty() ? null : alias);
            ps.setInt(5, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM company_accounts WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public CompanyAccount getAccountById(int id) {

        String sql = """
            SELECT *
            FROM company_accounts
            WHERE id = ?
            """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                return new CompanyAccount(
                        rs.getInt("id"),
                        rs.getString("bank"),
                        rs.getString("account_number"),
                        rs.getString("account_holder"),
                        rs.getString("alias")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }





}