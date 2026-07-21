package server;

public class OrderPendingRequest {
    private String type = "ORDER_PENDING_REQUEST";
    private int userId;
    private String symbol;
    private String side;
    private String orderType;   // "LIMIT" or "MIT"
    private double price;
    private double triggerPrice;
    private int qty;

    public int getUserId() { return userId; }
    public String getSymbol() { return symbol; }
    public String getSide() { return side; }
    public String getOrderType() { return orderType; }
    public double getPrice() { return price; }
    public double getTriggerPrice() { return triggerPrice; }
    public int getQty() { return qty; }
}