# ADR-015 — Financial Ledger / Reconciliation

## Status

Proposed

## Context

The Cheffy Bites platform requires a financial ledger that serves as the system of record for all financial transactions. The ledger must support:

- Append-only financial history (immutability)
- Auditability of all financial actions
- Reconciliation between customer payments, Chef payouts, Entrepreneur payouts, platform fees, and delivery settlements
- Traceability from business events (orders, bookings) to financial outcomes
- Support for refunds, adjustments, and corrections without overwriting historical facts

The financial model must include:
- payments
- payment_attempts
- payment_allocations
- refunds
- refund_lines
- payouts
- payout_lines
- ledger_entries
- idempotency_keys
- provider_events
- promotion_snapshots
- financial_snapshots

## Decision

### Core Principles

1. **Financial Immutability**
   - Financial history is append-only from a business perspective
   - Do not overwrite historical financial facts
   - For corrections, create new financial events (adjustments/refunds)

2. **Ledger as System of Record**
   - The ledger is the authoritative source for all financial transactions
   - Every financial event creates a corresponding ledger entry
   - Ledger entries are immutable once created

3. **Traceability**
   - Every ledger entry must be traceable to its originating business event
   - Every payout can be reconciled to ledger entries
   - Every refund references the underlying financial allocation

4. **Provider Neutrality**
   - External provider identifiers are stored where required for reconciliation
   - Provider webhook processing requires provider-event deduplication
   - The architecture remains provider-neutral while Stripe Connect may be described as a candidate provider

### Architecture Components

#### Ledger Entry Model

```sql
CREATE TABLE financial.ledger_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- References to originating business events (nullable where applicable)
    order_id UUID NULL REFERENCES order.orders(id),
    chef_order_group_id UUID NULL REFERENCES order.chef_order_groups(id),
    payout_id UUID NULL REFERENCES financial.payouts(id),
    payout_line_id UUID NULL REFERENCES financial.payout_lines(id),
    payment_id UUID NULL REFERENCES financial.payments(id),
    refund_id UUID NULL REFERENCES financial.refunds(id),
    
    -- Entry classification
    entry_type VARCHAR(50) NOT NULL, -- CUSTOMER_CHARGE, CHEF_REVENUE, ENTREPRENEUR_REVENUE, PLATFORM_FEE, DELIVERY_FEE, TAX_COLLECTED, PROMOTION_DISCOUNT, REFUND, PAYOUT, PAYOUT_REVERSAL, FINANCIAL_ADJUSTMENT
    entry_scope VARCHAR(50) NULL, -- ORDER, CHEF_ORDER_GROUP, KITCHEN_BOOKING, PLATFORM, DELIVERY
    
    -- Financial details
    amount_minor BIGINT NOT NULL,
    currency_code CHAR(3) NOT NULL,
    direction VARCHAR(10) NOT NULL, -- DEBIT, CREDIT
    
    -- Supporting evidence
    entry_snapshot JSONB NOT NULL,
    
    -- Timestamps
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

#### Core Financial Tables

```sql
-- Payments (customer charges)
CREATE TABLE financial.payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES order.orders(id),
    provider VARCHAR(50) NOT NULL,
    provider_payment_intent_id VARCHAR(255) UNIQUE NOT NULL,
    idempotency_key VARCHAR(255) UNIQUE NOT NULL,
    status VARCHAR(50) NOT NULL,
    amount_minor BIGINT NOT NULL,
    currency_code CHAR(3) NOT NULL,
    provider_metadata JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Payment Attempts (for idempotency and retries)
CREATE TABLE financial.payment_attempts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id UUID NOT NULL REFERENCES financial.payments(id),
    provider_attempt_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    amount_minor BIGINT NOT NULL,
    currency_code CHAR(3) NOT NULL,
    provider_payload JSONB NOT NULL,
    attempted_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Payment Allocations (who gets paid what)
