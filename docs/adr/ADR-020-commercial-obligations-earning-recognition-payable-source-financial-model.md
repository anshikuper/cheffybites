# ADR-020: Commercial Obligations, Earning Recognition and Payable-Source Financial Model

## Status

Proposed

## Context

Cheffy Bites receives money for several distinct commercial contexts: Food Orders, Kitchen Bookings and separately billable Equipment service, Dietitian Appointments, Meal Subscription billing cycles, and Kitchen Subscription billing cycles. The marketplace must explain the economic result of each context without assuming that payment collection proves service performance, that an actual service performer is the commercial provider, or that every source produces an immediate external payout.

The existing architecture deliberately assigns different responsibilities to different decisions:

- ADR-012 owns provider-neutral Payment, PaymentAttempt, PaymentAllocation, Refund, approved Payout execution, and external-provider orchestration.
- ADR-013 owns ChefOrderGroup as the actual-Chef operational and source-traceability boundary inside a concrete Food Order.
- ADR-014 owns Promotion evaluation, benefit calculation, funding semantics, application, redemption, restoration, and immutable Promotion evidence.
- ADR-015 owns balanced ledger posting, immutable POSTED records, compensating entries, and reconciliation.
- ADR-017 owns durable professional identity, professional-to-Organization authorization, credentials, and jurisdiction eligibility.
- ADR-018 owns Dietitian Appointment scheduling and operational outcomes.
- ADR-019 owns Meal and Kitchen Subscription agreements, entitlements, occurrences, and their distinct service-performance semantics.

Those decisions intentionally defer the durable commercial/economic model that answers:

- Who commercially supplied the applicable service?
- Who is the approved settlement beneficiary?
- What funded value exists?
- What service obligation exists?
- What portion has been performed and recognized as earned?
- What portion remains funded but unfulfilled?
- What Platform fee, Platform subsidy, provider-funded Promotion, processor-fee, and tax components exist?
- What is refund- or remediation-eligible?
- What recognized amount is payout-eligible?
- What has actually been paid through an external settlement rail?

Without an explicit model, implementations tend to collapse distinct identities and stages into PaymentAllocation, ChefOrderGroup, PayoutLine, or LedgerEntry. That would make Organization-operated supply incorrect, confuse service performance with payment collection, make subscription economics unauditable, and prevent typed source traceability across non-Order contexts.

This ADR defines the Financial economic architecture. It does not redesign the owning business aggregates, Promotion evaluation, provider orchestration, or ledger mechanics. It does not finalize an exact relational schema, API, event contract, chart of accounts, legal tax position, Merchant-of-Record position, or country/provider settlement policy.

## Decision

Cheffy Bites will maintain first-class, durable Financial commercial obligations and related recognition, remediation, and payout-eligibility facts. These facts will identify the accepted commercial-provider and settlement-beneficiary arrangement and retain typed relationships to the domain evidence that created and performed the obligation.

### Foundational Distinctions

The following are separate economic stages:

```text
PAYMENT RECEIVED
!= COMMERCIAL OBLIGATION CREATED
!= PROVIDER EARNING RECOGNIZED
!= PAYOUT ELIGIBLE
!= EXTERNAL PAYOUT COMPLETED
```

The following are separate identities when the accepted arrangement differs:

```text
SERVICE PERFORMER
!= COMMERCIAL PROVIDER
!= SETTLEMENT BENEFICIARY
```

The following are separate Financial concepts:

```text
PAYMENT ALLOCATION
!= COMMERCIAL OBLIGATION
!= EARNING RECOGNITION
!= PAYOUT LINE
!= LEDGER ENTRY
```

No implementation may collapse those identities or stages merely because they coincide in a simple independent-provider transaction.

### Financial Ownership

The Financial capability owns durable commercial and economic facts required to determine and explain:

- commercial-provider identity for an accepted transaction;
- approved settlement-beneficiary identity and historical arrangement;
- funded value and funding context;
- service obligations and their typed business sources;
- earning recognition and corrections;
- funded unfulfilled value and remaining service obligations;
- Platform-fee, Platform-subsidy, and provider-funded Promotion consequences;
- refundability and remediation economics;
- payout eligibility, holds, revocations, and recovery context; and
- relationship between eligible economic sources and later PayoutLines.

Financial does not own:

- Food Order or ChefOrderGroup preparation and fulfillment state;
- Appointment scheduling, completion, cancellation, or no-show classification;
- Kitchen physical booking or Equipment capacity;
- subscription agreement, entitlement, occurrence, or booking state;
- Promotion eligibility, compatibility, application, or redemption evaluation;
- payment-provider and refund-provider orchestration; or
- ledger balancing, posting finalization, account-code governance, or reconciliation mechanics.

The owning business domains produce authoritative service and outcome evidence. Financial consumes that evidence under accepted commercial terms and determines its economic consequence.

## Identity and Commercial Roles

### Service Performer

The service performer is the actual person or operational actor who prepared or delivered the service where an actual performer is applicable. Examples include the Chef represented by a ChefOrderGroup or the Dietitian identified by an Appointment.

Actual performer identity remains durable for operational accountability, quality/refund traceability, verified-experience eligibility, and reporting. It does not by itself create a marketplace earning or identify a settlement recipient.

```text
SERVICE PERFORMER != COMMERCIAL PROVIDER
```

### Commercial Provider

The **commercial provider** is the approved legal/commercial marketplace party responsible for supplying the applicable service under the accepted commercial arrangement.

Examples include:

- Ravi Foods Organization for food independently supplied by Chef Ravi;
- ABC Food Group for Organization-operated food prepared by Chef Ravi and Chef Maria;
- Dietitian D's independent business for an independently supplied consultation;
- Clinic A for a consultation performed by Dietitian D where legally, professionally, and commercially permitted; and
- the authorized Kitchen operating Organization for a Kitchen Booking or Kitchen Subscription service.

Commercial-provider identity is a transaction-time commercial determination. It must not be inferred merely from current Organization membership, professional engagement, property ownership, ChefOrderGroup identity, Promotion ownership, connected-account configuration, or PaymentAllocation source scope.

### Settlement Beneficiary

The **settlement beneficiary** is the approved party to which marketplace settlement becomes payable under the accepted commercial arrangement.

The commercial provider and settlement beneficiary will often be the same party:

```text
COMMERCIAL PROVIDER = SETTLEMENT BENEFICIARY
```

However, the architecture does not make that an irreversible invariant:

```text
COMMERCIAL PROVIDER != SETTLEMENT BENEFICIARY
where an explicitly authorized commercial arrangement requires it
```

This separation does not permit arbitrary beneficiary redirection. Beneficiary selection must be typed, authorized, governed by accepted commercial/legal policy, and historically retained. A current connected account, free-form identifier, client instruction, or provider callback cannot redefine the beneficiary of an existing obligation.

### Professional and Organization Authorization Boundary

ADR-017 professional-to-Organization authorization establishes that an eligible professional may perform through an Organization. It does not determine the commercial arrangement:

```text
PROFESSIONAL ORGANIZATION AUTHORIZATION
!= COMMERCIAL PROVIDER

PROFESSIONAL ORGANIZATION AUTHORIZATION
!= SETTLEMENT BENEFICIARY
```

An active ProfessionalOrganizationEngagement is relevant authorization evidence. Financial independently consumes the approved transaction-time commercial arrangement. It must not infer that every Organization authorizing a Chef or Dietitian is the provider, beneficiary, Promotion funder, or connected-account holder.

### Organization-Operated Food Supply

Organization-operated food is represented directly as Organization commercial economics rather than worker economics followed by payout redirection:

```text
Order O1 at Kitchen K1
    ├── ChefOrderGroup Ravi  → actual performer Ravi
    └── ChefOrderGroup Maria → actual performer Maria

Commercial provider:      ABC Food Group
Settlement beneficiary:   ABC Food Group, where approved
```

