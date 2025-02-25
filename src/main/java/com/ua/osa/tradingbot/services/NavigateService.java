package com.ua.osa.tradingbot.services;

import com.ua.osa.tradingbot.scheduler.TaskManager;
import com.ua.osa.tradingbot.websocket.WebSocketService;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NavigateService {
    public static final Subject<Boolean> runTradingBotSubject = PublishSubject.create();
    public static final Subject<Boolean> reportSubject = PublishSubject.create();
    public static final Subject<Boolean> reportOrderBookSubject = PublishSubject.create();

    @Autowired
    private StrategyStatisticService strategyStatisticService;
    @Autowired
    private TaskManager taskManager;
    @Autowired
    private WebSocketService webSocketService;

    @PostConstruct
    public void subsribeModules() {
        NavigateService.runTradingBotSubject.subscribe(
                item -> {
                    if (item) {
                        log.info("Starting trading bot...");
                        runTradingBot();
                    } else {
                        log.info("Stopping trading bot...");
                        stopTradingBot();
                    }

                }, // onNext
                error -> {
                }, // onError
                () -> System.out.println("Выполнено!") // onComplete
        );

        NavigateService.reportSubject.subscribe(next -> {
            strategyStatisticService.generateStatistic();
        });

        NavigateService.reportOrderBookSubject.subscribe(next -> {
            strategyStatisticService.generateOrderBookStatistic();
        });
    }

    public void runTradingBot() {
        taskManager.execute(() -> {
            webSocketService.startWebSocketCommunication();
        });
    }

    public void stopTradingBot() {
        this.webSocketService.closeConnection();
    }
}
