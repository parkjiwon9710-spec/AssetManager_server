package server;

import java.util.List;
import java.util.Map;

public class ChartHistoryResponse {
    public String type = "CHART_HISTORY_RESPONSE";
    public String symbol;
    public String timeFrame;
    public List<Map<String, Object>> candles;

    public ChartHistoryResponse(String symbol, String timeFrame, List<Map<String, Object>> candles) {
        this.symbol = symbol;
        this.timeFrame = timeFrame;
        this.candles = candles;
    }
}