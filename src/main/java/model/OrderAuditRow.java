package model;

public class OrderAuditRow {
    public String time;   // 🔥 LocalDateTime → String
    public String eventType;
    public String userName;
    public String operator;
    public String exchange;
    public String server;
    public String orderId;
    public String symbol;
    public double orderPrice;
    public double filledPrice;
    public String side;
    public int qty;
    public double fee;
    public double pnl;
    public double leverage;
    public double margin;
    public String positionSnapshot;
    public String openOrderSnapshot;
}