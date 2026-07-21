package server;

import model.AdminUserBasicInfo;

public class AdminUserBasicInfoResponse {
    public String type = "ADMIN_USER_BASIC_INFO_RESPONSE";
    public boolean success;
    public String message;
    public AdminUserBasicInfo data;

    public AdminUserBasicInfoResponse(boolean success, String message, AdminUserBasicInfo data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
}
