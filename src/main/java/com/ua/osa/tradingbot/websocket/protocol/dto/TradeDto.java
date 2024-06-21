package com.ua.osa.tradingbot.websocket.protocol.dto;

import java.math.BigDecimal;
import com.ua.osa.tradingbot.models.dto.enums.SideEnum;
import lombok.Data;

@Data
public class TradeDto {
    private Long id;
    private double time;
    private BigDecimal price;
    private BigDecimal amount;
    private SideEnum type;
}
