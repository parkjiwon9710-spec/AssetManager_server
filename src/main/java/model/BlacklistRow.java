package model;

public class BlacklistRow {
    private int id;
    private String type;      // "IP" or "MAC"
    private String value;
    private String reason;
    private String createdAt;

    public BlacklistRow(int id, String type, String value, String reason, String createdAt) {
        this.id = id;
        this.type = type;
        this.value = value;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public String getType() { return type; }
    public String getValue() { return value; }
    public String getReason() { return reason; }
    public String getCreatedAt() { return createdAt; }
}
