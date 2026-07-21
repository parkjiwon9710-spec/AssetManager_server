package model;

public class AdminUserQtyLimitData {
    private int maxFuturesQty;
    private int maxOptionsBuyQty;
    private int maxOptionsSellQty;
    private int maxOverseasQty;

    public int getMaxFuturesQty() { return maxFuturesQty; }
    public void setMaxFuturesQty(int v) { this.maxFuturesQty = v; }
    public int getMaxOptionsBuyQty() { return maxOptionsBuyQty; }
    public void setMaxOptionsBuyQty(int v) { this.maxOptionsBuyQty = v; }
    public int getMaxOptionsSellQty() { return maxOptionsSellQty; }
    public void setMaxOptionsSellQty(int v) { this.maxOptionsSellQty = v; }
    public int getMaxOverseasQty() { return maxOverseasQty; }
    public void setMaxOverseasQty(int v) { this.maxOverseasQty = v; }
}