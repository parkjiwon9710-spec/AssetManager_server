package server;

public class PositionRequest {
    private String type = "POSITION_REQUEST";
    private String requestId;
    private int userId;
    private String symbol;

    public PositionRequest(int userId, String symbol) {
        this.requestId = java.util.UUID.randomUUID().toString();
        this.userId = userId;
        this.symbol = symbol;
    }
    public String getRequestId() { return requestId; }
    public int getUserId() { return userId; }
    public String getSymbol() { return symbol; }
}

