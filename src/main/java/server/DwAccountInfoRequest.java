package server;

public class DwAccountInfoRequest {
    public String type = "DW_ACCOUNT_INFO_REQUEST";
    public int userId;

    public DwAccountInfoRequest(int userId) {
        this.userId = userId;
    }
}
