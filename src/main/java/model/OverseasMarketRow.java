package model;

public class OverseasMarketRow {
    private boolean holiday;
    private String symbol;
    private String displayName;
    private String tradeStart;
    private String tradeEnd;
    private String expiryDate;

    public OverseasMarketRow(boolean holiday, String symbol, String displayName,
                             String tradeStart, String tradeEnd, String expiryDate) {
        this.holiday = holiday;
        this.symbol = symbol;
        this.displayName = displayName;
        this.tradeStart = tradeStart;
        this.tradeEnd = tradeEnd;
        this.expiryDate = expiryDate;
    }

    public boolean isHoliday() { return holiday; }
    public String getSymbol() { return symbol; }
    public String getDisplayName() { return displayName; }
    public String getTradeStart() { return tradeStart; }
    public String getTradeEnd() { return tradeEnd; }
    public String getExpiryDate() { return expiryDate; }
}