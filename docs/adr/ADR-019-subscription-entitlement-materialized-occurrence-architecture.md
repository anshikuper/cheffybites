# ADR-019: Subscription, Entitlement and Materialized Occurrence Architecture

## Status

Proposed

## Context

Cheffy Bites requires two distinct subscription products:

1. **Customer Meal Subscription**, through which a Customer accepts versioned commercial terms for recurring meal entitlement and concrete meal-service occurrences; and
2. **Chef Kitchen Subscription**, through which a Chef accepts versioned commercial terms for recurring Kitchen access/capacity entitlement that may fund ordinary KitchenBookings.

The products share architectural concerns such as versioned offers, recurring billing, commitments, entitlement periods, bounded recurrence materialization, payment grace, pause, termination, provider nonperformance, historical evidence, and downstream Financial coordination. Those similarities do not make the products one business aggregate. Their service obligations, capacity dependencies, occurrence models, performance semantics, and cancellation consequences differ materially.

A Meal Subscription funds contractual rights to meal service. Provider performance is generally evidenced by individual fulfilled meal occurrences and their resulting ordinary food fulfillment. A Kitchen Subscription funds contractual rights to qualifying access/capacity. Provider performance may be satisfied by making the accepted access/capacity available even when the Chef voluntarily does not use every entitled hour. Treating these products identically would erase the distinction that future commercial-obligation and earning-recognition architecture requires.

Subscription planning also creates several facts that must not be conflated. An offer is not an accepted agreement. Funding cadence is not entitlement cadence. A long contractual commitment is not annual prepayment. A recurring local-time rule is not a concrete scheduled occurrence. An entitlement is not a physical reservation. Payment success is not service performance, earning recognition, payout eligibility, or external payout completion.

This ADR establishes the shared architecture principles and the domain-specific boundaries needed to model those facts correctly. It does not finalize SQL, tables, columns, foreign keys, APIs, exact events, Payment-provider behavior, accounting, ledger postings, earning recognition, payout eligibility, or settlement routing.

## Decision

### 1. Core Separation

Cheffy Bites will preserve these concepts as distinct business facts:

```text
SUBSCRIPTION OFFER
!= SUBSCRIPTION AGREEMENT
!= BILLING CYCLE
!= ENTITLEMENT CYCLE
!= RECURRING RULE
!= MATERIALIZED OCCURRENCE
!= PAYMENT
!= EARNING
!= PAYOUT
```

The two subscription products also remain distinct:

```text
MEAL SUBSCRIPTION
!= KITCHEN SUBSCRIPTION
```

Further required distinctions are:

```text
ENTITLEMENT != CALENDAR RESERVATION
RECURRING RULE != CONCRETE OCCURRENCE
SUBSCRIPTION != ORDER
KITCHEN SUBSCRIPTION != KITCHEN BOOKING
```

Shared architecture principles do not require a shared universal persistence aggregate, table, lifecycle, state machine, occurrence model, or entitlement representation.

### 2. No Universal Subscription Aggregate

Cheffy Bites will not introduce one canonical universal `Subscription`, `SubscriptionOffer`, `SubscriptionOccurrence`, or `Entitlement` aggregate/table merely because Meal and Kitchen subscriptions share vocabulary.

The architecture preserves domain-specific concepts, including conceptually:

```text
Customer Meal Subscription domain
    ChefMealPlan
    MealSubscriptionOffer
    MealSubscription
    domain-specific Meal entitlement cycle/accounting
    MealFulfillmentOccurrence

Chef Kitchen Subscription domain
    KitchenSubscriptionOffer
    ChefKitchenSubscription
    KitchenEntitlementCycle
    recurring Kitchen request rule
    KitchenBooking
```

Shared policy evaluators, temporal primitives, recurrence utilities, idempotency components, and entitlement-accounting patterns are permitted where genuinely reusable. They must not become a universal business aggregate or erase domain ownership.

Canonical core relationships must remain explicit and typed. The future persistence design must not use unconstrained relationships such as:

```text
subscription_type + subscription_id UUID
source_type + source_id UUID
entity_type + entity_id UUID
```

Generic `attributes JSON`, `metadata JSON`, or an arbitrary entity registry is not an acceptable canonical relationship model. Exact typed persistence remains future canonical ERD propagation.

### 3. Conceptual Ownership

ADR-019 owns the architecture principles for:

- Meal and Kitchen subscription offer/agreement separation;
- subscription commitment, billing, entitlement, renewal, pause, suspension, and termination semantics;
- entitlement-cycle accounting and its domain consequences;
- subscription recurring rules and bounded materialization;
- MealFulfillmentOccurrence planning and operational outcome semantics;
- Kitchen-subscription entitlement meaning around ordinary KitchenBookings;
- Customer/Chef voluntary non-use versus provider inability/nonperformance;
- subscription payment-grace effects on future service commitment;
- occurrence cancellation versus subscription pause/termination;
- provider-failure entitlement release/restoration classification; and
- service-performance inputs required by future ADR-020.

ADR-019 does not own:

- Payment, PaymentAttempt, provider interaction, or Refund orchestration;
- PricingSnapshot, Promotion evaluation, or Promotion redemption/restoration;
- commercial obligations, earning recognition, unfulfilled-value economics, or payout eligibility;
- ledger postings or reconciliation;
- settlement-beneficiary or connected-account routing;
- Kitchen Space or Equipment capacity-concurrency mechanisms;
- ChefOrderGroup, Order fulfillment, Appointment, or professional identity;
- exact persistence, API, or event contracts; or
- employee/contractor payroll.

Owning capabilities coordinate through explicit modular-monolith application interfaces and transactional-outbox integration where asynchronous decoupling is justified. One domain must not duplicate another domain's authoritative facts.

## Shared Subscription Principles

### 4. Offer, Agreement, Cycle, and Occurrence Roles

Each concept answers a different question:

- **Subscription offer:** What versioned product, commercial, eligibility, scheduling, cancellation, and governance terms are available for enrollment?
- **Subscription agreement:** What accepted terms govern this Customer/provider or Chef/provider relationship?
- **Billing cycle:** For what funding period and cadence is Payment requested?
- **Entitlement cycle:** During what period and in what quantity are contractual rights granted, reserved, consumed, released, rolled, or expired?
- **Commitment period:** For how long is the subscriber contractually committed, subject to accepted termination policy?
- **Recurring rule:** What repeated local-time or scheduling intent should be considered for future materialization?
- **Materialized occurrence:** What concrete domain service occurrence or reservation has been created for a resolved time?

These concepts may reference one another through future typed relationships, but they are not one aggregate and do not share one universal lifecycle.

### 5. Three Independent Time Periods

Subscription architecture must distinguish at least:

1. **Commitment period** — how long the Customer or Chef has contractually committed, subject to applicable law and accepted termination terms;
2. **Billing period** — how frequently funding or Payment is requested; and
3. **Entitlement period** — the period over which included meals, hours, credits, or governed units are granted and tracked.

The periods are independently configured by approved offer and Platform policy. They must not be assumed equal.

For example:

```text
12-month commitment
+ monthly billing
+ monthly entitlement cycle
```

is valid. A long commitment does not imply long-horizon prepayment.

For launch architecture, a long commitment must not require annual full prepayment. Shorter recurring billing periods are supported. Typical configurable cadences may include `WEEKLY` where explicitly enabled, `BIWEEKLY`, `MONTHLY`, or a governed equivalent. Exact availability is product/policy configuration, not a hard-coded universal list in this ADR.

### 6. Commitment and Renewal Are Separate

```text
COMMITMENT PERIOD != AUTO-RENEWAL POLICY
```

A subscription can separately define:

- a fixed commitment duration;
- renewal behavior;
- notice or acceptance requirements; and
- early or end-of-term termination rules.

Conceptual renewal policies may include `AUTO_RENEW`, `MANUAL_RENEW`, and `FIXED_END` where product policy permits them. The end of a commitment does not imply automatic renewal. Renewal cannot be inferred solely from the presence or duration of a commitment.

### 7. Versioned Offers and Historical Accepted Terms

Both subscription products require versioned offers. A live agreement retains or references the accepted offer/version and enough policy evidence to explain its protected contractual and commercial terms.

Material changes include, where applicable:

- price and currency;
- included meals, hours, credits, or booking units;
- billing and entitlement cadence;
- commitment and renewal behavior;
- pause, cancellation, and termination policy;
- rollover and expiry policy;
- meal selection or substitution rules;
- service or booking windows;
- eligibility and scope;
- included Equipment treatment;
- cleaning-time commercial treatment; and
- other terms whose change affects subscriber rights or provider obligations.

