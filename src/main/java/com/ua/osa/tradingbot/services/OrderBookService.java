package com.ua.osa.tradingbot.services;

import com.ua.osa.tradingbot.models.dto.OrderBook;

public interface OrderBookService {
    void updateOrderBook(String message);
    OrderBook getOrderBook();
}
