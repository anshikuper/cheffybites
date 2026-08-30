# ADR-014 — Promotion Engine

## Status

Proposed

## Amendment Scope

This proposal expands the existing food Promotion engine into one cross-domain Promotion engine within the modular monolith and reconciles evaluation/funding behavior with Organization-operated food supply and the simplified Dietitian professional-service model. It preserves the accepted food behavior, relies on ADR-006 for typed targeting and owner-identity structure, and does not absorb the future ADR-020 commercial-obligation/earning model or ADR-015 ledger and reconciliation responsibilities.

## Context

Cheffy Bites needs Chef, Platform, Entrepreneur, Dietitian, and Organization Promotions with deterministic conflict resolution, Chef isolation, immutable historical evidence, and partial-refund recalculation. Approved commercial contexts now include food Orders, Kitchen Bookings, Equipment Rentals, Dietitian consultations, Meal Subscriptions, and Kitchen Subscriptions. They share Promotion definition, compatibility, redemption, and evidence concerns but do not all share food-specific calculation scopes or qualifying bases. An actual Chef performer may differ from the Organization commercially supplying the food and from the approved party funding a Promotion benefit.

## Decision

### Promotion Ownership

```text
CHEF
PLATFORM
ENTREPRENEUR
DIETITIAN
ORGANIZATION
```

These are conceptual owner identities authorized only in applicable commercial domains; this list does not require one universal owner enum or table. `PLATFORM` is an ownership domain, not itself a food-order calculation scope.

Promotion owner, commercial domain, calculation scope, target, benefit, service performer, funding source, commercial provider, and settlement beneficiary remain separate dimensions:

```text
PROMOTION OWNER
!= COMMERCIAL DOMAIN
!= CALCULATION SCOPE
!= TARGET
!= BENEFIT
!= SERVICE PERFORMER
!= FUNDING SOURCE
!= COMMERCIAL PROVIDER
!= SETTLEMENT BENEFICIARY
```

Approved owners may operate only in authorized contexts: Chefs may sponsor approved food, ChefMealPlan, or MealSubscription-related Promotions; Entrepreneurs may sponsor KitchenBooking, EquipmentRental, or KitchenSubscription-related Promotions; Dietitians may sponsor eligible ConsultationOffering Promotions; Organizations may sponsor Promotions in commercial domains for which they have approved authority, including Organization-operated food or an approved clinic consultation context; and the Platform may sponsor approved Promotions across commercial domains. An Entrepreneur is not automatically an Organization or Kitchen property owner, and an authorized Kitchen operator Organization may own an eligible Promotion even when another party owns the property. Neither commercial domain, target, calculation scope, service performer, commercial provider, settlement beneficiary, nor funding source is inferred from owner alone. A campaign may declare another explicitly approved funding arrangement.

A provider/commercial-party-funded Customer-price Promotion normally reduces the applicable commercial-provider economics according to the accepted funding arrangement. For an independent Chef business, this may reduce that Chef business's commercial economics. For an Organization-employed Chef, the approved funding responsibility may instead belong to the Organization commercial provider and must not be treated as an automatic reduction of the employee Chef's wages or personal marketplace proceeds. An Entrepreneur/provider-funded benefit similarly follows the approved Kitchen commercial-provider arrangement, and a Dietitian/provider-funded consultation discount follows the approved professional-service provider arrangement. A Platform-funded Customer-price subsidy creates Platform-funded subsidy economics and normally preserves the provider's gross commercial service value under the applicable terms. These are captured commercial-calculation semantics; future ADR-020 and ADR-015 own the resulting financial obligations and ledger postings.

