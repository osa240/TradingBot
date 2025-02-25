package com.ua.osa.tradingbot.websocket.protocol.dto;

import com.ua.osa.tradingbot.models.dto.enums.TradePair;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class LastPriceDto {
    private TradePair pair;
    private BigDecimal lastPrice;
}
