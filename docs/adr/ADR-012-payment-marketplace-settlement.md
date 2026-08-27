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

A successful customer payment may be allocated to ChefOrderGroups for food sales, Entrepreneur kitchen bookings, equipment rentals, platform fees, delivery charges, taxes, and other approved financial obligations.

For food orders, `ChefOrderGroup` is the authoritative allocation boundary for Chef-related proceeds.

#### 2. Provider-Neutral Architecture

Stripe Connect may be the initial payment provider, but the domain model must not depend on Stripe-specific concepts. Provider-specific APIs are isolated behind a payment gateway adapter.

#### 3. Canonical Financial Ownership

All financial persistence uses the PostgreSQL `financial` schema defined by ADR-015. This ADR does not introduce a competing `payment` schema.

#### 4. Financial Immutability

Financial facts are append-only. Historical financial records must not be overwritten. Refunds, reversals, corrections, and adjustments create new financial records and corresponding ledger entries.

#### 5. Merchant of Record Remains Unresolved

This ADR does not decide Merchant of Record, tax remittance responsibility, chargeback liability, refund liability, marketplace legal obligations, or final settlement obligations. These decisions require legal and accounting approval.

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
Payment Allocation(s)
    ↓
Financial Ledger Entries
    ↓
Payout Eligibility
    ↓
Payout + Payout Lines
    ↓
Provider Settlement
    ↓
Reconciliation
```

## Architecture Components

### Payment Gateway Interface

```java
public interface PaymentGateway {
    PaymentIntent createPayment(PaymentRequest request);
    PaymentConfirmation confirmPayment(String paymentIntentId);
    RefundResult processRefund(RefundRequest request);
    PayoutResult retrievePayout(String payoutId);
    void handleWebhook(WebhookEvent event);
}
```

The interface represents the domain boundary between Cheffy Bites and an external payment provider.

### Financial Relationships

```text
Payment
    ↓
Payment Attempt
    ↓
Payment Allocation
    ├── ChefOrderGroup
    ├── Entrepreneur Booking
    ├── Platform
    └── Delivery
    ↓
Ledger Transaction
    ↓
Payout Eligibility
    ↓
Payout
    ↓
Payout Line
    ↓
Provider Settlement
```

The canonical tables are defined by ADR-015.

## Operational Rules

- One payment may have many allocations.
- Each allocation records the originating Order, recipient, recipient type, allocation type, amount, currency, and calculation evidence.
- Food-related allocations must reference the applicable `chef_order_group_id`.
- Kitchen bookings and equipment rentals may generate obligations to Entrepreneurs.
- Payout lifecycle is `PENDING → ELIGIBLE → PROCESSING → SUCCESS|FAILED`, with `ON_HOLD` available.
- Refunds create new immutable financial records and reference the original financial source where applicable.
- Financial commands must use idempotency protection.
- Provider webhooks must be deduplicated using `(provider_name, provider_event_id)` before creating side effects.

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
- Separates technical architecture from unresolved legal decisions.
- Supports reconciliation through ADR-015.

### Negative

- Allocation and reconciliation add implementation complexity.
- Provider integration requires careful idempotency handling.
- Settlement behavior depends on unresolved business/legal decisions.

## Implementation Notes

1. Use the `financial` schema defined by ADR-015.
2. Implement a provider-neutral `PaymentGateway`.
3. Protect all financial commands with idempotency.
4. Deduplicate provider webhook events.
5. Publish domain events through the transactional outbox.
6. Keep provider identifiers as integration references rather than domain identifiers.
7. Add integration and end-to-end tests for payment-to-payout flows.

## Alternatives Considered

- **Direct Provider Integration** — rejected because provider-specific concepts would leak into the domain architecture.
- **Single Payout Per Order** — rejected because it would not preserve sufficient ChefOrderGroup-level traceability.
- **Real-Time Payouts** — not selected as the default because payout eligibility requires configurable business rules, reconciliation, and risk controls.

## Dependencies

- ADR-013 — ChefOrderGroup Aggregate + Financial Boundary
- ADR-014 — Promotion Engine
- ADR-015 — Financial Ledger / Reconciliation
- ADR-016 — Event Versioning
