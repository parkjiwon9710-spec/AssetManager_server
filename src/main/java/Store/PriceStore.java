package Store;

import Market.PriceListener;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class PriceStore {

    private static final Map<String, Double> lastPrice = new ConcurrentHashMap<>();
    private static final Map<String, Double> bestBid   = new ConcurrentHashMap<>();
    private static final Map<String, Double> bestAsk   = new ConcurrentHashMap<>();
    private static final Map<String, Double> prevCloseMap = new ConcurrentHashMap<>();
    private static final Map<String, Double> openMap      = new ConcurrentHashMap<>();
    private static final Map<String, Double> highMap      = new ConcurrentHashMap<>();
    private static final Map<String, Double> lowMap       = new ConcurrentHashMap<>();



    private static final List<PriceListener> listeners = new CopyOnWriteArrayList<>();

    public static void addListener(PriceListener listener) {
        listeners.add(listener);
    }

    public static void removeListener(PriceListener listener) {
        listeners.remove(listener);
    }

    private static void notifyListeners(String symbol) {
        for (PriceListener listener : listeners) {
            listener.onPriceChanged(symbol);
        }
    }



    // 장 시작 시 1회
    public static void initDailyPrice(String symbol, double prevClose) {
        prevCloseMap.put(symbol, round(symbol, prevClose));

        openMap.remove(symbol);
        highMap.remove(symbol);
        lowMap.remove(symbol);
    }


//    public static void updateOHLC(String symbol, double open, double high, double low, double prevClose) {
//        openMap.put(symbol, open);
//        highMap.put(symbol, high);
//        lowMap.put(symbol, low);
//        prevCloseMap.put(symbol, prevClose);
//        notifyListeners(symbol);  // ✅ 필요 시 추가
//    }

    // 체결 or 틱
    public static void updateLast(String symbol, double price) {
        lastPrice.put(symbol, round(symbol, price));


        // 시가 없으면 오늘 시작
        openMap.putIfAbsent(symbol, round(symbol, price));

        // 고가 / 저가 자동 갱신
        highMap.merge(symbol, round(symbol, price), Math::max);
        lowMap.merge(symbol, round(symbol, price), Math::min);


        notifyListeners(symbol);  // ✅ 여기에 추가
    }

    // 호가 업데이트
    public static void updateBidAsk(String symbol, double bid, double ask) {

        if (!lastPrice.containsKey(symbol)) {
            System.out.println("[WARN] PriceStore.updateBidAsk: Unknown symbol " + symbol);
        }

        bestBid.put(symbol,  round(symbol, bid));
        bestAsk.put(symbol, round(symbol, ask));
    }



    public static double getLast(String symbol) {
        return lastPrice.getOrDefault(symbol, 0.0);
    }

    public static double getBestBid(String symbol) {
        return bestBid.getOrDefault(symbol, Double.NaN); // 존재하지 않으면 NaN 반환
    }

    public static double getBestAsk(String symbol) {
        return bestAsk.getOrDefault(symbol, Double.NaN); // 존재하지 않으면 NaN 반환
    }

    // ✅ 존재 여부 확인
    public static boolean hasSymbol(String symbol) {
        return bestBid.containsKey(symbol) && bestAsk.containsKey(symbol);
    }


    private static final Map<String, Double> tickSizeMap =
            new ConcurrentHashMap<>();

    public static void setTickSize(String symbol, double tickSize) {
        tickSizeMap.put(symbol, tickSize);
    }

    public static double getTickSize(String symbol) {
        return tickSizeMap.getOrDefault(symbol, 1.0);
    }


    public static double getPrevClose(String symbol) { return prevCloseMap.getOrDefault(symbol, 0.0); }
    public static double getOpen(String symbol)      { return openMap.getOrDefault(symbol, 0.0); }
    public static double getHigh(String symbol)      { return highMap.getOrDefault(symbol, 0.0); }
    public static double getLow(String symbol)       { return lowMap.getOrDefault(symbol, 0.0); }


    private static double round(String symbol, double price) {
        double tick = tickSizeMap.getOrDefault(symbol, 1.0);
        java.math.BigDecimal bdPrice = new java.math.BigDecimal(String.valueOf(price));
        java.math.BigDecimal bdTick = new java.math.BigDecimal(String.valueOf(tick));
        return bdPrice.divide(bdTick, 0, java.math.RoundingMode.HALF_UP)
                .multiply(bdTick)
                .doubleValue();
    }


}
