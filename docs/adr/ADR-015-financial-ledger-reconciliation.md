# ADR-015 — Financial Ledger / Reconciliation

## Status

Proposed

## Amendment Scope

This proposal extends the ledger's ability to record approved multi-context and Organization commercial economics while keeping service-performer evidence distinct from commercial-provider and settlement-beneficiary obligations. It does not create future ADR-020 or decide commercial obligations, earning recognition, unfulfilled-value semantics, Platform-fee/subsidy obligations, refundability, remediation, or payout eligibility. Dietitian food-sale, Meal Subscription, recommendation, and referral commission and employee payroll are not deferred ledger requirements. ADR-015 owns how an accepted financial fact is posted immutably, balanced, audited, and reconciled after the owning policy has determined that fact.

## Context

Cheffy Bites requires an append-only financial system of record for payments, allocations, refunds, adjustments, payouts, provider reconciliation, commercial-provider economics, Platform fees, delivery financial activity, ChefOrderGroup-level performer/source traceability, Dietitian consultation economics, Platform-funded subsidies and fee benefits, Meal Subscription billing/occurrence economics, and Kitchen Subscription access/capacity economics.

ADR-012 defines payment and settlement orchestration. This ADR defines canonical financial persistence and ledger semantics.

## Decision

### Core Principles

1. Financial business facts are append-only.
2. Corrections use new compensating/refund/adjustment records and new ledger transactions; a posted transaction is never reopened or rewritten.
3. The `financial` schema is the single canonical financial schema.
4. Provider identifiers are retained without making the domain provider-specific.
5. Provider events are deduplicated before side effects.
6. Every finalized financial movement is traceable to source and evidence.
7. A ledger transaction header is the canonical posting and finalization boundary for its entries.
8. Collection, earning recognition, payout eligibility, and external payout are distinct economic stages.

```text
PAYMENT RECEIVED
!= PROVIDER EARNING RECOGNIZED
!= PAYOUT ELIGIBLE
!= EXTERNAL PAYOUT COMPLETED

PROMOTION APPLIED
!= FINANCIAL SUBSIDY POSTED

SUBSCRIPTION BILLING SUCCESS
!= FULL PROVIDER EARNING RECOGNIZED

SERVICE PERFORMER
!= COMMERCIAL PROVIDER
!= SETTLEMENT BENEFICIARY

MARKETPLACE SETTLEMENT
!= EMPLOYEE / CONTRACTOR PAYROLL
```

Different stages may produce separate immutable LedgerTransactions at their actual economic times. They must not be collapsed into one status or posting merely because they arise from one commercial relationship.

### Canonical Financial Records

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

These current foundational tables do not pre-decide whether future ADR-020 requires additional non-ledger Financial aggregates. This ADR does not add speculative `financial.commercial_obligations`, `financial.earnings`, `financial.liabilities`, `financial.commissions`, `financial.subsidies`, `financial.provider_accounts`, `financial.settlement_beneficiaries`, `financial.payables`, `financial.customer_credits`, `financial.payroll`, or `financial.employee_compensation` tables, and does not require `financial.ledger_accounts`.

### Balanced Ledger Model

A single `direction` field without account identity is insufficient for a balanced double-entry-style journal. Every posting therefore has one explicit `financial.ledger_transactions` header and two or more child `financial.ledger_entries` that identify accounts and directions.

`financial.ledger_transactions` is the canonical ledger posting/finalization aggregate root. One header owns exactly one currency and all of its entries. `source_type` and `source_id` identify the provider-neutral business fact or operation that caused the posting; provider object identifiers remain on the applicable Payment, PaymentAttempt, Refund, Payout, or ProviderEvent record and are not the ledger's aggregate identity.

Conceptual schema (exact constraint, trigger, and function names are deferred to Flyway migration design):

