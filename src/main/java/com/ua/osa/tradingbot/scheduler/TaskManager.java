package com.ua.osa.tradingbot.scheduler;

import com.ua.osa.tradingbot.websocket.WebSocketService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class TaskManager {
    private final ThreadPoolTaskScheduler taskScheduler;
    private final TaskFactory taskFactory;

    @PostConstruct
    private void autoStart() {
        this.execute(taskFactory.createWebSocketConnectionTask());
    }


    // Запуск задачи немедленно
    public ScheduledFuture<?> execute(Runnable task) {
        return taskScheduler.schedule(task, triggerContext -> {
            task.run();
            return null;
        });
    }

    // Запуск задачи с задержкой
    public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
        return taskScheduler.schedule(task, new Date(System.currentTimeMillis() + unit.toMillis(delay)));
    }

    // Повторяющаяся задача с фиксированной задержкой
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        return taskScheduler.scheduleAtFixedRate(task, new Date(System.currentTimeMillis() + unit.toMillis(initialDelay)), unit.toMillis(period));
    }

    // Повторяющаяся задача с фиксированным интервалом
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, TimeUnit unit) {
        return taskScheduler.scheduleWithFixedDelay(task, new Date(System.currentTimeMillis() + unit.toMillis(initialDelay)), unit.toMillis(delay));
    }

    // Отмена задачи
    public void cancel(ScheduledFuture<?> futureTask) {
        futureTask.cancel(true);
    }

    // Остановка всех задач и завершение планировщика
    public void shutdown() {
        taskScheduler.shutdown();
    }
}
