package com.ua.osa.tradingbot.services;

import com.ua.osa.tradingbot.BotSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import java.math.BigDecimal;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProcessingServiceImpl implements ProcessingService {
    private final DecisionService decisionService;

    @Override
    public void processingLastPrice(List<BigDecimal> price) {
        decisionService.makeDecisionByPriceHistory(price, BotSettings.TRADE_PAIR.get());
    }

    @Override
    public void processingKlains(BarSeries series) {
        int size = series.getBarData().size();
        if (size > 26) {
            decisionService.makeDecisionByIndicators(series);
        } else {
            log.warn("Not enough data. Size of bars are: {}, but not 26", size);
        }
    }
}
