package com.ua.osa.tradingbot;

import com.ua.osa.tradingbot.models.dto.enums.OrderBookStatusEnum;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AppProperties {
    public static final AtomicReference<OrderBookStatusEnum> ORDERBOOK_SIGNAL =
            new AtomicReference<>(OrderBookStatusEnum.hold);
    public static final AtomicBoolean IS_BUY_ALREADY = new AtomicBoolean(false);
    public static final AtomicReference<BigDecimal> LAST_BUY_PRICE =
            new AtomicReference<>(BigDecimal.ZERO);
    public static final AtomicReference<BigDecimal> CLOSE_BUY_AMOUNT =
            new AtomicReference<>(BigDecimal.ZERO);

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
