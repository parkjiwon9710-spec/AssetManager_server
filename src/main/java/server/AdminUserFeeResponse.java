package server;

import model.AdminUserFeeData;

public class AdminUserFeeResponse {
    public String type = "ADMIN_USER_FEE_RESPONSE";
    public boolean success;
    public String message;
    public AdminUserFeeData data;

    public AdminUserFeeResponse(boolean success, String message, AdminUserFeeData data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
}
