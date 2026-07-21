package model;

public class FeeOverrideRow {
    private String symbol;
    private boolean enabled;
    private double fee;

    public FeeOverrideRow(String symbol, boolean enabled, double fee) {
        this.symbol = symbol;
        this.enabled = enabled;
        this.fee = fee;
    }

    public String getSymbol() { return symbol; }
    public boolean isEnabled() { return enabled; }
    public double getFee() { return fee; }
}
