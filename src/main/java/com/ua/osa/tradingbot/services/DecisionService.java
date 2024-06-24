package com.ua.osa.tradingbot.services;

import com.ua.osa.tradingbot.models.dto.enums.TradePair;

import java.math.BigDecimal;
import java.util.List;

public interface DecisionService {
    void makeDecisionByPriceHistory(List<BigDecimal> prices, TradePair pair);
}
