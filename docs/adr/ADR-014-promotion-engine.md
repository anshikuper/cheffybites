# ADR-014 — Promotion Engine

## Status

Proposed

## Context

The Cheffy Bites platform requires a promotion engine that supports:

- Chef promotions scoped to a Chef's own ChefOrderGroup
- Platform promotions that may apply at the order level
- Entrepreneur promotions for kitchen bookings and equipment rentals
- Multiple promotion types (percentage, fixed amount, BOGO, quantity threshold, etc.)
- Deterministic conflict resolution when multiple promotions are eligible
- Immutable promotion snapshots for historical audit and refund recalculation

The previous architecture used a blanket `stackable` flag and a simple "Chef promotions cannot stack with each other" rule. This approach is insufficient because:

1. It does not account for promotions targeting different items within the same ChefOrderGroup.
2. It does not support promotions at different scopes (ITEM, CHEF_ORDER_GROUP, DELIVERY, ORDER, PLATFORM).
3. It does not provide deterministic resolution when multiple promotions conflict.
4. It does not preserve enough information for refund recalculation.

## Decision

### Promotion Scopes

Promotions are evaluated at the following scopes:

```text
ITEM
CHEF_ORDER_GROUP
DELIVERY
ORDER
PLATFORM
```

- **ITEM**: Applies to individual eligible items within a ChefOrderGroup.
- **CHEF_ORDER_GROUP**: Applies to a Chef's group subtotal or qualifying basis within a ChefOrderGroup.
- **DELIVERY**: Applies to the delivery fee for the Kitchen Order.
- **ORDER**: Applies to the overall order total (platform-level promotions).
- **PLATFORM**: Platform-wide promotions that may target customers, segments, or locations.

### Qualifying Basis

Group-level promotions must declare an explicit qualifying basis:

```text
ALL_ELIGIBLE_ITEMS
NON_DISCOUNTED_ELIGIBLE_ITEMS
SPECIFIC_TARGET_ITEMS
GROUP_SUBTOTAL
DELIVERY_FEE
```

- **ALL_ELIGIBLE_ITEMS**: All eligible items in the scope are counted toward the qualifying threshold.
- **NON_DISCOUNTED_ELIGIBLE_ITEMS**: Only items not already discounted by an item-level promotion are counted. A group threshold promotion using this basis must exclude items already discounted by item-level promotions.
- **SPECIFIC_TARGET_ITEMS**: Only explicitly targeted items are counted.
- **GROUP_SUBTOTAL**: The group subtotal (after item-level discounts) is the qualifying amount.
- **DELIVERY_FEE**: The delivery fee is the qualifying amount.

### Compatibility and Conflict Resolution

There is no blanket global `stackable` flag. Promotion conflict resolution is deterministic and uses the following ordered criteria:

1. **Scope**: Promotions at different scopes may coexist (e.g., an ITEM promotion and a CHEF_ORDER_GROUP promotion).
2. **Compatibility**: Promotions may declare a `compatibility_group`. Promotions in the same compatibility group may coexist. Promotions in conflicting compatibility groups are mutually exclusive.
3. **Exclusivity**: Promotions may declare an `exclusivity_group`. Only one promotion from an exclusivity group may be applied per scope/target.
4. **Priority**: Higher priority value wins when promotions conflict.
5. **Savings**: When priority is equal, the promotion that produces greater customer savings wins.
6. **Deterministic Tie-Breaker**: When savings are equal, the promotion with the earlier creation timestamp (or lexicographically smaller ID) wins, ensuring deterministic behavior.

### Chef Promotion Independence

Chef A and Chef B are independent promotion domains.

- Chef A promotions evaluate only against Chef A's ChefOrderGroup items.
- Chef B promotions evaluate only against Chef B's ChefOrderGroup items.
- Chef A items must never be used to qualify Chef B promotions, and vice versa.
- Chef A and Chef B promotions may coexist in the same Order because they operate on independent scopes.

### Promotion Evaluation Sequence

```text
1. Partition Order into ChefOrderGroups
2. For each ChefOrderGroup:
   a. Evaluate ITEM-level promotions per eligible item
   b. Mark discounted items
   c. Calculate qualifying subtotal by configured basis
   d. Evaluate CHEF_ORDER_GROUP promotions
   e. Resolve conflicts deterministically
3. Evaluate DELIVERY promotions
4. Evaluate ORDER/PLATFORM promotions
5. Persist immutable PromotionSnapshot per applied/rejected promotion
6. Return Pricing Result
```

