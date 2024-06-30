package com.ua.osa.tradingbot.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ua.osa.tradingbot.services.indicators.MACD;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderBookServiceImpl implements OrderBookService {
    private final SortedMap<Double, Double> asks = MapUtils.synchronizedSortedMap(new TreeMap<Double, Double>(Comparator.reverseOrder()));
    private final SortedMap<Double, Double> bids = MapUtils.synchronizedSortedMap(new TreeMap<Double, Double>(Comparator.naturalOrder()));
    private volatile double timestamp;
    private final List<Double> dif = new CopyOnWriteArrayList<>();
    private final List<Double> shortEma = new CopyOnWriteArrayList<>();
    private final List<Double> longEma = new CopyOnWriteArrayList<>();

    @Override
    public synchronized void updateOrderBook(String message) {
        JsonElement paramsElement = JsonParser.parseString(message).getAsJsonObject().get("params");
        JsonArray paramsArray = paramsElement.getAsJsonArray();
        JsonObject element = paramsArray.get(1).getAsJsonObject();
        this.timestamp = element.getAsJsonObject().get("timestamp").getAsDouble();
        JsonArray asksUpdateList = Optional.ofNullable(element.getAsJsonObject().get("asks")).map(JsonElement::getAsJsonArray).orElse(null);
        JsonArray bidsUpdateList = Optional.ofNullable(element.getAsJsonObject().get("bids")).map(JsonElement::getAsJsonArray).orElse(null);

        updateData(asksUpdateList, asks);
        updateData(bidsUpdateList, bids);

        double totalAskVolume = asks.values().stream().mapToDouble(Double::doubleValue).sum();
        double totalBidVolume = bids.values().stream().mapToDouble(Double::doubleValue).sum();

        double volumeDifference = totalBidVolume - totalAskVolume;

        dif.add(volumeDifference);

        if (dif.size() > 20) {
            double shortEMA = MACD.calculateEMAOrdinary(dif, 5);
            double longEMA = MACD.calculateEMAOrdinary(dif, 200);
            this.shortEma.add(shortEMA);
            this.longEma.add(longEMA);
        }
    }

    private void updateData(JsonArray updateList, Map<Double, Double> orders) {
        if (Objects.nonNull(updateList)) {
            for (JsonElement jsonElement : updateList) {
                JsonArray innerArray = jsonElement.getAsJsonArray();
                double price = innerArray.get(0).getAsDouble();
                double amount = innerArray.get(1).getAsDouble();
                if (amount == 0) {
                    orders.remove(price);
                } else {
                    orders.put(price, amount);
                }
            }
        }
    }
}