The marketplace commercial obligation belongs to the accepted ABC Food Group provider/beneficiary arrangement. Ravi and Maria remain separately identifiable service performers and source evidence. The system must not first create individual marketplace earnings for Ravi and Maria and then redirect those earnings to ABC.

Multiple ChefOrderGroups may contribute source evidence to one Organization commercial obligation or to coordinated commercial economics for that Organization. They must not force two connected accounts, two settlement beneficiaries, two Payouts, or two provider obligations merely because two actual Chefs performed.

### Cheffy Operations

Cheffy's temporary bootstrap supply uses the same ordinary Organization model:

```text
Service performers:       Cheffy-employed or engaged Chefs
Commercial provider:      Cheffy Operations Organization
Settlement beneficiary:   Cheffy Operations Organization, where approved
```

No `internal_provider` financial type, Cheffy-specific payable, special settlement branch, or `if provider == CHEFFY` rule is introduced. Reducing or ending Cheffy-operated supply must require no Financial redesign.

### Marketplace Settlement and Payroll Boundary

```text
MARKETPLACE SETTLEMENT != EMPLOYEE / CONTRACTOR PAYROLL
```

If ABC Food Group employs or contracts Ravi, the marketplace may owe ABC Food Group. How ABC compensates Ravi is outside marketplace Financial architecture. This ADR does not define or create:

- salary or hourly wages;
- employee sales commission or bonuses;
- contractor compensation or invoices;
- timesheets or pay periods;
- payroll withholding or remittance;
- payroll taxes or employer contributions; or
- employee benefits.

ChefOrderGroup performance data may be an input to a separate lawful payroll or compensation process, but it does not create a worker marketplace earning merely because the Chef performed the service.

## Commercial Obligation Model

### First-Class Commercial Obligation

A `CommercialObligation`, or an equivalently named Financial concept selected during canonical design, is a durable economic obligation arising from an accepted marketplace commercial arrangement and one or more typed business sources.

Conceptually, the obligation must be able to identify or relate to:

- its applicable commercial context and typed source evidence;
- one approved commercial provider;
- one approved settlement beneficiary for the applicable obligation or an explicitly modeled approved arrangement;
- currency;
- contractual/gross service components;
- funding status and funding evidence where applicable;
- accepted Pricing, Promotion, fee, tax, and commercial-policy evidence;
- source-performance state or evidence;
- recognized earned amount and recognition history;
- remaining funded-unfulfilled amount or service obligation;
- Platform-fee, Platform-subsidy, and provider-funded Promotion consequences;
- refund/remediation effects and relationships;
- payout-eligibility decisions and later settlement references; and
- transaction-time provider/beneficiary/terms evidence sufficient for historical explanation.

This is a conceptual requirement. Exact tables, columns, constraints, state fields, and aggregate boundaries remain canonical ERD work.

### Commercial Obligation Is Not a Universal Business Parent

A Financial commercial obligation is not a universal parent aggregate for all commercial domains. The following remain authoritative business aggregates in their owning domains:

```text
Order
KitchenBooking
Appointment
MealSubscription
ChefKitchenSubscription
MealFulfillmentOccurrence
```

They must not become children of a Financial `BusinessTransaction`, universal `Payable`, or universal commerce aggregate. Financial references their typed commercial and service evidence without taking ownership of their lifecycle.

### Component and Effect Separation

One commercial/service source may produce several economic facts, including:

- provider gross service obligation;
- Cheffy Platform fee;
- Platform-funded Customer subsidy;
- provider-funded Customer discount;
- Platform-fee discount or waiver;
- tax component where applicable;
- processor fee learned through provider evidence;
- refund or remediation adjustment;
- earning recognition; and
- payout eligibility.

Therefore:

```text
ONE SOURCE
!= ONE COMMERCIAL EFFECT
!= ONE LEDGER TRANSACTION
!= ONE PAYOUT LINE
```

Posting granularity follows ADR-015 economic atomicity. Settlement grouping follows approved payout policy. Neither is inferred from source cardinality.

### Conceptual Lifecycle

The commercial-obligation model must preserve economic meanings equivalent to:

```text
CREATED
FUNDED, where applicable
UNFULFILLED
PARTIALLY_EARNED, where applicable
EARNED
REMEDIATION_PENDING, where applicable
SETTLEMENT_ELIGIBLE
SETTLED / externally paid evidence, where useful
```

Cancellation, reversal, or correction uses explicit history-preserving and compensating semantics. A finalized or posted historical fact is not destructively rewritten. This list defines required distinctions, not one mandatory physical enum or identical state machine for every product. Separate facts and derived lifecycle views may be more appropriate than one mutable status field.

### Funding Is Not Earning

Customer or other approved payer funding supplies collection/funding evidence. It does not prove provider performance:

```text
FUNDED != EARNED
```

An obligation may be unfunded, funded and unfulfilled, partially earned, fully earned, remediated, settlement-eligible, held, or externally settled according to its evidence and policy. Payment state remains owned by ADR-012.

### Funded Unfulfilled Value

Financial must represent funded value attributable to service not yet performed. Neutral terms include:

```text
FUNDED UNFULFILLED VALUE
REMAINING SERVICE OBLIGATION
```

The model must be able to explain:

```text
Customer funded value X
Provider earned value Y
Remaining unfulfilled value Z
```

This does not assert that the funds are legal **escrow**. The value may later be earned through valid performance, refunded or remediated, carried into an allowed replacement/occurrence, or otherwise resolved according to accepted policy. Partial fulfillment and partial remediation must remain explainable.

Funded unfulfilled value and restored entitlement do not silently create a generic, withdrawable Customer wallet. Any future Customer credit or wallet requires a separate explicit decision.

## Typed Payable Sources

### Meaning of Payable Source

“Payable-source” means that Financial can trace a commercial obligation and later payout eligibility to the actual typed commercial and service evidence that created or performed it. It does not mean that the source aggregate is itself a payable.

```text
CHEFORDERGROUP != PAYABLE
APPOINTMENT != PAYABLE
KITCHENBOOKING != PAYABLE
MEALFULFILLMENTOCCURRENCE != PAYABLE
```

The Financial domain creates and maintains the economic record while retaining typed references to source and performance evidence.

### Typed Relationship Requirement

Canonical commercial-source relationships must use:

- domain-valid source categories;
- typed relationships to approved source families;
- database-enforceable referential integrity where practical;
- explicit cardinality and uniqueness rules; and
- durable historical explainability.

The canonical model must not use an unconstrained relationship such as:

```text
source_type
source_id UUID
```

as the sole authoritative commercial-obligation or payable-source relationship. Controlled ledger source metadata under ADR-015 remains useful for posting correlation, but it does not replace typed CommercialObligation/source relationships.

The later ERD may evaluate typed source-family association tables, explicit source contribution records, domain-specific obligation relationships, or another strongly typed relational strategy. This ADR does not select one universal source registry.

### No Giant Nullable-FK or Metadata Design

The later canonical model must not prescribe one giant CommercialObligation table containing nullable foreign keys for every current and future source. It also must not use `entity_type`, `entity_id`, `attributes JSON`, or `metadata JSON` as the canonical representation of source identity, provider identity, beneficiary identity, or obligation ownership.

JSONB remains appropriate only for genuinely extensible non-relational or provider-specific metadata. It cannot replace core commercial relationships or relational integrity.

### Multiple Sources to One Commercial Result

Several operational facts may contribute to one commercial-provider result. For example, ChefOrderGroup Ravi and ChefOrderGroup Maria may both contribute to food supplied by ABC Food Group. Financial must retain their distinct source contributions while allowing an approved shared Organization provider/beneficiary obligation or coordinated provider economics.

This does not collapse the ChefOrderGroups. It also does not require one obligation representation per actual performer when the accepted commercial provider is shared.

### Source Families

The Financial architecture must support at least these typed source families without converting them into one business aggregate:

