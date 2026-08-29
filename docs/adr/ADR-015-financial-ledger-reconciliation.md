# ADR-015 — Financial Ledger / Reconciliation

## Status

Proposed

## Context

Cheffy Bites requires an append-only financial system of record for payments, allocations, refunds, adjustments, payouts, provider reconciliation, platform fees, delivery financial activity, and ChefOrderGroup-level seller traceability.

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

### Account-Code Governance

`account_code` is a required, controlled financial classification owned by the Financial domain. It is not arbitrary caller-controlled free text. The Financial domain owns the vocabulary of valid account codes and their stable, documented accounting meanings. Application/domain logic and database persistence must reject unknown or unauthorized codes. Provider-specific codes supplied by callers are not canonical ledger account codes.

Account codes may be deprecated, but a code used by a posted entry must retain its original value and historical meaning. Renaming, deleting, or reinterpreting such a code is prohibited; evolution introduces a new code while preserving historical evidence.

The exact database-backed representation and enforcement mechanism is deferred to domain and Flyway migration design. Valid options include a lookup/reference table, an appropriate CHECK or enum-like database constraint, seeded controlled reference data, or an equivalent database-backed mechanism. This ADR does not select one of those representations and does not introduce a full chart-of-accounts subsystem.

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

### Provider Events

Provider webhook deduplication uses:

```text
UNIQUE(provider_name, provider_event_id)
```

Processing must be idempotent and publish local downstream events through the transactional outbox where applicable.

### Reconciliation Boundary

Reconciliation compares immutable Cheffy Bites financial records and posted ledger transactions with provider reports, balance transactions, fees, transfers, refunds, disputes, and payouts. Provider data is evidence for matching; it is not the canonical ledger and must not rewrite posted transactions.

Reconciliation must retain provider-neutral links to the applicable Payment, PaymentAttempt, Refund, Payout, ProviderEvent, and ledger transaction while provider identifiers remain confined to provider-aware financial/infrastructure fields. A mismatch creates an auditable reconciliation exception. Any accounting correction is represented by a new approved financial adjustment and balanced compensating ledger transaction through the normal posting boundary.

Merchant-of-Record ownership, tax/remittance liability, chargeback/refund liability, connected-account topology, reserve and negative-balance policy, and country-specific settlement rules remain legal/accounting/provider gates. This ADR does not infer those decisions or encode them into account meanings before approval.

## Operational Flows

```text
Payment → Attempts → Confirmation → Allocations → Balanced Ledger Transaction → Payout Eligibility
Refund Request → Refund → Refund Lines → Adjustments → Balanced Compensating Ledger Transaction
Eligibility → Payout → Payout Lines → Provider Settlement → Balanced Settlement Ledger Transaction → Reconciliation
```

## Financial Invariants

- Money uses integer minor units plus ISO currency.
- A ledger transaction owns exactly one currency and cannot mix currencies.
- Every LedgerEntry belongs to exactly one LedgerTransaction.
- Only the database-enforced DRAFT to POSTED transition finalizes a ledger transaction.
- Every POSTED ledger transaction has at least two entries and equal, positive debit and credit totals.
- POSTED ledger transaction headers and their entries are immutable.
- Refunds/adjustments reference original sources where applicable.
- Food seller allocations and payout lines reference ChefOrderGroup.
- Provider events and financial commands are idempotent.
- Historical PromotionSnapshots and PricingSnapshots remain immutable calculation evidence; immutable Financial-domain records and posted LedgerTransactions/LedgerEntries preserve settled and accounting evidence.
- Corrections use new balanced transactions and never rewrite posted history.

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

## Consequences

### Positive

- Resolves the `payment` versus `financial` schema contradiction.
- Makes posting finalization, source uniqueness, currency ownership, and balance evidence database-visible.
- Database-enforced balanced journal and immutability semantics improve reconciliation and auditability.
- Strong ChefOrderGroup traceability.
- Provider-neutral design remains possible.

### Negative

- Requires accounting-aware implementation.
- Requires cross-row PostgreSQL finalization and immutability guards plus careful lock design.
- Requires governed ledger-account migrations and compensating postings instead of direct corrections.
- Reconciliation and recovery are more complex.
- Merchant-of-Record and tax liability remain external dependencies.

## Dependencies

ADR-012, ADR-013, ADR-014, ADR-016.
