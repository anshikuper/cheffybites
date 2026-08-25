# Cheffy Bites — AI Development Instructions

## 1. Purpose

This file is the repository-level instruction contract for any AI coding assistant working on the Cheffy Bites project.

The AI must use the documents under `/docs` as the architectural and business source of truth and must preserve the approved technology stack, domain boundaries, business rules, and coding conventions unless an explicit architecture change is proposed and approved.

This repository is developed primarily using IntelliJ IDEA for the Java/Spring Boot backend and may use IntelliJ IDEA for the monorepo/frontend work as appropriate.

---

## 2. Source-of-Truth Documents

Read these documents before implementing a new domain or changing an existing architectural boundary:

```text
/docs/01-master-spec.md
/docs/02-detailed-architecture.md
/docs/03-database-erd.md
/docs/04-api-contracts.md
/docs/05-event-contracts.md
/docs/adr/
```

Document precedence:

```text
Confirmed business requirements
        ↓
Approved ADRs
        ↓
Detailed architecture
        ↓
Database / API / Event contracts
        ↓
Implementation details
```

If two documents conflict, do not silently choose one. Identify the conflict and propose the required ADR or documentation correction before implementing the conflicting behavior.

---

## 3. Approved Baseline Technology Stack

Unless an approved ADR changes it, use:

### Backend

- Java 21 LTS
- Spring Boot 4.x
- Spring Security
- Spring Data JPA / Hibernate
- Flyway
- Gradle Kotlin DSL
- Bean Validation
- REST APIs
- OpenAPI
- JUnit 5
- Testcontainers

### Web

- TypeScript
- React
- Next.js App Router
- Tailwind CSS
- TanStack Query
- Zod where useful
- OpenAPI-generated API client
- pnpm

### Mobile

- React Native
- Expo where appropriate
- TypeScript
- React Navigation
- TanStack Query

### Data

- PostgreSQL as the system of record
- PostGIS for geospatial capabilities
- JSONB only where flexibility is justified
- Redis for cache/coordination, never authoritative transactional state
- Amazon S3 for object storage
- CloudFront for media delivery

### Cloud / Infrastructure

- AWS
- Docker
- ECS Fargate
- SQS / SNS / EventBridge where appropriate
- Terraform
- GitHub Actions
- OpenTelemetry

### External Providers

- Auth0 / OIDC for identity
- Stripe Connect for marketplace payments/payouts
- Stripe Tax is an evaluated option subject to legal/accounting approval
- Delivery provider adapters, with the first provider selected by ADR

Do not introduce MongoDB, DynamoDB, Kafka, Kubernetes, another ORM, another frontend framework, or another cloud provider merely because an alternative is popular.

---

## 4. Architecture Style

The baseline architecture is:

```text
Modular Monolith
+
Transactional Outbox
+
Selective Event-Driven Integration
```

The initial backend is one deployable Spring Boot application with clear bounded contexts/modules.

Do not create a separate microservice for every domain.

Potential future extraction is allowed when justified by scale, operational isolation, team ownership, or integration requirements.

---

## 5. Repository Structure

Expected structure:

```text
cheffy-bites/
│
├── AGENTS.md
├── docs/
│   ├── 01-master-spec.md
│   ├── 02-detailed-architecture.md
│   ├── 03-database-erd.md
│   ├── 04-api-contracts.md
│   ├── 05-event-contracts.md
│   └── adr/
│       ├── ADR-001-modular-monolith.md
│       ├── ADR-002-transactional-outbox.md
│       ├── ADR-003-nextjs.md
│       └── ...
│
├── apps/
│   ├── business-web/
│   ├── chef-web/
│   ├── customer-web/
│   ├── business-mobile/
│   ├── chef-mobile/
│   └── customer-mobile/
│
├── packages/
│   ├── api-client/
│   ├── domain-types/
│   ├── validation/
│   ├── design-tokens/
│   ├── ui-web/
│   ├── ui-mobile/
│   └── ...
│
├── backend/
│   ├── build.gradle.kts
│   └── src/
│
└── infrastructure/
    └── terraform/
```

Do not reorganize the repository unless the architecture documentation is updated first.

---

## 6. Backend Module Boundaries

