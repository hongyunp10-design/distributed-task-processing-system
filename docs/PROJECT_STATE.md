# Project State

## Current Milestone

Milestone 0.5 — Schema Management

## Completed

### Task API

- Create task
- Get task by ID
- List tasks
- Update task
- Delete task
- Request validation
- Global exception handling

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
- `V1__initial_schema.sql`
- Development database rebuilt from migration history
- Test database rebuilt from migration history
- `flyway_schema_history` verification

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

Test Status

Tests run: 70
Failures: 0
Errors: 0
Skipped: 0

Known Follow-up

The task creation response currently returns database-generated timestamps as
null immediately after insertion.

A subsequent GET returns the persisted created_at and updated_at values.

This should be handled as a separate behavior-consistency slice rather than
being mixed into schema management.

Next Candidate Work

* Decide whether POST task should reload the persisted entity.
* Design query indexes based on actual access patterns.
* Standardize timestamp types and precision through a new migration.
* Continue toward task execution and scheduling infrastructure.
