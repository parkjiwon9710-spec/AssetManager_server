package server;

public class ChartHistoryRequest {
    public String type = "CHART_HISTORY_REQUEST";
    public String symbol;
    public String timeFrame; // TimeFrame enum name (예: "MIN1")

    public ChartHistoryRequest(String symbol, String timeFrame) {
        this.symbol = symbol;
        this.timeFrame = timeFrame;
    }
}