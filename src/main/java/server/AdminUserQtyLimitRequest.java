package server;

public class AdminUserQtyLimitRequest {
    public String type = "ADMIN_USER_QTY_LIMIT_REQUEST";
    public String username;

    public AdminUserQtyLimitRequest(String username) {
        this.username = username;
    }
}
