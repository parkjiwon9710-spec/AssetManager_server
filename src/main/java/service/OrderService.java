package service;

import Store.PriceStore;
import model.OrderSide;

public class OrderService {

    private final OrderDAO orderDAO = new OrderDAO();
    private final OrderExecutionService executionService;

    public OrderService(OrderExecutionService executionService) {
        this.executionService = executionService;
    }

    // 리스크체크 없이 강제로 시장가 청산 (오버나잇 처리 전용)
    public boolean placeForceCloseOrder(int userId, String symbol, OrderSide side, int qty) {
        double executionPrice = side == OrderSide.BUY
                ? PriceStore.getBestAsk(symbol)
                : PriceStore.getBestBid(symbol);

        if (executionPrice <= 0) return false;

        // 서버 ORDER_REQUEST 핸들러가 쓰는 것과 동일한 executeMarket 시그니처 재사용
        int orderId = executionService.executeMarket(
                userId, symbol, side, qty, executionPrice,
                false, 0, false, 0
        );

        return orderId > 0;
    }

    public void cancelPendingOrdersBySymbol(String symbol) {
        orderDAO.cancelAllPendingBySymbol(symbol);
    }
}