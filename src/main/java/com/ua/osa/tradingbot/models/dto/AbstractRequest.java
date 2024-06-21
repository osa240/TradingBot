package com.ua.osa.tradingbot.models.dto;

import lombok.Data;

@Data
public class AbstractRequest {
    protected String request;
    protected final String nonce = String.valueOf(System.currentTimeMillis());
}
