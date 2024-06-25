package com.ua.osa.tradingbot.scheduler;

import com.ua.osa.tradingbot.scheduler.tasks.StartWorkTask;
import com.ua.osa.tradingbot.scheduler.tasks.WebSocketConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TaskFactory {
    @Autowired
    private WebSocketConnection webSocketConnection;
    @Autowired
    private StartWorkTask startWorkTask;

    public WebSocketConnection createWebSocketConnectionTask() {
        return this.webSocketConnection;
    }

    public StartWorkTask getStartWorkTask() {
        return startWorkTask;
    }
}
