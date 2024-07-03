package com.ua.osa.tradingbot.repository;

import com.ua.osa.tradingbot.models.entity.StrategyStatistic;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StrategyStatisticRepository extends CrudRepository<StrategyStatistic, Long> {
}
