package com.ua.osa.tradingbot.services;

import java.math.BigDecimal;
import java.util.List;
import org.ta4j.core.BarSeries;

public interface ProcessingService {

    void processingLastPrice(List<BigDecimal> price);

    void processingKlains(BarSeries series);
}
