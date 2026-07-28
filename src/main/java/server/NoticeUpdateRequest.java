package server;

public class NoticeUpdateRequest {
    public String type = "NOTICE_UPDATE_REQUEST";
    public int adminId;
    public int id;
    public String title;
    public String noticeType;
    public String contentRtf;
    public NoticeUpdateRequest(int adminId, int id, String title, String noticeType, String contentRtf) {
        this.adminId = adminId; this.id = id; this.title = title; this.noticeType = noticeType; this.contentRtf = contentRtf;
    }
}