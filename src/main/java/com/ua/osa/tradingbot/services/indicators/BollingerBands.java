package com.ua.osa.tradingbot.services.indicators;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

@UtilityClass
public class BollingerBands {

    public static BollingerBand calculateBollingerBands(List<BigDecimal> prices, int period, BigDecimal multiplier) {
        if (multiplier.compareTo(BigDecimal.ZERO) == 0) period = 2;

        int lastIndex = prices.size() - 1;
        List<BigDecimal> subset = prices.subList(lastIndex - period + 1, lastIndex + 1);
        BigDecimal sma = calculateSMA(subset);
        BigDecimal stdDev = calculateStandardDeviation(subset, sma);
        BigDecimal upperBand = sma.add(multiplier.multiply(stdDev));
        BigDecimal lowerBand = sma.subtract(multiplier.multiply(stdDev));
        BigDecimal lastPrice = prices.get(lastIndex);

        return new BollingerBand(sma, upperBand, lowerBand, lastPrice);
    }

    private static BigDecimal calculateSMA(List<BigDecimal> prices) {
        BigDecimal sum = prices.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(new BigDecimal(prices.size()), 10, RoundingMode.HALF_UP);
    }

    private static BigDecimal calculateStandardDeviation(List<BigDecimal> prices, BigDecimal sma) {
        BigDecimal sum = prices.stream()
                .map(price -> price.subtract(sma).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal variance = sum.divide(new BigDecimal(prices.size()), 10, RoundingMode.HALF_UP);
        return variance.sqrt(new MathContext(10));
    }

    public static class BollingerBand {
        BigDecimal sma;
        public BigDecimal upperBand;
        public BigDecimal lowerBand;
        public BigDecimal lastPrice;

        public BollingerBand(BigDecimal sma, BigDecimal upperBand, BigDecimal lowerBand, BigDecimal lastPrice) {
            this.sma = sma;
            this.upperBand = upperBand;
            this.lowerBand = lowerBand;
            this.lastPrice = lastPrice;
        }

        @Override
        public String toString() {
            return "BollingerBand{" +
                    "sma=" + sma +
                    ", upperBand=" + upperBand +
                    ", lowerBand=" + lowerBand +
                    ", lastPrice=" + lastPrice +
                    '}';
        }
    }
}
