package service;

import model.OrderSide;
import model.OrderType;
import model.Order;
import Market.MarketSpecCache;
import Store.PriceStore;
import model.Position;
import server.ClientEventMessage;
import server.SessionManager;

import java.time.LocalDateTime;
import java.util.List;


/// ////checkLiquidation → 락 체크 추가하지 않음 (로스컷 최우선 원칙 유지)
/// processPendingOrders, checkTpSl → 락 체크 추가 (오버나잇 처리 중 데이터 경합 방지, 약간의 지연은 허용)
///
public class OrderExecutionService {

    private final OrderDAO orderDAO = new OrderDAO();
    private final PositionService positionService = new PositionService();
    private final TopInfoService topInfoService = new TopInfoService();
    private final RiskService riskService = new RiskService();

    private final OrderAuditDAO orderAuditDAO = new service.OrderAuditDAO();


    // 시장가/체결 처리 공통 로직 (기존 DemoServer의 ORDER_REQUEST 인라인 로직을 여기로 이동)
    public int executeMarket(int userId, String symbol, OrderSide side, int qty, double orderPrice,
                             boolean tpEnabled, int tpTicks, boolean slEnabled, int slTicks,
                             LocalDateTime signalTime){

        double filledPrice = (side == OrderSide.BUY)
                ? Store.PriceStore.getBestAsk(symbol)
                : Store.PriceStore.getBestBid(symbol);

        LocalDateTime fillTime = LocalDateTime.now().withNano(0);

        int orderId = orderDAO.insertFilled(userId, symbol, side.name(), orderPrice, filledPrice, qty);

        if (orderId > 0) {
            model.TradeResult result = positionService.applyTrade(orderId, userId, symbol, side, filledPrice, qty,
                    tpEnabled, tpTicks, slEnabled, slTicks);   // 🔥 반환값 받음

            double margin = new service.UserDataDAO().getAvailableMargin(userId);   // 🔥 신규 조회

            String eventType = (side == OrderSide.BUY) ? "시장가 매수" : "시장가 매도";
            logSignalAndFill(userId, symbol, side, String.valueOf(orderId), eventType,
                    orderPrice, filledPrice, qty, signalTime, fillTime, result, margin);   // 🔥 result, margin 전달

            ClientEventMessage event = new ClientEventMessage(
                    "TRADE_EXECUTED", symbol,
                    side == OrderSide.BUY ? "BUY_EXECUTED" : "SELL_EXECUTED"
            );
            SessionManager.sendEventToCustomer(userId, event);

            clearLiquidationIfNoPosition(userId);
            topInfoService.pushToUser(userId);
        }

        return orderId;
    }




    // 신호만 (지정가 등록 시점 전용)
    private void logSignalOnly(int userId, String symbol, OrderSide side, String orderId,
                               String eventType, double orderPrice, int qty, LocalDateTime signalTime) {
        String exchange = service.OrderAuditDAO.resolveExchange(symbol);
        String symbolKor = service.UserDataDAO.symbolToKor(symbol);
        String sideKor = (side == OrderSide.BUY) ? "매수" : "매도";

        model.OrderAuditLog signalLog = new model.OrderAuditLog();
        signalLog.time = signalTime;
        signalLog.eventType = eventType;
        signalLog.userId = userId;
        signalLog.exchange = exchange;
        signalLog.server = "신호";
        signalLog.orderId = orderId;
        signalLog.symbol = symbol;
        signalLog.orderPrice = orderPrice;
        signalLog.filledPrice = 0;
        signalLog.side = side.name();
        signalLog.qty = qty;

        // 🔥 DB상 실제 미체결 + 지금 막 트리거된 이 주문 자신을 합쳐서 스냅샷 구성
        String existingSnapshot = service.OrderAuditDAO.buildOpenOrderSnapshot(userId);
        String selfEntry = symbolKor + ":" + sideKor + qty + "(" + orderPrice + ")";
        signalLog.openOrderSnapshot = existingSnapshot.isEmpty()
                ? selfEntry
                : existingSnapshot + ", " + selfEntry;

        orderAuditDAO.insertLog(signalLog);
    }

