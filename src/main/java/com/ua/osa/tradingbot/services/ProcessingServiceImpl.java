package com.ua.osa.tradingbot.services;

import com.ua.osa.tradingbot.models.dto.enums.TradePair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProcessingServiceImpl implements ProcessingService {
    private static final TradePair pair = TradePair.DBTC_DUSDT;

    private final AtomicReference<BigDecimal> price = new AtomicReference<>(BigDecimal.ZERO);
    private final DecisionService decisionService;

    @Override
    public void processingLastPrice(BigDecimal price) {
//        this.prices.add(price);
//
//        if (this.prices.size() > 20) {
//            decisionService.makeDecisionByPriceHistory(this.prices, pair);
//            this.prices.remove(0);
//        } else {
//            log.warn("Not enough data. Size of prices list is: {}, but not 20", prices.size());
//        }
        this.price.set(price);
    }

    @Override
    public void processingKlains(BarSeries series) {
        price.set(BigDecimal.valueOf(series.getLastBar().getClosePrice().doubleValue()));
        int size = series.getBarData().size();
        if (size > 26) {
            decisionService.makeDecisionByIndicators(series);
        } else {
            log.warn("Not enough data. Size of bars are: {}, but not 26", size);
        }
    }
}
