package model;

public class MarketMarginInfo {
    private long entryMargin;
    private long maintMargin;
    private long overnightMargin;
    private boolean overnightEnabled;

    public MarketMarginInfo(long entryMargin, long maintMargin, long overnightMargin, boolean overnightEnabled) {
        this.entryMargin = entryMargin;
        this.maintMargin = maintMargin;
        this.overnightMargin = overnightMargin;
        this.overnightEnabled = overnightEnabled;
    }

    public long getEntryMargin() { return entryMargin; }
    public long getMaintMargin() { return maintMargin; }
    public long getOvernightMargin() { return overnightMargin; }
    public boolean isOvernightEnabled() { return overnightEnabled; }
}