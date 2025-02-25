package com.ua.osa.tradingbot.models.dto.privaterequest.balance;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BalanceResponse {
    private BigDecimal available;
    private BigDecimal freeze;
}
