package server;

public class OrderBulkCancelRequest {
    private String type = "ORDER_BULK_CANCEL_REQUEST";
    private int userId;
    private String mode;        // "BY_TYPE_SIDE", "BY_SYMBOL", "ALL"
    private String symbol;
    private String orderType;   // LIMIT, MIT (mode가 BY_TYPE_SIDE일 때만 사용)
    private String side;        // BUY, SELL (mode가 BY_TYPE_SIDE일 때만 사용)

    public int getUserId() { return userId; }
    public String getMode() { return mode; }
    public String getSymbol() { return symbol; }
    public String getOrderType() { return orderType; }
    public String getSide() { return side; }
}