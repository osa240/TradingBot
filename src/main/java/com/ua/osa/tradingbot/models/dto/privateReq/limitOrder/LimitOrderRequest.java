package com.ua.osa.tradingbot.models.dto.privateReq.limitOrder;

import java.math.BigDecimal;
import com.ua.osa.tradingbot.models.dto.AbstractRequest;
import com.ua.osa.tradingbot.models.dto.enums.SideEnum;
import com.ua.osa.tradingbot.models.dto.enums.TradePair;
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
