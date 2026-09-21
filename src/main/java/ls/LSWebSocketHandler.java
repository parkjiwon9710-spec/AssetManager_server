package ls;

import Market.OvhQuoteParser;
import Market.OvcTradeParser;
import Market.QuoteUpdateListener;
import Market.TradeUpdateListener;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class LSWebSocketHandler
        extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private final String accessToken;
    private final String symbol;          // LS tr_key, 예: "HSIQ26"
    private final String internalSymbol;  // 우리 시스템 심볼, 예: "HSI"
    private final QuoteUpdateListener quoteListener;   // OVH(호가) 콜백
    private final TradeUpdateListener tradeListener;   // OVC(체결) 콜백 - 🔥 신규

    public LSWebSocketHandler(
            String accessToken,
            String symbol,
            String internalSymbol,
            QuoteUpdateListener quoteListener,
            TradeUpdateListener tradeListener   // 🔥 신규 파라미터
    ) {
        this.accessToken = accessToken;
        this.symbol = symbol;
        this.internalSymbol = internalSymbol;
        this.quoteListener = quoteListener;
        this.tradeListener = tradeListener;
    }

    // =========================================================
    // TCP/WebSocket 연결 완료
    // =========================================================

    @Override
    public void channelActive(
            ChannelHandlerContext ctx
    ) {
        System.out.println("WebSocket channel active");
    }

    // =========================================================
    // WebSocket Handshake 완료 / SSL 핸드셰이크 완료
    // =========================================================

    @Override
    public void userEventTriggered(
            ChannelHandlerContext ctx,
            Object evt
    ) throws Exception {

        if (evt instanceof io.netty.handler.ssl.SslHandshakeCompletionEvent sslEvent) {
            if (sslEvent.isSuccess()) {
                System.out.println("[LS] SSL 핸드셰이크 성공");
            } else {
                System.err.println("[LS] SSL 핸드셰이크 실패");
                sslEvent.cause().printStackTrace();
            }
        }

        if (evt instanceof
                io.netty.handler.codec.http.websocketx
                        .WebSocketClientProtocolHandler
                        .ClientHandshakeStateEvent event) {

            if (event ==
                    io.netty.handler.codec.http.websocketx
                            .WebSocketClientProtocolHandler
                            .ClientHandshakeStateEvent
                            .HANDSHAKE_COMPLETE) {

                System.out.println();
                System.out.println("==============================");
                System.out.println("WebSocket Handshake 성공");
                System.out.println("==============================");

                System.out.println();
                System.out.println("OVH/OVC 구독 요청");
                System.out.println("종목 : " + symbol);
                System.out.println();

                // 🔥 호가(OVH) + 체결(OVC) 둘 다 구독
                subscribe(ctx, "OVH");
                subscribe(ctx, "OVC");
            }
        }

        super.userEventTriggered(ctx, evt);
    }

    // =========================================================
    // OVH/OVC 실시간 구독 (공통화)
    // =========================================================

    private void subscribe(ChannelHandlerContext ctx, String trCd) {

        String trKey = String.format("%-8s", symbol);

        String request =
                "{"
                        + "\"header\":{"
                        + "\"token\":\"" + accessToken + "\","
                        + "\"tr_type\":\"3\""
                        + "},"
                        + "\"body\":{"
                        + "\"tr_cd\":\"" + trCd + "\","
                        + "\"tr_key\":\"" + trKey + "\""
                        + "}"
                        + "}";

        System.out.println();
        System.out.println("===== " + trCd + " 구독 요청 =====");
        System.out.println("종목 원본 : [" + symbol + "]");
        System.out.println("tr_key    : [" + trKey + "]");
        System.out.println("요청      : " + request);
        System.out.println("========================");
        System.out.println();

        ctx.writeAndFlush(
                new io.netty.handler.codec.http.websocketx.TextWebSocketFrame(request)
        );
    }

    // =========================================================
    // LS증권 WebSocket 메시지 수신
    // =========================================================

    @Override
    protected void channelRead0(
            ChannelHandlerContext ctx,
            TextWebSocketFrame frame
    ) {
        String message = frame.text();

        System.out.println("===== LS증권 수신 =====");
        System.out.println(message);
        System.out.println("======================");
        System.out.println();

        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            JsonObject header = json.getAsJsonObject("header");

            String trCd = header.has("tr_cd") ? header.get("tr_cd").getAsString() : null;

            if (!json.has("body") || json.get("body").isJsonNull()) {
                // 구독 등록 확인 응답 등 (rsp_cd만 있고 body 없음) - 무시
                return;
            }

            JsonObject body = json.getAsJsonObject("body");

            if ("OVH".equals(trCd)) {
                OvhQuoteParser.Result result = OvhQuoteParser.parse(body);
                if (quoteListener != null) {
                    quoteListener.onQuoteUpdate(internalSymbol, result);
                }
            } else if ("OVC".equals(trCd)) {
                OvcTradeParser.Result result = OvcTradeParser.parse(body);
                if (tradeListener != null) {
                    tradeListener.onTradeUpdate(internalSymbol, result);
                }
            }
            // 그 외 tr_cd는 무시

        } catch (Exception e) {
            System.err.println("[LS] 메시지 파싱 실패: " + message);
            e.printStackTrace();
        }
    }

    // =========================================================
    // 연결 종료
    // =========================================================

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("[LS] WebSocket 연결 종료 - " + java.time.LocalDateTime.now());
    }

    // =========================================================
    // 예외
    // =========================================================

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("[LS] WebSocket 오류 - " + java.time.LocalDateTime.now());
        cause.printStackTrace();
        ctx.close();
    }
}