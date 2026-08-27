# ADR-016 — Event Versioning

## Status

Accepted

## Context

Cheffy Bites publishes domain events through the transactional outbox. Event contracts must evolve without unnecessary breakage while breaking semantic changes remain explicit and historical events remain processable.

## Decision

### Versioned Event Envelope

```json
{
  "eventId": "uuid",
  "eventType": "OrderAccepted.v1",
  "eventVersion": 1,
  "occurredAt": "2026-09-01T12:00:00Z",
  "aggregateType": "ORDER",
  "aggregateId": "uuid",
  "correlationId": "uuid",
  "causationId": "uuid",
  "payload": {}
}
```

`eventType` includes the version suffix and `eventVersion` is numeric. They must agree; publishing `OrderAccepted.v1` with `eventVersion: 2` is invalid.

### Same-Version Changes

Allowed within the same version:

- Add optional payload fields.
- Add optional envelope metadata.
- Add enum values only when consumers explicitly tolerate unknown values.

Removing/renaming fields, changing types/cardinality/semantics, adding required fields, or changing aggregate meaning requires a new version.

### Consumer Requirements

Consumers MUST:

1. Route by complete event type/version.
2. Ignore unknown optional fields.
3. Validate required fields/types for supported versions.
4. Handle unknown enum values safely.
5. Treat unsupported versions as controlled compatibility conditions.

Consumers MUST NOT process a higher unknown version as if it were the older version. Depending on criticality, they must safely skip, park/retry/dead-letter, or alert according to explicit policy.

### Version Migration

For breaking changes:

1. Register the new contract.
2. Upgrade consumers.
3. Dual-publish old/new versions only when migration requires it.
4. Use separate event/outbox identities for separately published versions.
5. Monitor adoption.
6. Retire the old version after the migration window.

Dual publishing is a migration technique, not mandatory for every change.

### Registry

Maintain event type, version, schema/contract location, producer, known consumers, introduction date, deprecation status, and compatibility notes.

## Implementation Notes

- Store event type/version in outbox.
- Validate envelope consistency before publication.
- Use version-specific handlers.
- Monitor unsupported versions and migrations.
- Test additive compatibility, breaking migrations, unknown fields/enums, and dual publishing.

## Consequences

### Positive

- Explicit contract evolution.
- Additive changes avoid unnecessary migrations.
- Breaking changes are independently deployable.
- Historical events remain identifiable.

### Negative

- Multiple handlers may coexist temporarily.
- Registry and migration discipline are required.

## References

ADR-002, ADR-009, ADR-012, ADR-015.
