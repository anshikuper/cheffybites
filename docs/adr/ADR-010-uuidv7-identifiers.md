# ADR-010 — UUIDv7 Identifier Strategy

## Status

Proposed

## Context

The Cheffy Bites architecture requires a consistent identifier strategy across all services and databases. The current ERD uses generic `uuid id PK` without specifying the UUID version. `AGENTS.md` §8.4 recommends "preferably UUIDv7 or another approved time-sortable identifier approach."

UUIDv7 (as defined in RFC 9562) provides:
- Time-ordered identifiers (better for database indexing)
- 48-bit Unix timestamp with millisecond precision
- 74 bits of randomness
- Better performance for B-tree indexes compared to random UUIDs

## Decision

We will use **UUIDv7** as the standard identifier for all primary keys and foreign keys across the Cheffy Bites platform.

### Generation Strategy

**Primary: Java-side generation** (recommended for portability and testability)

```java
// Utility class for UUIDv7 generation
public final class UuidV7 {
    private static final long EPOCH_MILLIS = 12219292800000L; // UUID epoch: 1582-10-15
    
    public static UUID generate() {
        long timestamp = System.currentTimeMillis();
        long uuidTime = timestamp * 10000 + EPOCH_MILLIS;
        
        byte[] bytes = new byte[16];
        
        // Time high (48 bits)
        bytes[0] = (byte) (uuidTime >> 40);
        bytes[1] = (byte) (uuidTime >> 32);
        bytes[2] = (byte) (uuidTime >> 24);
        bytes[3] = (byte) (uuidTime >> 16);
        bytes[4] = (byte) (uuidTime >> 8);
        bytes[5] = (byte) uuidTime;
        
        // Version (4 bits) + time low (12 bits)
        bytes[6] = (byte) (0x70 | ((uuidTime >> 56) & 0x0F)); // Version 7
        bytes[7] = (byte) (uuidTime >> 48);
        
        // Variant (2 bits) + random (62 bits)
        SecureRandom random = new SecureRandom();
        random.nextBytes(bytes);
        bytes[8] = (byte) (0x80 | (bytes[8] & 0x3F)); // Variant 10x
        
        return UUID.nameUUIDFromBytes(bytes); // Or construct directly
    }
}
```

**Alternative: PostgreSQL `uuid_generate_v7()`** (if using `uuid-ossp` extension with v7 support)

```sql
-- Requires PostgreSQL 16+ or uuid-ossp with v7 support
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- For PostgreSQL 17+ with native UUIDv7
SELECT gen_random_uuid_v7();
```

### Hibernate Configuration

```java
@Entity
public class BaseEntity {
    @Id
    @GeneratedValue(generator = "uuid7")
    @GenericGenerator(
        name = "uuid7",
        type = UuidV7Generator.class
    )
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;
}

// Custom generator
public class UuidV7Generator implements IdentifierGenerator {
    @Override
    public Object generate(SharedSessionContractImplementor session, Object object) {
        return UuidV7.generate();
    }
}
```

### Database Schema

All primary keys and foreign keys use UUIDv7:

```sql
-- Example table with UUIDv7
CREATE TABLE identity.users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid_v7(), -- or application-generated
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Foreign keys reference UUIDv7
CREATE TABLE organization.organizations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid_v7(),
    owner_id UUID NOT NULL REFERENCES identity.users(id),
    ...
);
```

### Migration Strategy

1. Add UUID extensions to database:
```sql
-- V20260101_002_uuid_extensions.sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
-- For PostgreSQL 17+, native UUIDv7 is available
```

2. Update all existing tables to use UUIDv7 (if any exist with different UUID versions)

3. Configure Hibernate to use UUIDv7 generator globally

## Consequences

### Positive
- Time-ordered IDs improve B-tree index performance
- Better locality of reference for recent records
- Natural ordering for pagination and time-range queries
- 48-bit timestamp provides ~108 years of uniqueness
- Standardized across all services and databases

### Negative
- Requires custom Hibernate generator (not built-in)
- Slightly larger than UUIDv4 (same 128 bits, but structured)
- Timestamp component leaks creation time (minimal privacy concern)
- Need to ensure clock synchronization across services

## Implementation Notes

- Add `UuidV7` utility class to `common` module
- Configure Spring Boot to use custom generator by default
- Update all entity base classes to use UUIDv7
- Add Flyway migration for UUID extensions
- Document in developer onboarding guide

## Alternatives Considered

1. **UUIDv4 (random)** — Current default in many systems
   - Rejected: Poor index performance, no natural ordering

2. **ULID** — 128-bit, time-ordered, base32 encoded
   - Rejected: Not native UUID format, requires string columns

3. **TSID** — Time-sorted ID, 64-bit
   - Rejected: 64-bit may not be sufficient for distributed systems

4. **Snowflake IDs** — 64-bit, time + worker + sequence
   - Rejected: Requires worker ID coordination, not standard UUID

5. **PostgreSQL `gen_random_uuid()` (v4)** — Built-in
   - Rejected: Same issues as UUIDv4

## References

- RFC 9562: UUID Version 7
- PostgreSQL 17 UUIDv7 support
- Hibernate IdentifierGenerator SPI