package server;

import model.Position;

public class PositionByIdResponse {
    private String type = "POSITION_BY_ID_RESPONSE";
    private String requestId;
    private Position position;

    public PositionByIdResponse(String requestId, Position position) {
        this.requestId = requestId;
        this.position = position;
    }

    public String getType() { return type; }
    public String getRequestId() { return requestId; }
    public Position getPosition() { return position; }
}