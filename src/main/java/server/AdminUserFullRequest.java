package server;

public class AdminUserFullRequest {
    public String type = "ADMIN_USER_FULL_REQUEST";
    public String username;

    public AdminUserFullRequest(String username) {
        this.username = username;
    }
}