package com.ua.osa.tradingbot.services;

import java.math.BigDecimal;
import org.ta4j.core.BarSeries;

public interface ProcessingService {

    void processingLastPrice(BigDecimal price);
    void processingKlains(BarSeries series);
}
