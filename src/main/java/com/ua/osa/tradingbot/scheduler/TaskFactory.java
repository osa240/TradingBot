package com.ua.osa.tradingbot.scheduler;

import com.ua.osa.tradingbot.scheduler.tasks.WebSocketConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TaskFactory {
    @Autowired
    private WebSocketConnection webSocketConnection;

    public WebSocketConnection createWebSocketConnectionTask() {
        return this.webSocketConnection;
    }
}
