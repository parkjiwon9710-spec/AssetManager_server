package server;

public class AdminActionResult {
    private String type = "ADMIN_ACTION_RESULT";
    private boolean success;
    private String message;

    public AdminActionResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}