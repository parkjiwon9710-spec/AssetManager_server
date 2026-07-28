package server;

import model.TradeLimitSettings;

public class TradeLimitLoadResponse {
    private String type = "TRADE_LIMIT_LOAD_RESPONSE";
    private TradeLimitSettings settings;

    public TradeLimitLoadResponse(TradeLimitSettings settings) {
        this.settings = settings;
    }
}