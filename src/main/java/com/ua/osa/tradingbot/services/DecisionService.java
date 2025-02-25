package com.ua.osa.tradingbot.services;

import com.ua.osa.tradingbot.models.dto.OrderBook;
import com.ua.osa.tradingbot.models.dto.enums.TradePair;
import java.math.BigDecimal;
import java.util.List;
import org.ta4j.core.BarSeries;

public interface DecisionService {
    void makeDecisionByPriceHistory(List<BigDecimal> prices, TradePair pair);

    void makeDecisionByIndicators(BarSeries series);

    void makeDecisionByOrderBook(OrderBook orderBook);
}
