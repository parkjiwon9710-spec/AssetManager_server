package server;

public class AdminUserBalanceUpdateRequest {
    public String type = "ADMIN_USER_BALANCE_UPDATE_REQUEST";
    public String username;
    public int adminId;
    public long delta;      // 양수=입금, 음수=출금
    public String memo;

    public AdminUserBalanceUpdateRequest(String username, int adminId, long delta, String memo) {
        this.username = username;
        this.adminId = adminId;
        this.delta = delta;
        this.memo = memo;
    }
}