```sql
CREATE TABLE financial.ledger_transactions (
    id UUID PRIMARY KEY,
    currency_code CHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('DRAFT', 'POSTED')),
    posting_type VARCHAR(50) NOT NULL,
    source_type VARCHAR(50) NOT NULL,
    source_id UUID NOT NULL,
    compensates_ledger_transaction_id UUID NULL
        REFERENCES financial.ledger_transactions(id),
    entry_count INTEGER NOT NULL DEFAULT 0 CHECK (entry_count >= 0),
    total_debit_minor BIGINT NOT NULL DEFAULT 0 CHECK (total_debit_minor >= 0),
    total_credit_minor BIGINT NOT NULL DEFAULT 0 CHECK (total_credit_minor >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    posted_at TIMESTAMPTZ NULL,
    CHECK (
        (status = 'DRAFT' AND posted_at IS NULL)
        OR (status = 'POSTED' AND posted_at IS NOT NULL)
    ),
    UNIQUE (source_type, source_id, posting_type)
);

CREATE TABLE financial.ledger_entries (
    id UUID PRIMARY KEY,
    ledger_transaction_id UUID NOT NULL
        REFERENCES financial.ledger_transactions(id),
    account_code VARCHAR(100) NOT NULL,
    entry_type VARCHAR(50) NOT NULL,
    amount_minor BIGINT NOT NULL CHECK (amount_minor > 0),
    direction VARCHAR(10) NOT NULL CHECK (direction IN ('DEBIT', 'CREDIT')),
    order_id UUID NULL REFERENCES "order".orders(id),
    chef_order_group_id UUID NULL REFERENCES "order".chef_order_groups(id),
    payment_id UUID NULL,
    refund_id UUID NULL,
    payout_id UUID NULL,
    source_type VARCHAR(50) NOT NULL,
    source_id UUID NOT NULL,
    entry_snapshot JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

The header owns `currency_code`; entries do not independently select a currency. This makes currency mixing structurally impossible within one ledger transaction. A compensating transaction must use the currency of the transaction it compensates.

`posting_type` distinguishes independently postable movements for one source when required and participates in the idempotent source uniqueness key. The Financial module owns the finite, documented values for `posting_type`, `source_type`, `entry_type`, and `direction`; callers and providers cannot supply arbitrary canonical values.

Source/business identity, posting type, and account code are distinct:

```text
SOURCE / BUSINESS REFERENCE = what occurrence caused the posting
POSTING TYPE                = what kind of financial posting this is
ACCOUNT CODE                = accounting classification of one LedgerEntry
```

Conceptual posting classifications may include `PAYMENT_CAPTURE`, `PAYMENT_REFUND`, `EARNING_RECOGNITION`, `SUBSIDY_FUNDING`, `FEE_RECOGNITION`, `PAYOUT`, `PAYOUT_REVERSAL`, and `FINANCIAL_ADJUSTMENT`. These examples establish an extensible controlled classification rather than a finalized enum or food-only taxonomy. No Dietitian-food-specific `COMMISSION_RECOGNITION` requirement is retained. This does not conclude that no future approved commercial model could ever require another controlled classification; such a model requires its own decision. One atomic recognized financial occurrence may include multiple related economic effects in one balanced transaction; conversely, payment, later earning, and later payout must not be forced into one LedgerTransaction when they occur at different economic times. Posting granularity follows economic atomicity.

The conceptual `source_type`/`source_id` metadata remains useful for posting and event correlation, but this ADR does not approve an unconstrained universal business relationship based only on arbitrary type plus UUID. Future ADR-020 and the canonical ERD must define typed relational source/obligation links where stronger integrity is required. No universal business aggregate is introduced merely to simplify ledger references.

### Posting Lifecycle and Database Enforcement

The only lifecycle is:

```text
DRAFT → POSTED
```

`POSTED` is terminal. A DRAFT header and its entries may be assembled only inside the local posting transaction; normal operation must never commit a DRAFT posting for later completion. Before allowing the transition to POSTED, PostgreSQL must independently:

1. serialize finalization and child-entry mutation on the parent ledger transaction;
2. reject a transaction with fewer than two entries;
3. calculate the entry count, total debit minor units, and total credit minor units from the persisted child entries;
4. persist those validated values on the header;
5. require total debits to equal total credits and require the balanced total to be greater than zero; and
6. when compensation is specified, reject self-reference and require the referenced transaction to be POSTED in the same currency; and
7. set `posted_at` from the database clock.

Application validation may fail early, but it is not the authoritative balance control. A PostgreSQL trigger on the `DRAFT → POSTED` transition, or an equivalently protected database finalization function plus database guards, must enforce these rules even when SQL bypasses the application service. Direct status updates must not provide a path around finalization. Exact trigger/function names and privilege statements belong to migration design rather than this ADR.

Database guards on ledger-entry insert, update, and delete must lock/check the parent and permit mutation only while it remains DRAFT. Locking must prevent entry mutation from racing with finalization.

### Immutability and Corrections

After POSTED:

- the ledger transaction header cannot be updated, reverted to DRAFT, or deleted;
- its ledger entries cannot be inserted, updated, or deleted; and
- its validated entry count, debit total, credit total, currency, source references, posting type, and posting timestamp cannot change.

Database enforcement, not application convention alone, must protect posted headers and entries. A correction creates and posts a new balanced ledger transaction. When it reverses or corrects a specific posting, `compensates_ledger_transaction_id` references that original transaction. Compensation is an audit link, not permission to mutate or mark the original transaction as superseded.

A correction may be a full reversal or a partial adjustment according to the accepted business fact. It always creates a new balanced LedgerTransaction and never edits the original economic history.

### Account-Code Governance

`account_code` is a required, controlled financial classification owned by the Financial domain. It is not arbitrary caller-controlled free text. The Financial domain owns the vocabulary of valid account codes and their stable, documented accounting meanings. Application/domain logic and database persistence must reject unknown or unauthorized codes. Provider-specific codes supplied by callers are not canonical ledger account codes.

Account codes may be deprecated, but a code used by a posted entry must retain its original value and historical meaning. Renaming, deleting, or reinterpreting such a code is prohibited; evolution introduces a new code while preserving historical evidence.

The exact database-backed representation and enforcement mechanism is deferred to domain and Flyway migration design. Valid options include a lookup/reference table, an appropriate CHECK or enum-like database constraint, seeded controlled reference data, or an equivalent database-backed mechanism. This ADR does not select one of those representations and does not introduce a full chart-of-accounts subsystem.

The Financial domain owns valid account-code semantics. Callers cannot generate free-form canonical account codes. Exact Chart of Accounts values and whether a future operational need justifies an account registry remain accounting/implementation design; a `financial.ledger_accounts` table is not mandatory.

### Atomic Posting Boundary

One local PostgreSQL transaction must atomically include:

1. the authoritative Payment, PaymentAllocation, Refund, RefundLine, Payout, PayoutLine, or other approved financial state change;
2. insertion of the DRAFT ledger transaction header;
3. insertion of all ledger entries;
4. the database-enforced transition of the balanced header to POSTED; and
5. insertion of the transactional-outbox row for `LedgerTransactionPosted.v1` and any other events caused by the same state change.

If any step fails, all steps roll back. The outbox publisher emits only after commit. External provider calls do not participate in the PostgreSQL transaction: their idempotently persisted result or verified webhook drives the subsequent local atomic financial posting. This preserves provider separation without weakening local consistency.

### Idempotency

Financial commands use idempotency keys. Reuse of a key with a different request hash must be rejected.

Posting is also idempotent at the Financial command/business-event boundary. Retrying the same recognized financial fact must not create duplicate economic postings, regardless of broker delivery semantics. Corrections are new intentional facts, not duplicate retries. Exact key and index representation remains for detailed design and the canonical ERD.

### Provider Events

Provider webhook deduplication uses:

```text
UNIQUE(provider_name, provider_event_id)
```

Processing must be idempotent and publish local downstream events through the transactional outbox where applicable.

### Reconciliation Boundary

Reconciliation compares immutable Cheffy Bites financial records and posted ledger transactions with provider reports, balance transactions, fees, transfers, refunds, disputes, and payouts. Provider data is evidence for matching; it is not the canonical ledger and must not rewrite posted transactions.

Reconciliation must retain durable provider-neutral links to the applicable provider reference, Payment, PaymentAttempt, Refund, Payout, commercial source/reference, currency, amount, date/time, connected account/recipient where applicable, ProviderEvent, and LedgerTransaction. Provider identifiers remain confined to provider-aware financial/infrastructure fields. Free-text matching is not the primary architecture. A mismatch creates an auditable reconciliation exception/investigation. Any accounting correction is represented by a new approved financial adjustment and balanced compensating LedgerTransaction through the normal posting boundary; provider data never silently edits ledger history.

Payment-processor fees are distinct from Cheffy Platform marketplace fees. Processor fees may be learned from provider settlement evidence and reconciled; Platform fees arise from Cheffy's approved commercial economics. Their exact accounting treatment remains for accounting design and ADR-020.

Merchant-of-Record ownership, tax/remittance liability, chargeback/refund liability, connected-account topology, reserve and negative-balance policy, and country-specific settlement rules remain legal/accounting/provider gates. This ADR does not infer those decisions or encode them into account meanings before approval.

## Multi-Context Economic Capability

The ledger must be capable of recording accepted financial facts originating from Food Orders, ChefOrderGroups, commercial-provider relationships, KitchenBookings, Dietitian Appointments, Meal Subscription billing/fulfillment, Kitchen Subscription billing/access obligations, Refunds, Payouts, Platform subsidies, and financial corrections. Business-domain aggregates retain their own lifecycle state; Order, Appointment, KitchenBooking, MealSubscription, ChefKitchenSubscription, MealFulfillmentOccurrence, cancellation, fulfillment, and entitlement state do not become ledger aggregate state.

`PaymentAllocation` remains an internal payment-side allocation/reference. It is not a LedgerEntry and does not prove earning, payout eligibility, external settlement, or ledger account classification. PromotionApplication, PricingSnapshot, ChefOrderGroup, and business fulfillment state are also upstream evidence rather than LedgerEntries. Multiple ChefOrderGroups may contribute to one commercial-provider obligation without forcing one PaymentAllocation recipient or LedgerEntry beneficiary per performer. A posting may reference validated immutable allocation and commercial evidence, but it does not recompute historical economics from current configuration or duplicate the full PricingSnapshot inside each LedgerEntry. Exact generalized allocation/obligation representation remains for ADR-020 and the canonical ERD.

### Service Performer, Commercial Provider, and Settlement Beneficiary

The ledger records accepted commercial economic facts. Service-performer identity may remain source/reference evidence when financially or operationally relevant, but the performer is not automatically the earning or settlement beneficiary.

```text
SERVICE PERFORMER = who performed or prepared the service
COMMERCIAL PROVIDER = which approved marketplace business supplied it
SETTLEMENT BENEFICIARY = which approved party is owed settlement
```

These concepts may align for an independent provider but are not permanently equivalent. The ledger posts only the accepted commercial-provider/settlement relationship supplied by Financial policy and future ADR-020; it does not authorize arbitrary third-party routing or decide exact provider/payee tables and foreign keys.

For Organization-operated food, Order O1 at Kitchen K1 may contain ChefOrderGroup Ravi and ChefOrderGroup Maria while ABC Food Group is the commercial provider and settlement beneficiary. Ravi and Maria remain traceable through Order/ChefOrderGroup source evidence, but the ledger must not post individual employee marketplace earnings and redirect them to ABC merely because they performed the work. The accepted commercial obligation belongs to the applicable ABC provider/payee arrangement.

```text
CHEFORDERGROUP != COMMERCIAL PROVIDER
CHEFORDERGROUP != SETTLEMENT BENEFICIARY
CHEFORDERGROUP != PAYOUT
CHEFORDERGROUP != LEDGER ACCOUNT
```

Multiple ChefOrderGroups may contribute to one Organization's commercial economics without being collapsed. One ChefOrderGroup does not require one earning beneficiary, one Payout, or one connected account. ADR-013 remains authoritative for actual-Chef grouping.

The same ledger architecture supports an independent Chef where Ravi is the performer and Ravi Foods Organization is commercial provider/beneficiary. Cheffy Operations uses the same ordinary Organization semantics: a Cheffy-employed Chef remains performer evidence while Cheffy Operations Organization is the applicable provider/beneficiary. No provider-specific Cheffy branch is introduced, and a later exit from direct supply requires no ledger redesign.

### Marketplace Settlement and Payroll Boundary

```text
MARKETPLACE SETTLEMENT != EMPLOYEE / CONTRACTOR PAYROLL
```

ABC Food Group may later compensate Ravi through salary, hourly wage, employee bonus, lawful incentive, or contractor compensation under its own worker arrangement. Employee gross wages, payroll tax, withholding, vacation pay, benefits, employer contributions, bonus accrual, and payroll remittance are not automatically part of Cheffy's marketplace ledger. This ADR introduces no employee-payroll accounting or payroll postings. Any future Cheffy internal accounting/payroll integration is a separate architecture concern.

### Customer-Funded Unfulfilled Value

The ledger must represent received Customer/Buyer funds that are not yet fully provider-earned as a distinguishable economic stage until ADR-020-defined recognition or remediation facts occur. This neutral concept may be described as customer-funded unfulfilled value, not-yet-earned value, or a deferred/unrecognized economic obligation. It must not be called **escrow** unless legal, accounting, and provider review explicitly approves that regulated characterization and operating model.

ADR-015 does not choose liability/deferred-revenue account names or recognition timing. It requires only that the double-entry model can faithfully post the policy result supplied by ADR-020.

### Meal Subscription Economics

A Meal Subscription billing-cycle payment does not automatically make the full cycle amount provider-earned. Provider earning is primarily tied to qualifying fulfilled MealFulfillmentOccurrences under approved product policy, so separate economic times may produce separate postings for:

- billing/payment funding received;
- customer-funded value for future unfulfilled obligations;
- occurrence-level earning recognition;
- Customer refund/remediation;
- Platform subsidy;
- provider-funded Promotion effects;
- Platform fee; and
- later payout eligibility/settlement stages where applicable.

The captured economic allocation from Pricing and future ADR-020 controls the amounts. ADR-015 does not assume monthly payment divided evenly by meal count, decide recognition timing, or define exact account codes/journal entries. Under Organization-operated supply, the commercial provider may be an Organization employing the actual occurrence-level Chef performer; performer traceability does not make that Chef the earning or payout beneficiary.

### Kitchen Subscription Economics

Kitchen Subscription is contracted access/capacity entitlement and must not copy the Meal Subscription occurrence-earning model. Commercial-provider earning is not restricted only to physically used KitchenBooking hours. Under the accepted terms, economics may become earned when contracted access/capacity was genuinely provided even if the Chef voluntarily used only part of expiring entitlement.

Future ADR-020 and Kitchen-subscription policy must distinguish voluntary unused entitlement where contracted capacity was provided from provider-caused inability to supply contracted capacity. Provider failure may create a refund, credit, extension, replacement, obligation release, or other approved remediation. ADR-015 records the resulting balanced facts without deciding which remedy or recognition policy applies. The approved commercial provider may be the authorized Kitchen operator Organization rather than the property owner. This ADR does not model landlord rent, lease accounting, property-owner payments, or lease liabilities.

### Dietitian Consultation Economics

Dietitian Appointment payment success does not itself establish Dietitian earning. The ledger supports separate accepted facts for Customer contribution, Appointment payment/funding, completed professional service, provider-funded consultation discount where allowed, Dietitian cancellation/no-show, Customer cancellation/no-show, refund/remediation, Platform-funded consultation subsidy, ordinary Platform fee, optional Platform-fee discount/waiver, earning recognition when policy permits, payout eligibility, and external settlement. Dietitian/provider cancellation or no-show produces no Dietitian earning for the unprovided service. Exact Customer cancellation/no-show economics, recognition, provider/payee relationship, and payout eligibility follow captured policy and ADR-020; Appointment lifecycle does not move into the ledger aggregate.

```text
PAYMENT RECEIVED
!= DIETITIAN EARNING RECOGNIZED
!= PAYOUT ELIGIBLE
!= EXTERNAL PAYOUT COMPLETED
```

Where legally and professionally permitted, the actual practicing Dietitian remains source/performer evidence while a clinic/Organization may be the commercial provider and settlement beneficiary. This professional-provider model does not create Dietitian food-sale economics.

### Removed Dietitian Food-Commission Model

Dietitian food-sale, Meal Subscription, recommendation/referral, Chef-purchase, or ChefOrderGroup commission is not current product scope. The ledger does not require commission deducted from Chef/provider proceeds, Dietitian food-commission recognition, attribution conflict, or food-commission refund reversal. ADR-020 must not model those obligations under the current decision. Reconsideration requires a new explicit product, professional-regulatory, Promotion, financial, and architecture decision.

### Platform Subsidy and Platform-Fee Benefits

Platform-funded Customer-price subsidy is a funding component distinct from Customer cash collected. For example, a $70 consultation may combine a $20 Customer contribution and a $50 Platform subsidy while retaining $70 gross service economics under the applicable terms. Customer Payment amount therefore need not equal total provider gross service value.

The ledger must distinguish Platform-funded Customer subsidy, ordinary Platform fee, Platform-fee discount, and Platform-fee waiver rather than netting them into one unexplained discount. Provider-funded Customer-price Promotions may reduce provider economics under their accepted terms, while Platform-funded subsidies normally do not reduce provider gross economics in the same way. Promotion/Pricing supplies immutable commercial calculation and funding evidence; future ADR-020 supplies the resulting obligation/recognition facts; ADR-015 posts them. Promotion evidence is not ledger evidence.

For example, commercial food value of $30 may comprise $20 Customer contribution plus $10 Platform-funded subsidy for ABC Food Group as commercial provider. The ledger must be able to explain the relevant funded commercial value under accepted policy and must not infer provider gross economics only from the Customer Payment amount.

A provider-funded Promotion may calculate against ChefOrderGroup Ravi while ABC Food Group bears the discount economics under the accepted arrangement. ChefOrderGroup scope is not proof that Ravi personally funded the Promotion, and no employee compensation effect is posted. ADR-014 decides Promotion behavior, ADR-020 decides the resulting obligation, and ADR-015 posts the accepted fact.

```text
PLATFORM CUSTOMER SUBSIDY
!= PLATFORM-FEE DISCOUNT
!= PLATFORM-FEE WAIVER
```

The Platform fee applies to the applicable commercial-provider relationship. An employed performer does not personally owe that fee merely because they performed the service.

### Refund, Remediation, Provider Failure, and Customer Credit

Refunds and remediation create new balanced financial facts and never rewrite the original Payment or posted LedgerTransactions. A subsidized or multi-party adjustment may affect Customer contribution, provider-funded discount, commercial-provider earning, Customer-funded unfulfilled value, Platform fee, Platform subsidy, delivery, tax, or other captured obligations. Legitimate Dietitian consultation adjustments remain consultation facts; food adjustments contain no Dietitian recommendation/referral commission. The ledger uses the approved adjustment derived from durable transaction-time economic evidence and does not assume naive proportional allocation unless ADR-020/Pricing explicitly establishes that method for the case.

Provider-caused non-performance—including Dietitian cancellation/no-show, food-provider non-fulfillment, inability to provide contracted Kitchen capacity, cancelled confirmed KitchenBooking, or subscription-occurrence failure—may produce refunds, obligation release, subsidy release, fee reversal, earning reversal/adjustment, credits, payout adjustment, or other remediation as new balanced transactions. Original funded/earned history remains immutable; business policy and ADR-020 determine the required adjustment. Under Organization-operated supply, provider-failure economics apply to the commercial-provider arrangement and do not automatically become an employee payroll deduction.

A future Customer/Platform credit may be supported only when separately approved. It is a distinct financial obligation/value and is not a cash refund, promo code, Platform subsidy, or provider earning. This ADR does not add Customer-credit tables or assume every refund becomes credit; refund to the original payment method may remain the required default protection.

### Payout Stages

Provider earning recognition, payout eligibility, and external payout completion remain distinct. ADR-020 determines payout eligibility policy, ADR-012 orchestrates approved provider payout/settlement activity, and ADR-015 records and reconciles the resulting movements. Earning does not require immediate payout, and no one-Payout-per-Order, ChefOrderGroup, Appointment, KitchenBooking, or subscription-cycle rule is introduced.

Settlement may aggregate accepted obligations across multiple Orders, ChefOrderGroups, Appointments, Bookings, subscription occurrences, or other approved sources under future payout policy.

## Operational Flows

```text
Payment → Attempts → Confirmation → Payment-Side Allocations → Balanced Funding Posting
Accepted Recognition Fact → Balanced Recognition Posting → Separately Determined Payout Eligibility
Refund Request → Refund → Refund Lines → Adjustments → Balanced Compensating Ledger Transaction
Eligibility → Payout → Payout Lines → Provider Settlement → Balanced Settlement Ledger Transaction → Reconciliation
```

One commercial relationship may therefore produce multiple LedgerTransactions over time. One atomic recognized financial occurrence may still use one balanced LedgerTransaction containing all of its related effects; needless fragmentation is not required.

## Financial Invariants

- Money uses integer minor units plus ISO currency.
- A ledger transaction owns exactly one currency and cannot mix currencies.
- Every LedgerEntry belongs to exactly one LedgerTransaction.
- Only the database-enforced DRAFT to POSTED transition finalizes a ledger transaction.
- Every POSTED ledger transaction has at least two entries and equal, positive debit and credit totals.
- POSTED ledger transaction headers and their entries are immutable.
- Refunds/adjustments reference original sources where applicable.
- Food economic facts may reference ChefOrderGroup as actual-Chef performer/source evidence without making it the commercial provider, beneficiary, Payout, or ledger account.
- Provider events and financial commands are idempotent.
- Historical PromotionSnapshots and PricingSnapshots remain immutable calculation evidence; immutable Financial-domain records and posted LedgerTransactions/LedgerEntries preserve settled and accounting evidence.
- Corrections use new balanced transactions and never rewrite posted history.
- Payment received, earning recognized, payout eligible, and external payout completed are separate facts.
- Promotion application is upstream evidence; accepted provider-funded Promotion, Platform-subsidy, and Platform-fee facts are later Financial postings.
- Customer-funded unfulfilled value remains distinguishable until accepted recognition or remediation policy resolves it.
- Marketplace settlement remains separate from employee or contractor payroll.
- Historical economics use durable transaction-time Pricing/Financial evidence and are not recomputed from current Promotion, Organization membership, Chef employer, Platform fee, provider account, Kitchen operator, or Subscription price.

## Events

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
LedgerTransactionPosted.v1
```

