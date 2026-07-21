package server;

public class AdminUserQtyLimitSaveRequest {
    public String type = "ADMIN_USER_QTY_LIMIT_SAVE_REQUEST";
    public String username;
    public int adminId;
    public int maxFuturesQty;
    public int maxOptionsBuyQty;
    public int maxOptionsSellQty;
    public int maxOverseasQty;

    public AdminUserQtyLimitSaveRequest(String username, int adminId, int maxFuturesQty,
                                        int maxOptionsBuyQty, int maxOptionsSellQty, int maxOverseasQty) {
        this.username = username;
        this.adminId = adminId;
        this.maxFuturesQty = maxFuturesQty;
        this.maxOptionsBuyQty = maxOptionsBuyQty;
        this.maxOptionsSellQty = maxOptionsSellQty;
        this.maxOverseasQty = maxOverseasQty;
    }
}