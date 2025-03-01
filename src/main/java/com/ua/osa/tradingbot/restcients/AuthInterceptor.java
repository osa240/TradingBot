package com.ua.osa.tradingbot.restcients;

import com.google.gson.Gson;
import com.ua.osa.tradingbot.models.dto.enums.MethodEnum;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import java.util.Base64;
import java.util.Formatter;
import java.util.Objects;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;

public class AuthInterceptor implements RequestInterceptor {
    @Value("${whitebit.api.key}")
    private String apiKey;
    @Value("${whitebit.api.secret}")
    private String apiSecret;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String path = requestTemplate.path();
        if (MethodEnum.isPrivateMethod(path)) {
            byte[] body = requestTemplate.body();
            String bodyString = getBodyString(body, path);
            String payload = getPayload(bodyString.getBytes());
            requestTemplate
                    .header("Content-type", "application/json")
                    .header("X-TXC-APIKEY", apiKey)
                    .header("X-TXC-PAYLOAD", payload)
                    .header("X-TXC-SIGNATURE", calcSignature(payload));
        }
    }

    private String getBodyString(byte[] body, String path) {
        Gson gson = new Gson();
        return gson.toJson(gson.fromJson(new String(body),
                Objects.requireNonNull(MethodEnum.getSerializeClass(path))
        ));
    }

    private String getPayload(byte[] bodyTemplate) {
        return Base64.getEncoder().encodeToString(bodyTemplate);
    }

    private String calcSignature(String data) {
        final String Hmac_Sha512 = "HmacSHA512";
        SecretKeySpec secretKeySpec = new SecretKeySpec(apiSecret.getBytes(), Hmac_Sha512);
        Mac mac = null;
        try {
            mac = Mac.getInstance(Hmac_Sha512);
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
