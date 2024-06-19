package com.ua.osa.tradingbot.websocket.protocol;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SendMessage {
    private Integer id;
    private String method;
    private List<Object> params;
}