- Food Order, ChefOrderGroup, OrderItem, and fulfillment evidence;
- Dietitian Appointment and its accepted offering/outcome evidence;
- KitchenBooking and separately billable Equipment service evidence;
- Meal Subscription billing cycle, entitlement context, MealFulfillmentOccurrence, and resulting concrete Food Order fulfillment;
- Kitchen Subscription billing cycle, entitlement/access-capacity obligation, and related KitchenBooking/provider-nonperformance evidence; and
- Financial remediation and correction facts that reference their original typed economic sources.

Exact physical source relationships and generalized Refund/PayoutLine source links remain deferred to the canonical ERD.

## Earning Recognition

### Definition

**Earning recognition** is the Financial determination that an identified commercial provider has satisfied the accepted service-performance condition for some amount of a commercial obligation.

Recognition requires:

- typed and durable source/performance evidence;
- accepted commercial terms and policy version;
- historical Pricing and Promotion evidence;
- the applicable service outcome;
- correct transaction-time commercial-provider and beneficiary context; and
- currency-consistent amount attribution.

Financial must not recognize earning solely from Payment success, a current configuration join, connected-account readiness, a subjective rating, chat text, or broker message order.

### Incremental Recognition

Recognition may be partial and incremental where the product requires it. One funded billing cycle may cover several future services. Each qualifying performance occurrence may recognize only its attributable amount while the remainder stays funded and unfulfilled.

The allocation method follows accepted offer and Pricing evidence. No universal equal division is assumed. If a PricingSnapshot and accepted offer explicitly define equal economic allocation, that captured method may be used; otherwise naive equal division is prohibited.

### Recognition Correction

Recognized or posted historical facts are not destructively edited. When source evidence is corrected or later remediation changes the economic result, Financial records a new reversal or adjustment fact. ADR-015 posts the accepted correction using a new balanced compensating LedgerTransaction where posting is required.

The original recognition remains auditable. A correction is distinct from duplicate event processing and requires its own authorized reason, source, time, and idempotent operation identity.

### Performance Evidence and Ratings

Financial uses structured domain service facts:

- Food Order and ChefOrderGroup/fulfillment outcomes;
- Appointment outcome;
- MealFulfillmentOccurrence and concrete Food fulfillment;
- Kitchen access/capacity obligation and provider-nonperformance evidence; and
- KitchenBooking/service outcome.

```text
RATING != SERVICE PERFORMANCE FACT
```

ADR-023 may use verified service history for review eligibility, but ratings or average reputation do not establish or negate earning. A dispute or remediation workflow may affect economics only through explicit structured policy and Financial/domain operations.

Chat or Conversation messages are not authoritative Financial source facts. ADR-021 owns Conversation architecture. Financial adjustments must not be inferred from message text. Taxonomy/reference metadata under ADR-022 likewise does not own or establish commercial obligations.

## Food Order Economics

### Source and Performance Model

Food economics preserve:

- one physical Kitchen per concrete Order;
- one logical Payment for the current Food Order checkout model;
- multiple ChefOrderGroups where actual Chefs at that same Kitchen participate;
- actual performer evidence per ChefOrderGroup;
- one commercial provider potentially covering multiple ChefOrderGroups;
- captured OrderItem, Pricing, Promotion, fee, tax, and fulfillment evidence; and
- one pickup or delivery service lane under ADR-005.

Provider earning is based on accepted and performed food fulfillment according to approved Order/service policy. Payment success or ChefOrderGroup creation alone is insufficient. ChefOrderGroup and Order fulfillment facts provide typed performance evidence; Financial owns the resulting provider recognition.

### ChefOrderGroup Boundary

ADR-013 remains authoritative:

```text
CHEFORDERGROUP != COMMERCIAL PROVIDER
CHEFORDERGROUP != SETTLEMENT BENEFICIARY
CHEFORDERGROUP != COMMERCIAL OBLIGATION
CHEFORDERGROUP != PAYOUT
```

ChefOrderGroup remains:

- actual Chef participation evidence;
- Chef preparation and authorization boundary;
- Chef Promotion calculation/reference scope where applicable;
- refund and quality traceability;
- financial source/reference evidence; and
- reporting and analytics boundary.

It does not own Payment, Refund, CommercialObligation, Payout, or Ledger. Referencing ChefOrderGroup from Financial does not transfer Financial ownership into the Order domain.

### Shared Organization Provider

Food prepared by Ravi and Maria for ABC Food Group does not require one earning record, Payout, or connected account per ChefOrderGroup. Financial may preserve each group's source contribution while recognizing the approved commercial economics for ABC Food Group.

An independent Chef arrangement remains ordinary: Ravi is the performer while Ravi Foods Organization may be both commercial provider and beneficiary. Cheffy Operations follows the same pattern. No provider type changes the core Financial flow.

### One Source, Multiple Effects

Food source evidence may produce provider gross service value, provider-funded discount, Platform subsidy, Platform fee, delivery and tax effects, refund/remediation, recognition, and payout eligibility. These effects may occur at different times and may not have one-to-one correspondence with ChefOrderGroups, LedgerTransactions, or PayoutLines.

## Dietitian Appointment Economics

### Non-Order Financial Context

A Dietitian Appointment is an approved non-Order commercial context:

```text
DIETITIAN APPOINTMENT != FOOD ORDER
```

Financial uses a typed Appointment relationship and does not create a fake Order, ChefOrderGroup, or food source to represent consultation economics.

### Performer, Provider, and Beneficiary

The Appointment retains the actual practicing Dietitian under ADR-017 and ADR-018. Where legally, professionally, and commercially permitted, the commercial provider may be:

- an independent Dietitian business;
- a clinic; or
- another approved Organization.

The settlement beneficiary follows the approved arrangement and may coincide with the provider. Organization identity never replaces actual Dietitian performer identity.

### Appointment Outcomes and Earning

ADR-018 supplies structured outcomes, including:

- `COMPLETED` actual professional service;
- Customer cancellation;
- Customer no-show;
- Dietitian/provider cancellation; and
- Dietitian/provider no-show.

A completed Appointment may provide service-performance evidence for recognition under accepted terms. Provider cancellation or provider no-show does not recognize normal consultation earning for unprovided service. Customer cancellation and Customer no-show may have policy-defined commercial consequences, but this ADR does not invent amounts, charge windows, or final policy. Financial uses captured Appointment, offering, cancellation/no-show policy, Pricing, and Promotion evidence.

### No Dietitian Food Commission

Dietitian commercial economics in the current model are professional-service economics only. This ADR does not introduce:

- DietitianChefAssociation;
- Dietitian food-sale commission;
- Dietitian Meal Subscription commission;
- recommendation or referral commission;
- Chef-purchase attribution;
- food recommendation attribution; or
- deduction from Chef or Organization food proceeds.

Reconsidering such a model requires a separate explicit product, professional-regulatory, privacy, Financial, Promotion, and architecture decision.

## Kitchen Booking Economics

KitchenBooking is a non-Food typed commercial source. Provider performance follows the accepted Kitchen service and booking terms and the structured outcome supplied by the Kitchen/booking domain.

The authorized Kitchen operating Organization may be the commercial provider and beneficiary. Property ownership alone does not establish either role:

```text
KITCHEN PROPERTY OWNER
!= KITCHEN OPERATOR
!= SETTLEMENT BENEFICIARY
where applicable
```

This ADR does not design landlord payments, leases, property-owner accounting, or lease liabilities.

Separately billable Equipment may produce distinct typed source contributions and economic components according to existing booking/rental architecture and captured Pricing evidence. Equipment reference metadata does not itself become a commercial obligation.

## Meal Subscription Economics

### Billing and Performance Separation

ADR-019 remains authoritative for Meal Subscription agreement, billing cycle, entitlement, occurrence, and concrete fulfillment semantics.

