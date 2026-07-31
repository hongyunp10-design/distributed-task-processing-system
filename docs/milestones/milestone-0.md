# Milestone 0 — Establish Performance Baseline

## Problem

The service had functional task execution, but it did not yet provide a
controlled and observable environment for performance experiments.

Without explicit runtime configuration and executor metrics, experiment results
could not be reproduced or explained reliably. In particular, the system could
not show whether work was actively executing, waiting in the queue, or already
completed.

## Solution

The baseline milestone introduced three incremental changes.

### Commit 1 — Executor Queue Configuration

The task execution executor now honors the configured queue capacity instead of
silently relying on a framework default.

### Commit 2 — Reproducible Baseline Environment

A dedicated `baseline` profile defines the experiment environment explicitly:

- isolated MySQL database
- isolated Redis logical database
- fixed worker polling configuration
- fixed executor pool and queue configuration
- retry scheduler disabled
- timeout scheduler disabled
- fixed logging levels

### Commit 3 — Baseline Observability

Spring Boot Actuator and Micrometer expose:

- application health
- HTTP metrics
- JVM metrics
- process and system metrics
- HikariCP metrics
- task-executor runtime metrics

Custom executor gauges read from the actual runtime `ThreadPoolExecutor`:

- `async.task.executor.active`
- `async.task.executor.pool.size`
- `async.task.executor.queue.size`
- `async.task.executor.queue.remaining`
- `async.task.executor.completed`

## Trade-off

Prometheus, Grafana, OpenTelemetry, tracing, business metrics, latency metrics,
and queue-wait metrics were intentionally excluded.

The goal of this slice is to make the local baseline observable with the
smallest useful infrastructure change. Adding an external monitoring stack
before the workload and measurement model are stable would increase operational
complexity without improving the first baseline experiment.

The metrics are also sourced from the runtime executor rather than from
configuration properties. Configuration describes intended capacity; runtime
state shows what the service is actually doing.

## Result

Runtime verification demonstrated that:

- active executor count reflects work currently running
- queue size reflects queued runnables
- queue remaining capacity increases as queued work drains
- completed count increases when executor runnables finish
- health, HTTP, JVM, process, system, and HikariCP metrics are exposed

The observability work also exposed an existing dispatch defect: tasks already
waiting in the executor queue remain persisted as `PENDING` and can therefore
be submitted again by a later polling cycle.

The existing state claim prevents duplicate business execution, but the
duplicate runnables waste executor capacity. This issue is not caused by the
metrics implementation and is intentionally deferred to a separate change.

## Next Step

Design a reliable task-dispatch claim so that a task can be distinguished
between:

- available for dispatch
- already queued
- actively running
- completed

The design must also consider submission failure, application restart, and
future multi-instance execution.