The backend should remain organized by business domain.

Expected modules include:

```text
identity
organization
entrepreneur
kitchen
equipment
booking
chef
catalog
food
customer
cart
order
pricing
promotion
payment
refund
tax
payout
delivery
notification
chat
review
foodrequest
administration
common
```

Each domain module should follow this structure where practical:

```text
module/
├── api/
├── application/
├── domain/
└── infrastructure/
```

Rules:

- Domain code must not depend on controllers.
- Domain code must not directly depend on Stripe, AWS SDKs, DoorDash, Uber, Redis, or other external providers.
- External integrations must be behind interfaces/ports and infrastructure adapters.
- Cross-domain calls should go through application services/use cases or domain events, not direct access to another module's persistence internals.
- Do not expose JPA entities directly from REST APIs.

---

## 7. Critical Business Invariants

These are non-negotiable unless an approved business/architecture decision changes them.

### 7.1 One Kitchen Per Customer Order

An Order belongs to exactly one physical Kitchen.

An Order may contain multiple Chefs only when those Chefs operate from the same Kitchen.

```text
Order
 └── Kitchen A
      ├── ChefOrderGroup A
      ├── ChefOrderGroup B
      └── ChefOrderGroup C
```

Food from Kitchen B cannot be added to that Order.

The backend must enforce this invariant.

### 7.2 ChefOrderGroup Is First-Class

`ChefOrderGroup` is a first-class operational and financial entity.

It represents one Chef's portion of a Kitchen Order and is the authoritative boundary for:

- Chef-specific order history
- Chef fulfillment state
- Chef promotions
- Chef-level revenue calculation
- Chef refunds/adjustments
- Chef payout allocation
- Chef analytics/reporting

Expected relationship:

```text
Order 1 ─── N ChefOrderGroup 1 ─── N OrderItem
```

Every food OrderItem must belong to exactly one ChefOrderGroup.

Every ChefOrderGroup belongs to exactly one Order and one Chef/Chef Business.

### 7.3 One Delivery Per Kitchen Order

A multi-Chef Order from one Kitchen uses one delivery workflow and one delivery fee, subject to the delivery policy.

Delivery status is associated with the Kitchen Order, while ChefOrderGroups track independent preparation status.

### 7.4 Chef Promotions Are Chef-Scoped

A Chef promotion may apply only to eligible items within that Chef's ChefOrderGroup.

Never use quantities or totals belonging to another Chef to satisfy a Chef promotion.

Example:

```text
Chef A: 2 items
Chef B: 2 items

Chef A promotion: 20% off 2+ items

Chef A qualifies.
Chef B does not qualify because of Chef A's promotion.
```

### 7.5 Promotion Stacking

Current rules:

- Chef promotions cannot stack with another Chef promotion.
- Platform promotions may stack with a Chef promotion.
- A customer may use only one promo code per transaction.
- Promo codes are single-use.
- Expired promotions are invalid at checkout.
- Partial refunds may require promotion recalculation.

Do not invent additional stacking behavior. Follow the promotion ADR/configuration.

### 7.6 Financial Immutability

Financial history is append-only from a business perspective.

Do not overwrite finalized financial facts.

Corrections use new transactions/events such as:

```text
Original Charge
    +
Refund
    +
Adjustment
```

### 7.7 Booking Concurrency

Kitchen spaces and equipment are time-based resources.

Availability must account for:

- Requested time
- Existing bookings
- Temporary holds
- Cleaning duration
- Operating hours
- Blackout periods
- Equipment quantity

Never implement booking availability as a simple boolean lookup.

### 7.8 Backend Is Authoritative

The backend is authoritative for:

- Pricing
- Promotions
- Tax
- Payment confirmation
- Refund calculations
- Payout calculations
- Booking availability
- Order state transitions
- Authorization

Frontend validation is for UX only.

---

## 8. Database Rules

### 8.1 PostgreSQL Is the System of Record

Use PostgreSQL for transactional business data.

Use PostGIS for geospatial requirements.

Do not add a NoSQL database unless an approved ADR identifies a demonstrated workload that benefits from it.

### 8.2 JSONB Policy

JSONB is allowed only when domain flexibility is actually required.

Good candidates include:

