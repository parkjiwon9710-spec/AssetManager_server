package service;

import model.OrderSide;
import model.OrderType;
import model.Order;
import Market.MarketSpecCache;
import Store.PriceStore;
import model.Position;
import server.ClientEventMessage;
import server.SessionManager;

import java.util.List;


/// ////checkLiquidation → 락 체크 추가하지 않음 (로스컷 최우선 원칙 유지)
/// processPendingOrders, checkTpSl → 락 체크 추가 (오버나잇 처리 중 데이터 경합 방지, 약간의 지연은 허용)
///
public class OrderExecutionService {

    private final OrderDAO orderDAO = new OrderDAO();
    private final PositionService positionService = new PositionService();
    private final TopInfoService topInfoService = new TopInfoService();
    private final RiskService riskService = new RiskService();
    // 시장가/체결 처리 공통 로직 (기존 DemoServer의 ORDER_REQUEST 인라인 로직을 여기로 이동)
    public int executeMarket(int userId, String symbol, OrderSide side, int qty, double executionPrice,
                             boolean tpEnabled, int tpTicks, boolean slEnabled, int slTicks){

        int orderId = orderDAO.insertFilled(userId, symbol, side.name(), executionPrice, qty);

        if (orderId > 0) {
            positionService.applyTrade(orderId, userId, symbol, side, executionPrice, qty,
                    tpEnabled, tpTicks, slEnabled, slTicks);

            ClientEventMessage event = new ClientEventMessage(
                    "TRADE_EXECUTED", symbol,
                    side == OrderSide.BUY ? "BUY_EXECUTED" : "SELL_EXECUTED"
            );
            SessionManager.sendEventToCustomer(userId, event);

            clearLiquidationIfNoPosition(userId);

            topInfoService.pushToUser(userId);   // 🔥 여기 한 곳으로 통합
        }

        return orderId;
    }

    // 지정가 체결 조건 판단
    public boolean shouldExecuteLimit(OrderSide side, double limitPrice, double bestBid, double bestAsk) {
        if (side == OrderSide.BUY) {
            return limitPrice >= bestAsk;
        } else {
            return limitPrice <= bestBid;
        }
    }

    // MIT/STOP 체결 조건 판단
    public boolean shouldExecuteStopOrMit(OrderSide side, double trigger, double prevPrice, double currentPrice) {
        return (prevPrice < trigger && currentPrice >= trigger)
                || (prevPrice > trigger && currentPrice <= trigger);
    }

    // 🔥 핵심: 매 tick마다 미체결 주문들 검사해서 조건 맞으면 체결
    public void processPendingOrders(String symbol, double prevPrice, double currentPrice,
                                     double bestBid, double bestAsk) {

        if (MarketSessionManager.isSymbolLocked(symbol)) {
            return;   // 🔥 오버나잇 처리 중이면 스킵
        }


        if (!MarketSpecCache.isTrading(symbol)) {
            return;
        }

        List<Order> pending = orderDAO.findPendingConditionalOrders(symbol);

        for (Order o : pending) {

            OrderSide side = OrderSide.valueOf(o.getSide());
            OrderType type = OrderType.valueOf(o.getOrderType());

            boolean shouldFill = false;
            double executionPrice = 0;

            switch (type) {
                case MARKET:
                    shouldFill = true;
                    executionPrice = (side == OrderSide.BUY) ? bestAsk : bestBid;
                    break;

                case LIMIT:
                    shouldFill = shouldExecuteLimit(side, o.getPrice(), bestBid, bestAsk);
                    if (shouldFill) {
                        executionPrice = (side == OrderSide.BUY) ? bestAsk : bestBid;
                    }
                    break;

                case STOP:
                case MIT:
                    shouldFill = shouldExecuteStopOrMit(side, o.getTriggerPrice(), prevPrice, currentPrice);
                    if (shouldFill) {
                        executionPrice = (side == OrderSide.BUY) ? bestAsk : bestBid;
                    }
                    break;

                default:
                    continue;
            }

            if (!shouldFill) continue;

            // 🔥 로스컷 진행 중인 유저는 이번 체결을 보류 (신규 진입 방지)
            if (riskService.isLiquidating(o.getUserId())) {
                System.out.println("[서버] 로스컷 진행 중이라 미체결 주문 체결 보류 - userId: " + o.getUserId() + ", orderId: " + o.getId());
                continue;
            }

            orderDAO.markFilled(o.getId(), executionPrice);
            positionService.applyTrade(o.getId(), o.getUserId(), o.getSymbol(), side, executionPrice, o.getQty(),
                    false, 0, false, 0);

            ClientEventMessage event = new ClientEventMessage(
                    "TRADE_EXECUTED", o.getSymbol(),
                    side == OrderSide.BUY ? "BUY_EXECUTED" : "SELL_EXECUTED"
            );
            SessionManager.sendEventToCustomer(o.getUserId(), event);

            clearLiquidationIfNoPosition(o.getUserId());

            System.out.println("[서버] 미체결 자동체결 - orderId: " + o.getId()
                    + ", userId: " + o.getUserId() + ", symbol: " + o.getSymbol()
                    + ", price: " + executionPrice);
        }
    }

/// /////////로스컷관련메소드//////////
    public void checkLiquidation(String symbol, double price) {

        // 🔥 락 체크 넣지 않음 — 로스컷은 오버나잇 처리 중에도 반드시 동작해야 함

        List<Integer> userIds = positionService.getUsersBySymbol(symbol);

        for (int userId : userIds) {

            if (!riskService.shouldForceLiquidate(userId)) {
                continue;
            }

            if (!riskService.tryStartLiquidation(userId)) {
                continue;
            }

            System.out.println("[서버] 로스컷 시작 - userId: " + userId);

            positionService.forcecloseAllPositions(userId);
        }
    }

