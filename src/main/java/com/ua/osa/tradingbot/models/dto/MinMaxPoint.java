package com.ua.osa.tradingbot.models.dto;

import lombok.Data;

@Data
public class MinMaxPoint {
    private final int indexMin;
    private final int indexMax;
    private final double minValue;
    private final double maxValue;
    private final double difference;

    public MinMaxPoint(int indexMin,int indexMax, double minValue, double maxValue) {
        this.indexMin = indexMin;
        this.indexMax = indexMax;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.difference = maxValue - minValue;
    }
}