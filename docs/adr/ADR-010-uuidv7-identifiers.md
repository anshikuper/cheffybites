# ADR-010 --- UUIDv7 Identifier Strategy

## Status

Proposed

## Context

Cheffy Bites requires a consistent identifier strategy across domain
entities, financial records, events, and database tables.

The current architecture uses PostgreSQL `uuid` columns but does not
consistently define the UUID version.

UUIDv7, standardized by RFC 9562, is time-ordered and preserves the
standard 128-bit UUID representation. Compared with random UUIDv4
values, UUIDv7 generally provides better insertion locality for B-tree
indexes while remaining suitable for distributed identifier generation.

## Decision

UUIDv7 is the default identifier strategy for newly created Cheffy Bites
domain records that use UUID identifiers.

The database column type remains:

``` text
UUID
```

Foreign keys do not have a UUID "version" of their own; they store the
UUID value generated for the referenced record.

## Generation Strategy

### Primary --- Application-Side Generation

Application services generate UUIDv7 identifiers before persistence.

The implementation must use a standards-compliant UUIDv7 generator from
the approved Java/Hibernate stack or an approved, well-tested library.

Do not implement UUIDv7 using ad-hoc bit manipulation.

In particular:

-   UUIDv7 uses Unix epoch milliseconds directly.
-   UUIDv7 does not use the legacy UUID Gregorian epoch conversion.
-   Do not call `UUID.nameUUIDFromBytes(...)`; that creates a name-based
    UUID and does not preserve a UUIDv7 layout.
-   Do not overwrite timestamp/version bytes by calling
    `SecureRandom.nextBytes(...)` after constructing them.

The exact Java API is an implementation choice and must be verified
against the Hibernate/Spring Boot version used by the project.

### Database-Side Generation

Database-side UUIDv7 generation may be used where the deployed
PostgreSQL version provides a verified native UUIDv7 function.

Flyway migrations must not assume a function such as
`gen_random_uuid_v7()` exists unless that exact function is available in
the deployed PostgreSQL version.

If application-side generation is the project standard, table
definitions should normally omit UUID defaults:

``` sql
CREATE TABLE identity.users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

## Persistence Rules

-   UUIDv7 identifiers are generated once.
-   Identifiers are immutable.
-   IDs are generated before an entity is published in a domain event.
-   The same identifier is used across persistence, API references, and
    event correlation where that entity ID is required.
-   External provider identifiers remain separate from internal UUIDv7
    identifiers.

## Ordering Rules

UUIDv7 provides useful time ordering, but identifier ordering must not
replace explicit business timestamps.

For deterministic pagination, use a stable tuple such as:

``` text
(created_at, id)
```

or another domain-appropriate ordering key.

Do not infer authoritative business time from a UUID timestamp when a
canonical timestamp field exists.

## Security and Privacy

UUIDv7 contains an approximate creation timestamp.

Therefore:

-   IDs must not be treated as secrets.
-   Authorization must never depend on identifier unpredictability.
-   Public APIs must enforce normal authentication and authorization
    checks.
-   Sensitive creation times must not be inferred from IDs as an
    application feature unless explicitly required.

## Migration Strategy

For an existing database:

1.  Do not rewrite valid existing UUID primary keys solely to convert
    UUIDv4 to UUIDv7.
2.  Keep existing IDs stable.
3.  Generate UUIDv7 for new records after the strategy is adopted.
4.  Foreign keys continue referencing existing identifiers normally.
5.  Any exceptional ID migration requires a separate migration plan and
    ADR because changing primary keys can affect references, events,
    caches, logs, and external integrations.

## Consequences

### Positive

-   Standard UUID representation.
-   Better time locality than random UUIDv4 identifiers.
-   Suitable for distributed generation.
-   Consistent identifier strategy across the platform.
-   No central worker-ID coordination.

### Negative

-   UUIDv7 exposes approximate creation time.
-   Mixed UUID versions may exist after adoption if legacy data already
    uses UUIDv4.
-   Generator support must be verified against the actual
    Java/Hibernate/PostgreSQL versions.
-   UUID ordering is not a substitute for explicit business timestamps.

## Alternatives Considered

### UUIDv4

Not selected as the default because random values provide poorer B-tree
insertion locality.

### ULID

Not selected because UUIDv7 provides time ordering while retaining the
native UUID data type and standard UUID representation.

### TSID / Snowflake-Style IDs

Not selected because they introduce a separate 64-bit identifier scheme
and may require additional coordination or library conventions.

## Implementation Notes

1.  Select and test one RFC 9562-compliant UUIDv7 generator for the
    backend stack.
2.  Centralize identifier generation in a shared infrastructure utility
    or approved persistence mechanism.
3.  Use PostgreSQL `UUID` columns.
4.  Do not add `uuid-ossp` solely for UUIDv7 unless an explicitly
    required database function depends on it.
5.  Add tests that verify:
    -   UUID version is 7.
    -   RFC variant is correct.
    -   IDs are unique.
    -   IDs remain stable after persistence.
6.  Update ERD and developer guidance to state UUIDv7 as the default for
    new UUID identifiers.

## References

-   RFC 9562 --- Universally Unique IDentifiers (UUIDs), UUID Version 7
-   PostgreSQL UUID data type documentation
-   Hibernate identifier-generation documentation for the project's
    selected version
