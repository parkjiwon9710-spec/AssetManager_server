package server;

public class OrderAuditQueryRequest {
    private String type = "ORDER_AUDIT_QUERY_REQUEST";   // 🔥 추가
    private int userId;
    private String date;

    public OrderAuditQueryRequest(int userId, String date) {
        this.userId = userId;
        this.date = date;
    }

    public String getType() { return type; }
    public int getUserId() { return userId; }
    public String getDate() { return date; }
}