package com.ua.osa.tradingbot.models.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Ticker {
    protected BigDecimal bid;
    protected BigDecimal ask;
    protected BigDecimal low;
    protected BigDecimal high;
    protected BigDecimal last;
    protected BigDecimal vol;
    protected BigDecimal deal;
    protected BigDecimal change;
}
