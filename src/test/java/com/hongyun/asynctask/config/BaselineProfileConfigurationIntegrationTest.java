package com.hongyun.asynctask.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        // Reuse the existing test database so this test does not require
        // another database during the normal test suite.
        "spring.datasource.url="
                + "jdbc:mysql://localhost:3307/async_task_service_test"
                + "?useSSL=false"
                + "&allowPublicKeyRetrieval=true"
                + "&serverTimezone=UTC",

        // Prevent scheduled work from running during context verification.
        "spring.task.scheduling.enabled=false"
})
@ActiveProfiles("baseline")
class BaselineProfileConfigurationIntegrationTest {

    @Autowired
    private Environment environment;

    @Autowired
    private TaskExecutionAsyncProperties asyncProperties;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutionExecutor;

    @Test
    void shouldLoadDedicatedBaselineConfiguration() {
        assertThat(environment.getActiveProfiles())
                .containsExactly("baseline");

        assertThat(environment.getProperty(
                "app.task.execution-worker.enabled",
                Boolean.class
        )).isTrue();

        assertThat(environment.getProperty(
                "app.task.execution-worker.fixed-delay-ms",
                Long.class
        )).isEqualTo(10_000L);

        assertThat(environment.getProperty(
                "app.task.execution-worker.batch-size",
                Integer.class
        )).isEqualTo(5);

        assertThat(environment.getProperty(
                "app.task.retry-scheduler.enabled",
                Boolean.class
        )).isFalse();

        assertThat(environment.getProperty(
                "app.task.timeout-scheduler.enabled",
                Boolean.class
        )).isFalse();

        assertThat(environment.getProperty(
                "spring.data.redis.database",
                Integer.class
        )).isEqualTo(1);
    }

    @Test
    void shouldApplyBaselineExecutorConfiguration() {
        assertThat(asyncProperties.getCorePoolSize()).isEqualTo(2);
        assertThat(asyncProperties.getMaxPoolSize()).isEqualTo(4);
        assertThat(asyncProperties.getQueueCapacity()).isEqualTo(100);
        assertThat(asyncProperties.getThreadNamePrefix())
                .isEqualTo("baseline-task-execution-");

        assertThat(taskExecutionExecutor.getCorePoolSize()).isEqualTo(2);
        assertThat(taskExecutionExecutor.getMaxPoolSize()).isEqualTo(4);
        assertThat(taskExecutionExecutor.getThreadPoolExecutor()
                .getQueue()
                .remainingCapacity())
                .isEqualTo(100);
    }
}
