package server;

public class AdminUserFullSaveResult {
    public String type = "ADMIN_USER_FULL_SAVE_RESULT";
    public boolean success;
    public String message;

    public AdminUserFullSaveResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
