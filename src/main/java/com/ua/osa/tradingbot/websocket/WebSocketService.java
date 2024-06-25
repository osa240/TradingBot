package com.ua.osa.tradingbot.websocket;

import com.ua.osa.tradingbot.websocket.protocol.MessageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebSocketService {
    private final WebSocketClient webSocketClient;

    public void startWebSocketCommunication() {
        webSocketClient.connect();
    }

    public void sendMessage(MessageRequest message) {
        webSocketClient.sendMessage(message);
    }
}
