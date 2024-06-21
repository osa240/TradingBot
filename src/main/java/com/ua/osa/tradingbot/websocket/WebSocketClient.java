package com.ua.osa.tradingbot.websocket;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import com.google.gson.Gson;
import com.ua.osa.tradingbot.models.dto.enums.WebSocketMethodEnum;
import com.ua.osa.tradingbot.websocket.protocol.MessageRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Component
@Slf4j
public class WebSocketClient {
    public static final String SENDED = "Sended: {}";
    public static final String RECEIVED = "Received: {}";
    private final Sinks.Many<String> messageSink = Sinks.many().multicast().onBackpressureBuffer();
    private final AtomicBoolean isReconnecting = new AtomicBoolean(false);
    private final ConcurrentLinkedQueue<String> messageQueue = new ConcurrentLinkedQueue<>();

    private WebSocketSession session = null;

    public void connect() {
        ReactorNettyWebSocketClient client = new ReactorNettyWebSocketClient();
        String url = "wss://api.whitebit.com/ws";
        client.execute(
                URI.create(url),
                this::handleSession
        ).doOnError(error -> {
            log.error("Connection error: " + error.getMessage());
            reconnect();
        }).subscribe();
    }

    public void sendMessage(MessageRequest message) {
        String payload = getPayload(message);
        if (this.session != null && this.session.isOpen()) {
            this.messageSink.tryEmitNext(payload);
        } else {
            reconnect();
            this.messageQueue.add(payload);
        }
    }

    private Mono<Void> handleSession(WebSocketSession session) {
        this.session = session;
        MessageRequest messageRequest = new MessageRequest(0, WebSocketMethodEnum.ping, new ArrayList<>());

        Mono<Void> send = session.send(
                Flux.interval(Duration.ofSeconds(50))
                        .map(time -> session.textMessage(getPayload(messageRequest)))
                        .mergeWith(messageSink.asFlux()
                                .map(session::textMessage))

        ).then().doOnTerminate(() -> {
            while (!messageQueue.isEmpty()) {
                messageSink.tryEmitNext(messageQueue.poll());
            }
        });

        Mono<Void> receive = session.receive()
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
        });
    }

    private void reconnect() {
        if (isReconnecting.compareAndSet(false, true)) {
            log.warn("Reconnecting...");
            // Wait for a few seconds before reconnecting
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            connect();
            isReconnecting.set(false);
        }
    }
}
