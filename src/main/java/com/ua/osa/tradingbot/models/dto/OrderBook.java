package com.ua.osa.tradingbot.models.dto;

import java.util.*;

public class OrderBook {
    private NavigableMap<Double, Double> bids; // Цена -> Объем
    private NavigableMap<Double, Double> asks; // Цена -> Объем

    public OrderBook() {
        bids = new TreeMap<>(Collections.reverseOrder());
        asks = new TreeMap<>();
    }

    public void updateBid(double price, double volume) {
        if (volume == 0) {
            bids.remove(price);
        } else {
            bids.put(price, volume);
        }
    }

    public void updateAsk(double price, double volume) {
        if (volume == 0) {
            asks.remove(price);
        } else {
            asks.put(price, volume);
        }
    }

    public NavigableMap<Double, Double> getBids() {
        return bids;
    }

    public NavigableMap<Double, Double> getAsks() {
        return asks;
    }

    public double getBestBid() {
        return bids.isEmpty() ? 0 : bids.firstKey();
    }

    public double getBestAsk() {
        return asks.isEmpty() ? 0 : asks.firstKey();
    }

    public double getTotalBidVolume() {
        return bids.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    public double getTotalAskVolume() {
        return asks.values().stream().mapToDouble(Double::doubleValue).sum();
    }
}
