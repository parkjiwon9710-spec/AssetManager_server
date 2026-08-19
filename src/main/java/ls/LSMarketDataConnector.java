package ls;

import Market.QuoteUpdateListener;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
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

public class LSMarketDataConnector {

    // 🔥 모의투자 포트. 실전 전환 시 9443으로 변경
    private static final int WS_PORT = 29443;
    private static final String WS_HOST = "openapi.ls-sec.co.kr";

    // symbol: LS tr_key (예: "HSIU26"), internalSymbol: 우리 시스템 심볼 (예: "HSI")
    public static void connect(String accessToken, String symbol, String internalSymbol, QuoteUpdateListener listener) {

        Thread thread = new Thread(() -> {
            try {
                runConnection(accessToken, symbol, internalSymbol, listener);
            } catch (Exception e) {
                System.err.println("[LS] " + internalSymbol + " 웹소켓 연결 실패");
                e.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.setName("LS-WS-" + internalSymbol);
        thread.start();
    }

    private static void runConnection(String accessToken, String symbol, String internalSymbol, QuoteUpdateListener listener) throws Exception {

        URI uri = URI.create("wss://" + WS_HOST + ":" + WS_PORT + "/websocket");
        SslContext sslContext = SslContextBuilder.forClient().build();
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(sslContext.newHandler(ch.alloc(), uri.getHost(), WS_PORT));
                            ch.pipeline().addLast(new HttpClientCodec());
                            ch.pipeline().addLast(new HttpObjectAggregator(65536));

                            WebSocketClientHandshaker handshaker =
                                    WebSocketClientHandshakerFactory.newHandshaker(
                                            uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders());

                            ch.pipeline().addLast(new WebSocketClientProtocolHandler(handshaker));
                            ch.pipeline().addLast(new LSWebSocketHandler(accessToken, symbol, internalSymbol, listener));
                        }
                    });

            System.out.println("[LS] " + internalSymbol + "(" + symbol + ") 웹소켓 연결 시도...");

            ChannelFuture connectFuture = bootstrap.connect(uri.getHost(), WS_PORT);
            connectFuture.addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    System.out.println("[LS] " + internalSymbol + " TCP 연결 성공, SSL/핸드셰이크 진행 중...");
                } else {
                    System.err.println("[LS] " + internalSymbol + " TCP 연결 실패");
                    future.cause().printStackTrace();
                }
            });

            Channel channel = connectFuture.sync().channel();
            channel.closeFuture().sync();

        } finally {
            group.shutdownGracefully();
        }
    }
}