    // 체결만 (지정가가 나중에 트리거될 때 전용)
    private void logFillOnly(int userId, String symbol, OrderSide side, String orderId,
                             String eventType, double filledPrice, int qty, LocalDateTime fillTime,
                             model.TradeResult result, double margin) {
        String exchange = service.OrderAuditDAO.resolveExchange(symbol);

        // 🔥 기존: 이번 체결 종목만 담던 로직 제거
        // String positionSnapshot = "";
        // if (result.positionQty > 0) { ... }

        model.OrderAuditLog fillLog = new model.OrderAuditLog();
        fillLog.time = fillTime;
        fillLog.eventType = eventType;
        fillLog.userId = userId;
        fillLog.exchange = exchange;
        fillLog.server = "체결";
        fillLog.orderId = orderId;
        fillLog.symbol = symbol;
        fillLog.orderPrice = 0;
        fillLog.filledPrice = filledPrice;
        fillLog.side = side.name();
        fillLog.qty = qty;
        fillLog.fee = result.fee;
        fillLog.pnl = result.realizedPnl;
        fillLog.margin = margin;
        fillLog.positionSnapshot = service.OrderAuditDAO.buildPositionSnapshot(userId);   // 🔥 전체 종목 통합
        orderAuditDAO.insertLog(fillLog);
    }

    // 신호+체결 (MARKET, MIT, STOP 전용 - 기존 것 그대로)
    private void logSignalAndFill(int userId, String symbol, OrderSide side, String orderId,
                                  String eventType, double orderPrice, double filledPrice, int qty,
                                  LocalDateTime signalTime, LocalDateTime fillTime,
                                  model.TradeResult result, double margin) {
        logSignalOnly(userId, symbol, side, orderId, eventType, orderPrice, qty, signalTime);
        logFillOnly(userId, symbol, side, orderId, eventType, filledPrice, qty, fillTime, result, margin);
    }

    // 🔥 소수점 4째자리에서 반올림
    private double round4(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }

    // 🔥 거부 로그 (한 줄만)
    private void logReject(int userId, String symbol, OrderSide side, String orderId,
                           String eventType, double orderPrice, int qty) {
        model.OrderAuditLog rejectLog = new model.OrderAuditLog();
        rejectLog.time = java.time.LocalDateTime.now().withNano(0);
        rejectLog.eventType = eventType;
        rejectLog.userId = userId;
        rejectLog.exchange = service.OrderAuditDAO.resolveExchange(symbol);
        rejectLog.server = "거부";
        rejectLog.orderId = orderId;
        rejectLog.symbol = symbol;
        rejectLog.orderPrice = orderPrice;
        rejectLog.filledPrice = 0;
        rejectLog.side = side.name();
        rejectLog.qty = qty;
        orderAuditDAO.insertLog(rejectLog);

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

        if (MarketSessionManager.isSymbolLocked(symbol)) return;
        if (!MarketSpecCache.isTrading(symbol)) return;

        List<Order> pending = orderDAO.findPendingConditionalOrders(symbol);

        for (Order o : pending) {

            OrderSide side = OrderSide.valueOf(o.getSide());
            OrderType type = OrderType.valueOf(o.getOrderType());

            boolean shouldFill = false;
            double orderPrice = 0;
            LocalDateTime signalTime = null;

            switch (type) {
                case MARKET:
                    shouldFill = true;
                    orderPrice = (side == OrderSide.BUY) ? bestAsk : bestBid;
                    signalTime = LocalDateTime.now().withNano(0);
                    break;

                case LIMIT:
                    shouldFill = shouldExecuteLimit(side, o.getOrderPrice(), bestBid, bestAsk);
                    if (shouldFill) {
                        orderPrice = o.getOrderPrice();
                        signalTime = LocalDateTime.now().withNano(0);
                    }
                    break;

                case STOP:
                case MIT:
                    shouldFill = shouldExecuteStopOrMit(side, o.getTriggerPrice(), prevPrice, currentPrice);
                    if (shouldFill) {
                        orderPrice = (side == OrderSide.BUY) ? bestAsk : bestBid;
                        signalTime = LocalDateTime.now().withNano(0);
                    }
                    break;

                default:
                    continue;
            }

            if (!shouldFill) continue;

            if (riskService.isLiquidating(o.getUserId())) {
                System.out.println("[서버] 로스컷 진행 중이라 미체결 주문 체결 보류 - userId: " + o.getUserId() + ", orderId: " + o.getId());
                logReject(o.getUserId(), o.getSymbol(), side, String.valueOf(o.getId()),
                        "로스컷 진행중 체결 보류", orderPrice, o.getQty());
                continue;
            }

            double filledPrice;
            if (type == OrderType.LIMIT) {
                filledPrice = orderPrice;
            } else {
                filledPrice = (side == OrderSide.BUY)
                        ? Store.PriceStore.getBestAsk(o.getSymbol())
                        : Store.PriceStore.getBestBid(o.getSymbol());
            }

            LocalDateTime fillTime = LocalDateTime.now().withNano(0);

            orderDAO.markFilled(o.getId(), orderPrice, filledPrice);

            model.TradeResult result = positionService.applyTrade(o.getId(), o.getUserId(), o.getSymbol(), side, filledPrice, o.getQty(),
                    false, 0, false, 0);   // 🔥 반환값 받음

            double margin = new service.UserDataDAO().getAvailableMargin(o.getUserId());   // 🔥 조회

            String eventType = switch (type) {
                case LIMIT -> (side == OrderSide.BUY) ? "지정가 매수" : "지정가 매도";
                case MIT -> (side == OrderSide.BUY) ? "MIT 매수" : "MIT 매도";
                case STOP -> (side == OrderSide.BUY) ? "STOP 매수" : "STOP 매도";
                default -> (side == OrderSide.BUY) ? "시장가 매수" : "시장가 매도";
            };

            // 🔥 LIMIT은 이미 등록 시점에 신호를 남겼으므로 체결만, 나머지는 신호+체결 둘 다
            if (type == OrderType.LIMIT) {
                logFillOnly(o.getUserId(), o.getSymbol(), side, String.valueOf(o.getId()),
                        eventType, filledPrice, o.getQty(), fillTime, result, margin);
            } else {
                logSignalAndFill(o.getUserId(), o.getSymbol(), side, String.valueOf(o.getId()),
                        eventType, orderPrice, filledPrice, o.getQty(), signalTime, fillTime, result, margin);
            }

            ClientEventMessage event = new ClientEventMessage(
                    "TRADE_EXECUTED", o.getSymbol(),
                    side == OrderSide.BUY ? "BUY_EXECUTED" : "SELL_EXECUTED"
            );
            SessionManager.sendEventToCustomer(o.getUserId(), event);

            clearLiquidationIfNoPosition(o.getUserId());

            System.out.println("[서버] 미체결 자동체결 - orderId: " + o.getId()
                    + ", userId: " + o.getUserId() + ", symbol: " + o.getSymbol()
                    + ", orderPrice: " + orderPrice + ", filledPrice: " + filledPrice);
        }
    }



