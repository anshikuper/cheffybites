# ADR-015 — Financial Ledger / Reconciliation

## Status

Proposed

## Context

Cheffy Bites requires an append-only financial system of record for payments, allocations, refunds, adjustments, payouts, provider reconciliation, platform fees, delivery financial activity, and ChefOrderGroup-level seller traceability.

ADR-012 defines payment and settlement orchestration. This ADR defines canonical financial persistence and ledger semantics.

## Decision

### Core Principles

1. Financial business facts are append-only.
2. Corrections use compensating/refund/adjustment records.
3. The `financial` schema is the single canonical financial schema.
4. Provider identifiers are retained without making the domain provider-specific.
5. Provider events are deduplicated before side effects.
6. Every finalized financial movement is traceable to source and evidence.

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
financial.ledger_entries
```

### Balanced Ledger Model

A single `direction` field without account identity is insufficient for a balanced double-entry-style journal. Ledger postings therefore identify a transaction and account.

```sql
CREATE TABLE financial.ledger_entries (
    id UUID PRIMARY KEY,
    transaction_id UUID NOT NULL,
    account_code VARCHAR(100) NOT NULL,
    entry_type VARCHAR(50) NOT NULL,
    amount_minor BIGINT NOT NULL CHECK (amount_minor > 0),
    currency_code CHAR(3) NOT NULL,
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

For each finalized `(transaction_id, currency_code)`, total debits must equal total credits. The posting service validates this before finalization.

### Idempotency

Financial commands use idempotency keys. Reuse of a key with a different request hash must be rejected.

### Provider Events

Provider webhook deduplication uses:

```text
UNIQUE(provider_name, provider_event_id)
```

Processing must be idempotent and publish local downstream events through the transactional outbox where applicable.

## Operational Flows

```text
Payment → Attempts → Confirmation → Allocations → Balanced Ledger Transaction → Payout Eligibility
Refund Request → Refund → Refund Lines → Adjustments → Balanced Compensating Ledger Transaction
Eligibility → Payout → Payout Lines → Provider Settlement → Balanced Settlement Ledger Transaction → Reconciliation
```

## Financial Invariants

- Money uses integer minor units plus ISO currency.
- A financial transaction cannot mix currencies.
- Finalized ledger transactions balance.
- Finalized ledger rows are immutable.
- Refunds/adjustments reference original sources where applicable.
- Food seller allocations and payout lines reference ChefOrderGroup.
- Provider events and financial commands are idempotent.
- Historical promotion/financial snapshots are immutable evidence.

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

## Consequences

### Positive

- Resolves the `payment` versus `financial` schema contradiction.
- Balanced journal semantics improve reconciliation.
- Strong ChefOrderGroup traceability.
- Provider-neutral design remains possible.

### Negative

- Requires accounting-aware implementation.
- Reconciliation and recovery are more complex.
- Merchant-of-Record and tax liability remain external dependencies.

## Dependencies

ADR-012, ADR-013, ADR-014, ADR-016.
