package com.ua.osa.tradingbot.websocket.protocol;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MessageRequest {
    private int id;
    private String method;
    private List<Object> params = new ArrayList<>();
}
