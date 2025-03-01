package com.ua.osa.tradingbot.services;

import static com.ua.osa.tradingbot.AppProperties.IS_BUY_ALREADY;

import com.ua.osa.tradingbot.config.TelegramConfig;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@Slf4j
public class TelegramBotService extends TelegramLongPollingBot {
    private TelegramConfig config;

    public TelegramBotService(TelegramConfig config) {
        this.config = config;
        List<BotCommand> listOfCommands = new LinkedList<>();
        listOfCommands.add(new BotCommand("/start_bot", "starting trading bot"));
        listOfCommands.add(new BotCommand("/stop_bot", "stopping trading bot"));
        listOfCommands.add(new BotCommand("/buy_flag", "change status to buy (manual)"));
        listOfCommands.add(new BotCommand("/sell_flag", "change status to sell (manual)"));
        listOfCommands.add(new BotCommand("/report", "download a report"));
        listOfCommands.add(new BotCommand("/report_orderbook", "download a report of order book"));
        try {
            execute(new SetMyCommands(listOfCommands));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @PostConstruct
    public void start() {
        new Thread(() -> {
            try {
                Thread.sleep(15000);
                sendMessageToUser("Trading bot is in ACTIVE state");
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }
        }).start();
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
        try {
            log.info(update.toString());
            String message = update.getMessage().getText();
            if ("/buy_flag".equals(message)) {
                IS_BUY_ALREADY.set(true);
                log.info("Current BUY status: {}", IS_BUY_ALREADY.get());
            } else if ("/sell_flag".equals(message)) {
                IS_BUY_ALREADY.set(false);
                log.info("Current BUY status: {}", IS_BUY_ALREADY.get());
            } else if ("/start_bot".equals(message)) {
                NavigateService.runTradingBotSubject.onNext(true);
            } else if ("/stop_bot".equals(message)) {
                NavigateService.runTradingBotSubject.onNext(false);
            } else if ("/report".equals(message)) {
                NavigateService.reportSubject.onNext(true);
            } else if ("/report_orderbook".equals(message)) {
                NavigateService.reportOrderBookSubject.onNext(true);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void sendMessageToUser(String messageStr) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(this.config.getUserId()));
        message.setText(messageStr);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }

    public void sendFileToUser(File report, String messageStr) {
        SendDocument sendDocumentRequest = new SendDocument();
        sendDocumentRequest.setChatId(String.valueOf(this.config.getUserId()));
        sendDocumentRequest.setDocument(new InputFile(report));
        sendDocumentRequest.setCaption(messageStr);

        try {
            execute(sendDocumentRequest);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }
}
