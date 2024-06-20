package com.ua.osa.tradingbot.restClients;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Formatter;
import com.google.gson.Gson;
import com.ua.osa.tradingbot.models.dto.balance.BalanceRequest;
import feign.RequestInterceptor;
import feign.RequestTemplate;

public class AuthInterceptor implements RequestInterceptor {
    private static final String BALANCE_METHOD = "/api/v4/trade-account/balance";
    private static final String API_KEY = "0027baf96b5b50a9ed888c2b33b5e8b2";
    private static final String API_SECRET = "f19d67c2a4c157613bc067897a02e983";

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String path = requestTemplate.path();
        if (path.contains(BALANCE_METHOD)) {
            byte[] body = requestTemplate.body();
            Gson gson = new Gson();
            String bodyString = gson.toJson(gson.fromJson(new String(body), BalanceRequest.class));
            String payload = getPayload(bodyString.getBytes());
            requestTemplate
                    .header("Content-type", "application/json")
                    .header("X-TXC-APIKEY", API_KEY)
                    .header("X-TXC-PAYLOAD", payload)
                    .header("X-TXC-SIGNATURE", calcSignature(payload));
        }
    }

    private String getPayload(byte[] bodyTemplate) {
        return Base64.getEncoder().encodeToString(bodyTemplate);
    }

    private static String calcSignature(String data) {
        final String HMAC_SHA512 = "HmacSHA512";
        SecretKeySpec secretKeySpec = new SecretKeySpec(API_SECRET.getBytes(), HMAC_SHA512);
        Mac mac = null;
        try {
            mac = Mac.getInstance(HMAC_SHA512);
            mac.init(secretKeySpec);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        byte[] bytes = mac.doFinal(data.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
}
