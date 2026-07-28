package model;

public class TradeLimitSettings {
    private MarketMarginInfo overseas;
    private MarketMarginInfo domestic;
    private MarketMarginInfo option;
    private SystemTradeMode mode;

    public TradeLimitSettings(MarketMarginInfo overseas, MarketMarginInfo domestic,
                              MarketMarginInfo option, SystemTradeMode mode) {
        this.overseas = overseas;
        this.domestic = domestic;
        this.option = option;
        this.mode = mode;
    }

    public MarketMarginInfo getOverseas() { return overseas; }
    public MarketMarginInfo getDomestic() { return domestic; }
    public MarketMarginInfo getOption() { return option; }
    public SystemTradeMode getMode() { return mode; }
}