```text
MEAL SUBSCRIPTION BILLING
!= MEAL SERVICE PERFORMANCE
```

A billing cycle may fund several future MealFulfillmentOccurrences. Billing success creates funding evidence and may create funded service obligations. It does not recognize all future service as earned.

### Incremental Occurrence Recognition

Provider earning is generally recognized from fulfilled MealFulfillmentOccurrence and resulting qualifying concrete Food Order fulfillment evidence. Each fulfilled occurrence may recognize only its attributable economic amount. The remaining amount remains funded and unfulfilled until later performance or remediation.

The attributable amount follows the accepted MealSubscriptionOffer/version, PricingSnapshot, Promotion evidence, and occurrence/Order evidence. Financial must not divide the cycle total equally merely because the cycle contains a known occurrence count unless the accepted pricing model actually establishes equal allocation.

### Meal Funded-Unfulfilled Value

The Financial model must represent:

```text
funded
unfulfilled
partially fulfilled / partially earned
fully fulfilled / fully earned
```

Funded-unfulfilled value is protected as an explainable remaining service obligation. It is not automatically legal escrow, a Customer cash wallet, or immediately provider-earned value.

### Provider Nonperformance

When the Chef/provider cannot supply a confirmed occurrence, ADR-019 supplies entitlement and occurrence failure evidence. Financial must ensure:

- no normal service earning is recognized for unprovided fulfillment;
- affected funded-unfulfilled value remains identifiable;
- refund/remediation economics can be determined;
- Promotion and Platform-subsidy consequences can be reversed, restored, or reallocated according to captured policy; and
- payout eligibility does not arise from failed performance.

A permitted replacement occurrence, entitlement restoration, extension, refund, or other remedy resolves the affected obligation only according to accepted policy and new durable evidence.

### Customer Cancellation and Voluntary Non-Use

Customer-caused cancellation, missed selection, or voluntary non-use remains distinct from provider failure. Financial must not automatically infer:

```text
UNUSED ENTITLEMENT = FULL REFUND
```

or:

```text
UNUSED ENTITLEMENT = PROVIDER FAILURE
```

The economic consequence follows the accepted occurrence cancellation, non-use, entitlement, offer, and consumer-protection policy.

### Billing Failure

A failed future billing cycle does not erase valid prior funded obligations or prior recognized earnings. It does not rewrite completed service history. Future obligations for which required funding was not obtained must not be treated as funded or earned.

```text
LATER BILLING FAILURE
!= REVERSAL OF PRIOR VALID EARNING
```

## Kitchen Subscription Economics

### Distinct Service-Performance Meaning

ADR-019 remains authoritative for Kitchen Subscription agreement, entitlement, access/capacity, booking, voluntary non-use, and operator-nonperformance evidence.

Kitchen Subscription performance is not necessarily measured solely by occupied KitchenBooking hours. The accepted commercial service may be making qualifying access/capacity entitlement available during the agreed period.

```text
VOLUNTARY UNUSED KITCHEN ENTITLEMENT
!= OPERATOR NONPERFORMANCE
```

If the operator genuinely satisfied the accepted access/capacity obligation, provider earning may be recognized according to accepted terms even if the Chef voluntarily used less than the entitlement. This ADR deliberately does not finalize the exact recognition date or allocation method; those require approved commercial and accounting policy consistent with the accepted offer.

### Operator Nonperformance

```text
CHEF VOLUNTARY NON-USE
!= OPERATOR INABILITY TO SUPPLY PROMISED ACCESS/CAPACITY
```

If the operator failed to make promised qualifying capacity/access available, the affected portion may create:

- a remaining unfulfilled obligation;
- entitlement restoration or extension;
- replacement capacity;
- refund, credit, or another remediation;
- reduced or reversed earning recognition; and
- payout ineligibility or hold.

ADR-019 supplies domain evidence. Financial determines the economic consequence from captured terms and policy.

### Unused Entitlement

Expired voluntary unused entitlement is not automatically Customer/Chef cash credit, refund, or provider failure. Its treatment follows the accepted offer and service-performance policy.

```text
UNUSED ENTITLEMENT != CASH
```

The architecture does not create a generic wallet or perpetual cash-equivalent balance from expired Kitchen entitlement.

### Kitchen Booking Relationship

Kitchen Subscription billing is a non-Order funding context. Ordinary KitchenBookings remain the typed physical reservation facts governed by ADR-007. Subscription economics must not force Kitchen Subscription into Meal occurrence-based recognition or assume that every physical booking hour independently defines the entire access/capacity service obligation.

## Platform Fee / Subsidy / Promotion Economics

### Platform Fee

A **Platform fee** is Cheffy's marketplace/service fee charged according to accepted commercial terms. It remains separate from:

- commercial-provider gross service value;
- payment-processor fee;
- Platform-funded Customer subsidy;
- provider-funded Customer discount;
- Platform-fee discount or waiver; and
- taxes.

No hard-coded Platform, Organization, Chef, Dietitian, or subscription fee percentage is established by this ADR. Fee terms are versioned/configurable commercial policy and must remain historically explainable.

### Platform-Fee Recognition

Platform-fee recognition follows accepted fee policy and the applicable commercial performance/refund conditions. Customer Payment receipt is not automatically final Platform-fee earning for every future or unfulfilled service:

```text
CUSTOMER PAYMENT RECEIVED
!= FINAL PLATFORM FEE EARNED IN EVERY CONTEXT
```

Recognition policy may differ by commercial context, but must use captured transaction-time fee terms and service evidence rather than current configuration.

### Processor Fee

The payment-provider or card-processor fee is distinct from Cheffy's Platform fee:

```text
PROCESSOR FEE != PLATFORM FEE
```

Processor cost must not be described as Cheffy marketplace revenue. Provider evidence and ADR-015 reconciliation may establish processor-fee facts. Exact accounting treatment remains Financial/accounting policy.

### Platform Customer Subsidy

A **Platform Customer subsidy** is Platform-funded value that reduces the Customer contribution while preserving approved provider commercial economics according to the Promotion/offer terms.

```text
PLATFORM CUSTOMER SUBSIDY
!= PLATFORM FEE DISCOUNT
!= PLATFORM FEE WAIVER
```

Financial must not silently net subsidy against provider earning. It consumes immutable Promotion/Pricing funding evidence and records the resulting funding, recognition, remediation, and payout consequences. Customer Payment amount alone cannot determine provider gross service economics.

### Provider-Funded Promotion

A provider-funded Promotion reduces provider commercial economics according to accepted Promotion terms. The funding party may be an Organization even when ChefOrderGroup is the calculation scope:

```text
PROMOTION CALCULATION SCOPE
!= PROMOTION FUNDING PARTY
```

For Organization-operated food, ChefOrderGroup Ravi may isolate qualifying items while ABC Food Group funds the discount. Financial must not infer that Ravi personally funds the Promotion or that his payroll changes.

### Promotion Authority

ADR-006 owns typed Promotion owner and target identity. ADR-014 owns applicability, deterministic calculation, benefit type, funding semantics, application, redemption, and restoration evidence. Financial consumes that immutable evidence and determines the resulting economic consequence. It does not re-run Promotion eligibility or maintain a competing Promotion engine.

Promotion evidence remains Promotion-owned. Financial may reference it but must not duplicate it as Financial Promotion state.

### Platform-Fee Benefits

A Platform-fee discount or waiver changes Platform fee economics. It is not necessarily a Customer-price discount and does not silently change provider gross service value:

```text
CUSTOMER PRICE DISCOUNT
!= PLATFORM FEE DISCOUNT
!= PLATFORM FEE WAIVER
```

Each effect requires distinguishable historical evidence.

## Refund / Remediation

### Ownership Boundary

ADR-012 owns provider-neutral Refund orchestration, provider interaction, and Refund lifecycle evidence. ADR-020 owns the commercial/economic determination of:

