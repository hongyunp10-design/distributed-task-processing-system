package com.hongyun.asynctask.config;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class TaskExecutionMetricsConfig {

    @Bean
    public MeterBinder taskExecutionExecutorMetrics(
            @Qualifier("taskExecutionExecutor")
            ThreadPoolTaskExecutor taskExecutionExecutor
    ) {
        return registry -> {
            Gauge.builder(
                            "async.task.executor.active",
                            taskExecutionExecutor,
                            executor -> getThreadPoolExecutor(executor)
                                    .getActiveCount()
                    )
                    .description("Number of task execution threads actively executing tasks")
                    .register(registry);

            Gauge.builder(
                            "async.task.executor.pool.size",
                            taskExecutionExecutor,
                            executor -> getThreadPoolExecutor(executor)
                                    .getPoolSize()
                    )
                    .description("Current number of threads in the task execution pool")
                    .register(registry);

            Gauge.builder(
                            "async.task.executor.queue.size",
                            taskExecutionExecutor,
                            executor -> getThreadPoolExecutor(executor)
                                    .getQueue()
                                    .size()
                    )
                    .description("Number of tasks waiting in the task execution queue")
                    .register(registry);

            Gauge.builder(
                            "async.task.executor.queue.remaining",
                            taskExecutionExecutor,
                            executor -> getThreadPoolExecutor(executor)
                                    .getQueue()
                                    .remainingCapacity()
                    )
                    .description("Remaining capacity of the task execution queue")
                    .register(registry);

            Gauge.builder(
                            "async.task.executor.completed",
                            taskExecutionExecutor,
                            executor -> getThreadPoolExecutor(executor)
                                    .getCompletedTaskCount()
                    )
                    .description("Total number of tasks completed by the task execution executor")
                    .register(registry);
        };
    }

    private ThreadPoolExecutor getThreadPoolExecutor(
            ThreadPoolTaskExecutor taskExecutionExecutor
    ) {
        return taskExecutionExecutor.getThreadPoolExecutor();
    }
}