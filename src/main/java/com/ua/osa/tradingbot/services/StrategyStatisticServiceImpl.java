package com.ua.osa.tradingbot.services;

import com.ua.osa.tradingbot.models.dto.TradingRecord;
import com.ua.osa.tradingbot.models.dto.TradingRecordOrderBook;
import com.ua.osa.tradingbot.models.entity.OrderBookStatistic;
import com.ua.osa.tradingbot.models.entity.StrategyStatistic;
import com.ua.osa.tradingbot.repository.OrderBookStatisticRepository;
import com.ua.osa.tradingbot.repository.StrategyStatisticRepository;
import com.ua.osa.tradingbot.services.indicators.IndicatorEnum;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class StrategyStatisticServiceImpl implements StrategyStatisticService {

    private final StrategyStatisticRepository repository;
    private final OrderBookStatisticRepository orderBookRepository;
    private final ReportService reportService;
    private final Map<Integer, List<TradingRecord>> statisticMap = new HashMap<>();

    @Override
    public void save(StrategyStatistic strategyStatistic) {
        repository.save(strategyStatistic);
    }

    @Override
    public void generateStatistic() {
        Iterable<StrategyStatistic> statistics = repository.findAll();

        for (StrategyStatistic statistic : statistics) {
            for (IndicatorEnum value : IndicatorEnum.values()) {
                int result = 0;
                if (IndicatorEnum.AI == value) {
                    result = statistic.getAi();
                } else if (IndicatorEnum.MA == value) {
                    result = statistic.getMa();
                } else if (IndicatorEnum.BOLLINGER_BANDS == value) {
                    result = statistic.getBb();
                } else if (IndicatorEnum.RSI == value) {
                    result = statistic.getRsi();
                } else if (IndicatorEnum.MACD == value) {
                    result = statistic.getMacd();
                } else if (IndicatorEnum.STOCHASTIC_OSCILLATOR == value) {
                    result = statistic.getStockRsi();
                } else if (IndicatorEnum.ADX == value) {
                    result = statistic.getAdx();
                }
                checkResult(statistic, result, value.ordinal());
            }
        }

        reportService.generateReportStrategyStatistic(statisticMap);
        statisticMap.clear();
    }

    @Override
    public void generateOrderBookStatistic() {
        List<OrderBookStatistic> all = orderBookRepository.findAll();
        int size = all.size();
        if (!all.getLast().getIsOpen()) {
            size--;
        }

        List<TradingRecordOrderBook> result = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            OrderBookStatistic orderBookStatistic = all.get(i);
            if (!orderBookStatistic.getIsOpen()) {
                result.add(new TradingRecordOrderBook(
                        orderBookStatistic.getTimestamp(),
                        orderBookStatistic.getClosePrice(),
                        orderBookStatistic.getBidsTotalAmount(),
                        orderBookStatistic.getAsksTotalAmount(),
                        null,
                        null,
                        null,
                        null,
                        null));
            } else if (!result.isEmpty()) {
                TradingRecordOrderBook last = result.getLast();

                last.setTimestampClose(orderBookStatistic.getTimestamp());
                last.setPriceClose(orderBookStatistic.getClosePrice());
                last.setAsksAmountClose(orderBookStatistic.getAsksTotalAmount());
                last.setBidsAmountClose(orderBookStatistic.getBidsTotalAmount());

                last.setDif(last.getPriceClose().subtract(last.getPriceOpen()));
            }
        }
        reportService.generateReportStrategyStatistic(result);
    }

    private void checkResult(StrategyStatistic statistic, int result, int index) {
        if (result == 1) {
            List<TradingRecord> tradingRecords = statisticMap.get(index);
            if (CollectionUtils.isNotEmpty(tradingRecords)) {
                TradingRecord last = tradingRecords.getLast();
                if (last == null || last.getPriceClose() != null) {
                    createNewTradingRecord(statistic, tradingRecords, index);
                }
            } else {
                tradingRecords = new LinkedList<>();
                createNewTradingRecord(statistic, tradingRecords, index);
                statisticMap.put(index, tradingRecords);
            }
        } else if (result == 2) {
            List<TradingRecord> tradingRecords = statisticMap.get(index);
            if (CollectionUtils.isNotEmpty(tradingRecords)) {
                TradingRecord last = tradingRecords.getLast();
                if (last != null && last.getPriceClose() == null) {
                    last.setTimestampClose(statistic.getTimestamp());
                    last.setPriceClose(statistic.getClosePrice());
                    last.setDif(last.getPriceClose().subtract(last.getPriceOpen()));
                }
            }
        }
    }

    private void createNewTradingRecord(StrategyStatistic statistic,
                                        List<TradingRecord> tradingRecords,
                                        int index) {
        TradingRecord newTradingRecord = new TradingRecord();
        newTradingRecord.setIndicator(IndicatorEnum.getNameByIndex(index));
        newTradingRecord.setTimestampOpen(statistic.getTimestamp());
        newTradingRecord.setPriceOpen(statistic.getClosePrice());
        tradingRecords.add(newTradingRecord);
    }
}
