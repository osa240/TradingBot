package com.ua.osa.tradingbot.models.dto.publicReq.orderbook;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderBookDto {
    @JsonProperty(value = "ticker_id")
    private String tickerId;
    private long timestamp;
    private List<List<String>> asks; // Заявки на продажу
    private List<List<String>> bids; // Заявки на покупку

    public List<Order> getAsks() {
        final List<Order> results = new ArrayList<>();
        for (List<String> ask : asks) {
            results.add(new Order(ask.get(0), ask.get(1)));
        }
        return results;
    }

    public List<Order> getBids() {
        final List<Order> results = new ArrayList<>();
        for (List<String> bid : bids) {
            results.add(new Order(bid.get(0), bid.get(1)));
        }
        return results;
    }
}
