package com.ua.osa.tradingbot.models.dto.publicrequest.pair;

import com.ua.osa.tradingbot.models.dto.Ticker;
import java.math.BigDecimal;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class TickerPair extends Ticker {
    private BigDecimal open;

    @Override
    public String toString() {
        return "TickerPair{"
                + "bid=" + bid
                + ", ask=" + ask
                + ", low=" + low
                + ", high=" + high
                + ", last=" + last
                + ", vol=" + vol
                + ", deal=" + deal
                + ", change=" + change
                + ", open=" + open
                + '}';
    }
}
