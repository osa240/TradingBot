package com.ua.osa.tradingbot.services;

import com.ua.osa.tradingbot.AppProperties;
import com.ua.osa.tradingbot.models.dto.enums.MethodEnum;
import com.ua.osa.tradingbot.models.dto.enums.OrderBookStatusEnum;
import com.ua.osa.tradingbot.models.dto.enums.SideEnum;
import com.ua.osa.tradingbot.models.dto.enums.TradePair;
import com.ua.osa.tradingbot.models.dto.privateReq.limitOrder.LimitOrderRequest;
import com.ua.osa.tradingbot.restClients.WhiteBitClient;
import com.ua.osa.tradingbot.services.indicators.BollingerBands;
import com.ua.osa.tradingbot.services.indicators.MACD;
import com.ua.osa.tradingbot.services.indicators.RSI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

@Component
@Slf4j
@RequiredArgsConstructor
public class DecisionServiceImpl implements DecisionService {
    private final AtomicBoolean buyNow = new AtomicBoolean(false);
    private final WhiteBitClient whiteBitClient;

    @Override
    public void makeDecisionByPriceHistory(List<BigDecimal> closingPrices, TradePair pair) {
        try {
            // todo
//            strategy_3(closingPrices);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void makeDecisionByIndicators(BarSeries series, BigDecimal lastPrice, TradePair pair) {
        try {
            strategy_3(series, lastPrice, pair);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }



    /**
     * Trading by 3 indicators: RSI + MACD + BollingerBand
     * @param closingPrices
     */
    private void strategy_1(List<BigDecimal> closingPrices) {
        BigDecimal currentPrice = closingPrices.getLast();

        // Пример значений индикаторов
        BigDecimal currentRSI = RSI.calculateRSI(closingPrices, 14);
        BollingerBands.BollingerBand currentBands = BollingerBands.calculateBollingerBands(closingPrices, 20, new BigDecimal("2"));
        MACD.MACDResult currentMACD = MACD.calculateMACD(closingPrices, 12, 26, 9);

        // Пример торгового решения
        if (buyNow.get() && currentRSI.compareTo(new BigDecimal("70")) > 0
                && currentMACD.macdLine.compareTo(currentMACD.signalLine) < 0
                && closingPrices.get(closingPrices.size() - 1).compareTo(currentBands.upperBand) > 0) {
            // Продаем, если короткая SMA ниже длинной SMA и текущая цена ниже короткой SMA
            log.info("Sell signal - Current Price: " + currentPrice);
            // Здесь можно добавить логику для выполнения продажи

            LimitOrderRequest request = new LimitOrderRequest();
            request.setRequest(MethodEnum.NEW_LIMIT_ORDER.getMethod());
            request.setMarket(TradePair.DBTC_DUSDT);
            BigDecimal buingAmount = AppProperties.BUY_AMOUNT.get();
            BigDecimal buingPrice = AppProperties.BUY_PRICE.get();
            request.setAmount(buingAmount);
            request.setPrice(currentPrice);
            request.setSide(SideEnum.sell);
            whiteBitClient.newLimitOrder(request);
            this.buyNow.set(false);

            log.info("---------------------------------------------------------------------------------------");
            log.info("Statistic: ");
            log.info("Buy on {}, sell on {}", buingPrice, currentPrice);
            BigDecimal different = currentPrice.subtract(buingPrice);
            log.info("different: {}", different);
            log.info("diffrrent in money: {}", different.multiply(buingAmount));
            log.info("---------------------------------------------------------------------------------------");
        } else if (!buyNow.get() && currentRSI.compareTo(new BigDecimal("30")) < 0
                && currentMACD.macdLine.compareTo(currentMACD.signalLine) > 0
                && closingPrices.get(closingPrices.size() - 1).compareTo(currentBands.lowerBand) < 0) {
            log.info("Buy signal - Current Price: " + currentPrice);
            // Здесь можно добавить логику для выполнения покупки

            BigDecimal buyAmount = getBuyAmount(new BigDecimal(100), currentPrice);
            AppProperties.BUY_AMOUNT.set(buyAmount);
            AppProperties.BUY_PRICE.set(currentPrice);
            LimitOrderRequest request = new LimitOrderRequest();
            request.setRequest(MethodEnum.NEW_LIMIT_ORDER.getMethod());
            request.setMarket(TradePair.DBTC_DUSDT);
            request.setAmount(buyAmount);
            request.setPrice(currentPrice);
            request.setSide(SideEnum.buy);
            whiteBitClient.newLimitOrder(request);
            this.buyNow.set(true);
        } else if (currentMACD.macdLine.compareTo(currentMACD.signalLine) > 0) {
            log.info("MACD Bullish Signal");
        } else if (currentMACD.macdLine.compareTo(currentMACD.signalLine) < 0) {
            log.info("MACD Bearish Signal");
        } else {
            log.info("Hold");
        }
    }

    /**
     * MACD, RSI, EMA, OrderBook
     * @param series
     * @param lastPrice
     * @param pair
     */
    private void strategy_3(BarSeries series, BigDecimal lastPrice, TradePair pair) {
        // MACD Indicator
        MACDIndicator macd = new MACDIndicator(new ClosePriceIndicator(series), 12, 26);
        EMAIndicator macdSignal = new EMAIndicator(macd, 9);
        Rule macdCrossUp = new CrossedUpIndicatorRule(macd, macdSignal);
        Rule macdCrossDown = new CrossedDownIndicatorRule(macd, macdSignal);

        // RSI Indicator
        RSIIndicator rsi = new RSIIndicator(new ClosePriceIndicator(series), 14);
        Rule rsiOverbought = new OverIndicatorRule(rsi, DecimalNum.valueOf(70));
        Rule rsiOversold = new UnderIndicatorRule(rsi, DecimalNum.valueOf(30));

        // SMA Indicator
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        EMAIndicator shortSma = new EMAIndicator(closePrice, 50);
        EMAIndicator longSma = new EMAIndicator(closePrice, 200);
        Rule smaCrossUp = new CrossedUpIndicatorRule(shortSma, longSma);
        Rule smaCrossDown = new CrossedDownIndicatorRule(shortSma, longSma);

        // Combine rules for strong signals
        Rule buyingRule = macdCrossUp.and(rsiOversold).and(smaCrossUp);
        Rule sellingRule = macdCrossDown.and(rsiOverbought).and(smaCrossDown);

        Strategy strategy = new BaseStrategy(buyingRule, sellingRule);
        TradingRecord tradingRecord = new BaseTradingRecord();
        int endIndex = series.getEndIndex();
        if (!buyNow.get()
                && AppProperties.ORDERBOOK_SIGNAL.get().equals(OrderBookStatusEnum.buy)
                && strategy.shouldEnter(endIndex)) {
            tradingRecord.enter(endIndex);
            log.info("Strong buy signal generated.");

            BigDecimal buyAmount = getBuyAmount(new BigDecimal(10), lastPrice);
            AppProperties.BUY_AMOUNT.set(buyAmount);
            AppProperties.BUY_PRICE.set(lastPrice);
            LimitOrderRequest request = new LimitOrderRequest();
            request.setRequest(MethodEnum.NEW_LIMIT_ORDER.getMethod());
            request.setMarket(TradePair.DBTC_DUSDT);
            request.setAmount(buyAmount);
            request.setPrice(lastPrice);
            request.setSide(SideEnum.buy);
            whiteBitClient.newLimitOrder(request);
            this.buyNow.set(true);
        } else if (buyNow.get()
                && AppProperties.ORDERBOOK_SIGNAL.get().equals(OrderBookStatusEnum.buy)
                && strategy.shouldExit(endIndex)) {
            tradingRecord.exit(endIndex);
            log.info("Strong sell signal generated.");

            LimitOrderRequest request = new LimitOrderRequest();
            request.setRequest(MethodEnum.NEW_LIMIT_ORDER.getMethod());
            request.setMarket(TradePair.DBTC_DUSDT);
            BigDecimal buingAmount = AppProperties.BUY_AMOUNT.get();
            BigDecimal buingPrice = AppProperties.BUY_PRICE.get();
            request.setAmount(buingAmount);
            request.setPrice(lastPrice);
            request.setSide(SideEnum.sell);
            whiteBitClient.newLimitOrder(request);
            this.buyNow.set(false);

            log.info("---------------------------------------------------------------------------------------");
            log.info("Statistic: ");
            log.info("Buy on {}, sell on {}", buingPrice, lastPrice);
            BigDecimal different = lastPrice.subtract(buingPrice);
            log.info("different: {}", different);
            log.info("diffrrent in money: {}", different.multiply(buingAmount));
            log.info("---------------------------------------------------------------------------------------");
        } else {
            log.info("No strong signal generated.");
        }
    }

    /**
     * EMA (buy), RSI, MACD(sell)
     * @param closingPrices
     */
    private void strategy_3(List<BigDecimal> closingPrices) {
        BigDecimal shortEMA = MACD.calculateEMAOrdinary(closingPrices, 8);
        BigDecimal longEMA = MACD.calculateEMAOrdinary(closingPrices, 20);
        BigDecimal rsi = RSI.calculateRSI(closingPrices, 14);
        MACD.MACDResult currentMACD = MACD.calculateMACD(closingPrices, 12, 26, 9);

        BigDecimal currentPrice = closingPrices.getLast();
        if (!buyNow.get()
                && currentPrice.compareTo(shortEMA) > 0
                && rsi.compareTo(BigDecimal.valueOf(30)) < 0) {
            // сигнал на покупку
            log.info("Buy signal - Current Price: " + currentPrice);
            // Здесь можно добавить логику для выполнения покупки

            BigDecimal buyAmount = getBuyAmount(new BigDecimal(100), currentPrice);
            AppProperties.BUY_AMOUNT.set(buyAmount);
            AppProperties.BUY_PRICE.set(currentPrice);

            LimitOrderRequest request = new LimitOrderRequest();
            request.setRequest(MethodEnum.NEW_LIMIT_ORDER.getMethod());
            request.setMarket(TradePair.DBTC_DUSDT);
            request.setAmount(buyAmount);
            request.setPrice(currentPrice);
            request.setSide(SideEnum.buy);
            whiteBitClient.newLimitOrder(request);
            this.buyNow.set(true);
        } else if (buyNow.get()
                && rsi.compareTo(BigDecimal.valueOf(70)) > 0
                && currentMACD.macdLine.compareTo(currentMACD.signalLine) < 0) {
            // сигнал на продажу
            log.info("Sell signal - Current Price: " + currentPrice);
            // Здесь можно добавить логику для выполнения продажи

            LimitOrderRequest request = new LimitOrderRequest();
            request.setRequest(MethodEnum.NEW_LIMIT_ORDER.getMethod());
            request.setMarket(TradePair.DBTC_DUSDT);
            BigDecimal buingAmount = AppProperties.BUY_AMOUNT.get();
            BigDecimal buingPrice = AppProperties.BUY_PRICE.get();
            request.setAmount(buingAmount);
            request.setPrice(currentPrice);
            request.setSide(SideEnum.sell);
            whiteBitClient.newLimitOrder(request);
            this.buyNow.set(false);

            log.info("---------------------------------------------------------------------------------------");
            log.info("Statistic: ");
            log.info("Buy on {}, sell on {}", buingPrice, currentPrice);
            BigDecimal different = currentPrice.subtract(buingPrice);
            log.info("different: {}", different);
            log.info("diffrrent in money: {}", different.multiply(buingAmount));
            log.info("---------------------------------------------------------------------------------------");
        }
    }

    private BigDecimal getBuyAmount(BigDecimal deposit, BigDecimal currentPrice) {
        return deposit.divide(currentPrice, 6, BigDecimal.ROUND_DOWN);
    }
}
