package com.ua.osa.tradingbot.services;

import com.ua.osa.tradingbot.models.dto.enums.MethodEnum;
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

@Component
@Slf4j
@RequiredArgsConstructor
public class DecisionServiceImpl implements DecisionService {
    private static BigDecimal buingPrice = BigDecimal.ZERO;
    private static BigDecimal buingAmount = BigDecimal.ZERO;

    private final AtomicBoolean buyNow = new AtomicBoolean(false);
    private final WhiteBitClient whiteBitClient;

    @Override
    public void makeDecisionByPriceHistory(List<BigDecimal> closingPrices, TradePair pair) {
        strategy_1(closingPrices);
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

            DecisionServiceImpl.buingPrice = currentPrice;
            DecisionServiceImpl.buingAmount = getBuyAmount(new BigDecimal(100), currentPrice);

            LimitOrderRequest request = new LimitOrderRequest();
            request.setRequest(MethodEnum.NEW_LIMIT_ORDER.getMethod());
            request.setMarket(TradePair.DBTC_DUSDT);
            request.setAmount(buingAmount);
            request.setPrice(buingPrice);
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

    private BigDecimal getBuyAmount(BigDecimal deposit, BigDecimal currentPrice) {
        return deposit.divide(currentPrice, 6, BigDecimal.ROUND_DOWN);
    }

    private void strategy_2(List<BigDecimal> closingPrices) {

    }
}
