package com.ua.osa.tradingbot.models.dto;

import com.ua.osa.tradingbot.services.indicators.IndicatorEnum;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
public class TradingRecord {

    private IndicatorEnum indicator;

    private Date timestampOpen;
    private BigDecimal priceOpen;

    private Date timestampClose;
    private BigDecimal priceClose;

    private BigDecimal dif;
}
