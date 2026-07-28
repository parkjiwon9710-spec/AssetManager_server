package server;

public class OptionSaveRequest {

    private String type = "OPTION_SAVE_REQUEST";
    private String tradeStart;
    private String tradeEnd;
    private boolean holidayToday;
    private String expiryDate;

    public OptionSaveRequest(
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

    public String getType() {
        return type;
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
