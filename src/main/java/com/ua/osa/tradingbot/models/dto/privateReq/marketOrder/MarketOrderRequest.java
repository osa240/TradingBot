package com.ua.osa.tradingbot.models.dto.privateReq.marketOrder;

import com.ua.osa.tradingbot.models.dto.AbstractRequest;
import com.ua.osa.tradingbot.models.dto.enums.SideEnum;
import com.ua.osa.tradingbot.models.dto.enums.TradePair;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
public class MarketOrderRequest extends AbstractRequest {
    private TradePair market;
    private SideEnum side;
    private BigDecimal amount;
}
