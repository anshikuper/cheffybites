# ADR-016 — Event Versioning

## Status

Accepted

## Context

The Cheffy Bites platform uses an event-driven architecture with domain events published via the transactional outbox pattern. As the system evolves, event schemas may need to change. We need a clear strategy for evolving event contracts without breaking existing consumers or losing the ability to process historical events.

Key concerns:

- Consumers must be able to process events produced by newer versions of producers
- Producers must be able to add fields without breaking consumers
- Breaking changes must be clearly identifiable and require a new version
- Event history must remain processable even after schema evolution
- The system must support multiple event versions in production simultaneously during transitions

## Decision

### Versioning Strategy

We adopt a **versioned event envelope** with **additive forward compatibility** within a major version.

### Event Envelope

Every event published by the system uses this envelope:

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

**`eventType`** includes the event name and version suffix (e.g., `OrderAccepted.v1`).

**`eventVersion`** is an integer representing the schema version of this event.

### Versioning Rules

#### 1. Additive Changes (Non-Breaking) — Same Version

The following changes are permitted without incrementing `eventVersion`:

- Adding new optional fields to the payload
- Adding new metadata fields to the envelope
- Adding new enum values (consumers must handle unknown values gracefully)

Example — Adding `estimatedDeliveryTime` to `OrderAccepted.v1`:

```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "eventType": "OrderAccepted.v1",
  "eventVersion": 1,
  "occurredAt": "2026-09-01T12:00:00Z",
  "aggregateType": "ORDER",
  "aggregateId": "550e8400-e29b-41d4-a716-446655440001",
  "correlationId": "550e8400-e29b-41d4-a716-446655440002",
  "causationId": "550e8400-e29b-41d4-a716-446655440003",
  "payload": {
    "orderId": "550e8400-e29b-41d4-a716-446655440001",
    "status": "ACCEPTED",
    "estimatedDeliveryTime": "2026-09-01T13:30:00Z"
  }
}
```

#### 2. Breaking Changes — New Version

The following changes **require** incrementing `eventVersion` (e.g., v1 → v2):

- Removing or renaming fields
- Changing field types
- Changing semantics of existing fields
- Changing cardinality (e.g., scalar → array)
- Adding required fields
- Changing enum semantics
- Changing the aggregate boundary

Example — Breaking change for `OrderAccepted.v2`:

```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "eventType": "OrderAccepted.v2",
  "eventVersion": 2,
  "occurredAt": "2026-09-01T12:00:00Z",
  "aggregateType": "ORDER",
  "aggregateId": "550e8400-e29b-41d4-a716-446655440001",
  "correlationId": "550e8400-e29b-41d4-a716-446655440002",
  "causationId": "550e8400-e29b-41d4-a716-446655440003",
  "payload": {
    "orderId": "550e8400-e29b-41d4-a716-446655440001",
    "status": "ACCEPTED",
    "acceptedAt": "2026-09-01T12:00:00Z",
    "estimatedReadyTime": "2026-09-01T12:45:00Z"
  }
}
```

### Consumer Requirements

Consumers **MUST**:

