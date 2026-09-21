package server;

public class AuditLogRequest {
    private String type = "AUDIT_LOG_REQUEST";   // 🔥 추가
    private String eventType;
    private int userId;
    private String userName;
    private String operator;
    private String symbol;
    private double orderPrice;
    private double filledPrice;
    private String side;
    private int qty;

    public AuditLogRequest(String eventType, int userId, String userName, String operator,
                           String symbol, double orderPrice, double filledPrice, String side, int qty) {
        this.eventType = eventType;
        this.userId = userId;
        this.userName = userName;
        this.operator = operator;
        this.symbol = symbol;
        this.orderPrice = orderPrice;
        this.filledPrice = filledPrice;
        this.side = side;
        this.qty = qty;
    }

    public String getType() { return type; }
    public String getEventType() { return eventType; }
    public int getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getOperator() { return operator; }
    public String getSymbol() { return symbol; }
    public double getOrderPrice() { return orderPrice; }
    public double getFilledPrice() { return filledPrice; }
    public String getSide() { return side; }
    public int getQty() { return qty; }
}
