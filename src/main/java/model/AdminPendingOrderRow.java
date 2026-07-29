package model;

/**
 * 관리자 "고객 포지션" 패널의 미체결 주문 표시용 행.
 * model.PendingOrderRow(고객 본인 화면용)와 별도로 이름/아이디/userId/orderId를 포함.
 */
public class AdminPendingOrderRow {
    private String name;
    private String username;
    private int userId;
    private String symbol;
    private String side;       // "BUY" / "SELL"
    private double qty;
    private double orderPrice;
    private double currentPrice;
    private int orderId;

    public AdminPendingOrderRow(String name, String username, int userId, String symbol, String side,
                                double qty, double orderPrice, double currentPrice, int orderId) {
        this.name = name;
        this.username = username;
        this.userId = userId;
        this.symbol = symbol;
        this.side = side;
        this.qty = qty;
        this.orderPrice = orderPrice;
        this.currentPrice = currentPrice;
        this.orderId = orderId;
    }

    public String getName() { return name; }
    public String getUsername() { return username; }
    public int getUserId() { return userId; }
    public String getSymbol() { return symbol; }
    public String getSide() { return side; }
    public double getQty() { return qty; }
    public double getOrderPrice() { return orderPrice; }
    public double getCurrentPrice() { return currentPrice; }
    public int getOrderId() { return orderId; }
}