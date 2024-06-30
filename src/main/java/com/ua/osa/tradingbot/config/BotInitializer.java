package com.ua.osa.tradingbot.config;

import com.ua.osa.tradingbot.services.TelegramBotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

/**
 * Created by S.Orlov on 01.05.23
 */
@Component
public class BotInitializer {

    @Autowired
    private TelegramBotService telegramBotService;

    @EventListener({ContextRefreshedEvent.class})
    public void init() {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(telegramBotService);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