Events follow ADR-016.

`LedgerTransactionPosted.v1` represents successful finalization of one balanced LedgerTransaction posting unit, not creation of an individual LedgerEntry. It is inserted into the transactional outbox in the same local transaction that changes the header to POSTED. It must not exist for DRAFT or failed postings.

At ADR level, the event identifies:

```text
ledgerTransactionId
currencyCode
postingType
sourceType
sourceId
compensatesLedgerTransactionId (optional)
entryCount
totalDebitMinor
totalCreditMinor
postedAt
```

The published contract uses the ADR-016 versioned envelope and canonical name `LedgerTransactionPosted.v1`. Account-level entries and snapshots remain authoritative database evidence and are not required in the integration-event payload. The event is provider-neutral and contains no provider credentials or raw sensitive provider payload.

Its aggregate identity remains `aggregateType = LEDGER_TRANSACTION` and `aggregateId = ledgerTransactionId`, with `eventVersion = 1` and matching `.v1` event type under ADR-016. The payload does not add its own `schemaVersion` and does not embed every LedgerEntry. Downstream consumers treat it as notification of an already durable, database-finalized POSTED fact; they do not decide whether the ledger balanced. New commercial domains do not change these semantics.

## Responsibility Boundary

### ADR-015 owns

- LedgerTransaction and LedgerEntry;
- double-entry balance and one-currency transactions;
- Financial-controlled account codes;
- database-safe `DRAFT → POSTED` finalization and posted immutability;
- new balanced compensating/adjustment transactions;
- atomic local financial posting plus transactional outbox;
- the `LedgerTransactionPosted.v1` boundary;
- Financial posting idempotency; and
- internal ledger versus external-provider reconciliation and exception/evidence principles.