New offer versions apply prospectively according to Platform policy, notice, acceptance, renewal, and protected-period rules. Current offer configuration must not silently rewrite historical accepted agreements, cycles, occurrences, bookings, or policy outcomes.

### 8. Offer Governance Lifecycle

An offer supports a conceptual governance/availability lifecycle equivalent to:

```text
DRAFT
PENDING_APPROVAL
ACTIVE
PAUSED
REJECTED
RETIRED
```

Equivalent final names may be chosen in canonical contract work, and not every transition is required for every provider workflow. This lifecycle describes offer governance and availability for enrollment; it is not the subscriber agreement lifecycle.

```text
STOP NEW ENROLLMENT != TERMINATE EXISTING SUBSCRIBERS
RETIRED OFFER != TERMINATED EXISTING AGREEMENT
```

Pausing or retiring an offer prevents or governs prospective enrollment according to policy. It does not automatically terminate accepted agreements. Provider termination of existing agreements requires an explicit protected workflow and remediation policy.

### 9. Platform Subscription Policy

A `PlatformSubscriptionPolicy` or equivalent governed policy capability may control cross-cutting limits such as:

- supported billing cadences;
- maximum prepaid horizon;
- maximum or permitted commitment duration;
- rollover limits;
- grace-period bounds;
- pause eligibility and frequency;
- allowed early-termination policy families;
- material-change notice/acceptance requirements;
- offer approval or eligibility;
- enrollment caps and capacity-risk safeguards; and
- when new enrollment or future hard confirmation must be suspended.

This policy capability is not a universal Subscription aggregate. This ADR does not finalize its table or schema.

### 10. Shared Entitlement Principle

An entitlement represents a contractual right within a defined entitlement period. Depending on domain policy, accounting must be capable of distinguishing semantics equivalent to:

```text
AVAILABLE
RESERVED
CONSUMED
RELEASED
EXPIRED
```

These are shared accounting meanings, not a mandatory identical physical state machine for both products.

An entitlement is not by itself:

- a confirmed MealFulfillmentOccurrence;
- a Food Order;
- a Kitchen Space reservation;
- an Equipment reservation;
- provider earning;
- payout eligibility; or
- cash or Customer withdrawable credit.

### 11. Entitlement Double-Spend Protection

Entitlement reservation and consumption must be database-safe. Two concurrent requests must not reserve or consume the same remaining entitlement twice.

Correctness must not depend solely on:

- UI checks;
- Redis or an in-memory balance;
- an application read-before-write sequence;
- advisory locks; or
- Payment-provider state.

The exact database representation may differ by domain. It may use deterministic row locking, database-enforced constraints, serialized balance mutation, append-only allocation records with enforceable capacity, or another approved database-safe mechanism appropriate to the final model.

For subscription-funded KitchenBookings, Accepted ADR-007 remains authoritative for coherent entitlement and physical-capacity coordination. ADR-019 does not redefine its Space exclusion, Equipment locking, atomic booking, or replacement correctness mechanisms.

### 12. Unused Entitlement and Rollover

Default architecture may allow unused entitlement to expire at the end of the applicable entitlement period.

```text
UNUSED ENTITLEMENT != CASH
```

Unused entitlement does not automatically become:

- a cash balance;
- withdrawable Customer or Chef credit;
- perpetual entitlement; or
- a promise of an arbitrary future slot.

An offer may permit bounded rollover. When enabled, rollover must be explicit, governed, quantitatively bounded, and subject to deterministic expiration and consumption-order rules. The system must preserve enough old-cycle identity and history to explain where rolled entitlement originated and when it expires. Indefinite accumulating balances are not the default.

Exact rollover algorithms, limits, and ordering remain offer/policy and later implementation design.

## Recurring Rules / Materialization

### 13. Recurring Intent Is Not Capacity

A recurring rule expresses Customer, Chef, or provider scheduling intent. It is not a concrete service occurrence and does not reserve physical capacity forever.

```text
RECURRING RULE != MATERIALIZED OCCURRENCE
```

Changing a recurring rule affects future materialization and already-materialized future occurrences only according to the accepted modification policy. It must not silently rewrite completed, cancelled, expired, or otherwise historical occurrences.

### 14. Bounded Materialization Horizon

The system must not generate infinite future rows or reservations. Materialization uses a bounded configurable horizon and/or occurrence limit. A horizon-extension process may materialize additional future occurrences over time.

Bounded materialization avoids:

- indefinite scarce-capacity reservation;
- unbounded transactions;
- excessive persistence;
- stale far-future assumptions;
- applying obsolete offer, timezone, eligibility, or capacity configuration indefinitely; and
- treating projected renewal as funded/accepted service.

The architecture does not require one giant transaction spanning months of occurrences. Materialization must be idempotent and use stable recurrence/occurrence identity or equivalent duplicate prevention. Exact batch size, horizon, retry, and scheduler design remain implementation policy.

### 15. Domain-Specific Occurrences

Cheffy Bites will not introduce one universal `SubscriptionOccurrence` aggregate.

For Meal Subscription, the domain-specific planned service occurrence is `MealFulfillmentOccurrence`.

For Kitchen Subscription, the concrete physical reservation remains the ordinary `KitchenBooking` governed by ADR-007. A recurring Kitchen-subscription rule materializes or requests ordinary KitchenBookings. It does not create a parallel subscription-booking object that becomes a second authority for physical capacity.

### 16. Timezone and Materialized Instants

ADR-011 remains the temporal decision consumed by this Proposed ADR; ADR-011's status is unchanged.

Recurring subscription rules that express local civil time must retain an explicit authoritative IANA timezone context. Server, JVM, browser, device, and numeric-offset defaults are not recurrence authority.

Concrete MealFulfillmentOccurrence windows and KitchenBooking intervals are resolved into real instants. Daylight-saving gaps and overlaps follow ADR-011's explicit, deterministic resolution requirements. An ambiguous or nonexistent local time must not be silently guessed through a library default.

Once materialized, historical concrete instants are not silently rewritten because the applicable Kitchen, provider, offer, browser, or scheduling timezone later changes. Future unmaterialized recurrence follows the owning effective configuration and accepted modification/versioning policy.

## Customer Meal Subscription

### 17. Meal Domain Model

The Customer Meal Subscription domain preserves:

```text
ChefMealPlan
    ↓ commercial subscription terms
MealSubscriptionOffer
    ↓ Customer acceptance
MealSubscription
    ↓ entitlement and bounded materialization
MealFulfillmentOccurrence
    ↓ when ready for concrete food execution
ordinary Food Order
```

These are distinct concepts. `MealSubscription` is not a giant, long-lived Food Order. One MealSubscription may generate many MealFulfillmentOccurrences over time, and qualifying concrete occurrences may later generate or link to ordinary Food Orders.

### 18. ChefMealPlan Boundary

`ChefMealPlan` is the Chef-owned catalog/service definition describing meal composition and permitted Customer choice. It may compose existing FoodListings rather than duplicating food entities.

Supported composition may include conceptually:

```text
FIXED
SELECTABLE
```

and future `ROTATING` or equivalent behavior where approved.

```text
DIETITIAN MEAL PLAN != CHEF MEAL PLAN
```

`DietitianMealPlan` remains private professional guidance under its owning domain. It is not a Chef-owned commercial plan, subscription offer, subscription agreement, Promotion attribution source, or commission source.

This ADR does not design exact ChefMealPlan persistence.

### 19. MealSubscriptionOffer

`MealSubscriptionOffer` contains versioned commercial subscription terms for an applicable ChefMealPlan/version. Conceptual terms may include:

- applicable ChefMealPlan/version;
- billing cadence;
- entitlement cadence;
- meals or governed credits per entitlement period;
- commitment and renewal terms;
- price and currency;
- selection and no-selection rules;
- scheduling/service windows and cutoffs;
- pause policy;
- rollover and expiry policy;
- occurrence cancellation policy;
- agreement termination policy;
- enrollment availability and eligibility; and
- Promotion eligibility context.

This list describes required meanings, not final columns.

### 20. MealSubscription Agreement

`MealSubscription` is the Customer's accepted commercial agreement. It retains or references:

- the accepted MealSubscriptionOffer/version;
- applicable Customer and provider context;
- agreement start and applicable commitment/renewal terms;
- accepted policy evidence; and
- enough durable context to explain later billing, entitlement, pause, termination, and occurrence decisions.

Historical terms must not be derived solely from the latest MealSubscriptionOffer. MealSubscription lifecycle state must not be replaced by Payment, occurrence, or Food Order state.

