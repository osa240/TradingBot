package com.ua.osa.tradingbot.websocket;

import com.ua.osa.tradingbot.BotSettings;
import com.ua.osa.tradingbot.models.dto.enums.WebSocketMethodEnum;
import com.ua.osa.tradingbot.websocket.protocol.MessageRequest;
import java.util.Collections;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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

    public void closeConnection() {
        Set<WebSocketMethodEnum> webSocketMethodEnums = BotSettings.SUBSCRIBES.get();
        if (CollectionUtils.isNotEmpty(webSocketMethodEnums)) {
            webSocketMethodEnums.forEach(type -> webSocketClient.sendMessage(new MessageRequest(
                    type.ordinal(), type.getUnsubscribe(), Collections.emptyList()
            )));
        }
        webSocketClient.closeConnection();
    }
}
