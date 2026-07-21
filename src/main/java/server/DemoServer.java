package server;

import Market.MarketContext;
import com.google.gson.Gson;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import model.*;
import service.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class DemoServer {

    private static final Gson gson = new Gson();
    private static final UserDAO userDAO = new UserDAO();
    private static final BlacklistDAO blacklistDAO = new BlacklistDAO();
    private static final service.OrderDAO orderDAO = new service.OrderDAO();
    private static final service.PositionService positionService = new service.PositionService();
    private static final service.RiskService riskService = new service.RiskService();
    private static final service.OrderExecutionService orderExecutionService = new service.OrderExecutionService();
    private static final RealtimePnlService realtimePnlService = new RealtimePnlService();
    private static final service.UserService userService = new service.UserService();
    private static final service.TradeHistoryDAO tradeHistoryDAO = new service.TradeHistoryDAO();
    private static final service.AdminDepositService depositService = new AdminDepositService();
    private static final service.TopInfoService topInfoService = new TopInfoService();
    private static final service.CustomerDepositService customerDepositService = new CustomerDepositService();
    private static final service.DepositHistoryService depositHistoryService = new DepositHistoryService();
    private static final service.ChatDAO chatDAO = new service.ChatDAO();
    private static final service.AdminUserListService adminUserListService = new AdminUserListService();
    private static final service.AdminUserBasicService adminUserBasicService = new AdminUserBasicService();
    private static final service.AdminUserAccountService adminUserAccountService = new AdminUserAccountService(); // 필드
    private static final service.AdminUserDepositService adminUserDepositService = new AdminUserDepositService();
    private static final service.AdminUserFeeService adminUserFeeService = new AdminUserFeeService();
    private static final service.AdminUserQtyLimitService adminUserQtyLimitService = new AdminUserQtyLimitService();
    private static final service.AdminUserFullService adminUserFullService = new AdminUserFullService();
    private static final service.AdminUserRegisterService adminUserRegisterService = new AdminUserRegisterService();

    private static final service.SymbolFeeOverrideDAO symbolFeeOverrideDAO = new service.SymbolFeeOverrideDAO();
    private static final service.CompanyAccountDAO companyAccountDAO = new service.CompanyAccountDAO();
    private static final service.MarketSpecDAO marketSpecDAO = new service.MarketSpecDAO();
    private static final service.SystemQtyLimitDAO systemQtyLimitDAO = new service.SystemQtyLimitDAO();

    public static void main(String[] args) throws InterruptedException {




        // 🔥 옵션 체인이 아직 없으면 생성 (있으면 조용히 스킵)
        new service.MarketSpecDAO().generateKospiOptionChain();

//서버 실행할 때 마켓스피씨캐시 로드
        Market.MarketSpecCache.load();   // 추가


// 🔥 새 코드 - 모든 종목 등록
        Map<String, MarketContext> marketContexts = new java.util.concurrent.ConcurrentHashMap<>();
        Map<String, Market.MarketSimulator> marketSimulators = new java.util.concurrent.ConcurrentHashMap<>();
        Map<String, Double> prevPrices = new java.util.concurrent.ConcurrentHashMap<>();

        for (Market.MarketSpec spec : Market.MarketSpecCache.getAll()) {
            String symbol = spec.getSymbol();
            Market.MarketContext ctx = new Market.MarketContext(spec);
            Market.MarketSimulator sim = new Market.MarketSimulator(ctx);

            marketContexts.put(symbol, ctx);
            marketSimulators.put(symbol, sim);
            prevPrices.put(symbol, ctx.getCurrentPrice());
        }




        java.util.concurrent.Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {

            for (String symbol : marketSimulators.keySet()) {

                double prevPrice = prevPrices.get(symbol);

                marketSimulators.get(symbol).tick();

                double currentPrice = Store.PriceStore.getLast(symbol);
                double bestBid = Store.PriceStore.getBestBid(symbol);
                double bestAsk = Store.PriceStore.getBestAsk(symbol);

                orderExecutionService.checkLiquidation(symbol, currentPrice);
                orderExecutionService.checkTpSl(symbol, prevPrice, currentPrice);
                orderExecutionService.processPendingOrders(symbol, prevPrice, currentPrice, bestBid, bestAsk);

                Market.OrderBookSnapshot snapshot = marketContexts.get(symbol).getSnapshot();
                if (snapshot != null) {
                    PriceUpdateMessage update = new PriceUpdateMessage(
                            symbol, currentPrice, bestBid, bestAsk, snapshot.getAsks(), snapshot.getBids()
                    );
                    SessionManager.broadcastToSubscribers(symbol, update);
                }

                prevPrices.put(symbol, currentPrice);
            }

        }, 0, 300, java.util.concurrent.TimeUnit.MILLISECONDS);



/// ////////////////////////실시간종합현황 스케줄러
        java.util.concurrent.Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                List<model.RealtimePnlRow> rows = realtimePnlService.compute();
                RealtimePnlResponse response = new RealtimePnlResponse(rows);
                SessionManager.broadcastToAdmins(response);

                System.out.println("[서버] 실시간손익 갱신 push 완료 - " + rows.size() + "건, " + java.time.LocalTime.now());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 10, java.util.concurrent.TimeUnit.SECONDS);
        /// ////////////////////

