package service;

import model.*;
import server.AdminAllPositionsResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * 관리자 "고객 포지션" 패널용 - 전체 고객의 포지션/미체결을 모아서 반환.
 * 기존에 있던 1인분 조회 메서드들(positionService.loadPositionRows(userId),
 * orderDAO.loadPendingOrderRows(userId))을 고객 목록만큼 반복 호출해서 합치는 방식이라
 * PositionService / OrderDAO 자체는 전혀 건드리지 않습니다.
 *
 * ⚠️ 아래 getter들은 기존 model.PositionRow / model.PendingOrderRow / model.AdminUserListRow에
 * "이런 이름일 것"이라고 가정하고 작성했습니다. 실제 필드명과 다르면 컴파일 에러 나는 부분만
 * 맞춰서 고쳐주시면 됩니다.
 *   - model.AdminUserListRow: getId() (userId), getUsername(), getName()
 *   - model.PositionRow: getSymbol(), getSide(), getQty(), getAvgPrice(), getCurrentPrice(), getPnl()
 *   - model.PendingOrderRow: getSymbol(), getSide(), getQty(), getOrderPrice() (or getPrice()), getCurrentPrice(), getOrderId()
 */
public class AdminPositionAggregateService {

    private final AdminUserListService adminUserListService;
    private final PositionService positionService;
    private final OrderDAO orderDAO;

    public AdminPositionAggregateService(AdminUserListService adminUserListService,
                                         PositionService positionService,
                                         OrderDAO orderDAO) {
        this.adminUserListService = adminUserListService;
        this.positionService = positionService;
        this.orderDAO = orderDAO;
    }

    public AdminAllPositionsResponse computeAll() {
        List<AdminPositionRow> positions = new ArrayList<>();
        List<AdminPendingOrderRow> pendingOrders = new ArrayList<>();

        List<AdminUserListRow> customers = adminUserListService.loadCustomers("");

        for (AdminUserListRow customer : customers) {
            int userId = customer.getId();
            String name = customer.getName();
            String username = customer.getUsername();

            for (PositionRow p : positionService.loadPositionRows(userId)) {
                positions.add(new AdminPositionRow(
                        name, username, userId,
                        p.getSymbol(), p.getDisplaySide(), p.getQty(),
                        p.getAvgPrice(), p.getCurrentPrice(), p.getPnl()
                ));
            }

            for (PendingOrderRow o : orderDAO.loadPendingOrderRows(userId)) {
                pendingOrders.add(new AdminPendingOrderRow(
                        name, username, userId,
                        o.getSymbol(), o.getSide(), o.getQty(),
                        o.getOrderPrice(), o.getCurrentPrice(), o.getId()
                ));
            }
        }

        return new AdminAllPositionsResponse(positions, pendingOrders);
    }
}