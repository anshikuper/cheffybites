# ADR-012 — Payment / Marketplace Settlement

## Status

Proposed

## Amendment Scope

This proposal generalizes the payment, refund, and provider-interaction orchestration described here beyond food Orders and reconciles it with Organization-operated marketplace supply. It does not accept this ADR, create the future ADR-020, or decide the generalized commercial-provider obligation, earning-recognition, Platform-fee, Platform-subsidy, refundability, settlement-beneficiary, or payout-eligibility model that ADR-020 must own. Dietitian food-sale, Meal Subscription, recommendation, and referral commission are not current product scope and are not deferred to ADR-020 by this proposal.

## Context

Cheffy Bites is a multi-sided marketplace where Customers or other approved payers fund food Orders, Kitchen Bookings, Dietitian Appointments, Meal Subscription billing cycles, and Kitchen Subscription billing cycles. Independent professional businesses, qualified commercial-provider Organizations, authorized Kitchen operator Organizations, the Platform, and other approved parties may later have economic interests arising from those commercial contexts. One payment may require payment-side allocation across multiple approved shares while preserving the actual Chef or Dietitian performer where operationally relevant.

The payment architecture must support one Customer payment for a multi-Chef food Order, internal payment-side allocation, ChefOrderGroup-level traceability for food-related financial activity, provider-neutral architecture, and immutable financial records for audit and reconciliation. Equivalent provider-neutral initiation, attempt, confirmation, failure, refund, webhook, idempotency, and reconciliation capabilities are required for the other approved billable contexts without converting them into Orders.

ADR-015 defines the canonical financial persistence and ledger model. This ADR defines payment and settlement orchestration.

The responsibility boundary is deliberate:

- this ADR answers how Cheffy initiates, tracks, retries, reconciles provider events for, allocates payment-side value for, and refunds a monetary Payment;
- the future ADR-020 will answer which commercial-provider obligation was funded, when value becomes earned, what remains unfulfilled or refundable, which Platform-fee or Platform-subsidy economics apply, which approved settlement beneficiary is owed value, and when value becomes payout-eligible;
- ADR-015 remains authoritative for ledger posting, accounting finalization, and reconciliation evidence;
- ADR-014 and the Pricing and Promotion domains remain authoritative for promotion calculation and immutable pricing/promotion evidence; and
- ADR-013 remains authoritative for ChefOrderGroup operational Chef identity and boundary;
- Organization and owning business domains retain Organization membership, business-provider identity, Kitchen operating authority, performer engagement, Order, Appointment, KitchenBooking, subscription, fulfillment, cancellation, and other business lifecycle states; and
- employee or contractor wages, payroll, incentive compensation, withholding, and remittance remain outside marketplace Payment/Payout architecture.

This ADR does not introduce a universal `Payable` aggregate merely to link these contexts. The exact typed source relationships, cardinalities, and database foreign keys for generalized commercial obligations require the future ADR-020 and corresponding reconciliation of the canonical ERD.

## Decision

We will implement centralized, provider-neutral payment and refund orchestration with internal payment-side allocation and provider settlement integration for approved billable contexts. Economic recognition and payout eligibility are downstream decisions outside this ADR.

### Core Principles

#### 1. One Logical Payment → Multiple Payment-Side Allocations

A successfully priced and approved collection request creates one logical Payment for its approved billable context. That Payment may be distributed into multiple internal PaymentAllocations representing payment-side value attributed to approved shares such as commercial-provider food value, Platform fees, delivery, tax where applicable, rental components, consultation components, Platform-funded Customer subsidy, or other explicitly approved categories. An allocation does not by itself establish commercial-provider earning recognition, refundability, payout eligibility, a ledger posting, a connected-account transfer, or provider settlement.

For the current food-checkout architecture, one concrete food Order has at most one logical Payment. This requires conceptual uniqueness equivalent to `UNIQUE(order_id)` for the food-Order specialization. Food Order split tender and multiple independent Payments for one Order remain unsupported unless separately approved. This specialization must not be interpreted as requiring every Payment to belong to an Order.

The Payment cardinality for a Kitchen Booking, Dietitian Appointment, Meal Subscription billing cycle, or Kitchen Subscription billing cycle is not inferred from the food-Order rule. The future obligation architecture must define those source relationships and replay-safe uniqueness rules without collapsing the distinct business concepts into one universal aggregate.

