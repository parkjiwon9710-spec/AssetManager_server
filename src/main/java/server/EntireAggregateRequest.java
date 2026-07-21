package server;

public class EntireAggregateRequest {
    private String type = "ENTIRE_AGGREGATE_REQUEST";
    private long startMillis;
    private long endMillis;

    public long getStartMillis() { return startMillis; }
    public long getEndMillis() { return endMillis; }
}