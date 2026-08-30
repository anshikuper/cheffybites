# ADR-013 — ChefOrderGroup Aggregate + Financial Boundary

## Status

Proposed

## Amendment Scope

This proposal preserves Chef-specific traceability for subscription-origin food Orders, clarifies that ChefOrderGroup identity follows the actual Chef performer rather than a shared Organization/commercial-provider/payee identity, and removes Dietitian food recommendation/referral attribution from the current decision. It does not generalize ChefOrderGroup into a provider or Organization grouping or transfer Organization, subscription, Professional/Dietitian, Promotion/Pricing, or Financial ownership into ChefOrderGroup.

## Context

One customer Order belongs to exactly one physical Kitchen but may contain items from multiple actual Chef performers operating at that Kitchen. Each Chef needs independent acceptance, preparation visibility, authorization, promotion evaluation/reference, quality/refund traceability, reporting, analytics, and historical identity.

Approved product requirements also permit a confirmed MealFulfillmentOccurrence to generate or link to a normal concrete food Order. An Organization may employ or engage multiple Chef performers while acting as the commercial provider and settlement beneficiary for their food. Subscription-origin and Organization-operated contexts need immutable traceability into the applicable Order, actual Chef performers, ChefOrderGroups, OrderItems, commercial context, and Financial facts without changing aggregate ownership.

## Decision

`ChefOrderGroup` is a first-class Chef operational and financial-reference boundary within one concrete food Order. It represents exactly one actual Chef performer's operational participation in that Order; it is not a generic Organization, commercial-provider, seller, professional, settlement-beneficiary, or marketplace-party grouping.

### Core Invariants

- An Order references exactly one Kitchen.
- A ChefOrderGroup belongs to exactly one Order and identifies exactly one actual Chef performer/operational Chef identity participating in that Order.
- At most one ChefOrderGroup exists for one `(Order + actual Chef performer/operational identity)` combination.
- Every OrderItem belongs to exactly one ChefOrderGroup.
- The identified Chef must be authorized in the applicable transaction-time business/Organization context to perform at the Order's Kitchen.
- Food allocations, refunds, payouts, and ledger entries reference the relevant ChefOrderGroup where applicable.
- The parent Order is authoritative for the physical Kitchen. ChefOrderGroup does not derive or independently own another Kitchen identity.
- All Chef items in one concrete Order, including an Order originating from Meal Subscription fulfillment, must be produced at that same physical Kitchen.
- Shared employer, commercial-provider, settlement-beneficiary, or connected-account identity must not merge distinct Chef performers into one ChefOrderGroup.

Conceptually:

```text
Concrete Food Order → exactly one physical Kitchen
    ├── ChefOrderGroup A → Chef A OrderItems
    ├── ChefOrderGroup B → Chef B OrderItems
    └── ...
```

ChefOrderGroup remains the authoritative boundary for one Chef's preparation lifecycle, authorization, OrderItem grouping, Order history, applicable Chef Promotion calculation/reference scope, economic/source traceability, refund and payout traceability, and reporting/analytics. The introduction of Meal Subscription does not weaken those responsibilities after a concrete food Order has materialized.

### Data Model

```text
ChefOrderGroup
    ├── exactly one concrete Order
    ├── exactly one durable actual-Chef performer/operational identity
    ├── preparation status
    ├── Chef-specific OrderItems
    ├── monetary summary/reference values where applicable
    ├── immutable historical source/evidence references where applicable
    └── optimistic-concurrency version
```

The former illustrative uniqueness wording `(order_id, chef_business_id)` is not sufficient if `chef_business_id` can identify a common employer, commercial provider, or settlement Organization. If a future canonical field named `chef_business_id` instead identifies a durable Chef-specific operational/storefront actor that cannot be shared by distinct Chef performers, the ERD may retain that semantic only after making the distinction explicit. This ADR does not choose among `chef_id`, `chef_profile_id`, `chef_professional_profile_id`, `chef_membership_id`, `chef_business_actor_id`, or another physical reference. The canonical ERD must select a relational key and uniqueness constraint that enforce one group per Order and actual Chef performer without binding identity to a current employer.

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

