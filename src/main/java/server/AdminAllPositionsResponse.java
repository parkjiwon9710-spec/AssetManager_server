package server;

import model.AdminPendingOrderRow;
import model.AdminPositionRow;

import java.util.List;

public class AdminAllPositionsResponse {
    private String type = "ADMIN_ALL_POSITIONS_UPDATE";
    private List<AdminPositionRow> positions;
    private List<AdminPendingOrderRow> pendingOrders;

    public AdminAllPositionsResponse(List<AdminPositionRow> positions, List<AdminPendingOrderRow> pendingOrders) {
        this.positions = positions;
        this.pendingOrders = pendingOrders;
    }

    public List<AdminPositionRow> getPositions() { return positions; }
    public List<AdminPendingOrderRow> getPendingOrders() { return pendingOrders; }
}