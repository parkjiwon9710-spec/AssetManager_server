package server;



public class DailyProfitRequest {

    private String type = "DAILY_PROFIT_REQUEST";

    private int userId;
    private long startMillis;
    private long endMillis;

    public DailyProfitRequest(int userId, long startMillis, long endMillis) {
        this.userId = userId;
        this.startMillis = startMillis;
        this.endMillis = endMillis;
    }

    // Gson 역직렬화용
    public DailyProfitRequest() {
    }

    public String getType() {
        return type;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public long getStartMillis() {
        return startMillis;
    }

    public void setStartMillis(long startMillis) {
        this.startMillis = startMillis;
    }

    public long getEndMillis() {
        return endMillis;
    }

    public void setEndMillis(long endMillis) {
        this.endMillis = endMillis;
    }
}
