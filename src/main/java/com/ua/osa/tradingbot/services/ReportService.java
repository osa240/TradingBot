package com.ua.osa.tradingbot.services;

import com.ua.osa.tradingbot.models.dto.TradingRecord;

import java.util.List;
import java.util.Map;

public interface ReportService {
    void generateReportStrategyStatistic(Map<Integer, List<TradingRecord>> statisticMap);
}
