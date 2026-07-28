package model;

public class PendingOrderRow {

    private int id;
    private String symbol;

    private String side;        // 원본 "BUY" / "SELL"
    private String orderType;   // 원본 "LIMIT" / "MIT" 등

    private double orderPrice;
    private double currentPrice;

    private String displaySideType;  // "매수 / 지정가" 등
    private int qty;

    public PendingOrderRow() {
    }

    public PendingOrderRow(int id,
                           String symbol,
                           String side,
                           String orderType,
                           double orderPrice,
                           double currentPrice,
                           String displaySideType,
                           int qty) {
        this.id = id;
        this.symbol = symbol;
        this.side = side;
        this.orderType = orderType;
        this.orderPrice = orderPrice;
        this.currentPrice = currentPrice;
        this.displaySideType = displaySideType;
        this.qty = qty;
    }

    public int getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getSide() {
        return side;
    }

    public String getOrderType() {
        return orderType;
    }

    public double getOrderPrice() {
        return orderPrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public String getDisplaySideType() {
        return displaySideType;
    }

    public int getQty() {
        return qty;
    }
}