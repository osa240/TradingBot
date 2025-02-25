package com.ua.osa.tradingbot.restcients;

import com.ua.osa.tradingbot.models.dto.privaterequest.balance.BalanceRequest;
import com.ua.osa.tradingbot.models.dto.privaterequest.balance.BalanceResponse;
import com.ua.osa.tradingbot.models.dto.privaterequest.limitorder.LimitOrderRequest;
import com.ua.osa.tradingbot.models.dto.privaterequest.limitorder.OrderResponse;
import com.ua.osa.tradingbot.models.dto.privaterequest.marketorder.MarketOrderRequest;
import com.ua.osa.tradingbot.models.dto.publicrequest.all.AllPairsResponse;
import com.ua.osa.tradingbot.models.dto.publicrequest.kline.KlineResponse;
import com.ua.osa.tradingbot.models.dto.publicrequest.orderbook.OrderBookDto;
import com.ua.osa.tradingbot.models.dto.publicrequest.pair.TickerPairResponse;
import feign.Param;
import feign.RequestLine;
import java.util.Map;

public interface WhiteBitClient {

    @RequestLine("GET /api/v1/public/tickers")
    AllPairsResponse getAllInfo();

    @RequestLine("GET /api/v1/public/ticker?market={pair}")
    TickerPairResponse getPairInfo(@Param("pair") String pair);

    @RequestLine("GET /api/v1/public/kline?market={pair}&interval={interval}&limit={limit}")
    KlineResponse getKlains(@Param("pair") String pair,
                            @Param("interval") String interval,
                            @Param("limit") String limit);

    @RequestLine("GET /api/v1/public/kline?market={pair}&interval={interval}"
            + "&limit={limit}&start={start}&end={end}")
    KlineResponse getKlains(@Param("pair") String pair,
                            @Param("interval") String interval,
                            @Param("limit") String limit,
                            @Param("start") long start,
                            @Param("end") long end);

    @RequestLine("GET /api/v4/public/orderbook/{pair}")
    OrderBookDto getOrderBook(@Param("pair") String pair);

    @RequestLine("POST /api/v4/trade-account/balance")
    Map<String, BalanceResponse> getAllBalances(BalanceRequest request);

    @RequestLine("POST /api/v4/trade-account/balance")
    BalanceResponse getBalance(BalanceRequest request);

    @RequestLine("POST /api/v4/order/new")
    OrderResponse newLimitOrder(LimitOrderRequest request);

    @RequestLine("POST /api/v4/order/new")
    OrderResponse newMarketOrder(MarketOrderRequest request);
}
