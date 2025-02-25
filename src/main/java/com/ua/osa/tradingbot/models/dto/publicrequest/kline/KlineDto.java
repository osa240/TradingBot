package com.ua.osa.tradingbot.models.dto.publicrequest.kline;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class KlineDto {
    private long time;
    private BigDecimal open;
    private BigDecimal close;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal volumeStock;
    private BigDecimal volumeMoney;
}
