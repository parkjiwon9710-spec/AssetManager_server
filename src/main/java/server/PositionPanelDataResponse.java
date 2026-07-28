package server;

import model.Order;
import model.Position;

import java.util.List;

public class PositionPanelDataResponse {
    private String type = "POSITION_PANEL_DATA_RESPONSE";
    private List<model.PositionRow> positions;
    private List<model.PendingOrderRow> pendingOrders;

    public PositionPanelDataResponse(List<model.PositionRow> positions,
                                     List<model.PendingOrderRow> pendingOrders) {
        this.positions = positions;
        this.pendingOrders = pendingOrders;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {   // 🔥 추가
        this.type = type;
    }

    public List<model.PositionRow> getPositions() {
        return positions;
    }

    public List<model.PendingOrderRow> getPendingOrders() {
        return pendingOrders;
    }
}