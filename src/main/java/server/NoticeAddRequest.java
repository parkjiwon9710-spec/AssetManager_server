package server;

public class NoticeAddRequest {
    public String type = "NOTICE_ADD_REQUEST";
    public int adminId;
    public String title;
    public String noticeType;
    public String contentRtf;
    public NoticeAddRequest(int adminId, String title, String noticeType, String contentRtf) {
        this.adminId = adminId; this.title = title; this.noticeType = noticeType; this.contentRtf = contentRtf;
    }
}