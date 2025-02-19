package com.ua.osa.tradingbot.services;

import com.ua.osa.tradingbot.AppProperties;
import com.ua.osa.tradingbot.models.dto.enums.TradePair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProcessingServiceImpl implements ProcessingService {
    private static final TradePair pair = TradePair.DBTC_DUSDT;

    private final AtomicReference<BigDecimal> price = new AtomicReference<>(BigDecimal.ZERO);
    private final DecisionService decisionService;

    @Override
    public void processingLastPrice(List<BigDecimal> price) {
        this.price.set(price.getLast());
        decisionService.makeDecisionByPriceHistory(price, AppProperties.TRADE_PAIR.get());
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