One provider interaction is not one Payment. A Payment can have multiple PaymentAttempts while remaining the same logical customer payment.

#### 2. Provider-Neutral Architecture

Stripe Connect may be the initial payment provider, but the domain model must not depend on Stripe-specific concepts. Provider-specific APIs are isolated behind a payment gateway adapter.

#### 3. Canonical Financial Ownership

All financial persistence uses the PostgreSQL `financial` schema defined by ADR-015. This ADR does not introduce a competing `payment` schema.

#### 4. Financial Immutability

Financial facts are append-only. Historical financial records must not be overwritten. Refunds, reversals, corrections, and adjustments create new financial records and, where accounting postings are required, new ledger transactions and entries under ADR-015.

#### 5. Merchant of Record Remains Unresolved

This ADR does not decide Merchant of Record, tax collection or remittance responsibility, chargeback liability, refund liability, connected-account topology, reserve policy, negative-balance handling, country-specific settlement timing, provider risk requirements, marketplace legal obligations, or final settlement obligations. These remain legal, accounting, and provider approval gates. Foundational schema terminology must not be interpreted as deciding them.

#### 6. Payment State Is Not Business Fulfillment State

Payment and PaymentAttempt states describe collection/provider interaction only. They must not be used as substitutes for Order, KitchenBooking, Appointment, Meal Subscription, Kitchen Subscription, entitlement, occurrence, cancellation, or fulfillment states. A business domain may react to authoritative payment outcomes through an idempotent application workflow, but it retains authority over its own transitions.

#### 7. Collection, Recognition, and Settlement Are Distinct

```text
PAYMENT RECEIVED != PROVIDER EARNING RECOGNIZED
PROVIDER EARNING RECOGNIZED != PAYOUT ELIGIBLE
PAYOUT ELIGIBLE != EXTERNAL PAYOUT COMPLETED
```

This ADR owns the first collection/provider-interaction concern and provider execution workflows for approved Refunds or Payouts. It does not decide the middle commercial-recognition and payout-eligibility concerns.

#### 8. Service Performer, Commercial Provider, and Settlement Beneficiary Are Distinct

```text
SERVICE PERFORMER
!= COMMERCIAL PROVIDER
!= SETTLEMENT BENEFICIARY
```

The **service performer** answers, "Who actually performed, prepared, or provided the service?" The **commercial provider** answers, "Which commercial or legal marketplace provider supplied or sold the service?" The **settlement beneficiary** identifies the approved party to which marketplace settlement is owed under the applicable arrangement. These identities may coincide, and the commercial provider will commonly be the settlement beneficiary, but this ADR does not make either equivalence irreversible.

This distinction does not authorize arbitrary third-party routing. The approved commercial and legal arrangement must establish any settlement beneficiary. The exact commercial-provider, performer-reference, settlement-beneficiary, obligation, and payout relationships remain for ADR-020 and the canonical ERD.

#### 9. Marketplace Settlement Is Not Payroll

```text
MARKETPLACE SETTLEMENT != EMPLOYEE / CONTRACTOR PAYROLL
```

When an Organization commercially supplies a service performed by an employed or engaged Chef or Dietitian, marketplace processing may establish an eventual settlement obligation to the Organization. It must not fabricate an individual worker marketplace earning and then redirect that earning to the employer. Salary, hourly wage, lawful performance compensation, contractor compensation, payroll, withholding, employer payroll tax, timesheets, pay periods, and payroll remittance are outside this ADR and Cheffy's normal marketplace Financial/Payout architecture.

Cheffy Operations uses this same ordinary Organization commercial-provider model when it temporarily supplies launch inventory. No payment behavior may branch merely because the provider is Cheffy. A later Cheffy exit from direct supply must not require payment-architecture redesign. Any future payroll-system integration is a separate concern.

## Financial Flow

```text
Approved Payer
    ↓
Approved Billable Context
    ↓
Authoritative Pricing + Promotion + Tax + Fees
    ↓
Payment
    ↓
Payment Attempt(s)
    ↓
Payment Confirmation
    ↓
Internal Payment Allocation(s)
    ↓
Ledger Transaction + Ledger Entries (ADR-015)
    ↓
Commercial Obligation / Recognition / Payout Eligibility (future ADR-020)
    ↓
Approved Payout + Payout Lines
    ↓
External Provider Settlement
    ↓
Reconciliation
```

## Architecture Components

