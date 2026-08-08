package model;

public class AdminUserQtyLimitData {
    private int maxFuturesQty;
    private int maxOptionsQty;
    private int maxOverseasQty;

    public int getMaxFuturesQty() { return maxFuturesQty; }
    public void setMaxFuturesQty(int v) { this.maxFuturesQty = v; }
    public int getMaxOptionsQty() { return maxOptionsQty; }
    public void setMaxOptionsQty(int v) { this.maxOptionsQty = v; }
    public int getMaxOverseasQty() { return maxOverseasQty; }
    public void setMaxOverseasQty(int v) { this.maxOverseasQty = v; }
}