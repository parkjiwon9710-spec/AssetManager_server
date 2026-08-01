package model;

/**
 * 관리자 "고객 포지션" 패널의 미체결 주문 표시용 행.
 *
 * 🔧 추가: category(국내선물/해외선물/옵션), displayName(예: "나스닥") 필드
 */
public class AdminPendingOrderRow {
    private String name;
    private String username;
    private int userId;
    private String symbol;
    private String displayName;
    private String category;
    private String side;       // "BUY" / "SELL"
    private String orderType;  // "LIMIT" / "MIT" / "STOP" 등
    private double qty;
    private double orderPrice;
    private double currentPrice;
    private int orderId;

    public AdminPendingOrderRow(String name, String username, int userId, String symbol, String displayName,
                                String category, String side, String orderType, double qty,
                                double orderPrice, double currentPrice, int orderId) {
        this.name = name;
        this.username = username;
        this.userId = userId;
        this.symbol = symbol;
        this.displayName = displayName;
        this.category = category;
        this.side = side;
        this.orderType = orderType;
        this.qty = qty;
        this.orderPrice = orderPrice;
        this.currentPrice = currentPrice;
        this.orderId = orderId;
    }

    public String getName() { return name; }
    public String getUsername() { return username; }
    public int getUserId() { return userId; }
    public String getSymbol() { return symbol; }
    public String getDisplayName() { return displayName; }
    public String getCategory() { return category; }
    public String getSide() { return side; }
    public String getOrderType() { return orderType; }
    public double getQty() { return qty; }
    public double getOrderPrice() { return orderPrice; }
    public double getCurrentPrice() { return currentPrice; }
    public int getOrderId() { return orderId; }
}