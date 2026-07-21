package server;

public class MarketOperationSaveResponse {
    private String type = "MARKET_OPERATION_SAVE_RESPONSE";
    private boolean success;
    private String message;

    public MarketOperationSaveResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
