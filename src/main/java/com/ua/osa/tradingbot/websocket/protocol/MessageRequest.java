package com.ua.osa.tradingbot.websocket.protocol;

import java.util.ArrayList;
import java.util.List;
import com.ua.osa.tradingbot.models.dto.enums.WebSocketMethodEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequest {
    private int id;
    private WebSocketMethodEnum method;
    private List<Object> params = new ArrayList<>();
}