### Future ADR-020 owns

- commercial-provider economic obligations and generalized source/obligation relationships;
- settlement-beneficiary relationships and financially relevant service-performer references;
- customer-funded unfulfilled-value semantics;
- provider earning-recognition policy;
- Platform subsidy/fee and provider-funded Promotion obligation semantics;
- refundability and remediation economics; and
- payout eligibility.

### ADR-012 owns

- Payment, PaymentAttempt, and provider-neutral payment/refund orchestration;
- ProviderEvents; and
- provider settlement/payout orchestration after authorization.

### ADR-014 and Pricing own

- Promotion calculation and application evidence;
- PricingSnapshot; and
- subsidy and fee-benefit commercial calculation evidence.

### Business domains own

- Order, Appointment, KitchenBooking, MealSubscription, ChefKitchenSubscription, and MealFulfillmentOccurrence; and
- cancellation, fulfillment, entitlement, and other business lifecycle state.

### ADR-013 owns

- actual Chef performer and ChefOrderGroup operational identity.

### Organization / business architecture owns

- commercial-provider identity and business lifecycle;
- Organization membership and performer engagement/authorization; and
- Kitchen operating authority.

### HR / payroll outside the marketplace owns

- wages, salary, employee bonus, and worker commission/incentive;
- withholding and payroll taxes; and
- payroll remittance and other worker-compensation accounting.

