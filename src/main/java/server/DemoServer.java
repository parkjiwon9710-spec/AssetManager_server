package server;

import Market.MarketContext;
import com.google.gson.*;
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.*;

public class DemoServer {

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalTime.class, (JsonSerializer<LocalTime>) (src, typeOfSrc, context) ->
                    new JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalTime.class, (JsonDeserializer<LocalTime>) (json, typeOfT, context) ->
                    LocalTime.parse(json.getAsString()))
            .create();
    private static final UserDAO userDAO = new UserDAO();
    private static final BlacklistDAO blacklistDAO = new BlacklistDAO();
    private static final service.OrderDAO orderDAO = new service.OrderDAO();
    private static final service.PositionService positionService = new service.PositionService();
//    private static final service.OrderService orderService = new service.OrderService();
    private static final service.RiskService riskService = new service.RiskService();
    private static final service.OrderExecutionService orderExecutionService = new service.OrderExecutionService();
    private static final RealtimePnlService realtimePnlService = new RealtimePnlService();
    private static final service.UserService userService = new service.UserService();
    private static final service.TradeHistoryDAO tradeHistoryDAO = new service.TradeHistoryDAO();
    private static final service.AdminDepositService depositService = new AdminDepositService();
    private static final service.TopInfoService topInfoService = new TopInfoService();
    private static final service.PositionPanelService positionPanelService = new PositionPanelService();
    private static final service.CustomerDepositService customerDepositService = new CustomerDepositService();
    private static final service.DepositHistoryService depositHistoryService = new DepositHistoryService();
    private static final service.ChatDAO chatDAO = new service.ChatDAO();
    private static final service.AdminUserListService adminUserListService = new AdminUserListService();
    private static final service.AdminPositionAggregateService adminPositionAggregateService = new service.AdminPositionAggregateService(adminUserListService, positionService, orderDAO);

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
    private static final service.SystemTradeModeDAO systemTradeModeDAO = new service.SystemTradeModeDAO();
    private static final service.SymbolTradeSettingDAO symbolTradeSettingDAO = new SymbolTradeSettingDAO();
    private static final service.OrderService orderService = new OrderService(orderExecutionService);

    private static final service.UserStatusDAO userStatusDAO = new UserStatusDAO();
    private static final service.SoundSettingDAO soundSettingDAO = new SoundSettingDAO();

    private static final service.NoticeService noticeService = new NoticeService();


    private static final service.ChartService chartService = new ChartService();

    public static void main(String[] args) throws InterruptedException {





//서버 실행할 때 마켓스피씨캐시, 환율 로드

        Market.MarketSpecCache.load();
        Store.ExchangeRateCache.load();


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

                System.out.println("[서버] 관리자 실시간종합현황 갱신 push 완료 - " + rows.size() + "건, " + java.time.LocalTime.now());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 10, java.util.concurrent.TimeUnit.SECONDS);
        /// ////////////////////

        /// ///정기적으로 업데이트스케줄러!!!!!!!!!!!!!!///////////////

        /// ////////////////////////관리자 고객 포지션 스케줄러
        java.util.concurrent.Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                server.AdminAllPositionsResponse response = adminPositionAggregateService.computeAll();
                SessionManager.broadcastToAdmins(response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 5, java.util.concurrent.TimeUnit.SECONDS);
        /// ////////////////////////

/// ////////////////유저들 탑인포패널 담보금같은 정보 그리고 포지션패널도 정기적으로 업데이트스케줄러.////
        java.util.concurrent.Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                int count = 0;
                for (Integer userId : SessionManager.getConnectedCustomerIds()) {
                    topInfoService.pushToUser(userId);
                    positionPanelService.pushToUser(userId);
                    count++;
                }
                System.out.println("[서버] TopInfo 정기 push 완료 - " + count + "명, " + java.time.LocalTime.now());
                System.out.println("[서버] PositionPanel 정기 push 완료 - " + count + "명, " + java.time.LocalTime.now());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 10, java.util.concurrent.TimeUnit.SECONDS);
/// /////////////////////////////////////////////


        /// //오버나잇 스케줄러/////////
        OvernightProcessor overnightProcessor = new OvernightProcessor(
                orderService, positionService, marketSpecDAO, userService
        );
        OvernightProcessorHolder.init(overnightProcessor);

        OvernightScheduler scheduler = new OvernightScheduler(overnightProcessor, marketSpecDAO);
        OvernightSchedulerHolder.init(scheduler);
        scheduler.start();
        /// ///////////////////////////////////////////


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
                                            // 마지막 로그인 갱신
                                            userStatusDAO.updateLastLogin(user.getId());

