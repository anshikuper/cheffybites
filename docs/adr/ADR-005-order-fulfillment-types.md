# ADR-005 — Order Fulfillment Type Separation

## Status

Proposed

## Context

The original Order state machine in `02-detailed-architecture.md` §18.1 contained a transition `PICKED_UP → COMPLETED` that was duplicated from the delivery path. This created confusion about the state machine and made it unclear which fulfillment types were supported.

The problem:

```text
READY_FOR_FULFILLMENT → PICKED_UP
DRIVER_ASSIGNED → PICKED_UP
PICKED_UP → OUT_FOR_DELIVERY
PICKED_UP → COMPLETED
```

- `PICKED_UP` can be reached from both `READY_FOR_FULFILLMENT` (pickup) and `DRIVER_ASSIGNED` (delivery with handoff)
- `PICKED_UP → COMPLETED` is the pickup completion path
- `PICKED_UP → OUT_FOR_DELIVERY` is the delivery continuation path

This is valid but not explicit. Different stakeholders need to understand the model without reading the state machine closely.

## Decision

We will add an explicit `FULFILLMENT_TYPE` field to the Order aggregate and split the state machine into two parallel lanes:

### Order Fulfillment Types

```text
FULFILLMENT_TYPE: PICKUP | DELIVERY
```

### Pickup Lane

```text
PENDING_ACCEPTANCE
  → ACCEPTED
  → PREPARING
  → READY
  → HANDED_OFF     (set when customer arrives to pick up)
  → PICKED_UP
  → COMPLETED
```

### Delivery Lane

```text
PENDING_ACCEPTANCE
  → ACCEPTED
  → PREPARING
  → READY
  → DELIVERY_REQUESTED
  → DRIVER_ASSIGNED
  → PICKED_UP
  → OUT_FOR_DELIVERY
  → DELIVERED
  → COMPLETED
```

### Constraints

- `FULFILLMENT_TYPE` must be set at order creation time and cannot be changed
- `PICKED_UP` state is valid for both pickup and delivery orders
- `HANDED_OFF` is set when the kitchen physically hands off the order (pickup customer or delivery driver)
- `OUT_FOR_DELIVERY` is only valid for delivery orders
- `COMPLETED` can be reached from `PICKED_UP` (pickup) or `DELIVERED` (delivery)

## Consequences

### Positive

- State machine is self-documenting and explicit
- Fulfillment type can be used for analytics and routing
- Clear separation of pickup and delivery workflows
- Reduces confusion for new developers

### Negative

- Additional field on Order aggregate
- Need to validate fulfillment type transitions
- May need to update existing tests

## Alternatives Considered

1. **Implicit labeling** — Add footnotes to existing diagram explaining paths
   - Rejected: Not explicit enough, easy to miss

2. **Single state machine with guard conditions** — Use `FULFILLMENT_TYPE` as a guard condition on transitions
   - Considered: More complex, harder to reason about
   - Rejected: Parallel lanes are clearer

3. **Subclassing Order** — Create `PickupOrder` and `DeliveryOrder` subclasses
   - Rejected: Over-engineering for MVP, single table inheritance adds complexity

## Implementation Notes

- Add `fulfillment_type` column to `ORDERS` table: `VARCHAR(20) NOT NULL`
- Add database check constraint: `CHECK (fulfillment_type IN ('PICKUP', 'DELIVERY'))`
- Add state transition validation in application service
- Update API contracts to include `fulfillmentType` in order responses
- Update `02-detailed-architecture.md` §18.1 with explicit lane diagram
