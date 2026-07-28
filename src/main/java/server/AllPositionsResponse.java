package server;

import model.Position;

import java.util.List;

public class AllPositionsResponse {
    private String type = "ALL_POSITIONS_RESPONSE";
    private String requestId;
    private List<Position> positions;

    public AllPositionsResponse(String requestId, List<Position> positions) {
        this.requestId = requestId;
        this.positions = positions;
    }

    public String getType() { return type; }
    public String getRequestId() { return requestId; }
    public List<Position> getPositions() { return positions; }
}
