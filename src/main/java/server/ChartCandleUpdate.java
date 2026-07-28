package server;

import java.util.Map;

public class ChartCandleUpdate {
    public String type = "CHART_CANDLE_UPDATE";
    public String symbol;
    public String timeFrame;
    public Map<String, Object> candle; // 진행 중인 마지막 봉 1개

    public ChartCandleUpdate(String symbol, String timeFrame, Map<String, Object> candle) {
        this.symbol = symbol;
        this.timeFrame = timeFrame;
        this.candle = candle;
    }
}