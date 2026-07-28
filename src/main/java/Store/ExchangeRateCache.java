package Store;

import db.DBUtil;
import java.sql.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class ExchangeRateCache {

    private static final Map<String, Double> cache = new ConcurrentHashMap<>();

    public static void load() {
        String sql = "SELECT currency, rate_to_krw FROM exchange_rates";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            cache.clear();
            while (rs.next()) {
                cache.put(rs.getString("currency"), rs.getDouble("rate_to_krw"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static double getRate(String currency) {
        return cache.getOrDefault(currency, 1.0);
    }

    public static Map<String, Double> getAll() {
        return new java.util.HashMap<>(cache);
    }
}