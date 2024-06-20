package com.ua.osa.tradingbot.models.dto.balance;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BalanceRequest {
    private String request;
    private final String nonce = String.valueOf(System.currentTimeMillis());
    private final boolean nonceWindow = true;


    public BalanceRequest(String request) {
        this.request = request;
    }
}
