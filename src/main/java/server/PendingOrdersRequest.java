package server;

public class PendingOrdersRequest {

    private String type = "PENDING_ORDERS_REQUEST";
    private int userId;
    private String symbol;

    public PendingOrdersRequest(int userId, String symbol) {
        this.userId = userId;
        this.symbol = symbol;
    }

    public String getType() {
        return type;
    }

    public int getUserId() {
        return userId;
    }

    public String getSymbol() {
        return symbol;
    }
}