Accepted ADR-006 owns the canonical typed Promotion owner and target identities, domain-valid owner/target structure, and owner/target referential-integrity requirements. Canonical Promotion-owner persistence must represent `CHEF`, `PLATFORM`, `ENTREPRENEUR`, `DIETITIAN`, and `ORGANIZATION` as typed semantic owner cases, preserve domain-valid identity and database-enforceable referential integrity where practical, and retain durable historical owner attribution. It must not use an unconstrained `owner_type` plus arbitrary UUID `owner_id` relationship that could identify an unrelated entity. Owner identity remains distinct from target, calculation scope, and funding party where policy permits them to differ. Current Organization membership, professional engagement, or other mutable configuration may support current authorization but must not be used to recompute historical Promotion ownership; owner inactivity and taxonomy or reference-label changes likewise must not rewrite that attribution. Platform ownership remains a typed semantic case; it does not require a fake arbitrary Platform UUID. ADR-014 consumes the authorized owner/target context during evaluation; it does not own or finalize the exact owner table, Organization foreign key, Platform representation, enum implementation, target table, or relational owner/target structure. Exact physical Promotion-owner persistence is deferred to the canonical ERD in `docs/03-database-erd.md` after the ADR decisions are reconciled. `CHEF_BUSINESS` retains its accepted meaning and is not redefined as every Organization employing or engaging Chefs.

An Organization-wide food Promotion may evaluate qualifying offerings across multiple actual Chef performers when ADR-006 supplies the valid typed Organization owner/target context. An Organization-owned Promotion may instead target only Ravi's FoodListings while Maria's remain ineligible. In either case, Ravi's and Maria's ChefOrderGroups remain separate, and the evaluation preserves `OWNER != TARGET != CALCULATION SCOPE != FUNDING SOURCE`: Ravi's ChefOrderGroup may be a calculation/reference scope while ABC Food Group is the owner and commercial provider and ABC Food Group or another approved party bears the benefit under the accepted terms. Ravi does not personally fund the Promotion merely because his ChefOrderGroup is the calculation scope.

### Promotion Commercial Domains

The Promotion commercial domain identifies the evaluator/policy boundary for one commercial calculation. Approved domains may include:

```text
FOOD_ORDER
KITCHEN_BOOKING
EQUIPMENT_RENTAL
DIETITIAN_CONSULTATION
MEAL_SUBSCRIPTION
KITCHEN_SUBSCRIPTION
```

ChefMealPlan and subscription offers may be targets under ADR-006 while application occurs in the applicable purchase, subscription, or billing context. Owner does not determine domain, and unrelated transactions do not share one calculation merely because the same participant or Platform Promotion is involved.

### Promotion Calculation Scopes

For food Orders:

```text
ITEM
CHEF_ORDER_GROUP
DELIVERY
ORDER
```

Food scopes remain food-specific. Each other commercial domain defines typed scopes and qualifying bases appropriate to that domain. `ITEM`, `CHEF_ORDER_GROUP`, and `DELIVERY` are not forced onto Dietitian consultation, Kitchen Booking, Equipment Rental, Meal Subscription, or Kitchen Subscription pricing.

Calculation scope answers, "What monetary portion or components does this Promotion calculate against?" Commercial provider answers, "Which approved marketplace business commercially supplies the transaction?" Funding source answers, "Which approved party bears the economic Promotion benefit?" These dimensions are not interchangeable. `CHEF_ORDER_GROUP` may isolate Ravi's qualifying items while ABC Food Group is the commercial provider and approved economic funder.

### Qualifying Basis

```text
ALL_ELIGIBLE_ITEMS
NON_DISCOUNTED_ELIGIBLE_ITEMS
SPECIFIC_TARGET_ITEMS
GROUP_SUBTOTAL
DELIVERY_FEE
```

`NON_DISCOUNTED_ELIGIBLE_ITEMS` excludes items already receiving the relevant item-level discounts.

These food qualifying bases remain valid. Other domains may define explicit bases such as eligible consultation service value, booking/rental components, subscription billing-cycle value, commitment, entitlement quantity, or another approved domain-specific basis. A domain evaluator must not populate irrelevant food-only concepts merely for structural symmetry.

