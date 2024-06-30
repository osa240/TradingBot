package com.ua.osa.tradingbot.services;

import com.ua.osa.tradingbot.services.ai.dto.OperationEnum;
import org.ta4j.core.BarSeries;

public interface StrategyService {
    OperationEnum getOperationByLastKline(BarSeries series);
}
