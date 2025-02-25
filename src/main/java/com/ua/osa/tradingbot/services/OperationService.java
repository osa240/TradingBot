package com.ua.osa.tradingbot.services;

import java.math.BigDecimal;

public interface OperationService {
    boolean buy(BigDecimal price, BigDecimal amount);

    boolean buyMarket(BigDecimal amount);

    boolean sell(BigDecimal price, BigDecimal amount);

    boolean sellMarket(BigDecimal amount);
}