### Payment Aggregate

`Payment` is Cheffy Bites' logical collection record for one approved billable context. It is a Financial-domain aggregate and is not an Order, KitchenBooking, Appointment, subscription, commercial obligation, provider payment/session object, or business lifecycle aggregate.

Approved billable contexts currently comprise:

```text
FOOD_ORDER
KITCHEN_BOOKING
DIETITIAN_APPOINTMENT
MEAL_SUBSCRIPTION_BILLING_CYCLE
KITCHEN_SUBSCRIPTION_BILLING_CYCLE
```

These names describe approved orchestration contexts, not a final database discriminator or permission to store an arbitrary `source_type + UUID` relationship without referential integrity. Exact typed references and cardinalities remain subject to the future ADR-020 and canonical persistence design. The current food model permits at most one Payment per concrete food Order and does not support food Order split tender or multiple independent Payments for one food checkout.

A Payment owns the logical amount and currency expected from the approved payer and coordinates zero or more PaymentAttempts. Provider retries or additional provider interactions append to the attempt history and drive the resulting Payment state; they do not create another logical Payment. The owning business domain supplies authoritative priced evidence and a stable, authorized collection request; the Financial domain does not recalculate the commercial price from current configuration.

### PaymentAttempt

`PaymentAttempt` is one retryable provider interaction belonging to exactly one logical Payment. Examples include initial authorization or charge initiation, a retry after a retryable provider failure, or an additional interaction required to complete the same Payment. The model does not prescribe provider-specific retry semantics.

Every PaymentAttempt has its own Cheffy identity. A migration-ready model must also provide an unambiguous attempt sequence or equivalent uniqueness within its parent Payment. The selected `provider_name` identifies the adapter. A generic `provider_payment_reference` may be null before the provider accepts or creates an interaction when the provider flow genuinely requires that ordering. Once present, the `(provider_name, provider_payment_reference)` combination must not identify multiple PaymentAttempts. Provider adapters map this generic reference to a Stripe PaymentIntent ID, another provider's session or transaction reference, or an equivalent identifier.

Attempt-level monetary values, when present, use integer minor units and a currency code consistent with the parent Payment.

A failed attempt does not by itself terminate the parent business context or subscription. Retry exhaustion and the resulting business consequence are explicit, idempotent application decisions owned by the relevant business domain. A successful provider interaction must not be applied to a different logical Payment or billable context.

### Payment Gateway Interface

```java
public interface PaymentGateway {
    PaymentInitiationResult createPayment(PaymentInitiationRequest request);
    PaymentStatusResult retrievePaymentStatus(ProviderPaymentReference reference);
    RefundInitiationResult initiateRefund(RefundInitiationRequest request);
}
```

`PaymentGateway` is the provider-neutral integration boundary between Cheffy Bites and an external payment provider. `PaymentInitiationResult` is a Cheffy adapter result, not a Stripe PaymentIntent, the Cheffy Payment aggregate, or a provider database entity. It may carry provider-neutral initiation evidence such as a provider payment reference, current interaction status, a client interaction token or secret when required, redirect/action information, and appropriate provider metadata. Sensitive interaction values must not be logged and are persisted only when an approved secure flow requires it.

Adapters may also support payment status retrieval, refund initiation, recipient onboarding/account operations, and payout/transfer operations where those capabilities are approved. Those operations use provider-neutral requests, results, and references. Stripe Connect remains a likely initial adapter subject to legal, accounting, risk, and provider approval; it is not the domain model.

### ProviderEvent

`ProviderEvent` is immutable evidence of one incoming provider webhook or event. It is not a Payment, PaymentAttempt, or ledger transaction. Deduplication requires the equivalent of:

```text
UNIQUE(provider_name, provider_event_id)
```

Processing is idempotent. A duplicate callback must not duplicate Payment or other financial aggregate transitions, PaymentAllocations, Refunds, Payouts, ledger postings, or transactional-outbox events. Provider payload retention must follow security, privacy, and audit policy and must never make the provider event the canonical financial fact.

Webhook signatures and equivalent provider authentication must be verified before an event can drive financial side effects. Events may arrive duplicated, delayed, or out of order; handlers must compare persisted state and provider evidence rather than assume delivery order. Unknown or unmatched provider references are retained or quarantined as auditable reconciliation exceptions and must not be attached to a business context by guesswork.

### Financial Command Idempotency

