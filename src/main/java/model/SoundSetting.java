package model;

public class SoundSetting {

    private boolean buyExecuted;
    private boolean sellExecuted;

    private boolean buyReserved;
    private boolean sellReserved;

    private boolean orderModified;
    private boolean orderCancelled;

    public boolean isBuyExecuted() {
        return buyExecuted;
    }

    public void setBuyExecuted(boolean buyExecuted) {
        this.buyExecuted = buyExecuted;
    }

    public boolean isSellExecuted() {
        return sellExecuted;
    }

    public void setSellExecuted(boolean sellExecuted) {
        this.sellExecuted = sellExecuted;
    }

    public boolean isBuyReserved() {
        return buyReserved;
    }

    public void setBuyReserved(boolean buyReserved) {
        this.buyReserved = buyReserved;
    }

    public boolean isSellReserved() {
        return sellReserved;
    }

    public void setSellReserved(boolean sellReserved) {
        this.sellReserved = sellReserved;
    }

    public boolean isOrderModified() {
        return orderModified;
    }

    public void setOrderModified(boolean orderModified) {
        this.orderModified = orderModified;
    }

    public boolean isOrderCancelled() {
        return orderCancelled;
    }

    public void setOrderCancelled(boolean orderCancelled) {
        this.orderCancelled = orderCancelled;
    }
}