package com.ua.osa.tradingbot.models.dto.all;

import com.ua.osa.tradingbot.models.dto.Ticker;
import lombok.Data;

@Data
public class MarketPair {
    private Ticker ticker;
    private long at;
}
