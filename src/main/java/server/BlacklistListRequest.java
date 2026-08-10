package server;

public class BlacklistListRequest {
    public String type = "BLACKLIST_LIST_REQUEST";
    public String filterType; // "IP", "MAC", or null(전체)

    public BlacklistListRequest(String filterType) {
        this.filterType = filterType;
    }
}
