package server;

import model.Position;

public class PositionResponse {
    private String type = "POSITION_RESPONSE";
    private String requestId;
    private Position position;

    public PositionResponse(String requestId, Position position) {
        this.requestId = requestId;
        this.position = position;
    }
    public String getRequestId() { return requestId; }
    public Position getPosition() { return position; }
}
