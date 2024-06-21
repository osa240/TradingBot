package com.ua.osa.tradingbot.models.dto.enums;

public enum WebSocketMethodEnum {
    trades_subscribe("trades_update"),
    ping,
    lastprice_subscribe("lastprice_update"),
    market_subscribe,
    marketToday_subscribe;

    private String method;

    WebSocketMethodEnum() {
    }

    WebSocketMethodEnum(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }
}