`PICKED_UP`, `DRIVER_ASSIGNED`, `DRIVER_PICKED_UP`, `OUT_FOR_DELIVERY`, `DELIVERED`, and `COMPLETED` are not ChefOrderGroup preparation states. Refund status is also not a ChefOrderGroup state; it remains part of a Financial workflow.

## Meal Subscription Boundary

The following remain distinct concepts outside ChefOrderGroup ownership:

```text
ChefMealPlan
MealSubscriptionOffer
MealSubscription
MealEntitlementCycle
MealSubscriptionBillingCycle
MealFulfillmentOccurrence
```

```text
MealSubscription != Order
MealFulfillmentOccurrence != ChefOrderGroup
```

A MealFulfillmentOccurrence in `REQUESTED`, `PENDING_KITCHEN_CAPACITY`, `DECLINED`, or `EXPIRED` does not have a ChefOrderGroup merely because it might later lead to food fulfillment. ChefOrderGroup is created or used only when qualifying fulfillment has materialized into the normal concrete food Order model:

```text
MealSubscription
    ↓
MealFulfillmentOccurrence
    ↓ trace/link after Order materialization
Concrete Food Order
    ↓
ChefOrderGroup(s)
    ↓
OrderItem(s)
```

The originating commercial context must remain traceable so the system can determine which MealSubscription, MealFulfillmentOccurrence, applicable ChefMealPlan/version or selected FoodListing context, and entitlement/funded occurrence the concrete Order satisfied. This ADR does not require ChefOrderGroup to own every subscription reference and does not select the foreign-key or table layout. The Meal Subscription domain retains subscription, entitlement, Kitchen-capacity readiness, and occurrence lifecycle ownership; the later subscription architecture and canonical ERD must define the exact relational representation.

A MealSubscription or ChefMealPlan may use different eligible Kitchens across separate future dates. Nevertheless, each materialized concrete food Order references exactly one physical Kitchen and cannot combine items produced at different Kitchens. Obligations requiring different Kitchens must use separate concrete Order/fulfillment boundaries under the later subscription architecture.

Organization-operated Meal Subscription does not replace actual Chef identity with the Subscription's commercial-provider Organization. For example, ABC Food Group may be the commercial provider while Week 1 materializes an Order with ChefOrderGroup Ravi, Week 2 an Order with ChefOrderGroup Maria, and Week 3 an Order with ChefOrderGroup Ravi. Each concrete Order groups its actual performers and retains its applicable transaction-time commercial context. The exact subscription/provider foreign-key layout remains outside this ADR.

## Chef Performer and Organization Commercial-Provider Boundary

ChefOrderGroup primarily answers:

> Which actual Chef is operationally responsible for these OrderItems inside this concrete Order?

Commercial-provider identity answers:

> Which approved marketplace business commercially supplied or sold this food?

These concepts may align for an independent Chef business but do not have to. ChefOrderGroup is not the authoritative commercial-provider aggregate; Organization/business architecture owns commercial-provider identity, Organization membership, Kitchen operating authority, and the Chef engagement/authorization relationship.

```text
Order O1 → Kitchen K1

Commercial provider: ABC Food Group
Settlement beneficiary: ABC Food Group

Chef Ravi
  └── ChefOrderGroup Ravi
        └── Ravi's OrderItems

Chef Maria
  └── ChefOrderGroup Maria
        └── Maria's OrderItems

ChefOrderGroup Ravi != ChefOrderGroup Maria
```

Common `commercial_provider_organization_id`, employer, payee, or connected account must not merge those groups. The same architecture supports an independent Chef: Ravi remains the ChefOrderGroup performer while Ravi Foods Organization may be both commercial provider and settlement beneficiary. It also supports Cheffy Operations employing Chef A and Chef B under separate ChefOrderGroups with Cheffy Operations Organization as provider/payee. No Cheffy-specific ChefOrderGroup behavior is permitted, and a later Cheffy exit from direct supply does not change this decision.

