package com.ua.osa.tradingbot.models.dto.balance;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BalanceResponse {
    private String available;
    private String freeze;
}
