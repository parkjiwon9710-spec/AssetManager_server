package server;

public class NoticeAddResult {
    public String type = "NOTICE_ADD_RESULT";
    public boolean success;
    public String message;
    public NoticeAddResult(boolean success, String message) { this.success = success; this.message = message; }
}