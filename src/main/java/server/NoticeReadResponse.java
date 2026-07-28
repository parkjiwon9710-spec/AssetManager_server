package server;

public class NoticeReadResponse {
    public String type = "NOTICE_READ_RESPONSE";
    public String requestId;
    public boolean success;

    public NoticeReadResponse(String requestId, boolean success) {
        this.requestId = requestId;
        this.success = success;
    }
}