/// ////////////////유저들 탑인포패널 담보금같은 정보 업데이트스케줄러.////
        java.util.concurrent.Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                int count = 0;
                for (Integer userId : SessionManager.getConnectedCustomerIds()) {
                    topInfoService.pushToUser(userId);
                    count++;
                }
                System.out.println("[서버] TopInfo 정기 push 완료 - " + count + "명, " + java.time.LocalTime.now());   // 🔥 로그 추가
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 20, java.util.concurrent.TimeUnit.SECONDS);
/// /////////////////////////////////////////////


        int port = 9000;

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();

                            pipeline.addLast(new DelimiterBasedFrameDecoder(1024* 1024, Delimiters.lineDelimiter()));
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new StringEncoder());

                            pipeline.addLast(new SimpleChannelInboundHandler<String>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, String msg) {
                                    System.out.println("[서버] 원본 수신: " + msg);

                                    com.google.gson.JsonObject obj = gson.fromJson(msg, com.google.gson.JsonObject.class);
                                    String type = obj.get("type").getAsString();

                                    if ("LOGIN_REQUEST".equals(type)) {
                                        LoginRequest request = gson.fromJson(msg, LoginRequest.class);
                                        System.out.println("[서버] 로그인 시도 - 아이디: " + request.getUsername());

                                        String ip = ctx.channel().remoteAddress().toString().replace("/", "").split(":")[0];
                                        String mac = request.getMac();

                                        LoginResponse response;

                                        // 1. 블랙리스트 체크 (DB 조회, 계정 인증 전에 먼저)
                                        if (blacklistDAO.isBlacklisted(ip, mac)) {
                                            response = new LoginResponse(false, "차단된 IP 또는 기기입니다.");
                                            ctx.writeAndFlush(gson.toJson(response) + "\n");
                                            System.out.println("[서버] 블랙리스트 차단 - ip: " + ip + ", mac: " + mac);
                                            return;
                                        }

                                        // 2. 계정 인증
                                        User user = userDAO.login(request.getUsername(), request.getPassword());

                                        if (user != null) {
                                            String accountStatus = userDAO.getAccountStatus(user.getId());
                                            if ("블랙".equals(accountStatus)) {
                                                response = new LoginResponse(false, "사용할 수 없는 계정입니다.");
                                                ctx.writeAndFlush(gson.toJson(response) + "\n");
                                                System.out.println("[서버] 계정상태 차단 - userId: " + user.getId());
                                                return;
                                            }
                                            response = new LoginResponse(true, "로그인 성공", user.getId(), user.getUsername(), user.getName(), user.getRole(), user.getBalance());
                                            SessionManager.register(user.getId(), user.getUsername(), user.getName(), mac, user.getRole(), ctx);
                                            System.out.println("[서버] 로그인 성공 - userId: " + user.getId());   // 성공 로그도 추가해두면 좋음

                                        } else {
                                            response = new LoginResponse(false, "아이디 또는 비밀번호가 틀렸습니다");
                                            System.out.println("[서버] 로그인 실패 - 아이디/비밀번호 불일치: " + request.getUsername());   // 추가
                                        }

                                        ctx.writeAndFlush(gson.toJson(response) + "\n");

                                    } else if ("SESSION_LIST_REQUEST".equals(type)) {
                                        SessionManager.sendSessionListTo(ctx);
                                    } else if ("BLACKLIST_ADD".equals(type)) {          // ← 여기에 새로 추가
                                        BlacklistAddRequest request = gson.fromJson(msg, BlacklistAddRequest.class);
                                        blacklistDAO.addBlacklist(request.getTargetType(), request.getValue(), request.getReason());
                                        System.out.println("[서버] 블랙리스트 등록 - " + request.getTargetType() + ": " + request.getValue());
                                    } else if ("FORCE_LOGOUT_REQUEST".equals(type)) {          // ← 여기 새로 추가
                                        ForceLogoutRequest request = gson.fromJson(msg, ForceLogoutRequest.class);
                                        int targetUserId = request.getTargetUserId();

                                        ChannelHandlerContext targetCtx = SessionManager.getCustomer(targetUserId);
                                        if (targetCtx != null) {
                                            ForceLogoutMessage forceLogout = new ForceLogoutMessage("관리자에 의해 강제 로그아웃되었습니다.");
                                            targetCtx.writeAndFlush(gson.toJson(forceLogout) + "\n");
                                            System.out.println("[서버] userId=" + targetUserId + " 강제 로그아웃 명령 전송");
                                        } else {
                                            System.out.println("[서버] userId=" + targetUserId + " 접속중이지 않음 (강제로그아웃 요청 무시)");
                                        }
                                    } else if ("ORDER_REQUEST".equals(type)) {
                                        OrderRequest request = gson.fromJson(msg, OrderRequest.class);
                                        System.out.println("[서버] 주문 요청 - userId: " + request.getUserId()
                                                + ", symbol: " + request.getSymbol()
                                                + ", side: " + request.getSide()
                                                + ", price: " + request.getPrice()
                                                + ", qty: " + request.getQty());


/// //////////////////////////////////////////////////리스크 체크 추가///////////////////
                                        model.OrderSide side = model.OrderSide.valueOf(request.getSide());

                                        boolean allowed = riskService.canPlaceOrder(
                                                request.getUserId(), request.getSymbol(), side, request.getQty()
                                        );

                                        if (!allowed) {
                                            OrderResponse rejectResponse = new OrderResponse(false, "주문 가능 수량을 초과했습니다.", -1);
                                            ctx.writeAndFlush(gson.toJson(rejectResponse) + "\n");
                                            System.out.println("[서버] 리스크 체크 거부 - userId: " + request.getUserId());
                                            return;
                                        }
                                        /// /////////////////////////////////////
                                        int orderId = orderExecutionService.executeMarket(
                                                request.getUserId(), request.getSymbol(), side, request.getQty(), request.getPrice(),
                                                request.isTpEnabled(), request.getTpTicks(), request.isSlEnabled(), request.getSlTicks()
                                        );
                                        OrderResponse response;
                                        if (orderId > 0) {
                                            response = new OrderResponse(true, "주문 체결 완료", orderId);
                                            System.out.println("[서버] 주문 체결 성공 - orderId: " + orderId);
                                        } else {
                                            response = new OrderResponse(false, "주문 처리 실패", -1);
                                        }

                                        ctx.writeAndFlush(gson.toJson(response) + "\n");

                                    } else if ("ORDER_PENDING_REQUEST".equals(type)) {
                                        OrderPendingRequest request = gson.fromJson(msg, OrderPendingRequest.class);

                                        model.OrderSide side = model.OrderSide.valueOf(request.getSide());

                                        boolean allowed = riskService.canPlaceOrder(
                                                request.getUserId(), request.getSymbol(), side, request.getQty()
                                        );

                                        OrderResponse response;

                                        if (!allowed) {
                                            response = new OrderResponse(false, "주문 가능 수량을 초과했습니다.", -1);
                                            System.out.println("[서버] 대기주문 리스크 거부 - userId: " + request.getUserId());
                                        } else {
                                            int orderId = orderDAO.insertPending(
                                                    request.getUserId(),
                                                    request.getSymbol(),
                                                    request.getSide(),
                                                    request.getOrderType(),
                                                    request.getPrice(),
                                                    request.getTriggerPrice(),
                                                    request.getQty()
                                            );

                                            if (orderId > 0) {
                                                // 🔥 여기에 미체결 등록 성공 시 사운드내라고 이벤트보내는거 추가
                                                ClientEventMessage pendingEvent = new ClientEventMessage(
                                                        "PENDING_ORDER_CHANGED", request.getSymbol(),
                                                        "BUY".equals(request.getSide()) ? "BUY_RESERVED" : "SELL_RESERVED"
                                                );
                                                SessionManager.sendEventToCustomer(request.getUserId(), pendingEvent);

                                                response = new OrderResponse(true, "미체결 주문 등록 완료", orderId);
                                                System.out.println("[서버] 미체결 주문 등록 성공 - orderId: " + orderId);
                                            } else {
                                                response = new OrderResponse(false, "주문 등록 실패", -1);
                                            }
                                        }

                                        ctx.writeAndFlush(gson.toJson(response) + "\n");
                                    } else if ("ORDER_CANCEL_REQUEST".equals(type)) {
                                        OrderCancelRequest request = gson.fromJson(msg, OrderCancelRequest.class);

                                        String symbol = orderDAO.getSymbolByOrderId(request.getOrderId());  // 🔥 취소 전에 먼저 조회

                                        orderDAO.cancelPendingById(request.getOrderId());

                                        // 🔥 여기에 취소 성공 시 추가 (symbol 정보가 없으니 null로)
                                        ClientEventMessage cancelEvent = new ClientEventMessage("PENDING_ORDER_CHANGED",symbol, "ORDER_CANCELLED");
                                        SessionManager.sendEventToCustomer(request.getUserId(), cancelEvent);

                                        OrderResponse response = new OrderResponse(true, "취소 완료", request.getOrderId());
                                        ctx.writeAndFlush(gson.toJson(response) + "\n");
                                        System.out.println("[서버] 미체결 취소 완료 - orderId: " + request.getOrderId());
                                    } else if ("ORDER_BULK_CANCEL_REQUEST".equals(type)) {
                                        OrderBulkCancelRequest request = gson.fromJson(msg, OrderBulkCancelRequest.class);

                                        switch (request.getMode()) {
                                            case "BY_TYPE_SIDE" -> {
                                                orderDAO.cancelByTypeAndSide(
                                                        request.getUserId(), request.getSymbol(), request.getOrderType(), request.getSide()
                                                );
                                                //리스너등록
                                                ClientEventMessage event = new ClientEventMessage("PENDING_ORDER_CHANGED", request.getSymbol(), "ORDER_CANCELLED");
                                                SessionManager.sendEventToCustomer(request.getUserId(), event);

                                                System.out.println("[서버] 대량취소 완료 - userId: " + request.getUserId()
                                                        + ", symbol: " + request.getSymbol()
                                                        + ", type: " + request.getOrderType()
                                                        + ", side: " + request.getSide());
                                            }
                                            case "BY_SYMBOL" -> {
                                                orderDAO.cancelBySymbol(request.getUserId(), request.getSymbol());
                                                //리스너등록
                                                ClientEventMessage event = new ClientEventMessage("PENDING_ORDER_CHANGED", request.getSymbol(), "ORDER_CANCELLED");
                                                SessionManager.sendEventToCustomer(request.getUserId(), event);

                                                System.out.println("[서버] 현종목취소 완료 - userId: " + request.getUserId()
                                                        + ", symbol: " + request.getSymbol());
                                            }
                                            case "ALL" -> {
                                                orderDAO.cancelAll(request.getUserId());
                                                //리스너등록
                                                ClientEventMessage event = new ClientEventMessage("PENDING_ORDER_CHANGED", null, "ORDER_CANCELLED");
                                                SessionManager.sendEventToCustomer(request.getUserId(), event);

                                                System.out.println("[서버] 전종목취소 완료 - userId: " + request.getUserId());
                                            }
                                        }

                                        OrderResponse response = new OrderResponse(true, "취소 완료", -1);
                                        ctx.writeAndFlush(gson.toJson(response) + "\n");
                                    } else if ("SUBSCRIBE_REQUEST".equals(type)) {
                                        SubscribeRequest request = gson.fromJson(msg, SubscribeRequest.class);

                                        if (request.getPreviousSymbol() != null) {
                                            SessionManager.unsubscribe(request.getUserId(), request.getPreviousSymbol());
                                        }

                                        // 🔥 symbol이 null이면(순수 구독취소 목적) 새로 구독하지 않음
                                        if (request.getSymbol() != null) {
                                            SessionManager.subscribe(request.getUserId(), request.getSymbol());
                                        }


                                        System.out.println("[서버] 구독 변경 - userId: " + request.getUserId()
                                                + ", " + request.getPreviousSymbol() + " → " + request.getSymbol());
                                    } else if ("TPSL_UPDATE_REQUEST".equals(type)) {
                                        TpSlUpdateRequest request = gson.fromJson(msg, TpSlUpdateRequest.class);

                                        boolean success = positionService.updateTpSl(
                                                request.getUserId(), request.getSymbol(),
                                                request.isTpEnabled(), request.getTpTicks(),
                                                request.isSlEnabled(), request.getSlTicks()
                                        );

                                        if (success) {
                                            ClientEventMessage event = new ClientEventMessage("TRADE_EXECUTED", request.getSymbol(), null);
                                            SessionManager.sendEventToCustomer(request.getUserId(), event);
                                            System.out.println("[서버] TP/SL 즉시 갱신 완료 - userId: " + request.getUserId());
                                        }
                                    }

                                 else if ("AVAILABLE_QTY_REQUEST".equals(type)) {
                                    AvailableQtyRequest request = gson.fromJson(msg, AvailableQtyRequest.class);

                                    int pendingBuyQty = orderDAO.getPendingQtyBySide(request.getUserId(), request.getSymbol(), "BUY");
                                    int pendingSellQty = orderDAO.getPendingQtyBySide(request.getUserId(), request.getSymbol(), "SELL");

                                    int maxBuyQty = riskService.calcMaxBuyQty(request.getUserId(), request.getSymbol(), pendingBuyQty);
                                    int maxSellQty = riskService.calcMaxSellQty(request.getUserId(), request.getSymbol(), pendingSellQty);

                                    AvailableQtyResponse response = new AvailableQtyResponse(maxBuyQty, maxSellQty);
                                    ctx.writeAndFlush(gson.toJson(response) + "\n");
                                    } else if ("DAILY_PROFIT_REQUEST".equals(type)) {
                                        DailyProfitRequest request = gson.fromJson(msg, DailyProfitRequest.class);

                                        List<String[]> symbols = Market.MarketSpecCache.getAll().stream()
                                                .map(s -> new String[]{s.getSymbol(), s.getDisplayName()})
                                                .collect(java.util.stream.Collectors.toList());

                                        List<DailyProfitRow> rows = orderDAO.loadDailyProfit(
                                                request.getUserId(),
                                                new java.sql.Timestamp(request.getStartMillis()),
                                                new java.sql.Timestamp(request.getEndMillis()),
                                                symbols
                                        );

                                        DailyProfitResponse response = new DailyProfitResponse(true, "조회 완료", rows);
                                        ctx.writeAndFlush(gson.toJson(response) + "\n");
                                    } else if ("CUSTOMER_PROFIT_REQUEST".equals(type)) {
                                        CustomerProfitRequest request = gson.fromJson(msg, CustomerProfitRequest.class);

                                        List<String[]> symbols = Market.MarketSpecCache.getAll().stream()
                                                .map(s -> new String[]{s.getSymbol(), s.getDisplayName()})
                                                .collect(java.util.stream.Collectors.toList());

                                        List<model.CustomerProfitRow> rows = orderDAO.loadCustomerProfitSummary(
                                                request.getKeyword(),
                                                new java.sql.Timestamp(request.getStartMillis()),
                                                new java.sql.Timestamp(request.getEndMillis()),
                                                symbols
                                        );

                                        CustomerProfitResponse response = new CustomerProfitResponse(true, "조회 완료", rows);
                                        ctx.writeAndFlush(gson.toJson(response) + "\n");
                                    } else if ("USER_SEARCH_REQUEST".equals(type)) {
                                        UserSearchRequest request = gson.fromJson(msg, UserSearchRequest.class);

                                        List<model.UserSearchRow> rows = userDAO.searchUsers(
                                                request.getKeyword(), request.getSearchType()
                                        );

                                        UserSearchResponse response = new UserSearchResponse(true, "조회 완료", rows);
                                        ctx.writeAndFlush(gson.toJson(response) + "\n");
                                    } else if ("TRADE_HISTORY_REQUEST".equals(type)) {


                                        TradeHistoryRequest request =
                                                gson.fromJson(msg, TradeHistoryRequest.class);



                                        List<model.TradeHistoryRow> rows =
                                                new service.TradeHistoryDAO()
                                                        .loadTradeHistory(
                                                                request.getUserId(),
                                                                new java.sql.Timestamp(request.getStartMillis()),
                                                                new java.sql.Timestamp(request.getEndMillis()),
                                                                request.getSymbol()
                                                        );



                                        TradeHistoryResponse response =
                                                new TradeHistoryResponse(
                                                        true,
                                                        "조회 완료",
                                                        rows
                                                );


                                        ctx.writeAndFlush(
                                                gson.toJson(response)
                                                        + "\n"
                                        );


                                    } else if ("PARTNER_PROFIT_REQUEST".equals(type)) {
                                        PartnerProfitRequest request = gson.fromJson(msg, PartnerProfitRequest.class);

                                        Timestamp start = new Timestamp(request.getStartMillis());   // 🔥 이름 변경
                                        Timestamp end = new Timestamp(request.getEndMillis());       // 🔥 이름 변경

                                        List<model.PartnerProfitRow> rows = orderDAO.loadPartnerProfitSummary(start, end);

                                        double td=0, tad=0, tw=0, taw=0, tf=0, tp=0, tfp=0;
                                        for (model.PartnerProfitRow r : rows) {
                                            td += r.getDeposit(); tad += r.getAdminDeposit();
                                            tw += r.getWithdraw(); taw += r.getAdminWithdraw();
                                            tf += r.getFee(); tp += r.getPnl(); tfp += r.getFinalProfit();
                                        }
                                        model.PartnerProfitRow total = new model.PartnerProfitRow("TOTAL", "", "", td, tad, tw, taw, tf, tp, tfp);

                                        PartnerProfitResponse response = new PartnerProfitResponse(rows, total);
                                        ctx.writeAndFlush(gson.toJson(response) + "\n");

                                    } else if ("PARTNER_CHILDREN_PROFIT_REQUEST".equals(type)) {
                                        PartnerChildrenProfitRequest request = gson.fromJson(msg, PartnerChildrenProfitRequest.class);

                                        Timestamp start = new Timestamp(request.getStartMillis());   // 🔥 이름 변경
                                        Timestamp end = new Timestamp(request.getEndMillis());       // 🔥 이름 변경

                                        List<model.PartnerProfitRow> rows = orderDAO.loadPartnerChildrenProfitSummary(
                                                request.getPartnerUsername(), start, end
                                        );

                                        PartnerChildrenProfitResponse response = new PartnerChildrenProfitResponse(rows);
                                        ctx.writeAndFlush(gson.toJson(response) + "\n");
                                    } else if ("REALTIME_PNL_REQUEST".equals(type)) {
                                        // 🔥 netty 워커 스레드를 막지 않도록 별도 스레드에서 처리
                                        new Thread(() -> {
                                            List<model.RealtimePnlRow> rows = realtimePnlService.compute();
                                            RealtimePnlResponse response = new RealtimePnlResponse(rows);
                                            SessionManager.broadcastToAdmins(response);   // 요청한 관리자 포함 전원에게
                                        }).start();
                                    }else if ("DEPOSIT_MONITORING_REQUEST".equals(type)) {
                                        new Thread(() -> {
                                            SessionManager.broadcastToAdmins(buildDepositMonitoringResponse());
                                        }).start();
                                    }

                                    else if ("DEPOSIT_APPROVE_REQUEST".equals(type)) {
                                        new Thread(() -> {
                                            DepositApproveRequest req = gson.fromJson(msg, DepositApproveRequest.class);
                                            List<AdminDepositService.ApprovedInfo> affected  = depositService.approveRequests(req.requestIds, req.adminId);


                                            for (AdminDepositService.ApprovedInfo info : affected) {
                                                /// /////////////고객 탑인포패널 리프레쉬하라고 푸쉬/////////////
                                                topInfoService.pushToUser(info.userId);
                                                customerDepositService.pushBalanceToUser(info.userId);
                                                //////////////////////////////////////service
                                                // 🔥 승인된 고객에게 사운드 알림
                                                String soundType = "DEPOSIT".equals(info.type) ? "DEPOSIT_APPROVED" : "WITHDRAW_APPROVED";
                                                SessionManager.sendToCustomer(info.userId, new ClientEventMessage("PLAY_SOUND", null, soundType));
                                            }

                                            SessionManager.broadcastToAdmins(buildDepositMonitoringResponse());
                                        }).start();
                                    }

                                    else if ("DEPOSIT_REJECT_REQUEST".equals(type)) {
                                        new Thread(() -> {
                                            DepositRejectRequest req = gson.fromJson(msg, DepositRejectRequest.class);
                                            List<AdminDepositService.ApprovedInfo> affected = depositService.rejectRequests(req.requestIds, req.adminId);


                                            for (AdminDepositService.ApprovedInfo info : affected) {
                                                // 🔥 출금 거절로 환불된 고객들에게 담보금 push (입금 거절은 애초에 잔액 변화 없어서 push해도 값 그대로지만, 일괄 처리해도 무방)
                                                topInfoService.pushToUser(info.userId);
                                                customerDepositService.pushBalanceToUser(info.userId);

                                                // 🔥 거절된 고객에게 사운드 알림
                                                String soundType = "DEPOSIT".equals(info.type) ? "DEPOSIT_REJECTED" : "WITHDRAW_REJECTED";
                                                SessionManager.sendToCustomer(info.userId, new ClientEventMessage("PLAY_SOUND", null, soundType));
                                            }

                                            SessionManager.broadcastToAdmins(buildDepositMonitoringResponse());
                                        }).start();
                                    }

                                    else if ("DEPOSIT_REQUEST_SUBMIT".equals(type)) {
                                        new Thread(() -> {
                                            DepositRequestSubmit req = gson.fromJson(msg, DepositRequestSubmit.class);
                                            String failReason = customerDepositService.createRequest(
                                                    req.userId, req.requestType, req.amount, req.requestNote
                                            );

                                            boolean success = (failReason == null);
                                            String message = success ? "신청 완료!" : failReason;

                                            SessionManager.sendToCustomer(req.userId, new DepositRequestResult(success, message));

                                            if (success) {
                                                if ("WITHDRAW".equals(req.requestType)) {
                                                    topInfoService.pushToUser(req.userId);
                                                    customerDepositService.pushBalanceToUser(req.userId);
                                                }

                                                SessionManager.broadcastToAdmins(buildDepositMonitoringResponse());

                                                // 🔥 관리자 전원에게 신청 사운드 알림
                                                String soundType = "DEPOSIT".equals(req.requestType) ? "DEPOSIT_REQUEST" : "WITHDRAW_REQUEST";
                                                SessionManager.broadcastToAdmins(new ClientEventMessage("PLAY_SOUND", null, soundType));
                                            }
                                        }).start();
                                    }else if ("DW_BALANCE_REQUEST".equals(type)) {
                                        new Thread(() -> {
                                            DwBalanceRequest req = gson.fromJson(msg, DwBalanceRequest.class);
                                            customerDepositService.pushBalanceToUser(req.userId);   // 이미 만든 메서드 그대로 재사용
                                        }).start();
                                    }else if ("ADMIN_DW_HISTORY_REQUEST".equals(type)) {
                                        new Thread(() -> {
                                            AdminDwHistoryRequest req = gson.fromJson(msg, AdminDwHistoryRequest.class);

                                            Timestamp start = new Timestamp(req.startMillis);
                                            Timestamp end = new Timestamp(req.endMillis);

                                            List<AdminDepositHistoryRow> rows = depositHistoryService.getAdminHistory(req.nameKeyword, start, end);

                                            AdminDwHistoryResponse response = new AdminDwHistoryResponse(true, null, rows);
                                            ctx.writeAndFlush(gson.toJson(response) + "\n"); // 요청한 관리자에게만 응답
                                        }).start();
                                    }

                                    else if ("CUSTOMER_DW_HISTORY_REQUEST".equals(type)) {
                                        new Thread(() -> {
                                            CustomerDwHistoryRequest req = gson.fromJson(msg, CustomerDwHistoryRequest.class);

                                            Timestamp start = new Timestamp(req.startMillis);
                                            Timestamp end = new Timestamp(req.endMillis);

                                            List<CustomerDepositHistoryRow> rows = depositHistoryService.getCustomerHistory(req.userId, start, end);

                                            CustomerDwHistoryResponse response = new CustomerDwHistoryResponse(true, null, rows);
                                            ctx.writeAndFlush(gson.toJson(response) + "\n"); // 요청한 그 고객에게만 응답
                                        }).start();
                                    } else if ("TOP_INFO_REQUEST".equals(type)) {
                                        Integer userId = ctx.channel().attr(SessionManager.USER_ID_KEY).get();
                                        if (userId != null) {
                                            topInfoService.pushToUser(userId);
                                        }
                                    } else if ("ENTIRE_AGGREGATE_REQUEST".equals(type)) {
                                        EntireAggregateRequest request = gson.fromJson(msg, EntireAggregateRequest.class);

                                        Timestamp start = new Timestamp(request.getStartMillis());
                                        Timestamp end = new Timestamp(request.getEndMillis());

                                        List<String[]> symbols = Market.MarketSpecCache.getAll().stream()
                                                .map(s -> new String[]{s.getSymbol(), s.getDisplayName()})
                                                .collect(java.util.stream.Collectors.toList());

                                        Map<String, Double> summary = orderDAO.loadEntireSummary(start, end);
                                        List<model.EntireDailyRow> dailyRows = orderDAO.loadDailyAggregateList(start, end, symbols);

                                        EntireAggregateResponse response = new EntireAggregateResponse(summary, dailyRows);
                                        ctx.writeAndFlush(gson.toJson(response) + "\n");
                                    } else if ("CHAT_HISTORY_REQUEST".equals(type)) {
                                        ChatHistoryRequest request = gson.fromJson(msg, ChatHistoryRequest.class);
                                        List<model.ChatMessageRow> messages = chatDAO.loadMessages(request.getRoomId());
                                        ChatHistoryResponse response = new ChatHistoryResponse(messages);
                                        ctx.writeAndFlush(gson.toJson(response) + "\n");

                                    } else if ("CHAT_SEND_REQUEST".equals(type)) {
                                        ChatSendRequest request = gson.fromJson(msg, ChatSendRequest.class);

                                        boolean success = chatDAO.sendMessage(
                                                request.getRoomId(), request.getSenderType(), request.getSenderId(), request.getMessage()
                                        );

                                        model.ChatMessageRow saved = success ? chatDAO.findLatestMessage(request.getRoomId()) : null;

                                        ctx.writeAndFlush(gson.toJson(new ChatSendResponse(success, saved)) + "\n");   // 🔥 메시지 포함

                                        if (success) {
                                            ChatPushEvent event = new ChatPushEvent(request.getRoomId(), "NEW_MESSAGE", saved);

                                            if ("USER".equals(request.getSenderType())) {
                                                SessionManager.broadcastToAdmins(event);
                                            } else {
                                                SessionManager.sendToCustomer(request.getRoomId(), event);
                                            }

                                            SessionManager.broadcastToAdmins(new ChatListUpdate(chatDAO.getChatList()));
                                        }
                                    } else if ("CHAT_MARK_READ_REQUEST".equals(type)) {
                                        ChatMarkReadRequest request = gson.fromJson(msg, ChatMarkReadRequest.class);

                                        if ("USER".equals(request.getReaderType())) {
                                            chatDAO.markAsReadByUser(request.getRoomId());
                                            SessionManager.broadcastToAdmins(new ChatPushEvent(request.getRoomId(), "READ_UPDATE", null));
                                        } else {
                                            chatDAO.markAsReadByAdmin(request.getRoomId());
                                            SessionManager.sendToCustomer(request.getRoomId(), new ChatPushEvent(request.getRoomId(), "READ_UPDATE", null));
                                        }

                                        SessionManager.broadcastToAdmins(new ChatListUpdate(chatDAO.getChatList()));

                                    } else if ("CHAT_LIST_REQUEST".equals(type)) {
                                        ChatListUpdate response = new ChatListUpdate(chatDAO.getChatList());
                                        ctx.writeAndFlush(gson.toJson(response) + "\n");
                                    }else if ("ADMIN_USER_LIST_REQUEST".equals(type)) {
                                        new Thread(() -> {
                                            AdminUserListRequest req = gson.fromJson(msg, AdminUserListRequest.class);
                                            List<AdminUserListRow> rows = adminUserListService.loadCustomers(req.keyword);
                                            ctx.writeAndFlush(gson.toJson(new AdminUserListResponse(rows)) + "\n");
                                        }).start();
                                    }
//                                    else if ("ADMIN_USER_BASIC_INFO_REQUEST".equals(type)) {
//                                        new Thread(() -> {
//                                            AdminUserBasicInfoRequest req = gson.fromJson(msg, AdminUserBasicInfoRequest.class);
//                                            AdminUserBasicInfo info = adminUserBasicService.loadBasicInfo(req.username);
//
//                                            boolean success = (info != null);
//                                            AdminUserBasicInfoResponse response = new AdminUserBasicInfoResponse(
//                                                    success, success ? null : "조회 실패", info
//                                            );
//                                            ctx.writeAndFlush(gson.toJson(response) + "\n");
//                                        }).start();
//                                    }
//
//                                    else if ("ADMIN_USER_BASIC_INFO_SAVE_REQUEST".equals(type)) {
//                                        new Thread(() -> {
//                                            AdminUserBasicInfoSaveRequest req = gson.fromJson(msg, AdminUserBasicInfoSaveRequest.class);
//
//                                            String failReason = adminUserBasicService.saveBasicInfo(
//                                                    req.username, req.name, req.password, req.email, req.phone,
//                                                    req.recommender, req.grade, req.partnerMemo, req.bank,
//                                                    req.accountNumber, req.accountHolder, req.depositAccount,
//                                                    req.overnight, req.remote
//                                            );
//
//                                            boolean success = (failReason == null);
//                                            AdminUserBasicInfoSaveResult result = new AdminUserBasicInfoSaveResult(
//                                                    success, success ? "저장되었습니다." : failReason
//                                            );
//                                            ctx.writeAndFlush(gson.toJson(result) + "\n");
//
//                                            // 🔥 저장 성공 시 모든 관리자에게 변경 신호  해당하는 유저의 상세창 보고있는애들 관리자들만 업데이트된 정보들로 업데이트/고객정보 테이블은 상관x
//                                            if (success) {
//                                                SessionManager.broadcastToAdmins(new UserDataChangedEvent(req.username, req.adminId));
//                                            }
//                                        }).start();
//                                    }
//                                    else if ("ADMIN_USER_ACCOUNT_DATA_REQUEST".equals(type)) {
//                                        new Thread(() -> {
//                                            AdminUserAccountDataRequest req = gson.fromJson(msg, AdminUserAccountDataRequest.class);
//                                            AdminUserAccountData data = adminUserAccountService.loadAccountData(req.username);
//
//                                            boolean success = (data != null);
//                                            AdminUserAccountDataResponse response = new AdminUserAccountDataResponse(
//                                                    success, success ? null : "조회 실패", data
//                                            );
//                                            ctx.writeAndFlush(gson.toJson(response) + "\n");
//                                        }).start();
//                                    }
//
//                                    else if ("ADMIN_USER_ACCOUNT_DATA_SAVE_REQUEST".equals(type)) {
//                                        new Thread(() -> {
//                                            AdminUserAccountDataSaveRequest req = gson.fromJson(msg, AdminUserAccountDataSaveRequest.class);
//
//                                            String failReason = adminUserAccountService.saveAccountData(
//                                                    req.username, req.accountStatus, req.server, req.mileage, req.memo
//                                            );
//
//                                            boolean success = (failReason == null);
//                                            AdminUserAccountDataSaveResult result = new AdminUserAccountDataSaveResult(
//                                                    success, success ? "저장되었습니다." : failReason
//                                            );
//                                            ctx.writeAndFlush(gson.toJson(result) + "\n");
//
//                                            if (success) {
//                                                SessionManager.broadcastToAdmins(new UserDataChangedEvent(req.username, req.adminId));
//                                            }
//                                        }).start();
//                                    }
                                   else if ("ADMIN_USER_DEPOSIT_REQUEST".equals(type)) {
                                        new Thread(() -> {
                                            AdminUserDepositRequest req = gson.fromJson(msg, AdminUserDepositRequest.class);
                                            AdminUserDepositData data = adminUserDepositService.loadBalance(req.username);

                                            boolean success = (data != null);
                                            AdminUserDepositResponse response = new AdminUserDepositResponse(
                                                    success, success ? null : "조회 실패", data
                                            );
                                            ctx.writeAndFlush(gson.toJson(response) + "\n");
                                        }).start();
                                    }

                                    else if ("ADMIN_USER_BALANCE_UPDATE_REQUEST".equals(type)) {
                                        new Thread(() -> {
                                            AdminUserBalanceUpdateRequest req = gson.fromJson(msg, AdminUserBalanceUpdateRequest.class);

                                            int[] outUserId = new int[1];
                                            String failReason = adminUserDepositService.updateBalance(
                                                    req.username, req.delta, req.memo, req.adminId, outUserId
                                            );

                                            boolean success = (failReason == null);
                                            AdminUserBalanceUpdateResult result = new AdminUserBalanceUpdateResult(
                                                    success, success ? (req.delta > 0 ? "입금 완료되었습니다." : "출금 완료되었습니다.") : failReason
                                            );
                                            ctx.writeAndFlush(gson.toJson(result) + "\n");

                                            if (success) {
                                                int userId = outUserId[0];

                                                topInfoService.pushToUser(userId);
                                                customerDepositService.pushBalanceToUser(userId);
                                                SessionManager.broadcastToAdmins(new UserDataChangedEvent(req.username, req.adminId));

                                                // 고객에게는 승인 사운드
                                                String customerSoundType = req.delta > 0 ? "DEPOSIT_APPROVED" : "WITHDRAW_APPROVED";
                                                SessionManager.sendToCustomer(userId, new ClientEventMessage("PLAY_SOUND", null, customerSoundType));

                                                // 🔥 관리자는 요청자 한 명이 아니라 전원에게
                                                String adminSoundType = req.delta > 0 ? "DEPOSIT_MANUAL_EXECUTED" : "WITHDRAW_MANUAL_EXECUTED";
                                                SessionManager.broadcastToAdmins(new ClientEventMessage("PLAY_SOUND", null, adminSoundType));

                                                // 🔥 입출금모니터링 패널도 바로 갱신되도록 목록 브로드캐스트
                                                SessionManager.broadcastToAdmins(buildDepositMonitoringResponse());
                                            }
                                        }).start();
                                    }
//                                    else if ("ADMIN_USER_FEE_REQUEST".equals(type)) {
//                                        new Thread(() -> {
//                                            AdminUserFeeRequest req = gson.fromJson(msg, AdminUserFeeRequest.class);
//                                            AdminUserFeeData data = adminUserFeeService.loadFeeSettings(req.username);
//
//                                            AdminUserFeeResponse response = new AdminUserFeeResponse(true, null, data);
//                                            ctx.writeAndFlush(gson.toJson(response) + "\n");
//                                        }).start();
//                                    }
//
//                                    else if ("ADMIN_USER_DOMESTIC_FEE_SAVE_REQUEST".equals(type)) {
//                                        new Thread(() -> {
//                                            AdminUserDomesticFeeSaveRequest req = gson.fromJson(msg, AdminUserDomesticFeeSaveRequest.class);
//
//                                            String failReason = adminUserFeeService.saveDomesticFee(
//                                                    req.username, req.futuresFee, req.nightFuturesFee, req.optionsFee, req.nightOptionsFee
//                                            );
//
//                                            boolean success = (failReason == null);
//                                            ctx.writeAndFlush(gson.toJson(new AdminUserFeeSaveResult(
//                                                    success, success ? "국내 수수료 저장 완료" : failReason
//                                            )) + "\n");
//
//                                            if (success) {
//                                                SessionManager.broadcastToAdmins(new UserDataChangedEvent(req.username, req.adminId));
//                                            }
//                                        }).start();
//                                    }
//
//                                    else if ("ADMIN_USER_OVERSEAS_FEE_SAVE_REQUEST".equals(type)) {
//                                        new Thread(() -> {
//                                            AdminUserOverseasFeeSaveRequest req = gson.fromJson(msg, AdminUserOverseasFeeSaveRequest.class);
//
//                                            String failReason = adminUserFeeService.saveOverseasFee(req.username, req.rows);
//
//                                            boolean success = (failReason == null);
//                                            ctx.writeAndFlush(gson.toJson(new AdminUserFeeSaveResult(
//                                                    success, success ? "해외 수수료 저장 완료" : failReason
//                                            )) + "\n");
//
//                                            if (success) {
//                                                SessionManager.broadcastToAdmins(new UserDataChangedEvent(req.username, req.adminId));
//                                            }
//                                        }).start();
//                                    }else if ("ADMIN_USER_QTY_LIMIT_REQUEST".equals(type)) {
//                                        new Thread(() -> {
//                                            AdminUserQtyLimitRequest req = gson.fromJson(msg, AdminUserQtyLimitRequest.class);
//                                            AdminUserQtyLimitData data = adminUserQtyLimitService.loadQtyLimits(req.username);
//                                            int systemMax = adminUserQtyLimitService.getSystemMaxOverseasQty();
//
//                                            boolean success = (data != null);
//                                            AdminUserQtyLimitResponse response = new AdminUserQtyLimitResponse(
//                                                    success, success ? null : "조회 실패", data, systemMax
//                                            );
//                                            ctx.writeAndFlush(gson.toJson(response) + "\n");
//                                        }).start();
//                                    }
//
//                                    else if ("ADMIN_USER_QTY_LIMIT_SAVE_REQUEST".equals(type)) {
//                                        new Thread(() -> {
//                                            AdminUserQtyLimitSaveRequest req = gson.fromJson(msg, AdminUserQtyLimitSaveRequest.class);
//
//                                            String failReason = adminUserQtyLimitService.saveQtyLimits(
//                                                    req.username, req.maxFuturesQty, req.maxOptionsBuyQty, req.maxOptionsSellQty, req.maxOverseasQty
//                                            );
//
//                                            boolean success = (failReason == null);
//                                            ctx.writeAndFlush(gson.toJson(new AdminUserQtyLimitSaveResult(
//                                                    success, success ? "저장되었습니다." : failReason
//                                            )) + "\n");
//
//                                            if (success) {
//                                                SessionManager.broadcastToAdmins(new UserDataChangedEvent(req.username, req.adminId));
//                                            }
//                                        }).start();
//                                    }
//
//                                    else if ("ADMIN_USER_OVERSEAS_QTY_REQUEST".equals(type)) {
//                                        new Thread(() -> {
//                                            AdminUserOverseasQtyRequest req = gson.fromJson(msg, AdminUserOverseasQtyRequest.class);
//                                            List<OverseasQtyRow> rows = adminUserQtyLimitService.loadOverseasQtyLimits(req.username);
//                                            ctx.writeAndFlush(gson.toJson(new AdminUserOverseasQtyResponse(true, rows)) + "\n");
//                                        }).start();
//                                    }
//
//                                    else if ("ADMIN_USER_OVERSEAS_QTY_SAVE_REQUEST".equals(type)) {
//                                        new Thread(() -> {
//                                            AdminUserOverseasQtySaveRequest req = gson.fromJson(msg, AdminUserOverseasQtySaveRequest.class);
//
//                                            // 저장 시 검증용 시스템 최대값 다시 조회 (클라가 들고 있던 값이 stale할 수 있으니)
//                                            AdminUserQtyLimitData currentData = adminUserQtyLimitService.loadQtyLimits(req.username);
//                                            int overseasMax = currentData != null ? currentData.getMaxOverseasQty() : Integer.MAX_VALUE;
//
//                                            String failReason = adminUserQtyLimitService.saveOverseasQtyLimits(req.username, req.rows, overseasMax);
//
//                                            boolean success = (failReason == null);
//                                            ctx.writeAndFlush(gson.toJson(new AdminUserQtyLimitSaveResult(
//                                                    success, success ? "저장되었습니다." : failReason
//                                            )) + "\n");
//
//                                            if (success) {
//                                                SessionManager.broadcastToAdmins(new UserDataChangedEvent(req.username, req.adminId));
//                                            }
//                                        }).start();
//                                    }
                                   else if ("ADMIN_USER_FULL_REQUEST".equals(type)) {
                                        new Thread(() -> {
                                            AdminUserFullRequest req = gson.fromJson(msg, AdminUserFullRequest.class);
                                            AdminUserFullResponse response = adminUserFullService.loadAll(req.username);
                                            ctx.writeAndFlush(gson.toJson(response) + "\n");
                                        }).start();
                                    }

                                    else if ("ADMIN_USER_FULL_SAVE_REQUEST".equals(type)) {
                                        new Thread(() -> {
                                            AdminUserFullSaveRequest req = gson.fromJson(msg, AdminUserFullSaveRequest.class);

                                            String failReason = adminUserFullService.saveAll(req);

                                            boolean success = (failReason == null);
                                            ctx.writeAndFlush(gson.toJson(new AdminUserFullSaveResult(
                                                    success, success ? "저장되었습니다." : failReason
                                            )) + "\n");

                                            if (success) {
                                                SessionManager.broadcastToAdmins(new UserDataChangedEvent(req.username, req.adminId));
                                            }
                                        }).start();
                                    }

/// //////////////////////////////////////////상세정보끝

                                    /// //////새로운 고객 추가할 때
                                    else if ("ADMIN_USER_REGISTER_REQUEST".equals(type)) {
                                        new Thread(() -> {
                                            AdminUserRegisterRequest req = gson.fromJson(msg, AdminUserRegisterRequest.class);

                                            String failReason = adminUserRegisterService.insertNewUser(req);

                                            boolean success = (failReason == null);
                                            ctx.writeAndFlush(gson.toJson(new AdminUserRegisterResult(
                                                    success, success ? "신규 가입자 추가 완료!" : failReason
                                            )) + "\n");

                                            if (success) {
                                                // 신규가입도 목록에 영향 주니 관리자 전원 목록 갱신 신호
                                                SessionManager.broadcastToAdmins(new UserDataChangedEvent(req.username, req.adminId));
                                            }
                                        }).start();
                                    }





/// /////////////////////거래규칙설정 시작
                                 else if ("FEE_OVERRIDE_LIST_REQUEST".equals(type)) {

                                    List<String> symbols = Market.MarketSpecCache.getAll().stream()
                                            .map(Market.MarketSpec::getSymbol)
                                            .collect(java.util.stream.Collectors.toList());

                                    List<model.FeeOverrideRow> rows = symbolFeeOverrideDAO.getAllOverrides(symbols);

                                    FeeOverrideListResponse response = new FeeOverrideListResponse(rows);
                                    ctx.writeAndFlush(gson.toJson(response) + "\n");

                                } else if ("FEE_OVERRIDE_SAVE_REQUEST".equals(type)) {
                                    FeeOverrideSaveRequest request = gson.fromJson(msg, FeeOverrideSaveRequest.class);

                                    symbolFeeOverrideDAO.saveAllOverrides(request.getRows());

                                    FeeOverrideSaveResponse response = new FeeOverrideSaveResponse(true, "저장 완료");
                                    ctx.writeAndFlush(gson.toJson(response) + "\n");

                                    System.out.println("[서버] 수수료 오버라이드 저장 완료 - " + request.getRows().size() + "개 종목");
                                    } else if ("COMPANY_ACCOUNT_LIST_REQUEST".equals(type)) {

                                        List<Object[]> raw = companyAccountDAO.getAll();
                                        List<model.CompanyAccount> accounts = new ArrayList<>();
                                        for (Object[] row : raw) {
                                            accounts.add(new model.CompanyAccount(
                                                    (int) row[0], (String) row[1], (String) row[2], (String) row[3], (String) row[4]
                                            ));
                                        }

                                        CompanyAccountListResponse response = new CompanyAccountListResponse(accounts);
                                        ctx.writeAndFlush(gson.toJson(response) + "\n");

                                    } else if ("COMPANY_ACCOUNT_SAVE_REQUEST".equals(type)) {
                                        CompanyAccountSaveRequest request = gson.fromJson(msg, CompanyAccountSaveRequest.class);

                                        switch (request.getMode()) {
                                            case "ADD" -> companyAccountDAO.insert(
                                                    request.getBank(), request.getAccountNumber(), request.getAccountHolder(), request.getAlias()
                                            );
                                            case "EDIT" -> companyAccountDAO.update(
                                                    request.getId(), request.getBank(), request.getAccountNumber(),
                                                    request.getAccountHolder(), request.getAlias()
                                            );
                                            case "DELETE" -> companyAccountDAO.delete(request.getId());
                                        }

                                        CompanyAccountSaveResponse response = new CompanyAccountSaveResponse(true, "처리 완료");
                                        ctx.writeAndFlush(gson.toJson(response) + "\n");

                                        System.out.println("[서버] 회사계좌 " + request.getMode() + " 처리 완료");
                                    } else if ("MARKET_OPERATION_LOAD_REQUEST".equals(type)) {

                                        model.DomesticMarketData domestic = marketSpecDAO.loadDomesticDataModel();
                                        model.HsiMarketData hsi = marketSpecDAO.loadHsiDataModel();
                                        List<model.OverseasMarketRow> overseas = marketSpecDAO.loadOverseasDataList();

                                        MarketOperationLoadResponse response = new MarketOperationLoadResponse(domestic, hsi, overseas);
                                        ctx.writeAndFlush(gson.toJson(response) + "\n");

                                    } else if ("DOMESTIC_SAVE_REQUEST".equals(type)) {
                                        DomesticSaveRequest request = gson.fromJson(msg, DomesticSaveRequest.class);

                                        marketSpecDAO.saveDomesticData(
                                                request.getAuctionStart(), request.getTradeStart(), request.getTradeEnd(),
                                                request.isHolidayToday(), request.getExpiryDate()
                                        );

                                        MarketOperationSaveResponse response = new MarketOperationSaveResponse(true, "국내선물 저장 완료");
                                        ctx.writeAndFlush(gson.toJson(response) + "\n");
                                        System.out.println("[서버] 국내선물 운영시간 저장 완료");

                                    } else if ("HSI_SAVE_REQUEST".equals(type)) {
                                        HsiSaveRequest request = gson.fromJson(msg, HsiSaveRequest.class);

                                        marketSpecDAO.saveHsiData(
                                                request.getStart1(), request.getEnd1(), request.getStart2(), request.getEnd2(),
                                                request.getStart3(), request.getEnd3(), request.isHolidayToday(), request.getExpiryDate()
                                        );

                                        MarketOperationSaveResponse response = new MarketOperationSaveResponse(true, "항셍 저장 완료");
                                        ctx.writeAndFlush(gson.toJson(response) + "\n");
                                        System.out.println("[서버] 항셍 운영시간 저장 완료");

                                    } else if ("OVERSEAS_SAVE_REQUEST".equals(type)) {
                                        OverseasSaveRequest request = gson.fromJson(msg, OverseasSaveRequest.class);

                                        marketSpecDAO.saveOverseasDataList(request.getRows());

                                        MarketOperationSaveResponse response = new MarketOperationSaveResponse(true, "해외선물 저장 완료");
                                        ctx.writeAndFlush(gson.toJson(response) + "\n");
                                        System.out.println("[서버] 해외선물 운영시간 저장 완료");

                                    } else if ("SYSTEM_QTY_LIMIT_LOAD_REQUEST".equals(type)) {
                                        model.SystemQtyLimit data = systemQtyLimitDAO.getSettings();
                                        SystemQtyLimitResponse response = new SystemQtyLimitResponse(data);
                                        ctx.writeAndFlush(gson.toJson(response) + "\n");

                                    } else if ("SYSTEM_QTY_LIMIT_SAVE_REQUEST".equals(type)) {
                                        SystemQtyLimitSaveRequest request = gson.fromJson(msg, SystemQtyLimitSaveRequest.class);
                                        systemQtyLimitDAO.updateSettings(
                                                request.getFuturesQty(), request.getOptionBuyQty(),
                                                request.getOptionSellQty(), request.getOverseasQty()
                                        );
                                        SystemQtyLimitSaveResponse response = new SystemQtyLimitSaveResponse(true);
                                        ctx.writeAndFlush(gson.toJson(response) + "\n");
                                        System.out.println("[서버] 시스템 계약수 한도 저장 완료");
                                    }









                                    else if ("DW_ACCOUNT_INFO_REQUEST".equals(type)) {
                                        new Thread(() -> {
                                            DwAccountInfoRequest req = gson.fromJson(msg, DwAccountInfoRequest.class);
                                            DwAccountInfo info = customerDepositService.loadAccountInfo(req.userId);
                                            ctx.writeAndFlush(gson.toJson(new DwAccountInfoResponse(info)) + "\n");
                                        }).start();
                                    }



                                }



                                @Override
                                public void channelActive(ChannelHandlerContext ctx) {
                                    System.out.println("[서버] 클라이언트 접속: " + ctx.channel().remoteAddress());
                                }

                                @Override
                                public void channelInactive(ChannelHandlerContext ctx) {
                                    System.out.println("[서버] 클라이언트 접속 종료: " + ctx.channel().remoteAddress());
                                    SessionManager.unregister(ctx);
                                }
                            });
                        }
                    });

            ChannelFuture f = b.bind(port).sync();
            System.out.println("[서버] " + port + "번 포트에서 대기중...");

            // 관리자 흉내: 콘솔에 유저 id 입력하면 그 유저 강제 로그아웃
            startAdminConsole();

            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }


