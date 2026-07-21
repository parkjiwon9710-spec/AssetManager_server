package server;

public class AdminUserDomesticFeeSaveRequest {
    public String type = "ADMIN_USER_DOMESTIC_FEE_SAVE_REQUEST";
    public String username;
    public int adminId;
    public String futuresFee;
    public String nightFuturesFee;
    public String optionsFee;
    public String nightOptionsFee;

    public AdminUserDomesticFeeSaveRequest(String username, int adminId, String futuresFee,
                                           String nightFuturesFee, String optionsFee, String nightOptionsFee) {
        this.username = username;
        this.adminId = adminId;
        this.futuresFee = futuresFee;
        this.nightFuturesFee = nightFuturesFee;
        this.optionsFee = optionsFee;
        this.nightOptionsFee = nightOptionsFee;
    }
}
