package server;

public class BlacklistRemoveResult {
    public String type = "BLACKLIST_REMOVE_RESULT";
    public boolean success;

    public BlacklistRemoveResult(boolean success) {
        this.success = success;
    }
}
