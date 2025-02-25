package com.ua.osa.tradingbot.models.dto.privaterequest.limitorder;

import com.ua.osa.tradingbot.models.dto.AbstractRequest;
import com.ua.osa.tradingbot.models.dto.enums.SideEnum;
import com.ua.osa.tradingbot.models.dto.enums.TradePair;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class LimitOrderRequest extends AbstractRequest {
    private TradePair market;
    private SideEnum side;
    private BigDecimal amount;
    private BigDecimal price;

    private String clientOrderId;
    private Boolean postOnly;
    private Boolean ioc;
    private Integer bboRole;
}
