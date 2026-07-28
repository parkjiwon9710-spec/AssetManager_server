package server;

import java.util.UUID;

public class MustReadNoticeRequest {
    public String type = "MUST_READ_NOTICE_REQUEST";
    public String requestId = UUID.randomUUID().toString();
    public int userId;

    public MustReadNoticeRequest(int userId) {
        this.userId = userId;
    }

    public String getRequestId() { return requestId; }
}
