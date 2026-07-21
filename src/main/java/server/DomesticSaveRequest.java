package server;

public class DomesticSaveRequest {
    private String type = "DOMESTIC_SAVE_REQUEST";
    private String auctionStart;
    private String tradeStart;
    private String tradeEnd;
    private boolean holidayToday;
    private String expiryDate;

    public String getAuctionStart() { return auctionStart; }
    public String getTradeStart() { return tradeStart; }
    public String getTradeEnd() { return tradeEnd; }
    public boolean isHolidayToday() { return holidayToday; }
    public String getExpiryDate() { return expiryDate; }
}