// 사운드 설정 조회
                                            SoundSetting sound = soundSettingDAO.load(user.getId());

                                            response = new LoginResponse(
                                                    true, "로그인 성공", user.getId(), user.getUsername(), user.getName(), user.getRole(), user.getBalance(),
                                                    sound.isBuyExecuted(), sound.isSellExecuted(),
                                                    sound.isBuyReserved(), sound.isSellReserved(),
                                                    sound.isOrderModified(), sound.isOrderCancelled()
                                            );

                                            SessionManager.register(user.getId(), user.getUsername(), user.getName(), mac, user.getRole(), ctx);
                                            System.out.println("[서버] 로그인 성공 - userId: " + user.getId());

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
                                    } else if ("SCREEN_CAPTURE_REQUEST".equals(type)) {
                                        ScreenCaptureRequest request = gson.fromJson(msg, ScreenCaptureRequest.class);

                                        ChannelHandlerContext targetCtx = SessionManager.getCustomer(request.targetUserId);
                                        if (targetCtx != null) {
                                            ScreenCaptureCommand command = new ScreenCaptureCommand(request.requesterUserId);
                                            targetCtx.writeAndFlush(gson.toJson(command) + "\n");
                                            System.out.println("[서버] 화면 캡처 명령 전송 - target userId: " + request.targetUserId);
                                        } else {
                                            ScreenCaptureResult failResult = new ScreenCaptureResult(
                                                    request.requesterUserId, request.targetUserId,
                                                    false, "고객이 접속중이 아닙니다.", null, System.currentTimeMillis());
                                            SessionManager.sendToAdmin(request.requesterUserId, failResult);
                                            System.out.println("[서버] 캡처 요청 실패 - target userId " + request.targetUserId + " 미접속");
                                        }

                                    } else if ("SCREEN_CAPTURE_RESULT".equals(type)) {
                                        ScreenCaptureResult result = gson.fromJson(msg, ScreenCaptureResult.class);
                                        SessionManager.sendToAdmin(result.requesterUserId, result);
                                        System.out.println("[서버] 캡처 결과 전달 완료 - admin userId: " + result.requesterUserId);
                                    }


                                    else if ("ORDER_REQUEST".equals(type)) {
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

                                        // 변경 - 옵션은 제외하고, "OPTIONS" 대표 항목 하나만 추가
                                        List<String[]> symbols = new ArrayList<>();
                                        boolean hasOption = false;
                                        for (Market.MarketSpec s : Market.MarketSpecCache.getAll()) {
                                            if ("OPTIONS".equals(s.getMarketType())) {
                                                hasOption = true;
                                                continue;   // 개별 옵션 42개는 건너뜀
                                            }
                                            symbols.add(new String[]{s.getSymbol(), s.getDisplayName()});
                                        }
                                        if (hasOption) {
                                            symbols.add(new String[]{"OPTIONS", "옵션"});   // 대표 항목 하나
                                        }

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

                                        // 변경 - 옵션은 제외하고, "OPTIONS" 대표 항목 하나만 추가
                                        List<String[]> symbols = new ArrayList<>();
                                        boolean hasOption = false;
                                        for (Market.MarketSpec s : Market.MarketSpecCache.getAll()) {
                                            if ("OPTIONS".equals(s.getMarketType())) {
                                                hasOption = true;
                                                continue;   // 개별 옵션 42개는 건너뜀
                                            }
                                            symbols.add(new String[]{s.getSymbol(), s.getDisplayName()});
                                        }
                                        if (hasOption) {
                                            symbols.add(new String[]{"OPTIONS", "옵션"});   // 대표 항목 하나
                                        }

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

                                        // 변경 - 옵션은 제외하고, "OPTIONS" 대표 항목 하나만 추가
                                        List<String[]> symbols = new ArrayList<>();
                                        boolean hasOption = false;
                                        for (Market.MarketSpec s : Market.MarketSpecCache.getAll()) {
                                            if ("OPTIONS".equals(s.getMarketType())) {
                                                hasOption = true;
                                                continue;   // 개별 옵션 42개는 건너뜀
                                            }
                                            symbols.add(new String[]{s.getSymbol(), s.getDisplayName()});
                                        }
                                        if (hasOption) {
                                            symbols.add(new String[]{"OPTIONS", "옵션"});   // 대표 항목 하나
                                        }

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
                                        model.OptionMarketData option = marketSpecDAO.loadOptionDataModel();   // 🔥 추가
                                        model.HsiMarketData hsi = marketSpecDAO.loadHsiDataModel();
                                        List<model.OverseasMarketRow> overseas = marketSpecDAO.loadOverseasDataList();

                                        MarketOperationLoadResponse response = new MarketOperationLoadResponse(domestic, option, hsi, overseas);   // 🔥 순서 맞춰서 4개
                                        ctx.writeAndFlush(gson.toJson(response) + "\n");
                                    } else if ("DOMESTIC_SAVE_REQUEST".equals(type)) {
                                        DomesticSaveRequest request = gson.fromJson(msg, DomesticSaveRequest.class);

                                        marketSpecDAO.saveDomesticData(
                                                request.getAuctionStart(), request.getTradeStart(), request.getTradeEnd(),
                                                request.isHolidayToday(), request.getExpiryDate()
                                        );

                                        OvernightSchedulerHolder.get().reload();

                                        MarketOperationSaveResponse response = new MarketOperationSaveResponse(true, "국내선물 저장 완료");
                                        ctx.writeAndFlush(gson.toJson(response) + "\n");
                                        System.out.println("[서버] 국내선물 운영시간 저장 완료");

                                    } else if ("OPTION_SAVE_REQUEST".equals(type)) {
                                        OptionSaveRequest request = gson.fromJson(msg, OptionSaveRequest.class);

                                        try {
                                            marketSpecDAO.saveOptionData(
                                                    request.getTradeStart(),
                                                    request.getTradeEnd(),
                                                    request.isHolidayToday(),
                                                    request.getExpiryDate()
                                            );


                                            OvernightSchedulerHolder.get().reload();

                                            MarketOperationSaveResponse response = new MarketOperationSaveResponse(true, "옵션 저장 완료");   // 🔥 메시지 추가
                                            ctx.writeAndFlush(gson.toJson(response) + "\n");
                                            System.out.println("[서버] 옵션 운영시간 저장 완료");

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            MarketOperationSaveResponse response = new MarketOperationSaveResponse(false, "옵션 저장 실패");   // 🔥 메시지 추가
                                            ctx.writeAndFlush(gson.toJson(response) + "\n");
                                        }
                                    } else if ("HSI_SAVE_REQUEST".equals(type)) {
                                        HsiSaveRequest request = gson.fromJson(msg, HsiSaveRequest.class);

                                        marketSpecDAO.saveHsiData(
                                                request.getStart1(), request.getEnd1(), request.getStart2(), request.getEnd2(),
                                                request.getStart3(), request.getEnd3(), request.isHolidayToday(), request.getExpiryDate()
                                        );

                                        OvernightSchedulerHolder.get().reload();

                                        MarketOperationSaveResponse response = new MarketOperationSaveResponse(true, "항셍 저장 완료");
                                        ctx.writeAndFlush(gson.toJson(response) + "\n");
                                        System.out.println("[서버] 항셍 운영시간 저장 완료");

                                    } else if ("OVERSEAS_SAVE_REQUEST".equals(type)) {
                                        OverseasSaveRequest request = gson.fromJson(msg, OverseasSaveRequest.class);

                                        marketSpecDAO.saveOverseasDataList(request.getRows());


                                        OvernightSchedulerHolder.get().reload();

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
                                    } else if ("TRADE_LIMIT_LOAD_REQUEST".equals(type)) {

                                    Market.MarketSpec overseas = marketSpecDAO.getFirstOverseasSpec();
                                    Market.MarketSpec domestic = marketSpecDAO.getDomesticSpec();
                                    Market.MarketSpec option = marketSpecDAO.getOptionSpec();
                                    model.SystemTradeMode mode = systemTradeModeDAO.getSettings();

                                    model.MarketMarginInfo overseasInfo = overseas == null ? null : new model.MarketMarginInfo(
                                            overseas.getEntryMargin(), overseas.getMaintMargin(), overseas.getOvernightMargin(), overseas.isOvernightEnabled()
                                    );
                                    model.MarketMarginInfo domesticInfo = domestic == null ? null : new model.MarketMarginInfo(
                                            domestic.getEntryMargin(), domestic.getMaintMargin(), domestic.getOvernightMargin(), domestic.isOvernightEnabled()
                                    );
                                    model.MarketMarginInfo optionInfo = option == null ? null : new model.MarketMarginInfo(
                                            option.getEntryMargin(), option.getMaintMargin(), option.getOvernightMargin(), option.isOvernightEnabled()
                                    );

                                    model.TradeLimitSettings settings = new model.TradeLimitSettings(overseasInfo, domesticInfo, optionInfo, mode);

                                    TradeLimitLoadResponse response = new TradeLimitLoadResponse(settings);
                                    ctx.writeAndFlush(gson.toJson(response) + "\n");

                                } else if ("ENTRY_MARGIN_SAVE_REQUEST".equals(type)) {
                                    EntryMarginSaveRequest request = gson.fromJson(msg, EntryMarginSaveRequest.class);

                                    model.SystemTradeMode mode = systemTradeModeDAO.getSettings();
                                    if (mode == null) mode = new model.SystemTradeMode();

                                    if (request.isGlobal()) {
                                        mode.setOverseasEntryMarginMode("GLOBAL");
                                        marketSpecDAO.updateOverseasEntryMargin(request.getOverseasValue());
                                    } else {
                                        mode.setOverseasEntryMarginMode("PER_SYMBOL");
                                    }


                                        // 국내선물/옵션: 모드 구분 없이 항상 직접 반영
                                        marketSpecDAO.updateDomesticEntryMargin(request.getDomesticValue());
                                        marketSpecDAO.updateOptionEntryMargin(request.getOptionValue());

                                    systemTradeModeDAO.save(mode);

                                    TradeLimitSaveResponse response = new TradeLimitSaveResponse(true, "엔트리 증거금 설정 저장완료");
                                    ctx.writeAndFlush(gson.toJson(response) + "\n");
                                    System.out.println("[서버] 엔트리 증거금 설정 저장 완료");
                                }
                                    else if ("MAINT_MARGIN_SAVE_REQUEST".equals(type)) {
                                        MaintMarginSaveRequest request = gson.fromJson(msg, MaintMarginSaveRequest.class);
                                        model.SystemTradeMode mode = systemTradeModeDAO.getSettings();
                                        if (mode == null) mode = new model.SystemTradeMode();

                                        // 해외선물: 기존 GLOBAL/PER_SYMBOL 로직 유지
                                        if (request.isGlobal()) {
                                            mode.setOverseasMaintMarginMode("GLOBAL");
                                            marketSpecDAO.updateOverseasMaintMargin(request.getOverseasValue());
                                        } else {
                                            mode.setOverseasMaintMarginMode("PER_SYMBOL");
                                        }

                                        // 국내선물/옵션: 모드 구분 없이 항상 직접 반영
                                        marketSpecDAO.updateDomesticMaintMargin(request.getDomesticValue());
                                        marketSpecDAO.updateOptionMaintMargin(request.getOptionValue());

                                        systemTradeModeDAO.save(mode);

                                        TradeLimitSaveResponse response = new TradeLimitSaveResponse(true, "유지증거금 저장완료");
                                        ctx.writeAndFlush(gson.toJson(response) + "\n");
                                        System.out.println("[서버] 유지증거금 저장 완료");
                                    }
                                    else if ("OVERNIGHT_SETTINGS_SAVE_REQUEST".equals(type)) {
                                        OvernightSettingsSaveRequest request = gson.fromJson(msg, OvernightSettingsSaveRequest.class);

                                        model.SystemTradeMode mode = systemTradeModeDAO.getSettings();
                                        if (mode == null) mode = new model.SystemTradeMode();

                                        // 국내선물
                                        marketSpecDAO.updateDomesticOvernightMargin(request.getDomesticMarginValue());
                                        marketSpecDAO.updateDomesticOvernightEnabled(request.isDomesticOvernightEnabled());

                                        // 옵션
                                        marketSpecDAO.updateOptionOvernightMargin(request.getOptionMarginValue());
                                        marketSpecDAO.updateOptionOvernightEnabled(request.isOptionOvernightEnabled());

                                        // 해외선물 담보금
                                        if (request.isOverseasMarginGlobal()) {
                                            mode.setOverseasOvernightMarginMode("GLOBAL");
                                            marketSpecDAO.updateOverseasOvernightMargin(request.getOverseasMarginValue());
                                        } else {
                                            mode.setOverseasOvernightMarginMode("PER_SYMBOL");
                                        }

                                        // 해외선물 허용
                                        String permissionMode = request.getOverseasPermissionMode();
                                        mode.setOverseasPermissionMode(permissionMode);

                                        if ("ALL_ENABLE".equals(permissionMode)) {
                                            marketSpecDAO.updateOverseasOvernightEnabled(true);
                                        } else if ("ALL_DISABLE".equals(permissionMode)) {
                                            marketSpecDAO.updateOverseasOvernightEnabled(false);
                                        }
                                        // PER_SYMBOL이면 여기선 아무것도 안 건드림

                                        systemTradeModeDAO.save(mode);

                                        TradeLimitSaveResponse response = new TradeLimitSaveResponse(true, "오버나잇 설정 저장완료");
                                        ctx.writeAndFlush(gson.toJson(response) + "\n");
                                        System.out.println("[서버] 오버나잇 설정 저장 완료");
                                    }else if ("SYMBOL_VALUE_LOAD_REQUEST".equals(type)) {
                                        SymbolValueLoadRequest request = gson.fromJson(msg, SymbolValueLoadRequest.class);

                                        List<String> symbols = marketSpecDAO.getOverseasSymbols();
                                        Map<String, Long> values = new LinkedHashMap<>();

                                        for (String symbol : symbols) {
                                            long value = switch (request.getMarginType()) {
                                                case "ENTRY" -> symbolTradeSettingDAO.getEntryMargin(symbol);
                                                case "LOSSCUT" -> symbolTradeSettingDAO.getMaintMargin(symbol);
                                                case "OVERNIGHT" -> symbolTradeSettingDAO.getOvernightMargin(symbol);
                                                default -> 0;
                                            };
                                            values.put(symbol, value);
                                        }

                                        SymbolValueLoadResponse response = new SymbolValueLoadResponse(values);
                                        ctx.writeAndFlush(gson.toJson(response) + "\n");

                                    } else if ("SYMBOL_VALUE_SAVE_REQUEST".equals(type)) {
                                        SymbolValueSaveRequest request = gson.fromJson(msg, SymbolValueSaveRequest.class);

                                        for (Map.Entry<String, Long> entry : request.getValues().entrySet()) {
                                            switch (request.getMarginType()) {
                                                case "ENTRY" -> symbolTradeSettingDAO.saveEntryMargin(entry.getKey(), entry.getValue());
                                                case "LOSSCUT" -> symbolTradeSettingDAO.saveMaintMargin(entry.getKey(), entry.getValue());
                                                case "OVERNIGHT" -> symbolTradeSettingDAO.saveOvernightMargin(entry.getKey(), entry.getValue());
                                            }
                                        }

                                        TradeLimitSaveResponse response = new TradeLimitSaveResponse(true, "개별설정 저장완료");
                                        ctx.writeAndFlush(gson.toJson(response) + "\n");
                                        System.out.println("[서버] 심볼별 값 설정 저장 완료");

                                    } else if ("SYMBOL_PERMISSION_LOAD_REQUEST".equals(type)) {
                                        List<String> symbols = marketSpecDAO.getOverseasSymbols();
                                        Map<String, Boolean> values = new LinkedHashMap<>();

                                        for (String symbol : symbols) {
                                            values.put(symbol, symbolTradeSettingDAO.getOvernightEnabled(symbol));
                                        }

                                        SymbolPermissionLoadResponse response = new SymbolPermissionLoadResponse(values);
                                        ctx.writeAndFlush(gson.toJson(response) + "\n");

                                    } else if ("SYMBOL_PERMISSION_SAVE_REQUEST".equals(type)) {
                                        SymbolPermissionSaveRequest request = gson.fromJson(msg, SymbolPermissionSaveRequest.class);

                                        for (Map.Entry<String, Boolean> entry : request.getValues().entrySet()) {
                                            symbolTradeSettingDAO.saveOvernightEnabled(entry.getKey(), entry.getValue());
                                        }

                                        TradeLimitSaveResponse response = new TradeLimitSaveResponse(true, "개별 허용설정 저장완료");
                                        ctx.writeAndFlush(gson.toJson(response) + "\n");
                                        System.out.println("[서버] 심볼별 허용 설정 저장 완료");
                                    }








