package com.ua.osa.tradingbot.scheduler.tasks;

import java.math.BigDecimal;
import java.util.List;
import com.ua.osa.tradingbot.AppProperties;
import com.ua.osa.tradingbot.models.dto.enums.OrderBookStatusEnum;
import com.ua.osa.tradingbot.models.dto.publicReq.orderbook.Order;
import com.ua.osa.tradingbot.models.dto.publicReq.orderbook.OrderBookDto;
import com.ua.osa.tradingbot.restClients.WhiteBitClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StartWorkTask implements Runnable {
    private static final BigDecimal THRESHOLD = new BigDecimal("0.05"); // Порог для дисбаланса объема

    @Autowired
    private WhiteBitClient whiteBitClient;

    @Override
    public void run() {
        try {
            decideTrade(whiteBitClient.getOrderBook(AppProperties.TRADE_PAIR.get().name()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    public void decideTrade(OrderBookDto orderBook) {
        List<Order> bids = orderBook.getBids();
        List<Order> asks = orderBook.getAsks();

        BigDecimal totalBidVolume = bids.stream().map(Order::getAmount).map(BigDecimal::new).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalAskVolume = asks.stream().map(Order::getAmount).map(BigDecimal::new).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal volumeDifference = totalBidVolume.subtract(totalAskVolume);

        if (volumeDifference.compareTo(THRESHOLD) > 0) {
            AppProperties.ORDERBOOK_SIGNAL.set(OrderBookStatusEnum.buy);
            log.info("Buy: ORDERBOOK"); // Покупка, если объем заявок на покупку значительно превышает объем заявок на продажу
        } else if (volumeDifference.compareTo(THRESHOLD.negate()) < 0) {
            AppProperties.ORDERBOOK_SIGNAL.set(OrderBookStatusEnum.sell);
            log.info("Sell: ORDERBOOK"); // Продажа, если объем заявок на продажу значительно превышает объем заявок на покупку
        } else {
            AppProperties.ORDERBOOK_SIGNAL.set(OrderBookStatusEnum.hold);
            log.info("Hold: ORDERBOOK"); // Удержание, если нет значительного дисбаланса объема
        }
    }
}
