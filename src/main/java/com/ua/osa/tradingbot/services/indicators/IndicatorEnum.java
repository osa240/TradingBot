package com.ua.osa.tradingbot.services.indicators;

import java.util.ArrayList;
import java.util.List;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.adx.MinusDIIndicator;
import org.ta4j.core.indicators.adx.PlusDIIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

public enum IndicatorEnum {
    AI,
    MA,
    BOLLINGER_BANDS,
    RSI,
    MACD,
    STOCHASTIC_OSCILLATOR,
    ADX;

    public static List<Strategy> buildIndicatorStrategies(BarSeries series) {
        List<Strategy> result = new ArrayList<>();
        result.add(buildMa(series));
        result.add(buildBollingerBands(series));
        result.add(buildRsi(series));
        result.add(buildMacd(series));
        result.add(buildStickastikOscillator(series));
        result.add(buildAdx(series));
        return result;
    }

    private static Strategy buildMacd(BarSeries series) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);
        EMAIndicator signal = new EMAIndicator(macd, 9);

        Rule entryRule = new CrossedUpIndicatorRule(macd, signal);
        Rule exitRule = new CrossedDownIndicatorRule(macd, signal);

        return new BaseStrategy(MACD.name(), entryRule, exitRule);
    }

    private static Strategy buildAdx(BarSeries series) {
        ADXIndicator adx = new ADXIndicator(series, 14);
        PlusDIIndicator plusDI = new PlusDIIndicator(series, 14);
        MinusDIIndicator minusDI = new MinusDIIndicator(series, 14);

        Rule entryRule = new OverIndicatorRule(adx, 20)
                .and(new CrossedUpIndicatorRule(plusDI, minusDI));
        Rule exitRule = new OverIndicatorRule(adx, 20)
                .and(new CrossedDownIndicatorRule(plusDI, minusDI));

        return new BaseStrategy(ADX.name(), entryRule, exitRule);
    }

    private static Strategy buildRsi(BarSeries series) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        RSIIndicator rsi = new RSIIndicator(closePrice, 14);

        Rule entryRule = new UnderIndicatorRule(rsi, 30);
        Rule exitRule = new OverIndicatorRule(rsi, 70);

        return new BaseStrategy(RSI.name(), entryRule, exitRule);
    }

    private static Strategy buildStickastikOscillator(BarSeries series) {
        StochasticOscillatorKIndicator stochasticOscillatorK =
                new StochasticOscillatorKIndicator(series, 14);

        Rule entryRule = new CrossedUpIndicatorRule(stochasticOscillatorK, 20);
        Rule exitRule = new CrossedDownIndicatorRule(stochasticOscillatorK, 80);

        return new BaseStrategy(STOCHASTIC_OSCILLATOR.name(), entryRule, exitRule);
    }

    private static Strategy buildBollingerBands(BarSeries series) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        SMAIndicator sma = new SMAIndicator(closePrice, 20);
        BollingerBandsMiddleIndicator middleBand = new BollingerBandsMiddleIndicator(sma);
        BollingerBandsUpperIndicator upperBand = new BollingerBandsUpperIndicator(middleBand,
                new StandardDeviationIndicator(closePrice, 20), series.numOf(2));
        BollingerBandsLowerIndicator lowerBand = new BollingerBandsLowerIndicator(middleBand,
                new StandardDeviationIndicator(closePrice, 20), series.numOf(2));

        Rule entryRule = new CrossedUpIndicatorRule(closePrice, lowerBand);
        Rule exitRule = new CrossedDownIndicatorRule(closePrice, upperBand);

        return new BaseStrategy(BOLLINGER_BANDS.name(), entryRule, exitRule);
    }

    private static Strategy buildMa(BarSeries series) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        SMAIndicator shortSma = new SMAIndicator(closePrice, 50);
        SMAIndicator longSma = new SMAIndicator(closePrice, 200);

        Rule entryRule = new CrossedUpIndicatorRule(shortSma, longSma);
        Rule exitRule = new CrossedDownIndicatorRule(shortSma, longSma);

        return new BaseStrategy(MA.name(), entryRule, exitRule);
    }

    public static IndicatorEnum getNameByIndex(int index) {
        for (IndicatorEnum value : values()) {
            if (value.ordinal() == index) {
                return value;
            }
        }
        return null;
    }
}
