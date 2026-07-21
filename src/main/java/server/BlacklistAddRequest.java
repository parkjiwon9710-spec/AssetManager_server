package server;

public class BlacklistAddRequest {
    private String type = "BLACKLIST_ADD";
    private String targetType; // "IP" or "MAC"
    private String value;
    private String reason;

    public String getTargetType() { return targetType; }
    public String getValue() { return value; }
    public String getReason() { return reason; }
}