package com.ua.osa.tradingbot.services;

import com.ua.osa.tradingbot.models.dto.enums.TradePair;
import com.ua.osa.tradingbot.services.ai.dto.OperationEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.ta4j.core.BarSeries;

@Component
@Slf4j
@RequiredArgsConstructor
public class DecisionServiceImpl implements DecisionService {
    private final AtomicBoolean buyNow = new AtomicBoolean(false);
    private final StrategyService strategyService;
    private final OperationService operationService;

    @Override
    public synchronized void makeDecisionByPriceHistory(List<BigDecimal> closingPrices, TradePair pair) {
        try {
//            strategy_3(closingPrices);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public synchronized void makeDecisionByIndicators(BarSeries series) {
        try {
            makeDecision(strategyService.getOperationByLastKline(series), series);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void makeDecision(OperationEnum operation, BarSeries series) {
        if (!buyNow.get() && operation == OperationEnum.BUY) {
            this.buyNow.set(operationService.buy(
                    BigDecimal.valueOf(series.getLastBar().getClosePrice().doubleValue()),
                    null
            ));
        } else if (buyNow.get() && operation == OperationEnum.SELL) {
            this.buyNow.set(operationService.sell(
                    BigDecimal.valueOf(series.getLastBar().getClosePrice().doubleValue()),
                    null
            ));
        }
    }

    private BigDecimal getBuyAmount(BigDecimal deposit, BigDecimal currentPrice) {
        return deposit.divide(currentPrice, 6, BigDecimal.ROUND_DOWN);
    }
}
