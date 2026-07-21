package server;

public class AdminUserAccountDataRequest {
    public String type = "ADMIN_USER_ACCOUNT_DATA_REQUEST";
    public String username;

    public AdminUserAccountDataRequest(String username) {
        this.username = username;
    }
}