package server;

public class AdminUserBalanceUpdateResult {
    public String type = "ADMIN_USER_BALANCE_UPDATE_RESULT";
    public boolean success;
    public String message;

    public AdminUserBalanceUpdateResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
