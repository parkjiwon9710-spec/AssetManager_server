package Market;

import db.DBUtil;

import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MarketSpecCache {

    private static final Map<String, MarketSpec> cache = new LinkedHashMap<>();

    public static void load() {
        cache.clear();
        String sql = "SELECT * FROM market_specs ORDER BY sort_order";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {

                // tradeStart, tradeEnd 파싱
                LocalTime tradeStart = null;
                LocalTime tradeEnd = null;
                Time ts = rs.getTime("trade_start");
                Time te = rs.getTime("trade_end");
                if (ts != null) tradeStart = ts.toLocalTime();
                if (te != null) tradeEnd = te.toLocalTime();

                LocalTime auctionStartTime = null;
                Time as = rs.getTime("auction_start_time");
                if (as != null) auctionStartTime = as.toLocalTime();


                // 🔥 추가: 2/3구간 읽기
                LocalTime tradeStart2 = null;
                LocalTime tradeEnd2 = null;
                LocalTime tradeStart3 = null;
                LocalTime tradeEnd3 = null;
                Time ts2 = rs.getTime("trade_start2");
                Time te2 = rs.getTime("trade_end2");
                Time ts3 = rs.getTime("trade_start3");
                Time te3 = rs.getTime("trade_end3");
                if (ts2 != null) tradeStart2 = ts2.toLocalTime();
                if (te2 != null) tradeEnd2 = te2.toLocalTime();
                if (ts3 != null) tradeStart3 = ts3.toLocalTime();
                if (te3 != null) tradeEnd3 = te3.toLocalTime();

                MarketSpec spec = new MarketSpec(
                        rs.getString("symbol"),
                        rs.getString("display_name"),
                        rs.getString("contract_code"),

                        rs.getString("expiry_date") == null
                                ? ""
                                : rs.getString("expiry_date").substring(0, 7),

                        rs.getDouble("price_start"),
                        rs.getDouble("price_end"),
                        rs.getDouble("initial_price"),

                        rs.getDouble("tick_size"),
                        rs.getDouble("tick_value"),

                        rs.getDouble("contract_multiplier"),
                        rs.getString("currency"),

                        rs.getDouble("fee_per_contract"),

                        rs.getLong("entry_margin"),
                        rs.getLong("maint_margin"),

                        rs.getLong("overnight_margin"),
                        rs.getBoolean("overnight_enabled"),

                        rs.getBoolean("is_active"),

                        tradeStart,
                        tradeEnd,
                        auctionStartTime,
                        tradeStart2,   // 🔥 추가
                        tradeEnd2,     // 🔥 추가
                        tradeStart3,   // 🔥 추가
                        tradeEnd3,
                        rs.getString("fee_type"),
                        rs.getString("market_type")

                );
                cache.put(rs.getString("symbol"), spec);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static MarketSpec get(String symbol) {
        MarketSpec spec = cache.get(symbol);
        if (spec == null) throw new IllegalArgumentException("지원하지 않는 종목: " + symbol);
        return spec;
    }



    public static MarketPhase getPhase(String symbol) {
        MarketSpec spec = cache.get(symbol);
        if (spec == null || !spec.isActive()) return MarketPhase.CLOSED;

        LocalTime now = LocalTime.now();

        // 🔥 세션 1, 2, 3 순회 (HSI는 1~3 전부 사용, 나머지는 1만 사용하고 2/3은 null)
        if (inSession(now, spec.getTradeStart(), spec.getTradeEnd())) return MarketPhase.REGULAR;
        if (inSession(now, spec.getTradeStart2(), spec.getTradeEnd2())) return MarketPhase.REGULAR;
        if (inSession(now, spec.getTradeStart3(), spec.getTradeEnd3())) return MarketPhase.REGULAR;

        // AUCTION (KOSPI200 전용)
        LocalTime auctionStart = spec.getAuctionStartTime();
        if (auctionStart != null && spec.getTradeStart() != null) {
            if (inSession(now, auctionStart, spec.getTradeStart())) return MarketPhase.AUCTION;
        }

        return MarketPhase.CLOSED;
    }

    private static boolean inSession(LocalTime now, LocalTime start, LocalTime end) {
        if (start == null || end == null) return false;
        return end.isBefore(start)
                ? (now.isAfter(start) || now.isBefore(end))   // 자정 넘김 케이스 (HSI 3구간, NASDAQ/GOLD도 해당)
                : (now.isAfter(start) && now.isBefore(end));
    }


    // 기존 isTrading()은 그대로 둬도 무방하지만, 일관성을 위해 이렇게 재정의 권장:
    public static boolean isTrading(String symbol) {
        return getPhase(symbol) != MarketPhase.CLOSED;
    }




    public static void refresh() {
        load();
    }

    public static List<MarketSpec> getAll() {
        return new ArrayList<>(cache.values());
    }
}