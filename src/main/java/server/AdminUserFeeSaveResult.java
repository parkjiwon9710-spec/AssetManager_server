package server;

public class AdminUserFeeSaveResult {
    public String type = "ADMIN_USER_FEE_SAVE_RESULT";
    public boolean success;
    public String message;

    public AdminUserFeeSaveResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}