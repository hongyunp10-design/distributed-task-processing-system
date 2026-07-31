# Project State

## Current Milestone

Milestone 0 — Establish Performance Baseline

## Completed

### Baseline Runtime Environment

- Dedicated `baseline` Spring profile
- Isolated baseline MySQL database
- Isolated Redis logical database
- Explicit worker and executor configuration
- Retry scheduler disabled for baseline experiments
- Timeout scheduler disabled for baseline experiments
- Fixed logging configuration

### Baseline Observability

- Spring Boot Actuator
- Micrometer runtime metrics
- Health endpoint
- Metrics endpoint
- HTTP server metrics
- JVM metrics
- Process and system metrics
- HikariCP connection-pool metrics
- Runtime task-executor metrics:
    - `async.task.executor.active`
    - `async.task.executor.pool.size`
    - `async.task.executor.queue.size`
    - `async.task.executor.queue.remaining`
    - `async.task.executor.completed`

The custom executor metrics read directly from the runtime
`ThreadPoolExecutor`, rather than duplicating configured values.

### Task API

- Create task
- Get task by ID
- List tasks
- Update task
- Delete task
- Request validation
- Global exception handling
- POST response returns persisted database-generated timestamps

### Persistence

- MySQL persistence
- MyBatis mapper layer
- Database-generated task IDs

### Task Lifecycle

- PENDING → RUNNING
- RUNNING → SUCCESS
- RUNNING → FAILED
- FAILED → PENDING
- FAILED → DEAD
- Retry-count enforcement
- State-transition audit records
- Transactional rollback between task state and audit records

### Lifecycle Timestamps

- `created_at`
- `updated_at`
- `started_at`
- `completed_at`

`completed_at` is populated only for terminal states:

- SUCCESS
- DEAD

### Schema Management

- Flyway Core
- Flyway MySQL support
- Versioned schema migrations
- Development database rebuilt from migration history
- Test database rebuilt from migration history
- `flyway_schema_history` verification
- Standardized timestamp precision

### Architecture Decisions

- ADR-0001: Terminal completion timestamp
- ADR-0002: Versioned schema management with Flyway
- ADR-0003: Introduce Redis cache
- ADR-0004: Dedicated baseline runtime profile

## Current Database Tables

### task

Stores the current task state and lifecycle timestamps.

### task_state_audit

Stores append-only state-transition history.

### flyway_schema_history

Stores applied database migration history.

## Current Invariants

```text
SUCCESS / DEAD
⇔
completed_at IS NOT NULL

PENDING / RUNNING / FAILED
⇔
completed_at IS NULL

Task state update and audit insertion
occur in the same transaction.

Database schema changes
must be introduced through new Flyway migrations.
```

## Verification Status

Baseline observability verification passed:

- /actuator/health returns UP
- /actuator/metrics exposes framework and custom metrics
- Executor active count reflects runtime execution
- Executor queue size decreases while work is processed
- Executor remaining capacity increases as the queue drains
- Executor completed count increases as submitted runnables finish
- HikariCP, JVM, process, system, and HTTP metrics are available

## Known Follow-up

Executor metric verification exposed an existing dispatch problem.

Tasks remain persisted as PENDING after they have been submitted to the
in-memory executor queue. If execution takes longer than the polling interval,
a later worker scan can find and submit the same queued tasks again.

The task-state claim prevents duplicate business execution by rejecting the
later SUCCESS → RUNNING attempts. However, duplicate runnables consume queue
capacity, create unnecessary database work, add log noise, and increase the
executor completed-task count.

This behavior predates the observability change and will be addressed as a
separate reliability change after Commit 3.

## Next Candidate Work

- Design a reliable dispatch-claim mechanism
- Prevent repeated submission of tasks already waiting in the executor queue
- Define recovery behavior for application restart or failed submission
- Preserve correct behavior in a future multi-instance deployment
- Continue baseline workload and performance experiments