    // 🔥 호가 갱신 시 호출 - LIMIT만 체크. prevPrice 필요 없음
    public void checkLimitOrders(String symbol, double bestBid, double bestAsk) {

        if (MarketSessionManager.isSymbolLocked(symbol)) return;
        if (!MarketSpecCache.isTrading(symbol)) return;

        List<Order> pending = orderDAO.findPendingConditionalOrders(symbol);

        for (Order o : pending) {

            if (!"LIMIT".equals(o.getOrderType())) continue;

            OrderSide side = OrderSide.valueOf(o.getSide());

            boolean shouldFill = shouldExecuteLimit(side, o.getOrderPrice(), bestBid, bestAsk);
            if (!shouldFill) continue;

            if (riskService.isLiquidating(o.getUserId())) {
                System.out.println("[서버] 로스컷 진행 중이라 미체결 주문 체결 보류 - userId: " + o.getUserId() + ", orderId: " + o.getId());
                logReject(o.getUserId(), o.getSymbol(), side, String.valueOf(o.getId()),
                        "로스컷 진행중 체결 보류", o.getOrderPrice(), o.getQty());
                continue;
            }

            double filledPrice = o.getOrderPrice();
            LocalDateTime fillTime = LocalDateTime.now().withNano(0);

            orderDAO.markFilled(o.getId(), o.getOrderPrice(), filledPrice);

            model.TradeResult result = positionService.applyTrade(o.getId(), o.getUserId(), o.getSymbol(), side, filledPrice, o.getQty(),
                    false, 0, false, 0);

            double margin = new service.UserDataDAO().getAvailableMargin(o.getUserId());

            String eventType = (side == OrderSide.BUY) ? "지정가 매수" : "지정가 매도";

            // LIMIT은 등록 시점에 이미 신호 로그를 남겼으므로 체결만
            logFillOnly(o.getUserId(), o.getSymbol(), side, String.valueOf(o.getId()),
                    eventType, filledPrice, o.getQty(), fillTime, result, margin);

            ClientEventMessage event = new ClientEventMessage(
                    "TRADE_EXECUTED", o.getSymbol(),
                    side == OrderSide.BUY ? "BUY_EXECUTED" : "SELL_EXECUTED"
            );
            SessionManager.sendEventToCustomer(o.getUserId(), event);

            clearLiquidationIfNoPosition(o.getUserId());

            System.out.println("[서버] 지정가 자동체결 - orderId: " + o.getId()
                    + ", userId: " + o.getUserId() + ", symbol: " + o.getSymbol()
                    + ", filledPrice: " + filledPrice);
        }
    }



