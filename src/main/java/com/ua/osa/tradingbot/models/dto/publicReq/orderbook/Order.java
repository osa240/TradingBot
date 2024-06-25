package com.ua.osa.tradingbot.models.dto.publicReq.orderbook;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Order {
    private String price;
    private String amount;
}