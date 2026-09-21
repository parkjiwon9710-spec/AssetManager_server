package server;

import model.OrderAuditRow;
import java.util.List;

public class OrderAuditQueryResponse {
    private String type = "ORDER_AUDIT_QUERY_RESPONSE";   // 🔥 추가
    private boolean success;
    private List<OrderAuditRow> rows;

    public OrderAuditQueryResponse(boolean success, List<OrderAuditRow> rows) {
        this.success = success;
        this.rows = rows;
    }

    public String getType() { return type; }
    public boolean isSuccess() { return success; }
    public List<OrderAuditRow> getRows() { return rows; }
}
