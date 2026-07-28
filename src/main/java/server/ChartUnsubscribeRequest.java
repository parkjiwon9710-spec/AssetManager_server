package server;

public class ChartUnsubscribeRequest {
    public String type = "CHART_UNSUBSCRIBE_REQUEST";
    public int userId;

    public ChartUnsubscribeRequest(int userId) {
        this.userId = userId;
    }
}