- refundable amount;
- provider earning reduction or reversal where applicable;
- affected funded-unfulfilled value;
- Platform-fee consequence;
- Platform-subsidy consequence;
- provider-funded Promotion consequence;
- payout-eligibility hold or reversal; and
- credit, replacement, extension, or other remediation economic meaning.

ADR-015 owns any resulting balanced postings and reconciliation. Processor API behavior does not belong to this decision.

### Historical Evidence

Refund and remediation calculations use captured transaction-time evidence. They must not use current:

- menu or FoodListing price;
- ConsultationOffering price or policy;
- Meal or Kitchen Subscription offer;
- Platform fee;
- Promotion or subsidy configuration;
- tax configuration;
- Organization membership;
- professional engagement;
- provider/beneficiary configuration; or
- connected account.

PricingSnapshot remains the canonical immutable commercial-pricing authority. PromotionApplication, PromotionSnapshot, accepted policy/version, and typed service outcomes provide their owning evidence. Financial records resulting economic facts and references without duplicating the full PricingSnapshot.

### No Universal Proportional Refund Rule

Every partial refund is not automatically split proportionally across all historical components. Attribution follows the actual affected:

- OrderItem and ChefOrderGroup/source;
- Appointment outcome;
- Kitchen service or Equipment component;
- MealFulfillmentOccurrence;
- Kitchen entitlement/access-capacity obligation; and
- captured fee, subsidy, tax, and Promotion rules.

Proportional allocation may be valid only when the accepted policy and captured calculation evidence define it for the affected case. It is not a universal invariant.

### Provider and Customer Outcomes

Provider nonperformance and Customer cancellation/non-use remain distinct. Financial uses the structured responsible-side and policy evidence supplied by the owning domain. It must not infer responsibility from Payment status, a meeting-provider failure alone, Conversation text, or a rating.

### Refund After External Payout

The architecture supports refunds and remediation after external payout. It must not rewrite the original Payout or pretend settlement never occurred. Later consequences are represented through new Financial facts, ADR-015 reconciliation and compensating postings, and approved recovery, negative-balance, reserve, or future-settlement policy.

Exact provider-country recovery behavior remains unresolved.

### Earning Reversal and Correction

An earning reversal or correction creates a new explicit economic fact. Where posting is required, ADR-015 creates a new compensating LedgerTransaction. Original CommercialObligation, recognition, Payment, Refund, Payout, and posted ledger history remain auditable.

## Payout Eligibility / Settlement

### Payout Eligibility

**Payout eligibility** is a distinct Financial decision that some recognized earned amount is eligible to be included in external marketplace settlement.

```text
EARNING RECOGNIZED != PAYOUT ELIGIBLE
```

Eligibility may depend on approved policy concerning:

- recognized earning;
- refund/remediation or dispute windows;
- provider risk and reserve policy;
- provider compliance or legal restrictions;
- settlement-account readiness;
- negative provider balance;
- prior recovery obligations; and
- other explicitly approved settlement rules.

This ADR does not finalize country-, provider-, or product-specific waiting periods, reserves, thresholds, cadence, or legal restrictions.

### Eligibility Hold or Revocation

Before external payout, eligibility may be held or revoked because of refund/remediation, dispute, compliance, reserve/risk rules, or corrected earning evidence. Such changes are explicit Financial facts with reason, authority, source, time, and idempotency.

After external payout, the original settlement remains historical truth. Recovery uses new reconciliation, negative-balance, reserve, adjustment, or future-settlement facts rather than eligibility rewriting that erases the Payout.

### Provider Account Readiness

Earning may be recognized even while an external settlement account is temporarily not ready, if approved business/legal policy permits:

```text
EARNING
!= CONNECTED ACCOUNT READY
!= PAYOUT COMPLETED
```

Connected-account provisioning is not the definition of performance or earning. It is a settlement-execution prerequisite where required.

### Payout and Aggregation

ADR-012 and Financial remain owners of Payout/PayoutLine orchestration and provider execution. ADR-020 determines which economic amounts are eligible sources.

A Payout is not required per:

- Chef;
- ChefOrderGroup;
- Order;
- Appointment;
- KitchenBooking;
- subscription billing cycle;
- occurrence; or
- CommercialObligation.

One Payout may aggregate many eligible earned sources according to beneficiary, currency, cadence, reserve, risk, and settlement policy. One Payout cannot combine currencies into one economic amount.

### PayoutLine

A PayoutLine, or equivalent settlement detail, is Financial settlement evidence identifying the eligible commercial/economic source included in a Payout.

```text
PAYOUT LINE != ORIGINAL DOMAIN SOURCE
```

The exact typed PayoutLine-to-eligible-source relationship, uniqueness, and duplicate-settlement prevention remain canonical ERD work.

### Connected Accounts

The architecture does not require one payment-provider connected account per Chef performer, ChefOrderGroup, Dietitian performer, Appointment, or occurrence. Connected-account topology follows the approved provider/beneficiary arrangement plus provider capability, legal, risk, and country requirements.

Stripe Connect remains a likely initial adapter under ADR-012. Stripe-specific account types and transfer topology must not become the core CommercialObligation model.

### Negative Balance and Reserve

The architecture leaves room for approved:

- negative provider balance;
- settlement reserve;
- payout hold;
- post-payout recovery; and
- netting against future eligible settlement.

Exact provider-specific and country-specific mechanics remain unresolved and require later policy, provider, legal, accounting, and persistence decisions.

## PaymentAllocation Boundary

ADR-012 remains authoritative for PaymentAllocation as payment-side logical distribution/reference evidence.

```text
PAYMENT ALLOCATION != COMMERCIAL OBLIGATION
PAYMENT ALLOCATION != EARNING
PAYMENT ALLOCATION != PAYOUT
PAYMENT ALLOCATION != LEDGER ENTRY
```

PaymentAllocation may supply funding and source-contribution evidence. It does not prove service performance, determine settlement beneficiary, establish payout eligibility, classify a ledger account, or instruct an external transfer.

For example, PaymentAllocations may preserve food contribution evidence associated with ChefOrderGroup Ravi and ChefOrderGroup Maria. ADR-020 may determine one ABC Food Group commercial provider/beneficiary result. The PaymentAllocation recipient or source scope is not required to be the final settlement beneficiary.

## Ledger Boundary

ADR-015 remains authoritative for:

- LedgerTransaction and LedgerEntry;
- balanced posting;
- one-currency posting;
- database-enforced POSTED immutability;
- new compensating entries and transactions;
- controlled account-code governance;
- posting idempotency; and
- internal/provider reconciliation.

ADR-020 defines **what economic fact occurred**. ADR-015 defines **how that accepted fact is posted and reconciled**.

Economic facts defined by this ADR may support controlled posting classifications such as:

```text
PAYMENT_CAPTURE
PAYMENT_REFUND
EARNING_RECOGNITION
SUBSIDY_FUNDING
FEE_RECOGNITION
PAYOUT
PAYOUT_REVERSAL
FINANCIAL_ADJUSTMENT
```

or extensible equivalents governed by ADR-015. This ADR does not require exactly one LedgerTransaction per source, CommercialObligation, recognition, or event. One source may generate postings at several economic times, and one atomic accepted economic occurrence may have several related entries.

This ADR does not introduce mandatory `financial.ledger_accounts` or finalize a chart of accounts. ADR-015's controlled account-code strategy remains authoritative.

## PricingSnapshot and Promotion Evidence Boundary

`PricingSnapshot` remains the sole canonical immutable commercial-pricing snapshot authority. This ADR does not introduce:

- `FinancialSnapshot`;
- `CommercialObligationSnapshot`;
- `EarningSnapshot`; or
- `SettlementSnapshot`.

Financial may retain immutable facts and typed references needed for its own history, but it must not duplicate the full PricingSnapshot authority.

