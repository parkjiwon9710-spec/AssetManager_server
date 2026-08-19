package ls;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;

import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import java.net.URI;

public class LSWebSocketTest {

    // =========================================================
    // LS증권 운영 WebSocket
    // =========================================================

    private static final String WS_URL =
            "wss://openapi.ls-sec.co.kr:29443/websocket";

    // =========================================================
    // 테스트할 종목
    // =========================================================
    //
    // REST 마스터 조회에서 확인한 항셍 선물
    //
    // HSIQ26
    // Hang Seng (2026.08)
    //
    // =========================================================

    private static final String SYMBOL =
            "HSIU26";

    // =========================================================
    // 환경변수
    // =========================================================

    private static final String APP_KEY =
            System.getenv("LS_APP_KEY");

    private static final String APP_SECRET =
            System.getenv("LS_APP_SECRET");

    // =========================================================
    // MAIN
    // =========================================================

    public static void main(String[] args)
            throws Exception {

        // -----------------------------------------------------
        // 1. 환경변수 확인
        // -----------------------------------------------------

        if (APP_KEY == null || APP_KEY.isBlank()) {

            throw new IllegalStateException(
                    "LS_APP_KEY 환경변수가 없습니다."
            );
        }

        if (APP_SECRET == null || APP_SECRET.isBlank()) {

            throw new IllegalStateException(
                    "LS_APP_SECRET 환경변수가 없습니다."
            );
        }

        System.out.println();
        System.out.println("========================================");
        System.out.println("LS증권 해외선물 WebSocket 테스트");
        System.out.println("========================================");
        System.out.println("환경       : 운영");
        System.out.println("WebSocket  : " + WS_URL);
        System.out.println("종목       : " + SYMBOL);
        System.out.println("TR         : OVH");
        System.out.println("========================================");
        System.out.println();

        // -----------------------------------------------------
        // 2. Access Token 발급
        // -----------------------------------------------------

        String accessToken = getAccessToken();

        System.out.println("Access Token 발급 성공");
        System.out.println(
                "Token 앞부분 : "
                        + accessToken.substring(
                        0,
                        Math.min(
                                20,
                                accessToken.length()
                        )
                )
                        + "..."
        );

        System.out.println();

        // -----------------------------------------------------
        // 3. WebSocket URI
        // -----------------------------------------------------

        URI uri = URI.create(WS_URL);

        // -----------------------------------------------------
        // 4. SSL
        // -----------------------------------------------------

        SslContext sslContext =
                SslContextBuilder
                        .forClient()
                        .build();

        // -----------------------------------------------------
        // 5. Netty EventLoop
        // -----------------------------------------------------

        EventLoopGroup group =
                new NioEventLoopGroup();

        try {

            Bootstrap bootstrap =
                    new Bootstrap();

            bootstrap
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .option(
                            ChannelOption.SO_KEEPALIVE,
                            true
                    )
                    .handler(
                            new ChannelInitializer<SocketChannel>() {

                                @Override
                                protected void initChannel(
                                        SocketChannel ch
                                ) throws Exception {

                                    // --------------------------------
                                    // SSL
                                    // --------------------------------

                                    ch.pipeline().addLast(
                                            sslContext.newHandler(
                                                    ch.alloc(),
                                                    uri.getHost(),
                                                    29443
                                            )
                                    );

                                    // --------------------------------
                                    // HTTP
                                    // --------------------------------

                                    ch.pipeline().addLast(
                                            new HttpClientCodec()
                                    );

                                    ch.pipeline().addLast(
                                            new HttpObjectAggregator(
                                                    65536
                                            )
                                    );

                                    // --------------------------------
                                    // WebSocket Handshaker
                                    // --------------------------------

                                    WebSocketClientHandshaker handshaker =
                                            WebSocketClientHandshakerFactory
                                                    .newHandshaker(
                                                            uri,
                                                            WebSocketVersion.V13,
                                                            null,
                                                            true,
                                                            new DefaultHttpHeaders()
                                                    );

                                    // --------------------------------
                                    // WebSocket Protocol Handler
                                    // --------------------------------

                                    ch.pipeline().addLast(
                                            new WebSocketClientProtocolHandler(
                                                    handshaker
                                            )
                                    );

                                    // --------------------------------
                                    // LS증권 메시지 Handler
                                    // --------------------------------

                                    ch.pipeline().addLast(
                                            new LSWebSocketHandler(
                                                    accessToken,
                                                    SYMBOL,
                                                    "HSI",                                  // internalSymbol (테스트용)
                                                    (internalSymbol, result) -> {
                                                        System.out.println("[테스트] 파싱 결과 - bestBid: " + result.bestBid + ", bestAsk: " + result.bestAsk);
                                                    }
                                            )
                                    );
                                }
                            });

            // -----------------------------------------------------
            // 6. LS증권 WebSocket 서버 연결
            // -----------------------------------------------------

            System.out.println(
                    "WebSocket 서버 연결 시도..."
            );

            Channel channel =
                    bootstrap
                            .connect(
                                    uri.getHost(),
                                    29443
                            )
                            .sync()
                            .channel();

            System.out.println();
            System.out.println(
                    "WebSocket 서버 연결 성공"
            );

            System.out.println(
                    "호가 수신 대기중..."
            );

            System.out.println();

            // -----------------------------------------------------
            // 프로그램 종료 방지
            // -----------------------------------------------------

            channel
                    .closeFuture()
                    .sync();

        } finally {

            group.shutdownGracefully();
        }
    }

    // =========================================================
    // Access Token 발급
    // =========================================================

    private static String getAccessToken()
            throws Exception {

        java.net.http.HttpClient client =
                java.net.http.HttpClient
                        .newHttpClient();

        // -----------------------------------------------------
        // POST Body
        // -----------------------------------------------------

        String form =
                "grant_type=client_credentials"
                        + "&appkey=" + APP_KEY
                        + "&appsecretkey=" + APP_SECRET
                        + "&scope=oob";

        // -----------------------------------------------------
        // HTTP Request
        // -----------------------------------------------------

        java.net.http.HttpRequest request =
                java.net.http.HttpRequest
                        .newBuilder()
                        .uri(
                                URI.create(
                                        "https://openapi.ls-sec.co.kr:8080/oauth2/token"
                                )
                        )
                        .header(
                                "Content-Type",
                                "application/x-www-form-urlencoded"
                        )
                        .POST(
                                java.net.http.HttpRequest
                                        .BodyPublishers
                                        .ofString(form)
                        )
                        .build();

        // -----------------------------------------------------
        // HTTP Request 실행
        // -----------------------------------------------------

        java.net.http.HttpResponse<String> response =
                client.send(
                        request,
                        java.net.http.HttpResponse
                                .BodyHandlers
                                .ofString()
                );

        // -----------------------------------------------------
        // HTTP 상태 확인
        // -----------------------------------------------------

        if (response.statusCode() != 200) {

            throw new RuntimeException(
                    "Access Token 발급 실패\n"
                            + "HTTP Status: "
                            + response.statusCode()
                            + "\n"
                            + response.body()
            );
        }

        // -----------------------------------------------------
        // JSON 파싱
        // -----------------------------------------------------

        com.google.gson.JsonObject json =
                com.google.gson.JsonParser
                        .parseString(
                                response.body()
                        )
                        .getAsJsonObject();

        // -----------------------------------------------------
        // access_token 확인
        // -----------------------------------------------------

        if (!json.has("access_token")) {

            throw new RuntimeException(
                    "응답에 access_token이 없습니다.\n"
                            + response.body()
            );
        }

        return json
                .get("access_token")
                .getAsString();
    }
}