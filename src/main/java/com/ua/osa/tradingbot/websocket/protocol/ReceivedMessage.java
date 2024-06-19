package com.ua.osa.tradingbot.websocket.protocol;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class ReceivedMessage {
    private Integer id;
    private String result;
    private String error;
}
