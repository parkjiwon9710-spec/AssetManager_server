package model;

public class PositionRow {
    private int id;
    private String symbol;
    private double avgPrice;
    private double currentPrice;
    private String displaySide;   // "매수"/"매도"
    private int qty;
    private String pnl;           // 이미 포맷된 문자열

    public PositionRow() {}

    public PositionRow(int id, String symbol, double avgPrice, double currentPrice,
                       String displaySide, int qty, String pnl) {
        this.id = id;
        this.symbol = symbol;
        this.avgPrice = avgPrice;
        this.currentPrice = currentPrice;
        this.displaySide = displaySide;
        this.qty = qty;
        this.pnl = pnl;
    }

    public int getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getAvgPrice() {
        return avgPrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public String getDisplaySide() {
        return displaySide;
    }

    public int getQty() {
        return qty;
    }

    public String getPnl() {
        return pnl;
    }
}