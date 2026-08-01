package model;

public class TradeUpdateMessage {
    private String type = "TRADE_UPDATE";
    private String symbol;
    private double price;
    private int qty;
    private String time;   // 🔥 LocalDateTime -> String

    public TradeUpdateMessage(String symbol, double price, int qty, String time) {
        this.symbol = symbol;
        this.price = price;
        this.qty = qty;
        this.time = time;
    }

    public String getSymbol() { return symbol; }
    public double getPrice() { return price; }
    public int getQty() { return qty; }
    public String getTime() { return time; }
}