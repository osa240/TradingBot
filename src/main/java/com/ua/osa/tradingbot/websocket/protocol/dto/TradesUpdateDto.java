package com.ua.osa.tradingbot.websocket.protocol.dto;

import java.util.List;
import com.ua.osa.tradingbot.models.dto.enums.TradePair;
import lombok.Data;

@Data
public class TradesUpdateDto {
    private TradePair pair;
    private List<TradeDto> params;
}