- Provider-specific metadata
- Flexible promotion conditions
- Calculation snapshots
- Food-specific optional metadata
- Flexible ingredient/nutrition structures where justified
- External webhook payload metadata
- User-selected product options

Do not use JSONB for core relational ownership or financial relationships.

Do not replace:

```text
Order
ChefOrderGroup
OrderItem
Payment
Refund
Payout
Booking
Kitchen
Chef
```

with a single JSON document.

### 8.3 Money

Do not use floating point for money.

Prefer integer minor units plus currency.

Example:

```text
amount_minor = 1050
currency = CAD
```

### 8.4 IDs

Use a consistent UUID strategy, preferably UUIDv7 or another approved time-sortable identifier approach.

### 8.5 Time

- Store timestamps in UTC.
- Store business/location timezone identifiers.
- Never infer the business timezone solely from a client/browser locale.

### 8.6 Migrations

All schema changes require Flyway migrations.

Rules:

- Never edit an applied migration silently.
- Add a new migration for a schema change.
- Prefer backward-compatible migrations for rolling deployments.
- Add indexes and constraints intentionally.
- Destructive migrations require explicit planning.

---

## 9. API Rules

Public/application HTTP APIs use REST and OpenAPI.

Base path:

```text
/api/v1/...
```

Examples:

```text
GET  /api/v1/kitchens
POST /api/v1/kitchens
GET  /api/v1/kitchens/{kitchenId}
POST /api/v1/kitchens/{kitchenId}/spaces

GET  /api/v1/foods
POST /api/v1/foods

POST /api/v1/carts
POST /api/v1/carts/{cartId}/items

POST /api/v1/orders
GET  /api/v1/orders/{orderId}

POST /api/v1/chef-order-groups/{chefOrderGroupId}/accept
POST /api/v1/chef-order-groups/{chefOrderGroupId}/reject
POST /api/v1/chef-order-groups/{chefOrderGroupId}/preparing
POST /api/v1/chef-order-groups/{chefOrderGroupId}/ready
```

Rules:

- Use DTOs, not persistence entities.
- Validate all input.
- Return consistent error structures.
- Implement pagination for list endpoints.
- Use idempotency keys for mutations with financial, reservation, or externally observable side effects.
- Keep OpenAPI as the authoritative HTTP contract.

---

## 10. Event Rules

Important asynchronous workflows should use domain/integration events and the transactional outbox.

Examples:

```text
OrderCreated
PaymentSucceeded
PaymentFailed
OrderAccepted
OrderRejected
ChefOrderGroupPreparing
ChefOrderGroupReady
DeliveryRequested
DriverAssigned
OrderPickedUp
OrderOutForDelivery
OrderDelivered
OrderCancelled
RefundProcessed
PayoutCreated
PayoutPaid
PromotionApplied
PromotionInvalidated
KitchenBookingConfirmed
KitchenBookingCancelled
FoodRequestCreated
FoodRequestFulfilled
FoodPublished
```

Rules:

- Persist domain changes and the outbox record in the same database transaction.
- Publish the event asynchronously.
- Consumers must be idempotent.
- External webhook handlers must be idempotent.
- Do not use asynchronous messaging to replace simple in-process calls inside the modular monolith without a reason.

---

## 11. Payment Rules

Stripe Connect is the baseline marketplace payment provider.

Never trust a frontend "payment successful" page as proof of payment.

Payment status must be verified by the backend/provider webhook flow.

Webhook handling must be:

- Signature verified
- Idempotent
- Persisted/auditable
- Retry-safe

Never log:

- Secret keys
- Raw card data
- Payment credentials

---

## 12. Payout Rules

For food Orders:

```text
Order
 ├── ChefOrderGroup A → payout allocation for Chef A
 ├── ChefOrderGroup B → payout allocation for Chef B
 └── ChefOrderGroup C → payout allocation for Chef C
```

`ChefOrderGroup` is the originating operational boundary for Chef payout calculation.

Payout line items should preserve references to:

- Order
- ChefOrderGroup
- Chef Business
- Currency
- Gross amount
- Fees
- Adjustments
- Net payable amount
- Calculation snapshot

Do not calculate historical payouts from current product prices or current promotion settings.