### Promotion Snapshot

At checkout, the promotion engine must persist an immutable `PromotionSnapshot` for each evaluated promotion (applied or rejected). The snapshot must preserve:

```text
promotion_id
promotion_version
scope
qualifying_basis
eligible_item_ids
excluded_item_ids
qualifying_subtotal_minor
discount_minor
applied_status (APPLIED | REJECTED)
rejection_reason
chef_order_group_id (where applicable)
order_id
created_at
```

Historical orders must not depend on the current live promotion configuration. The snapshot is the authoritative record for refund recalculation.

### Promotion Model

```sql
CREATE TABLE promotion.promotions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_type VARCHAR(20) NOT NULL, -- CHEF, PLATFORM, ENTREPRENEUR
    owner_id UUID NOT NULL,
    promotion_scope VARCHAR(30) NOT NULL, -- ITEM, CHEF_ORDER_GROUP, DELIVERY, ORDER, PLATFORM
    promotion_type VARCHAR(30) NOT NULL, -- PERCENTAGE, FIXED_AMOUNT, BOGO, etc.
    name VARCHAR(255) NOT NULL,
    valid_from TIMESTAMPTZ NOT NULL,
    valid_to TIMESTAMPTZ NULL,
    priority INT NOT NULL DEFAULT 0,
    qualifying_basis VARCHAR(50) NULL, -- required for group-level promotions
    compatibility_group VARCHAR(100) NULL,
    exclusivity_group VARCHAR(100) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    conditions JSONB NOT NULL DEFAULT '{}',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

### Promo Code Rules

- A customer may use only one promo code per transaction.
- A promo code is single-use.
- Platform promotions may be restricted to specific users/segments.
- Promotions expire according to their configured validity.
- An expired promotion is invalid at checkout even if an item was previously added to the cart.
- A promotion becoming invalid after an item is removed/refunded must trigger the configured recalculation behavior.

### Partial Refund Recalculation

When an Order is partially refunded:

1. Identify the refunded OrderItems.
2. Remove their financial contribution from the remaining Order.
3. Recalculate the affected ChefOrderGroup.
4. Re-evaluate promotion validity according to the approved promotion rules.
5. Calculate the refund/adjustment.
6. Record the resulting financial adjustment.
7. Never overwrite the original finalized financial transaction.

The original `PromotionSnapshot` must remain available as evidence for the refund recalculation.

## Consequences

### Positive
- Deterministic promotion resolution eliminates ambiguity.
- Chef promotion isolation is enforced at the evaluation level.
- Promotion snapshots provide full audit trail for refunds.
- Multiple promotion types and scopes are supported.
- The engine is extensible to future promotion types.

### Negative
- Increased complexity in promotion evaluation logic.
- More data is stored per checkout (snapshots).
- Conflict resolution requires careful testing.

## Implementation Notes

1. **Promotion Engine Service**: Implement a dedicated `PromotionEvaluationService` that partitions the order by ChefOrderGroup and evaluates promotions per scope.
2. **Snapshot Persistence**: Persist `PromotionSnapshot` records in the same transaction as the order/pricing changes.
3. **Refund Recalculation**: Implement a `PromotionRecalculationService` that loads the original snapshot and recalculates eligibility after partial refunds.
4. **Testing**: Unit tests must cover:
   - Chef A promotion does not use Chef B items.
   - Item-level and group-level promotions coexist when scopes differ.
   - `NON_DISCOUNTED_ELIGIBLE_ITEMS` excludes items already discounted by item-level promotions.
   - Expired promotions are rejected at checkout.
   - Single-use promo codes cannot be reused.
   - Partial refunds trigger correct recalculation.
   - Deterministic tie-breaking when savings are equal.

## Alternatives Considered

1. **Blanket `stackable` flag** — Rejected: Cannot express the required compatibility, exclusivity, and priority rules.
2. **Priority-only resolution** — Rejected: Priority alone does not guarantee the best customer outcome and can produce non-deterministic results when priorities are equal.
3. **Separate Chef promotion tables** — Rejected: A unified promotion model with scope and ownership is more maintainable and extensible.

## References

- Master Spec §20–25 (Promotions Engine, Stacking Rules, Scope, Evaluation, Validation, Partial Refund)
- ADR-006 (Promotion Targeting Model)
- ADR-013 (ChefOrderGroup Aggregate + Financial Boundary)
