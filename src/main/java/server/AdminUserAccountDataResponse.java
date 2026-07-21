package server;

import model.AdminUserAccountData;

public class AdminUserAccountDataResponse {
    public String type = "ADMIN_USER_ACCOUNT_DATA_RESPONSE";
    public boolean success;
    public String message;
    public AdminUserAccountData data;

    public AdminUserAccountDataResponse(boolean success, String message, AdminUserAccountData data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
}
