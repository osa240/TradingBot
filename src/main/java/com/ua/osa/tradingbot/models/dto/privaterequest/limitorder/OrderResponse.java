package com.ua.osa.tradingbot.models.dto.privaterequest.limitorder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ua.osa.tradingbot.models.dto.enums.SideEnum;
import com.ua.osa.tradingbot.models.dto.enums.TradePair;
import java.math.BigDecimal;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderResponse {
    private Long orderId;
    private String clientOrderId;
    private TradePair market;
    private SideEnum side;
    private String timestamp;
    private BigDecimal dealMoney;
    private BigDecimal dealStock;
    private BigDecimal amount;
    private BigDecimal takerFee;
    private BigDecimal makerFee;
    private BigDecimal left;
    private BigDecimal dealFee;
    private BigDecimal price;
    private Boolean postOnly;
    private Boolean ioc;

}
