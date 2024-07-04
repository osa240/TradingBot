package com.ua.osa.tradingbot.services;

import com.ua.osa.tradingbot.scheduler.TaskManager;
import com.ua.osa.tradingbot.websocket.WebSocketService;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.concurrent.ScheduledFuture;

@Service
@Slf4j
public class NavigateService {
    public static final Subject<Boolean> runTradingBotSubject = PublishSubject.create();
    public static final Subject<Boolean> reportSubject = PublishSubject.create();

    @Autowired
    private StrategyStatisticService strategyStatisticService;
    @Autowired
    private TaskManager taskManager;
    @Autowired
    private WebSocketService webSocketService;

    private ScheduledFuture<?> currentSubscribe;

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
                error -> {}, // onError
                () -> System.out.println("Выполнено!") // onComplete
        );

        NavigateService.reportSubject.subscribe(next -> {
            strategyStatisticService.generateStatistic();
        });
    }


    public void runTradingBot() {
        this.currentSubscribe = taskManager.execute(() -> {
            webSocketService.startWebSocketCommunication();
        });
    }

    public void stopTradingBot() {
        if (this.currentSubscribe != null) {
            this.webSocketService.closeConnection();
            this.currentSubscribe.cancel(true);
        }
    }
}
