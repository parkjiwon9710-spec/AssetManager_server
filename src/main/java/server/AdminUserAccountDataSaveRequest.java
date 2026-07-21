package server;

public class AdminUserAccountDataSaveRequest {
    public String type = "ADMIN_USER_ACCOUNT_DATA_SAVE_REQUEST";
    public String username;
    public int adminId;
    public String accountStatus;
    public String server;
    public String mileage;
    public String memo;

    public AdminUserAccountDataSaveRequest(String username, int adminId, String accountStatus,
                                           String server, String mileage, String memo) {
        this.username = username;
        this.adminId = adminId;
        this.accountStatus = accountStatus;
        this.server = server;
        this.mileage = mileage;
        this.memo = memo;
    }
}
