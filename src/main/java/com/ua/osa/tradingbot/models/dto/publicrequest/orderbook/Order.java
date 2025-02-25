package com.ua.osa.tradingbot.models.dto.publicrequest.orderbook;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Order {
    private String price;
    private String amount;
}
