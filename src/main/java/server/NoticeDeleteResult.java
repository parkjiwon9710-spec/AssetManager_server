package server;

public class NoticeDeleteResult {
    public String type = "NOTICE_DELETE_RESULT";
    public boolean success;
    public String message;
    public NoticeDeleteResult(boolean success, String message) { this.success = success; this.message = message; }
}
