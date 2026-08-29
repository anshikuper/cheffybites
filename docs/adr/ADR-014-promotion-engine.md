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
12. Persist the resulting immutable PricingSnapshot where required by the Pricing workflow
```

### Promotion Snapshot

Every evaluated promotion, applied or rejected, records promotion/version, owner, scope, qualifying basis, eligible/excluded items, qualifying subtotal, discount, result/reason, conflict evidence, ChefOrderGroup where applicable, Order, and timestamp.

Original snapshots remain immutable. Refund recalculation creates new evidence and financial adjustments.

PromotionSnapshot and PromotionApplication are Promotion-owned evaluation evidence. The resulting PricingSnapshot, where captured by the pricing workflow, is Pricing-owned immutable commercial/calculation evidence. Payment, PaymentAllocation, Refund, Payout, LedgerTransaction, and LedgerEntry remain separate immutable Financial-domain facts; promotion evaluation does not create a separate snapshot aggregate in the Financial domain.

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
- A specific customer may successfully redeem a specific promo code at most once. This fixed per-customer rule is not configurable.
- A promo code may define optional `max_global_uses`; `NULL` means no configured global cap, and `max_global_uses = 1` defines a globally one-time code.
- No `max_uses_per_customer` policy is supported.
- Expired promotions are invalid at evaluation time.
- Items remaining in carts do not reserve expired promotions.

Automatic promotions may create PromotionApplication and PromotionSnapshot evidence, but they do not create promo-code redemption records merely because they applied automatically. Promotion calculation/application evidence and promo-code redemption are separate concepts.

### Promo-Code Redemption Lifecycle

Customer-entered codes use append-preserved redemption attempts with these states:

```text
RESERVED → REDEEMED
RESERVED → RELEASED
```

- `RESERVED` means the customer successfully claimed the code for an active checkout. It temporarily consumes per-customer and global redemption capacity.
- `REDEEMED` means checkout reached the authoritative successful payment/completion point. It permanently counts as used by that customer and is terminal for customer eligibility.
- `RELEASED` means the reservation definitively did not complete and no longer consumes redemption capacity. It is terminal historical evidence; a later attempt creates a new redemption row.

Checkout abandonment/expiry, explicit checkout cancellation, or final payment failure after the checkout is no longer active may release a reservation. A retryable provider payment-attempt failure does not release it while checkout remains active. Refunds never cause `REDEEMED → RELEASED`.

The current flow creates an Order in `PAYMENT_PENDING` before authoritative payment success, so every redemption row references `order_id` from `RESERVED` onward. A future flow that reserves a code before Order materialization requires a separate contract change; it is not inferred here. At most one `RESERVED` or `REDEEMED` customer-entered code redemption may exist for an Order.

### Promo-Code Redemption Model

```sql
CREATE TABLE promotion.promo_code_redemptions (
    id UUID PRIMARY KEY,
    promo_code_id UUID NOT NULL REFERENCES promotion.promo_codes(id),
    customer_id UUID NOT NULL REFERENCES customer.customer_profiles(id),
    order_id UUID NOT NULL REFERENCES "order".orders(id),
    status VARCHAR(20) NOT NULL,
    reserved_at TIMESTAMPTZ NOT NULL,
    redeemed_at TIMESTAMPTZ NULL,
    released_at TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (status IN ('RESERVED', 'REDEEMED', 'RELEASED'))
);

CREATE UNIQUE INDEX uq_promo_code_redemptions_customer_consuming
    ON promotion.promo_code_redemptions (promo_code_id, customer_id)
    WHERE status IN ('RESERVED', 'REDEEMED');

CREATE UNIQUE INDEX uq_promo_code_redemptions_order_consuming
    ON promotion.promo_code_redemptions (order_id)
    WHERE status IN ('RESERVED', 'REDEEMED');
```

The customer/code partial unique index enforces one active or successful redemption per customer per promo code while allowing a new attempt after a historical `RELEASED` row. A general `UNIQUE (promo_code_id)` constraint is incorrect because it would make every code globally single-use; global cardinality is controlled by optional `max_global_uses`.

### Redemption Reservation Concurrency

A plain count of consuming redemptions followed by comparison and insert is unsafe under PostgreSQL `READ COMMITTED`: concurrent customers can each observe the same final available global use and both insert. The stable serialization resource is the relevant `promotion.promo_codes` row.

In one local PostgreSQL reservation transaction:

1. Lock the promo-code row using row-level locking equivalent to `SELECT ... FOR UPDATE`.
2. After acquiring the lock, validate code lifecycle/status, validity/expiry, and customer eligibility.
3. Validate that no `RESERVED` or `REDEEMED` redemption already exists for the customer/code pair.
4. Count redemptions for the promo code whose status is `RESERVED` or `REDEEMED`.
5. If `max_global_uses` is not `NULL` and the consuming count is greater than or equal to the limit, reject the reservation.
6. Otherwise insert the `RESERVED` redemption associated with the Order.

The global count/check must occur after lock acquisition; a pre-lock count is not authoritative. The partial unique indexes remain database backstops for customer/code and Order cardinality. Advisory and distributed locks are not the default or required design.

Reservation is a local database transaction and does not create a distributed transaction with a payment provider. After authoritative successful payment/checkout completion, the appropriate idempotent local workflow transitions `RESERVED → REDEEMED`. If checkout definitively expires, is cancelled, or finally fails before redemption, it transitions `RESERVED → RELEASED` idempotently. A provider callback and database update are not one ACID transaction.

### Promotion Application and Redemption References

PromotionApplication and PromotionSnapshot preserve pricing-calculation evidence; PromoCodeRedemption preserves customer/code usage and cardinality. For PromotionApplication:

- `promotion_id` is required.
- `order_id` is required for an Order pricing application.
- `chef_order_group_id` is nullable because `ORDER` and `DELIVERY` scopes may have no ChefOrderGroup.
- `order_item_id` is nullable because non-`ITEM` scopes do not target one item.
- `promo_code_id` is nullable because automatic promotions use no entered code.
- `promo_code_redemption_id` is nullable and identifies the redemption when the application derives from a customer-entered code.

Order-, Delivery-, and Platform-level applications must not be forced into a ChefOrderGroup. Automatically applied promotion applications do not require a promo-code or redemption reference.

## Partial Refund Recalculation

1. Identify refunded items.
2. Identify affected ChefOrderGroup and scopes.
3. Load original promotion evidence.
4. Recalculate according to approved rules.
5. Determine refund/adjustment.
6. Create new immutable financial records.
7. Preserve all original promotion and financial evidence.

A full or partial refund does not restore promo-code eligibility, release a `REDEEMED` redemption, or overwrite the original application/redemption. Refund recalculation and adjustment create new immutable promotion and financial evidence while the successful redemption remains historical usage.

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
