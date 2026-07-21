package server;

public class AdminUserQtyLimitSaveResult {
    public String type = "ADMIN_USER_QTY_LIMIT_SAVE_RESULT";
    public boolean success;
    public String message;

    public AdminUserQtyLimitSaveResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}