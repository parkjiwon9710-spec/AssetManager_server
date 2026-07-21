package server;

import model.AdminUserQtyLimitData;

public class AdminUserQtyLimitResponse {
    public String type = "ADMIN_USER_QTY_LIMIT_RESPONSE";
    public boolean success;
    public String message;
    public AdminUserQtyLimitData data;
    public int systemMaxOverseasQty;   // 🔥 저장 시 검증용 시스템 최대값도 같이 내려줌

    public AdminUserQtyLimitResponse(boolean success, String message, AdminUserQtyLimitData data, int systemMaxOverseasQty) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.systemMaxOverseasQty = systemMaxOverseasQty;
    }
}