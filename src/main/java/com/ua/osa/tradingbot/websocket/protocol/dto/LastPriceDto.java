package com.ua.osa.tradingbot.websocket.protocol.dto;

import java.math.BigDecimal;
import com.ua.osa.tradingbot.models.dto.enums.TradePair;
import lombok.Data;

@Data
public class LastPriceDto {
    private TradePair pair;
    private BigDecimal lastPrice;
}
