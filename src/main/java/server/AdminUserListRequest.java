package server;

public class AdminUserListRequest {
    public String type = "ADMIN_USER_LIST_REQUEST";
    public String keyword;

    public AdminUserListRequest(String keyword) {
        this.keyword = keyword;
    }
}