    public void clearLiquidationIfNoPosition(int userId) {

        List<model.Position> positions = positionService.getAllPositions(userId);

        boolean hasAnyPosition = positions.stream().anyMatch(p -> p.getQty() != 0);

        if (!hasAnyPosition) {
            riskService.finishLiquidation(userId);
        }
    }
    /// /////////로스컷관련메소드//////////


    /// ///////////////////////tpsl체크관련메소드/////////////

    public void checkTpSl(String symbol, double prevPrice, double currentPrice) {

        if (MarketSessionManager.isSymbolLocked(symbol)) {
            return;  // 🔥 오버나잇 처리 중이면 스킵
        }


        for (Position pos : positionService.getActiveTpSlPositionsBySymbol(symbol))  {

            if (!symbol.equals(pos.getSymbol())) continue;
            if (pos.getQty() <= 0) continue;

            // 로스컷 진행 중인 유저는 TP/SL도 보류 (로스컷 우선)
            if (riskService.isLiquidating(pos.getUserId())) {
                continue;
            }

            // 🔴 익절
            if (pos.isTpEnabled()) {
                boolean hitTp = pos.isLong()
                        ? prevPrice < pos.getTpPrice() && currentPrice >= pos.getTpPrice()
                        : prevPrice > pos.getTpPrice() && currentPrice <= pos.getTpPrice();

                if (hitTp) {
                    double executionPrice = pos.isLong()
                            ? Store.PriceStore.getBestBid(pos.getSymbol())   // 롱 청산은 매도니까 bestBid
                            : Store.PriceStore.getBestAsk(pos.getSymbol());  // 숏 청산은 매수니까 bestAsk

                    positionService.closePosition(pos, executionPrice, "TP");   // 🔥 위치 이동
                    continue;
                }
            }

            // 🔵 손절
            if (pos.isSlEnabled()) {
                boolean hitSl = pos.isLong()
                        ? prevPrice > pos.getSlPrice() && currentPrice <= pos.getSlPrice()
                        : prevPrice < pos.getSlPrice() && currentPrice >= pos.getSlPrice();

                if (hitSl) {
                    double executionPrice = pos.isLong()
                            ? Store.PriceStore.getBestBid(pos.getSymbol())   // 롱 청산 = 매도 → bestBid
                            : Store.PriceStore.getBestAsk(pos.getSymbol());  // 숏 청산 = 매수 → bestAsk

                    positionService.closePosition(pos, executionPrice, "SL");   // 🔥 위치 이동
                }
            }
        }
    }


    /// ///////////////////////tpsl체크관련메소드/////////////

}