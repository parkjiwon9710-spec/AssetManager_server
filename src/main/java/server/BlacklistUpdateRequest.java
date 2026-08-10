package server;

public class BlacklistUpdateRequest {
    public String type = "BLACKLIST_UPDATE_REQUEST";
    public int id;
    public String value;
    public String reason;

    public BlacklistUpdateRequest(int id, String value, String reason) {
        this.id = id;
        this.value = value;
        this.reason = reason;
    }
}