### Chef Independence

Chef promotions evaluate only within the same ChefOrderGroup. Chef A items can never qualify Chef B promotions, and vice versa. Promotions for separate ChefOrderGroups may coexist.

Item Promotions on different items may coexist. An item-level Promotion and a ChefOrderGroup-level Promotion may coexist when their monetary scopes do not overlap and compatibility rules permit it. `NON_DISCOUNTED_ELIGIBLE_ITEMS` excludes already-discounted eligible items when the configured group policy requires that behavior.

ChefOrderGroup remains an approved Chef-specific calculation/reference scope even when multiple Chef performers share one Organization commercial provider. Scope does not identify the economic funder: ChefOrderGroup Ravi and ChefOrderGroup Maria remain separate calculation scopes, while an accepted arrangement may assign both benefits to ABC Food Group economics. An employee Chef is not automatically the funding party merely because that Chef performed the work.

The engine must also be capable of evaluating a commercial-provider-level policy across qualifying food offerings from multiple Chefs using the valid typed owner/target context supplied by ADR-006. It must not route an Organization-wide benefit through a fake individual Chef. This capability does not make ADR-014 the owner of the exact Organization-wide target or owner representation.

### Deterministic Conflict Resolution

1. Determine eligibility.
2. Partition by target and calculation scope.
3. Apply compatibility rules.
4. Apply exclusivity rules.
5. Higher priority wins conflicts.
6. If equal, greater customer savings wins.
7. If still equal, lexicographically smaller immutable promotion UUID wins.

Compatibility and exclusivity are evaluated within the relevant commercial calculation and monetary scopes. Overlapping scopes require resolution; non-overlapping scopes may coexist when policy permits. No global cross-domain stacking matrix and no blanket `stackable` boolean is introduced. Promotions in unrelated transactions, such as one food Order and one Dietitian consultation, do not need to stack or conflict with one another.

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

Every evaluated Promotion, applied or rejected, preserves the applicable Promotion id/version, owner, commercial domain, target/target family, calculation scope, commercial-provider context where relevant, qualifying basis, eligible/excluded components, original qualifying request timestamp where relevant, evaluated amount/subtotal, benefit type, calculated benefit, approved funding source/context, result/reason, compatibility/conflict evidence, source commercial-context reference, and timestamp. Food evidence additionally retains ChefOrderGroup, OrderItem, Delivery, and Order references where applicable. Other domains do not populate irrelevant food-only fields.

Original snapshots remain immutable. Refund recalculation creates new evidence and financial adjustments.

PromotionSnapshot and PromotionApplication are Promotion-owned evaluation evidence. The resulting PricingSnapshot, where captured by the pricing workflow, is Pricing-owned immutable commercial/calculation evidence. Payment, PaymentAllocation, Refund, Payout, LedgerTransaction, and LedgerEntry remain separate immutable Financial-domain facts; promotion evaluation does not create a separate snapshot aggregate in the Financial domain.

Promotion application evidence may explain, for example, a Platform-funded $50 Customer subsidy or a commercial-provider-funded 10% food discount. It is not canonical ledger, provider-earning, payout-eligibility, refund-allocation, or external-settlement evidence. There is no `FinancialSnapshot`; PricingSnapshot remains the canonical Pricing-owned commercial calculation snapshot.

### Benefit and Commercial Funding Model

At minimum, the engine distinguishes:

```text
CUSTOMER_PRICE_DISCOUNT
PLATFORM_FEE_DISCOUNT
PLATFORM_FEE_WAIVER
```

A Customer-price benefit changes the participant contribution. A Platform-fee discount or waiver changes the provider-facing Platform fee and must declare its own applicability, calculation basis, and evidence; it must not silently alter Customer subtotal calculations. A campaign may contain both conceptual benefit families where approved, but each calculation and consequence remains distinguishable. One undifferentiated numeric discount is not sufficient for every benefit.

