# ADR-012 — Payment / Marketplace Settlement

## Status

Proposed

## Context

Cheffy Bites is a multi-sided marketplace where customers pay for food orders, Chefs receive payouts for their food contributions, Entrepreneurs receive payouts for kitchen bookings and equipment rentals, and the platform collects applicable fees. A single customer checkout may create financial obligations to multiple recipients.

The payment architecture must support one customer payment for a multi-Chef Order, internal allocation across multiple recipients, ChefOrderGroup-level traceability for food-related financial activity, provider-neutral architecture, and immutable financial records for audit and reconciliation.

ADR-015 defines the canonical financial persistence and ledger model. This ADR defines payment and settlement orchestration.

## Decision

We will implement a centralized marketplace checkout with payment orchestration, internal allocation, payout eligibility, and provider-neutral settlement integration.

### Core Principles

#### 1. One Customer Payment → Multiple Allocations

A customer checkout creates one logical Payment for one Order. That Payment may be distributed into multiple internal PaymentAllocations for Chef proceeds, platform fees, delivery, tax obligations where applicable, Entrepreneur kitchen bookings or equipment rentals, and other explicitly approved financial obligations.

For the current checkout architecture, an Order has at most one logical Payment. This requires conceptual uniqueness equivalent to `UNIQUE(order_id)` on Payment. A future model allowing multiple independent payments for one Order, including split tender, requires a separate approved business and architecture decision.

One provider interaction is not one Payment. A Payment can have multiple PaymentAttempts while remaining the same logical customer payment.

#### 2. Provider-Neutral Architecture

Stripe Connect may be the initial payment provider, but the domain model must not depend on Stripe-specific concepts. Provider-specific APIs are isolated behind a payment gateway adapter.

#### 3. Canonical Financial Ownership

All financial persistence uses the PostgreSQL `financial` schema defined by ADR-015. This ADR does not introduce a competing `payment` schema.

#### 4. Financial Immutability

Financial facts are append-only. Historical financial records must not be overwritten. Refunds, reversals, corrections, and adjustments create new financial records and, where accounting postings are required, new ledger transactions and entries under ADR-015.

#### 5. Merchant of Record Remains Unresolved

This ADR does not decide Merchant of Record, tax collection or remittance responsibility, chargeback liability, refund liability, connected-account topology, reserve policy, negative-balance handling, country-specific settlement timing, provider risk requirements, marketplace legal obligations, or final settlement obligations. These remain legal, accounting, and provider approval gates. Foundational schema terminology must not be interpreted as deciding them.

## Financial Flow

```text
Customer
    ↓
Checkout (Pricing + Promotion + Tax + Fees)
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
Payout Eligibility
    ↓
Payout + Payout Lines
    ↓
External Provider Settlement
    ↓
Reconciliation
```

## Architecture Components

### Payment Aggregate

`Payment` is Cheffy Bites' logical customer payment for exactly one Order. It is a Financial-domain aggregate and is not a provider payment/session object. The current model permits at most one Payment per Order and does not support split tender or multiple independent payments for one checkout.

A Payment owns the logical amount and currency expected from the customer and coordinates zero or more PaymentAttempts. Provider retries or additional provider interactions update the attempt history and resulting Payment state; they do not create another logical Payment.

### PaymentAttempt

`PaymentAttempt` is one retryable provider interaction belonging to exactly one logical Payment. Examples include initial authorization or charge initiation, a retry after a retryable provider failure, or an additional interaction required to complete the same Payment. The model does not prescribe provider-specific retry semantics.

Every PaymentAttempt has its own Cheffy identity. A migration-ready model must also provide an unambiguous attempt sequence or equivalent uniqueness within its parent Payment. The selected `provider_name` identifies the adapter. A generic `provider_payment_reference` may be null before the provider accepts or creates an interaction when the provider flow genuinely requires that ordering. Once present, the `(provider_name, provider_payment_reference)` combination must not identify multiple PaymentAttempts. Provider adapters map this generic reference to a Stripe PaymentIntent ID, another provider's session or transaction reference, or an equivalent identifier.

