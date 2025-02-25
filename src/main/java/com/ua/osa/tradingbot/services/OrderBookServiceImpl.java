package com.ua.osa.tradingbot.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ua.osa.tradingbot.models.dto.OrderBook;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderBookServiceImpl implements OrderBookService {
    private final AtomicReference<OrderBook> orderBook = new AtomicReference<>(new OrderBook());
    private volatile double timestamp;

    @Override
    public synchronized void updateOrderBook(String message) {
        JsonElement paramsElement = JsonParser.parseString(message).getAsJsonObject().get("params");
        JsonArray paramsArray = paramsElement.getAsJsonArray();
        JsonObject element = paramsArray.get(1).getAsJsonObject();
        this.timestamp = element.getAsJsonObject().get("timestamp").getAsDouble();
        JsonArray asksUpdateList = Optional.ofNullable(element.getAsJsonObject()
                .get("asks"))
                .map(JsonElement::getAsJsonArray)
                .orElse(null);
        JsonArray bidsUpdateList = Optional.ofNullable(element.getAsJsonObject()
                .get("bids"))
                .map(JsonElement::getAsJsonArray)
                .orElse(null);

        updateData(asksUpdateList, bidsUpdateList);
    }

    @Override
    public OrderBook getOrderBook() {
        return this.orderBook.get();
    }

    private void updateData(JsonArray asksUpdateList, JsonArray bidsUpdateList) {
        OrderBook orderBook = this.orderBook.get();
        if (Objects.nonNull(asksUpdateList)) {
            for (JsonElement jsonElement : asksUpdateList) {
                JsonArray innerArray = jsonElement.getAsJsonArray();
                double price = innerArray.get(0).getAsDouble();
                double amount = innerArray.get(1).getAsDouble();
                orderBook.updateAsk(price, amount);
            }
        }
        if (Objects.nonNull(bidsUpdateList)) {
            for (JsonElement jsonElement : bidsUpdateList) {
                JsonArray innerArray = jsonElement.getAsJsonArray();
                double price = innerArray.get(0).getAsDouble();
                double amount = innerArray.get(1).getAsDouble();
                orderBook.updateBid(price, amount);
            }
        }
    }
}
