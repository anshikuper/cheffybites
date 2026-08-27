# ADR-014 — Promotion Engine

## Status

Proposed

## Context

Cheffy Bites needs Chef, Platform, and Entrepreneur promotions with deterministic conflict resolution, Chef isolation, immutable historical evidence, and partial-refund recalculation.

## Decision

### Promotion Ownership

```text
CHEF
PLATFORM
ENTREPRENEUR
```

`PLATFORM` is an ownership domain, not itself a food-order calculation scope.

### Promotion Calculation Scopes

For food Orders:

```text
ITEM
CHEF_ORDER_GROUP
DELIVERY
ORDER
```

Entrepreneur booking/equipment promotions are evaluated in their own domain unless a future cross-domain rule is approved.

### Qualifying Basis

```text
ALL_ELIGIBLE_ITEMS
NON_DISCOUNTED_ELIGIBLE_ITEMS
SPECIFIC_TARGET_ITEMS
GROUP_SUBTOTAL
DELIVERY_FEE
```

`NON_DISCOUNTED_ELIGIBLE_ITEMS` excludes items already receiving the relevant item-level discounts.

### Chef Independence

Chef promotions evaluate only within the same ChefOrderGroup. Chef A items can never qualify Chef B promotions, and vice versa. Promotions for separate ChefOrderGroups may coexist.

### Deterministic Conflict Resolution

1. Determine eligibility.
2. Partition by target and calculation scope.
3. Apply compatibility rules.
4. Apply exclusivity rules.
5. Higher priority wins conflicts.
6. If equal, greater customer savings wins.
7. If still equal, lexicographically smaller immutable promotion UUID wins.

### Promotion Evaluation Sequence

```text
1. Partition Order into ChefOrderGroups
2. Evaluate ITEM promotions
3. Resolve ITEM conflicts
4. Mark resulting discounts
5. Calculate configured qualifying basis
6. Evaluate CHEF_ORDER_GROUP promotions
7. Resolve conflicts
8. Evaluate DELIVERY promotions
9. Evaluate ORDER promotions
10. Persist immutable PromotionSnapshots for every evaluated promotion
11. Produce Pricing Result
12. Persist corresponding Financial Snapshot
```

### Promotion Snapshot

Every evaluated promotion, applied or rejected, records promotion/version, owner, scope, qualifying basis, eligible/excluded items, qualifying subtotal, discount, result/reason, conflict evidence, ChefOrderGroup where applicable, Order, and timestamp.

Original snapshots remain immutable. Refund recalculation creates new evidence and financial adjustments.

### Promotion Model

```sql
CREATE TABLE promotion.promotions (
    id UUID PRIMARY KEY,
    owner_type VARCHAR(20) NOT NULL,
    owner_id UUID NOT NULL,
    promotion_scope VARCHAR(30) NOT NULL,
    promotion_type VARCHAR(30) NOT NULL,
    name VARCHAR(255) NOT NULL,
    valid_from TIMESTAMPTZ NOT NULL,
    valid_to TIMESTAMPTZ NULL,
    priority INT NOT NULL DEFAULT 0,
    qualifying_basis VARCHAR(50) NULL,
    compatibility_group VARCHAR(100) NULL,
    exclusivity_group VARCHAR(100) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    conditions JSONB NOT NULL DEFAULT '{}',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (valid_to IS NULL OR valid_to > valid_from)
);
```

Promotion targeting and uniqueness follow accepted ADR-006.

## Promo Code Rules

- At most one customer-entered promo code may be applied per checkout.
- Automatically applied promotions may coexist when compatibility/exclusivity rules permit.
- Single-use is an explicit promotion policy backed by redemption records.
- Expired promotions are invalid at evaluation time.
- Items remaining in carts do not reserve expired promotions.

## Partial Refund Recalculation

1. Identify refunded items.
2. Identify affected ChefOrderGroup and scopes.
3. Load original promotion evidence.
4. Recalculate according to approved rules.
5. Determine refund/adjustment.
6. Create new immutable financial records.
7. Preserve all original promotion and financial evidence.

## Consequences

### Positive

- Deterministic resolution.
- Strict Chef promotion isolation.
- Supports multiple promotion scopes.
- Avoids blanket `stackable` logic.
- Strong historical evidence.

### Negative

- More complex pricing logic and tests.
- More snapshot storage.

## Dependencies

ADR-006, ADR-013, ADR-015, ADR-016.