Promotion owns immutable PromotionApplication, PromotionSnapshot, and redemption/restoration evidence. Financial consumes those facts. It does not maintain duplicate eligibility or evaluation state and does not become a competing Promotion engine.

## Historical Evidence

### Provider and Beneficiary History

Historical Financial records must preserve the provider/beneficiary arrangement that applied when the commercial obligation arose. Old obligations must not be recomputed from current:

- Organization membership;
- professional Organization engagement;
- Chef employer;
- Kitchen operator;
- connected account;
- provider configuration;
- beneficiary configuration; or
- provider legal name/display configuration.

Current references may support current operations, but historical arrangement evidence must remain stable and auditable.

### Accepted Terms and Source Evidence

Recognition, remediation, and settlement decisions retain or reference enough durable evidence to explain:

- accepted offer/terms and policy version;
- typed source and source contributions;
- service outcome and responsible side where applicable;
- PricingSnapshot and relevant Promotion evidence;
- provider and beneficiary arrangement;
- amount, currency, and economic classification;
- decision and effective real instants; and
- prior fact being reversed, adjusted, remediated, or settled.

Financial must not derive historical economics solely from current domain configuration.

### Currency

All monetary values use integer minor units and explicit ISO currency. Floating-point monetary calculations are prohibited.

Commercial obligations, recognition, remediation, eligibility, PayoutLine, and ledger records are currency-safe. Multiple currencies must not be combined into one amount, one recognition balance, one eligibility amount, one Payout amount, or one LedgerTransaction.

### Time and Identifiers

ADR-010's UUIDv7 direction applies when persistence is designed. UUID ordering is not authoritative economic chronology.

ADR-011 governs real instants and local schedules. Commercial-obligation creation, funding, recognition, remediation, eligibility, settlement, and correction occurrences are real instants and must use appropriate instant semantics. Product scheduling rules remain in their owning domain; ADR-020 does not redefine timezone materialization.

## Correctness and Coordination

### Idempotency

Commercial-obligation creation, recognition, remediation, eligibility, and reversal operations are idempotent. Duplicate commands, domain events, provider events, outbox deliveries, or retries must not create duplicate:

- commercial obligations;
- source contributions;
- earning recognition;
- refund/remediation economics;
- payout eligibility; or
- correction/reversal facts.

Exact idempotency-key persistence and uniqueness remain implementation/ERD work aligned with ADR-012 and ADR-015. A correction is a new authorized fact, not a retry of the original operation.

### Out-of-Order Events

Financial processing must tolerate duplicated, delayed, and out-of-order integration events. Handlers use current authoritative domain state plus durable source and prior Financial evidence to validate transitions. Broker order is not a correctness mechanism.

An event claiming a later outcome cannot be applied blindly when prerequisite evidence is missing or contradictory. The consumer safely defers, rejects, quarantines, reconciles, or re-evaluates according to explicit policy without fabricating source state.

### Transaction Boundaries

Where an owning local domain operation and required Financial economic facts can and must commit atomically inside the modular monolith and the same PostgreSQL database, prefer one local transaction through explicit module application interfaces when module boundaries permit.

Where asynchronous decoupling is justified, persist domain state and its transactional-outbox event atomically, then process Financial effects idempotently. No distributed transaction is required across PostgreSQL and an external payment, settlement, calendar, delivery, meeting, or notification provider.

External provider calls do not participate in a local PostgreSQL transaction. ADR-012 provider orchestration and ADR-015 reconciliation handle external consistency.

### Event and Outbox Direction

Important Financial facts may later produce events conceptually equivalent to:

```text
CommercialObligationCreated
EarningRecognized
PayoutEligibilityEstablished
CommercialRemediationRecorded
```

These are examples only. This ADR does not finalize event names, aggregate types, publication policy, payloads, or versions. Exact contracts belong to `docs/05-event-contracts.md` and must follow ADR-009 transactional-outbox persistence and ADR-016 versioning.

## Tax and Merchant-of-Record Boundary

This ADR does not assert that Cheffy is or is not Merchant of Record. It does not finalize:

- sales-tax remittance party;
- GST, QST, HST, or VAT responsibility;
- marketplace-facilitator status;
- withholding responsibility;
- tax liability ownership;
- chargeback liability; or
- country-specific provider settlement liability.

The architecture preserves typed tax components and historical calculation/evidence where required. Legal, tax, accounting, and provider decisions remain explicit launch gates and must not be inferred from CommercialObligation terminology.

## Modular Monolith Boundary

This architecture remains within ADR-001's single Spring Boot modular monolith and PostgreSQL system of record. It introduces no:

- payments microservice;
- earnings microservice;
- payout microservice;
- ledger microservice; or
- separate Financial database.

Financial capabilities communicate with owning domains through explicit in-process application interfaces and selective transactional-outbox integration. Future extraction requires a separate approved architecture decision based on demonstrated operational need.

## Foundational Financial Persistence and ERD Deferral

The current foundational Financial tables remain recognized:

```text
financial.payments
financial.payment_attempts
financial.payment_allocations
financial.refunds
financial.refund_lines
financial.payouts
financial.payout_lines
financial.idempotency_keys
financial.provider_events
financial.ledger_transactions
financial.ledger_entries
```

These eleven tables are foundational, not permanently exhaustive. Additional non-ledger Financial persistence will be required or may be required for:

- commercial obligations;
- typed source contributions;
- earning recognition and correction;
- funded-unfulfilled value or remaining service obligations;
- payout eligibility and holds; and
- remediation relationships.

This ADR does not finalize or create those tables. The later canonical ERD must decide exact typed relational representation for:

- commercial provider;
- settlement beneficiary;
- typed commercial source families;
- CommercialObligation;
- source contributions and their cardinality;
- earning recognition and adjustment;
- unfulfilled value;
- payout eligibility and reversal/hold;
- remediation relationships;
- generalized Refund and RefundLine source linkage; and
- PayoutLine eligible-source linkage and duplicate-settlement prevention.

The ERD must avoid both one giant nullable-FK table and one unconstrained polymorphic UUID source. It must not introduce a universal source registry merely to simulate referential integrity. Exact APIs and event contracts remain separate canonical propagation work.

## Required Invariants

1. `SERVICE PERFORMER != COMMERCIAL PROVIDER`.
2. `COMMERCIAL PROVIDER != SETTLEMENT BENEFICIARY` where an approved arrangement differs.
3. `PROFESSIONAL ORGANIZATION AUTHORIZATION != COMMERCIAL PROVIDER`.
4. `PROFESSIONAL ORGANIZATION AUTHORIZATION != SETTLEMENT BENEFICIARY`.
5. `PAYMENT RECEIVED != COMMERCIAL OBLIGATION != EARNING RECOGNIZED != PAYOUT ELIGIBLE != EXTERNAL PAYOUT COMPLETED`.
6. `PAYMENT ALLOCATION != COMMERCIAL OBLIGATION`.
7. `PAYMENT ALLOCATION != EARNING`.
8. `PAYMENT ALLOCATION != PAYOUT`.
9. `PAYMENT ALLOCATION != LEDGER ENTRY`.
10. `CHEFORDERGROUP != COMMERCIAL PROVIDER`.
11. `CHEFORDERGROUP != SETTLEMENT BENEFICIARY`.
12. `CHEFORDERGROUP != PAYOUT`.
13. `MEAL SUBSCRIPTION BILLING != MEAL FULFILLMENT`.
14. `VOLUNTARY KITCHEN NON-USE != OPERATOR NONPERFORMANCE`.
15. `PLATFORM CUSTOMER SUBSIDY != PLATFORM FEE DISCOUNT != PLATFORM FEE WAIVER`.
16. `PROCESSOR FEE != PLATFORM FEE`.
17. `MARKETPLACE SETTLEMENT != PAYROLL`.
18. PricingSnapshot remains the canonical commercial-pricing authority.
19. `RATING != SERVICE PERFORMANCE FACT`.
20. Funded-unfulfilled value is not automatically legal escrow or a Customer cash wallet.
21. One source does not require one obligation effect, LedgerTransaction, PayoutLine, Payout, beneficiary, or connected account.
22. Historical provider, beneficiary, pricing, Promotion, fee, and service evidence is not recomputed from current configuration.

