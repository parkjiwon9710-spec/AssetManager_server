package model;

public class SystemQtyLimit {

    private int maxFuturesQty;
    private int maxOptionsQty;
    private int maxOverseasQty;

    public int getMaxFuturesQty() {
        return maxFuturesQty;
    }

    public void setMaxFuturesQty(int maxFuturesQty) {
        this.maxFuturesQty = maxFuturesQty;
    }

    public int getMaxOptionsQty() {
        return maxOptionsQty;
    }

    public void setMaxOptionsQty(int maxOptionsQty) {
        this.maxOptionsQty = maxOptionsQty;
    }

    public int getMaxOverseasQty() {
        return maxOverseasQty;
    }

    public void setMaxOverseasQty(int maxOverseasQty) {
        this.maxOverseasQty = maxOverseasQty;
    }
}