package service;

import model.User;
import model.Position;
import server.TopInfoUpdate;
import java.util.List;

public class TopInfoService {

    private final UserService userService = new UserService();
    private final TradeHistoryDAO tradeHistoryDAO = new TradeHistoryDAO();
    // private final PositionService positionService = new PositionService();   // 🔥 필드 제거 (순환참조 원인)

    public TopInfoUpdate compute(int userId) {

        PositionService positionService = new PositionService();   // 🔥 메서드 안에서 필요할 때 생성

        User freshUser = userService.getUserById(userId);
        if (freshUser == null) return null;

        double realized = tradeHistoryDAO.getTodayNetPnl(userId);
        double unrealized = positionService.getTotalRealtimePnl(userId);
        long collateral = (long) (freshUser.getBalance() + unrealized);

        List<Position> positions = positionService.getAllPositions(userId);
        long losscut = 0;
        for (Position p : positions) {
            Market.MarketSpec spec = Market.MarketSpecCache.get(p.getSymbol());
            losscut += Math.abs(p.getQty()) * spec.getMaintMargin();
        }

        return new TopInfoUpdate(collateral, losscut, unrealized, realized);
    }

    public void pushToUser(int userId) {
        TopInfoUpdate update = compute(userId);
        if (update != null) {
            server.SessionManager.sendToCustomer(userId, update);
            System.out.println("[서버] TopInfo 즉시 push - userId: " + userId + ", " + java.time.LocalTime.now());
        }
    }
}