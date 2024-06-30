package com.ua.osa.tradingbot.services.indicators;

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.AbstractIndicator;
import org.ta4j.core.num.Num;

public class FibonacciRetracementLevels extends AbstractIndicator<Num> {

    private final int period;
    private Num high;
    private Num low;

    public FibonacciRetracementLevels(BarSeries series, int period) {
        super(series);
        this.period = period;
    }

    private void calculateHighLow() {
        high = getBarSeries().getBar(getBarSeries().getEndIndex()).getHighPrice();
        low = getBarSeries().getBar(getBarSeries().getEndIndex()).getLowPrice();

        for (int i = Math.max(0, getBarSeries().getEndIndex() - period + 1); i <= getBarSeries().getEndIndex(); i++) {
            if (getBarSeries().getBar(i).getHighPrice().isGreaterThan(high)) {
                high = getBarSeries().getBar(i).getHighPrice();
            }
            if (getBarSeries().getBar(i).getLowPrice().isLessThan(low)) {
                low = getBarSeries().getBar(i).getLowPrice();
            }
        }
    }

    public Num getLevel(double ratio) {
        calculateHighLow();
        return high.minus(high.minus(low).multipliedBy(numOf(ratio)));
    }

    @Override
    public Num getValue(int i) {
        return getLevel(0.5);
    }

    @Override
    public int getUnstableBars() {
        return 0;
    }
}
