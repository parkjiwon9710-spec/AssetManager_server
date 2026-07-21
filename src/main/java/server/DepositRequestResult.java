package server;


public class DepositRequestResult {
    public String type = "DEPOSIT_REQUEST_RESULT";
    public boolean success;
    public String message;

    public DepositRequestResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}