Financial commands retain Cheffy's operation/key/request-hash model through `financial.idempotency_keys` or its canonical equivalent. Reusing one operation and idempotency key with a different request hash must be rejected. Provider idempotency keys may additionally be used by adapters, but they do not replace Cheffy's financial idempotency controls.

### PaymentAllocation

`PaymentAllocation` is an internal, provider-neutral Cheffy payment-side financial fact. It records how collected value is logically attributed to approved shares. It is not the generalized commercial-obligation model and is not proof of service fulfillment, earning recognition, payout eligibility, ledger posting, provider transfer, or external settlement.

Each allocation requires:

- its parent Payment;
- allocation type;
- `amount_minor` and `currency_code` consistent with the Payment;
- the applicable typed source/operational scope reference, including ChefOrderGroup traceability for food value where required;
- a typed commercial-provider or recipient relationship where required by the approved allocation type and canonical model, without assuming that it is the service performer;
- typed source evidence appropriate to the approved billable context and allocation type where required; and
- immutable calculation/source evidence sufficient for audit without replacing relational ownership with polymorphic JSONB.

Recipient, commercial-provider, performer/source, and operational-scope cardinality is conditional on allocation type and commercial context. A required reference must be relationally valid and constrained by the canonical model. ChefOrderGroup references preserve applicable food source/performer traceability but do not imply that the Chef performer is the commercial provider, settlement beneficiary, connected-account holder, or external payout recipient. Platform, delivery, tax, Kitchen, Dietitian consultation, subscription, and other allocations use their applicable typed evidence rather than a fabricated ChefOrderGroup relationship.

Multiple ChefOrderGroups may contribute economic activity to one Organization commercial-provider/settlement relationship without being collapsed into one ChefOrderGroup. Conversely, one ChefOrderGroup does not automatically require one external recipient, one Payout, or one connected account. This ADR does not finalize generalized source, provider, beneficiary, or obligation columns, foreign keys, and cardinalities ahead of ADR-020. A universal arbitrary `source_type + UUID`, a giant nullable-foreign-key table, or a generic JSONB relationship is not approved merely by this orchestration decision.

PaymentAllocations must reconcile to the Payment according to an approved allocation policy and one currency. PricingSnapshot, PromotionSnapshot, PromotionApplication, FeeLineItem, and TaxLineItem remain evidence owned by Pricing, Promotion, and Tax; a PaymentAllocation may reference that immutable evidence but does not replace or recalculate it.

### Internal Allocation and External Settlement

Internal allocation is Cheffy's authoritative logical financial distribution. External settlement is provider execution of transfers or payouts according to the eventually approved connected-account and legal model. These are separate facts and separate lifecycle steps.

Internal payment-side allocations can be modeled before the final external settlement topology is approved. Cheffy owns immutable payment records, provider orchestration, and reconciliation. The applicable business, Pricing, Promotion, Tax, future commercial-obligation, and Financial/Ledger decisions own their respective calculation, obligation, recognition, refundability, posting, and payout-eligibility rules. The selected provider owns execution rails and provider-side account/compliance processes within the approved integration model.

The provider-neutral settlement integration does not require a connected account for every individual service performer. The approved settlement beneficiary normally owns the applicable marketplace settlement/connected-account relationship. It may be an independent Chef's business Organization, an Organization employing or engaging multiple Chefs, an authorized Kitchen operator Organization, or a Dietitian clinic/Organization where legally and professionally permitted. Exact connected-account topology remains unresolved and does not decide Merchant of Record.

### Financial Relationships

```text
Approved Billable Context
    ↓ authorized collection request
Logical Payment
    ├── PaymentAttempt 1
    ├── PaymentAttempt 2 (retry/additional interaction)
    └── Internal PaymentAllocations
            ├── Food value → ChefOrderGroup source scope(s) where applicable
            │       └── Approved commercial provider / beneficiary relationship (future ADR-020)
            ├── Platform fee
            ├── Delivery/tax payment-side share where applicable
            ├── Rental/consultation payment-side share where applicable
            ├── Platform-funded Customer subsidy where applicable
            └── Other approved payment-side shares
                    ↓
            Ledger posting where required (ADR-015)
                    ↓
            Commercial recognition and payout eligibility (future ADR-020)
                    ↓
            External provider settlement
```

