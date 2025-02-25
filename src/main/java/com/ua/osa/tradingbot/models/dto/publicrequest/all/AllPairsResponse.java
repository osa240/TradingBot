package com.ua.osa.tradingbot.models.dto.publicrequest.all;

import java.util.Map;
import lombok.Data;

@Data
public class AllPairsResponse {
    private Boolean success;
    private String message;
    private Map<String, MarketPair> result;
}
