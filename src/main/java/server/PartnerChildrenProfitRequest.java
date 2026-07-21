package server;

public class PartnerChildrenProfitRequest {
    private String type = "PARTNER_CHILDREN_PROFIT_REQUEST";
    private String partnerUsername;
    private long startMillis;
    private long endMillis;

    public String getPartnerUsername() { return partnerUsername; }
    public long getStartMillis() { return startMillis; }
    public long getEndMillis() { return endMillis; }
}