## Consequences

### Positive

- Actual performers remain distinct from commercial economics and retain operational accountability.
- Organization-operated food is represented as an Organization obligation rather than redirected employee earnings.
- Cheffy Operations uses the ordinary Organization model and can later exit direct supply without Financial redesign.
- Financial no longer assumes one Payout, beneficiary, or connected account per Chef or ChefOrderGroup.
- Payment collection is cleanly separated from commercial obligation, service performance, earning, payout eligibility, and settlement.
- Meal Subscription earning can follow incremental fulfilled-occurrence evidence.
- Kitchen Subscription earning can follow accepted access/capacity performance rather than physical usage alone.
- Funded-unfulfilled value and remaining service obligation become explainable without an unsupported escrow claim.
- Platform fee, processor fee, Platform subsidy, Platform-fee benefits, and provider-funded discounts remain distinct.
- Partial refunds and remediation use historical component and source evidence.
- Payout can aggregate many eligible earned sources while preserving detailed traceability.
- Provider/beneficiary arrangements remain historically stable after Organization or account changes.
- Marketplace settlement remains outside employee and contractor payroll.
- ADR-015 receives clear economic facts to post and reconcile rather than being asked to invent business policy.
- Typed source relationships preserve domain ownership and database integrity across Order and non-Order contexts.

### Negative / Costs

- Financial requires additional non-ledger economic records beyond Payment, Payout, and Ledger.
- Typed cross-domain source and source-contribution relationships require careful relational design.
- Each commercial context needs explicit performance-to-earning policy and tests.
- Incremental recognition and funded-unfulfilled value require durable amount tracking.
- Payout eligibility introduces a lifecycle distinct from earning and payout execution.
- Refund attribution across fee, subsidy, provider discount, tax, and source components is complex.
- Provider/beneficiary transaction-time history requires explicit evidence rather than current-state joins.
- Post-payout refunds require reconciliation, recovery, negative-balance, reserve, or future-netting policy.
- Out-of-order event handling and idempotency require source-specific uniqueness and transition controls.
- The canonical ERD must balance typed integrity against proliferation of source-family relationships without using polymorphic shortcuts.
- Tax, Merchant-of-Record, reserve, connected-account, and country-specific settlement decisions remain external launch dependencies.

## Alternatives Considered / Rejected

### 1. Payment Received Means Provider Earning Recognized

Rejected because collection proves funding, not service performance. Future or failed service would become incorrectly earned and subscription unfulfilled value could not be explained.

### 2. ChefOrderGroup Is the Commercial Provider or Payee

Rejected because ChefOrderGroup identifies one actual Chef's operational participation. An Organization may commercially supply food prepared by several Chefs.

### 3. One Payout per ChefOrderGroup

Rejected because one Organization beneficiary may aggregate economics from multiple groups and Orders. ChefOrderGroup remains source traceability, not settlement grouping.

### 4. One Connected Account per Actual Service Performer

Rejected because connected-account topology follows the approved beneficiary and legal/provider requirements, not actual-performer cardinality.

### 5. Employee Chef Earns the Marketplace Amount and Payout Is Redirected to the Employer

Rejected because the marketplace obligation belongs directly to the accepted Organization provider/beneficiary arrangement. Worker compensation is separate payroll.

### 6. Professional Organization Engagement Automatically Defines Commercial Provider

Rejected because ADR-017 engagement authorizes performance through an Organization; it does not establish commercial economics.

### 7. Commercial Provider Must Always Equal Settlement Beneficiary

Rejected because common equality must not prohibit a future explicitly approved legal/commercial arrangement that distinguishes them. Arbitrary redirection remains prohibited.

### 8. PaymentAllocation Is the Commercial Obligation

Rejected because PaymentAllocation is payment-side distribution/reference evidence and does not own service-performance or recognition lifecycle.

### 9. PaymentAllocation Is a Payout Instruction

Rejected because allocation does not establish earning, eligibility, beneficiary, connected-account readiness, or settlement authorization.

### 10. PaymentAllocation Is a LedgerEntry

Rejected because payment-side attribution and balanced accounting classification have distinct ownership and constraints under ADR-012 and ADR-015.

### 11. One Universal Payable or BusinessTransaction Aggregate Owns All Domains

Rejected because Order, KitchenBooking, Appointment, and both subscription products have distinct authority and lifecycles. Financial references typed evidence rather than absorbing them.

### 12. Arbitrary Source Type Plus UUID Is Canonical Financial Referential Integrity

Rejected because it permits dangling and cross-type relationships and cannot enforce domain-valid source cardinality. Controlled ledger correlation metadata does not justify this shortcut for core obligations.

### 13. One Universal Source Registry

Rejected because a registry created merely to simulate polymorphism obscures domain ownership and still does not provide the required source-family semantics.

### 14. Giant Nullable-FK CommercialObligation Table

Rejected because dozens of unrelated nullable source columns weaken constraints, increase invalid combinations, and make evolution unsafe.

### 15. Generic Metadata or JSON Is Canonical Source Identity

Rejected because core provider, beneficiary, obligation, and source relationships require typed relational integrity and queryable historical meaning.

### 16. Billing Success Earns All Future Meal Subscription Service Immediately

Rejected because Meal billing funds future obligations. Fulfilled occurrences and concrete food fulfillment provide performance evidence incrementally.

### 17. Divide Meal Billing Equally Across Occurrences in Every Plan

Rejected because accepted Pricing/offer evidence may assign different economic value. Equal allocation is valid only when explicitly captured by the approved model.

### 18. Kitchen Operator Earns Only for Physically Used Hours

Rejected because the accepted Kitchen Subscription service may be making access/capacity available, even when the Chef voluntarily uses less.

### 19. Voluntary Unused Kitchen Entitlement Equals Operator Nonperformance

Rejected because subscriber choice and provider inability have different performance, remediation, and earning consequences.

### 20. Current Configuration Is Sufficient for Historical Refunds

Rejected because prices, offers, fees, Promotions, tax, memberships, provider arrangements, and policies change. Refunds require transaction-time evidence.

### 21. Every Partial Refund Is Proportional Across All Components

Rejected because refunds must follow the affected source and captured component rules. Proportional treatment is not universally correct.

### 22. Rewrite the Original Payout After a Later Refund

Rejected because external settlement is historical fact. Later refund, recovery, reserve, negative-balance, and reconciliation effects are new facts.

### 23. Platform Subsidy Equals Platform-Fee Waiver

Rejected because subsidy funds Customer benefit while a fee waiver changes Cheffy's fee economics. They can have different provider and refund consequences.

### 24. Processor Fee Equals Platform Fee

Rejected because processor cost arises from the payment rail while Platform fee arises from Cheffy's commercial terms.

### 25. Connected-Account Readiness Defines Earning

Rejected because service performance and earning may exist before external account readiness. Account readiness is a settlement execution concern.

### 26. Funded-Unfulfilled Subscription Value Is Automatically Legal Escrow

Rejected because escrow is a legal, provider, operational, and accounting classification not decided by neutral obligation tracking.

### 27. Marketplace Financial Includes Employee Payroll

Rejected because marketplace provider settlement and worker compensation have different parties, policy, legal obligations, and systems.

### 28. Dietitian Receives Food-Sale or Meal Subscription Commission

Rejected because current Dietitian economics are limited to professional services. Guidance and discovery do not create food attribution or commission.

### 29. FinancialSnapshot Duplicates PricingSnapshot

Rejected because PricingSnapshot is the canonical commercial-pricing authority. Financial retains its own immutable economic facts and references without duplicating pricing authority.

