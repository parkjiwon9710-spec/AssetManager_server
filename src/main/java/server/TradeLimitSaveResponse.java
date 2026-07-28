package server;

public class TradeLimitSaveResponse {
    private String type = "TRADE_LIMIT_SAVE_RESPONSE";
    private boolean success;
    private String message;

    public TradeLimitSaveResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}