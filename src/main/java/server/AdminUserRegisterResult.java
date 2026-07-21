package server;

public class AdminUserRegisterResult {
    public String type = "ADMIN_USER_REGISTER_RESULT";
    public boolean success;
    public String message;

    public AdminUserRegisterResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}