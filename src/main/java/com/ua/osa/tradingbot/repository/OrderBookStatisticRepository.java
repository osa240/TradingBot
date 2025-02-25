package com.ua.osa.tradingbot.repository;

import com.ua.osa.tradingbot.models.entity.OrderBookStatistic;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderBookStatisticRepository extends CrudRepository<OrderBookStatistic, Long> {
    List<OrderBookStatistic> findAll();
}
