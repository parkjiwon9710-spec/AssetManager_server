package model;

public class LoginHistoryRow {
    private int userId;
    private String username;
    private String name;
    private String ip;
    private String mac;
    private String eventType;   // LOGIN / LOGOUT
    private long eventTime;

    public LoginHistoryRow(int userId, String username, String name,
                           String ip, String mac, String eventType, long eventTime) {
        this.userId = userId;
        this.username = username;
        this.name = name;
        this.ip = ip;
        this.mac = mac;
        this.eventType = eventType;
        this.eventTime = eventTime;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    public String getMac() {
        return mac;
    }

    public String getEventType() {
        return eventType;
    }

    public long getEventTime() {
        return eventTime;
    }
}