### 21. Meal Entitlement

Each Meal entitlement period grants a defined quantity, such as `N meals per period`, or equivalent governed credits.

Meal selection, reservation, release, expiration, and consumption must not double-spend that period's entitlement. Requested selection does not by itself consume entitlement irreversibly; the accepted occurrence and cancellation policy determines when reservation becomes consumption and when release/restoration applies.

The exact entitlement-cycle aggregate, balance, allocation, or event representation remains later ERD work.

### 22. Meal Selection and No-Selection Policy

The Customer may select meals according to the accepted ChefMealPlan and MealSubscriptionOffer. A period may support:

- explicit Customer selection;
- accepted plan-default selection; or
- a deterministic no-selection outcome.

The offer must define no-selection behavior, conceptually including an allowed Chef/plan default, skipping or expiry of the occurrence, requiring selection, or contacting the Customer through a governed workflow.

```text
REQUESTED SELECTION != CONFIRMED FULFILLMENT
```

Where selection matters, the provider must not silently substitute a materially different meal outside accepted plan terms or without required Customer authorization.

### 23. Meal Service Windows

Meal fulfillment may use Platform- or Chef-defined service windows. The model supports:

- Customer preference;
- plan/offer default;
- cutoff and override rules;
- requested window; and
- confirmed window.

A provider estimate is not automatically the accepted Customer service window. A materially different confirmed window requires the applicable policy and Customer approval. Concrete windows are materialized into real instants under ADR-011.

### 24. MealFulfillmentOccurrence Lifecycle

`MealFulfillmentOccurrence` is one concrete planned meal-service occurrence under a MealSubscription. Its conceptual lifecycle must be capable of representing at least:

```text
REQUESTED
PENDING_CHEF
PENDING_KITCHEN_CAPACITY
CONFIRMED
DECLINED
CANCELLED
EXPIRED
```

Equivalent names and narrowly necessary operational states may be selected later. Capacity loss after confirmation may require an explicit `CAPACITY_AT_RISK` or equivalent remediation condition, but this ADR does not finalize that state.

```text
REQUESTED != CONFIRMED
```

This occurrence lifecycle is Meal-domain specific. It is not a universal subscription lifecycle, Payment lifecycle, Food Order lifecycle, or KitchenBooking lifecycle.

### 25. Meal Capacity Before Confirmation

A future MealFulfillmentOccurrence may exist and record Customer demand before required Kitchen capacity is secured.

```text
FUTURE REQUEST MAY EXIST WITHOUT KITCHEN CAPACITY
```

However:

```text
CONCRETE FOOD FULFILLMENT CONFIRMED
requires
VALID CONFIRMED KITCHEN CAPACITY
```

An occurrence must not become `CONFIRMED` merely because entitlement is available, the Customer selected a meal, a Promotion qualified, or billing succeeded. Confirmation requires the applicable Chef/provider acceptance, service eligibility, deadline, and valid confirmed Kitchen capacity for actual production.

If required capacity is not secured by the applicable deadline, the occurrence is declined, expires, or enters an approved remediation path without misclassifying provider/capacity failure as Customer cancellation.

### 26. MealFulfillmentOccurrence to Food Order

When a MealFulfillmentOccurrence is ready for concrete food execution, it generates or links to the normal Food Order architecture. No special subscription Food Order architecture is introduced.

The concrete Order preserves:

- exactly one physical Kitchen;
- actual Chef performer identity through ChefOrderGroup;
- ordinary OrderItem relationships;
- ordinary PICKUP or DELIVERY fulfillment under ADR-005;
- ordinary Pricing and Promotion boundaries;
- ordinary Payment/Refund/Financial traceability; and
- traceability back to the MealSubscription, entitlement context, and MealFulfillmentOccurrence where required.

The MealFulfillmentOccurrence and subscription do not own ChefOrderGroup. A pre-Order requested, pending, declined, cancelled, or expired occurrence does not create a ChefOrderGroup merely because it could have led to fulfillment.

### 27. One Kitchen per Occurrence Order

Every concrete Food Order produced for a subscription occurrence remains bound to exactly one physical Kitchen. Multiple actual Chefs may participate only through separate ChefOrderGroups at that same Kitchen.

A MealSubscription is not permanently required to represent one physical Kitchen across its lifetime unless explicit accepted product terms require that restriction. Valid service capacity may resolve a Kitchen for each concrete fulfillment. Different occurrences may use different eligible Kitchens, but one concrete Order cannot span those Kitchens.

The one-Kitchen-per-Order invariant is not weakened by subscription origin.

### 28. Organization and Actual Chef Boundary

Meal Subscription supports Organization-operated supply while preserving:

```text
SERVICE PERFORMER
!= COMMERCIAL PROVIDER
!= SETTLEMENT BENEFICIARY
```

The actual Chef performer for a concrete Food Order remains captured through ADR-013's ChefOrderGroup boundary. A future commercial provider or settlement beneficiary may be an Organization under ADR-020. The Subscription agreement does not replace performer identity, and neither MealSubscription nor ChefOrderGroup requires its own connected account.

Cheffy Operations uses the same ordinary Organization and professional authorization model as any qualified provider. No Cheffy-specific subscription type or conditional branch is introduced.

### 29. Meal Service-Performance Semantics

```text
MEAL SUBSCRIPTION BILLING-CYCLE PAYMENT
!= ALL FUTURE MEALS PERFORMED
```

The Meal provider's service-performance source is generally the individual fulfilled MealFulfillmentOccurrence and resulting completed food fulfillment under approved policy.

```text
MEAL SUBSCRIPTION FUNDING != MEAL OCCURRENCE FULFILLMENT
```

This distinction allows future ADR-020 to classify funded billing, unfulfilled value, occurrence-level performance, refunds/remediation, earning recognition, and payout eligibility without treating collection as complete service delivery. ADR-019 creates no ledger posting and decides no earning amount or date.

### 30. Meal Provider Cancellation or Nonperformance

If the Chef/provider cannot supply a confirmed occurrence:

- the Customer does not lose the applicable meal entitlement solely because of provider nonperformance;
- reserved entitlement is released/restored or otherwise protected according to the accepted policy;
- the occurrence records provider/capacity nonperformance distinctly from Customer cancellation;
- the Customer may require a replacement, extension, refund, credit, or other approved remediation;
- the provider receives no service-performance credit for an unprovided meal; and
- Promotion restoration may apply under ADR-014.

ADR-019 owns the occurrence and entitlement classification. ADR-012 owns Refund orchestration. ADR-020 owns the commercial and earning/remediation consequences.

### 31. Meal Customer Cancellation and Voluntary Non-Use

```text
CANCEL ONE MEAL OCCURRENCE != TERMINATE MEAL SUBSCRIPTION
```

Customer cancellation of one occurrence follows the occurrence cancellation policy accepted for that agreement/occurrence. Entitlement release, restoration, partial consumption, or forfeiture depends on the captured timing and policy. Current offer settings must not determine the historical result.

Customer voluntary non-use, missed selection, or late cancellation is distinguishable from provider failure. The exact entitlement and commercial consequence remains policy-driven; ADR-019 supplies the classification and entitlement outcome, while ADR-020 determines Financial consequences.

### 32. Meal Provider Suspension

Provider/Chef or Organization suspension is not Customer pause. If the provider becomes unavailable, ineligible, or suspended, future occurrence handling follows provider-nonperformance, reassignment where approved, suspension, and remediation policy.

```text
PROVIDER SUSPENSION != CUSTOMER PAUSE
```

The system must not classify provider inability as a voluntary Customer decision or use it to impose Customer pause/termination penalties.

## Chef Kitchen Subscription

### 33. Kitchen Domain Model

The Chef Kitchen Subscription domain preserves:

```text
KitchenSubscriptionOffer
    ↓ Chef acceptance
ChefKitchenSubscription
    ↓ grants periodic quantity
KitchenEntitlementCycle
    ↓ funds/authorizes request
ordinary KitchenBooking
```

The Kitchen Subscription is a Chef/business-side product. It is not a Customer Meal Subscription and does not create a Customer Kitchen Subscription product.

### 34. KitchenSubscriptionOffer and Operator

`KitchenSubscriptionOffer` is the authorized Kitchen commercial/operating party's versioned plan. The authorized operator need not be the real-estate owner.

```text
KITCHEN PROPERTY OWNER
!= KITCHEN COMMERCIAL / OPERATING ORGANIZATION
```

This ADR does not design landlord, lease, property-owner payment, or real-estate accounting.

Conceptual offer terms may include:

