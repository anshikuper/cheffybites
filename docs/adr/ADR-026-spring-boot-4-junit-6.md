# ADR-026 — Spring Boot 4 Test Baseline with JUnit 6

## Status

Accepted

Accepted during `P1-S00-T01` compatibility reconciliation on 2026-09-03.

## Context

The approved backend baseline combines Java 21 and Spring Boot 4.x. Earlier
technology summaries also named JUnit 5. P1-S00 required an executable
compatibility proof rather than selecting versions from memory.

Spring Boot 4.1.1 manages Spring Framework 7.0.9 and JUnit Jupiter 6.0.3. An
isolated Java 21 and Gradle 9.7.1 proof explicitly overriding the supported
`junit-jupiter.version` property to 5.14.4 resolved JUnit Jupiter 5.14.4 and
JUnit Platform 1.14.4, but Spring integration-test initialization failed with
a binary `NoSuchMethodError` from `SpringExtension`. The same failure occurred
with Spring Boot 4.0.8. The equivalent Spring Boot 4.1.1 control using its
managed JUnit Jupiter 6.0.3 completed successfully.

The failure is a framework compatibility boundary, not an application-test
failure. Downgrading Spring Boot would contradict the approved Spring Boot 4
baseline, while forcing JUnit 5 would leave Spring integration testing
binary-incompatible.

## Decision

Cheffy Bites backend tests use JUnit 6 with Spring Boot 4.

The P1-S00 foundation pins:

```text
Java                    21 LTS
Spring Boot             4.1.1
JUnit Jupiter           6.0.3
JUnit Platform          6.0.3
```

Spring Boot dependency management remains authoritative for the mutually
compatible JUnit module set. The build must not override JUnit back to major
version 5. Testcontainers remains the PostgreSQL integration-test foundation.

Any later Spring Boot or JUnit upgrade must update the pinned compatibility
matrix and rerun at least a Java compile, Spring application-context test, and
PostgreSQL Testcontainers smoke test before adoption.

This ADR supersedes earlier JUnit 5 references in technology summaries. It does
not change production runtime behavior, business rules, API contracts, event
contracts, persistence contracts, or the modular-monolith architecture.

## Consequences

- Spring Boot 4 and Spring Framework 7 integration tests use their supported
  JUnit generation.
- Backend tests and test extensions must be compatible with JUnit 6.
- The Java 21 and Spring Boot 4 baselines remain unchanged.
- Dependency upgrades remain explicit and reproducibly proven rather than
  following an unpinned latest version.

## Alternatives Considered

### Force JUnit 5 Under Spring Boot 4

Rejected because both tested Spring Boot 4 lines resolved the requested JUnit
5 artifacts but failed during Spring test-extension initialization with a
binary method mismatch.

### Downgrade to Spring Boot 3

Rejected because it would violate the approved Spring Boot 4 architecture
baseline and the P1-S00 acceptance criteria.

### Use JUnit 6 Without Recording an Architecture Decision

Rejected because it would silently contradict canonical technology documents
and would make the compatibility rationale undiscoverable to later work.

## References

- `AGENTS.md`
- `docs/01-master-spec.md`
- `docs/02-detailed-architecture.md`
- `docs/product/P1-IMP-01-phase1-implementation-plan.md`
- Spring Boot 4.1.1 dependency management
- Spring Framework 7.0.9 `SpringExtension`
