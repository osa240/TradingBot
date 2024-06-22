package com.ua.osa.tradingbot.models.dto.enums;

public enum TradePair {
    BTC_USD,
    BTC_USDT,
    DBTC_DUSDT,
    NOT_USDT,
    ETH_USD,
    ;

    String description;

    TradePair() {
    }

    TradePair(String description) {
        this.description = description;
    }
}
