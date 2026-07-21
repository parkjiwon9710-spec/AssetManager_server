package server;

public class AdminUserBasicInfoRequest {
    public String type = "ADMIN_USER_BASIC_INFO_REQUEST";
    public String username;

    public AdminUserBasicInfoRequest(String username) {
        this.username = username;
    }
}