1. Read `eventVersion` from the envelope
2. Tolerate unknown fields in the payload (ignore, don't fail)
3. Handle unknown enum values gracefully
4. Implement version-specific handling for events they consume

Consumers **MUST NOT**:

1. Fail on events with a higher version than expected (log and handle gracefully)
2. Assume fields present in historical events are present in future events
3. Make assumptions about field types without validation

### Version Migration

#### Dual-Write Period

When transitioning between versions:

1. Publish both v1 and v2 events during a transition period
2. Consumers migrate to handle v2
3. Once all consumers support v2, deprecate v1
4. Stop publishing v1 after migration window closes

#### Version Detection Logic

```java
public Event processEvent(Event event) {
    return switch (event.eventType()) {
        case "OrderAccepted.v1" -> handleOrderAcceptedV1(event);
        case "OrderAccepted.v2" -> handleOrderAcceptedV2(event);
        default -> {
            log.warn("Unknown event type: {}, version: {}", 
                event.eventType(), event.eventVersion());
            yield handleUnknownEvent(event);
        }
    };
}
```

### Event Type Registry

Maintain a registry of all event types and their versions:

| Event Type | Current Version | Introduced | Deprecated | Breaking Changes |
|------------|-----------------|------------|------------|------------------|
| OrderCreated.v1 | 1 | 2026-01-01 | — | — |
| OrderAccepted.v1 | 1 | 2026-01-01 | — | — |
| OrderAccepted.v2 | 2 | 2026-09-01 | — | Added estimatedReadyTime |
| PaymentSucceeded.v1 | 1 | 2026-01-01 | — | — |
| RefundProcessed.v1 | 1 | 2026-01-01 | — | — |

### Schema Evolution Examples

#### Scenario 1: Add Kitchen ID to OrderAccepted

Current payload:
```json
{
  "orderId": "uuid",
  "status": "ACCEPTED"
}
```

Add `kitchenId` as an optional field:
```json
{
  "orderId": "uuid",
  "status": "ACCEPTED",
  "kitchenId": "uuid"
}
```

**Version: Same (v1)** — Adding optional fields is non-breaking.

#### Scenario 2: Split Status into Preparation Status per ChefOrderGroup

Current payload:
```json
{
  "orderId": "uuid",
  "status": "PREPARING"
}
```

New payload:
```json
{
  "orderId": "uuid",
  "chefOrderGroups": [
    {
      "chefOrderGroupId": "uuid",
      "status": "PREPARING"
    }
  ]
}
```

**Version: New (v2)** — Changing the payload structure is breaking.

#### Scenario 3: Add Delivery Priority

Current payload:
```json
{
  "orderId": "uuid",
  "delivery": {
    "address": "123 Main St",
    "requestedAt": "2026-09-01T12:00:00Z"
  }
}
```

New payload:
```json
{
  "orderId": "uuid",
  "delivery": {
    "address": "123 Main St",
    "requestedAt": "2026-09-01T12:00:00Z",
    "priority": "STANDARD"
  }
}
```

**Version: Same (v1)** — Adding optional nested fields is non-breaking.

### Consequences

#### Positive

- **Forward Compatibility**: Consumers can process events from newer producers
- **Clear Migration Path**: Dual-write period allows gradual migrations
- **Auditability**: Event history remains processable even after schema evolution
- **Explicit Versioning**: `eventType` and `eventVersion` make the version clear
- **Consumer Protection**: Unknown fields are tolerated, preventing cascading failures

#### Negative

- **Consumer Complexity**: Consumers must implement version-specific handling
- **Dual-Write Overhead**: Transition periods require publishing multiple versions
- **Documentation Burden**: Event schema registry must be kept current
- **Testing Overhead**: Multiple versions must be tested

### Implementation Notes

1. **Event Publishing**
   - Use a versioned event factory to create events
   - Include `eventVersion` in the outbox record
   - Validate event schema before publishing

2. **Event Consumption**
   - Implement a version router that dispatches to version-specific handlers
   - Log unknown event types/versions for monitoring
   - Use schema validation libraries that ignore unknown fields

3. **Monitoring**
   - Track event version distribution across consumers
   - Alert on unknown event types/versions
   - Monitor dual-write transition progress

4. **Testing**
   - Test each event version handler independently
   - Test version migration scenarios
   - Test graceful handling of unknown fields and versions

### Alternatives Considered

1. **Schema Registry (Confluent)** — Rejected: Adds operational complexity; our simpler envelope versioning is sufficient for our scale.

2. **Unversioned Events with Full Compatibility** — Rejected: Without explicit versions, it's difficult to track schema evolution and manage migrations.

3. **Major.Minor Versioning** — Rejected: For our use case, major version increments for breaking changes are sufficient; minor versioning adds unnecessary complexity.

4. **Event Transformation at Gateway** — Rejected: Transforms hide the true event shape and complicate debugging.

### References

- ADR-009 (Outbox Schema) — Event envelope format
- ADR-015 (Financial Ledger) — Example events using eventVersion
- 02-detailed-architecture.md §41 (Event Architecture)
- 05-event-contracts.md
