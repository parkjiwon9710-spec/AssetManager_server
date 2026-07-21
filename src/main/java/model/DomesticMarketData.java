package model;

public class DomesticMarketData {
    private String auctionStart;
    private String tradeStart;
    private String tradeEnd;
    private boolean holidayToday;
    private String expiryDate;

    public DomesticMarketData(String auctionStart, String tradeStart, String tradeEnd,
                              boolean holidayToday, String expiryDate) {
        this.auctionStart = auctionStart;
        this.tradeStart = tradeStart;
        this.tradeEnd = tradeEnd;
        this.holidayToday = holidayToday;
        this.expiryDate = expiryDate;
    }

    public String getAuctionStart() { return auctionStart; }
    public String getTradeStart() { return tradeStart; }
    public String getTradeEnd() { return tradeEnd; }
    public boolean isHolidayToday() { return holidayToday; }
    public String getExpiryDate() { return expiryDate; }
}
