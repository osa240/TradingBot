package com.ua.osa.tradingbot.scheduler;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class TaskManager {
    private final ThreadPoolTaskScheduler taskScheduler;

    // Запуск таска одразу
    public ScheduledFuture<?> execute(Runnable task) {
        return taskScheduler.schedule(task, triggerContext -> {
            task.run();
            return null;
        });
    }

    // Запуск таски с затримкою
    public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
        return taskScheduler.schedule(task,
                new Date(System.currentTimeMillis() + unit.toMillis(delay))
        );
    }

    // Повторювана таска с фіксованой затримкою
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task,
                                                  long initialDelay,
                                                  long period,
                                                  TimeUnit unit) {
        return taskScheduler.scheduleAtFixedRate(task,
                new Date(System.currentTimeMillis() + unit.toMillis(initialDelay)),
                unit.toMillis(period)
        );
    }

    // Повторювана таска с фіксованим інтервалом
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task,
                                                     long initialDelay,
                                                     long delay,
                                                     TimeUnit unit) {
        return taskScheduler.scheduleWithFixedDelay(task,
                new Date(System.currentTimeMillis() + unit.toMillis(initialDelay)),
                unit.toMillis(delay)
        );
    }

    // Зуминка таски
    public void cancel(ScheduledFuture<?> futureTask) {
        futureTask.cancel(true);
    }

    // Зупинка всіх тасків
    public void shutdown() {
        taskScheduler.shutdown();
    }
}