For the food specialization, this preserves one Customer Payment, one Order, one Kitchen, and multiple ChefOrderGroups when applicable. A separate Customer Payment is not created for each Chef. The other billable contexts remain their own domain aggregates and are not converted into Orders or ChefOrderGroups.

### Organization Commercial-Provider Examples

```text
Independent Chef model
  Service performer: Chef Ravi
  Commercial provider: Ravi's business / Organization
  Settlement beneficiary: Ravi's business / Organization

Organization-employed Chef model
  Service performer: Chef Ravi
  Commercial provider: ABC Food Group
  Settlement beneficiary: ABC Food Group

Cheffy bootstrap model
  Service performer: Cheffy-employed Chef
  Commercial provider: Cheffy Operations Organization
  Settlement beneficiary: Cheffy Operations Organization
```

The payment architecture must not require each individual Chef performer to be an independent connected-account settlement recipient. For example, one concrete food Order may have one logical Payment, ChefOrderGroup Ravi and ChefOrderGroup Maria as distinct operational/source references, and ABC Food Group as the common commercial provider and eventual settlement beneficiary under accepted commercial terms. Ravi's and Maria's employee or contractor compensation is outside marketplace settlement. This example prescribes neither percentages, ledger entries, payroll allocations, nor payout grouping.

Cheffy's marketplace fee follows the commercial-provider arrangement. An Organization-operated food transaction does not produce both an Organization marketplace-provider fee and an additional marketplace-provider fee against each employed Chef merely because those Chefs performed the work. Operational Chef-specific pricing or allocation evidence may still be preserved where required. Pricing and future ADR-020 own fee calculation and economic obligation; this ADR owns payment and settlement orchestration.

### Commercial Providers Across Billable Contexts

- **Food Order:** The commercial provider may be an independent Chef's business or an Organization employing/engaging one or more Chef performers. ChefOrderGroups remain distinct even when they contribute to one Organization provider obligation.
- **Kitchen Booking:** The commercial provider/settlement beneficiary may be the authorized Kitchen operator Organization rather than the real-estate property owner. If Property Owner A leases operating rights to Organization B, an approved KitchenBooking may economically belong to Organization B. This ADR does not model lease accounting, landlord payments, or operating-right persistence.
- **Dietitian Appointment:** The actual practicing Dietitian remains traceable on the Appointment. Where legally and professionally permitted, a clinic/Organization may be the commercial provider and settlement beneficiary. Professional independence, billing, title, credential, and jurisdiction constraints remain launch gates.
- **Meal Subscription:** The commercial provider may be an Organization employing/engaging the occurrence-level Chef performer. Performer and ChefOrderGroup/occurrence traceability remain; direct external payout to each performer is not assumed.
- **Kitchen Subscription:** The commercial provider may be the authorized Kitchen operator Organization rather than the property owner. Kitchen Subscription remains distinct from Meal Subscription.

Dietitian professional-service payment support includes Customer payment, PaymentAttempts and retries, provider-neutral Refund orchestration, Dietitian-owned consultation Promotions, Platform-funded Customer subsidy, Platform fee, and provider cancellation/no-show remediation where approved. Payment success does not establish Dietitian earning. Dietitian food-sale, Meal Subscription, recommendation, referral, or Chef-purchase commission and related PaymentAllocation are not current product scope. Reconsidering such a model requires a new explicit product, professional-regulatory, financial, and architecture decision.

### Platform-Funded Customer Subsidy

Customer contribution and Platform-funded Customer subsidy are distinct funding facts. A subsidized transaction may have an Organization commercial provider. For example, commercial food value of 30 currency units may comprise a Customer contribution of 20 and Platform subsidy of 10. Provider gross economics must not be inferred solely from the Customer Payment amount. Pricing, Promotion, future ADR-020, and ADR-015 own the applicable calculation, obligation, and posting decisions; this ADR preserves provider-neutral collection and settlement orchestration without creating Dietitian food-commission allocation.

### ChefOrderGroup Boundary

ADR-013 remains authoritative. ChefOrderGroup is the operational preparation, Chef authorization, Chef promotion, and Chef financial allocation/reference boundary. The Financial domain owns Payment, PaymentAllocation, Refund, Payout, LedgerTransaction, and LedgerEntry. Referencing `chef_order_group_id` does not transfer ownership of those financial facts to ChefOrderGroup.

