package com.ua.osa.tradingbot.services;

import com.ua.osa.tradingbot.models.dto.balance.BalanceRequest;
import com.ua.osa.tradingbot.models.dto.enums.TradePair;
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
    public void showBalance() {
        System.out.println(client.getBalance(new BalanceRequest("/api/v4/trade-account/balance")));
    }
}
