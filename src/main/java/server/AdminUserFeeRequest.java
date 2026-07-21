package server;

public class AdminUserFeeRequest {
    public String type = "ADMIN_USER_FEE_REQUEST";
    public String username;

    public AdminUserFeeRequest(String username) {
        this.username = username;
    }
}