---

## 13. Order State Rules

Order states must be explicit and validated.

Typical transitions:

```text
PAYMENT_PENDING
  → PAID
  → PENDING_CHEF_ACCEPTANCE
  → ACCEPTED
  → PREPARING
  → READY_FOR_FULFILLMENT
  → DELIVERY / PICKUP
  → COMPLETED
```

Rejected/cancelled/refunded paths must be explicitly modeled.

ChefOrderGroup has its own state machine:

```text
PENDING_ACCEPTANCE
  → ACCEPTED
  → PREPARING
  → READY
```

External delivery state may update overall Order/Delivery state but must pass domain validation.

---

## 14. Authorization Rules

Authorization is enforced on the backend.

Use:

```text
User
 ↓
Organization / Business
 ↓
Role
 ↓
Permission
 ↓
Resource ownership
```

Examples:

- Entrepreneur owner can manage their organization's kitchens.
- Kitchen manager can manage assigned kitchens.
- Chef can manage their Chef Business and its listings.
- Customer can only access their carts/orders/requests.
- Admin functions require elevated permissions.

Never rely solely on frontend route protection.

---

## 15. Security Requirements

At minimum:

- HTTPS everywhere
- OIDC/OAuth2
- MFA support
- Secure token handling
- Secrets Manager or equivalent
- Encryption in transit and at rest
- Input validation
- Output encoding
- Rate limiting
- Restricted CORS
- SSRF protection for external URL processing
- Secure file upload validation
- Malware scanning strategy for sensitive uploads
- Audit logging for privileged/financial actions
- OWASP-aligned secure coding

Never store secrets in source code.

---

## 16. Testing Requirements

Every new business capability must include appropriate tests.

Mandatory high-value tests include:

### Order

- Multiple Chefs from the same Kitchen can share one Order.
- Items from different Kitchens cannot share one Order.
- Each OrderItem belongs to exactly one ChefOrderGroup.

### Promotions

- Chef promotion is evaluated only against that Chef's group.
- Multiple Chef promotions do not stack.
- Platform + Chef stacking follows configured rules.
- Single-use promo codes cannot be reused.
- Expired promotions fail at checkout.
- Partial refunds recalculate promotions correctly.

### Booking

- Overlapping Space bookings cannot both be confirmed.
- Cleaning time blocks the required occupancy period.
- Equipment capacity cannot be exceeded.

### Payments

- Duplicate payment attempts do not create duplicate charges.
- Replayed webhooks are safe.
- Refund processing is idempotent.

### Authorization

- Cross-organization access is rejected.
- Customers cannot access other customers' orders.
- Chefs cannot mutate other Chefs' ChefOrderGroups.

---

## 17. AI Coding Workflow

The AI must work incrementally.

Do not generate the entire platform in one step.

For each feature:

```text
Read docs
  ↓
Inspect repository
  ↓
Confirm impacted domain(s)
  ↓
Define/verify domain model
  ↓
Create Flyway migration
  ↓
Implement repository/data access
  ↓
Implement domain rules
  ↓
Implement application use case
  ↓
Implement authorization
  ↓
Implement REST/OpenAPI
  ↓
Implement frontend/mobile integration
  ↓
Add tests
  ↓
Run build/tests
  ↓
Update documentation/ADR if needed
```

Do not skip steps for business-critical features.

---

## 18. Before Changing Existing Code

Before modifying code, the AI must inspect:

1. Existing repository structure.
2. Existing module boundaries.
3. Existing Flyway migrations.
4. Existing OpenAPI contracts.
5. Existing tests.
6. Existing ADRs.
7. Related implementation in adjacent modules.

Do not recreate files unnecessarily.

Do not overwrite existing business logic without understanding its role.

---

## 19. IntelliJ IDEA Development Expectations

IntelliJ IDEA is the primary development environment for this project.

When changing the backend:

- Keep the project buildable/importable by IntelliJ IDEA.
- Use the Gradle wrapper and repository-defined Gradle configuration.
- Preserve IntelliJ-compatible project/module structure.
- Prefer standard Spring/Java conventions that IntelliJ understands well.
- Keep generated sources and build outputs out of source control unless explicitly required.
- Do not modify IntelliJ project metadata unnecessarily.