- eligible Kitchen and Space scope;
- hours, credits, or governed booking units per entitlement cycle;
- billing, entitlement, commitment, and renewal cadence;
- price and currency;
- booking approval mode;
- booking horizon and recurrence policy;
- cancellation and entitlement-restoration policy;
- pause and termination terms;
- rollover/expiry rules;
- included Equipment entitlement, if any;
- cleaning-time commercial treatment;
- enrollment/capacity-risk limits; and
- Promotion eligibility context.

This list is not a final column design.

### 35. ChefKitchenSubscription Agreement

`ChefKitchenSubscription` is the Chef's accepted agreement. It retains or references the accepted KitchenSubscriptionOffer/version and enough policy evidence to explain agreement, entitlement, booking, pause, billing-failure, termination, and provider-failure outcomes.

Current KitchenSubscriptionOffer configuration must not silently rewrite accepted protected terms or existing KitchenBookings.

### 36. Explicit Kitchen Scope

A Kitchen Subscription has an explicit physical/operational scope. Depending on accepted offer design, it may apply to:

- one physical Kitchen;
- qualifying equivalent Spaces within one Kitchen; or
- a governed Space class within one Kitchen.

It does not imply access to every Kitchen operated by an Organization unless an explicitly approved offer says so. One concrete KitchenBooking cannot span physical Kitchens.

Future weighted Space credits or cross-Kitchen entitlement require explicit approved product and persistence design; they are not inferred here.

### 37. Kitchen Entitlement Versus Capacity

`KitchenEntitlementCycle` grants a defined amount such as hours, credits, or governed booking units.

Entitlement answers:

> How much contractual booking right remains in this entitlement cycle?

It does not answer:

> Is an eligible Space and required Equipment available at the requested time?

```text
KITCHEN ENTITLEMENT != KITCHEN BOOKING
ENTITLEMENT != PHYSICAL CAPACITY
KITCHEN SUBSCRIPTION != GUARANTEED ARBITRARY CALENDAR SLOT
```

A Chef may possess sufficient entitlement and still fail to obtain a requested slot because capacity, operating hours, policy, Equipment, or approval requirements are not satisfied. ADR-007 remains authoritative for actual Space and Equipment capacity.

### 38. Subscription-Funded Booking Confirmation

A subscription-funded KitchenBooking must coherently satisfy:

- active and eligible ChefKitchenSubscription/agreement state;
- an applicable funded/valid entitlement cycle where funding is required;
- enough remaining entitlement;
- eligible Kitchen/Space scope;
- Space capacity;
- EquipmentRental capacity where requested;
- operating hours and blackout rules;
- cleaning occupancy;
- booking lead time and duration rules;
- applicable approval requirements; and
- hold/expiry and confirmation policy.

The subscription does not bypass ADR-007. The local reservation operation must not commit contradictory partial entitlement, Space, Equipment, or KitchenBooking state.

### 39. Booking Approval Modes

An offer may support policy modes equivalent to:

```text
AUTO_CONFIRM_IF_AVAILABLE
ENTREPRENEUR_APPROVAL_REQUIRED
```

An approval request is not confirmed capacity and must not consume entitlement permanently. It may reserve entitlement and capacity temporarily under the applicable hold policy.

If approval is denied or expires, the proposed entitlement reservation and capacity hold are released coherently. The original agreement and unrelated bookings remain unaffected.

### 40. Equipment Boundary

Kitchen Space entitlement does not automatically include all rentable Equipment. The accepted KitchenSubscriptionOffer must explicitly identify any included Equipment entitlement and its limits.

Otherwise, Equipment remains independently reserved and priced through the existing Equipment Rental/Booking architecture. `EquipmentCatalogItem` remains reference data, while actual rentable Equipment capacity remains governed through ADR-007's EquipmentRental model.

### 41. Cleaning Occupancy and Entitlement Charge

```text
PHYSICAL CLEANING OCCUPANCY
!= COMMERCIAL ENTITLEMENT CHARGE
```

Required cleaning time may block physical Space and Equipment capacity under ADR-007. Whether that time consumes subscription hours/credits, is included, or is separately charged must be explicit in the accepted KitchenSubscriptionOffer.

Commercial treatment must not shorten the physical occupancy interval, and physical occupancy alone must not invent an entitlement charge.

### 42. Recurring Kitchen Booking Pattern

A Chef may request a recurring booking pattern under a Kitchen Subscription. The recurring rule is not a permanent reservation. Only bounded, materialized ordinary KitchenBookings may reserve capacity.

Every concrete occurrence independently satisfies ADR-007's Space, cleaning, Equipment, hold/confirmation, entitlement, and concurrency protections.

Accepted recurrence-request outcomes include conceptually:

- **ALL_OR_NOTHING:** the bounded request/materialization operation satisfies its accepted all-or-nothing outcome policy and does not leave unintended partial accepted results; and
- **BEST_AVAILABLE:** individual concrete occurrences may independently succeed or fail, while every successful booking receives complete ADR-007 protection.

This is not implemented as one giant long-lived transaction across the full recurrence horizon.

### 43. Recurring Modification Scope

Recurring Kitchen modifications support concepts equivalent to:

```text
THIS_OCCURRENCE
THIS_AND_FUTURE
```

`THIS_OCCURRENCE` affects the selected materialized KitchenBooking. `THIS_AND_FUTURE` may update the recurrence rule and reconcile already-materialized eligible future bookings under policy. Completed or past bookings remain unchanged, and history is not deleted.

Each affected concrete booking follows ADR-007's replacement/cancellation protections. A recurring-rule edit does not directly overwrite capacity records.

### 44. Booking Replacement and Entitlement Delta

ADR-007's entitlement-delta replacement semantics remain authoritative. Conceptually, within the same entitlement cycle:

```text
4 hours -> 4 hours
additional entitlement requirement = 0

4 hours -> 6 hours
additional entitlement requirement = +2

4 hours -> 3 hours
appropriate entitlement release = 1
```

The exact release remains subject to accepted cancellation/credit policy.

The original valid booking, Space occupancy, Equipment allocations, and entitlement allocation remain protected until the replacement is safe. A failed replacement leaves the original unchanged and creates no orphan entitlement or capacity reservation. A simple replacement must not double-charge the full new duration.

### 45. Cross-Cycle Booking Modification

When a modification moves a booking between entitlement cycles:

- old-cycle release, restoration, forfeiture, or other consequence follows the old cycle's captured policy/state; and
- new-cycle entitlement is independently validated and reserved under the new cycle's policy/state.

Old-cycle entitlement is not a global undifferentiated balance and must not be borrowed to bypass new-cycle rules unless an explicitly approved rollover/transfer policy allows it. If the new cycle lacks entitlement or capacity, the original booking remains protected under ADR-007.

### 46. Kitchen Operator Cancellation

If the Kitchen operator/provider cancels or cannot provide confirmed capacity:

- the Chef does not lose the applicable entitlement because of provider failure;
- entitlement is released/restored or otherwise protected according to policy;
- no provider-caused cancellation penalty is imposed on the Chef;
- affected Space and Equipment capacity are released coherently;
- dependent MealFulfillmentOccurrences may enter capacity-risk/remediation handling; and
- Financial remediation may be required.

ADR-019 owns the entitlement/booking-service classification. ADR-020 owns commercial consequences, and ADR-012 owns any Refund orchestration.

### 47. Voluntary Kitchen Non-Use Versus Operator Nonperformance

Kitchen Subscription performance differs critically from Meal Subscription performance.

The Kitchen commercial service can include making agreed qualifying access/capacity entitlement available during the entitlement period. Therefore:

```text
VOLUNTARY UNUSED KITCHEN ENTITLEMENT
!= KITCHEN OPERATOR FAILED TO PERFORM
```

When accepted access/capacity obligations were genuinely made available, voluntary non-use may still represent provider performance for future commercial-recognition purposes. The entitlement may expire according to accepted policy without proving operator failure.

Separately:

```text
VOLUNTARY CHEF NON-USE
!= OPERATOR INABILITY TO SUPPLY PROMISED ACCESS/CAPACITY
```

Operator inability may require entitlement restoration, extension, replacement capacity, credit, refund, or another approved remediation. ADR-019 classifies the service outcome. ADR-020 determines exact earning and remediation economics.

### 48. Kitchen Subscription Performance Semantics

Kitchen-provider performance is not necessarily measured solely by occupied KitchenBooking hours. It may be satisfied by making the agreed qualifying access/capacity entitlement available under the accepted plan, even if the Chef voluntarily does not use all entitlement.

