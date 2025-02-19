package com.ua.osa.tradingbot.services;

import com.ua.osa.tradingbot.models.entity.StrategyStatistic;

public interface StrategyStatisticService {
    void save(StrategyStatistic strategyStatistic);
    void generateStatistic();
    void generateOrderBookStatistic();
}
