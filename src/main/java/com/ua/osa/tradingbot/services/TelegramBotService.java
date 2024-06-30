package com.ua.osa.tradingbot.services;

import com.ua.osa.tradingbot.config.TelegramConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.LinkedList;
import java.util.List;

@Service
@Slf4j
public class TelegramBotService extends TelegramLongPollingBot {
    public static final String RESTART_COMMAND = "/restart";
    public static final String RESTART_DESCRIPTION = "Перезапуск бота(хз зачем, в каждом боте есть)";

    private final TelegramConfig config;
    private final Long userId;

    public TelegramBotService(TelegramConfig config) {
        this.config = config;
        this.userId = config.getUserId();

        List<BotCommand> listOfCommands = new LinkedList<>();
        listOfCommands.add(new BotCommand(RESTART_COMMAND, RESTART_DESCRIPTION));
        try {
            execute(new SetMyCommands(listOfCommands));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.info(update.toString());
    }

    public void sendMessageToUser(String messageStr) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(this.userId));
        message.setText(messageStr);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }
}
