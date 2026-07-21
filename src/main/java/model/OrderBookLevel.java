package model;

public class OrderBookLevel {

    private final double price;
    private final int qty;
    private final int count;

    public OrderBookLevel(double price, int qty, int count) {
        this.price = price;
        this.qty = qty;
        this.count = count;
    }

    public double getPrice() { return price; }
    public int getQty() { return qty; }
    public int getCount() { return count; }
}
