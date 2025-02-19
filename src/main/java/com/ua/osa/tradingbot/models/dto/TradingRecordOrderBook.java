package com.ua.osa.tradingbot.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradingRecordOrderBook {

    private Date timestampOpen;
    private BigDecimal priceOpen;
    private BigDecimal bidsAmountOpen;
    private BigDecimal asksAmountOpen;

    private Date timestampClose;
    private BigDecimal priceClose;
    private BigDecimal bidsAmountClose;
    private BigDecimal asksAmountClose;

    private BigDecimal dif;
}
