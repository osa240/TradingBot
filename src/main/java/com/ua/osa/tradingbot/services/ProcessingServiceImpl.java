package com.ua.osa.tradingbot.services;

import com.ua.osa.tradingbot.models.dto.enums.TradePair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProcessingServiceImpl implements ProcessingService {
    // TODO: 24.06.2024 for TESTS!
    private static TradePair pair = TradePair.BTC_USDT;

    private final List<BigDecimal> prices = Collections.synchronizedList(new ArrayList<>());
    private final DecisionService decisionService;

    @Override
    public void processingLastPrice(BigDecimal price) {
        this.prices.add(price);

        if (this.prices.size() > 30) {
            decisionService.makeDecisionByPriceHistory(this.prices, pair);
            this.prices.remove(0);
        } else {
            log.warn("Not enough data. Size of list is: {}, but not 30", prices.size());
        }
    }
}
