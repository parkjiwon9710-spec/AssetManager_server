package server;

public class BlacklistRemoveRequest {
    public String type = "BLACKLIST_REMOVE_REQUEST";
    public int id;

    public BlacklistRemoveRequest(int id) {
        this.id = id;
    }
}
