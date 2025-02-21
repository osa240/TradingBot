package com.ua.osa.tradingbot.services;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.ua.osa.tradingbot.BotSettings;
import com.ua.osa.tradingbot.models.dto.enums.WebSocketMethodEnum;
import com.ua.osa.tradingbot.models.dto.publicReq.kline.KlineResponse;
import com.ua.osa.tradingbot.restClients.WhiteBitClient;
import com.ua.osa.tradingbot.websocket.protocol.MessageRequest;
import com.ua.osa.tradingbot.websocket.protocol.dto.TradeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

@Service
@Slf4j
@RequiredArgsConstructor
public class CollectDataServiceImpl implements CollectDataService {

    private static final int TRADE_PAIR_POSITION = 0;
    private static final int LAST_PRICE_POSITION = 1;

    private final Map<String, List<BigDecimal>> lastPriceData = new ConcurrentHashMap<>();
    private final Map<String, List<TradeDto>> lastTradeData = new ConcurrentHashMap<>();
    private final ProcessingService processingService;
    private final AtomicReference<BarSeries> series = new AtomicReference<>();
    private final WhiteBitClient whiteBitClient;
    private final OrderBookService orderBookService;

    {
        series.set(new BaseBarSeries(BotSettings.TRADE_PAIR.get().name()));
    }

    @Override
    public synchronized void collectDataFromWebSocket(String message) {
        try {
            if (message != null) {
                if (message.contains(WebSocketMethodEnum.lastprice_subscribe.getMethod())) {
                    collectLastPrice(message);
                } else if (message.contains(WebSocketMethodEnum.trades_subscribe.getMethod())) {
                    collectTrades(message);
                } else if (message.contains(WebSocketMethodEnum.candles_subscribe.getMethod())) {
                    collectCandles(message);
                } else if (message.contains(WebSocketMethodEnum.depth_subscribe.getMethod())) {
                    orderBookService.updateOrderBook(message);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void collectTrades(String message) {
        JsonElement paramsElement = JsonParser.parseString(message).getAsJsonObject().get("params");
        JsonArray paramsArray = paramsElement.getAsJsonArray();
        String tradePair = paramsArray.get(TRADE_PAIR_POSITION).getAsString();
        JsonElement tradesElement = paramsArray.get(LAST_PRICE_POSITION);
        Type tradeListType = new TypeToken<List<TradeDto>>() {}.getType();
        List<TradeDto> tradesFromWebSocket = new Gson().fromJson(tradesElement, tradeListType);

        if (!CollectionUtils.isEmpty(tradesFromWebSocket)) {
            List<TradeDto> tradeDtos = lastTradeData.get(tradePair);
            if (CollectionUtils.isEmpty(tradeDtos)) {
                tradeDtos = new LinkedList<>();
                lastTradeData.put(tradePair, tradeDtos);
            }
            tradeDtos.addAll(tradesFromWebSocket);
        }
    }

    private void collectLastPrice(String message) {
        MessageRequest messageRequest = new Gson().fromJson(message, MessageRequest.class);
        List<Object> params = messageRequest.getParams();
        if (!CollectionUtils.isEmpty(params)) {
            String tradePair = getStringFromParams(params, TRADE_PAIR_POSITION);
            BigDecimal lastPrice = getBigDecimalFromParams(params);
            List<BigDecimal> lastPriceList = lastPriceData.get(tradePair);
            if (CollectionUtils.isEmpty(lastPriceList)) {
                lastPriceList = new LinkedList<>();
                lastPriceData.put(tradePair, lastPriceList);
            }
            lastPriceList.add(lastPrice);
            processingService.processingLastPrice(lastPriceList);
        }
    }

    private String getStringFromParams(List<Object> params, int i) {
        return (String) params.get(i);
    }

    private void collectCandles(String message) {
        BarSeries barSeries = series.get();
        if (barSeries.isEmpty()) {
            KlineResponse response = whiteBitClient.getKlains(BotSettings.TRADE_PAIR.get().name(), "1m", "1440");
            for (List<Object> kline : response.getResult()) {
                long timestamp = ((Number) kline.get(0)).longValue();
                ZonedDateTime endTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault());
                Num open = DecimalNum.valueOf((String) kline.get(1));
                Num high = DecimalNum.valueOf((String) kline.get(3));
                Num low = DecimalNum.valueOf((String) kline.get(4));
                Num close = DecimalNum.valueOf((String) kline.get(2));
                Num volumeInStock = DecimalNum.valueOf((String) kline.get(5));
                Num volumeInMoney = DecimalNum.valueOf((String) kline.get(6));
                BaseBar bar = new BaseBar(
                        Duration.ofMinutes(5),
                        endTime, open, high, low, close, volumeInStock, volumeInMoney
                );
                addNewBar(barSeries, bar);
            }
        }
        JsonElement paramsElement = JsonParser.parseString(message).getAsJsonObject().get("params");
        JsonArray paramsArray = paramsElement.getAsJsonArray();
        for (JsonElement jsonElement : paramsArray.asList()) {
            JsonArray asJsonArray = jsonElement.getAsJsonArray();

            long timestamp = asJsonArray.get(0).getAsLong();
            ZonedDateTime endTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault());
            Num open = DecimalNum.valueOf(asJsonArray.get(1).getAsDouble());
            Num high = DecimalNum.valueOf(asJsonArray.get(3).getAsDouble());
            Num low = DecimalNum.valueOf(asJsonArray.get(4).getAsDouble());
            Num close = DecimalNum.valueOf(asJsonArray.get(2).getAsDouble());
            Num volumeInStock = DecimalNum.valueOf(asJsonArray.get(5).getAsDouble());
            Num volumeInMoney = DecimalNum.valueOf(asJsonArray.get(6).getAsDouble());

            BaseBar bar = new BaseBar(Duration.ofMinutes(1), endTime, open, high, low, close, volumeInStock, volumeInMoney);
            addNewBar(barSeries, bar);
        }
        processingService.processingKlains(barSeries);
    }

    private void addNewBar(BarSeries barSeries, BaseBar bar) {
        try {
            if (!barSeries.isEmpty()) {
                Bar lastBar = barSeries.getLastBar();
                if (lastBar.getEndTime().isEqual(bar.getEndTime())) {
                    barSeries.addBar(bar, true);
                } else {
                    barSeries.addBar(bar);
                }
            } else {
                barSeries.addBar(bar);
            }
        } catch (Exception e) {
            log.error("Duplicate bar");
        }
    }

    private BigDecimal getBigDecimalFromParams(List<Object> params) {
        return BigDecimal.valueOf(Double.parseDouble(getStringFromParams(params, CollectDataServiceImpl.LAST_PRICE_POSITION)));
    }
}