```text
CHEFORDERGROUP != COMMERCIAL PROVIDER
CHEFORDERGROUP != SETTLEMENT BENEFICIARY
```

A ChefOrderGroup is important economic/source evidence without necessarily being the external recipient. One ChefOrderGroup does not require one Payout, one connected account, or one settlement beneficiary. Multiple ChefOrderGroups may contribute to obligations payable to one Organization under future ADR-020 and settlement policy.

The commercial provider Organization is also not a substitute for physical Kitchen identity. One Organization may operate Kitchen K1, K2, and K3, but concrete Order O1 still resolves to exactly one of them. Same Organization does not mean same physical Kitchen, and ChefOrderGroup must not contradict the parent Order's Kitchen.

Organization membership, employment/contractor status, hiring, salary, employee commission, payroll, and membership lifecycle remain outside ADR-013. This ADR requires durable performer identity and transaction-time authorization context but does not permanently bind a Chef to a current employer. A Chef may operate independently or through different authorized Organizations over time subject to owning business and legal rules; exact membership and effective-dating design remains for the owning architecture and canonical ERD.

Organization operation must not erase individual Chef accountability. The actual Chef identity remains available for verified Chef service reviews, preparation accountability, performance history, cancellation/readiness metrics, quality/refund traceability, and moderation or suspension where applicable. Any future Organization reputation is a separate product decision and does not replace Chef-specific reputation or history.

## Dietitian Guidance and Privacy Boundary

A `DietitianProfessionalProfile`, `DietitianMealPlan`, and professional recommendation remain owned by the Professional/Dietitian domain. `DietitianMealPlan` is private professional guidance for the Customer; it does not become Order-owned or ChefOrderGroup-owned.

The Customer may use selected, authorized requirements from professional guidance to search, filter, choose a FoodListing or ChefMealPlan, or create a FoodRequest. That use does not create Dietitian commercial attribution on a resulting Order or ChefOrderGroup. ChefOrderGroup does not require a reference to DietitianProfessionalProfile, DietitianMealPlan, recommendation, referral, or commission merely because Customer discovery originated from professional guidance.

```text
Private Dietitian / Customer professional information
    ↓ Customer-authorized extraction
Marketplace requirements / FoodRequest
    ↓
Commercial offering / concrete Order
    ↓
ChefOrderGroup(s) for actual Chef performers
```

If an Order originates from a FoodRequest, the owning FoodRequest/Order architecture may preserve the appropriate commercial source traceability. It must not automatically expose the private DietitianMealPlan or professional record to a Chef. The Professional, FoodRequest, and privacy architecture owns which Customer-authorized requirements may be shared; ADR-013 does not absorb that design.

Dietitian-Chef food commercial agreements, recommendation/referral financial attribution, food-sale or Meal Subscription commission, commission deduction from Chef proceeds, per-ChefOrderGroup Dietitian attribution, multiple-Dietitian commission conflict, first-touch/last-touch commission, attribution duration, and refund reversal of Dietitian food commission are not current product scope. Reconsidering any such model requires a new explicit product, professional-regulatory, financial, privacy, and architecture decision.

## Promotion and Pricing Boundary

Chef Promotion calculation may continue to use ChefOrderGroup as a monetary/economic scope for food Orders under ADR-014. Commercial-provider Organization identity does not automatically replace ChefOrderGroup Promotion scope: Chef Ravi and Chef Maria may have distinct Chef-specific Promotion eligibility inside one same-Kitchen Order even when ABC Food Group commercially supplies the Order. Organization-owned Promotion behavior, if later approved, remains governed by ADR-006/ADR-014 and is not invented here. A Platform subsidy is not a ChefOrderGroup state. Promotion rules, applications, and snapshots remain Promotion-owned; PricingSnapshot and commercial calculation evidence remain Pricing-owned.

## Financial Boundary