This ADR does not determine an earning date, recognition allocation, refund amount, or ledger entry. It provides ADR-020 with the distinction among access/capacity made available, entitlement reserved, booking confirmed, voluntary non-use, and operator inability.

### 49. Kitchen Subscription Capacity Risk

KitchenSubscriptionOffers must not intentionally oversell promised access in a way the operating model cannot reasonably support. The architecture must permit:

- enrollment caps;
- plan availability controls;
- per-Kitchen capacity-risk limits;
- suspension or pause of new enrollment;
- suspension of future hard confirmations where required;
- operational utilization and failure monitoring; and
- protected handling of existing agreements.

Exact capacity-risk algorithms are policy and implementation work. An enrollment cap does not permanently reserve a private calendar slot for every subscriber.

## Billing / Entitlement / Commitment Boundaries

### 50. Subscription Billing Is a Non-Order Context

Meal Subscription billing cycles and Kitchen Subscription billing cycles are approved non-Order billable contexts.

Cheffy Bites must not create fake Food Orders merely to charge either subscription product. ADR-012 remains the Payment, PaymentAttempt, provider interaction, idempotency, and Refund-orchestration boundary.

Payment-provider state is not entitlement source of truth. The owning subscription domain reacts idempotently to authoritative Payment outcomes while retaining authority over agreement, entitlement, occurrence, booking-funding, grace, suspension, and termination state.

### 51. Funding Is Not Performance or Settlement

```text
SUBSCRIPTION BILLING PAYMENT RECEIVED
!= PROVIDER EARNING RECOGNIZED
```

and:

```text
PAYMENT RECEIVED
!= EARNING RECOGNIZED
!= PAYOUT ELIGIBLE
!= EXTERNAL PAYOUT COMPLETED
```

ADR-019 defines service, entitlement, and occurrence semantics. ADR-020 will define commercial obligations, unfulfilled value, earning recognition, remediation economics, and payout eligibility. ADR-015 owns ledger posting and reconciliation. ADR-012 owns provider-neutral Payment/Refund and approved payout-execution orchestration.

Customer-funded or Chef-funded subscription value that is not yet fulfilled or otherwise performed must use neutral architecture language such as `funded`, `unfulfilled`, `pending service`, or `remaining subscription obligation`. This ADR makes no legal **escrow** claim.

## Payment Failure / Pause / Termination

### 52. Payment-Failure Progression

Subscription agreement behavior must support a conceptual payment-failure progression equivalent to:

```text
ACTIVE
  ↓
PAYMENT_GRACE
  ↓
SUSPENDED_PAYMENT
  ↓
TERMINATED_NONPAYMENT
```

Final UI labels and domain-specific state machines may differ. PaymentAttempt failure alone does not automatically cause all transitions; the owning subscription workflow applies policy idempotently using authoritative Payment outcomes and deadlines.

### 53. Grace and Prior Funded Service

During payment grace:

- already funded and valid prior-cycle services/entitlements are not silently invalidated by a later-cycle failure;
- completed history remains unchanged;
- previously funded confirmed occurrences/bookings retain their captured rules;
- limited soft planning or request activity may be permitted; and
- new hard provider/capacity commitments for an unfunded future cycle are not created beyond policy.

```text
LATER PAYMENT FAILURE
!= INVALIDATION OF PRIOR FUNDED SERVICE
```

The required funding deadline must be explicit and historically explainable.

### 54. No New Hard Confirmation Without Required Funding

If accepted subscription terms require cycle funding before service commitment, an unfunded future occurrence or booking cannot become irreversibly `CONFIRMED`. Soft planning, requested state, pending selection, or other non-capacity/non-performance intent may exist.

For Meal Subscription, failed future-cycle billing must not create new hard meal-fulfillment obligations where funding is required. Previously funded confirmed occurrences retain their existing terms.

For Kitchen Subscription, an unfunded future-cycle reservation must not block scarce Kitchen capacity indefinitely. If funding is not secured by the applicable deadline, its hold/reservation is releasable according to policy. Late funding does not automatically restore a slot allocated to another Chef after valid release.

### 55. Pause

Pause is explicit, governed, and distinct from cancellation and termination. For MVP, the architecture supports whole-cycle pause behavior equivalent to:

```text
PAUSE_BILLING
```

where product policy permits. Pause generally applies prospectively to an eligible future billing/entitlement cycle. It may prevent future cycle generation or future materialization according to policy.

Pause does not silently delete or invalidate:

- completed occurrences;
- confirmed bookings;
- historical entitlement cycles;
- previous billing evidence; or
- already confirmed future service governed by its own cancellation/modification policy.

Arbitrary partial-day proration is not required for MVP.

### 56. Occurrence Cancellation, Pause, and Termination

```text
CANCEL ONE OCCURRENCE
!= PAUSE SUBSCRIPTION
!= TERMINATE SUBSCRIPTION
```

Cancelling one MealFulfillmentOccurrence or KitchenBooking applies that occurrence's captured policy and does not automatically pause or terminate the agreement.

Pause temporarily governs eligible future cycles without ending the agreement.

Termination ends future agreement operation according to accepted policy. It does not delete historical cycles, entitlements, occurrences, bookings, payments, or evidence.

Retiring the offer and suspending the provider are also separate actions from subscriber termination.

### 57. Early Termination Policy

The architecture supports governed early-termination policy families conceptually such as:

```text
NO_PENALTY
FIXED_TERMINATION_FEE
DISCOUNT_RECAPTURE
```

or an approved equivalent. `PAY_ALL_REMAINING_COMMITMENT_VALUE` is not the default architecture.

Commercial termination policy respects a hierarchy equivalent to:

```text
applicable law / regulatory requirement
    ↓
Platform policy
    ↓
approved provider offer
    ↓
Customer/Chef accepted versioned terms
```

The model must preserve which terms were accepted. Current offer settings cannot rewrite historical agreement terms. Exact legal validity, calculation, and amounts remain outside this ADR.

### 58. Provider-Failure Termination

Provider-caused inability to continue a subscription must support penalty-free Customer/Chef exit and applicable remediation according to policy. The subscriber must not be charged an early-termination penalty because the provider can no longer perform.

ADR-019 records provider-failure classification and entitlement/service consequences. ADR-020 owns exact refund, credit, unfulfilled-value, earning, and payout consequences.

### 59. Subscription Modification

Material changes to a live subscription may require:

- a prospective offer version;
- Customer/Chef acceptance;
- notice under Platform and legal policy;
- future-cycle application;
- repricing;
- entitlement recalculation; and
- reconciliation of eligible future materialized occurrences.

Already accepted historical terms, completed cycles, and past occurrences are not silently mutated.

For recurring Kitchen service, `THIS_OCCURRENCE` and `THIS_AND_FUTURE` provide modification scope without deleting history. For Meal service, a Customer selection/window change and an offer/agreement change remain distinct operations.

## Promotion / Pricing Boundary

### 60. Promotion Authority

ADR-006 retains typed Promotion owner/target semantics, and ADR-014 retains Promotion eligibility, advance-booking evaluation, application, compatibility, redemption, restoration, material-change revalidation, and Platform-subsidy behavior.

ADR-019 supplies only typed subscription, cycle, occurrence, and service-outcome context. It does not duplicate the Promotion engine.

For a future/pending occurrence that has not obtained required provider or Kitchen capacity, Promotion qualification, code intent, or reversible reservation may be preserved under ADR-014. The request must not prematurely consume an irreversible redemption solely because it was requested. Provider/capacity failure follows ADR-014's restoration policy.

```text
PLATFORM CUSTOMER SUBSIDY != PLATFORM-FEE WAIVER
```

ADR-019 does not own subsidy accounting, Platform-fee accounting, or provider earning.

### 61. Pricing and Historical Evidence

`PricingSnapshot` remains the canonical immutable Pricing evidence authority. This ADR does not introduce:

- `SubscriptionFinancialSnapshot`;
- `MealSubscriptionFinancialSnapshot`;
- `KitchenSubscriptionFinancialSnapshot`; or
- `FinancialSnapshot`.

Historical subscription economics and policy outcomes must be explainable from accepted offer/version evidence, PricingSnapshot, Promotion evidence, Payment/Financial evidence, and domain occurrence/entitlement history as applicable.

Historical behavior must not be recomputed solely from current offer, Pricing, Promotion, rollover, pause, termination, Kitchen capacity, Chef membership, or Organization membership.

## Financial Boundary / ADR-020 Inputs

### 62. Meal Subscription Inputs for ADR-020

ADR-019 must provide future ADR-020 with typed, durable service evidence sufficient to distinguish:

