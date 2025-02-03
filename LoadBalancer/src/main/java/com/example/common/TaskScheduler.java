package com.example.common;

import java.util.concurrent.*;

public class TaskScheduler {
    private final ScheduledExecutorService scheduler;

    public TaskScheduler(int poolSize) {
        this.scheduler = Executors.newScheduledThreadPool(poolSize);
    }

    public void scheduleTask(Runnable task, long delay, TimeUnit unit) {
        scheduler.schedule(task, delay, unit);
    }
}