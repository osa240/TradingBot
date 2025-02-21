package com.ua.osa.tradingbot.services;

import static com.ua.osa.tradingbot.AppProperties.IS_BUY_ALREADY;

import com.ua.osa.tradingbot.models.dto.OrderBook;
import com.ua.osa.tradingbot.models.dto.enums.TradePair;
import com.ua.osa.tradingbot.models.entity.OrderBookStatistic;
import com.ua.osa.tradingbot.repository.OrderBookStatisticRepository;
import com.ua.osa.tradingbot.services.ai.dto.OperationEnum;
import com.ua.osa.tradingbot.services.indicators.BollingerBands;
import com.ua.osa.tradingbot.services.strategies.OrderBookStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.ta4j.core.BarSeries;

@Component
@Slf4j
@RequiredArgsConstructor
public class DecisionServiceImpl implements DecisionService {
    public final AtomicReference<BigDecimal> lastPrice = new AtomicReference<>(BigDecimal.ZERO);

    private final StrategyService strategyService;
    private final OperationService operationService;
    private final OrderBookStatisticRepository orderBookStatisticRepository;
    private final OrderBookService orderBookService;

    @Override
    public synchronized void makeDecisionByPriceHistory(List<BigDecimal> closingPrices, TradePair pair) {
        try {
            if (closingPrices.size() < 20) {
                log.warn("Count {} of prices is below 20. Waiting please...", closingPrices.size());
                return;
            }
            BigDecimal lastPrice = closingPrices.getLast();
            this.lastPrice.set(lastPrice);
            checkCurrentPriceByBollingerBands(closingPrices);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void checkCurrentPriceByBollingerBands(List<BigDecimal> closingPrices) {
        // Розраховуємо Bollinger Bands
        BollingerBands.BollingerBand bands = BollingerBands.calculateBollingerBands(closingPrices, 20, new BigDecimal("2"));

        // Приймаємо торгове рішення
        makeTradingDecision(bands);
    }

    @Override
    public synchronized void makeDecisionByIndicators(BarSeries series) {
        try {
            makeDecision(strategyService.getOperationByLastKline(series), series);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public synchronized void makeDecisionByOrderBook(OrderBook orderBook) {
        try {
            BigDecimal lastPrice = this.lastPrice.get();
            if (lastPrice.compareTo(BigDecimal.ZERO) > 0) {
                OrderBookStrategy strategy = new OrderBookStrategy(orderBook);
                if (!IS_BUY_ALREADY.get() && strategy.shouldEnter(0)) {
                    log.info("Buying...");
                    OrderBookStatistic orderBookStatistic = new OrderBookStatistic();
                    orderBookStatistic.setClosePrice(lastPrice);
                    orderBookStatistic.setAsksTotalAmount(BigDecimal.valueOf(orderBook.getTotalAskVolume()));
                    orderBookStatistic.setBidsTotalAmount(BigDecimal.valueOf(orderBook.getTotalBidVolume()));
                    orderBookStatistic.setIsOpen(false);
                    orderBookStatistic.setTimestamp(new Date());

                    orderBookStatisticRepository.save(orderBookStatistic);
                    IS_BUY_ALREADY.set(operationService.buyMarket(null));
                } else if (IS_BUY_ALREADY.get() && strategy.shouldExit(0)) {
                    log.info("Selling...");

                    OrderBookStatistic orderBookStatistic = new OrderBookStatistic();
                    orderBookStatistic.setClosePrice(lastPrice);
                    orderBookStatistic.setAsksTotalAmount(BigDecimal.valueOf(orderBook.getTotalAskVolume()));
                    orderBookStatistic.setBidsTotalAmount(BigDecimal.valueOf(orderBook.getTotalBidVolume()));
                    orderBookStatistic.setIsOpen(true);
                    orderBookStatistic.setTimestamp(new Date());

                    orderBookStatisticRepository.save(orderBookStatistic);
                    IS_BUY_ALREADY.set(!operationService.sellMarket(null));
                } else {
                    log.info("Waiting...");
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void makeDecision(OperationEnum operation, BarSeries series) {
        if (!IS_BUY_ALREADY.get() && operation == OperationEnum.BUY) {
            IS_BUY_ALREADY.set(operationService.buy(
                    BigDecimal.valueOf(series.getLastBar().getClosePrice().doubleValue()),
                    null
            ));
        } else if (IS_BUY_ALREADY.get() && operation == OperationEnum.SELL) {
            IS_BUY_ALREADY.set(operationService.sell(
                    BigDecimal.valueOf(series.getLastBar().getClosePrice().doubleValue()),
                    null
            ));
        }
    }

    private int makeTradingDecision(BollingerBands.BollingerBand bands) {
        if (bands.lastPrice.compareTo(bands.lowerBand) < 0) {
            makeDecisionByOrderBook(orderBookService.getOrderBook());
            return 1;
        } else if (bands.lastPrice.compareTo(bands.upperBand) > 0) {
            makeDecisionByOrderBook(orderBookService.getOrderBook());
            return 2;
        } else {
            return 0;
        }
    }
}
