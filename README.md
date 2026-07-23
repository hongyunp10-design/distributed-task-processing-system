# Distributed Task Processing System

A production-oriented backend project that incrementally evolves from a simple async task service into a distributed task processing platform.

Instead of building everything at once, this repository demonstrates how a backend system grows milestone by milestone through engineering trade-offs, experiments, and architectural evolution.

---

# Project Goal

The goal of this project is not only to implement an async task service.

It is to practice how experienced backend engineers design, evolve, and maintain production systems.

Every milestone introduces one real engineering problem and explores different design choices before implementation.

---

# Current Status

## Phase 1 (Completed)

Current capabilities include:

- RESTful Task API
- MySQL persistence
- Task state machine
- Atomic task claiming
- Asynchronous task execution
- Retry scheduler
- Timeout recovery
- Redis cache
- Task execution worker
- Task state audit
- Statistics API
- Integration tests

Current test status:  
- 67 Tests
- 0 Failures
- 0 Errors

---

# Engineering Workflow

Every milestone follows the same workflow:

Requirement

↓

Design

↓

Experiment

↓

Implementation

↓

Verification

↓

Documentation

↓

Git Review

↓

Code Review

---

# Roadmap

| Milestone | Topic | Status |
|------------|--------|--------|
| Phase 1 | Async Task Processing System | ✅ Completed |
| Milestone 0 | Establish Baseline Metrics | ⏳ Planned |  
| Milestone 1 | Concurrency & Atomic Claim | ⏳ Planned |  
| Milestone 2 | Worker Failure Recovery | ⏳ Planned |  
| Milestone 3 | Retry Strategy | ⏳ Planned |  
| Milestone 4 | Idempotency | ⏳ Planned |  
| Milestone 5 | Queue Evolution | ⏳ Planned |  
| Milestone 6 | Observability | ⏳ Planned |  
| Milestone 7 | Scaling | ⏳ Planned |  
| Milestone 8 | Cloud Native | ⏳ Planned |  
| Milestone 9 | Chaos Testing | ⏳ Planned |

---

# Repository Structure
- src/
- docs/
- README.md
- Documentation will be added incrementally as the project evolves.

---

# Documentation

Planned documentation includes:

- Architecture

- Design Decisions

- Milestones

- Experiments

- Interview Notes

---

# Philosophy

This repository focuses on learning engineering thinking instead of memorizing frameworks.

Every feature should answer:

- Why is it needed?

- What problem does it solve?

- What are the trade-offs?

- How can we verify it works?

Every milestone must leave behind reusable engineering assets, not just working code.

Optimization without measurement is guesswork.