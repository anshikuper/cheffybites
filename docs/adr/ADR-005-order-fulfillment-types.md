# ADR-005 --- Order Fulfillment Type Separation

## Status

Proposed

## Context

The Order fulfillment workflow must explicitly distinguish customer
pickup from delivery.

The previous state model allowed `PICKED_UP` to mean both customer
pickup and delivery-driver pickup, which made transitions ambiguous. It
also mixed Chef preparation states with Order fulfillment states.

ADR-013 establishes `ChefOrderGroup` as the Chef-level preparation
boundary. Therefore, the parent Order should coordinate aggregate
readiness and then own the customer/delivery fulfillment workflow.

## Decision

Every Order must have an immutable `fulfillment_type`:

``` text
PICKUP
DELIVERY
```

`fulfillment_type` is selected before checkout is finalized and becomes
immutable once the Order is created.

### Preparation Coordination

Chef preparation is tracked by `ChefOrderGroup` as defined by ADR-013:

``` text
PENDING_ACCEPTANCE
→ ACCEPTED
→ PREPARING
→ READY
```

The parent Order may enter `READY_FOR_FULFILLMENT` only when the Order
coordination rules determine that all required, non-cancelled
ChefOrderGroups are ready.

### Pickup Lane

``` text
PAID
→ PENDING_CHEF_ACCEPTANCE
→ ACCEPTED
→ PREPARING
→ READY_FOR_FULFILLMENT
→ PICKED_UP
→ COMPLETED
```

For `PICKUP`, `PICKED_UP` means the completed handoff to the customer or
the customer's authorized pickup party.

### Delivery Lane

``` text
PAID
→ PENDING_CHEF_ACCEPTANCE
→ ACCEPTED
→ PREPARING
→ READY_FOR_FULFILLMENT
→ DELIVERY_REQUESTED
→ DRIVER_ASSIGNED
→ DRIVER_PICKED_UP
→ OUT_FOR_DELIVERY
→ DELIVERED
→ COMPLETED
```

For `DELIVERY`, `DRIVER_PICKED_UP` explicitly means the delivery driver
has taken possession of the Order.

This avoids overloading `PICKED_UP` with two different meanings.

## Constraints

-   `fulfillment_type` is required for every Order.
-   `fulfillment_type` is immutable after Order creation.
-   `PICKED_UP` is valid only for `PICKUP`.
-   `DELIVERY_REQUESTED`, `DRIVER_ASSIGNED`, `DRIVER_PICKED_UP`,
    `OUT_FOR_DELIVERY`, and `DELIVERED` are valid only for `DELIVERY`.
-   `COMPLETED` is reached from `PICKED_UP` for pickup Orders.
-   `COMPLETED` is reached from `DELIVERED` for delivery Orders.
-   ChefOrderGroup readiness must be coordinated before the Order enters
    `READY_FOR_FULFILLMENT`.
-   Cancellation/refund transitions remain governed by the Order
    cancellation and financial rules and are not redefined by this ADR.

## Database Representation

``` sql
ALTER TABLE "order".orders
    ADD COLUMN fulfillment_type VARCHAR(20) NOT NULL;

ALTER TABLE "order".orders
    ADD CONSTRAINT ck_orders_fulfillment_type
    CHECK (fulfillment_type IN ('PICKUP', 'DELIVERY'));
```

Application-level state-transition validation must enforce
fulfillment-type-specific transitions.

## API Contract

Order creation and responses must expose:

``` json
{
  "fulfillmentType": "PICKUP"
}
```

or:

``` json
{
  "fulfillmentType": "DELIVERY"
}
```

Clients must not be allowed to change the value after Order creation.

## Events

Fulfillment events should use unambiguous names:

``` text
OrderReadyForFulfillment.v1
OrderPickedUp.v1
DeliveryRequested.v1
DriverAssigned.v1
DriverPickedUp.v1
OrderOutForDelivery.v1
OrderDelivered.v1
OrderCompleted.v1
```

Event contracts follow ADR-016.

## Consequences

### Positive

-   Pickup and delivery paths are explicit.
-   `PICKED_UP` no longer has two meanings.
-   Chef preparation remains owned by ChefOrderGroup.
-   Delivery-specific states are clearly separated.
-   Analytics and routing can rely on an immutable fulfillment type.

### Negative

-   Existing diagrams, APIs, tests, and event contracts must be updated.
-   Order coordination logic must aggregate ChefOrderGroup readiness.
-   A separate `DRIVER_PICKED_UP` state adds one explicit state to the
    delivery workflow.

## Alternatives Considered

### Shared `PICKED_UP` State

Rejected because customer pickup and driver pickup represent different
business meanings and lead to ambiguous transitions.

### `HANDED_OFF` Plus `PICKED_UP`

Rejected because it introduces redundant states unless a distinct
business event must occur between physical handoff and possession.

### Order Subclasses

Rejected because separate `PickupOrder` and `DeliveryOrder` types add
unnecessary persistence and domain complexity.

## Implementation Notes

1.  Add `fulfillment_type` to `"order".orders`.
2.  Update Order transition validation.
3.  Update `02-detailed-architecture.md` Order state diagrams.
4.  Update API contracts.
5.  Update event contracts.
6.  Add tests proving that pickup-only and delivery-only transitions
    cannot cross lanes.
7.  Coordinate readiness using ChefOrderGroup rules from ADR-013.

## Dependencies

-   ADR-013 --- ChefOrderGroup Aggregate + Financial Boundary
-   ADR-016 --- Event Versioning
