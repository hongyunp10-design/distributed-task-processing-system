# ADR-0004: Dedicated Baseline Runtime Profile

## Status

Accepted

## Context

Milestone 0 requires repeatable and comparable performance experiments.

Previously, development and performance experiments used the same runtime
configuration. Manual changes to worker scheduling, executor capacity,
logging, retry behavior, timeout behavior, database data, or Redis cache state
could change experiment results without being recorded.

A performance baseline must run under an explicit and isolated configuration.

## Decision

Introduce a dedicated Spring profile:

```text
baseline
The profile is defined in:
src/main/resources/application-baseline.yml

All Milestone 0 performance experiments must activate this profile.

The profile:

* uses a dedicated MySQL database;
* uses a separate Redis logical database;
* explicitly configures the execution worker;
* explicitly configures the async executor;
* disables retry scheduling;
* disables timeout scheduling;
* fixes application logging levels.

Experiment-specific variables may be supplied as explicit command-line
overrides. Such overrides must be recorded with the experiment result.

Development and test profiles remain unchanged.
```
## Consequences
### Positive
* Baseline experiments have an explicit runtime configuration.
* Development data does not contaminate baseline data.
* Redis cache state is isolated from development.
* Scheduler and executor settings are reproducible.
* Experiment commands can be recorded and rerun.
* Future performance results can be compared against the same configuration.

### Negative
* The project now maintains an additional runtime profile.
* Baseline database creation is an environment setup requirement.
* Experiment-specific overrides must be documented carefully.
* Configuration changes to shared application behavior may need corresponding
    baseline-profile review.

## Alternatives Considered
### Modify `application.yml` before each experiment
Rejected because it depends on manual edits, can contaminate development
configuration, and does not produce a reliable experiment history.

### Create a separate benchmark application
Rejected for Milestone 0 because it would duplicate application wiring and
introduce unnecessary maintenance complexity.

## Scope
This decision establishes configuration isolation only.

It does not introduce:

* performance metrics;
* workload generation;
* batch task creation;
* benchmark result APIs;
* SQL optimization;
* executor optimization;
* production observability infrastructure.
