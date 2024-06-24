package com.ua.osa.tradingbot.services;

import com.ua.osa.tradingbot.models.dto.enums.TickerEnum;
import com.ua.osa.tradingbot.models.dto.privateReq.balance.BalanceResponse;
import com.ua.osa.tradingbot.models.dto.privateReq.limitOrder.LimitOrderRequest;

public interface GetPriceCenterMainService {
    void showAllInfo();
    void showPairInfo();
    void showAllBalances();
    BalanceResponse showBalance(TickerEnum ticker);
    void createOrder(LimitOrderRequest order);
}
