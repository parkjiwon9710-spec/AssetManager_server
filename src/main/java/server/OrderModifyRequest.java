package server;

public class OrderModifyRequest {
    private String type = "ORDER_MODIFY_REQUEST";
    private int userId;
    private String symbol;
    private int orderId;
    private String orderType;
    private String side;
    private double newPrice;
    private int qty;

    public OrderModifyRequest(int userId, String symbol, int orderId, String orderType, String side, double newPrice, int qty) {
        this.userId = userId;
        this.symbol = symbol;
        this.orderId = orderId;
        this.orderType = orderType;
        this.side = side;
        this.newPrice = newPrice;
        this.qty = qty;
    }

    public String getType() { return type; }
    public int getUserId() { return userId; }
    public String getSymbol() { return symbol; }
    public int getOrderId() { return orderId; }
    public String getOrderType() { return orderType; }
    public String getSide() { return side; }
    public double getNewPrice() { return newPrice; }
    public int getQty() { return qty; }
}