```text
financial.payment_allocations → chef_order_group_id
financial.refund_lines        → chef_order_group_id
financial.payout_lines        → chef_order_group_id
financial.ledger_entries      → chef_order_group_id
```

The Financial domain owns `Payment`, `PaymentAttempt`, `PaymentAllocation`, `Refund`, `RefundLine`, `Payout`, `PayoutLine`, `LedgerTransaction`, and `LedgerEntry`. ChefOrderGroup is the Chef-specific operational and economic/source-reference boundary. A Financial fact may reference Order, ChefOrderGroup, OrderItem, commercial provider, and subscription-origin evidence where appropriate. Reference does not transfer aggregate ownership, identify the external recipient by itself, or move a Financial lifecycle state into ChefOrderGroup.

This ADR does not finalize a generic polymorphic financial source, arbitrary `source_type + UUID`, exact generalized foreign keys, or source cardinalities. Future ADR-020 and the canonical ERD must define the commercial-obligation and Financial relationships with appropriate relational integrity.

Commercial-provider food value, Platform fees, Platform subsidies, taxes, delivery amounts, and other approved obligations are Financial/economic facts where applicable; they are not additional ChefOrderGroups. Payment collection, earning recognition, payout eligibility, and payout completion remain distinct:

```text
PAYMENT
!= EARNING RECOGNITION
!= PAYOUT ELIGIBILITY
!= PAYOUT COMPLETION
```

Payout remains Financial-owned. ChefOrderGroup is a source/reference boundary, not the Payout aggregate or settlement beneficiary. A Payout may contain lines attributable to obligations originating from multiple ChefOrderGroups, multiple Orders, and other commercial sources under the eventual settlement architecture; this ADR requires neither one Payout per ChefOrderGroup nor one Payout per Order nor immediate external payout after Order or ChefOrderGroup completion. Exact commercial-provider obligation and settlement-beneficiary relationships belong to future ADR-020.

```text
PAYMENT RECEIVED
!= EARNING RECOGNIZED
!= PAYOUT ELIGIBLE
!= EXTERNAL PAYOUT COMPLETED
```

```text
MARKETPLACE SETTLEMENT != EMPLOYEE / CONTRACTOR PAYROLL
```

ChefOrderGroup metrics may support an Organization's lawful internal compensation processes, but Cheffy's marketplace Financial architecture must not model an employed Chef's wages as a Chef marketplace payout redirected to the employer. Payroll, wages, worker remuneration, withholding, and employee incentive compensation remain outside this ADR.

## Partial Refund Rule

Only the directly affected ChefOrderGroup food allocation is recalculated. Shared Order-level effects follow approved tax, promotion, delivery, and fee rules. Other ChefOrderGroups remain independently traceable.

A partial refund affecting Chef A may reference the Order, ChefOrderGroup A, applicable OrderItems, immutable Pricing/Promotion evidence, commercial provider, and subscription-origin evidence without making Refund part of ChefOrderGroup. Refund processing may require new adjustments to commercial-provider value, Platform fees, Platform subsidies, tax, delivery, or other obligations according to captured economic evidence. Chef-specific quality/refund traceability for an employed Chef does not make the refund a payroll deduction. This ADR does not calculate those adjustments; Refund and resulting adjustments remain Financial-owned under future ADR-020 and ADR-015.

## Historical Traceability

ChefOrderGroup historical identity must continue to explain the actual Chef who participated and the applicable transaction-time authorization/commercial context even if the Chef later leaves an Organization, joins another Organization, works independently, changes profile details, the Organization changes name, the provider arrangement ends, the connected account changes, a ChefMealPlan changes, a Promotion changes, or subscription terms renew. Historical performer identity must not be recomputed from current Organization membership. Current/latest references may exist for convenience, but immutable historical evidence must preserve the applicable performer, physical Kitchen, commercial-provider context, subscription origin, Pricing, and Promotion context for the concrete Order. Exact snapshot/reference strategy remains for the canonical ERD and detailed architecture.

