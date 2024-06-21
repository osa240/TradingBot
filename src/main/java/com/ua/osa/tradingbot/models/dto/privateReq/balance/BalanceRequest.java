package com.ua.osa.tradingbot.models.dto.privateReq.balance;

import com.ua.osa.tradingbot.models.dto.AbstractRequest;
import com.ua.osa.tradingbot.models.dto.enums.TickerEnum;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BalanceRequest extends AbstractRequest {
    private final boolean nonceWindow = true;
    private TickerEnum ticker;

    public BalanceRequest(String request) {
        super();
        this.request = request;
    }

    public BalanceRequest(String request, TickerEnum ticker) {
        super();
        this.request = request;
        this.ticker = ticker;
    }
}
