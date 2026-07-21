package model;

public class SymbolTradeSetting {

    private String symbol;

    private long entryMargin;
    private long maintMargin;
    private long overnightMargin;

    private boolean overnightEnabled;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public long getEntryMargin() {
        return entryMargin;
    }

    public void setEntryMargin(long entryMargin) {
        this.entryMargin = entryMargin;
    }

    public long getMaintMargin() {
        return maintMargin;
    }

    public void setMaintMargin(long maintMargin) {
        this.maintMargin = maintMargin;
    }

    public long getOvernightMargin() {
        return overnightMargin;
    }

    public void setOvernightMargin(long overnightMargin) {
        this.overnightMargin = overnightMargin;
    }

    public boolean isOvernightEnabled() {
        return overnightEnabled;
    }

    public void setOvernightEnabled(boolean overnightEnabled) {
        this.overnightEnabled = overnightEnabled;
    }
}