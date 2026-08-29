# ADR-009 — Outbox Table Schema

## Status

Accepted

## Context

The Cheffy Bites architecture uses the Transactional Outbox pattern for event-driven integration. This pattern ensures that domain events are reliably published to consumers by writing them to an outbox table in the same transaction as the domain changes, then having a separate process poll the outbox and publish to the message broker.

The original architecture (`02-detailed-architecture.md` §42) only included a sequence diagram, but no concrete table definition was provided in the ERD.

## Decision

We will implement the outbox with the following schema:

### Outbox Events Table

```sql
CREATE SCHEMA IF NOT EXISTS outbox;

CREATE TABLE outbox.outbox_events (
    id UUID PRIMARY KEY,
    
    -- Event identification
    aggregate_type VARCHAR(100) NOT NULL,  -- e.g., 'ORDER', 'KITCHEN_BOOKING'
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(200) NOT NULL,        -- e.g., 'OrderCreated.v1'
    event_version INT NOT NULL DEFAULT 1,
    
    -- Tracing identifiers
    correlation_id UUID,                     -- Links related events
    causation_id UUID,                      -- What triggered this event
    
    -- Event payload
    payload JSONB NOT NULL,
    
    -- Timestamps
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    published_at TIMESTAMPTZ NULL,           -- NULL = not yet published
    
    -- Retry management
    attempts INT NOT NULL DEFAULT 0,
    last_error TEXT NULL,
    next_attempt_at TIMESTAMPTZ NULL,
    
    -- Metadata
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Index for finding unpublished events
CREATE INDEX idx_outbox_unpublished 
ON outbox.outbox_events (published_at, next_attempt_at) 
WHERE published_at IS NULL;

-- Index for aggregate lookups
CREATE INDEX idx_outbox_aggregate 
ON outbox.outbox_events (aggregate_type, aggregate_id);

-- Index for correlation tracing
CREATE INDEX idx_outbox_correlation 
ON outbox.outbox_events (correlation_id) 
WHERE correlation_id IS NOT NULL;
```

The application supplies `id` through the repository-approved identifier-generation layer when it persists the event in the domain transaction. The persisted database type remains `UUID`; this schema does not prescribe a database-generated random UUID default.

### Event Envelope

Each event follows this envelope structure:

```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "eventType": "OrderCreated.v1",
  "occurredAt": "2026-09-01T12:00:00Z",
  "aggregateType": "ORDER",
  "aggregateId": "550e8400-e29b-41d4-a716-446655440001",
  "correlationId": "550e8400-e29b-41d4-a716-446655440002",
  "causationId": "550e8400-e29b-41d4-a716-446655440003",
  "eventVersion": 1,
  "payload": {
    "orderId": "550e8400-e29b-41d4-a716-446655440001",
    "customerId": "550e8400-e29b-41d4-a716-446655440004",
    "kitchenId": "550e8400-e29b-41d4-a716-446655440005",
    "totalAmount": 4500,
    "currency": "CAD"
  }
}
```

### Outbox Processor Behavior

The outbox processor (scheduled job) follows this logic:

1. Query for events where `published_at IS NULL` and `next_attempt_at IS NULL OR next_attempt_at <= now()`
2. Process in batches (configurable batch size, default 100)
3. For each event:
   - Publish to SNS/SQS or EventBridge
   - If successful: set `published_at = now()`
   - If failed: increment `attempts`, set `last_error`, calculate `next_attempt_at`
4. Exponential backoff: `next_attempt_at = now() + (2^attempts * 1000ms)`, capped at 1 hour
5. After max attempts (default 10), move to dead letter (log + alert)

### Compatibility

- Additive payload changes are allowed within a version.
- Breaking changes require a new `event_version`.
- Consumers must tolerate unknown fields in event payloads.

### Transaction Pattern

```java
@Transactional
public void createOrder(CreateOrderCommand command) {
    // 1. Persist domain changes
    Order order = orderRepository.save(new Order(command));
    
    // 2. Persist outbox event in same transaction
    outboxEventRepository.save(new OutboxEvent(
        aggregateType = "ORDER",
        aggregateId = order.getId(),
        eventType = "OrderCreated.v1",
        payload = order.toEventPayload(),
        occurredAt = Instant.now(),
        correlationId = command.getCorrelationId()
    ));
    
    // 3. Commit transaction - both changes are atomic
}
```

## Consequences

### Positive
- Guaranteed delivery: if the transaction commits, the event is in the outbox
- No distributed transaction needed between database and message broker
- Easy replay: just reset `published_at` to NULL
- Built-in retry with exponential backoff
- Event history preserved for debugging
- Correlation and causation IDs for tracing

### Negative
- Adds latency: events are not published until the processor runs (typically sub-second)
- Additional database table and index storage
- Need to manage dead letters and monitoring
- Processor adds operational complexity

## Implementation Notes

- Use Spring's `@TransactionalEventListener` with a custom outbox repository
- Implement `OutboxEventPublisher` as a `@Scheduled` job
- Add Spring Actuator endpoints for outbox monitoring
- Configure dead letter queue in SNS/SQS for failed events
- Add alerting for events exceeding max attempts
- Keep inbound provider webhook deduplication in a separate provider-events/inbox table rather than the outbox

## Alternatives Considered

1. **Change Data Capture (CDC) with Debezium**
   - Rejected: Adds infrastructure complexity (Kafka, Debezium connector)
   - Overkill for MVP scale

2. **Direct publishing with retries**
   - Rejected: No guaranteed delivery if broker is down
   - Violates "at least once" guarantee

3. **Polling publisher with SELECT ... SKIP LOCKED**
   - Considered: Better for concurrent processors
   - Can be added as optimization if needed

4. **Event Store as separate table**
   - Rejected: Over-engineering for MVP
   - Simple outbox is sufficient
