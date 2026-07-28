package server;

import model.Notice;
import java.util.List;

public class MustReadNoticeResponse {
    public String type = "MUST_READ_NOTICE_RESPONSE";
    public String requestId;
    public List<Notice> notices;

    public MustReadNoticeResponse(String requestId, List<Notice> notices) {
        this.requestId = requestId;
        this.notices = notices;
    }
}