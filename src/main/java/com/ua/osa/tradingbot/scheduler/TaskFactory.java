package com.ua.osa.tradingbot.scheduler;

import com.ua.osa.tradingbot.scheduler.tasks.OrderBookCollectTask;
import com.ua.osa.tradingbot.scheduler.tasks.WebSocketConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TaskFactory {
    @Autowired
    private WebSocketConnection webSocketConnection;
    @Autowired
    private OrderBookCollectTask orderBookCollectTask;

    public WebSocketConnection createWebSocketConnectionTask() {
        return this.webSocketConnection;
    }

    public OrderBookCollectTask getCollectOrderBooksTask() {
        return orderBookCollectTask;
    }
}
