package com.ua.osa.tradingbot.services.strategies;

import com.ua.osa.tradingbot.models.dto.OrderBook;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.TradingRecord;

public class OrderBookStrategy extends BaseStrategy {

    private OrderBook orderBook;

    public OrderBookStrategy(OrderBook orderBook) {
        super(createEntryRule(orderBook), createExitRule(orderBook));
        this.orderBook = orderBook;
    }

    private static Rule createEntryRule(OrderBook orderBook) {
        return new Rule() {
            @Override
            public boolean isSatisfied(int index, TradingRecord tradingRecord) {
                double totalBidVolume = orderBook.getTotalBidVolume();
                double totalAskVolume = orderBook.getTotalAskVolume();
                System.out.println("Result:");
                System.out.println("totalBidVolume: " + totalBidVolume);
                System.out.println("totalAskVolume: " + totalAskVolume);
                System.out.println("Disbalance: " + (totalBidVolume - totalAskVolume * 1.5));
                // Якщо об'єм заявок на купівлю в 1.5 рази більше аніж на продаж
                return totalBidVolume > totalAskVolume * 1.5;
            }
        };
    }

    private static Rule createExitRule(OrderBook orderBook) {
        return new Rule() {
            @Override
            public boolean isSatisfied(int index, TradingRecord tradingRecord) {
                double totalBidVolume = orderBook.getTotalBidVolume();
                double totalAskVolume = orderBook.getTotalAskVolume();
                System.out.println("Result of Sell:");
                System.out.println("totalBidVolume: " + totalBidVolume);
                System.out.println("totalAskVolume: " + totalAskVolume);
                System.out.println("Disbalance: " + (totalAskVolume - totalBidVolume * 1.5));
                // Якщо об'єм заявок на продаж в 1.5 рази більше аніж на купівлю
                return totalAskVolume > totalBidVolume * 1.5;
            }
        };
    }
}