ChefOrderGroup identifies the applicable actual Chef performer/operational food scope under ADR-013; it does not automatically identify the commercial provider, settlement beneficiary, connected-account holder, or Payout recipient. Common Organization settlement must not merge the separate ChefOrderGroups of different performers. Exact ChefOrderGroup identity/key reconciliation remains ADR-013's responsibility.

### Refund Boundary

`Refund` is an append-only Financial-domain aggregate/fact that traces the original Payment and the applicable approved business context through canonical relational evidence. A food Refund retains the original commercial transaction, commercial-provider arrangement, Order, allocation-, item-, and ChefOrderGroup/performer-level traceability where applicable. Refunds for Kitchen Bookings, Dietitian Appointments, or subscription billing cycles must retain the applicable typed context, provider arrangement, relevant performer evidence, and immutable decision evidence without pretending those contexts are Orders.

`RefundLine` preserves the approved allocation/component-level traceability needed for partial refunds and redistribution where applicable. This ADR does not finalize a generalized RefundLine source schema or decide which unfulfilled value is refundable; the owning business policy, promotion/refund recalculation rules, and future ADR-020 supply the authorized refund decision and amount. ADR-012 orchestrates the idempotent provider refund and records its outcome.

Refunds and corrections append new financial evidence. They never rewrite the original Payment, PaymentAllocation, pricing or promotion evidence, commercial obligation, or ledger history. Exact provider refund references and provider-side interactions remain adapter concerns represented through generic provider references and immutable ProviderEvents.

A Refund does not create or recalculate employee wages, payroll, or contractor compensation. Exact commercial allocation, reversal, remediation, and recognition consequences remain for ADR-020 and ADR-015.

### Payout Boundary

This ADR may coordinate provider execution and provider-event reconciliation for a Payout that an authoritative downstream workflow has already approved. It does not determine whether an earning exists, when it is recognized, who is payout-eligible, or the amount eligible for payout. Those commercial decisions belong to the future ADR-020, with accounting posting governed by ADR-015.

`Payout` and `PayoutLine` remain Financial-domain facts. Their eventual source-obligation traceability and uniqueness must prevent double settlement, including ChefOrderGroup source references for food economic activity where applicable. Multiple ChefOrderGroups and multiple Orders may contribute to an Organization provider obligation that the eventual payout model aggregates; this ADR requires neither one Payout per ChefOrderGroup nor one Payout per Order and does not finalize payout grouping. Payout creation is not proof that an external provider transfer completed; provider transfer/payout state changes are applied through authenticated, idempotent provider workflows.

Cheffy intends to automate approved provider payout/settlement workflows, but this ADR does not promise a transfer topology before the legal model, connected-account model, liability model, provider risk requirements, and provider approval are resolved.

### Ledger Integration

Financial state changes requiring accounting postings create `financial.ledger_transactions` and child `financial.ledger_entries` through ADR-015's posting/finalization mechanism. ADR-015 is authoritative for balance enforcement, one-currency posting, finalization, posted immutability, compensating transactions, atomic financial-state/ledger/outbox persistence, reconciliation, and `LedgerTransactionPosted.v1`. This ADR does not define a ledger-entry-only finalization model or duplicate those database mechanics.

### Tax Boundary

Stripe Tax or another tax provider is an integration adapter and evidence source, not the Tax domain model or Cheffy's financial system of record. Order, Pricing, and Tax evidence may preserve tax calculations. Financial allocations and ADR-015 postings represent resulting financial obligations and finalized accounting facts. Tax collection and remittance ownership remains unresolved pending legal and accounting approval.

### Canonical Financial Schema Boundary

The foundational financial persistence boundary is:

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

ADR-015 is authoritative for the final two ledger records. No additional ledger-account persistence entity is introduced by this ADR. Pricing snapshots, promotion snapshots, fee calculation lines, tax calculation lines, and Order commercial totals remain with their owning domains even though they contain monetary values.

These are the current foundational financial tables, not a declaration that they are permanently sufficient for future commercial-provider obligation, liability, earning-recognition, Platform-fee, Platform-subsidy, settlement-beneficiary, or payout-eligibility requirements. This ADR adds no speculative ADR-020 tables and does not authorize implementing generalized source relationships before the owning canonical documents are reconciled. There is no separate `FinancialSnapshot`; `PricingSnapshot` remains Pricing-owned immutable commercial/calculation evidence.

### Responsibility Boundary

