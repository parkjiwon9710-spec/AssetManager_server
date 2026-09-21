package server;

public class ExchangeRateUpdateResult {
    private final String type = "EXCHANGE_RATE_UPDATE_RESULT";
    private boolean success;
    private String message;

    public ExchangeRateUpdateResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public String getType() { return type; }
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}
