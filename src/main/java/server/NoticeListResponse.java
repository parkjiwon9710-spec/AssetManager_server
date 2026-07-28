package server;

import model.Notice;
import java.util.List;
public class NoticeListResponse {
    public String type = "NOTICE_LIST_RESPONSE";
    public List<Notice> notices;
    public NoticeListResponse(List<Notice> notices) { this.notices = notices; }
}