ADR-012 owns logical Payment, PaymentAttempt, provider-neutral PaymentGateway and initiation results/references, idempotent provider operations, ProviderEvent evidence, provider-neutral Refund orchestration, payment-side internal allocation/reference, approved Payout/provider-settlement execution orchestration, and local-database/provider consistency strategy.

Future ADR-020 owns the generalized relationship among commercial provider, financially relevant service-performer reference, settlement beneficiary, commercial obligation, provider earning recognition, Customer-funded unfulfilled value, Platform-fee obligation, Platform-subsidy obligation, payout eligibility, refund/remediation allocation, and generalized financial sources. ADR-020 must not model Dietitian food-sale, Meal Subscription, recommendation, referral, or Chef-purchase commission under the current product decision.

ADR-013 owns ChefOrderGroup operational Chef identity and boundary. ADR-015 owns LedgerTransaction/LedgerEntry, immutable balanced posting, and reconciliation. Organization and owning business domains own Organization membership, Kitchen operating authority, performer employment/engagement, business-provider identity, and business lifecycle. HR/payroll outside the marketplace owns wages, payroll, employee incentive compensation, withholding, and remittance.

### Money and Currency

All canonical monetary values use integer minor units plus currency code; floating point is prohibited. Currency consistency is required across Payment, monetary PaymentAttempt fields, PaymentAllocation, Refund, RefundLine, Payout, PayoutLine, and related ledger transactions/entries. Exact relational enforcement belongs to the canonical ERD and migration design.

### Local Transaction and Provider Coordination

No local PostgreSQL transaction spans an external provider call. No two-phase commit or distributed database/provider transaction is introduced. Provider operations are coordinated through idempotent commands, PaymentAttempts, immutable/deduplicated ProviderEvents, the transactional outbox, and reconciliation.

Provider success or failure appends evidence and drives idempotent local state transitions; it does not overwrite financial history. Reconciliation compares Cheffy's canonical financial truth with provider-reported truth. Discrepancies create auditable reconciliation evidence and investigation and, when correction is required, new compensating financial and ledger records under ADR-015. Provider state is not the sole system of record.

## Operational Rules

- One Order has at most one logical Payment under the current checkout model.
- Approved orchestration contexts are Food Order, Kitchen Booking, Dietitian Appointment, Meal Subscription billing cycle, and Kitchen Subscription billing cycle; they remain distinct business aggregates.
- Cardinality for non-food contexts is not inferred from the one-Payment-per-food-Order specialization.
- One Payment may have many PaymentAttempts and many PaymentAllocations.
- Each PaymentAttempt has its own identity and belongs to exactly one Payment.
- Each allocation records the originating Payment, allocation type, amount, currency, conditionally required typed recipient/source evidence, and immutable calculation evidence.
- Food-value allocations preserve the applicable ChefOrderGroup source/performer traceability where required; that reference does not designate an external recipient.
- Multiple ChefOrderGroups may relate to one Organization commercial-provider/settlement relationship without being collapsed.
- An individual service performer is not required to own a connected account or receive an external marketplace Payout.
- Marketplace settlement is distinct from employee or contractor payroll.
- Customer contribution is distinct from Platform-funded Customer subsidy; provider economics are not inferred solely from collected Customer value.
- Payment success does not establish fulfillment, earning recognition, payout eligibility, or external payout completion.
- Business cancellation, subscription, occurrence, entitlement, Appointment, KitchenBooking, and Order states remain owned by their business domains.
- Provider payout execution begins only from an independently authorized payout instruction; ADR-012 does not calculate payout eligibility.
- Refunds create new immutable financial records and reference the original financial source and detailed evidence where applicable.
- Financial commands use Cheffy idempotency independent of provider idempotency.
- Provider webhooks are deduplicated using `(provider_name, provider_event_id)` before side effects.
- Provider calls occur outside local PostgreSQL transactions.

## Event Integration

Payment-related events follow ADR-016 and are published through the transactional outbox:

```text
PaymentInitiated.v1
PaymentSucceeded.v1
PaymentFailed.v1
PaymentCaptured.v1
PaymentAllocated.v1
RefundRequested.v1
RefundProcessed.v1
RefundAllocated.v1
PayoutCreated.v1
PayoutProcessed.v1
PayoutFailed.v1
PayoutReconciled.v1
```

