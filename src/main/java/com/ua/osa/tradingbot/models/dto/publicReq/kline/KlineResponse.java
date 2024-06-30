package com.ua.osa.tradingbot.models.dto.publicReq.kline;

import lombok.Data;
import java.util.List;

@Data
public class KlineResponse {
    private Boolean success;
    private String message;
    private List<List<Object>> result;
}
