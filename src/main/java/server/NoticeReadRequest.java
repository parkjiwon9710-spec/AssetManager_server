package server;

import java.util.UUID;

public class NoticeReadRequest {
    public String type = "NOTICE_READ_REQUEST";
    public String requestId = UUID.randomUUID().toString();
    public int userId;
    public int noticeId;

    public NoticeReadRequest(int userId, int noticeId) {
        this.userId = userId;
        this.noticeId = noticeId;
    }

    public String getRequestId() { return requestId; }
}