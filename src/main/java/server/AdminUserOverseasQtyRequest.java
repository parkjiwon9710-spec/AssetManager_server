package server;

public class AdminUserOverseasQtyRequest {
    public String type = "ADMIN_USER_OVERSEAS_QTY_REQUEST";
    public String username;

    public AdminUserOverseasQtyRequest(String username) {
        this.username = username;
    }
}