    // 🔥 체결(tape) 갱신 시에만 호출 - STOP/MIT/MARKET 체크. prevPrice 필요
    public void checkStopAndMitOrders(String symbol, double prevPrice, double currentPrice,
                                      double bestBid, double bestAsk) {

        if (MarketSessionManager.isSymbolLocked(symbol)) return;
        if (!MarketSpecCache.isTrading(symbol)) return;

        List<Order> pending = orderDAO.findPendingConditionalOrders(symbol);

        for (Order o : pending) {

            String typeStr = o.getOrderType();
            if (!"MARKET".equals(typeStr) && !"STOP".equals(typeStr) && !"MIT".equals(typeStr)) continue;

            OrderSide side = OrderSide.valueOf(o.getSide());
            OrderType type = OrderType.valueOf(typeStr);

            boolean shouldFill = false;
            double orderPrice = 0;
            LocalDateTime signalTime = null;

            if (type == OrderType.MARKET) {
                shouldFill = true;
                orderPrice = (side == OrderSide.BUY) ? bestAsk : bestBid;
                signalTime = LocalDateTime.now().withNano(0);
            } else { // STOP, MIT
                shouldFill = shouldExecuteStopOrMit(side, o.getTriggerPrice(), prevPrice, currentPrice);
                if (shouldFill) {
                    orderPrice = (side == OrderSide.BUY) ? bestAsk : bestBid;
                    signalTime = LocalDateTime.now().withNano(0);   // 🔥 주석 풀어서 반영
                }
            }

            if (!shouldFill) continue;

            if (riskService.isLiquidating(o.getUserId())) {
                System.out.println("[서버] 로스컷 진행 중이라 미체결 주문 체결 보류 - userId: " + o.getUserId() + ", orderId: " + o.getId());
                logReject(o.getUserId(), o.getSymbol(), side, String.valueOf(o.getId()),
                        "로스컷 진행중 체결 보류", orderPrice, o.getQty());
                continue;
            }

            double filledPrice = (side == OrderSide.BUY)
                    ? Store.PriceStore.getBestAsk(o.getSymbol())
                    : Store.PriceStore.getBestBid(o.getSymbol());

            LocalDateTime fillTime = LocalDateTime.now().withNano(0);

            orderDAO.markFilled(o.getId(), orderPrice, filledPrice);

            model.TradeResult result = positionService.applyTrade(o.getId(), o.getUserId(), o.getSymbol(), side, filledPrice, o.getQty(),
                    false, 0, false, 0);

            double margin = new service.UserDataDAO().getAvailableMargin(o.getUserId());

            String eventType = switch (type) {
                case MIT -> (side == OrderSide.BUY) ? "MIT 매수" : "MIT 매도";
                case STOP -> (side == OrderSide.BUY) ? "STOP 매수" : "STOP 매도";
                default -> (side == OrderSide.BUY) ? "시장가 매수" : "시장가 매도";
            };

            logSignalAndFill(o.getUserId(), o.getSymbol(), side, String.valueOf(o.getId()),
                    eventType, orderPrice, filledPrice, o.getQty(), signalTime, fillTime, result, margin);

            ClientEventMessage event = new ClientEventMessage(
                    "TRADE_EXECUTED", o.getSymbol(),
                    side == OrderSide.BUY ? "BUY_EXECUTED" : "SELL_EXECUTED"
            );
            SessionManager.sendEventToCustomer(o.getUserId(), event);

            clearLiquidationIfNoPosition(o.getUserId());

            System.out.println("[서버] MIT/STOP/시장가 자동체결 - orderId: " + o.getId()
                    + ", userId: " + o.getUserId() + ", symbol: " + o.getSymbol()
                    + ", orderPrice: " + orderPrice + ", filledPrice: " + filledPrice);
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

            // 🔥 로스컷 발생 이벤트 로그 (계좌 단위, 종목별 청산 로그와 별개)
            model.OrderAuditLog liquidationStartLog = new model.OrderAuditLog();
            liquidationStartLog.time = java.time.LocalDateTime.now().withNano(0);
            liquidationStartLog.eventType = "로스컷 발생";
            liquidationStartLog.userId = userId;
            liquidationStartLog.server = "신호";
            new service.OrderAuditDAO().insertLog(liquidationStartLog);


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