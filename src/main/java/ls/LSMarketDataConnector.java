package ls;

import Market.QuoteUpdateListener;
import Market.TradeUpdateListener;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class LSMarketDataConnector {

    private static final int WS_PORT = 29443;
    private static final String WS_HOST = "openapi.ls-sec.co.kr";

    // 🔥 심볼별 연결 핸들 - 재연결/심볼전환을 위해 상태 보관
    private static class ConnectionHandle {
        volatile String trKey;
        volatile Channel currentChannel;
    }

    private static final ConcurrentHashMap<String, ConnectionHandle> handles = new ConcurrentHashMap<>();

    public static void connect(
            String accessToken, String trKey, String internalSymbol,
            QuoteUpdateListener quoteListener, TradeUpdateListener tradeListener
    ) {
        ConnectionHandle handle = new ConnectionHandle();
        handle.trKey = trKey;
        handles.put(internalSymbol, handle);

        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    String freshToken = ls.LSAuth.getAccessToken();
                    runConnection(freshToken, handle, internalSymbol, quoteListener, tradeListener);
                } catch (Exception e) {
                    System.err.println("[LS] " + internalSymbol + " 웹소켓 연결 실패/종료 - 5초 후 재연결 시도");
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {}
            }
        });
        thread.setDaemon(true);
        thread.setName("LS-WS-" + internalSymbol);
        thread.start();
    }

    // 🔥 신규 - 이월 시 새 tr_key로 전환. 기존 연결을 강제로 끊어서 재연결 루프가 새 tr_key로 다시 붙게 함
    public static void switchSymbol(String internalSymbol, String newTrKey) {
        ConnectionHandle handle = handles.get(internalSymbol);
        if (handle == null) {
            System.err.println("[LS] " + internalSymbol + " 연결 핸들 없음 - switchSymbol 무시");
            return;
        }
        handle.trKey = newTrKey;
        System.out.println("[LS] " + internalSymbol + " tr_key 전환: " + newTrKey + " - 기존 연결 종료 후 재연결");
        if (handle.currentChannel != null) {
            handle.currentChannel.close();   // 강제 종료 -> runConnection()의 closeFuture().sync()가 리턴 -> 재연결 루프가 새 trKey로 다시 붙음
        }
    }

    private static void runConnection(
            String accessToken, ConnectionHandle handle, String internalSymbol,
            QuoteUpdateListener quoteListener, TradeUpdateListener tradeListener
    ) throws Exception {

        String trKey = handle.trKey;   // 🔥 이 순간의 tr_key로 연결 (전환되면 다음 루프에서 최신값 사용)
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
                            ch.pipeline().addLast(new LSWebSocketHandler(
                                    accessToken, trKey, internalSymbol, quoteListener, tradeListener
                            ));
                        }
                    });

            System.out.println("[LS] " + internalSymbol + "(" + trKey + ") 웹소켓 연결 시도...");

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
            handle.currentChannel = channel;   // 🔥 저장 - switchSymbol()이 강제 종료할 수 있도록
            channel.closeFuture().sync();

        } finally {
            group.shutdownGracefully();
        }
    }
}