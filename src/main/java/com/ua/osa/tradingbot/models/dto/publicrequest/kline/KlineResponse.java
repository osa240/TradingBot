package com.ua.osa.tradingbot.models.dto.publicrequest.kline;

import java.util.List;
import lombok.Data;

@Data
public class KlineResponse {
    private Boolean success;
    private String message;
    private List<List<Object>> result;
}
