package com.ua.osa.tradingbot.models.dto.privaterequest.marketorder;

import com.ua.osa.tradingbot.models.dto.AbstractRequest;
import com.ua.osa.tradingbot.models.dto.enums.SideEnum;
import com.ua.osa.tradingbot.models.dto.enums.TradePair;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MarketOrderRequest extends AbstractRequest {
    private TradePair market;
    private SideEnum side;
    private BigDecimal amount;
}
