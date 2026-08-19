package server;

import com.google.gson.Gson;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SessionManager {

    private static final Gson gson = new Gson();

    // userId -> 연결
    private static final Map<Integer, ChannelHandlerContext> customerConnections = new ConcurrentHashMap<>();
    private static final Map<Integer, ChannelHandlerContext> proxyConnections = new ConcurrentHashMap<>(); // 🔥 추가 - 관리자 대리접속 전용, customerConnections와 절대 안 섞임
    private static final Map<Integer, ChannelHandlerContext> adminConnections = new ConcurrentHashMap<>();

    // userId -> 세션 정보 (고객만 대상, 관리자는 목록에 안 보여줄 것이므로)
    private static final Map<Integer, SessionInfo> customerSessionInfo = new ConcurrentHashMap<>();

    public static final AttributeKey<Integer> USER_ID_KEY = AttributeKey.valueOf("userId");
    public static final AttributeKey<String> ROLE_KEY = AttributeKey.valueOf("role");
    public static final AttributeKey<Boolean> IS_PROXY_KEY = AttributeKey.valueOf("isProxy"); // 🔥 추가

    public static boolean register(int userId, String username, String name, String mac, String role, ChannelHandlerContext ctx) {
        return register(userId, username, name, mac, role, ctx, false);
    }

    // 🔥 관리자 대리접속용 오버로드 (isProxy=true면 이중접속 체크 스킵 + 별도 맵에 등록)
    public static boolean register(int userId, String username, String name, String mac, String role, ChannelHandlerContext ctx, boolean isProxy) {

        if (!"ADMIN".equals(role) && !isProxy) {
            // 🔥 이중접속 방지: 기존 세션 있으면 새 로그인 거부 (기존 세션은 건드리지 않음)
            ChannelHandlerContext existing = customerConnections.get(userId);
            if (existing != null && existing.channel().isActive()) {
                return false;
            }
        }

        ctx.channel().attr(USER_ID_KEY).set(userId);
        ctx.channel().attr(ROLE_KEY).set(role);
        ctx.channel().attr(IS_PROXY_KEY).set(isProxy); // 🔥 추가

        String ip = ctx.channel().remoteAddress().toString().replace("/", "").split(":")[0];

        if ("ADMIN".equals(role)) {
            adminConnections.put(userId, ctx);
            System.out.println("[세션] 관리자 등록 - userId: " + userId + ", 현재 관리자 접속자 수: " + adminConnections.size());
            sendSessionListTo(ctx);
        } else if (isProxy) {
            // 🔥 대리접속은 별도 맵에만 등록, customerConnections/customerSessionInfo는 절대 건드리지 않음
            proxyConnections.put(userId, ctx);
            System.out.println("[세션] 관리자 대리접속 등록 - userId: " + userId + ", 현재 대리접속 수: " + proxyConnections.size());
        } else {
            customerConnections.put(userId, ctx);
            customerSessionInfo.put(userId, new SessionInfo(userId, username, name, ip, mac, System.currentTimeMillis()));
            System.out.println("[세션] 고객 등록 - userId: " + userId + ", 현재 고객 접속자 수: " + customerConnections.size());
            broadcastSessionListToAdmins();
        }
        return true;
    }

    public static void unregister(ChannelHandlerContext ctx) {
        Integer userId = ctx.channel().attr(USER_ID_KEY).get();
        String role = ctx.channel().attr(ROLE_KEY).get();
        Boolean isProxy = ctx.channel().attr(IS_PROXY_KEY).get(); // 🔥 추가

        if (userId == null) return;

        if ("ADMIN".equals(role)) {
            adminConnections.remove(userId);
            System.out.println("[세션] 관리자 해제 - userId: " + userId + ", 현재 관리자 접속자 수: " + adminConnections.size());
        } else if (Boolean.TRUE.equals(isProxy)) {
            // 🔥 대리접속 종료 - 고객의 진짜 세션(customerConnections)은 절대 건드리지 않음
            proxyConnections.remove(userId, ctx);
            System.out.println("[세션] 관리자 대리접속 해제 - userId: " + userId + ", 현재 대리접속 수: " + proxyConnections.size());
        } else {
            // 🔥 이 ctx가 현재 맵에 등록된 채널일 때만 제거 (레이스 컨디션 방지)
            customerConnections.remove(userId, ctx);
            if (customerConnections.get(userId) == null) {
                customerSessionInfo.remove(userId);
            }
            chartSubscriptions.remove(userId);
            for (Map<Integer, Integer> counts : symbolSubscriberCounts.values()) {
                counts.remove(userId);
            }
            System.out.println("[세션] 고객 해제 - userId: " + userId + ", 현재 고객 접속자 수: " + customerConnections.size());
            broadcastSessionListToAdmins();
        }
    }




    private static final Map<String, ProxyTokenInfo> proxyTokens = new ConcurrentHashMap<>();

    private static class ProxyTokenInfo {
        int userId;
        long expiresAt;
        ProxyTokenInfo(int userId, long expiresAt) {
            this.userId = userId;
            this.expiresAt = expiresAt;
        }
    }

    public static String issueProxyToken(int customerUserId) {
        String token = java.util.UUID.randomUUID().toString();
        proxyTokens.put(token, new ProxyTokenInfo(customerUserId, System.currentTimeMillis() + 30_000));
        return token;
    }

    public static Integer consumeProxyToken(String token) {
        ProxyTokenInfo info = proxyTokens.remove(token);
        if (info == null || info.expiresAt < System.currentTimeMillis()) return null;
        return info.userId;
    }



    public static ChannelHandlerContext getCustomer(int userId) {
        return customerConnections.get(userId);
    }

    // 🔥 새 헬퍼 - 실제 고객 세션 + 관리자 대리접속 세션 둘 다 반환 (push용)
    private static List<ChannelHandlerContext> getAllContextsForUser(int userId) {
        List<ChannelHandlerContext> list = new java.util.ArrayList<>();
        ChannelHandlerContext real = customerConnections.get(userId);
        if (real != null) list.add(real);
        ChannelHandlerContext proxy = proxyConnections.get(userId);
        if (proxy != null) list.add(proxy);
        return list;
    }

    // 현재 접속중인 고객 목록을 모든 관리자에게 전송    무조건 "고객 접속자 리스트"만 포장해서 보내는 전용 메서드
    private static void broadcastSessionListToAdmins() {
        List<SessionInfo> list = customerSessionInfo.values().stream().collect(Collectors.toList());
        SessionListMessage message = new SessionListMessage(list);
        String json = gson.toJson(message);

        for (ChannelHandlerContext adminCtx : adminConnections.values()) {
            adminCtx.writeAndFlush(json + "\n");
        }
    }
    // 접속중인 관리자 전원한테 전송       뭘 보낼지 호출하는 쪽이 정하는 범용 배달 메서드 (실시간손익, 입출금목록 등 다 이거 재사용)
    public static void broadcastToAdmins(Object message) {
        String json = gson.toJson(message);
        for (ChannelHandlerContext adminCtx : adminConnections.values()) {
            adminCtx.writeAndFlush(json + "\n");
        }
    }
    //접속중인 고객 전원한테??
    public static void broadcastToCustomers(Object message) {
        String json = gson.toJson(message);
        for (ChannelHandlerContext ctx : customerConnections.values()) {
            ctx.writeAndFlush(json + "\n");
        }
    }
    /// ///////////////////////////////주문창 맨 위 리프레쉬용도 담보금같은거////////////
//접속중인 유저들
    public static java.util.Set<Integer> getConnectedCustomerIds() {
        // 🔥 정기 스케줄러(TopInfo, PositionPanel 등)가 대리접속만 있는 유저도 챙기도록 합침
        java.util.Set<Integer> all = new java.util.HashSet<>(customerConnections.keySet());
        all.addAll(proxyConnections.keySet());
        return all;
    }
    //특정고객한명에게 임의메세지를 보내는 범용메서드
    public static void sendToCustomer(int userId, Object message) {
        String json = gson.toJson(message);
        List<ChannelHandlerContext> targets = getAllContextsForUser(userId);
        System.out.println("[DEBUG] sendToCustomer userId=" + userId + ", 대상 채널 수=" + targets.size());
        for (ChannelHandlerContext ctx : getAllContextsForUser(userId)) { // 🔥 수정 - 실제세션 + 대리접속 둘 다
            ctx.writeAndFlush(json + "\n");
        }
    }
    /// //////////////////////////////////////////

    /// //////////////////////////////
    public static void sendSessionListTo(ChannelHandlerContext ctx) {
        List<SessionInfo> list = customerSessionInfo.values().stream().collect(Collectors.toList());
        SessionListMessage message = new SessionListMessage(list);
        ctx.writeAndFlush(gson.toJson(message) + "\n");
    }

    public static void sendEventToCustomer(int userId, ClientEventMessage event) {
        String json = new Gson().toJson(event);
        for (ChannelHandlerContext ctx : getAllContextsForUser(userId)) { // 🔥 수정
            ctx.writeAndFlush(json + "\n");
        }
    }
/// //////////////////////////////////


    /// ///////////////특정관리자1명에게 개별전송   캡쳐때 쓰일듯
    public static ChannelHandlerContext getAdmin(int userId) {
        return adminConnections.get(userId);
    }

    public static void sendToAdmin(int userId, Object message) {
        ChannelHandlerContext ctx = adminConnections.get(userId);
        if (ctx != null) {
            ctx.writeAndFlush(gson.toJson(message) + "\n");
        }
    }
    /// ////////////////





    ////구독관련, 고객이 종목 화면을 열 때 → 서버에 "NASDAQ 구독할게" 알림
    /// 고객이 종목을 바꾸거나 창을 닫을 때 → "NASDAQ 구독취소, HSI 구독할게"
    /// 서버는 tick마다 → "NASDAQ을 구독 중인 고객 목록"에게만 broadcast
    // 🔥 새 필드 - symbol -> (userId -> 구독 중인 창 개수)
    private static final Map<String, Map<Integer, Integer>> symbolSubscriberCounts = new ConcurrentHashMap<>();

    public static void subscribe(int userId, String symbol) {
        int count = symbolSubscriberCounts
                .computeIfAbsent(symbol, s -> new ConcurrentHashMap<>())
                .merge(userId, 1, Integer::sum);
        System.out.println("[구독] " + symbol + " userId=" + userId + " count=" + count);
    }

    public static void unsubscribe(int userId, String symbol) {
        Map<Integer, Integer> counts = symbolSubscriberCounts.get(symbol);
        if (counts == null) return;

        counts.computeIfPresent(userId, (uid, count) -> {
            int newCount = count - 1;
            System.out.println("[구독취소] " + symbol + " userId=" + userId + " count=" + newCount);
            return newCount > 0 ? newCount : null;
        });
    }
    public static void broadcastToSubscribers(String symbol, Object message) {
        Map<Integer, Integer> counts = symbolSubscriberCounts.get(symbol);
        if (counts == null || counts.isEmpty()) {
//            System.out.println("[서버] " + symbol + " 구독자 없음, 스킵");
            return;
        }
        System.out.println("[서버] " + symbol + " 구독자 " + counts.size() + "명에게 전송");
        String json = new Gson().toJson(message);
        for (Integer userId : counts.keySet()) {
            for (ChannelHandlerContext ctx : getAllContextsForUser(userId)) { // 🔥 수정
                ctx.writeAndFlush(json + "\n");
            }
        }
    }





    /////////////// 🔥 차트 전용 구독 - 고객당 차트창 1개, 종목+시간봉 하나만 추적
    private static class ChartSubscription {
        String symbol;
        String timeFrame;
        ChartSubscription(String symbol, String timeFrame) {
            this.symbol = symbol;
            this.timeFrame = timeFrame;
        }
    }

    private static final Map<Integer, ChartSubscription> chartSubscriptions = new ConcurrentHashMap<>();

    public static void setChartSubscription(int userId, String symbol, String timeFrame) {
        chartSubscriptions.put(userId, new ChartSubscription(symbol, timeFrame));
    }
    public static void removeChartSubscription(int userId) {
        chartSubscriptions.remove(userId);
    }


    public static List<Integer> getChartSubscribers(String symbol, String timeFrame) {
        List<Integer> result = new java.util.ArrayList<>();
        for (Map.Entry<Integer, ChartSubscription> entry : chartSubscriptions.entrySet()) {
            ChartSubscription sub = entry.getValue();
            if (symbol.equals(sub.symbol) && timeFrame.equals(sub.timeFrame)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }
    ///////////////
}