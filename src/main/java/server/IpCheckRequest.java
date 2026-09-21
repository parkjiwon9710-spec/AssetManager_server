package server;

public class IpCheckRequest {
    private String type = "IP_CHECK_REQUEST";
    private int userId;
    private String mac;

    public IpCheckRequest(int userId, String mac) {
        this.userId = userId;
        this.mac = mac;
    }

    public int getUserId() { return userId; }
    public String getMac() { return mac; }
}
