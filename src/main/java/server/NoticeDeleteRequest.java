package server;

public class NoticeDeleteRequest {
    public String type = "NOTICE_DELETE_REQUEST";
    public int adminId;
    public int id;
    public NoticeDeleteRequest(int adminId, int id) { this.adminId = adminId; this.id = id; }
}