The listed names are the current event baseline, not an approval to add a generic untyped source field or to change existing event contracts incompatibly. Multi-context payload/source evolution requires explicit canonical event-contract updates under ADR-016. Events communicate outcomes; they do not transfer lifecycle ownership from the applicable business domain or turn provider callbacks into canonical business facts.

## Consequences

### Positive

- Supports one customer checkout with multiple payment-side shares and approved commercial-provider relationships.
- Supports provider-neutral payment and refund orchestration across approved billable contexts without converting them into Orders.
- Preserves ChefOrderGroup-level financial traceability.
- Supports Organization commercial providers and common Organization settlement without erasing individual performer evidence.
- Keeps marketplace settlement separate from worker payroll.
- Keeps payment providers replaceable.
- Separates internal allocation from external settlement execution.
- Separates technical architecture from unresolved legal decisions.
- Supports reconciliation through ADR-015.

### Negative

- Allocation and reconciliation add implementation complexity.
- Provider integration requires careful idempotency handling.
- Type-specific allocation references and attempt uniqueness require explicit migration constraints.
- Settlement behavior depends on unresolved business/legal decisions.
- Generalized provider/beneficiary obligation relationships, recognition, refundability, Platform-fee/subsidy economics, and payout eligibility remain for ADR-020.

## Implementation Notes

1. Use the `financial` schema defined by ADR-015.
2. Implement a provider-neutral `PaymentGateway`.
3. Return `PaymentInitiationResult` from payment initiation; do not expose a provider SDK object as the domain contract.
4. Use generic provider references and keep provider identifiers as integration references rather than domain identifiers.
5. Protect all financial commands with Cheffy operation/key/request-hash idempotency.
6. Deduplicate immutable provider events before side effects.
7. Publish domain events through the transactional outbox.
8. Use ADR-015 ledger transactions for accounting postings.
9. Add integration and end-to-end tests for duplicate commands, attempt retries, authenticated/replayed/out-of-order webhooks, provider-reference matching, allocation currency and sum invariants, context authorization, refund traceability, provider failure, and reconciliation.
10. Test the food specialization separately: one Payment per concrete food Order, no split tender, no Payment per Chef, and ChefOrderGroup traceability.
11. Test each approved non-food context without assigning it Order lifecycle or food-specific cardinality semantics.
12. Test an Organization-operated food Order with separate ChefOrderGroups for Ravi and Maria, one Organization commercial provider/beneficiary, no per-employee connected-account requirement, and no payroll allocation.
13. Test Kitchen operator settlement independently of property ownership, and Dietitian Appointment performer traceability independently of an approved clinic/Organization provider relationship.
14. Test Platform-funded Customer subsidy without inferring provider gross economics only from Customer contribution.
15. Do not implement speculative generalized source/provider/beneficiary columns or ADR-020 economic rules from this proposal alone; update the canonical ERD, API, and event contracts after the owning decisions are approved.

## Alternatives Considered

- **Direct Provider Integration** — rejected because provider-specific concepts would leak into the domain architecture.
- **Single Payout Per Order** — rejected because it would not preserve sufficient ChefOrderGroup-level traceability.
- **One External Recipient Per ChefOrderGroup** — rejected because ChefOrderGroup preserves actual Chef operational/source traceability while an Organization may be the common commercial provider and settlement beneficiary.
- **Employee Marketplace Earning Redirected to Employer** — rejected because Organization-operated transactions belong commercially to the applicable Organization arrangement; worker compensation is a separate payroll/employment concern.
- **Real-Time Payouts** — not selected as the default because payout eligibility requires configurable business rules, reconciliation, and risk controls.
- **Universal Payable Aggregate in This ADR** — not selected because payment orchestration does not yet define generalized commercial obligations, earning recognition, or payout eligibility; those decisions belong to future ADR-020.
- **Treat Every Billable Context as an Order** — rejected because KitchenBooking, Appointment, and both subscription products retain distinct ownership and lifecycle semantics.
- **Provider Objects as Canonical Financial Aggregates** — rejected because provider references and events are integration/reconciliation evidence, not Cheffy's domain model.

## Dependencies

- ADR-013 — ChefOrderGroup Aggregate + Financial Boundary
- ADR-014 — Promotion Engine
- ADR-015 — Financial Ledger / Reconciliation
- ADR-016 — Event Versioning
- Future ADR-020 — Commercial Obligation / Earning Recognition / Payout Eligibility (not created or decided by this ADR)
