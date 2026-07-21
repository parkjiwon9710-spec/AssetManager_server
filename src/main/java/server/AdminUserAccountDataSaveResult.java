package server;

public class AdminUserAccountDataSaveResult {
    public String type = "ADMIN_USER_ACCOUNT_DATA_SAVE_RESULT";
    public boolean success;
    public String message;

    public AdminUserAccountDataSaveResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
