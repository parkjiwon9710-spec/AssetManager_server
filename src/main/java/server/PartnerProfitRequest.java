package server;

public class PartnerProfitRequest {
    private String type = "PARTNER_PROFIT_REQUEST";
    private long startMillis;
    private long endMillis;

    public long getStartMillis() { return startMillis; }
    public long getEndMillis() { return endMillis; }
}