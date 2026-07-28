package service;

import model.PositionRow;
import model.PendingOrderRow;
import server.PositionPanelDataResponse;
import server.SessionManager;

import java.util.List;

public class PositionPanelService {

    private final PositionService positionService = new PositionService();
    private final OrderDAO orderDAO = new OrderDAO();

    public void pushToUser(int userId) {
        List<PositionRow> positions = positionService.loadPositionRows(userId);
        List<PendingOrderRow> pending = orderDAO.loadPendingOrderRows(userId);

        PositionPanelDataResponse response = new PositionPanelDataResponse(positions, pending);
        response.setType("POSITION_PANEL_DATA_PUSH");

        SessionManager.sendToCustomer(userId, response);
        System.out.println("[서버] PositionPanel 즉시 push - userId: " + userId + ", " + java.time.LocalTime.now());
    }
}