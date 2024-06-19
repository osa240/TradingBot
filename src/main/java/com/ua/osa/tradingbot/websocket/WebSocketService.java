package com.ua.osa.tradingbot.websocket;

import com.ua.osa.tradingbot.websocket.protocol.SendMessage;
import org.springframework.stereotype.Service;

@Service
public class WebSocketService {
    private final WebSocketClient webSocketClient;

    public WebSocketService(WebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
    }

    public void startWebSocketCommunication() {
        String url = "wss://api.whitebit.com/ws";
        webSocketClient.connect(url);
    }

    public void sendMessage(SendMessage message) {
        webSocketClient.sendMessage(message);
    }
}
