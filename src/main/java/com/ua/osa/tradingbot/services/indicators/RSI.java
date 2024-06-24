package com.ua.osa.tradingbot.services.indicators;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@UtilityClass
public class RSI {

    public static BigDecimal calculateRSI(List<BigDecimal> prices, int period) {
        return IntStream.range(period, prices.size())
                .mapToObj(i -> {
                    List<BigDecimal> subset = prices.subList(i - period, i);
                    BigDecimal avgGain = calculateAverageGain(subset);
                    BigDecimal avgLoss = calculateAverageLoss(subset);
                    BigDecimal rs = BigDecimal.ZERO;
                    if (avgLoss.compareTo(BigDecimal.ZERO) != 0) {
                        rs = avgGain.divide(avgLoss, 10, RoundingMode.HALF_UP);
                    }
                    return BigDecimal.valueOf(100).subtract(BigDecimal.valueOf(100).divide(BigDecimal.ONE.add(rs), 10, RoundingMode.HALF_UP));
                })
                .collect(Collectors.toList()).getLast();
    }

    private static BigDecimal calculateAverageGain(List<BigDecimal> prices) {
        BigDecimal gainSum = BigDecimal.ZERO;
        for (int i = 1; i < prices.size(); i++) {
            BigDecimal change = prices.get(i).subtract(prices.get(i - 1));
            if (change.compareTo(BigDecimal.ZERO) > 0) {
                gainSum = gainSum.add(change);
            }
        }
        return gainSum.divide(new BigDecimal(prices.size() - 1), 10, RoundingMode.HALF_UP);
    }

    private static BigDecimal calculateAverageLoss(List<BigDecimal> prices) {
        BigDecimal lossSum = BigDecimal.ZERO;
        for (int i = 1; i < prices.size(); i++) {
            BigDecimal change = prices.get(i).subtract(prices.get(i - 1));
            if (change.compareTo(BigDecimal.ZERO) < 0) {
                lossSum = lossSum.add(change.abs());
            }
        }
        return lossSum.divide(new BigDecimal(prices.size() - 1), 10, RoundingMode.HALF_UP);
    }
}
