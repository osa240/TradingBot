package com.ua.osa.tradingbot.restClients;


import com.ua.osa.tradingbot.models.dto.all.AllPairsResponse;
import com.ua.osa.tradingbot.models.dto.pair.TickerPairResponse;
import feign.Param;
import feign.RequestLine;

public interface WhiteBitClient {

    @RequestLine("GET /api/v1/public/tickers")
    AllPairsResponse getAllInfo();

    @RequestLine("GET /api/v1/public/ticker?market={pair}")
    TickerPairResponse getPairInfo(@Param("pair") String pair);
}