## Responsibility Boundary

### ChefOrderGroup owns

- one actual Chef performer's participation inside one concrete food Order;
- Chef preparation lifecycle;
- Chef-specific authorization and OrderItem grouping; and
- Chef-scoped operational/reputation, economic-source, refund/payout-traceability, reporting, and analytics boundaries.

### Order owns

- exactly one authoritative physical Kitchen;
- Customer checkout and immutable fulfillment type; and
- pickup, delivery coordination, and final fulfillment lifecycle.

### Meal Subscription domain owns

- MealSubscription and its accepted terms;
- entitlement and billing/entitlement-cycle behavior;
- occurrence request and Kitchen-capacity readiness; and
- MealFulfillmentOccurrence lifecycle before and around concrete Order materialization.

### Professional / Dietitian domain owns

- DietitianProfessionalProfile;
- private DietitianMealPlan and professional recommendations; and
- Customer authorization governing any extracted marketplace requirements.

### Organization / business architecture owns

- commercial-provider identity and business lifecycle;
- Organization membership and Chef engagement/authorization relationships; and
- Kitchen operating authority.

### Financial domain owns

- Payment, PaymentAttempt, and PaymentAllocation;
- Refund and RefundLine;
- Payout and PayoutLine;
- commercial-provider obligation, settlement-beneficiary, earning-recognition, and payout-eligibility facts decided by future ADR-020; and
- LedgerTransaction and LedgerEntry under ADR-015.

### Promotion / Pricing domains own

- Promotion rules, applications, and evaluation evidence; and
- PricingSnapshot and commercial calculation evidence.

Dietitian consultations and Kitchen bookings retain their own domain aggregates and must not be forced through ChefOrderGroup. No `OrganizationOrderGroup`, `ProviderOrderGroup`, `SellerOrderGroup`, or `MarketplacePartyGroup` is introduced.

### HR / payroll outside the marketplace owns

- employee or contractor compensation;
- wages and payroll; and
- worker remuneration, withholding, and payroll remittance.

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
- Actual-Chef performer and economic-source traceability without conflating Organization settlement.
- Supports multi-Chef preparation without breaking one-Kitchen-per-Order.
- Supports subscription-origin and Organization-operated supply without expanding ChefOrderGroup ownership.
- Preserves individual Chef accountability and history under common Organization operation.

### Negative

- Parent/child state coordination is required.
- Cross-table invariants need explicit enforcement.
- Additional aggregate complexity.
- Exact subscription-origin, performer key, provider/beneficiary, and generalized Financial relationships remain for later owning decisions and canonical persistence design.

## Implementation Notes

- Implement optimistic concurrency using the aggregate version.
- Enforce Chef/Kitchen/Order consistency transactionally.
- Add indexes and a uniqueness constraint for Order plus the canonical durable actual-Chef performer reference selected by the ERD; do not use a shared Organization/provider/payee identifier as the performer key.
- Ensure financial records use `chef_order_group_id` where required.
- Add tests for multi-Chef Orders, separate employed Chefs under one Organization, independent-Chef and Cheffy Operations use of the same model, partial rejection, cancellation, readiness aggregation, partial refunds, and financial traceability.
- Test that pre-materialization MealFulfillmentOccurrence states create no ChefOrderGroup and that a materialized subscription-origin Order preserves occurrence traceability and the one-Kitchen invariant.
- Test that Dietitian guidance or FoodRequest origin creates no Dietitian commercial attribution on Order/ChefOrderGroup and does not expose private professional records.
- Test that common commercial provider, settlement beneficiary, or connected account does not merge distinct Chef performers and that historical performer identity does not depend on current Organization membership.
- Do not implement speculative subscription-origin or generalized Financial source columns from this ADR alone; update the owning ADRs and canonical ERD first.

## Dependencies

ADR-005, ADR-012, ADR-014, ADR-015, ADR-016, and future ADR-020 for commercial-provider obligations, settlement-beneficiary relationships, earning recognition, and payout eligibility.
