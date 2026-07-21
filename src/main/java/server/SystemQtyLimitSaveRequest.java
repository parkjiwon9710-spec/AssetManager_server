package server;

public class SystemQtyLimitSaveRequest {
    private String type = "SYSTEM_QTY_LIMIT_SAVE_REQUEST";
    private int futuresQty;
    private int optionBuyQty;
    private int optionSellQty;
    private int overseasQty;

    public int getFuturesQty() { return futuresQty; }
    public int getOptionBuyQty() { return optionBuyQty; }
    public int getOptionSellQty() { return optionSellQty; }
    public int getOverseasQty() { return overseasQty; }
}
