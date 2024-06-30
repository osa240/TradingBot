package com.ua.osa.tradingbot.services;

import com.ua.osa.tradingbot.models.dto.enums.TradePair;
import com.ua.osa.tradingbot.services.ai.AiService;
import com.ua.osa.tradingbot.services.ai.dto.OperationEnum;
import com.ua.osa.tradingbot.services.indicators.FibonacciRetracementLevels;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.ta4j.core.*;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.adx.MinusDIIndicator;
import org.ta4j.core.indicators.adx.PlusDIIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.num.Num;
import ta4jexamples.strategies.*;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class StrategyServiceImpl implements StrategyService {

    private TradePair pair;

    /**
     * Last close price
     */
    private ClosePriceIndicator closePrice;

    /**
     * AI
     */
    private OperationEnum ai;

    /**
     * RSI
     */
    private RSIIndicator rsi;

    /**
     * STOCHASTIC_RSI
     */
    private StochasticOscillatorKIndicator stochasticOscK;
    private StochasticOscillatorDIndicator stochasticOscD;

    /**
     * VOLUME
     */
    private VolumeIndicator volume;
    private SMAIndicator averageVolume;

    /**
     * SMA
     */
    private SMAIndicator shortSma;
    private SMAIndicator longSma;

    /**
     * DMI
     */
    private PlusDIIndicator plusDI;
    private MinusDIIndicator minusDI;
    private ADXIndicator adx;

    /**
     * EMA
     */
    private EMAIndicator shortEma;
    private EMAIndicator longEma;

    /**
     * MACD
     */
    private MACDIndicator macd;
    private EMAIndicator macdSignal;

    /**
     * BOLLINGER BANDS
     */
    private EMAIndicator sma;
    private StandardDeviationIndicator standardDeviation;
    private Num factor;
    private BollingerBandsMiddleIndicator middleBand;
    private BollingerBandsUpperIndicator upperBand;
    private BollingerBandsLowerIndicator lowerBand;

    /**
     * FIBONACHI
     */
    private FibonacciRetracementLevels fibRetracement;

    private final AiService aiService;

    @Override
    public OperationEnum getOperationByLastKline(BarSeries series) {
        this.ai = aiService.getRecommendedOperation(series);
//        initializeIndicators(series);
        List<Strategy> strategies = List.of(
                ADXStrategy.buildStrategy(series),
                CCICorrectionStrategy.buildStrategy(series),
                GlobalExtremaStrategy.buildStrategy(series),
                MovingMomentumStrategy.buildStrategy(series),
                RSI2Strategy.buildStrategy(series),
                UnstableIndicatorStrategy.buildStrategy(series)
        );
        int entire = 0;
        int exit = 0;
        int wait = 0;
        switch (this.ai) {
            case BUY -> entire++;
            case SELL -> exit++;
            case WAIT -> wait++;
        }
        for (Strategy strategy : strategies) {
            if (strategy.shouldEnter(series.getEndIndex())) {
                entire++;
            } else if (strategy.shouldExit(series.getEndIndex())) {
                exit++;
            } else {
                wait++;
            }
        }
        log.info("Результаты:");
        log.info("За покупку: {}.", entire);
        log.info("За продажу: {}.", exit);
        log.info("За ожидание: {}.", wait);
        log.info("-----------------");
        if (entire > 3) {
            log.info("Решение о покупке принято.");
            return OperationEnum.BUY;
        } else if (exit > 3) {
            log.info("Решение о продаже принято.");
            return OperationEnum.SELL;
        }
        log.info("Решение о ожидании принято.");
        return OperationEnum.WAIT;
    }

    private void initializeIndicators(BarSeries series) {
        this.closePrice = new ClosePriceIndicator(series);
        this.rsi = new RSIIndicator(closePrice, 14);
        this.stochasticOscK = new StochasticOscillatorKIndicator(series, 14);
        this.stochasticOscD = new StochasticOscillatorDIndicator(stochasticOscK);
        this.volume = new VolumeIndicator(series, 20);
        this.averageVolume = new SMAIndicator(volume, 20);
        this.shortSma = new SMAIndicator(closePrice, 5);
        this.longSma = new SMAIndicator(closePrice, 200);
        this.plusDI = new PlusDIIndicator(series, 14);
        this.minusDI = new MinusDIIndicator(series, 14);
        this.adx = new ADXIndicator(series, 14);
        this.shortEma = new EMAIndicator(closePrice, 5);
        this.longEma = new EMAIndicator(closePrice, 200);
        this.macd = new MACDIndicator(closePrice, 12, 26);
        this.macdSignal = new EMAIndicator(macd, 9);
        this.sma = new EMAIndicator(closePrice, 20);
        this.standardDeviation = new StandardDeviationIndicator(closePrice, 20);
        this.factor = series.numOf(2);
        this.middleBand = new BollingerBandsMiddleIndicator(sma);
        this.upperBand = new BollingerBandsUpperIndicator(middleBand, standardDeviation, factor);
        this.lowerBand = new BollingerBandsLowerIndicator(middleBand, standardDeviation, factor);
        this.fibRetracement = new FibonacciRetracementLevels(series, 4);

    }
}
