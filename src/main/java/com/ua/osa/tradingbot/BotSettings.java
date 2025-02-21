package com.ua.osa.tradingbot;

import com.ua.osa.tradingbot.models.dto.enums.TradePair;
import com.ua.osa.tradingbot.models.dto.enums.WebSocketMethodEnum;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public final class BotSettings {
    public static final AtomicReference<Set<WebSocketMethodEnum>> SUBSCRIBES = new AtomicReference<>(new HashSet<>());
    public static final AtomicReference<TradePair> TRADE_PAIR = new AtomicReference<>();
    public static final AtomicReference<BigDecimal> BUY_AMOUNT = new AtomicReference<>();


    static {
        // TODO: insert types of subscribes from enum WebSocketMethodEnum.class
        SUBSCRIBES.get().addAll(Set.of(
                WebSocketMethodEnum.candles_subscribe
        ));

        // TODO: insert trade pair for trading bot
        TRADE_PAIR.set(TradePair.BTC_USDT);

        // TODO: write your amount for buy and sell
        BUY_AMOUNT.set(BigDecimal.valueOf(0.0002));
    }
}
