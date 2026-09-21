package model;

public class OverseasMarketRow {
    private boolean holiday;
    private String symbol;
    private String displayName;
    private String tradeStart;
    private String tradeEnd;
    private String expiryDate;
    private Integer rolloverDaysBeforeExpiry;   // 🔥 신규
    private String rolloverStatus;               // 🔥 신규

    public OverseasMarketRow(boolean holiday, String symbol, String displayName,
                             String tradeStart, String tradeEnd, String expiryDate,
                             Integer rolloverDaysBeforeExpiry, String rolloverStatus) {
        this.holiday = holiday;
        this.symbol = symbol;
        this.displayName = displayName;
        this.tradeStart = tradeStart;
        this.tradeEnd = tradeEnd;
        this.expiryDate = expiryDate;
        this.rolloverDaysBeforeExpiry = rolloverDaysBeforeExpiry;
        this.rolloverStatus = rolloverStatus;
    }

    public boolean isHoliday() { return holiday; }
    public String getSymbol() { return symbol; }
    public String getDisplayName() { return displayName; }
    public String getTradeStart() { return tradeStart; }
    public String getTradeEnd() { return tradeEnd; }
    public String getExpiryDate() { return expiryDate; }
    public Integer getRolloverDaysBeforeExpiry() { return rolloverDaysBeforeExpiry; }
    public String getRolloverStatus() { return rolloverStatus; }
}