## Out of Scope

This ADR does not decide the exact Chart of Accounts, exact account codes, a mandatory ledger-accounts table, ADR-020 aggregate/table design, exact provider/payee foreign keys, recognition timing, refund accounting policy, subscription revenue recognition, employee payroll, lease/landlord accounting, tax or Merchant-of-Record legal conclusions, connected-account legal model, chargeback liability, reserves, payout cadence, reconciliation job schema, event payload changes, or API fields.

## Consequences

### Positive

- Resolves the `payment` versus `financial` schema contradiction.
- Makes posting finalization, source uniqueness, currency ownership, and balance evidence database-visible.
- Database-enforced balanced journal and immutability semantics improve reconciliation and auditability.
- Strong ChefOrderGroup traceability.
- Provider-neutral design remains possible.
- Can record approved multi-context and Organization-provider funding, recognition, subsidy, fee, remediation, and settlement stages without conflating them with performer identity or payroll.

### Negative

- Requires accounting-aware implementation.
- Requires cross-row PostgreSQL finalization and immutability guards plus careful lock design.
- Requires governed ledger-account migrations and compensating postings instead of direct corrections.
- Reconciliation and recovery are more complex.
- Merchant-of-Record and tax liability remain external dependencies.
- Economic policy and generalized non-ledger relationships remain dependent on future ADR-020 and canonical persistence design.

## Dependencies

ADR-012, ADR-013, ADR-014, ADR-016, and future ADR-020 for commercial-provider obligations, settlement-beneficiary relationships, earning recognition, Platform-subsidy/fee and provider-funded Promotion obligations, refund/remediation economics, payout eligibility, and generalized commercial-source relationships. ADR-020 must not model Dietitian food-sale, Meal Subscription, recommendation/referral food commission, or employee payroll under the current product decision.