A Platform-fee benefit applies to the applicable commercial provider's fee obligation under the approved arrangement. The engine must not assume an employee performer owes that fee merely because the employee performed the service.

A Platform-funded Customer-price Promotion is the canonical commercial mechanism for a Platform marketplace subsidy; no separate subsidy engine is introduced. For example:

```text
Dietitian consultation service value: $70
Platform customer subsidy:            $50
Customer pays:                         $20
Dietitian gross service value:         $70
```

The normal Platform fee may still apply. Customer payment of zero does not imply a zero Platform fee; only a separate approved fee benefit reduces or waives that fee. Customer contribution and Platform-funded Customer subsidy are distinct funding facts. The provider's gross commercial value must not be inferred solely from the Customer contribution. Promotion/Pricing owns why the subsidy applies, eligibility, cap, benefit calculation, and immutable application evidence. Financial owns the resulting subsidy/funding obligation, earning consequences, ledger, refund adjustment, and payout consequences under future ADR-020 and ADR-015.

Platform-funded subsidy may support independent-provider food, Organization-operated food, Dietitian consultation, or another approved domain. The same engine evaluates the benefit; no provider-type-specific subsidy engine is introduced.

### DietitianMealPlan Discovery and Privacy Boundary

`DietitianMealPlan` and professional recommendations may provide Customer-authorized structured meal requirements used for search, filtering, matching, FoodListing or ChefMealPlan selection, or FoodRequest creation. That discovery flow is not a Promotion attribution mechanism. A later food Order does not automatically receive a Dietitian-linked Promotion, referral benefit, financial attribution, or commission merely because professional guidance informed discovery.

Food PromotionApplication or PromotionSnapshot evidence must not retain DietitianProfessionalProfile, DietitianMealPlan, recommendation/referral identity, or full professional records merely for that discovery origin. Diagnosis, medication, clinical notes, private professional commentary, and unrelated professional information must not be exposed to a Chef or processed by the Promotion engine unnecessarily. Ordinary Customer-authorized dietary or meal-requirement attributes may participate as commercial eligibility inputs where an approved Promotion permits them. ADR-014 does not turn Promotion into a health-record processing engine.

A future Promotion that genuinely requires a narrowly defined professional eligibility attribute requires explicit product, privacy, professional-regulatory, targeting, and Customer-authorization approval. It must not be inferred from the current discovery flow.

### Promotion Model

The following partial DDL illustrates only Promotion-engine fields governed by ADR-014. Promotion-owner columns are deliberately omitted: Accepted ADR-006 governs canonical typed owner identity and relational semantics, and `docs/03-database-erd.md` will define the exact physical owner representation. This block is not a complete executable canonical schema and must not be extended with an unconstrained owner discriminator plus arbitrary UUID relationship.