- billing cycle funded;
- entitlement granted;
- entitlement reserved/released/consumed/expired as applicable;
- occurrence requested;
- occurrence pending Chef or Kitchen capacity;
- occurrence confirmed;
- occurrence fulfilled through qualifying concrete food fulfillment;
- occurrence cancelled and responsible side;
- provider failure or capacity failure;
- Customer voluntary non-use or missed selection;
- subscription paused, suspended, or terminated; and
- funded value remaining unfulfilled.

ADR-019 does not determine the accounting or commercial consequence of those facts.

### 63. Kitchen Subscription Inputs for ADR-020

ADR-019 must provide future ADR-020 with typed, durable service evidence sufficient to distinguish:

- billing cycle funded;
- entitlement granted;
- entitlement reserved;
- entitlement consumed, released, restored, rolled, or expired;
- booking requested/held/confirmed under ADR-007;
- qualifying access/capacity made available;
- Chef voluntary non-use;
- operator inability/nonperformance;
- operator-caused booking cancellation;
- subscription paused, payment-suspended, or terminated; and
- remaining or failed subscription obligation.

Kitchen performance must not be inferred solely from physically occupied booking hours. ADR-020 will determine exact recognition and remediation rules.

### 64. Refund and Remediation Boundary

ADR-019 may define:

- why a service/entitlement outcome occurred; and
- what entitlement release, restoration, expiration, extension, or replacement consequence follows.

ADR-012 owns Refund orchestration. ADR-020 owns commercial refund obligation, unfulfilled value, provider earning, payout eligibility, and remediation economics. ADR-015 owns append-only ledger posting and reconciliation.

No ledger entries, universal Payable, commercial-obligation table, earning table, payout-eligibility table, settlement-beneficiary table, or connected-account route is created by this ADR.

### 65. Organization, Settlement, and Payroll Boundary

```text
SERVICE PERFORMER
!= COMMERCIAL PROVIDER
!= SETTLEMENT BENEFICIARY
```

For Meal Subscription, the actual Chef performer remains captured on concrete Orders. For Kitchen Subscription, the authorized Kitchen operator may be the commercial provider. Future ADR-020 owns exact commercial-provider and beneficiary relationships.

```text
MARKETPLACE SETTLEMENT != EMPLOYEE / CONTRACTOR PAYROLL
```

This ADR introduces no employee wage, salary, commission, bonus, withholding, payroll, contractor invoice, or worker-compensation architecture.

## Dietitian and Appointment Boundaries

### 66. Dietitian Simplification

This ADR does not introduce or reintroduce:

- Dietitian food-sale commission;
- Dietitian Meal Subscription commission;
- Dietitian referral attribution;
- Dietitian-Chef commercial relationship;
- Dietitian deduction from Chef/provider proceeds; or
- Dietitian ownership of ChefMealPlan or MealSubscriptionOffer.

A Customer may use authorized Dietitian guidance for discovery, but the Dietitian does not thereby become a MealSubscription commercial participant.

### 67. Appointment Boundary

ADR-018's Proposed Dietitian engagement and Appointment boundary is preserved; ADR-018's status is unchanged.

```text
APPOINTMENT != SUBSCRIPTION OCCURRENCE
DIETITIAN CLIENT ENGAGEMENT != SUBSCRIPTION
```

This ADR does not turn DietitianClientEngagement, Appointment, ConsultationOffering, or DietitianMealPlan into a subscription. Future professional-care subscription products require a separate explicit product and architecture decision.

## Order and Booking Boundaries

### 68. ChefOrderGroup and Order Fulfillment

ADR-013 remains the proposed owning decision for ChefOrderGroup's actual-performer and concrete-Order boundary; its status is unchanged.

`MealSubscription`, `MealFulfillmentOccurrence`, and Meal entitlement do not own ChefOrderGroup. ChefOrderGroup is tied to the concrete Food Order and actual Chef performer.

ADR-005's proposed Order fulfillment boundary is unchanged. Subscription-origin Food Orders use the normal immutable `PICKUP` or `DELIVERY` fulfillment type and normal Order-level states. Subscription does not add Order fulfillment states.

### 69. Booking Concurrency

Accepted ADR-007 remains authoritative for:

- Kitchen Space occupancy;
- cleaning occupancy;
- EquipmentRental capacity;
- PostgreSQL exclusion/serialization correctness;
- subscription entitlement plus physical booking-capacity coordination;
- holds and confirmation;
- replacement booking entitlement-delta semantics; and
- failure behavior preserving the original booking.

ADR-019 consumes those decisions and does not duplicate or weaken them.

## Events, Outbox, Identifiers, and Deployment

### 70. Lifecycle Events and Transactional Outbox

Important subscription lifecycle and occurrence changes may later produce events conceptually equivalent to:

- Meal Subscription activated;
- Meal fulfillment occurrence confirmed;
- Meal fulfillment occurrence cancelled;
- Kitchen Subscription activated;
- Kitchen entitlement cycle opened;
- KitchenBooking funded by subscription entitlement;
- subscription payment grace started;
- subscription suspended for payment; and
- subscription terminated.

These are examples of semantic signals, not final event names, payloads, aggregate types, publication rules, or contracts. Exact contracts belong to `docs/05-event-contracts.md` and must follow Accepted ADR-016 versioning.

When an event is approved, authoritative local state and its outbox row commit in the same local PostgreSQL transaction under Accepted ADR-009. Consumers are idempotent and retry-safe.

### 71. No Distributed Transaction

The architecture does not require one distributed transaction across PostgreSQL, Payment providers, notifications, delivery, calendars, or other external systems.

Local state and outbox persistence are transactional. External effects use asynchronous/idempotent orchestration, authenticated callbacks where applicable, retries, explicit failure state, and reconciliation.

### 72. Identifiers

ADR-010's Proposed UUIDv7 direction applies when persistence is designed; ADR-010's status is unchanged. This ADR introduces no new identifier strategy.

UUID ordering must not replace explicit business timestamps or be treated as authoritative business chronology.

### 73. Modular Monolith

This architecture remains inside Accepted ADR-001's modular monolith. It introduces no:

- subscription microservice;
- entitlement microservice;
- meal-subscription microservice; or
- kitchen-subscription microservice.

Domain capabilities communicate through explicit in-process application interfaces and selective outbox integration. A separate service may be considered only through a later approved architecture decision based on demonstrated operational need.

## Detailed Invariants

1. `MEAL SUBSCRIPTION != KITCHEN SUBSCRIPTION`.
2. `SUBSCRIPTION != ORDER`.
3. `SUBSCRIPTION OFFER != SUBSCRIPTION AGREEMENT`.
4. `BILLING PERIOD != ENTITLEMENT PERIOD != COMMITMENT PERIOD`.
5. `COMMITMENT != AUTO-RENEW`.
6. A long commitment does not require long-horizon or annual full prepayment.
7. Current offer configuration does not rewrite accepted historical terms.
8. `RETIRED OFFER != TERMINATED EXISTING AGREEMENT`.
9. `ENTITLEMENT != CALENDAR RESERVATION`.
10. `KITCHEN SUBSCRIPTION != GUARANTEED SLOT`.
11. `RECURRING RULE != MATERIALIZED OCCURRENCE`.
12. Recurrence materialization is bounded; no recurring rule reserves infinite future capacity.
13. `REQUESTED != CONFIRMED`.
14. `MEAL BILLING != MEAL FULFILLMENT`.
15. `KITCHEN ENTITLEMENT != KITCHEN BOOKING`.
16. Entitlement reservation/consumption is database-safe and cannot be double-spent concurrently.
17. `UNUSED ENTITLEMENT != CASH`.
18. Rollover is explicit, bounded, deterministic, and historically explainable when enabled.
19. MealFulfillmentOccurrence cannot become confirmed without required valid confirmed Kitchen capacity.
20. A pre-confirmation Meal request may exist without Kitchen capacity.
21. Concrete subscription meal fulfillment uses the ordinary Food Order architecture.
22. Every concrete Food Order remains bound to exactly one physical Kitchen.
23. ChefOrderGroup remains tied to the concrete Order and actual Chef performer.
24. Organization commercial-provider identity does not replace actual Chef performer identity.
25. `VOLUNTARY KITCHEN NON-USE != OPERATOR NONPERFORMANCE`.
26. Kitchen-provider performance is not defined solely by physical usage.
27. Equipment remains independently governed unless the accepted offer explicitly includes Equipment entitlement.
28. Cleaning occupancy does not automatically determine entitlement charge.
29. A failed KitchenBooking replacement leaves the original valid booking protected.
30. Cross-cycle booking modification validates old and new entitlement cycles independently.
31. `PAYMENT RECEIVED != EARNING RECOGNIZED != PAYOUT ELIGIBLE != EXTERNAL PAYOUT COMPLETED`.
32. Payment-provider state is not subscription or entitlement source of truth.
33. Prior funded service is not silently invalidated by later-cycle payment failure.
34. No new hard future confirmation is created without required funding.
35. `CANCEL OCCURRENCE != PAUSE SUBSCRIPTION != TERMINATE SUBSCRIPTION`.
36. Provider suspension is not subscriber pause.
37. Offer retirement is not agreement termination.
38. Provider failure permits protected, penalty-free exit/remediation according to policy.
39. `DIETITIAN MEAL PLAN != CHEF MEAL PLAN`.
40. `APPOINTMENT != SUBSCRIPTION OCCURRENCE`.
41. `MARKETPLACE SETTLEMENT != PAYROLL`.
42. `PLATFORM CUSTOMER SUBSIDY != PLATFORM-FEE WAIVER`.
43. PricingSnapshot remains canonical immutable Pricing evidence; no FinancialSnapshot is introduced.
44. Meal and Kitchen subscription persistence uses typed domain relationships, not unconstrained type-plus-UUID links or generic metadata.
45. No subscription lifecycle state substitutes for Order, KitchenBooking, Payment, Promotion, or Financial state.

