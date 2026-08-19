package server;

public class AdminUserBulkEditResult {
    public String type = "ADMIN_USER_BULK_EDIT_RESULT";
    public boolean success;
    public String message;
    public int successCount;
    public int failCount;

    public AdminUserBulkEditResult(boolean success, String message, int successCount, int failCount) {
        this.success = success;
        this.message = message;
        this.successCount = successCount;
        this.failCount = failCount;
    }
}
