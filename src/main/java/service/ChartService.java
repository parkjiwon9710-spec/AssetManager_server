package service; // 실제 서버 프로젝트의 패키지에 맞춰주세요

import Market.PriceListener;
import Store.PriceStore;
import com.google.gson.Gson;
import model.Candle;
import server.ChartCandleUpdate;
import model.TimeFrame;
import server.SessionManager; // 실제 SessionManager 패키지에 맞춰주세요

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChartService implements PriceListener {

    private final Map<String, Map<TimeFrame, List<Candle>>> candleMap =
            new ConcurrentHashMap<>();

    private final Map<String, Map<TimeFrame, Candle>> currentCandleMap =
            new ConcurrentHashMap<>();

    private final Gson gson = new Gson();

    // 🔥 종목별 락 - 종목마다 독립적으로 처리되도록 분리
    private final Map<String, Object> symbolLocks = new ConcurrentHashMap<>();

    public ChartService() {
        PriceStore.addListener(this);
    }

    @Override
    public void onPriceChanged(String symbol) {

        Object lock = symbolLocks.computeIfAbsent(symbol, k -> new Object());

        synchronized (lock) {

            double price = PriceStore.getLast(symbol);
            LocalDateTime now = LocalDateTime.now();

            for (TimeFrame tf : TimeFrame.values()) {

                LocalDateTime candleTime = CandleAggregator.truncate(now, tf);

                currentCandleMap.computeIfAbsent(symbol, k -> new ConcurrentHashMap<>());
                Map<TimeFrame, Candle> map = currentCandleMap.get(symbol);
                Candle current = map.get(tf);

                if (current == null) {
                    Candle candle = new Candle(candleTime, price, tf);
                    map.put(tf, candle);

                    candleMap.computeIfAbsent(symbol, k -> new ConcurrentHashMap<>())
                            .computeIfAbsent(tf, k -> new ArrayList<>())
                            .add(candle);

                    pushCandleUpdate(symbol, tf, candle);
                    continue;
                }

                if (!current.getCandleTime().equals(candleTime)) {
                    Candle candle = new Candle(candleTime, price, tf);
                    map.put(tf, candle);

                    candleMap.get(symbol).get(tf).add(candle);
                    trim(symbol, tf);

                    pushCandleUpdate(symbol, tf, candle);

                } else {
                    current.update(price);
                    pushCandleUpdate(symbol, tf, current);
                }
            }
        }
    }

    private void trim(String symbol, TimeFrame tf) {
        List<Candle> list = candleMap.get(symbol).get(tf);
        if (list == null) return;

        while (list.size() > 5000) {
            list.remove(0);
        }
    }

    private void pushCandleUpdate(String symbol, TimeFrame tf, Candle candle) {
        Map<String, Object> candleData = new HashMap<>();
        candleData.put("time", candle.getCandleTime().toEpochSecond(ZoneOffset.UTC));
        candleData.put("open", candle.getOpen());
        candleData.put("high", candle.getHigh());
        candleData.put("low", candle.getLow());
        candleData.put("close", candle.getClose());
        candleData.put("volume", candle.getVolume());

        ChartCandleUpdate update = new ChartCandleUpdate(symbol, tf.name(), candleData);

        for (Integer userId : SessionManager.getChartSubscribers(symbol, tf.name())) {
            System.out.println("[캔들푸시] " + symbol + " " + tf + " close=" + candle.getClose()
                    + " 구독자수=" + SessionManager.getChartSubscribers(symbol, tf.name()).size());
            SessionManager.sendToCustomer(userId, update);
        }
    }

    public List<Candle> getCandles(String symbol, TimeFrame tf) {
        return candleMap
                .getOrDefault(symbol, Collections.emptyMap())
                .getOrDefault(tf, Collections.emptyList());
    }

    public List<Map<String, Object>> getChartData(String symbol, TimeFrame tf) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (Candle c : getCandles(symbol, tf)) {
            Map<String, Object> map = new HashMap<>();
            map.put("time", c.getCandleTime().toEpochSecond(ZoneOffset.UTC));
            map.put("open", c.getOpen());
            map.put("high", c.getHigh());
            map.put("low", c.getLow());
            map.put("close", c.getClose());
            map.put("volume", c.getVolume());
            result.add(map);
        }

        return result;
    }
}