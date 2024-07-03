package com.ua.osa.tradingbot.services;

import com.ua.osa.tradingbot.models.dto.enums.TradePair;
import com.ua.osa.tradingbot.models.entity.StrategyStatistic;
import com.ua.osa.tradingbot.repository.StrategyStatisticRepository;
import com.ua.osa.tradingbot.scheduler.TaskManager;
import com.ua.osa.tradingbot.services.ai.AiService;
import com.ua.osa.tradingbot.services.ai.dto.OperationEnum;
import com.ua.osa.tradingbot.services.indicators.FibonacciRetracementLevels;
import com.ua.osa.tradingbot.services.indicators.IndicatorEnum;
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

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static com.ua.osa.tradingbot.services.ai.dto.OperationEnum.*;

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
    private final TaskManager taskManager;
    private final StrategyStatisticRepository repository;

    @Override
    public OperationEnum getOperationByLastKline(BarSeries series) {
        this.ai = aiService.getRecommendedOperation(series);

        final List<Strategy> strategies = IndicatorEnum.buildIndicatorStrategies(series);
        int entire = 0;
        int exit = 0;
        int wait = 0;
        switch (this.ai) {
            case BUY -> entire++;
            case SELL -> exit++;
            case WAIT -> wait++;
        }
        int endIndex = series.getEndIndex();
        log.info("-----------------");
        log.info("AI: {}.", this.ai);
        StrategyStatistic strategyStatistic = new StrategyStatistic();
        strategyStatistic.setAi(this.ai.ordinal());
        strategyStatistic.setClosePrice(BigDecimal.valueOf(series.getLastBar().getClosePrice().doubleValue()));
        for (Strategy strategy : strategies) {
            int result = 0;
            if (strategy.shouldEnter(endIndex)) {
                entire++;
                log.info("{}: {}", strategy.getName(), BUY.name());
                result = 1;
            } else if (strategy.shouldExit(endIndex)) {
                exit++;
                log.info("{}: {}", strategy.getName(), SELL.name());
                result = 2;
            } else {
                wait++;
                log.info("{}: {}", strategy.getName(), WAIT.name());
            }
            addStrategyResult(strategy, strategyStatistic, result);
        }
        log.info("-----------------");
        log.info("Results:");
        log.info("For BUY: {}.", entire);
        log.info("For SELL: {}.", exit);
        log.info("For WAIT: {}.", wait);

        OperationEnum result = WAIT;
        if (entire > 3) {
            log.info("BUYING...");
            result = BUY;
        } else if (exit > 3) {
            log.info("SELLING...");
            result = OperationEnum.SELL;
        }
        log.info("WAITING...");

        taskManager.execute(() -> {
            try {
                strategyStatistic.setTimestamp(new Date());

                repository.save(strategyStatistic);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });

        return result;
    }

    private void addStrategyResult(Strategy strategy, StrategyStatistic strategyStatistic, int tmp) {
        String strategyName = strategy.getName();
        if (IndicatorEnum.MA.name().equals(strategyName)) {
            strategyStatistic.setMa(tmp);
        } else if (IndicatorEnum.BOLLINGER_BANDS.name().equals(strategyName)) {
            strategyStatistic.setBb(tmp);
        } else if (IndicatorEnum.RSI.name().equals(strategyName)) {
            strategyStatistic.setRsi(tmp);
        } else if (IndicatorEnum.MACD.name().equals(strategyName)) {
            strategyStatistic.setMacd(tmp);
        } else if (IndicatorEnum.STOCHASTIC_OSCILLATOR.name().equals(strategyName)) {
            strategyStatistic.setStockRsi(tmp);
        } else if (IndicatorEnum.ADX.name().equals(strategyName)) {
            strategyStatistic.setAdx(tmp);
        }
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
        // TODO: 03.07.2024
    }
}