CREATE TABLE financial.payment_allocations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id UUID NOT NULL REFERENCES financial.payments(id),
    order_id UUID NOT NULL REFERENCES order.orders(id),
    chef_order_group_id UUID NULL REFERENCES order.chef_order_groups(id),
    recipient_type VARCHAR(50) NOT NULL, -- CHEF, ENTREPRENEUR, PLATFORM
    recipient_id UUID NOT NULL,
    allocation_type VARCHAR(50) NOT NULL, -- FOOD_ORDER, KITCHEN_BOOKING, PLATFORM_FEE, DELIVERY_FEE
    amount_minor BIGINT NOT NULL,
    currency_code CHAR(3) NOT NULL,
    allocation_evidence JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Refunds
CREATE TABLE financial.refunds (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id UUID NOT NULL REFERENCES financial.payments(id),
    order_id UUID NOT NULL REFERENCES order.orders(id),
    idempotency_key VARCHAR(255) UNIQUE NOT NULL,
    reason VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    requested_minor BIGINT NOT NULL,
    approved_minor BIGINT NOT NULL,
    currency_code CHAR(3) NOT NULL,
    provider_metadata JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Refund Lines (traceable to specific items or ChefOrderGroups)
CREATE TABLE financial.refund_lines (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    refund_id UUID NOT NULL REFERENCES financial.refunds(id),
    order_item_id UUID NULL REFERENCES order.order_items(id),
    chef_order_group_id UUID NULL REFERENCES order.chef_order_groups(id),
    line_type VARCHAR(50) NOT NULL, -- ITEM, CHEF_ORDER_GROUP, DELIVERY, FEE, TAX
    amount_minor BIGINT NOT NULL,
    currency_code CHAR(3) NOT NULL,
    refund_evidence JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Payouts (provider-managed payouts to recipients)
CREATE TABLE financial.payouts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_type VARCHAR(50) NOT NULL, -- CHEF, ENTREPRENEUR
    recipient_id UUID NOT NULL,
    idempotency_key VARCHAR(255) UNIQUE NOT NULL,
    status VARCHAR(50) NOT NULL,
    amount_minor BIGINT NOT NULL,
    currency_code CHAR(3) NOT NULL,
    provider_reference VARCHAR(255) NULL,
    provider_metadata JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Payout Lines (traceable to specific business events)
CREATE TABLE financial.payout_lines (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payout_id UUID NOT NULL REFERENCES financial.payouts(id),
    order_id UUID NOT NULL REFERENCES order.orders(id),
    chef_order_group_id UUID NULL REFERENCES order.chef_order_groups(id),
    kitchen_booking_id UUID NULL REFERENCES kitchen.kitchen_bookings(id),
    line_type VARCHAR(50) NOT NULL, -- GROSS, FEE, ADJUSTMENT, NET
    gross_minor BIGINT NOT NULL,
    fee_minor BIGINT NOT NULL,
    adjustment_minor BIGINT NOT NULL,
    net_minor BIGINT NOT NULL,
    currency_code CHAR(3) NOT NULL,
    calculation_snapshot JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Idempotency Keys (for operations with financial side effects)
CREATE TABLE financial.idempotency_keys (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    operation_type VARCHAR(50) NOT NULL, -- CREATE_ORDER, PAYMENT, REFUND, PAYOUT
    idempotency_key VARCHAR(255) UNIQUE NOT NULL,
    actor_user_id UUID NULL REFERENCES identity.users(id),
    request_hash VARCHAR(255) NOT NULL,
    response_snapshot JSONB NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Provider Events (for webhook deduplication)
CREATE TABLE financial.provider_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provider_name VARCHAR(50) NOT NULL,
    provider_event_id VARCHAR(255) NOT NULL,
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id UUID NOT NULL,
    payload JSONB NOT NULL,
    received_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    processed_at TIMESTAMPTZ NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'RECEIVED',
    UNIQUE(provider_name, provider_event_id)
);
```

#### Operational Rules

##### 1. Payment Flow
```text
Customer Payment
    ↓
Payment (system of record for customer charge)
    ↓
Payment Attempts (provider interactions)
    ↓
Payment Allocations (to ChefOrderGroup, Entrepreneur, Platform)
    ↓
Ledger Entries (one per allocation)
    ↓
Payouts (provider-managed)
    ↓
Payout Lines (traceable to business events)
    ↓
Ledger Entries (settlement records)
```

##### 2. Refund Flow
```text
Refund Request
    ↓
Refund (system of record)
    ↓
Refund Lines (traceable to source)
    ↓
Ledger Entries (refund records)
    ↓
Promotion Recalculation (if applicable)
    ↓
Adjustment Ledger Entries (if promotion invalidated)
```

##### 3. Payout Flow
```text
Payout Eligibility Determination
    ↓
Payout (system of record)
    ↓
Payout Lines (traceable to source)
    ↓
Ledger Entries (settlement records)
    ↓
Provider Payout (external)
```

##### 4. Provider Webhook Processing
```text
Provider Webhook
    ↓
Provider Event (deduplicated by provider_event_id)
    ↓
Ledger Entry Update (if applicable)
    ↓
Business State Update (order status, etc.)
    ↓
Outbox Event (for downstream systems)
```

### Event Integration

#### Core Financial Events
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
LedgerEntryCreated.v1
```

#### Event Payload Example (Payment Allocation)
```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "eventType": "PaymentAllocated.v1",
  "eventVersion": 1,
  "occurredAt": "2026-09-01T12:00:00Z",
  "aggregateType": "PAYMENT",
  "aggregateId": "550e8400-e29b-41d4-a716-446655440001",
  "correlationId": "550e8400-e29b-41d4-a716-446655440002",
  "payload": {
    "paymentId": "550e8400-e29b-41d4-a716-446655440001",
    "orderId": "550e8400-e29b-41d4-a716-446655440003",
    "allocations": [
      {
        "chefOrderGroupId": "550e8400-e29b-41d4-a716-446655440004",
        "amountMinor": 5000,
        "currencyCode": "CAD",
        "recipientType": "CHEF",
        "allocationType": "FOOD_ORDER"
      },
      {
        "entrepreneurId": "550e8400-e29b-41d4-a716-446655440005",
        "amountMinor": 2000,
        "currencyCode": "CAD",
        "recipientType": "ENTREPRENEUR",
        "allocationType": "KITCHEN_BOOKING"
      }
    ]
  }
}
```

### Consequences

#### Positive
- **Complete Audit Trail**: Every financial transaction is traceable to its source
- **Reconciliation Ready**: Payouts can be reconciled to ledger entries
- **Provider Flexibility**: Architecture supports multiple payment providers
- **Legal Compliance**: Supports financial regulations requiring immutable records
- **Dispute Resolution**: Full traceability enables effective dispute handling

#### Negative
- **Increased Storage**: More tables and JSONB snapshots increase storage requirements
- **Query Complexity**: Financial reports may require joins across multiple tables
- **Operational Overhead**: More components to monitor and maintain

### Implementation Notes

1. **Database Schema**
   - Add `financial` schema to PostgreSQL
   - Include all tables defined above
   - Add foreign key constraints for referential integrity
   - Add indexes for common query patterns

2. **Service Implementation**
   - Create `FinancialLedgerService` for ledger management
   - Implement `PaymentService` for payment orchestration
   - Add `PayoutService` for payout management
   - Create `RefundService` for refund processing

3. **Event Integration**
   - Add financial-related events to event contracts
   - Implement outbox pattern for reliable event publishing
   - Add webhook handlers for provider events with deduplication

4. **Testing**
   - Unit tests for ledger entry creation
   - Integration tests for payment-to-payout flow
   - E2E tests for refund recalculation scenarios
   - Property-based tests for financial invariants

### Alternatives Considered

1. **Single Financial Transactions Table** — Rejected: Would lose the ability to trace specific business relationships (e.g., which ChefOrderGroup generated which payout).

2. **Provider-Specific Financial Tables** — Rejected: Would violate provider-neutrality principle and create vendor lock-in.

3. **Application-Only Financial Tracking** — Rejected: Would not provide the audit trail and reconciliation capabilities required for a financial platform.

### References

- Master Spec §33–43 (Payments, Revenue/Fees, Food Order Revenue Allocation, Kitchen Booking Financial Model, Payouts, Payout Timing, Refunds, Cancellation, Taxes, Financial Immutability, Ledger/Financial Model, Order Financial Snapshot)
- ADR-012 (Payment / Marketplace Settlement)
- ADR-013 (ChefOrderGroup Aggregate + Financial Boundary)
- ADR-014 (Promotion Engine)