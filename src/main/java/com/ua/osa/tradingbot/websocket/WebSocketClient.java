package com.ua.osa.tradingbot.websocket;

import java.net.URI;
import com.google.gson.Gson;
import com.ua.osa.tradingbot.websocket.protocol.SendMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Component
public class WebSocketClient {

    private WebSocketSession session = null;
    private Sinks.Many<SendMessage> messageSink;


    private final ReactorNettyWebSocketClient client = new ReactorNettyWebSocketClient();

    public void connect(String url) {
        messageSink = Sinks.many().multicast().onBackpressureBuffer();
        Void block = client.execute(
                URI.create(url),
                this::handleSession
        ).block();
    }

    public void sendMessage(SendMessage message) {
        if (this.session != null && this.session.isOpen()) {
            this.messageSink.tryEmitNext(message);
        } else {
            throw new IllegalStateException("WebSocket session is not open.");
        }
    }

    private Mono<Void> handleSession(WebSocketSession session) {
        this.session = session;
        Mono<Void> send = session.send(
                messageSink.asFlux()
                        .map(message -> session.textMessage(getPayload(message)))
        );

        Mono<Void> receive = session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .flatMap(this::handleMessage)
                .then();

        return Mono.zip(send, receive).then();
    }

    private String getPayload(SendMessage ping) {
        return new Gson().toJson(ping);
    }

    private Mono<Void> handleMessage(String message) {
        return Mono.fromRunnable(() -> {
            System.out.println("Received: " + message);
        });
    }
}