### 30. Ratings or Conversation Text Determine Earning

Rejected because subjective ratings and unstructured messages are not authoritative service-performance or commercial-adjustment facts.

### 31. One LedgerTransaction per Source or Commercial Obligation

Rejected because funding, earning, remediation, fee recognition, payout, and recovery may occur at different times, while one atomic economic fact may contain several related postings.

### 32. One Financial Microservice per Economic Stage

Rejected because ADR-001 requires the modular-monolith baseline and the current transactions benefit from one PostgreSQL consistency boundary.

## Dependencies / Related ADRs

- **ADR-001 — Modular Monolith First (Accepted):** Financial capabilities remain within one deployable modular monolith; no payment, earning, payout, or ledger microservice is introduced.
- **ADR-005 — Order Fulfillment Type Separation (Proposed):** Food pickup/delivery completion and fulfillment evidence remain Order-owned inputs; this ADR changes no Order state.
- **ADR-006 — Promotion Targeting Model (Accepted):** Typed Promotion owner and target identity remain distinct from calculation scope, funder, provider, and beneficiary.
- **ADR-007 — Booking Concurrency Control (Accepted):** Kitchen Space, cleaning, Equipment, holds, confirmed capacity, and subscription-funded booking concurrency remain authoritative source evidence.
- **ADR-009 — Outbox Table Schema (Accepted):** Approved Financial events use transactional-outbox persistence with local state.
- **ADR-010 — UUIDv7 Identifier Strategy (Proposed):** Future Financial records follow the repository identifier direction without treating UUID order as economic chronology.
- **ADR-011 — Timezone Modeling (Proposed):** Real Financial occurrences use instant semantics; local service schedules remain owned by their business domains.
- **ADR-012 — Payment Marketplace Settlement (Proposed):** Payment, PaymentAttempt, PaymentAllocation, Refund orchestration, Payout execution, ProviderEvent, and provider adapters remain ADR-012 concerns.
- **ADR-013 — ChefOrderGroup Aggregate + Financial Boundary (Proposed):** ChefOrderGroup remains actual-Chef operational and source-traceability evidence and does not become a Financial aggregate.
- **ADR-014 — Promotion Engine (Proposed):** Promotion evaluation, funding semantics, application, redemption, restoration, and immutable evidence remain Promotion-owned.
- **ADR-015 — Financial Ledger and Reconciliation (Proposed):** LedgerTransaction, LedgerEntry, balanced posting, compensating entries, account-code governance, and reconciliation remain ADR-015 concerns.
- **ADR-016 — Event Versioning (Accepted):** Any future Financial event contract uses explicit compatible versioning.
- **ADR-017 — Professional Identity, Credentials and Jurisdiction Eligibility (Proposed):** Professional identity and Organization authorization remain inputs and do not automatically define provider or beneficiary.
- **ADR-018 — Dietitian Engagement, Appointment Scheduling and Online Meeting Provisioning (Proposed):** Appointment and its structured operational outcomes remain typed service evidence and are not redesigned here.
- **ADR-019 — Subscription, Entitlement and Materialized Occurrence Architecture (Proposed):** Meal occurrence performance and Kitchen access/capacity performance semantics are consumed without changing subscription ownership.

No related ADR status is changed by this Proposed ADR.

## Future ADR Relationships

- **ADR-021 — Authorized Multi-Context Conversation Architecture:** Conversation remains independent. Messages do not create authoritative Financial facts.
- **ADR-022 — Taxonomy / Reference Data Architecture:** Taxonomy remains independent and does not own commercial obligations or source identity.
- **ADR-023 — Verified-Experience Reviews and Reputation:** May reference service and Financial history where appropriate, but review/reputation cannot redefine earning or service-performance facts.

This ADR does not draft or pre-accept ADR-021, ADR-022, or ADR-023.

## Out of Scope

This Proposed ADR does not decide:

- exact tables, columns, foreign keys, indexes, constraints, enum values, or migration SQL;
- exact aggregate/class names;
- exact API endpoints, request/response fields, errors, or OpenAPI schemas;
- exact event names, payloads, aggregate types, publication rules, or consumers;
- exact earning-recognition dates or amounts where commercial policy remains unapproved;
- exact refund, cancellation, no-show, replacement, credit, or termination amounts;
- exact payout cadence, thresholds, reserves, negative-balance, recovery, or country/provider mechanics;
- connected-account topology or Stripe-specific settlement architecture;
- Merchant-of-Record, tax-remittance, marketplace-facilitator, withholding, or tax-liability conclusions;
- final chart of accounts, account codes, or mandatory ledger-account persistence;
- payroll, employee/contractor compensation, benefits, withholding, or remittance;
- landlord, lease, property-owner, or real-estate accounting;
- Customer cash wallet or generic withdrawable credit;
- Dietitian food-sale, Meal Subscription, recommendation, or referral commission;
- review, reputation, Conversation, or taxonomy architecture; or
- a universal Provider, Payable, BusinessTransaction, SourceRegistry, or FinancialSnapshot aggregate.

## Implementation / Propagation Notes

This Proposed ADR does not authorize application code, migrations, SQL, API changes, or event-contract changes by itself. After approval, implementation planning must:

1. Reconcile `docs/03-database-erd.md` with typed provider, beneficiary, commercial-obligation, source-contribution, recognition, unfulfilled-value, eligibility, remediation, Refund, and PayoutLine relationships.
2. Preserve the eleven foundational Financial tables while adding only the justified non-ledger persistence selected by canonical ERD work.
3. Avoid an unconstrained `source_type + source_id`, universal source registry, giant nullable-FK table, and generic JSON metadata relationship.
4. Define transaction-time commercial-provider and settlement-beneficiary selection/authorization and historical evidence without deriving old obligations from current Organization state.
5. Define typed source adapters or internal application interfaces for Food Order, Appointment, KitchenBooking, Meal Subscription, and Kitchen Subscription evidence.
6. Define idempotent obligation, recognition, remediation, eligibility, hold/revocation, and correction operations with database-enforced uniqueness where appropriate.
7. Define out-of-order event handling based on authoritative source state and durable evidence rather than broker ordering.
8. Coordinate local domain and Financial writes in one PostgreSQL transaction where atomicity is required and module boundaries permit; otherwise use ADR-009 outbox plus idempotent Financial processing.
9. Keep external provider calls outside local transactions and coordinate them through ADR-012 orchestration and ADR-015 reconciliation.
10. Preserve PricingSnapshot as canonical pricing evidence and reference Promotion-owned immutable evidence without creating a FinancialSnapshot.
11. Define context-specific recognition policies for food fulfillment, completed/cancelled/no-show Appointment outcomes, Meal occurrences, Kitchen Booking service, and Kitchen access/capacity performance.
12. Define component-aware refund/remediation policy using historical evidence rather than universal proportional allocation.
13. Define payout-eligibility policy separately from recognition and provider payout execution, including hold/revocation and post-payout recovery paths.
14. Update `docs/04-api-contracts.md` only after exact authorized representations are approved.
15. Update `docs/05-event-contracts.md` only after exact event semantics are approved, following ADR-016.
16. Add unit and integration tests for every identity, stage, currency, historical-evidence, idempotency, source-integrity, recognition, remediation, and settlement invariant.

At minimum, future tests must prove that Organization-operated food preserves separate actual Chef performers while producing approved shared Organization economics; professional engagement does not automatically select provider; payment and allocation do not create earning; Meal billing supports incremental occurrence recognition and funded-unfulfilled value; Kitchen voluntary non-use differs from operator failure; Platform subsidy differs from fee benefit; historical refunds do not use current configuration; post-payout remediation preserves original settlement; duplicate and out-of-order events do not duplicate or corrupt economic facts; payout may aggregate many eligible sources; and no payroll, fake Order, FinancialSnapshot, arbitrary polymorphic source, or Cheffy-specific Financial path is introduced.
