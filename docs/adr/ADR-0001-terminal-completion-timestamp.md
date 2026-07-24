# ADR-0001: Persist Terminal Task Completion Timestamp

- Status: Accepted
- Date: 2026-07-23
- Milestone: M0 - Observability Baseline
- Slice: Slice 1

---

# Problem

The task lifecycle was incomplete.

Current lifecycle:

created_at
↓
started_at
↓
?

The system knew when a task was created and when the latest execution attempt started,
but it never recorded when the task actually finished.

As a result:

- End-to-End Latency could not be calculated.
- Completion Throughput could not be calculated.
- Dashboard could not distinguish unfinished tasks from completed tasks.
- Lifecycle data model was incomplete.

---

# Options Considered

## Option A

Record completed_at whenever a task leaves RUNNING.

Lifecycle:

RUNNING
↓
SUCCESS / FAILED

Pros

- Easy implementation.

Cons

- FAILED is not a terminal state.
- Retryable tasks would incorrectly appear completed.
- Completion metrics become inaccurate.

Rejected.

---

## Option B

Record completed_at only when entering terminal states.

Terminal states:

SUCCESS
DEAD

Pros

- Represents the true end of task lifecycle.
- Metrics become semantically correct.
- Retry remains part of the same lifecycle.

Accepted.

---

# Decision

Persist completed_at only when a task enters a terminal state.

Terminal states:

SUCCESS
DEAD

Non-terminal states:

PENDING
RUNNING
FAILED

must never have completed_at.

System invariant:

SUCCESS / DEAD
⇔
completed_at IS NOT NULL

PENDING / RUNNING / FAILED
⇔
completed_at IS NULL

---

# Trade-offs

Advantages

- Correct lifecycle semantics.
- Enables lifecycle metrics.
- Simple invariant.
- Easy to validate.

Disadvantages

- FAILED no longer represents an execution end.
- Attempt-level execution duration cannot be reconstructed from the task table alone.

Attempt-level analysis will instead rely on task_state_audit.

---

# Implementation

Changes introduced:

- Add completedAt to Task model.
- Map completed_at in MyBatis.
- Populate completed_at on:
    - RUNNING → SUCCESS
    - FAILED → DEAD
- Preserve NULL for:
    - PENDING
    - RUNNING
    - FAILED
- Extend lifecycle integration tests.
- Extend transaction rollback tests.

---

# Validation

Verified by automated tests.

Lifecycle tests:

✓ SUCCESS sets completed_at

✓ DEAD sets completed_at

✓ FAILED keeps completed_at NULL

✓ Retry keeps completed_at NULL

Transaction tests:

✓ Status update rolls back if audit insert fails.

✓ completed_at also rolls back.

Total tests:

70 passed.

---

# Future Impact

This decision establishes the lifecycle foundation for future observability work.

Upcoming metrics enabled by this ADR:

- End-to-End Latency
- Completion Throughput
- Success Rate
- Dead Task Rate

Attempt-level metrics will be derived from task_state_audit rather than the task table.

Future schema work:

- Introduce Flyway.
- Unify lifecycle timestamps to TIMESTAMP(3).