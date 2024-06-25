package com.ua.osa.tradingbot.restClients;

import java.util.Map;
import com.ua.osa.tradingbot.models.dto.privateReq.limitOrder.LimitOrderRequest;
import com.ua.osa.tradingbot.models.dto.privateReq.limitOrder.LimitOrderResponse;
import com.ua.osa.tradingbot.models.dto.publicReq.all.AllPairsResponse;
import com.ua.osa.tradingbot.models.dto.privateReq.balance.BalanceRequest;
import com.ua.osa.tradingbot.models.dto.privateReq.balance.BalanceResponse;
import com.ua.osa.tradingbot.models.dto.publicReq.orderbook.OrderBookDto;
import com.ua.osa.tradingbot.models.dto.publicReq.pair.TickerPairResponse;
import feign.Param;
import feign.RequestLine;

public interface WhiteBitClient {

    @RequestLine("GET /api/v1/public/tickers")
    AllPairsResponse getAllInfo();

    @RequestLine("GET /api/v1/public/ticker?market={pair}")
    TickerPairResponse getPairInfo(@Param("pair") String pair);

    @RequestLine("GET /api/v4/public/orderbook/{pair}")
    OrderBookDto getOrderBook(@Param("pair") String pair);

    @RequestLine("POST /api/v4/trade-account/balance")
    Map<String, BalanceResponse> getAllBalances(BalanceRequest request);

    @RequestLine("POST /api/v4/trade-account/balance")
    BalanceResponse getBalance(BalanceRequest request);

    @RequestLine("POST /api/v4/order/new")
    LimitOrderResponse newLimitOrder(LimitOrderRequest request);
}
