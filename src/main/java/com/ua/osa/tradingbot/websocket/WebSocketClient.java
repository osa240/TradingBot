package com.ua.osa.tradingbot.websocket;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.google.gson.Gson;
import com.ua.osa.tradingbot.models.dto.enums.WebSocketMethodEnum;
import com.ua.osa.tradingbot.services.CollectDataService;
import com.ua.osa.tradingbot.websocket.protocol.MessageRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class WebSocketClient {
    private static final String URL = "wss://api.whitebit.com/ws";
    private static final String SENDED = "Sended: {}";
    private static final String RECEIVED = "Received: {}";

    private final AtomicBoolean isReconnecting = new AtomicBoolean(false);
    private final ConcurrentLinkedQueue<String> messageQueue = new ConcurrentLinkedQueue<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final AtomicReference<WebSocketSession> sessionRef = new AtomicReference<>(null);
    private final AtomicReference<ReactorNettyWebSocketClient> client = new AtomicReference<>(null);
    private final AtomicReference<Disposable> subscribe = new AtomicReference<>(null);
    private final MessageRequest pingRequest = new MessageRequest(0, WebSocketMethodEnum.ping, new ArrayList<>());
    private final CollectDataService collectDataService;
    private ScheduledFuture<?> reconnectTask;


    public WebSocketClient(CollectDataService collectDataService) {
        this.collectDataService = collectDataService;
        startReconnectTask();
    }

    public synchronized void connect() {
        if (client.get() == null) {
            client.set(new ReactorNettyWebSocketClient());
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

    private void startReconnectTask() {
        reconnectTask = scheduler.scheduleAtFixedRate(this::checkAndReconnect, 0, 30, TimeUnit.SECONDS);
    }

    private Mono<Void> handleSession(WebSocketSession session) {
        sessionRef.set(session);

        Mono<Void> send = sessionRef.get().send(
                Flux.interval(Duration.ofSeconds(29))
                        .map(time -> sessionRef.get().textMessage(getPayload(pingRequest)))
                        .mergeWith(Flux.fromIterable(messageQueue).map(sessionRef.get()::textMessage))
        );
        Mono<Void> receive = sessionRef.get().receive()
                .map(WebSocketMessage::getPayloadAsText)
                .flatMap(this::handleMessage)
                .then();

        log.info("WebSocketConnection established");
        return Mono.zip(send, receive).then();
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

    public synchronized void reconnect() {
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
                connect();
                isReconnecting.set(false);
                sessionRef.set(null);
                client.set(null);
            }
        }
    }

    private synchronized  void reconnectAndSend(String message) {
        reconnect();
        messageQueue.add(message);
    }
}
