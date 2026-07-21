package server;

public class CustomerDwHistoryRequest {
    public String type = "CUSTOMER_DW_HISTORY_REQUEST";
    public int userId;
    public long startMillis;
    public long endMillis;

    public CustomerDwHistoryRequest(int userId, long startMillis, long endMillis) {
        this.userId = userId;
        this.startMillis = startMillis;
        this.endMillis = endMillis;
    }
}
