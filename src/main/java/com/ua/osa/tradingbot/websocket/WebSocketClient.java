package com.ua.osa.tradingbot.websocket;

import com.google.gson.Gson;
import com.ua.osa.tradingbot.AppProperties;
import com.ua.osa.tradingbot.BotSettings;
import com.ua.osa.tradingbot.models.dto.enums.TradePair;
import com.ua.osa.tradingbot.models.dto.enums.WebSocketMethodEnum;
import com.ua.osa.tradingbot.scheduler.TaskManager;
import com.ua.osa.tradingbot.services.CollectDataService;
import com.ua.osa.tradingbot.websocket.protocol.MessageRequest;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.resolver.dns.DnsAddressResolverGroup;
import io.netty.resolver.dns.DnsNameResolverBuilder;
import io.netty.resolver.dns.SingletonDnsServerAddressStreamProvider;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketClient {
    private static final String URL = "wss://api.whitebit.com/ws";
    private static final String SENDED = "Sended: {}";
    private static final String RECEIVED = "Received: {}";

    private final AtomicBoolean isReconnecting = new AtomicBoolean(false);
    private final ConcurrentLinkedQueue<String> messageQueue = new ConcurrentLinkedQueue<>();
    private final AtomicReference<WebSocketSession> sessionRef = new AtomicReference<>(null);
    private final AtomicReference<ReactorNettyWebSocketClient> client =
            new AtomicReference<>(null);
    private final AtomicReference<Disposable> subscribe = new AtomicReference<>(null);
    private final MessageRequest pingRequest = new MessageRequest(
            0, WebSocketMethodEnum.ping.name(), new ArrayList<>()
    );
    private final CollectDataService collectDataService;
    private final TaskManager taskManager;

    public synchronized void connect() {
        if (client.get() == null) {
            client.set(initializeClient());
        }
        if (sessionRef.get() != null && sessionRef.get().isOpen()) {
            log.info("WebSocket session is already open.");
            return;
        }

        if (subscribe.get() != null) {
            subscribe.get().dispose();
        }

        Disposable subscribe = client.get().execute(
                URI.create(URL),
                this::handleSession
        ).doOnError(error -> {
            log.error("Connection error: " + error.getMessage());
            reconnect();
        }).subscribe();

        this.subscribe.set(subscribe);
    }

    public synchronized void sendMessage(MessageRequest message) {
        String payload = getPayload(message);
        if (this.sessionRef.get() != null && this.sessionRef.get().isOpen()) {
            sessionRef.get().send(Mono.just(sessionRef.get().textMessage(payload)))
                    .doOnError(error -> {
                        log.error("Send error: " + error.getMessage());
                        reconnectAndSend(payload);
                    }).subscribe();
        } else {
            reconnectAndSend(payload);
        }
    }

    private Mono<Void> handleSession(WebSocketSession session) {
        Mono<Void> send = createSend(session);
        Mono<Void> receive = createReceive(session);
        sessionRef.set(session);
        taskManager.schedule(this::restartSubscribes, 5000, TimeUnit.MILLISECONDS);
        Mono<Void> zip = Mono.zip(send, receive).then();
        log.info("WebSocketConnection established");
        return zip;
    }

    private void restartSubscribes() {
        log.info("Restart all subscribes");

        TradePair tradePair = BotSettings.TRADE_PAIR.get();
        for (WebSocketMethodEnum webSocketMethodEnum : BotSettings.SUBSCRIBES.get()) {
            if (webSocketMethodEnum.equals(WebSocketMethodEnum.lastprice_subscribe)) {
                sendMessage(new MessageRequest(
                        webSocketMethodEnum.ordinal(),
                        webSocketMethodEnum.name(),
                        List.of(tradePair)
                ));
            } else if (webSocketMethodEnum.equals(WebSocketMethodEnum.candles_subscribe)) {
                sendMessage(new MessageRequest(
                        webSocketMethodEnum.ordinal(),
                        webSocketMethodEnum.name(),
                        List.of(tradePair, 60)
                ));
            } else if (webSocketMethodEnum.equals(WebSocketMethodEnum.depth_subscribe)) {
                sendMessage(new MessageRequest(
                        webSocketMethodEnum.ordinal(),
                        webSocketMethodEnum.name(),
                        List.of(tradePair, 100, "100", true)
                ));
            }
        }
    }

    private Mono<Void> createReceive(WebSocketSession session) {
        Mono<Void> receive = session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .flatMap(this::handleMessage)
                .then();
        return receive;
    }

    private Mono<Void> createSend(WebSocketSession session) {
        Mono<Void> send = session.send(
                Flux.interval(Duration.ofSeconds(29))
                        .map(time -> {
                            if (!AppProperties.isInternetAvailable()) {
                                throw new RuntimeException("The internet is unreachable");
                            }
                            return session.textMessage(getPayload(pingRequest));
                        }).mergeWith(Flux.fromIterable(messageQueue)
                        .map(session::textMessage))
                        .doOnError(error -> {
                            log.error("Send error: " + error.getMessage());
                            this.sessionRef.set(null);
                            reconnectAndSend(getPayload(pingRequest));
                        })
        );
        return send;
    }

    private String getPayload(MessageRequest messageRequest) {
        String payloadRequest = new Gson().toJson(messageRequest);
        logMessage(payloadRequest, SENDED);
        return payloadRequest;
    }

    private void logMessage(String payloadRequest, String s) {
        log.info(s, payloadRequest);
    }

    private Mono<Void> handleMessage(String message) {
        return Mono.fromRunnable(() -> {
            logMessage(message, RECEIVED);
            collectDataService.collectDataFromWebSocket(message);
        });
    }

    private synchronized void checkAndReconnect() {
        if (sessionRef.get() == null || !sessionRef.get().isOpen()) {
            log.warn("Session is closed. Reconnecting...");
            reconnect();
        }
    }

    private synchronized void reconnect() {
        if (isReconnecting.compareAndSet(false, true)) {
            log.warn("Reconnecting...");
            try {
                if (sessionRef.get() != null) {
                    sessionRef.get().close().subscribe();
                }
                Thread.sleep(5000);
            } catch (Exception e) {
                log.error("Error closing session: " + e.getMessage());
            } finally {
                clearDnsCache();
                connect();
                isReconnecting.set(false);
                sessionRef.set(null);
                client.set(null);
            }
        }
    }

    private synchronized void reconnectAndSend(String message) {
        reconnect();
        messageQueue.add(message);
    }

    private void clearDnsCache() {
        try {
            Class<InetAddress> klass = InetAddress.class;
            Method clearCache = klass.getDeclaredMethod("clearCache");
            clearCache.setAccessible(true);
            clearCache.invoke(null);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private ReactorNettyWebSocketClient initializeClient() {
        SslContext sslContext;
        try {
            sslContext = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SSL context", e);
        }

        // Create custom DNS server address stream provider
        SingletonDnsServerAddressStreamProvider dnsServerAddressStreamProvider =
                new SingletonDnsServerAddressStreamProvider(new InetSocketAddress("8.8.8.8", 53));

        // Create DNS resolver
        DnsNameResolverBuilder dnsResolverBuilder = new DnsNameResolverBuilder()
                .channelType(NioDatagramChannel.class)
                .nameServerProvider(dnsServerAddressStreamProvider);

        // Create TcpClient with DNS configuration
        TcpClient tcpClient = TcpClient.create()
                .resolver(spec -> spec.dnsAddressResolverGroupProvider(
                        DnsAddressResolverGroup::new))
                .secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));

        // Create HttpClient with TcpClient
        HttpClient httpClient = HttpClient.from(tcpClient)
                .wiretap(true)
                .responseTimeout(Duration.ofSeconds(10));

        // Create ReactorNettyWebSocketClient with HttpClient
        return new ReactorNettyWebSocketClient(httpClient);
    }

    public void closeConnection() {
        if (sessionRef.get() != null && sessionRef.get().isOpen()) {
            this.subscribe.get().dispose();
            this.sessionRef.get().close();
        }
    }
}
