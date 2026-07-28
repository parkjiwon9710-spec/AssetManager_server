package server;

public class ChartSubscribeRequest {
    public String type = "CHART_SUBSCRIBE_REQUEST";
    public int userId;
    public String symbol;
    public String timeFrame;

    public ChartSubscribeRequest(int userId, String symbol, String timeFrame) {
        this.userId = userId;
        this.symbol = symbol;
        this.timeFrame = timeFrame;
    }
}
