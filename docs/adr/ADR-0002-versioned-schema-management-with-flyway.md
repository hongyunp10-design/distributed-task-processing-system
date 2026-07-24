# ADR-0002: Versioned Schema Management with Flyway

## Problem

The project database schema was created and modified manually.

The development and test databases had already drifted:

- `task.type` was `VARCHAR(100)` in development and `VARCHAR(32)` in test.
- `task.status` was `VARCHAR(50)` in development and `VARCHAR(32)` in test.
- The databases used different collations.
- No versioned schema history existed in the repository.

This made database creation dependent on local state and prevented reliable
environment reproduction.

## Options Considered

### Option A: Continue managing the schema manually

Rejected because schema changes would remain undocumented and environments
could continue to drift.

### Option B: Enable Hibernate `ddl-auto=update`

Rejected because the project uses MyBatis and requires explicit, reviewable,
versioned schema evolution.

Automatic schema mutation would also make production changes harder to audit
and control.

### Option C: Adopt Flyway with a baseline for existing databases

Rejected for the current project because there is no production data and the
project is still greenfield.

Baselining would preserve manually created schemas instead of proving that the
schema can be reproduced from repository history.

### Option D: Adopt Flyway using a clean greenfield migration history

Accepted.

Both development and test databases can be recreated from empty databases, so
the repository can define the complete schema beginning with version 1.

## Decision

Use Flyway as the only schema migration mechanism.

The initial schema is defined in:

```text
src/main/resources/db/migration/V1__initial_schema.sql

The migration creates:

* task
* task_state_audit

Flyway creates and maintains:

* flyway_schema_history

Development and test databases are rebuilt from an empty state and migrated
through the same version history.

Migration files are immutable after execution. Future schema changes must be
introduced through new versioned migrations such as V2, V3, and later
versions.

Trade-offs

Benefits

* The schema is reproducible from Git.
* Development, test, CI, and future production environments follow the same
    migration history.
* Schema changes are explicit and reviewable.
* Environment drift becomes easier to detect.
* Database evolution is auditable through flyway_schema_history.

Costs

* Every schema change requires a new migration.
* Applied migrations cannot be edited casually.
* Developers must understand migration ordering and compatibility.
* Destructive changes require deliberate rollout planning.

Implementation

Added Maven dependencies:

* org.flywaydb:flyway-core
* org.flywaydb:flyway-mysql

Spring Boot manages the dependency versions.

Created:
src/main/resources/db/migration/V1__initial_schema.sql

The initial migration defines the existing verified schema without copying
runtime state such as historical AUTO_INCREMENT values.

Existing local development and test databases were recreated because they
contained no production data.

No baselineOnMigrate configuration was enabled.

Validation

Validation completed successfully:

* Flyway executed V1__initial_schema.sql against an empty test database.
* flyway_schema_history recorded version 1 successfully.
* All 70 automated tests passed.
* Flyway executed the same migration against an empty development database.
* The application started successfully.
* The task creation and retrieval APIs successfully persisted and loaded data.
* Development and test schemas now share the same table definitions.

Future Impact

All future schema changes must use new Flyway migrations.

Potential future migrations include:

* adding indexes for task scheduling queries;
* adding referential constraints where appropriate;
* standardizing timestamp precision;
* adding task priority or scheduling fields;
* introducing outbox or queue-related tables.

V1__initial_schema.sql must not be modified after this decision.