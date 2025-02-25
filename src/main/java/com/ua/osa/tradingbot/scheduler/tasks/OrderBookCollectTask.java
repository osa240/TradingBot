package com.ua.osa.tradingbot.scheduler.tasks;

import com.ua.osa.tradingbot.AppProperties;
import com.ua.osa.tradingbot.BotSettings;
import com.ua.osa.tradingbot.models.dto.enums.OrderBookStatusEnum;
import com.ua.osa.tradingbot.models.dto.publicrequest.orderbook.Order;
import com.ua.osa.tradingbot.models.dto.publicrequest.orderbook.OrderBookDto;
import com.ua.osa.tradingbot.restcients.WhiteBitClient;
import java.math.BigDecimal;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderBookCollectTask implements Runnable {
    // Порог для дізбаланса об'єма
    private static final BigDecimal THRESHOLD = new BigDecimal("0.05");

    @Autowired
    private WhiteBitClient whiteBitClient;

    @Override
    public void run() {
        try {
            decideTrade(whiteBitClient.getOrderBook(BotSettings.TRADE_PAIR.get().name()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    public void decideTrade(OrderBookDto orderBook) {
        List<Order> bids = orderBook.getBids();
        List<Order> asks = orderBook.getAsks();

        BigDecimal totalBidVolume = bids.stream()
                .map(Order::getAmount)
                .map(BigDecimal::new)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalAskVolume = asks.stream()
                .map(Order::getAmount)
                .map(BigDecimal::new)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal volumeDifference = totalBidVolume.subtract(totalAskVolume);

        if (volumeDifference.compareTo(THRESHOLD) > 0) {
            AppProperties.ORDERBOOK_SIGNAL.set(OrderBookStatusEnum.buy);
            log.info("Buy: ORDERBOOK");
        } else if (volumeDifference.compareTo(THRESHOLD.negate()) < 0) {
            AppProperties.ORDERBOOK_SIGNAL.set(OrderBookStatusEnum.sell);
            log.info("Sell: ORDERBOOK");
        } else {
            AppProperties.ORDERBOOK_SIGNAL.set(OrderBookStatusEnum.hold);
            log.info("Hold: ORDERBOOK");
        }
    }
}
