package server;

public class AvailableQtyResponse {
    private String type = "AVAILABLE_QTY_RESPONSE";
    private int maxBuyQty;
    private int maxSellQty;

    public AvailableQtyResponse(int maxBuyQty, int maxSellQty) {
        this.maxBuyQty = maxBuyQty;
        this.maxSellQty = maxSellQty;
    }
}