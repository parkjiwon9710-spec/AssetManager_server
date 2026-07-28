package server;

import java.util.UUID;

public class PositionByIdRequest {
    private String type = "POSITION_BY_ID_REQUEST";
    private String requestId;
    private int id;

    public PositionByIdRequest(int id) {
        this.id = id;
        this.requestId = UUID.randomUUID().toString();
    }

    public String getType() { return type; }
    public String getRequestId() { return requestId; }
    public int getId() { return id; }
}