Use the IDE for:

- Running Spring Boot configurations
- Running JUnit tests
- Debugging
- Inspecting Spring wiring
- Reviewing database mappings
- Running Gradle tasks

The AI must still ensure commands work from the command line/CI, not only inside IntelliJ.

---

## 20. IntelliJ + Frontend

Frontend applications may be developed in IntelliJ IDEA using the repository's pnpm workspace.

Do not duplicate dependencies independently across the six applications when a shared workspace package is appropriate.

Use the existing monorepo package strategy.

---

## 21. Git Rules

Use small, focused commits when practical.

Recommended commit style:

```text
feat(kitchen): add kitchen space creation
fix(order): reject cross-kitchen cart items
test(promotion): cover chef scoped quantity rule
docs(adr): define postgres jsonb policy
```

Do not commit:

- Secrets
- Local credentials
- IDE caches
- Build artifacts
- `node_modules`
- `.gradle`
- Application logs
- Local databases
- Provider credentials

---

## 22. Definition of Done

A feature is not complete until:

- Business requirements are implemented.
- Backend authorization is implemented.
- Database migration is present.
- API contract is updated.
- Tests pass.
- Error states are handled.
- Loading/empty states are handled where UI is involved.
- Observability is addressed.
- Audit requirements are addressed where applicable.
- No critical security issue is known.
- CI passes.
- Documentation is updated where behavior or architecture changed.

---

## 23. Architecture Change Protocol

If implementation reveals a genuine architectural issue, do not silently redesign the system.

Use this exact format:

```text
PROPOSED ARCHITECTURE CHANGE

Reason:

Current design:

Proposed design:

Alternatives considered:

Business impact:

Technical impact:

Migration impact:

Required ADR:
```

The AI must wait for architectural approval before making a material change to the approved architecture unless the user explicitly authorizes the change in the current task.

---

## 24. AI Response Format for Implementation Tasks

For non-trivial implementation requests, structure the response as:

### 1. Understanding
Briefly state the feature and impacted domains.

### 2. Architecture impact
State whether the existing architecture is sufficient.

### 3. Files to change
List files before changing them.

### 4. Database changes
List Flyway migrations and schema impact.

### 5. Backend changes
Describe domain/application/API changes.

### 6. Frontend/mobile changes
Describe impacted applications/packages.

### 7. Tests
List unit/integration/E2E coverage.

### 8. Verification
List commands/tests that should be run.

### 9. Architecture change notice
Only include this section when proposing an architectural change.

---

## 25. Prohibited AI Behaviors

Do not:

- Invent business rules.
- Silently change architecture.
- Replace PostgreSQL with NoSQL.
- Create microservices just to separate classes.
- Put core business data into JSONB merely to avoid schema design.
- Put authoritative business rules in the frontend.
- Trust frontend payment success.
- Store raw payment credentials.
- Bypass Flyway.
- Modify applied migrations silently.
- Disable tests to make the build pass.
- Remove validation to simplify implementation.
- Ignore authorization because a screen is already protected.
- Create duplicate APIs when an existing contract can be extended safely.
- Break the one-Kitchen-per-Order invariant.
- Merge ChefOrderGroups across Chefs.
- Apply Chef A promotions using Chef B's items.
- Calculate Chef payouts from the entire Order without ChefOrderGroup allocation.

---

## 26. First Implementation Principle

When starting development, do not start with all screens.

Start with a vertical slice that proves the architecture.

Recommended first vertical slice:

```text
Identity
  ↓
Organization
  ↓
Entrepreneur
  ↓
Kitchen
  ↓
Kitchen Space
  ↓
Chef
  ↓
Chef books Space
```

Then build the food marketplace and the single-Kitchen / multi-Chef ordering flow.

The most important transaction path to prove early is:

```text
Chef
  ↓
Kitchen Booking
  ↓
Customer
  ↓
Cart for One Kitchen
  ↓
Multiple ChefOrderGroups
  ↓
Chef-scoped Promotions
  ↓
One Payment
  ↓
One Delivery
  ↓
Chef-level Payout Allocation
```

This path validates the most important architectural assumptions in Cheffy Bites.