Attempt-level monetary values, when present, use integer minor units and a currency code consistent with the parent Payment.

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

### Financial Command Idempotency

Financial commands retain Cheffy's operation/key/request-hash model through `financial.idempotency_keys` or its canonical equivalent. Reusing one operation and idempotency key with a different request hash must be rejected. Provider idempotency keys may additionally be used by adapters, but they do not replace Cheffy's financial idempotency controls.

### PaymentAllocation

`PaymentAllocation` is an internal, provider-neutral Cheffy financial fact. It records how customer payment value is logically distributed into approved obligations or shares. It is not proof that funds have been transferred by a provider and is not equivalent to a connected-account transfer.

Each allocation requires:

- its parent Payment and source Order;
- allocation type;
- `amount_minor` and `currency_code` consistent with the Payment;
- recipient type and a typed relational recipient reference where that allocation type has a recipient;
- `chef_order_group_id` and the applicable Chef recipient reference for Chef proceeds;
- a typed delivery, tax, booking, equipment-rental, or other approved source-obligation reference where that allocation type requires one; and
- immutable calculation/source evidence sufficient for audit without replacing relational ownership with polymorphic JSONB.

Recipient and source cardinality is conditional on allocation type. A required recipient/source reference must be non-null for a type that needs it and must be null when it is inapplicable, enforced by the canonical relational model and migration constraints. `chef_order_group_id` is required for Chef food proceeds but is not required for platform, delivery, tax, or other allocations that do not belong to a Chef group. Exact typed foreign keys and type-specific constraints belong to the canonical ERD and migration design; a generic JSONB relationship is not the canonical model.

### Internal Allocation and External Settlement

Internal allocation is Cheffy's authoritative logical financial distribution. External settlement is provider execution of transfers or payouts according to the eventually approved connected-account and legal model. These are separate facts and separate lifecycle steps.

Internal allocations can be modeled before the final external settlement topology is approved. Cheffy owns allocation rules, immutable financial records, payout obligations, reconciliation, and refund redistribution logic. The selected provider owns execution rails and provider-side account/compliance processes within the approved integration model.

### Financial Relationships

```text
One Order
    ↓ at most one
Logical Payment
    ├── PaymentAttempt 1
    ├── PaymentAttempt 2 (retry/additional interaction)
    └── Internal PaymentAllocations
            ├── Chef proceeds → ChefOrderGroup where applicable
            ├── Platform fee
            ├── Delivery obligation
            ├── Tax obligation where applicable
            └── Other approved obligations
                    ↓
            Ledger posting where required (ADR-015)
                    ↓
            Payout eligibility and obligations
                    ↓
            External provider settlement
```

This preserves one customer payment, one Order, one Kitchen, and multiple ChefOrderGroups when applicable. A separate customer payment is not created for each Chef.

### ChefOrderGroup Boundary

ADR-013 remains authoritative. ChefOrderGroup is the operational preparation, Chef authorization, Chef promotion, and Chef financial allocation/reference boundary. The Financial domain owns Payment, PaymentAllocation, Refund, Payout, LedgerTransaction, and LedgerEntry. Referencing `chef_order_group_id` does not transfer ownership of those financial facts to ChefOrderGroup.

### Refund Boundary

`Refund` is a Financial-domain aggregate. It traces the original Payment and Order. `RefundLine` preserves allocation-, item-, and ChefOrderGroup-level traceability where applicable, including the immutable evidence needed to explain partial refunds and redistribution.

Refunds and corrections append new financial evidence. They never rewrite the original Payment, PaymentAllocation, promotion snapshot, or ledger history. Exact provider refund references and provider-side attempts remain adapter concerns represented through generic provider references and immutable ProviderEvents.

### Payout Boundary

`Payout` is a Financial-domain aggregate representing an obligation or settlement toward an eligible recipient; its creation does not prove that an external provider transfer has completed. Provider transfer/payout state may update the Cheffy Payout through idempotent provider workflows.

