package server;

import java.util.UUID;

public class AllPositionsRequest {
    private String type = "ALL_POSITIONS_REQUEST";
    private String requestId;
    private int userId;

    public AllPositionsRequest(int userId) {
        this.userId = userId;
        this.requestId = UUID.randomUUID().toString();
    }

    public String getType() { return type; }
    public String getRequestId() { return requestId; }
    public int getUserId() { return userId; }
}