package com.ua.osa.tradingbot.models.dto.enums;

public enum WebSocketMethodEnum {
    trades_subscribe("trades_update", "trades_unsubscribe"),
    ping,
    lastprice_subscribe("lastprice_update", "lastprice_unsubscribe"),
    market_subscribe,
    candles_subscribe("candles_update", "candles_unsubscribe"),
    depth_subscribe("depth_update", "depth_unsubscribe"),
    marketToday_subscribe;

    private String method;
    private String unsubscribe;

    WebSocketMethodEnum() {
    }

    WebSocketMethodEnum(String method) {
        this.method = method;
    }

    WebSocketMethodEnum(String method, String unsubscribe) {
        this.method = method;
        this.unsubscribe = unsubscribe;
    }

    public String getMethod() {
        return method;
    }

    public String getUnsubscribe() {
        return unsubscribe;
    }
}
