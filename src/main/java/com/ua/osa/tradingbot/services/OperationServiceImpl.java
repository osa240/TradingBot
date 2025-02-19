package com.ua.osa.tradingbot.services;

import com.ua.osa.tradingbot.AppProperties;
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
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
@RequiredArgsConstructor
public class OperationServiceImpl implements OperationService {
    private final WhiteBitClient whiteBitClient;
    private final TelegramBotService telegramBotService;
    private final TaskManager taskManager;

    private final AtomicReference<BigDecimal> buyPrice = new AtomicReference<>(BigDecimal.ZERO);
    private final AtomicReference<BigDecimal> buyAmount = new AtomicReference<>(BigDecimal.valueOf(0.0002));
    private final AtomicReference<BigDecimal> closeAmountToBuy = new AtomicReference<>(BigDecimal.ZERO);

    @Override
    public boolean buy(BigDecimal price, BigDecimal amount) {
        if (amount == null) {
            amount = buyAmount.get();
        }
        buyPrice.set(price);
        buyAmount.set(amount);
        BigDecimal closeAmountToBuy = price.multiply(amount);
        this.closeAmountToBuy.set(closeAmountToBuy);

        LimitOrderRequest request = new LimitOrderRequest();
        request.setRequest(MethodEnum.NEW_LIMIT_ORDER.getMethod());
        request.setSide(SideEnum.buy);
        request.setMarket(AppProperties.TRADE_PAIR.get());

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
                                    Цена: %s
                                    Объем: %s
                                    -------------------------
                                    Сумма: %s
                                    \uD83D\uDFE2 \uD83D\uDFE2 \uD83D\uDFE2""",
                            price, this.buyAmount.get(), closeAmountToBuy));
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
                amount = buyAmount.get();
            }

            LimitOrderRequest request = new LimitOrderRequest();
            request.setRequest(MethodEnum.NEW_LIMIT_ORDER.getMethod());
            request.setSide(SideEnum.sell);
            request.setMarket(AppProperties.TRADE_PAIR.get());

            request.setAmount(amount);
            request.setPrice(price);

            whiteBitClient.newLimitOrder(request);
            log.info("Strong sell signal generated.");

            taskManager.execute(() -> {
                try {
                    // statistika
                    BigDecimal buingPrice = this.buyPrice.get();
                    BigDecimal buingAmount = this.buyAmount.get();
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
                                    Цена: %s
                                    Объем: %s
                                    --------------------------
                                    Сумма: %s
                                    ::::::::::::::::::::::::::
                                    Результат: %s
                                    ::::::::::::::::::::::::::
                                    \uD83D\uDD34 \uD83D\uDD34 \uD83D\uDD34""",
                            lastPrice, buingAmount, sellSum, sellSum.subtract(this.closeAmountToBuy.get())));
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
            amount = buyAmount.get();
        }

        MarketOrderRequest request = new MarketOrderRequest();

        request.setRequest(MethodEnum.NEW_MARKET_ORDER.getMethod());
        request.setSide(SideEnum.buy);
        request.setMarket(AppProperties.TRADE_PAIR.get());
        request.setAmount(new BigDecimal("10"));

        try {
            OrderResponse orderResponse = whiteBitClient.newMarketOrder(request);
            if (Objects.nonNull(orderResponse)) {
                BigDecimal amountForSell = orderResponse.getAmount();
                closeAmountToBuy.set(amountForSell);
            }
            log.info("Strong buy signal generated.");

            taskManager.execute(() -> {
                try {
                    telegramBotService.sendMessageToUser(String.format("""
                                    \uD83D\uDFE2 \uD83D\uDFE2 \uD83D\uDFE2
                                    Купівля
                                    -------------------------
                                    Объем: %s
                                    Sum: 10
                                    \uD83D\uDFE2 \uD83D\uDFE2 \uD83D\uDFE2""",
                            closeAmountToBuy.get()));
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
            amount = closeAmountToBuy.get();
        }

        MarketOrderRequest request = new MarketOrderRequest();

        request.setRequest(MethodEnum.NEW_MARKET_ORDER.getMethod());
        request.setSide(SideEnum.sell);
        request.setMarket(AppProperties.TRADE_PAIR.get());
        request.setAmount(amount);

        try {
            OrderResponse orderResponse = whiteBitClient.newMarketOrder(request);
            if (Objects.nonNull(orderResponse)) {
                final BigDecimal amountForSell = orderResponse.getAmount();
                closeAmountToBuy.set(amountForSell);

                log.info("Strong buy signal generated.");

                taskManager.execute(() -> {
                    try {
                        telegramBotService.sendMessageToUser(String.format("""
                                    \uD83D\uDD34 \uD83D\uDD34 \uD83D\uDD34
                                    Продаємо
                                    --------------------------
                                    Объем: %s
                                    Сумма: %s
                                    ::::::::::::::::::::::::::
                                    Результат: %s
                                    ::::::::::::::::::::::::::
                                    \uD83D\uDD34 \uD83D\uDD34 \uD83D\uDD34""",
                                closeAmountToBuy.get(), amountForSell, amountForSell.subtract(new BigDecimal("10"))));
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