## Out of Scope

This Proposed ADR does not decide:

- exact subscription table design;
- exact ERD foreign keys, columns, indexes, constraints, or migration SQL;
- exact API endpoints, fields, errors, OpenAPI schemas, or client types;
- exact event names, payloads, aggregate types, consumers, or publication rules;
- Payment-provider implementation;
- exact Payment cardinality for subscription billing contexts;
- commercial earning-recognition policy or timing;
- payout eligibility or external settlement routing;
- ledger account codes, posting rules, or reconciliation implementation;
- tax, Merchant-of-Record, legal escrow, or legal subscription classification;
- employee/contractor payroll or compensation;
- Dietitian food-sale, referral, or Meal Subscription commission;
- detailed Delivery pricing;
- landlord, lease, property-owner, or Restaurant accounting;
- exact entitlement-balance, allocation, rollover, or expiration schema;
- exact recurrence expression, horizon, batch size, or scheduler implementation;
- exact cancellation windows, grace periods, pause frequency, or early-termination amounts;
- exact enrollment-cap or capacity-risk algorithm;
- a universal Subscription, SubscriptionOccurrence, Entitlement, Payable, Provider, or FinancialSnapshot aggregate; or
- future professional-care subscriptions.

## Consequences

### Positive

- Long commitments do not force long-horizon prepayment.
- Billing, entitlement, and commitment can evolve independently according to approved offers and Platform policy.
- Offer versioning and durable policy evidence keep historical accepted terms explainable.
- Retiring an offer can stop new enrollment without destroying existing agreements.
- Entitlement and physical capacity remain correctly separate.
- Database-safe entitlement accounting prevents concurrent double spend.
- Meal Subscription does not become one giant Food Order.
- Kitchen Subscription does not become one permanent Calendar slot or parallel booking authority.
- Bounded materialization avoids infinite reservations, huge transactions, and stale far-future assumptions.
- Meal and Kitchen products share architecture principles without losing domain-specific semantics.
- Existing Order, ChefOrderGroup, fulfillment, KitchenBooking, Equipment, Payment, Promotion, Pricing, outbox, and Financial boundaries remain reusable.
- Meal billing can be separated from occurrence fulfillment.
- Kitchen access/capacity performance can be separated from voluntary physical non-use.
- Provider failure remains distinguishable from Customer/Chef voluntary non-use.
- Payment grace can protect prior funded service while preventing unfunded future hard commitments.
- ADR-020 receives clean typed service-performance inputs without ADR-019 pre-deciding accounting.
- Organization-operated and Cheffy-operated supply work without erasing actual performers or introducing special branches.

### Negative / Costs

- Multiple explicit commitment, billing, and entitlement periods add conceptual and implementation complexity.
- Separate Meal and Kitchen aggregates require more typed persistence than a universal table.
- Entitlement accounting requires database concurrency design, allocation history, and careful release/restoration rules.
- Bounded occurrence materialization requires idempotent horizon extension, retry, and reconciliation.
- Offer versions and accepted policy evidence require deliberate historical storage.
- Payment grace, suspension, funding deadlines, and termination require explicit workflows.
- Rollover and expiry require deterministic policy and broad edge-case testing.
- Meal selection, service-window confirmation, and provider nonperformance require more occurrence states and evidence.
- Kitchen recurring modification, replacement delta, approval holds, and cross-cycle moves require careful coordination with ADR-007.
- Meal and Kitchen products need different service-performance semantics for future Financial decisions.
- Later ERD work must establish domain-specific typed relationships without polymorphic shortcuts.

## Alternatives Considered / Rejected

### 1. One Universal Subscription Aggregate or Table

Rejected because Meal and Kitchen products have different service obligations, occurrence models, capacity dependencies, and performance semantics. Shared policies do not justify one universal lifecycle or persistence aggregate.

### 2. Subscription Is One Giant Long-Lived Order

Rejected because billing cycles, entitlement cycles, future occurrences, and agreement changes do not fit one Food Order lifecycle. Each qualifying Meal occurrence uses an ordinary concrete Order only when actual fulfillment materializes.

### 3. Kitchen Subscription Is a Permanent Calendar Slot

Rejected because subscription grants governed booking entitlement, not perpetual ownership of a weekly slot. Every concrete booking remains bounded and capacity-checked.

### 4. Entitlement Is Physical Reservation

Rejected because contractual quantity and physical availability answer different questions. A Chef can have entitlement without an available slot, and available capacity does not prove entitlement.

### 5. Recurring Rule Reserves Infinite Future Capacity

Rejected because it would indefinitely consume scarce capacity, generate stale assumptions, and create unbounded persistence/transaction behavior. Materialization is bounded.

### 6. Billing, Entitlement, and Commitment Periods Are Always Equal

Rejected because valid products can combine a long commitment, shorter recurring billing, and independently configured entitlement cadence.

### 7. Long Commitment Requires Annual Full Prepayment

Rejected as the launch default because commitment and funding cadence are separate. Shorter recurring billing limits long-horizon funding exposure and preserves configurable policy.

### 8. Billing Success Means the Provider Earned the Entire Subscription Value

Rejected because collection, service performance, earning recognition, payout eligibility, and external payout are distinct facts.

### 9. Meal Provider Earns All Future Meals Immediately on Billing

Rejected because future meals have not yet been performed. Fulfilled MealFulfillmentOccurrences and their concrete food fulfillment supply performance evidence for ADR-020.

### 10. Kitchen Operator Performs Only When Every Purchased Hour Is Used

Rejected because the accepted service may be making qualifying access/capacity available. Voluntary Chef non-use does not automatically negate provider performance.

### 11. Voluntary Unused Kitchen Entitlement Is Operator Nonperformance

Rejected because voluntary non-use and inability to provide promised access/capacity have different entitlement, remediation, and future earning implications.

### 12. Provider Inability and Subscriber Voluntary Non-Use Are the Same Outcome

Rejected because responsibility and protection differ. Provider inability may restore entitlement and trigger penalty-free remediation; voluntary non-use follows accepted subscriber policy.

### 13. Current Offer Configuration Rewrites Historical Terms

Rejected because accepted agreements and completed decisions must remain auditable. Material changes apply prospectively through versioning, notice, and acceptance policy.

### 14. One Occurrence Cancellation Terminates the Subscription

Rejected because occurrence cancellation and agreement termination have different scope, policy, and historical effects.

### 15. Pause Equals Termination

Rejected because a prospective whole-cycle pause can suspend eligible future billing/materialization without ending the agreement or deleting history.

### 16. Retiring an Offer Terminates Existing Agreements

Rejected because offer availability and accepted agreement lifecycle are distinct. Stopping new enrollment does not erase provider obligations to existing subscribers.

### 17. Failed Future Payment Invalidates Previously Funded Service

Rejected because later-cycle collection failure does not rewrite prior funded cycles, completed service, or confirmed service governed by captured terms.

### 18. Payment Provider State Is Entitlement Source of Truth

Rejected because provider state describes external collection interaction, not domain entitlement, reservation, consumption, release, occurrence, booking, pause, or termination truth.

### 19. Universal Source Type Plus UUID Relationships

Rejected because unconstrained polymorphic references weaken relational integrity, permit dangling/cross-type relationships, and obscure domain ownership. Later ERD work must use typed relationships.

