package com.ua.osa.tradingbot.websocket;

import com.ua.osa.tradingbot.websocket.protocol.MessageRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WebSocketService {
    private final WebSocketClient webSocketClient;

    public WebSocketService(WebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
    }

    public void startWebSocketCommunication() {
        webSocketClient.connect();
    }

    public void sendMessage(MessageRequest message) {
        webSocketClient.sendMessage(message);
    }
}
