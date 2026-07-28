package server;

public class NoticeUpdateResult {
    public String type = "NOTICE_UPDATE_RESULT";
    public boolean success;
    public String message;
    public NoticeUpdateResult(boolean success, String message) { this.success = success; this.message = message; }
}