# Cheffy Bites — Event Contracts

> **Source of Truth:** This document is **subsidiary** to [`02-detailed-architecture.md`](docs/02-detailed-architecture.md) Sections 41–42.
> All event contract changes must be made in `02-detailed-architecture.md` first.
> This document exists for convenient reference and will be regenerated from `02` during CI.
> The authoritative machine-readable contract is the AsyncAPI document generated from the backend.

---

# 41. Event Architecture

Events are internal integration contracts, not database row dumps.

Recommended envelope:

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

Compatibility rules:

- Additive changes within a version are allowed.
- Breaking changes require a new event version.
- Consumers must tolerate unknown fields.

Core events:

```text
KitchenPublished.v1
KitchenBookingConfirmed.v1
KitchenBookingCancelled.v1
KitchenBookingHeld.v1
KitchenBookingHoldExpired.v1
FoodPublished.v1
FoodAvailabilityChanged.v1
FoodRequestCreated.v1
FoodRequestInterestAdded.v1
FoodRequestFulfilled.v1
OrderCreated.v1
ChefOrderGroupCreated.v1
ChefOrderGroupAdjusted.v1
PromotionSnapshotCreated.v1
PaymentInitiated.v1
PaymentSucceeded.v1
PaymentFailed.v1
PaymentCaptured.v1
PaymentAllocated.v1
OrderAccepted.v1
OrderRejected.v1
ChefOrderGroupPreparing.v1
ChefOrderGroupReady.v1
DeliveryRequested.v1
DriverAssigned.v1
OrderPickedUp.v1
OrderOutForDelivery.v1
OrderDelivered.v1
OrderCancelled.v1
RefundRequested.v1
RefundProcessed.v1
RefundAllocated.v1
PayoutCreated.v1
PayoutProcessed.v1
PayoutFailed.v1
PayoutReconciled.v1
PromotionApplied.v1
PromotionInvalidated.v1
```

---

# 42. Transactional Outbox

Important domain writes and event creation happen in the same PostgreSQL transaction.

```mermaid
sequenceDiagram
    participant A as Application Service
    participant D as Domain Tables
    participant O as Outbox Table
    participant P as Publisher
    participant Q as SQS/EventBridge
    participant C as Consumer

    A->>D: Update business state
    A->>O: Insert domain event
    D-->>A: Commit
    O-->>A: Commit
    P->>O: Read unpublished events
    P->>Q: Publish event
    Q->>C: Deliver event
    C->>C: Process idempotently
    C->>O: Mark event published (or retry state)
```

Consumer deduplication should be based on `eventId` or a consumer-specific inbox table.

Provider webhooks are inbound events and must be deduplicated separately through provider-event storage rather than the outbox.

---
