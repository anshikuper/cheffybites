# ADR-012 — Payment / Marketplace Settlement

## Status

Proposed

## Context

The Cheffy Bites platform is a multi-sided marketplace where:
- Customers pay for food orders
- Chefs receive payouts for their food contributions
- Entrepreneurs receive payouts for kitchen bookings and equipment rentals
- The platform collects fees for services

The payment architecture must support:
- One customer payment for a multi-Chef Kitchen Order
- Internal allocation of payment to multiple sellers (Chefs + Entrepreneurs)
- Provider-neutral architecture with Stripe Connect as a candidate
- Clear separation of Merchant-of-Record decision (legal/accounting pending)
- Immutable financial records for audit and reconciliation

## Decision

We will implement a centralized marketplace checkout with automated settlement:

### Core Principles

1. **One Customer Payment → Internal Allocation → Multiple Payouts**
   - Customer pays once per Kitchen Order
   - Payment is allocated to ChefOrderGroups and Entrepreneur bookings
   - Platform fees and delivery fees are settled separately

2. **Provider-Neutral Architecture**
   - Stripe Connect is the likely initial provider
   - Architecture remains provider-neutral until legal/accounting sign-off
   - External provider adapters hide implementation details

3. **Financial Immutability**
   - All financial facts are append-only
   - Historical records cannot be overwritten
   - Corrections use new transactions/adjustments

4. **Clear Merchant-of-Record Separation**
   - Technical payment processing is provider-managed
   - Legal Merchant-of-Record decision remains unresolved
   - Tax remittance, chargeback, and refund liability decisions pending

### Architecture Components

#### Payment Flow
```text
Customer
    ↓
Checkout (Pricing + Promotion + Tax + Fees)
    ↓
Payment Provider (Stripe Connect)
    ↓
Ledger (Internal Financial Ledger)
    ↓
Chef Payouts
    ↓
Entrepreneur Payouts
    ↓
Platform Fees
    ↓
Delivery Settlement
```

#### Financial Ledger Model
```text
Payment
    ↓
PaymentAttempt
    ↓
PaymentTransaction
    ↓
PaymentAllocation (to ChefOrderGroup, Entrepreneur, Platform)
    ↓
Payout
    ↓
PayoutLineItem (per ChefOrderGroup)
    ↓
LedgerEntry
```

#### Key Tables

```sql
-- Payments table (system of record for customer charges)
CREATE TABLE payment.payments (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES order.orders(id),
    provider VARCHAR(50) NOT NULL, -- e.g., 'STRIPE_CONNECT'
    provider_payment_intent_id VARCHAR(255) UNIQUE NOT NULL,
    idempotency_key VARCHAR(255) UNIQUE NOT NULL,
    status VARCHAR(50) NOT NULL,
    amount_minor BIGINT NOT NULL,
    currency_code CHAR(3) NOT NULL,
    provider_metadata JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Payment allocations (who gets paid what)
CREATE TABLE payment.payment_allocations (
    id UUID PRIMARY KEY,
    payment_id UUID NOT NULL REFERENCES payment.payments(id),
    order_id UUID NOT NULL REFERENCES order.orders(id),
    chef_order_group_id UUID NULL REFERENCES order.chef_order_groups(id),
    recipient_type VARCHAR(50) NOT NULL, -- 'CHEF', 'ENTREPRENEUR', 'PLATFORM'
    recipient_id UUID NOT NULL,
    allocation_type VARCHAR(50) NOT NULL, -- 'FOOD_ORDER', 'KITCHEN_BOOKING', 'PLATFORM_FEE'
    amount_minor BIGINT NOT NULL,
    currency_code CHAR(3) NOT NULL,
    allocation_evidence JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Payouts (provider-managed payouts to recipients)
CREATE TABLE payment.payouts (
    id UUID PRIMARY KEY,
    recipient_type VARCHAR(50) NOT NULL, -- 'CHEF', 'ENTREPRENEUR'
    recipient_id UUID NOT NULL,
    idempotency_key VARCHAR(255) UNIQUE NOT NULL,
    status VARCHAR(50) NOT NULL,
    amount_minor BIGINT NOT NULL,
    currency_code CHAR(3) NOT NULL,
    provider_reference VARCHAR(255) NULL,
    provider_metadata JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Payout lines (traceable to specific business events)
CREATE TABLE payment.payout_lines (
    id UUID PRIMARY KEY,
    payout_id UUID NOT NULL REFERENCES payment.payouts(id),
    order_id UUID NOT NULL REFERENCES order.orders(id),
    chef_order_group_id UUID NULL REFERENCES order.chef_order_groups(id),
    kitchen_booking_id UUID NULL REFERENCES kitchen.kitchen_bookings(id),
    line_type VARCHAR(50) NOT NULL, -- 'GROSS', 'FEE', 'ADJUSTMENT', 'NET'
    gross_minor BIGINT NOT NULL,
    fee_minor BIGINT NOT NULL,
    adjustment_minor BIGINT NOT NULL,
    net_minor BIGINT NOT NULL,
    currency_code CHAR(3) NOT NULL,
    calculation_snapshot JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

### Operational Rules

#### Payment Allocation
1. **One Payment, Multiple Allocations**
   - A single customer payment can have multiple allocations
   - Each allocation references the originating Order and ChefOrderGroup
   - Platform fees and delivery fees are separate allocations

2. **ChefOrderGroup Traceability**
   - Every payout line must reference the originating ChefOrderGroup
   - This allows answering "Which orders generated a Chef payout?" without reconstructing from Order Items

3. **Entrepreneur Payouts**
   - Kitchen bookings generate separate payouts to Entrepreneurs
   - Equipment rentals are part of booking payouts

#### Payout Rules
1. **Payout Schedule**
   - Configurable payout timing (initially TBD)
   - Payout eligibility determined by business rules
   - Provider may execute payouts, but Cheffy Bites owns eligibility and reconciliation

2. **Payout State Machine**
```text
PENDING
    ↓
