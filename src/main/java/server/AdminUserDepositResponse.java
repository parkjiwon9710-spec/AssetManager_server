package server;

import model.AdminUserDepositData;

public class AdminUserDepositResponse {
    public String type = "ADMIN_USER_DEPOSIT_RESPONSE";
    public boolean success;
    public String message;
    public AdminUserDepositData data;

    public AdminUserDepositResponse(boolean success, String message, AdminUserDepositData data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
}