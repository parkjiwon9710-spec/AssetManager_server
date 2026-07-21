package model;

public class SystemQtyLimit {

    private int maxFuturesQty;
    private int maxOptionsBuyQty;
    private int maxOptionsSellQty;
    private int maxOverseasQty;

    public int getMaxFuturesQty() {
        return maxFuturesQty;
    }

    public void setMaxFuturesQty(int maxFuturesQty) {
        this.maxFuturesQty = maxFuturesQty;
    }

    public int getMaxOptionsBuyQty() {
        return maxOptionsBuyQty;
    }

    public void setMaxOptionsBuyQty(int maxOptionsBuyQty) {
        this.maxOptionsBuyQty = maxOptionsBuyQty;
    }

    public int getMaxOptionsSellQty() {
        return maxOptionsSellQty;
    }

    public void setMaxOptionsSellQty(int maxOptionsSellQty) {
        this.maxOptionsSellQty = maxOptionsSellQty;
    }

    public int getMaxOverseasQty() {
        return maxOverseasQty;
    }

    public void setMaxOverseasQty(int maxOverseasQty) {
        this.maxOverseasQty = maxOverseasQty;
    }
}