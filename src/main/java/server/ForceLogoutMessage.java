package server;

public class ForceLogoutMessage {
    private String type = "FORCE_LOGOUT";
    private String reason;

    public ForceLogoutMessage(String reason) {
        this.reason = reason;
    }

    public String getType() { return type; }
    public String getReason() { return reason; }
}