Each `PayoutLine` traces the source payable obligation, allocation, Order, and ChefOrderGroup or other recipient source where applicable. The canonical model must enforce sufficient uniqueness/idempotency for the applicable settlement context so the same payable obligation cannot be settled twice. Payout obligations and calculations remain inside Cheffy's financial model rather than being manually calculated outside it.

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

### Money and Currency

All canonical monetary values use integer minor units plus currency code; floating point is prohibited. Currency consistency is required across Payment, monetary PaymentAttempt fields, PaymentAllocation, Refund, RefundLine, Payout, PayoutLine, and related ledger transactions/entries. Exact relational enforcement belongs to the canonical ERD and migration design.

### Local Transaction and Provider Coordination

No local PostgreSQL transaction spans an external provider call. No two-phase commit or distributed database/provider transaction is introduced. Provider operations are coordinated through idempotent commands, PaymentAttempts, immutable/deduplicated ProviderEvents, the transactional outbox, and reconciliation.

Provider success or failure appends evidence and drives idempotent local state transitions; it does not overwrite financial history. Reconciliation compares Cheffy's canonical financial truth with provider-reported truth. Discrepancies create auditable reconciliation evidence and investigation and, when correction is required, new compensating financial and ledger records under ADR-015. Provider state is not the sole system of record.

## Operational Rules

- One Order has at most one logical Payment under the current checkout model.
- One Payment may have many PaymentAttempts and many PaymentAllocations.
- Each PaymentAttempt has its own identity and belongs to exactly one Payment.
- Each allocation records the originating Payment and Order, allocation type, amount, currency, conditionally required typed recipient/source references, and calculation evidence.
- Chef-proceeds allocations reference the applicable `chef_order_group_id`; unrelated allocation types do not.
- Kitchen bookings and equipment rentals may generate obligations to Entrepreneurs.
- Payout lifecycle is `PENDING → ELIGIBLE → PROCESSING → SUCCESS|FAILED`, with `ON_HOLD` available.
- Payout creation records an obligation, not proof of provider settlement completion.
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

## Consequences

### Positive

- Supports one customer checkout with multiple financial recipients.
- Preserves ChefOrderGroup-level financial traceability.
- Keeps payment providers replaceable.
- Separates internal allocation from external settlement execution.
- Separates technical architecture from unresolved legal decisions.
- Supports reconciliation through ADR-015.

### Negative

- Allocation and reconciliation add implementation complexity.
- Provider integration requires careful idempotency handling.
- Type-specific allocation references and attempt uniqueness require explicit migration constraints.
- Settlement behavior depends on unresolved business/legal decisions.

## Implementation Notes

1. Use the `financial` schema defined by ADR-015.
2. Implement a provider-neutral `PaymentGateway`.
3. Return `PaymentInitiationResult` from payment initiation; do not expose a provider SDK object as the domain contract.
4. Use generic provider references and keep provider identifiers as integration references rather than domain identifiers.
5. Protect all financial commands with Cheffy operation/key/request-hash idempotency.
6. Deduplicate immutable provider events before side effects.
7. Publish domain events through the transactional outbox.
8. Use ADR-015 ledger transactions for accounting postings.
9. Add integration and end-to-end tests for duplicate commands, attempt retries, replayed webhooks, allocation cardinality, refund traceability, payout-obligation uniqueness, provider failure, and reconciliation.

## Alternatives Considered

- **Direct Provider Integration** — rejected because provider-specific concepts would leak into the domain architecture.
- **Single Payout Per Order** — rejected because it would not preserve sufficient ChefOrderGroup-level traceability.
- **Real-Time Payouts** — not selected as the default because payout eligibility requires configurable business rules, reconciliation, and risk controls.

## Dependencies

- ADR-013 — ChefOrderGroup Aggregate + Financial Boundary
- ADR-014 — Promotion Engine
- ADR-015 — Financial Ledger / Reconciliation
- ADR-016 — Event Versioning
