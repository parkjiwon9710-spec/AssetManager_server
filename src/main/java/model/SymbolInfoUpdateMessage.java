package model;

public class SymbolInfoUpdateMessage {
    private final String type = "SYMBOL_INFO_UPDATE";
    private String symbol;
    private double last;
    private double prevClose;
    private double open;
    private double high;
    private double low;
    private String expiry;

    public SymbolInfoUpdateMessage(String symbol, double last, double prevClose, double open, double high, double low,String expiry) {
        this.symbol = symbol;
        this.last = last;
        this.prevClose = prevClose;
        this.open = open;
        this.high = high;
        this.low = low;
        this.expiry = expiry;
    }

    public String getType() { return type; }
    public String getSymbol() { return symbol; }
    public double getLast() { return last; }
    public double getPrevClose() { return prevClose; }
    public double getOpen() { return open; }
    public double getHigh() { return high; }
    public double getLow() { return low; }
    public String getExpiry() { return expiry; }
}