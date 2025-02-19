package com.ua.osa.tradingbot.models.dto.enums;

import java.util.Arrays;
import com.ua.osa.tradingbot.models.dto.AbstractRequest;
import com.ua.osa.tradingbot.models.dto.privateReq.balance.BalanceRequest;
import com.ua.osa.tradingbot.models.dto.privateReq.limitOrder.LimitOrderRequest;
import lombok.Getter;

@Getter
public enum MethodEnum {
    BALANCE("/api/v4/trade-account/balance"),
    NEW_LIMIT_ORDER("/api/v4/order/new"),
    NEW_MARKET_ORDER("/api/v4/order/market");

    String method;

    MethodEnum(String method) {
        this.method = method;
    }

    public static boolean isPrivateMethod(String path) {
        return Arrays.stream(values())
                .filter(v -> v.method.equals(path))
                .findFirst()
                .orElse(null) != null;
    }

    public static <T> Class<? extends AbstractRequest> getSerializeClass(String path) {
        return switch (getEnumValueByPath(path)) {
            case BALANCE -> BalanceRequest.class;
            case NEW_LIMIT_ORDER -> LimitOrderRequest.class;
            case NEW_MARKET_ORDER -> LimitOrderRequest.class;
        };
    }

    private static MethodEnum getEnumValueByPath(String path) {
        return Arrays.stream(values())
                .filter(m -> path.contains(m.method))
                .findFirst()
                .orElse(null);
    }
}
