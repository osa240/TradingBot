package com.ua.osa.tradingbot.services.indicators;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@UtilityClass
public class MACD {

    public static MACDResult calculateMACD(List<BigDecimal> prices, int shortPeriod, int longPeriod, int signalPeriod) {
        List<BigDecimal> shortEMA = calculateEMA(prices, shortPeriod);
        List<BigDecimal> longEMA = calculateEMA(prices, longPeriod);

        List<BigDecimal> macdLine = IntStream.range(0, prices.size())
                .mapToObj(i -> shortEMA.get(i).subtract(longEMA.get(i)))
                .collect(Collectors.toList());

        List<BigDecimal> signalLine = calculateEMA(macdLine, signalPeriod);

        return IntStream.range(0, prices.size())
                .mapToObj(i -> new MACDResult(macdLine.get(i), signalLine.get(i), macdLine.get(i).subtract(signalLine.get(i))))
                .collect(Collectors.toList()).getLast();
    }

    public static List<BigDecimal> calculateEMA(List<BigDecimal> prices, int period) {
        final BigDecimal multiplier = BigDecimal.valueOf(2).divide(BigDecimal.valueOf(period + 1), 10, RoundingMode.HALF_UP);
        final BigDecimal[] previousEMA = {prices.stream()
                .limit(period)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(period), 10, RoundingMode.HALF_UP)};

        return IntStream.range(0, prices.size())
                .mapToObj(i -> {
                    if (i < period) {
                        return previousEMA[0];
                    }
                    BigDecimal price = prices.get(i);
                    previousEMA[0] = price.subtract(previousEMA[0]).multiply(multiplier).add(previousEMA[0]);
                    return previousEMA[0];
                })
                .collect(Collectors.toList());
    }

    public BigDecimal calculateEMAOrdinaryBD(List<BigDecimal> prices, int period) {
        BigDecimal multiplier = BigDecimal.valueOf(2.0 / (period + 1));
        BigDecimal ema = prices.get(0); // начальное значение EMA

        for (int i = 1; i < prices.size(); i++) {
            ema = prices.get(i).subtract(ema).multiply(multiplier).add(ema);
        }
        return ema;
    }

    public double calculateEMAOrdinary(List<Double> data, int period) {
        double multiplier = 2.0 / (period + 1);
        double ema = data.get(0); // начальное значение EMA

        for (int i = 1; i < data.size(); i++) {
            ema = ((data.get(i) - ema) * multiplier) + ema;
        }
        return ema;
    }

    public static class MACDResult {
        public BigDecimal macdLine;
        public BigDecimal signalLine;
        BigDecimal histogram;

        public MACDResult(BigDecimal macdLine, BigDecimal signalLine, BigDecimal histogram) {
            this.macdLine = macdLine;
            this.signalLine = signalLine;
            this.histogram = histogram;
        }

        @Override
        public String toString() {
            return "MACDResult{" +
                    "macdLine=" + macdLine +
                    ", signalLine=" + signalLine +
                    ", histogram=" + histogram +
                    '}';
        }
    }
}
