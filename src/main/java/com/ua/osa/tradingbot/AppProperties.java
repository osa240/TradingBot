package com.ua.osa.tradingbot;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import com.ua.osa.tradingbot.models.dto.enums.OrderBookStatusEnum;
import com.ua.osa.tradingbot.models.dto.enums.TradePair;
import com.ua.osa.tradingbot.models.dto.enums.WebSocketMethodEnum;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AppProperties {
    public static final AtomicReference<TradePair> TRADE_PAIR = new AtomicReference<>(TradePair.BTC_USDT);
    public static final AtomicReference<OrderBookStatusEnum> ORDERBOOK_SIGNAL = new AtomicReference<>(OrderBookStatusEnum.hold);
    public static final AtomicReference<Set<WebSocketMethodEnum>> SUBSCRIBES = new AtomicReference<>(new HashSet<>());

    private static final String TEST_URL = "http://www.google.com";

    public static boolean isInternetAvailable() {
        try {
            URL url = new URL(TEST_URL);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("HEAD");
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(5000);
            int responseCode = urlConnection.getResponseCode();
            return (200 <= responseCode && responseCode <= 399);
        } catch (IOException e) {
            return false;
        }
    }
}
