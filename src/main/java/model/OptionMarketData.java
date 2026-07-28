package model;

public class OptionMarketData {

    private String tradeStart;
    private String tradeEnd;
    private boolean holidayToday;
    private String expiryDate;

    public OptionMarketData(
            String tradeStart,
            String tradeEnd,
            boolean holidayToday,
            String expiryDate
    ) {
        this.tradeStart = tradeStart;
        this.tradeEnd = tradeEnd;
        this.holidayToday = holidayToday;
        this.expiryDate = expiryDate;
    }

    public String getTradeStart() {
        return tradeStart;
    }

    public String getTradeEnd() {
        return tradeEnd;
    }

    public boolean isHolidayToday() {
        return holidayToday;
    }

    public String getExpiryDate() {
        return expiryDate;
    }
}