package com.ua.osa.tradingbot.services;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.ua.osa.tradingbot.models.dto.enums.WebSocketMethodEnum;
import com.ua.osa.tradingbot.websocket.protocol.MessageRequest;
import com.ua.osa.tradingbot.websocket.protocol.dto.TradeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class CollectDataServiceImpl implements CollectDataService {

    private static final int TRADE_PAIR_POSITION = 0;
    private static final int LAST_PRICE_POSITION = 1;

    private final Map<String, List<BigDecimal>> lastPriceData = new ConcurrentHashMap<>();
    private final Map<String, List<TradeDto>> lastTradeData = new ConcurrentHashMap<>();
    private final ProcessingService processingService;


    @Override
    public synchronized void collectDataFromWebSocket(String message) {
        try {
            if (message != null) {
                if (message.contains(WebSocketMethodEnum.lastprice_subscribe.getMethod())) {
                    collectLastPrice(message);
                } else if (message.contains(WebSocketMethodEnum.trades_subscribe.getMethod())) {
                    collectTrades(message);
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
            BigDecimal lastPrice = BigDecimal.valueOf(Double.parseDouble(getStringFromParams(params, LAST_PRICE_POSITION)));
            List<BigDecimal> lastPriceList = lastPriceData.get(tradePair);
            if (CollectionUtils.isEmpty(lastPriceList)) {
                lastPriceList = new LinkedList<>();
                lastPriceData.put(tradePair, lastPriceList);
            }
            lastPriceList.add(lastPrice);
            processingService.processingLastPrice(lastPrice);
        }
    }

    private String getStringFromParams(List<Object> params, int i) {
        return (String) params.get(i);
    }
}
