package server;

public class OrderRequest {
    private String type = "ORDER_REQUEST";
    private int userId;
    private String symbol;
    private String side;
    private double price;
    private int qty;

    // 🔥 추가
    private boolean tpEnabled;
    private int tpTicks;
    private boolean slEnabled;
    private int slTicks;

    public int getUserId() { return userId; }
    public String getSymbol() { return symbol; }
    public String getSide() { return side; }
    public double getPrice() { return price; }
    public int getQty() { return qty; }

    // getter 추가
    public boolean isTpEnabled() { return tpEnabled; }
    public int getTpTicks() { return tpTicks; }
    public boolean isSlEnabled() { return slEnabled; }
    public int getSlTicks() { return slTicks; }
}