package model;

import java.time.LocalDateTime;

public class OrderAuditLog {

    public LocalDateTime time;

    public int userId;
    public String userName;
    public String eventType;
    public String operator;      // admin / system / bot
    public String exchange;      // 🔥 홍콩거래소 / 한국거래소 / CME
    public String server;        // 🔥 신호 / 체결 / 거부

    public String orderId;
    public String symbol;

    public double orderPrice;    // 🔥 price → orderPrice
    public double filledPrice;   // 🔥 신규

    public String side;
    public int qty;

    public double fee;
    public double pnl;

    public double leverage;
    public double margin;

    public int filledQty;
    public int remainingQty;

    public String positionSnapshot;
    public String openOrderSnapshot;
}