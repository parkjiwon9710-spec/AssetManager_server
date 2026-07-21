package service;

import db.DBUtil;
import model.Position;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PositionDAO {

    public Position findByUserAndSymbol(int userId, String symbol) {

        String sql =
                "SELECT * FROM positions WHERE user_id=? AND symbol=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, symbol);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Position p = new Position();
                p.setOrderId(rs.getInt("order_id"));
                p.setId(rs.getInt("id"));
                p.setUserId(rs.getInt("user_id"));
                p.setSymbol(rs.getString("symbol"));
                p.setDirection(rs.getString("direction"));
                p.setQty(rs.getInt("qty"));
                p.setAvgPrice(rs.getDouble("avg_price"));
                p.setRealizedPnl(rs.getDouble("realized_pnl"));

                p.setTpEnabled(rs.getBoolean("tp_enabled"));
                p.setTpPrice(rs.getDouble("tp_price"));
                p.setSlEnabled(rs.getBoolean("sl_enabled"));
                p.setSlPrice(rs.getDouble("sl_price"));

                return p;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void insert(Position p) {

        String sql =
                "INSERT INTO positions " +
                        "(order_id, user_id, symbol, direction, qty, avg_price, realized_pnl, " +
                        " tp_enabled, tp_price, sl_enabled, sl_price, tp_ticks, sl_ticks) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, p.getOrderId());
            ps.setInt(2, p.getUserId());
            ps.setString(3, p.getSymbol());
            ps.setString(4, p.getDirection());
            ps.setInt(5, p.getQty());
            ps.setDouble(6, p.getAvgPrice());
            ps.setDouble(7, p.getRealizedPnl());

            ps.setBoolean(8, p.isTpEnabled());
            ps.setDouble(9, p.getTpPrice());
            ps.setBoolean(10, p.isSlEnabled());
            ps.setDouble(11, p.getSlPrice());

            ps.setInt(12, p.getTpTicks());   // 🔥 추가
            ps.setInt(13, p.getSlTicks());   // 🔥 추가

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(Position p) {

        String sql =
                "UPDATE positions SET qty=?,\n" +
                        "  avg_price=?,\n" +
                        "  realized_pnl=?,\n" +
                        "  tp_enabled=?,\n" +
                        "  tp_price=?,\n" +
                        "  sl_enabled=?,\n" +
                        "  sl_price=?,\n" +
                        "  tp_ticks=?,\n" +
                        "  sl_ticks=? WHERE id=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, p.getQty());
            ps.setDouble(2, p.getAvgPrice());
            ps.setDouble(3, p.getRealizedPnl());
            ps.setBoolean(4, p.isTpEnabled());
            ps.setDouble(5, p.getTpPrice());
            ps.setBoolean(6, p.isSlEnabled());
            ps.setDouble(7, p.getSlPrice());
            ps.setInt(8, p.getTpTicks());
            ps.setInt(9, p.getSlTicks());
            ps.setInt(10, p.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int positionId) {

        String sql = "DELETE FROM positions WHERE id=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, positionId);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Position> findAll() {

        List<Position> list = new ArrayList<>();

        String sql = "SELECT * FROM positions";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Position p = new Position();
                p.setOrderId(rs.getInt("order_id"));
                p.setId(rs.getInt("id"));
                p.setUserId(rs.getInt("user_id"));
                p.setSymbol(rs.getString("symbol"));
                p.setDirection(rs.getString("direction"));
                p.setQty(rs.getInt("qty"));
                p.setAvgPrice(rs.getDouble("avg_price"));
                p.setRealizedPnl(rs.getDouble("realized_pnl"));
                p.setTpEnabled(rs.getBoolean("tp_enabled"));
                p.setTpPrice(rs.getDouble("tp_price"));
                p.setSlEnabled(rs.getBoolean("sl_enabled"));
                p.setSlPrice(rs.getDouble("sl_price"));
                list.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public Position findById(int id) {
        String sql = "SELECT * FROM positions WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Position p = new Position();
                p.setOrderId(rs.getInt("order_id"));
                p.setId(rs.getInt("id"));
                p.setUserId(rs.getInt("user_id"));
                p.setSymbol(rs.getString("symbol"));
                p.setQty(rs.getInt("qty"));
                p.setAvgPrice(rs.getDouble("avg_price"));
                p.setDirection(rs.getString("direction"));
                p.setRealizedPnl(rs.getDouble("realized_pnl"));
                return p;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Position> findAllByUser(int userId) {

        List<Position> list = new ArrayList<>();

        String sql = "SELECT * FROM positions WHERE user_id=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Position p = new Position();
                p.setOrderId(rs.getInt("order_id"));
                p.setId(rs.getInt("id"));
                p.setUserId(rs.getInt("user_id"));
                p.setSymbol(rs.getString("symbol"));
                p.setDirection(rs.getString("direction"));
                p.setQty(rs.getInt("qty"));
                p.setAvgPrice(rs.getDouble("avg_price"));
                p.setRealizedPnl(rs.getDouble("realized_pnl"));
                // 🔥 TP/SL 관련 컬럼 추가
                p.setTpEnabled(rs.getBoolean("tp_enabled"));
                p.setTpPrice(rs.getDouble("tp_price"));
                p.setSlEnabled(rs.getBoolean("sl_enabled"));
                p.setSlPrice(rs.getDouble("sl_price"));
                p.setTpTicks(rs.getInt("tp_ticks"));
                p.setSlTicks(rs.getInt("sl_ticks"));
                list.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Integer> findUserIdsBySymbol(String symbol) {

        List<Integer> userIds = new ArrayList<>();

        String sql =
                "SELECT DISTINCT user_id FROM positions WHERE symbol = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, symbol);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                userIds.add(rs.getInt("user_id"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return userIds;
    }

    // 어떠한 심볼에서 손익절 저굥된 포지션만 들고오기
    public List<Position> findActiveTpSlPositionsBySymbol(String symbol) {

        List<Position> list = new ArrayList<>();

        String sql = "SELECT * FROM positions WHERE symbol=? AND (tp_enabled=1 OR sl_enabled=1)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, symbol);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Position p = new Position();
                p.setId(rs.getInt("id"));
                p.setUserId(rs.getInt("user_id"));
                p.setSymbol(rs.getString("symbol"));
                p.setDirection(rs.getString("direction"));
                p.setQty(rs.getInt("qty"));
                p.setAvgPrice(rs.getDouble("avg_price"));
                p.setTpEnabled(rs.getBoolean("tp_enabled"));
                p.setTpPrice(rs.getDouble("tp_price"));
                p.setSlEnabled(rs.getBoolean("sl_enabled"));
                p.setSlPrice(rs.getDouble("sl_price"));
                p.setTpTicks(rs.getInt("tp_ticks"));   // 🔥 추가
                p.setSlTicks(rs.getInt("sl_ticks"));
                list.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
}