```sql
CREATE TABLE promotion.promotions (
    id UUID PRIMARY KEY,
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

ADR-006 alone owns targeting structure, typed target-family relationships, and target referential integrity. This ADR does not duplicate that design. In particular, advance booking is not a target, and EquipmentCatalogItem reference metadata is not automatically the commercial discount subject; an approved EquipmentRental or other typed commercial resource is used under ADR-006.

## Domain Evaluator Architecture

One extensible Promotion engine remains inside the modular monolith. Common core responsibilities are:

- Promotion definition and immutable version;
- deterministic evaluation contract;
- compatibility and exclusivity framework;
- promo-code reservation/redemption lifecycle;
- common application, revalidation, restoration, and evidence lifecycle; and
- delegation to a typed commercial-domain evaluator.

Domain-specific evaluators/policies provide:

- calculation scopes and qualifying bases;
- authoritative source data and eligible monetary components;
- applicable commercial-provider and approved funding context without inferring either from the service performer;
- the advance-time reference when relevant;
- cancellation, no-show, termination-penalty applicability; and
- material-change revalidation rules.

This avoids copy/paste Promotion engines while also avoiding one monolithic switch whose unrelated domain rules are mixed together. It introduces neither a microservice nor a separate cross-domain Promotion ADR.

## Advance-Booking Eligibility and Revalidation

Advance booking is an eligibility condition, not a Promotion owner, target, commercial domain, calculation scope, commercial provider, funding source, or separate engine. The original qualifying requester timestamp normally determines whether the request met a configured lead time. Provider-side processing, approval, or Kitchen-capacity delay does not remeasure and destroy that qualification merely because confirmation occurs later.

Examples include a food request at least 72 hours before fulfillment, a Kitchen request at least 14 days before the requested booking, or Meal Subscription enrollment at least a configured duration before first service. The evaluator preserves the original request timestamp and relevant inputs as evidence.

```text
OPERATIONAL MINIMUM LEAD TIME
!= PROMOTIONAL ADVANCE-BOOKING LEAD TIME
```

A Promotion never overrides an operational cutoff. A request may be operationally valid without satisfying the longer promotional lead time.

A material requester-initiated change may trigger domain-policy re-evaluation, including changes to fulfillment date/window, quantity, product/offering, or commercial scope. Provider-side operational updates or processing delay alone are not requester changes. Exact materiality is owned by the applicable domain evaluator.

Promotion validity and other eligibility are revalidated at the domain's policy-defined commit/confirmation point. Cart or request presence alone never guarantees application. A campaign may explicitly protect qualification established while the campaign was valid at the original request timestamp; otherwise campaign expiry before the defined confirmation point can make it ineligible. The Promotion version/domain policy must state which behavior applies rather than imposing one silent rule globally.

## Pending Meal Fulfillment

A MealFulfillmentOccurrence may move through:

```text
REQUESTED → PENDING_KITCHEN_CAPACITY → CONFIRMED | EXPIRED | DECLINED
```

A request may qualify before Kitchen capacity is secured. While pending, the engine may preserve or reserve Promotion id/version, qualification timestamp, evaluated commercial inputs, quoted benefit, entered-code intent, and other evidence needed to explain the commercial intent.

```text
PENDING REQUEST != SUCCESSFUL FINAL REDEMPTION
```

Pending qualification must not prematurely create an irreversible successful redemption. If the occurrence cannot become commercially confirmable because it expires, is declined, or suffers provider/Kitchen-capacity failure, its pending reservation does not permanently consume participant eligibility unless an explicitly approved campaign policy says otherwise. Exact table and state names remain for the canonical ERD.

Assignment or reassignment of an Organization-employed Chef while fulfillment remains pending does not by itself create another Promotion use, invalidate the Customer's original qualifying timestamp, create Dietitian attribution, or copy private Dietitian-plan information into Promotion evidence. A material Customer/request change may still trigger the normal domain-policy re-evaluation.

## Domain-Specific Commercial Behavior

### Meal Subscription

Meal Subscription Promotions may apply to initial enrollment, first billing cycle, an explicit first-N-cycle duration, minimum commitment, minimum meals/entitlement, advance enrollment, or an eligible ChefMealPlan/MealSubscriptionOffer target under ADR-006. ChefMealPlan remains a catalog product and does not own subscription-billing Promotion state. Each billing/application occurrence preserves the Promotion version and relevant eligibility evidence. Renewal does not continue a Promotion indefinitely unless duration and renewal applicability explicitly say so.

```text
ChefMealPlan != MealSubscriptionOffer != MealSubscription
```

The Meal Subscription commercial provider may be an Organization employing or engaging the actual occurrence-level Chef performers. The actual Chef performer, commercial provider, settlement beneficiary, and Promotion funder are not assumed to be the same party. DietitianMealPlan-guided discovery creates no Meal Subscription Promotion attribution or commission.

### Kitchen Subscription

Entrepreneur or Platform Promotions may apply to an eligible KitchenSubscriptionOffer, first billing cycle, commitment incentive, advance enrollment, or a separately defined equipment-related benefit. Kitchen entitlement and base subscription terms are not themselves monetary Promotion benefits and remain separate from Promotion.

The authorized Kitchen operator/commercial Organization may differ from the property owner. Promotion evaluation follows the approved commercial-provider and ADR-006 target context; property ownership alone does not make a landlord the Promotion funder or operator. Exact owner/target representation remains ADR-006's responsibility.

### Dietitian Consultation

The engine supports Dietitian-owned consultation discounts and Platform-funded consultation subsidies against an eligible ConsultationOffering target under ADR-006. Provider cancellation/no-show invokes captured Customer-protection and restoration policy; Customer cancellation/no-show follows the captured policy. Customer-price Promotions do not automatically cover cancellation or no-show charges. Professional-service tax and legal treatment are outside this ADR.

```text
DIETITIAN CONSULTATION PROMOTION
!= DIETITIANMEALPLAN-GUIDED FOOD PROMOTION ATTRIBUTION
```

A Customer may receive a consultation Promotion and later independently buy food. The later purchase does not inherit a Dietitian-linked Promotion or financial relationship merely because the consultation occurred.

### Kitchen Booking and Equipment Rental

Entrepreneur or Platform Promotions may apply to KitchenBooking, EquipmentRental, or KitchenSubscription commercial contexts through typed targets under ADR-006. EquipmentCatalogItem remains master reference metadata and does not own rental pricing or discount economics.

## Promo Code Rules

- At most one customer-entered promo code may be applied per concrete food Order checkout.
- Automatically applied promotions may coexist when compatibility/exclusivity rules permit.
- A specific customer may successfully redeem a specific promo code at most once. This fixed per-customer rule is not configurable.
- A promo code may define optional `max_global_uses`; `NULL` means no configured global cap, and `max_global_uses = 1` defines a globally one-time code.
- No `max_uses_per_customer` policy is supported.
- Expired promotions are invalid at evaluation time.
- Items remaining in carts do not reserve expired promotions.

For a new commercial domain, the default direction is at most one authenticated participant-entered code per relevant checkout or billing action unless an approved requirement permits more. This does not rewrite existing Customer/Order semantics or create multi-code stacking. A non-Customer buyer, such as a Chef purchasing Kitchen services, uses the appropriate authenticated redeemer/participant eligibility; exact generalized redeemer representation remains for the canonical ERD.

An employed Chef acting as an authorized participant for KitchenBooking or KitchenSubscription does not thereby become the commercial provider for a Customer food Order. Redeemer identity, service performer, and commercial-provider identity remain separate concepts.

First-use is scoped to the commercial history named by the Promotion, such as first eligible food Order, first Dietitian consultation, or first Meal Subscription enrollment. It is not marketplace-global unless explicitly configured. Rescheduling the same underlying service does not create another first-use opportunity.

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

That accepted food flow remains unchanged. New commercial contexts may reserve a promo-code-backed transaction before final commercial confirmation/payment without requiring an Order ID. Their commercial reference and uniqueness/cardinality are domain-specific and must use typed/domain-safe relationships in the canonical ERD; this ADR does not finalize one universal promo-redemption foreign-key design.

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

Generalized capacity-limited redemption retains the same database-safe principle: reservation must serialize against the authoritative promo-code capacity source and must not oversubscribe `max_global_uses`. A check-then-insert race is prohibited even if the final ERD selects different typed references or indexes for another commercial domain.

Reservation is a local database transaction and does not create a distributed transaction with a payment provider. After authoritative successful payment/checkout completion, the appropriate idempotent local workflow transitions `RESERVED → REDEEMED`. If checkout definitively expires, is cancelled, or finally fails before redemption, it transitions `RESERVED → RELEASED` idempotently. A provider callback and database update are not one ACID transaction.

### Provider-Failure Restoration

A normal refund does not make a redeemed code reusable and never rewrites `REDEEMED → RELEASED`. However, when an approved campaign/domain policy protects a participant from provider-caused non-performance—such as Dietitian consultation cancellation/no-show, food-provider non-fulfillment, or Kitchen operator/provider failure to provide the confirmed service—the engine supports an explicit eligibility-restoration or replacement treatment.

Restoration appends a policy-driven adjustment, release, eligibility-restoration fact, or equivalent later canonical representation. It never deletes or mutates away historical application/redemption evidence. Customer/requester cancellation does not automatically restore first-use or single-use eligibility; captured campaign/domain policy controls it. Under Organization-operated supply, provider-failure economics follow the commercial-provider arrangement and do not become an employee payroll deduction. The exact table/state representation remains deferred to the canonical ERD.

### Promotion Application and Redemption References

PromotionApplication and PromotionSnapshot preserve pricing-calculation evidence; PromoCodeRedemption preserves customer/code usage and cardinality. For PromotionApplication:

- `promotion_id` is required.
- `order_id` is required for an Order pricing application.
- `chef_order_group_id` is nullable because `ORDER` and `DELIVERY` scopes may have no ChefOrderGroup.
- `order_item_id` is nullable because non-`ITEM` scopes do not target one item.
- `promo_code_id` is nullable because automatic promotions use no entered code.
- `promo_code_redemption_id` is nullable and identifies the redemption when the application derives from a customer-entered code.

Order-, Delivery-, and Platform-level applications must not be forced into a ChefOrderGroup. Automatically applied promotion applications do not require a promo-code or redemption reference.

Cross-domain applications use a typed/domain-safe commercial-context reference rather than requiring every context to have an Order ID. Exact physical relationships belong to the canonical ERD. Application evidence records commercial evaluation, not Financial posting.

## Cancellation, No-Show, and Penalty Applicability

A `CUSTOMER_PRICE_DISCOUNT` does not automatically subsidize a cancellation fee, no-show fee, early-termination penalty, or other penalty-like charge. A campaign must explicitly opt into that treatment where legally and commercially permitted. The owning business/Pricing domain calculates the charge; this engine determines whether a Promotion benefit is eligible to apply to it. This decision is independent of employee compensation and payroll.

For example, a Platform consultation subsidy must not automatically fund a Dietitian no-show charge when the Customer no-shows. Provider-caused failure follows the Customer-protection/restoration policy instead. Promotion behavior remains subject to approved consumer, subscription, professional-service, tax, jurisdiction, marketing, cancellation, and no-show rules, with the applicable policy version retained as evidence.

## Partial Refund Recalculation

1. Identify refunded items.
2. Identify affected ChefOrderGroup and scopes.
3. Load original promotion evidence.
4. Recalculate according to approved rules.
5. Determine refund/adjustment.
6. Create new immutable financial records.
7. Preserve all original promotion and financial evidence.

A full or partial refund does not restore promo-code eligibility, release a `REDEEMED` redemption, or overwrite the original application/redemption. Refund recalculation and adjustment create new immutable promotion and financial evidence while the successful redemption remains historical usage.

Cross-domain repricing likewise preserves the original application. New adjustment evidence uses captured original commercial inputs to identify affected components, which may include Customer contribution, commercial-provider-funded discount, Platform subsidy, Platform-fee benefit, delivery, tax, or other approved commercial components. Dietitian consultation repricing may retain its legitimate consultation-provider funding and subsidy evidence, but food repricing contains no Dietitian recommendation/referral attribution or food commission. ADR-014 does not perform final financial allocation: future ADR-020 owns commercial-provider obligation, settlement-beneficiary, earning-recognition, Platform-fee, Platform-subsidy, refund/remediation, and payout-eligibility semantics, while ADR-015 owns ledger posting and reconciliation.

## Financial, Settlement, and Payroll Responsibility Boundary

ADR-014 owns Promotion evaluation, compatibility/exclusivity, benefit calculation, approved funding context, revalidation, redemption/restoration policy, and immutable Promotion evidence. It does not determine final financial obligations, settlement, earning recognition, payout eligibility, or ledger postings.

Future ADR-020 owns commercial-provider obligation, settlement-beneficiary relationships, earning recognition, Customer-funded unfulfilled value, Platform-fee and Platform-subsidy obligations, payout eligibility, and refund/remediation economics. It must not model Dietitian food-sale, Meal Subscription, recommendation, referral, or Chef-purchase commission under the current product decision. ADR-012 owns provider-neutral payment/refund/settlement orchestration, and ADR-015 owns immutable ledger posting and reconciliation.

```text
MARKETPLACE PROMOTION ECONOMICS != EMPLOYEE PAYROLL
```

Promotion may affect an Organization's marketplace economics. ADR-014 does not calculate salary, hourly wages, employee sales commission, employee bonus, payroll deduction, or worker compensation. An Organization may independently use marketplace performance data for lawful compensation programs outside this Promotion architecture.

The intended ADR ownership remains: ADR-006 for typed targeting and owner relationships; ADR-014 for engine evaluation, benefit, and funding behavior; future ADR-020 for financial obligations, earning, and settlement eligibility; and ADR-015 for ledger/reconciliation. No additional Promotion ADR is introduced.

## Consequences

### Positive

- Deterministic resolution.
- Strict Chef promotion isolation.
- Supports multiple promotion scopes.
- Avoids blanket `stackable` logic.
- Strong historical evidence.
- Supports domain-specific evaluation without duplicating the Promotion engine.
- Distinguishes customer-price, Platform-fee, and Platform-funded subsidy behavior.
- Supports Organization-operated food without conflating Chef calculation scope, commercial provider, funding source, or employee compensation.
- Preserves Dietitian consultation Promotions without creating food recommendation/referral attribution.

### Negative

- More complex pricing logic and tests.
- More snapshot storage.
- Domain-specific evaluators and restoration policies require explicit versioning and broad concurrency/revalidation coverage.

## Implementation Notes

- Keep the core engine and typed domain evaluators inside the modular monolith.
- Preserve existing food evaluation, code cardinality, and promo-code-row locking semantics.
- Add tests for each commercial domain's scopes, qualifying bases, owner/funding combinations, and evidence without requiring food-only fields.
- Test advance qualification against original requester time, provider delay, material requester changes, operational cutoffs, and campaign-expiry policy.
- Test pending MealFulfillmentOccurrence reservation/release, provider-failure restoration, Customer cancellation, first-use rescheduling, and concurrent global-cap reservation.
- Test customer-price subsidy separately from Platform-fee reduction/waiver and verify penalty charges are excluded unless explicitly opted in.
- Test Organization-operated food where ChefOrderGroup Ravi is the calculation scope, ABC Food Group is commercial provider/funder, and Ravi's payroll is unaffected by Promotion evaluation.
- Test independent-Chef and Cheffy Operations transactions through the same engine with no provider-specific branch.
- Test Organization-wide food policy evaluation from a valid ADR-006 context without introducing a fake Chef target or redefining ADR-006's physical owner/target representation in ADR-014.
- Test Dietitian consultation discounts/subsidies separately from DietitianMealPlan-guided food discovery; food evidence must contain no Dietitian recommendation/referral attribution or private professional record.
- Do not implement generalized target relationships here; follow ADR-006 and update the canonical ERD/API contracts before implementation.
- Do not create Financial obligations or ledger postings from Promotion evidence alone.

## Dependencies

ADR-006, ADR-012, ADR-013, ADR-015, ADR-016, and future ADR-020 for commercial-provider obligations, settlement-beneficiary relationships, earning recognition, Platform-fee/Platform-subsidy facts, refund/remediation allocation, and payout eligibility.