//입출금승인헬퍼메소드//
    private static DepositMonitoringResponse buildDepositMonitoringResponse() {
        List<AdminDepositMonitoring> deposits = depositService.getMonitoring("DEPOSIT");
        List<AdminDepositMonitoring> withdraws = depositService.getMonitoring("WITHDRAW");
        return new DepositMonitoringResponse(deposits, withdraws);
    }


    // 콘솔에서 "강제로그아웃 9" 이렇게 입력하면 userId=9인 유저를 로그아웃시킴 (관리자 프로그램 대신 임시 흉내)
    private static void startAdminConsole() {
        Thread adminThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            System.out.println("[관리자 콘솔] '강제로그아웃 <userId>' 입력 시 해당 유저 로그아웃");
            while (true) {
                String line = scanner.nextLine();
                if (line.startsWith("강제로그아웃")) {
                    try {
                        int userId = Integer.parseInt(line.split(" ")[1].trim());
                        ChannelHandlerContext ctx = SessionManager.getCustomer(userId);
                        if (ctx != null) {
                            ForceLogoutMessage forceLogout = new ForceLogoutMessage("관리자에 의해 강제 로그아웃되었습니다.");
                            ctx.writeAndFlush(gson.toJson(forceLogout) + "\n");
                            System.out.println("[관리자] userId=" + userId + " 강제 로그아웃 명령 전송");
                        } else {
                            System.out.println("[관리자] userId=" + userId + " 접속중이지 않음");
                        }
                    } catch (Exception e) {
                        System.out.println("사용법: 강제로그아웃 9");
                    }
                }
            }
        });
        adminThread.setDaemon(true);
        adminThread.start();
    }
}