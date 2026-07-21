package model;

import java.time.LocalDateTime;

public class Trade {

    private final String symbol;
    private final double price;
    private final int qty;
    private final LocalDateTime time;

    public Trade(String symbol, double price, int qty, LocalDateTime time) {
        this.symbol = symbol;
        this.price = price;
        this.qty = qty;
        this.time = time;
    }

    public String getSymbol() { return symbol; }
    public double getPrice() { return price; }
    public int getQty() { return qty; }
    public LocalDateTime getTime() { return time; }
}
