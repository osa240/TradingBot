package com.ua.osa.tradingbot.services;

import com.ua.osa.tradingbot.models.dto.TradingRecord;
import com.ua.osa.tradingbot.models.dto.TradingRecordOrderBook;
import java.util.List;
import java.util.Map;

public interface ReportService {
    void generateReportStrategyStatistic(Map<Integer, List<TradingRecord>> statisticMap);

    void generateReportStrategyStatistic(List<TradingRecordOrderBook> statistic);
}
