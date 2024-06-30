package com.ua.osa.tradingbot.models.dto.publicReq.kline;

import lombok.Data;
import java.math.BigDecimal;

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
