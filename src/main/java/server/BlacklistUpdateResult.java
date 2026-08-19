package server;

public class BlacklistUpdateResult {
    public String type = "BLACKLIST_UPDATE_RESULT";
    public boolean success;

    public BlacklistUpdateResult(boolean success) {
        this.success = success;
    }
}
