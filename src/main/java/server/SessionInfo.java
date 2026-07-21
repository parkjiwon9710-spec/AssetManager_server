package server;

public class SessionInfo {
    private int userId;
    private String username;
    private String name;   // 추가
    private String ip;
    private String mac;    // 추가
    private long connectedAt;

    public SessionInfo(int userId, String username, String name, String ip, String mac, long connectedAt) {
        this.userId = userId;
        this.username = username;
        this.name = name;
        this.ip = ip;
        this.mac = mac;
        this.connectedAt = connectedAt;
    }

    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getName() { return name; }
    public String getIp() { return ip; }
    public String getMac() { return mac; }
    public long getConnectedAt() { return connectedAt; }
}