/// //////////////고객 프로그램에서 회사계좌 띄우는거??
                                    else if ("DW_ACCOUNT_INFO_REQUEST".equals(type)) {
                                        new Thread(() -> {
                                            DwAccountInfoRequest req = gson.fromJson(msg, DwAccountInfoRequest.class);
                                            DwAccountInfo info = customerDepositService.loadAccountInfo(req.userId);
                                            ctx.writeAndFlush(gson.toJson(new DwAccountInfoResponse(info)) + "\n");
                                        }).start();
                                    }
/// //////////

                                    /// ////////공지사항관련
                                    else if ("NOTICE_LIST_REQUEST".equals(type)) {
                                        new Thread(() -> {
                                            List<Notice> list = noticeService.getNotices();
                                            ctx.writeAndFlush(gson.toJson(new NoticeListResponse(list)) + "\n"); // 요청자에게만
                                        }).start();
                                    }

                                    else if ("NOTICE_ADD_REQUEST".equals(type)) {
                                        new Thread(() -> {
                                            NoticeAddRequest req = gson.fromJson(msg, NoticeAddRequest.class);
                                            boolean success = noticeService.addNotice(req.title, req.noticeType, req.contentRtf);

                                            ctx.writeAndFlush(gson.toJson(new NoticeAddResult(
                                                    success, success ? "등록 완료" : "등록 실패"
                                            )) + "\n");

                                            if (success) {
                                                SessionManager.broadcastToAdmins(new NoticeChangedEvent());
                                                SessionManager.broadcastToCustomers(new NoticeChangedEvent());
                                            }
                                        }).start();
                                    }

                                    else if ("NOTICE_UPDATE_REQUEST".equals(type)) {
                                        new Thread(() -> {
                                            NoticeUpdateRequest req = gson.fromJson(msg, NoticeUpdateRequest.class);
                                            boolean success = noticeService.updateNotice(req.id, req.title, req.contentRtf, req.noticeType);

                                            ctx.writeAndFlush(gson.toJson(new NoticeUpdateResult(
                                                    success, success ? "수정 완료" : "수정 실패"
                                            )) + "\n");

                                            if (success) {
                                                SessionManager.broadcastToAdmins(new NoticeChangedEvent());
                                                SessionManager.broadcastToCustomers(new NoticeChangedEvent());
                                            }
                                        }).start();
                                    }

                                    else if ("NOTICE_DELETE_REQUEST".equals(type)) {
                                        new Thread(() -> {
                                            NoticeDeleteRequest req = gson.fromJson(msg, NoticeDeleteRequest.class);
                                            boolean success = noticeService.deleteNotice(req.id);

                                            ctx.writeAndFlush(gson.toJson(new NoticeDeleteResult(
                                                    success, success ? "삭제 완료" : "삭제 실패"
                                            )) + "\n");

                                            if (success) {
                                                SessionManager.broadcastToAdmins(new NoticeChangedEvent());
                                                SessionManager.broadcastToCustomers(new NoticeChangedEvent());
                                            }
                                        }).start();
                                    }


                                    else if ("MUST_READ_NOTICE_REQUEST".equals(type)) {
                                        new Thread(() -> {
                                            MustReadNoticeRequest req = gson.fromJson(msg, MustReadNoticeRequest.class);
                                            List<Notice> notices = noticeService.getMustReadNotices(req.userId);

                                            ctx.writeAndFlush(gson.toJson(
                                                    new MustReadNoticeResponse(req.requestId, notices)
                                            ) + "\n");
                                        }).start();
                                    }

                                    else if ("NOTICE_READ_REQUEST".equals(type)) {
                                        new Thread(() -> {
                                            NoticeReadRequest req = gson.fromJson(msg, NoticeReadRequest.class);
                                            boolean success = noticeService.markNoticeRead(req.userId, req.noticeId);

                                            ctx.writeAndFlush(gson.toJson(
                                                    new NoticeReadResponse(req.requestId, success)
                                            ) + "\n");

                                            if (success) {
                                                // 필독공지를 읽었다는 건 목록/상태가 바뀐 거니 관리자 쪽 통계 화면 있으면 여기도 broadcast 가능
                                                // (지금 당장 필요 없으면 생략해도 무방)
                                            }
                                        }).start();
                                    }
                                    /// //////////


                                    /// 환율 관련//////////////
                                 else if ("EXCHANGE_RATE_ALL_REQUEST".equals(type)) {
                                    // 클라이언트 최초 로그인 시 전체 환율 요청
                                    ExchangeRateAllResponse response = new ExchangeRateAllResponse(Store.ExchangeRateCache.getAll());
                                    ctx.writeAndFlush(gson.toJson(response) + "\n");

                                } else if ("EXCHANGE_RATE_UPDATE_REQUEST".equals(type)) {
                                    // 관리자가 환율 변경
                                    ExchangeRateUpdateRequest request = gson.fromJson(msg, ExchangeRateUpdateRequest.class);

                                    String sql = "UPDATE exchange_rates SET rate_to_krw=? WHERE currency=?";
                                    try (Connection conn = db.DBUtil.getConnection();
                                         PreparedStatement ps = conn.prepareStatement(sql)) {
                                        ps.setDouble(1, request.getRate());
                                        ps.setString(2, request.getCurrency());
                                        ps.executeUpdate();

                                        Store.ExchangeRateCache.load();   // 캐시 재로드

                                        // 🔥 접속 중인 모든 고객에게 push
                                        ExchangeRatePushEvent event = new ExchangeRatePushEvent(Store.ExchangeRateCache.getAll());
                                        for (Integer userId : SessionManager.getConnectedCustomerIds()) {
                                            SessionManager.sendToCustomer(userId, event);
                                        }

                                        ctx.writeAndFlush(gson.toJson(new MarketOperationSaveResponse(true, "환율 저장 완료")) + "\n");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        ctx.writeAndFlush(gson.toJson(new MarketOperationSaveResponse(false, "환율 저장 실패")) + "\n");
                                    }
                                }

                                    //////////////


                                    /// ///비번변경 및 사운드설정쪽
                                    else if ("CHANGE_PASSWORD_REQUEST".equals(type)) {
                                        new Thread(() -> {
                                            ChangePasswordRequest req = gson.fromJson(msg, ChangePasswordRequest.class);

                                            int result = userService.changePassword(req.userId, req.currentPw, req.newPw);

                                            String message = switch (result) {
                                                case 0 -> "비밀번호가 변경되었습니다.";
                                                case -1 -> "현재 비밀번호가 일치하지 않습니다.";
                                                case -2 -> "새 비밀번호는 4자 이상이어야 합니다.";
                                                default -> "알 수 없는 오류가 발생했습니다.";
                                            };

                                            ctx.writeAndFlush(gson.toJson(new ChangePasswordResponse(result, message)) + "\n");
                                        }).start();
                                    }
                                    else if ("SOUND_SETTING_LOAD_REQUEST".equals(type)) {
                                        new Thread(() -> {
                                            SoundSettingLoadRequest req = gson.fromJson(msg, SoundSettingLoadRequest.class);

                                            SoundSetting setting = soundSettingDAO.load(req.userId);

                                            ctx.writeAndFlush(gson.toJson(new SoundSettingLoadResponse(setting)) + "\n");
                                        }).start();
                                    }
                                    else if ("SOUND_SETTING_SAVE_REQUEST".equals(type)) {
                                        new Thread(() -> {
                                            SoundSettingSaveRequest req = gson.fromJson(msg, SoundSettingSaveRequest.class);

                                            boolean success = soundSettingDAO.save(req.userId, req.setting);

                                            String message = success ? "저장되었습니다." : "저장 중 오류가 발생했습니다.";

                                            ctx.writeAndFlush(gson.toJson(new SoundSettingSaveResponse(success, message)) + "\n");
                                        }).start();
                                    }
                                    /// ////////////////////////////////

                                else if ("PENDING_ORDERS_REQUEST".equals(type)) {
                                    PendingOrdersRequest request = gson.fromJson(msg, PendingOrdersRequest.class);
                                    List<Order> orders = orderDAO.getMyPendingOrders(request.getUserId(), request.getSymbol());   // 서버 OrderDAO 재사용
                                    PendingOrdersResponse response = new PendingOrdersResponse(orders);
                                    ctx.writeAndFlush(gson.toJson(response) + "\n");
                                    } else if ("POSITION_PANEL_DATA_REQUEST".equals(type)) {
                                        PositionPanelDataRequest request = gson.fromJson(msg, PositionPanelDataRequest.class);
                                        List<model.PositionRow> positions = positionService.loadPositionRows(request.getUserId());
                                        List<model.PendingOrderRow> pending = orderDAO.loadPendingOrderRows(request.getUserId());
                                        ctx.writeAndFlush(gson.toJson(new PositionPanelDataResponse(positions, pending)) + "\n");
                                    } else if ("POSITION_REQUEST".equals(type)) {
                                        PositionRequest request = gson.fromJson(msg, PositionRequest.class);
                                        Position pos = positionService.getPosition(request.getUserId(), request.getSymbol());
                                        ctx.writeAndFlush(gson.toJson(new PositionResponse(request.getRequestId(), pos)) + "\n");
                                    } else if ("ALL_POSITIONS_REQUEST".equals(type)) {
                                        AllPositionsRequest request = gson.fromJson(msg, AllPositionsRequest.class);
                                        List<Position> positions = positionService.getAllPositions(request.getUserId());
                                        ctx.writeAndFlush(gson.toJson(new AllPositionsResponse(request.getRequestId(), positions)) + "\n");
                                    } else if ("POSITION_BY_ID_REQUEST".equals(type)) {
                                        PositionByIdRequest request = gson.fromJson(msg, PositionByIdRequest.class);
                                        Position pos = positionService.getPositionById(request.getId());
                                        ctx.writeAndFlush(gson.toJson(new PositionByIdResponse(request.getRequestId(), pos)) + "\n");
                                    }  else if ("ADMIN_ALL_POSITIONS_REQUEST".equals(type)) {
                                        new Thread(() -> {
                                            server.AdminAllPositionsResponse response = adminPositionAggregateService.computeAll();
                                            SessionManager.broadcastToAdmins(response);
                                        }).start();
                                    }
                                    else if ("ADMIN_LIQUIDATE_POSITION_REQUEST".equals(type)) {
                                        new Thread(() -> {
                                            server.AdminLiquidatePositionRequest request =
                                                    gson.fromJson(msg, server.AdminLiquidatePositionRequest.class);

                                            boolean ok = liquidatePosition(request.getUserId(), request.getSymbol());

                                            server.AdminActionResult result = new server.AdminActionResult(
                                                    ok, ok ? "청산 완료" : "청산 실패 (해당 포지션이 없습니다)"
                                            );
                                            ctx.writeAndFlush(gson.toJson(result) + "\n");

                                            SessionManager.broadcastToAdmins(adminPositionAggregateService.computeAll());
                                        }).start();
                                    }
                                    else if ("ADMIN_LIQUIDATE_ALL_REQUEST".equals(type)) {
                                        new Thread(() -> {
                                            int count = 0;
                                            for (model.AdminPositionRow row : adminPositionAggregateService.computeAll().getPositions()) {
                                                if (liquidatePosition(row.getUserId(), row.getSymbol())) count++;
                                            }

                                            server.AdminActionResult result =
                                                    new server.AdminActionResult(true, count + "건 전체청산 완료");
                                            ctx.writeAndFlush(gson.toJson(result) + "\n");

                                            SessionManager.broadcastToAdmins(adminPositionAggregateService.computeAll());
                                        }).start();
                                    }
                                    else if ("ADMIN_CANCEL_ALL_ORDERS_REQUEST".equals(type)) {
                                        new Thread(() -> {
                                            List<model.AdminUserListRow> customers = adminUserListService.loadCustomers("");
                                            for (model.AdminUserListRow c : customers) {
                                                orderDAO.cancelAll(c.getId());
                                                SessionManager.sendEventToCustomer(
                                                        c.getId(),
                                                        new ClientEventMessage("PENDING_ORDER_CHANGED", null, "ORDER_CANCELLED")
                                                );
                                            }

                                            server.AdminActionResult result =
                                                    new server.AdminActionResult(true, "전체 미체결 취소 완료");
                                            ctx.writeAndFlush(gson.toJson(result) + "\n");

                                            SessionManager.broadcastToAdmins(adminPositionAggregateService.computeAll());
                                        }).start();
                                    }






                                    else if ("CHART_HISTORY_REQUEST".equals(type)) {
                                        ChartHistoryRequest request = gson.fromJson(msg, ChartHistoryRequest.class);
                                        List<Map<String, Object>> candles = chartService.getChartData(request.symbol, TimeFrame.valueOf(request.timeFrame));
                                        ChartHistoryResponse response = new ChartHistoryResponse(request.symbol, request.timeFrame, candles);
                                        ctx.writeAndFlush(gson.toJson(response) + "\n");

                                    } else if ("CHART_SUBSCRIBE_REQUEST".equals(type)) {
                                        ChartSubscribeRequest request = gson.fromJson(msg, ChartSubscribeRequest.class);
                                        SessionManager.setChartSubscription(request.userId, request.symbol, request.timeFrame);
                                    }else if ("CHART_UNSUBSCRIBE_REQUEST".equals(type)) {
                                        ChartUnsubscribeRequest request = gson.fromJson(msg, ChartUnsubscribeRequest.class);
                                        SessionManager.removeChartSubscription(request.userId);
                                    }

                                    else if ("OVERNIGHT_PREVIEW_REQUEST".equals(type)) {
                                        OvernightPreviewRequest request = gson.fromJson(msg, OvernightPreviewRequest.class);
                                        OvernightInfo info = OvernightProcessorHolder.get().preview(request.userId);
                                        OvernightPreviewResponse response = new OvernightPreviewResponse(info);
                                        ctx.writeAndFlush(gson.toJson(response) + "\n");
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


    // 🔥 관리자 개별/전체청산에서 공용으로 쓰는 청산 헬퍼
    // ⚠️ pos.getSide()가 String을 반환하는지 model.OrderSide를 반환하는지 몰라서
    //    양쪽 다 되게 .toString() 거쳐서 다시 valueOf 하는 방식으로 짰습니다.
    //    이미 model.OrderSide를 반환한다면 이 두 줄은 그냥 pos.getSide() 하나로 줄이셔도 됩니다.
    private static boolean liquidatePosition(int userId, String symbol) {
        Position pos = positionService.getPosition(userId, symbol);
        if (pos == null || pos.getQty() == 0) return false;

        model.OrderSide currentSide = pos.isLong() ? model.OrderSide.BUY : model.OrderSide.SELL;
        model.OrderSide closingSide = (currentSide == model.OrderSide.BUY)
                ? model.OrderSide.SELL
                : model.OrderSide.BUY;

        double currentPrice = Store.PriceStore.getLast(symbol);

        int orderId = orderExecutionService.executeMarket(
                userId, symbol, closingSide, (int) pos.getQty(), currentPrice,
                false, 0, false, 0
        );

        return orderId > 0;
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