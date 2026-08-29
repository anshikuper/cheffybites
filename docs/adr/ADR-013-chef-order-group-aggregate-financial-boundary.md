# ADR-013 — ChefOrderGroup Aggregate + Financial Boundary

## Status

Proposed

## Context

One customer Order belongs to exactly one physical Kitchen but may contain items from multiple Chefs operating at that Kitchen. Each Chef needs independent acceptance, preparation visibility, promotion evaluation, revenue, refund, payout, reporting, and analytics traceability.

## Decision

`ChefOrderGroup` is a first-class operational and financial boundary within an Order.

### Core Invariants

- An Order references exactly one Kitchen.
- A ChefOrderGroup belongs to exactly one Order and one Chef Business.
- At most one ChefOrderGroup exists for `(order_id, chef_business_id)`.
- Every OrderItem belongs to exactly one ChefOrderGroup.
- A ChefOrderGroup must belong to a Chef Business authorized for the Order's Kitchen.
- Food allocations, refunds, payouts, and ledger entries reference the relevant ChefOrderGroup where applicable.

### Data Model

```sql
CREATE TABLE "order".chef_order_groups (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES "order".orders(id),
    chef_business_id UUID NOT NULL REFERENCES chef.chef_businesses(id),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_ACCEPTANCE',
    subtotal_minor BIGINT NOT NULL CHECK (subtotal_minor >= 0),
    discount_minor BIGINT NOT NULL CHECK (discount_minor >= 0),
    net_minor BIGINT NOT NULL CHECK (net_minor >= 0),
    currency_code CHAR(3) NOT NULL,
    version INT NOT NULL DEFAULT 0,
    latest_pricing_snapshot_id UUID NULL,
    latest_promotion_snapshot_id UUID NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_chef_order_group UNIQUE(order_id, chef_business_id)
);
```

`latest_pricing_snapshot_id` and `latest_promotion_snapshot_id` are convenience references only and are never the historical source of truth. PricingSnapshot remains Pricing-owned immutable commercial/calculation evidence, and PromotionSnapshot remains Promotion-owned evaluation evidence. ChefOrderGroup may reference that evidence but does not own either aggregate or any Financial-domain aggregate.

### Order Items

```sql
CREATE TABLE "order".order_items (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES "order".orders(id),
    chef_order_group_id UUID NOT NULL REFERENCES "order".chef_order_groups(id),
    food_listing_id UUID NOT NULL REFERENCES food.food_listings(id),
    product_name_snapshot VARCHAR(255) NOT NULL,
    unit_price_minor BIGINT NOT NULL CHECK (unit_price_minor >= 0),
    quantity INT NOT NULL CHECK (quantity > 0),
    gross_minor BIGINT NOT NULL CHECK (gross_minor >= 0),
    discount_minor BIGINT NOT NULL CHECK (discount_minor >= 0),
    net_minor BIGINT NOT NULL CHECK (net_minor >= 0),
    tax_minor BIGINT NOT NULL CHECK (tax_minor >= 0),
    currency_code CHAR(3) NOT NULL,
    item_snapshot JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

Cross-table invariants must be enforced by transactional application logic or an explicitly approved database mechanism. PostgreSQL `CHECK` constraints cannot enforce cross-row `EXISTS` queries.

## Operational State Model

```text
PENDING_ACCEPTANCE → ACCEPTED
PENDING_ACCEPTANCE → REJECTED
ACCEPTED → PREPARING
ACCEPTED → CANCELLED
PREPARING → READY
PREPARING → CANCELLED
```

ChefOrderGroup owns Chef preparation responsibility. Pickup/delivery fulfillment and final completion remain parent Order responsibilities under ADR-005.

## Financial Boundary

```text
financial.payment_allocations → chef_order_group_id
financial.refund_lines        → chef_order_group_id
financial.payout_lines        → chef_order_group_id
financial.ledger_entries      → chef_order_group_id
```

The Financial domain owns payment, refund, payout, and ledger aggregates. ChefOrderGroup is the business allocation reference.

## Partial Refund Rule

Only the directly affected ChefOrderGroup food allocation is recalculated. Shared Order-level effects follow approved tax, promotion, delivery, and fee rules. Other ChefOrderGroups remain independently traceable.

## Events

```text
ChefOrderGroupCreated.v1
ChefOrderGroupAccepted.v1
ChefOrderGroupRejected.v1
ChefOrderGroupPreparing.v1
ChefOrderGroupReady.v1
ChefOrderGroupCancelled.v1
```

Events follow ADR-016.

## Consequences

### Positive

- Clear Chef operational boundary.
- Seller-level financial traceability.
- Supports multi-Chef preparation without breaking one-Kitchen-per-Order.

### Negative

- Parent/child state coordination is required.
- Cross-table invariants need explicit enforcement.
- Additional aggregate complexity.

## Implementation Notes

- Implement optimistic concurrency using the aggregate version.
- Enforce Chef/Kitchen/Order consistency transactionally.
- Add indexes for `order_id`, `chef_business_id`, and `(order_id, chef_business_id)`.
- Ensure financial records use `chef_order_group_id` where required.
- Add tests for multi-Chef Orders, partial rejection, cancellation, readiness aggregation, partial refunds, and financial traceability.

## Dependencies

ADR-005, ADR-012, ADR-014, ADR-015, ADR-016.
