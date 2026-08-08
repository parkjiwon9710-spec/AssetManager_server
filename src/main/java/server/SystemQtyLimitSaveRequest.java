package server;


public class SystemQtyLimitSaveRequest {
    private String type = "SYSTEM_QTY_LIMIT_SAVE_REQUEST";
    private int futuresQty, optionQty, overseasQty;

    public SystemQtyLimitSaveRequest(int futuresQty, int optionQty, int overseasQty) {
        this.futuresQty = futuresQty;
        this.optionQty = optionQty;
        this.overseasQty = overseasQty;
    }

    public int getFuturesQty() { return futuresQty; }
    public int getOptionQty() { return optionQty; }
    public int getOverseasQty() { return overseasQty; }
}