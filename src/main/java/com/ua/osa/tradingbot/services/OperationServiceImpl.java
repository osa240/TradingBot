package com.ua.osa.tradingbot.services;

import static com.ua.osa.tradingbot.AppProperties.CLOSE_BUY_AMOUNT;
import static com.ua.osa.tradingbot.AppProperties.LAST_BUY_PRICE;
import static com.ua.osa.tradingbot.BotSettings.BUY_AMOUNT;

import com.ua.osa.tradingbot.BotSettings;
import com.ua.osa.tradingbot.models.dto.enums.MethodEnum;
import com.ua.osa.tradingbot.models.dto.enums.SideEnum;
import com.ua.osa.tradingbot.models.dto.privateReq.limitOrder.LimitOrderRequest;
import com.ua.osa.tradingbot.models.dto.privateReq.limitOrder.OrderResponse;
import com.ua.osa.tradingbot.models.dto.privateReq.marketOrder.MarketOrderRequest;
import com.ua.osa.tradingbot.restClients.WhiteBitClient;
import com.ua.osa.tradingbot.scheduler.TaskManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class OperationServiceImpl implements OperationService {
    private final WhiteBitClient whiteBitClient;
    private final TelegramBotService telegramBotService;
    private final TaskManager taskManager;

    @Override
    public boolean buy(BigDecimal price, BigDecimal amount) {
        if (amount == null) {
            amount = BUY_AMOUNT.get();
        }
        LAST_BUY_PRICE.set(price);
        BUY_AMOUNT.set(amount);
        BigDecimal closeAmountToBuy = price.multiply(amount);
        CLOSE_BUY_AMOUNT.set(closeAmountToBuy);

        LimitOrderRequest request = new LimitOrderRequest();
        request.setRequest(MethodEnum.NEW_LIMIT_ORDER.getMethod());
        request.setSide(SideEnum.buy);
        request.setMarket(BotSettings.TRADE_PAIR.get());

        request.setPrice(price);
        request.setAmount(amount);

        try {
            whiteBitClient.newLimitOrder(request);
            log.info("Strong buy signal generated.");

            taskManager.execute(() -> {
                try {
                    telegramBotService.sendMessageToUser(String.format("""
                                    \uD83D\uDFE2 \uD83D\uDFE2 \uD83D\uDFE2
                                    КІПІВЛЯ*
                                    *прохання перевірити
                                    -------------------------
                                    Ціна: %s
                                    Об'єм: %s
                                    -------------------------
                                    Сума: %s
                                    \uD83D\uDFE2 \uD83D\uDFE2 \uD83D\uDFE2""",
                            price, BUY_AMOUNT.get(), closeAmountToBuy));
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            });
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            taskManager.execute(() -> {
                try {
                    telegramBotService.sendMessageToUser(String.format("""
                            ⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️
                            ⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️
                                                
                            ------------------------
                            Купляємо вручну ! ! !
                            ------------------------
                            %s
                            ------------------------
                            ⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️
                            ⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️
                            """, e.getMessage()));
                } catch (Exception e1) {
                    log.error(e1.getMessage(), e1);
                }
            });
        }
        return false;
    }

    @Override
    public boolean sell(BigDecimal price, BigDecimal amount) {
        try {
            if (amount == null) {
                amount = BUY_AMOUNT.get();
            }

            LimitOrderRequest request = new LimitOrderRequest();
            request.setRequest(MethodEnum.NEW_LIMIT_ORDER.getMethod());
            request.setSide(SideEnum.sell);
            request.setMarket(BotSettings.TRADE_PAIR.get());

            request.setAmount(amount);
            request.setPrice(price);

            whiteBitClient.newLimitOrder(request);
            log.info("Strong sell signal generated.");

            taskManager.execute(() -> {
                try {
                    // statistika
                    BigDecimal buingPrice = LAST_BUY_PRICE.get();
                    BigDecimal buingAmount = BUY_AMOUNT.get();
                    BigDecimal lastPrice = request.getPrice();

                    log.info("---------------------------------------------------------------------------------------");
                    log.info("Statistic: ");
                    log.info("Buy on {}, sell on {}", buingPrice, lastPrice);
                    BigDecimal different = lastPrice.subtract(buingPrice);
                    log.info("different: {}", different);
                    log.info("diffrrent in money: {}", different.multiply(buingAmount));
                    log.info("---------------------------------------------------------------------------------------");

                    BigDecimal sellSum = lastPrice.multiply(buingAmount);
                    telegramBotService.sendMessageToUser(String.format("""
                                    \uD83D\uDD34 \uD83D\uDD34 \uD83D\uDD34
                                    П Р О Д А Ж*
                                    *прохання перевірити
                                    --------------------------
                                    Ціна: %s
                                    Об'єм: %s
                                    --------------------------
                                    Сума: %s
                                    ::::::::::::::::::::::::::
                                    Результат: %s
                                    ::::::::::::::::::::::::::
                                    \uD83D\uDD34 \uD83D\uDD34 \uD83D\uDD34""",
                            lastPrice, buingAmount, sellSum, sellSum.subtract(CLOSE_BUY_AMOUNT.get())));
                } catch (Exception e1) {
                    log.error(e1.getMessage(), e1);
                }
            });
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            taskManager.execute(() -> {
                try {
                    telegramBotService.sendMessageToUser(String.format("""
                            ⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️
                            ⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️
                                                
                            ------------------------
                            Купляємо вручну ! ! !
                            ------------------------
                            %s
                            ------------------------
                            ⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️
                            ⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️
                            """, e.getMessage()));
                } catch (Exception e1) {
                    log.error(e1.getMessage(), e1);
                }
            });
        }
        return false;
    }

    @Override
    public boolean buyMarket(BigDecimal amount) {
        if (amount == null) {
            amount = BUY_AMOUNT.get();
        }

        MarketOrderRequest request = new MarketOrderRequest();

        request.setRequest(MethodEnum.NEW_MARKET_ORDER.getMethod());
        request.setSide(SideEnum.buy);
        request.setMarket(BotSettings.TRADE_PAIR.get());
        request.setAmount(new BigDecimal("10"));

        try {
            OrderResponse orderResponse = whiteBitClient.newMarketOrder(request);
            if (Objects.nonNull(orderResponse)) {
                BigDecimal amountForSell = orderResponse.getAmount();
                CLOSE_BUY_AMOUNT.set(amountForSell);
            }
            log.info("Strong buy signal generated.");

            taskManager.execute(() -> {
                try {
                    telegramBotService.sendMessageToUser(String.format("""
                                    \uD83D\uDFE2 \uD83D\uDFE2 \uD83D\uDFE2
                                    Купівля
                                    -------------------------
                                    Об'єм: %s
                                    Сума: 10
                                    \uD83D\uDFE2 \uD83D\uDFE2 \uD83D\uDFE2""",
                            CLOSE_BUY_AMOUNT.get()));
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            });
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            taskManager.execute(() -> {
                try {
                    telegramBotService.sendMessageToUser(String.format("""
                            ⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️
                            ⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️
                                                
                            ------------------------
                            Купляємо вручну ! ! !
                            ------------------------
                            %s
                            ------------------------
                            ⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️
                            ⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️
                            """, e.getMessage()));
                } catch (Exception e1) {
                    log.error(e1.getMessage(), e1);
                }
            });
        }
        return false;
    }

    @Override
    public boolean sellMarket(BigDecimal amount) {
        if (amount == null) {
            amount = CLOSE_BUY_AMOUNT.get();
        }

        MarketOrderRequest request = new MarketOrderRequest();

        request.setRequest(MethodEnum.NEW_MARKET_ORDER.getMethod());
        request.setSide(SideEnum.sell);
        request.setMarket(BotSettings.TRADE_PAIR.get());
        request.setAmount(amount);

        try {
            OrderResponse orderResponse = whiteBitClient.newMarketOrder(request);
            if (Objects.nonNull(orderResponse)) {
                final BigDecimal amountForSell = orderResponse.getAmount();
                CLOSE_BUY_AMOUNT.set(amountForSell);

                log.info("Strong buy signal generated.");

                taskManager.execute(() -> {
                    try {
                        telegramBotService.sendMessageToUser(String.format("""
                                    \uD83D\uDD34 \uD83D\uDD34 \uD83D\uDD34
                                    Продаємо
                                    --------------------------
                                    Об'єм: %s
                                    Сума: %s
                                    ::::::::::::::::::::::::::
                                    Результат: %s
                                    ::::::::::::::::::::::::::
                                    \uD83D\uDD34 \uD83D\uDD34 \uD83D\uDD34""",
                                CLOSE_BUY_AMOUNT.get(), amountForSell, amountForSell.subtract(new BigDecimal("10"))));
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                });
            }

            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            taskManager.execute(() -> {
                try {
                    telegramBotService.sendMessageToUser(String.format("""
                            ⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️
                            ⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️
                                                
                            ------------------------
                            Купляємо вручну ! ! !
                            ------------------------
                            %s
                            ------------------------
                            ⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️
                            ⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️⛔️
                            """, e.getMessage()));
                } catch (Exception e1) {
                    log.error(e1.getMessage(), e1);
                }
            });
        }
        return false;
    }
}
