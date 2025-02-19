package com.ua.osa.tradingbot.repository;

import com.ua.osa.tradingbot.models.entity.OrderBookStatistic;
import com.ua.osa.tradingbot.models.entity.StrategyStatistic;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderBookStatisticRepository extends CrudRepository<OrderBookStatistic, Long> {
    List<OrderBookStatistic> findAll();
}
