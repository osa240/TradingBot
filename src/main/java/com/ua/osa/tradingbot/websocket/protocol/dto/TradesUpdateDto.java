package com.ua.osa.tradingbot.websocket.protocol.dto;

import com.ua.osa.tradingbot.models.dto.enums.TradePair;
import java.util.List;
import lombok.Data;

@Data
public class TradesUpdateDto {
    private TradePair pair;
    private List<TradeDto> params;
}
