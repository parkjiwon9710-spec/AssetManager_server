package server;

public class AdminUserBasicInfoSaveResult {
    public String type = "ADMIN_USER_BASIC_INFO_SAVE_RESULT";
    public boolean success;
    public String message;

    public AdminUserBasicInfoSaveResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
