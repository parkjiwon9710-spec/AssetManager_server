package server;

public class LoginHistoryRequest {
    private String type = "LOGIN_HISTORY_REQUEST";
    private long startMillis;
    private long endMillis;
    private String keyword;

    public LoginHistoryRequest(long startMillis, long endMillis, String keyword) {
        this.startMillis = startMillis;
        this.endMillis = endMillis;
        this.keyword = keyword;
    }

    public long getStartMillis() { return startMillis; }
    public long getEndMillis() { return endMillis; }
    public String getKeyword() { return keyword; }
}