### 20. Dietitian Receives Meal Subscription Food Commission

Rejected because Dietitian professional guidance is not Chef selection, food-sale attribution, referral entitlement, or Meal Subscription commercial participation under the current product baseline.

### 21. Subscription Creates a Special Food Order Architecture

Rejected because ordinary Food Order, one-Kitchen, ChefOrderGroup, PICKUP/DELIVERY, Pricing, Promotion, and Financial architecture already governs concrete food execution.

### 22. Subscription Bypasses ADR-007 Capacity Rules

Rejected because entitlement cannot establish physical availability. Subscription-funded KitchenBookings must satisfy the same Space, cleaning, Equipment, hold, confirmation, and database-concurrency protections.

### 23. One Universal SubscriptionOccurrence Aggregate

Rejected because MealFulfillmentOccurrence is a planned meal-service occurrence while KitchenBooking is an ordinary physical reservation. Their lifecycles and authority are not interchangeable.

### 24. Unused Entitlement Automatically Becomes Cash or Perpetual Credit

Rejected because entitlement is a bounded contractual right, not automatically money. Expiry is permitted by accepted terms, and rollover must be explicit and bounded.

### 25. Pay All Remaining Commitment Value Is the Default Termination Model

Rejected because lawful and product-appropriate termination policies vary. No-penalty, fixed-fee, discount-recapture, and provider-failure protection must remain possible.

### 26. One Distributed Transaction Across Database and Providers

Rejected because external Payment, notification, delivery, and other providers cannot participate reliably in Cheffy's local PostgreSQL transaction. Local state plus outbox and idempotent orchestration are used instead.

## Dependencies / Related ADRs

- **ADR-001 — Modular Monolith First (Accepted):** Meal and Kitchen subscription capabilities remain in the modular monolith; this ADR creates no subscription or entitlement microservice.
- **ADR-005 — Order Fulfillment Type Separation (Proposed):** Concrete subscription-origin Food Orders retain the normal PICKUP/DELIVERY fulfillment boundary; this ADR changes no Order state.
- **ADR-006 — Promotion Targeting Model (Accepted):** Typed MealSubscriptionOffer, KitchenSubscriptionOffer, and related Promotion target/owner semantics remain ADR-006 concerns.
- **ADR-007 — Booking Concurrency Control (Accepted):** Concrete subscription-funded KitchenBooking, Space, cleaning, Equipment, entitlement coordination, and replacement concurrency remain ADR-007 concerns.
- **ADR-009 — Outbox Table Schema (Accepted):** Approved subscription events use the transactional outbox and commit with local domain state.
- **ADR-010 — UUIDv7 Identifier Strategy (Proposed):** Future subscription persistence follows the repository identifier direction without using UUID ordering as chronology.
- **ADR-011 — Timezone Modeling (Proposed):** Local recurrence, explicit IANA context, real materialized instants, DST handling, and historical instant semantics follow ADR-011.
- **ADR-012 — Payment Marketplace Settlement (Proposed):** Payment, PaymentAttempt, provider interaction, idempotency, Refund orchestration, and approved settlement execution remain ADR-012 concerns.
- **ADR-013 — ChefOrderGroup Aggregate + Financial Boundary (Proposed):** Actual Chef performer and ChefOrderGroup remain tied to concrete Food Orders; subscription and occurrence do not own ChefOrderGroup.
- **ADR-014 — Promotion Engine (Proposed):** Promotion evaluation, application, compatibility, redemption, restoration, provider-failure handling, and Platform subsidy semantics remain ADR-014 concerns.
- **ADR-015 — Financial Ledger and Reconciliation (Proposed):** Append-only ledger posting, Financial immutability, and reconciliation remain ADR-015 concerns.
- **ADR-016 — Event Versioning (Accepted):** Every future subscription event contract follows explicit versioning and compatibility rules.
- **ADR-017 — Professional Identity, Credentials and Jurisdiction Eligibility (Proposed):** Actual performer identity, professional-to-Organization authorization, Organization operation, and historical attribution remain ADR-017 concerns.
- **ADR-018 — Dietitian Engagement, Appointment Scheduling and Online Meeting Provisioning (Proposed):** DietitianClientEngagement and Appointment remain outside subscription architecture; this ADR does not turn them into subscription concepts.

No related ADR status is changed by this Proposed ADR.

## Future ADR Dependencies

- **ADR-020 — Commercial Obligations, Earning Recognition and Payable-Source Financial Model:** directly depends on ADR-019. ADR-020 requires the Meal distinction between funding and occurrence fulfillment, and the Kitchen distinction among access/capacity made available, voluntary non-use, and operator inability. ADR-020 will own commercial obligations, earning recognition, unfulfilled value, remediation economics, settlement beneficiaries, and payout eligibility. ADR-019 does not draft those rules.
- **ADR-023 — Verified-Experience Reviews and Reputation:** may later use completed MealFulfillmentOccurrence/resulting Food Order and completed KitchenBooking evidence. ADR-019 supplies materialized service identity and outcome semantics but does not design review eligibility, moderation, scoring, or reputation.

## Implementation / Propagation Notes

This Proposed ADR does not authorize application code, migrations, SQL, API changes, or event-contract changes by itself. After acceptance, implementation planning must:

1. Reconcile the canonical ERD with separate typed Meal and Kitchen offer, agreement, cycle, recurrence, entitlement, and occurrence relationships.
2. Preserve `ChefMealPlan`, `MealSubscriptionOffer`, `MealSubscription`, and `MealFulfillmentOccurrence` separately from `KitchenSubscriptionOffer`, `ChefKitchenSubscription`, `KitchenEntitlementCycle`, and `KitchenBooking`.
3. Avoid a universal Subscription, Entitlement, SubscriptionOccurrence, type-plus-UUID relationship, giant nullable-FK table, or generic metadata relationship.
4. Define versioned offer and accepted-policy evidence without deriving historical terms from current configuration.
5. Design database-safe domain entitlement reservation/consumption/release and prove concurrent requests cannot double-spend entitlement.
6. Compose Kitchen entitlement persistence with ADR-007's accepted Space, Equipment, hold, confirmation, replacement, and deterministic locking/constraint behavior.
7. Define bounded, idempotent recurrence materialization, stable duplicate prevention, horizon extension, and deterministic DST behavior under ADR-011.
8. Define MealFulfillmentOccurrence selection, service-window, provider/capacity, confirmation, cancellation, expiry, and Order-materialization workflows without creating ChefOrderGroup prematurely.
9. Preserve exactly one physical Kitchen for each generated Food Order and normal ADR-005/ADR-013 fulfillment and performer boundaries.
10. Define Kitchen recurrence `ALL_OR_NOTHING`, `BEST_AVAILABLE`, `THIS_OCCURRENCE`, and `THIS_AND_FUTURE` orchestration without one long-lived transaction.
11. Define payment-grace, funding deadline, suspension, pause, and termination workflows that protect prior funded service and release unfunded scarce capacity when required.
12. Integrate PricingSnapshot and ADR-014 Promotion evidence without introducing a FinancialSnapshot or irreversible pending-occurrence redemption.
13. Supply typed service-performance evidence to ADR-020 without creating commercial-obligation, earning, Payable, payout-eligibility, ledger, or connected-account schema in subscription domains.
14. Define approved events in `docs/05-event-contracts.md` only through separate canonical propagation, following ADR-009 and ADR-016.
15. Update `docs/03-database-erd.md`, `docs/04-api-contracts.md`, and `docs/05-event-contracts.md` only through separately approved propagation work.
16. Add real-PostgreSQL concurrency tests, preferably using Testcontainers and independent transactions, for entitlement double-spend and subscription-funded booking coordination.
17. Add tests for bounded recurrence, idempotent horizon extension, DST gaps/overlaps, offer version history, rollover/expiry, grace/suspension, pause/termination, provider failure, and Customer/Chef voluntary non-use.
18. Add tests proving Meal and Kitchen products remain separate, subscription billing creates no fake Food Order, pending Meal occurrences create no ChefOrderGroup, and no Cheffy-specific provider path exists.

At minimum, future tests must prove that concurrent requests cannot consume the same entitlement twice, a recurring rule alone reserves no physical capacity, materialization never creates an unbounded future series, a Meal occurrence cannot confirm without required Kitchen capacity, a generated Food Order retains exactly one Kitchen, failed KitchenBooking replacement preserves the original, cross-cycle moves validate cycles independently, later payment failure does not invalidate prior funded service, and provider failure remains distinguishable from voluntary non-use.
