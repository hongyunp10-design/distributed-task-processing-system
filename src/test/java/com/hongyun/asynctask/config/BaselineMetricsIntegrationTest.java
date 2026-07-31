package com.hongyun.asynctask.config;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url="
                + "jdbc:mysql://localhost:3307/async_task_service_test"
                + "?useSSL=false"
                + "&allowPublicKeyRetrieval=true"
                + "&serverTimezone=UTC",

        // Prevent scheduled workers from changing executor metrics
        // while this test is asserting their initial values.
        "spring.task.scheduling.enabled=false"
})
@AutoConfigureMockMvc
@ActiveProfiles("baseline")
class BaselineMetricsIntegrationTest {

    private static final String ACTIVE_METRIC =
            "async.task.executor.active";

    private static final String POOL_SIZE_METRIC =
            "async.task.executor.pool.size";

    private static final String QUEUE_SIZE_METRIC =
            "async.task.executor.queue.size";

    private static final String QUEUE_REMAINING_METRIC =
            "async.task.executor.queue.remaining";

    private static final String COMPLETED_METRIC =
            "async.task.executor.completed";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    @Qualifier("taskExecutionExecutor")
    private ThreadPoolTaskExecutor taskExecutionExecutor;

    @Test
    void shouldExposeHealthAndMetricsEndpoints() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.names").isArray())
                .andExpect(jsonPath(
                        "$.names[?(@ == 'async.task.executor.active')]"
                ).exists())
                .andExpect(jsonPath(
                        "$.names[?(@ == 'async.task.executor.pool.size')]"
                ).exists())
                .andExpect(jsonPath(
                        "$.names[?(@ == 'async.task.executor.queue.size')]"
                ).exists())
                .andExpect(jsonPath(
                        "$.names[?(@ == 'async.task.executor.queue.remaining')]"
                ).exists())
                .andExpect(jsonPath(
                        "$.names[?(@ == 'async.task.executor.completed')]"
                ).exists());
    }

    @Test
    void shouldExposeEachExecutorMetricEndpoint() throws Exception {
        assertMetricEndpointAvailable(ACTIVE_METRIC);
        assertMetricEndpointAvailable(POOL_SIZE_METRIC);
        assertMetricEndpointAvailable(QUEUE_SIZE_METRIC);
        assertMetricEndpointAvailable(QUEUE_REMAINING_METRIC);
        assertMetricEndpointAvailable(COMPLETED_METRIC);
    }

    @Test
    void shouldRegisterMetricsFromRuntimeExecutor() {
        assertGaugeValueIsNonNegative(ACTIVE_METRIC);
        assertGaugeValueIsNonNegative(POOL_SIZE_METRIC);
        assertGaugeValueIsNonNegative(QUEUE_SIZE_METRIC);
        assertGaugeValueIsNonNegative(COMPLETED_METRIC);

        Gauge queueRemaining = meterRegistry.find(QUEUE_REMAINING_METRIC)
                .gauge();

        assertThat(queueRemaining).isNotNull();
        assertThat(queueRemaining.value())
                .isEqualTo(
                        taskExecutionExecutor
                                .getThreadPoolExecutor()
                                .getQueue()
                                .remainingCapacity()
                );
        assertThat(queueRemaining.value()).isEqualTo(100.0);
    }

    private void assertMetricEndpointAvailable(
            String metricName
    ) throws Exception {
        mockMvc.perform(get("/actuator/metrics/{metricName}", metricName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(metricName))
                .andExpect(jsonPath("$.measurements").isArray())
                .andExpect(jsonPath("$.measurements[0].value").isNumber());
    }

    private void assertGaugeValueIsNonNegative(
            String metricName
    ) {
        Gauge gauge = meterRegistry.find(metricName).gauge();

        assertThat(gauge).isNotNull();
        assertThat(gauge.value()).isGreaterThanOrEqualTo(0.0);
    }
}