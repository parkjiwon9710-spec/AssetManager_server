package server;

public class AdminUserDepositRequest {
    public String type = "ADMIN_USER_DEPOSIT_REQUEST";
    public String username;

    public AdminUserDepositRequest(String username) {
        this.username = username;
    }
}
