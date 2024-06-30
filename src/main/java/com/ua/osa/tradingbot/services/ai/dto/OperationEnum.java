package com.ua.osa.tradingbot.services.ai.dto;

import org.nd4j.linalg.api.ndarray.INDArray;

public enum OperationEnum {
    WAIT,
    BUY,
    SELL;

    public static OperationEnum getByOrdinar(INDArray predicted) {
        for (int i = 0; i < 3; i++) {
            if (predicted.getInt(i) == 1) {
                int j = 0;
                for (OperationEnum value : values()) {
                    if (i == j) {
                        return value;
                    }
                    j++;
                }
            }
        }
        return null;
    }
}
