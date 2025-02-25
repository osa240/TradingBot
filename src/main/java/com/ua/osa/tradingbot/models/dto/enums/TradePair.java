package com.ua.osa.tradingbot.models.dto.enums;

import lombok.Getter;

@Getter
public enum TradePair {
    DBTC_DUSDT,
    BTC_USDT,
    BTC_USD,
    NOT_USDT,
    ETH_USD
    ;

    private String description;

    TradePair() {
    }

    TradePair(String description) {
        this.description = description;
    }
}
