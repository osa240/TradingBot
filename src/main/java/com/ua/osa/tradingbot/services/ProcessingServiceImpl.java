package com.ua.osa.tradingbot.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Component
@Slf4j
public class ProcessingServiceImpl implements ProcessingService {

    private final List<BigDecimal> maxList = Collections.synchronizedList(new LinkedList<>());
    private final List<BigDecimal> minList = Collections.synchronizedList(new LinkedList<>());
    private final List<BigDecimal> prices = Collections.synchronizedList(new LinkedList<>());

    @Override
    public void processingLastPrice(BigDecimal price) {
        this.prices.add(price);

        if (this.prices.size() > 2) {
            findLocalMaxAndMin();
        }

    }

    @Override
    public void processingLastPrice(List<BigDecimal> prices) {
        if (CollectionUtils.isEmpty(this.prices)) {
            this.prices.addAll(prices);
        } else {
            this.prices.add(prices.get(prices.size() - 1));
        }
        findLocalMaxAndMin();
    }

    private void findLocalMaxAndMin() {
        this.maxList.clear();
        this.minList.clear();
        for (int i = 1; i < prices.size() - 1; i++) {
            if (prices.get(i).compareTo(prices.get(i - 1)) > 0 && prices.get(i).compareTo(prices.get(i + 1)) > 0) {
                this.maxList.add(prices.get(i));
            } else if (prices.get(i).compareTo(prices.get(i - 1)) < 0 && prices.get(i).compareTo(prices.get(i + 1)) < 0) {
                this.minList.add(prices.get(i));
            }
        }
        log.info("MAX_LIST: " + maxList);
        log.info("MIN_LIST: " + minList);
        determineTrend();
    }

    public void determineTrend() {
        if (maxList.size() < 2 || minList.size() < 2) {
            log.info("Not enough data to determine trend.");
            return;
        }

        BigDecimal lastMax = maxList.get(maxList.size() - 1);
        BigDecimal previousMax = maxList.get(maxList.size() - 2);

        BigDecimal lastMin = minList.get(minList.size() - 1);
        BigDecimal previousMin = minList.get(minList.size() - 2);

        if (lastMax.compareTo(previousMax) > 0 && lastMin.compareTo(previousMin) > 0) {
            log.info("ТРЕНД ВОСХОДЯЩИЙ");
        } else if (lastMax.compareTo(previousMax) < 0 && lastMin.compareTo(previousMin) < 0) {
            log.info("ТРЕНД НИСХОДЯЩИЙ");
        } else {
            log.info("ТРЕНД БОКОВОЙ");
        }
    }
}
