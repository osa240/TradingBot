package com.ua.osa.tradingbot.services;

import com.ua.osa.tradingbot.models.dto.enums.MethodEnum;
import com.ua.osa.tradingbot.models.dto.enums.TickerEnum;
import com.ua.osa.tradingbot.models.dto.privateReq.balance.BalanceRequest;
import com.ua.osa.tradingbot.models.dto.enums.TradePair;
import com.ua.osa.tradingbot.models.dto.privateReq.balance.BalanceResponse;
import com.ua.osa.tradingbot.models.dto.privateReq.limitOrder.LimitOrderRequest;
import com.ua.osa.tradingbot.restClients.WhiteBitClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetPriceCenterMainServiceImpl implements GetPriceCenterMainService {

    private final WhiteBitClient client;

    @Override
    public void showAllInfo() {
        System.out.println(client.getAllInfo());
    }

    @Override
    public void showPairInfo() {
        System.out.println(client.getPairInfo(TradePair.BTC_USDT.name()));
    }

    @Override
    public void showAllBalances() {
        System.out.println(client.getAllBalances(new BalanceRequest(MethodEnum.BALANCE.getMethod())));
    }

    @Override
    public BalanceResponse showBalance(TickerEnum ticker) {
        return client.getBalance(new BalanceRequest(MethodEnum.BALANCE.getMethod(), ticker));
    }

    @Override
    public void createOrder(LimitOrderRequest order) {
        System.out.println(client.newLimitOrder(order));
    }
}