ELIGIBLE
    ↓
PROCESSING
    ├── SUCCESS
    └── FAILED
```

3. **Hold State**
   - Possible for disputes, verification, fraud review, or account problems
   - `ON_HOLD` state in payout state machine

#### Refund Rules
1. **Immutable Historical Facts**
   - Refunds create new financial records
   - Original charge remains unchanged
   - Refund allocations reference the original payment allocations

2. **Refund Recalculation**
   - Partial refunds trigger promotion recalculation
   - Affected ChefOrderGroup allocations are recalculated
   - Other ChefOrderGroups remain independently traceable

### Provider Integration

#### Stripe Connect Integration
Stripe Connect is the likely initial provider for marketplace payments:

```text
Customer Payment
    ↓
Stripe Connect (collects from customer)
    ↓
Stripe Connect (allocates to connected accounts)
    ↓
Connected Account Payouts (to Chefs/Entrepreneurs)
    ↓
Platform Fee (Stripe Connect retains)
```

**Provider Adapter Interface:**
```java
public interface PaymentGateway {
    PaymentIntent createPayment(PaymentRequest request);
    PaymentConfirmation confirmPayment(String paymentIntentId);
    RefundResult processRefund(RefundRequest request);
    PayoutResult retrievePayout(String payoutId);
    void handleWebhook(WebhookEvent event);
}
```

### Event Integration

#### Payment Events
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

#### Event Payload Example
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
        "currencyCode": "CAD"
      },
      {
        "entrepreneurId": "550e8400-e29b-41d4-a716-446655440005",
        "amountMinor": 2000,
        "currencyCode": "CAD"
      }
    ]
  }
}
```

### Consequences

#### Positive
- **Clear Financial Boundaries**: ChefOrderGroup is the authoritative query boundary for Chef payouts
- **Auditability**: Every financial transaction is traceable to its originating business event
- **Provider Flexibility**: Architecture supports multiple payment providers
- **Legal Separation**: Technical payment processing is separate from legal Merchant-of-Record decisions

#### Negative
- **Complexity**: Multiple financial tables and relationships increase implementation complexity
- **Provider Dependency**: Initial reliance on Stripe Connect may create vendor lock-in
- **Legal Overhead**: Merchant-of-Record decision requires legal/accounting validation

### Implementation Notes

1. **Database Schema**
   - Add `payment` schema to PostgreSQL
   - Include all tables defined above
   - Add foreign key constraints for referential integrity

2. **Service Implementation**
   - Create `PaymentService` for payment orchestration
   - Implement `PaymentGateway` adapter for Stripe Connect
   - Add `PayoutService` for payout management

3. **Event Integration**
   - Add payment-related events to event contracts
   - Implement outbox pattern for reliable event publishing
   - Add webhook handlers for provider events

4. **Testing**
   - Unit tests for payment allocation logic
   - Integration tests for provider integration
   - E2E tests for payment-to-payout flow

### Alternatives Considered

1. **Direct Provider Integration**
   - Rejected: Would require deep Stripe Connect knowledge
   - Our adapter pattern provides flexibility

2. **Single-Payout-Per-Order**
   - Rejected: Would lose ChefOrderGroup traceability
   - Multiple allocations preserve business boundaries

3. **Real-Time Payouts**
   - Rejected: Would require complex provider coordination
   - Configurable settlement periods are more practical

## References

- Stripe Connect documentation for marketplace payments
- Payment provider webhook security best practices
- Financial regulations for marketplace platforms
- PCI DSS compliance requirements