package model;

public class Order {

    private int id;
    private int userId;
    private String symbol;
    private String side;        // BUY / SELL
    private double orderPrice;
    private Double filledPrice;     // 신규, PENDING이면 null
    private int qty;
    private String status;      // PENDING / FILLED
    private String orderType;   // MARKET / LIMIT / STOP / MIT
    private Double triggerPrice;

    // getter / setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getSide() { return side; }
    public void setSide(String side) { this.side = side; }




    public double getOrderPrice() { return orderPrice; }
    public void setOrderPrice(double orderPrice) { this.orderPrice = orderPrice; }

    public Double getFilledPrice() { return filledPrice; }
    public void setFilledPrice(Double filledPrice) { this.filledPrice = filledPrice; }


    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }

    public Double getTriggerPrice() { return triggerPrice; }
    public void setTriggerPrice(Double triggerPrice) { this.triggerPrice = triggerPrice; }
}
