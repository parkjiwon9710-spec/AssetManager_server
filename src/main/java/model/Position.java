package model;

public class Position {

    private int id;
    private int userId;
    private int orderId;   // 🔥 이거 추가
    private String symbol;
    private int qty;
    private double avgPrice;
    private String direction; // 매수 / 매도
    private double realizedPnl; // 🔥 추가


    // 🔥 TP / SL
    private boolean tpEnabled;
    private boolean slEnabled;
    private double tpPrice;
    private double slPrice;
    private int tpTicks;      // 🔥 추가
    private int slTicks;

    // getter / setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getOrderId() {
        return orderId;
    }
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }

    public double getAvgPrice() { return avgPrice; }
    public void setAvgPrice(double avgPrice) { this.avgPrice = avgPrice; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public double getRealizedPnl() { return realizedPnl; }
    public void setRealizedPnl(double realizedPnl) {
        this.realizedPnl = realizedPnl;
    }

    /* =========================
       🔥 TP / SL getter / setter
    ========================= */

    public boolean isTpEnabled() {
        return tpEnabled;
    }

    public void setTpEnabled(boolean tpEnabled) {
        this.tpEnabled = tpEnabled;
    }

    public boolean isSlEnabled() {
        return slEnabled;
    }

    public void setSlEnabled(boolean slEnabled) {
        this.slEnabled = slEnabled;
    }

    public double getTpPrice() {
        return tpPrice;
    }

    public void setTpPrice(double tpPrice) {
        this.tpPrice = tpPrice;
    }

    public double getSlPrice() {
        return slPrice;
    }

    public void setSlPrice(double slPrice) {
        this.slPrice = slPrice;
    }

    public int getTpTicks() {
        return tpTicks;
    }

    public void setTpTicks(int tpTicks) {
        this.tpTicks = tpTicks;
    }

    public int getSlTicks() {
        return slTicks;
    }

    public void setSlTicks(int slTicks) {
        this.slTicks = slTicks;
    }

    /* =========================
       편의 메서드 (강력 추천)
    ========================= */

    public boolean isLong() {
        return "LONG".equals(direction);
    }

    public boolean isShort() {
        return "SHORT".equals(direction);
    }
}


