package com.ua.osa.tradingbot.models.dto.pair;

import lombok.Data;

@Data
public class TickerPairResponse {
    private Boolean success;
    private String message;
    private TickerPair result;
}
