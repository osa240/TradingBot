package com.ua.osa.tradingbot.scheduler.tasks;

import com.ua.osa.tradingbot.websocket.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WebSocketConnection implements Runnable {
    @Autowired
    private WebSocketService webSocketService;

    @Override
    public void run() {
        webSocketService.startWebSocketCommunication();
    }
}
