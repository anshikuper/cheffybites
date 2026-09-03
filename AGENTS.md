# Cheffy Bites — AI Development Instructions

## 1. Purpose

This file is the repository-level instruction contract for any AI coding assistant working on the Cheffy Bites project, including ZOO Code, GitHub Copilot, Claude Code, Codex, Cursor, or other compatible development agents.

The AI must use the documents under `/docs` as the architectural and business source of truth and must preserve the approved technology stack, domain boundaries, business rules, security requirements, financial rules, and coding conventions unless an explicit architecture or business change is proposed and approved.

The AI must work incrementally and must not redesign the system while implementing individual features.

---

# 2. Source-of-Truth Documents

Before implementing a new domain, changing an existing domain, changing a database relationship, modifying a financial workflow, or changing an architectural boundary, the AI must review the relevant documents under:

```text
/docs/01-master-spec.md
/docs/02-detailed-architecture.md
/docs/03-database-erd.md
/docs/04-api-contracts.md
/docs/05-event-contracts.md
/docs/adr/
```

For major cross-domain changes, review all relevant documents rather than only one document.

## Scope-specific document ownership

Each canonical document is authoritative within its declared scope; do not apply a global linear document-precedence hierarchy:

- `docs/adr/` owns architecture decisions and each ADR's status. Accepted ADRs govern the decisions they record; Proposed ADRs are not silently treated as Accepted.
- `docs/01-master-spec.md` owns product requirements, business rules, domain behavior, invariants, capabilities, and business policy.
- `docs/02-detailed-architecture.md` owns integrated architecture explanation, domain/component interaction, cross-domain coordination, implementation direction, and architectural overview. It does not override specialized persistence, API, or event representations.
- `docs/03-database-erd.md` owns exact canonical persistence representation.
- `docs/04-api-contracts.md` owns exact canonical API representation.
- `docs/05-event-contracts.md` owns exact canonical event representation.
- `AGENTS.md` is implementation-agent guidance that summarizes and points to canonical sources; it does not override them.
- `plans/architecture-review.md` is superseded historical material and has no canonical authority.

If two sources conflict:

1. Do not silently choose one based on an assumed hierarchy.
2. Identify the conflict.
3. Explain the impact.
4. Stop implementation of the conflicting area.
5. Propose explicit reconciliation of the owning canonical documents or ADR.
6. Wait for approval before implementing a material conflicting change.

Where an Accepted ADR records an architectural decision, persistence, API, event, and integrated-architecture documents must conform to that decision within their respective scopes. A Proposed ADR does not automatically override an Accepted ADR or the current approved baseline.

The AI must never invent a business rule merely because an implementation detail is missing.

If a requirement is ambiguous but does not materially affect architecture, clearly state the assumption before implementation.

---

# 3. Approved Baseline Technology Stack

Unless an approved ADR changes it, use the following stack.

## Backend

- Java 21 LTS
- Spring Boot 4.x
- Spring Security
- Spring Data JPA / Hibernate
- Flyway
- Gradle Kotlin DSL
- Bean Validation
- REST APIs
- OpenAPI
- JUnit 6
- Testcontainers

## Web

- TypeScript
- React
- Next.js App Router
- Tailwind CSS
- TanStack Query
- Zod where useful
- OpenAPI-generated API client
- pnpm

## Mobile

- React Native
- Expo where appropriate
- TypeScript
- React Navigation
- TanStack Query

## Data

- PostgreSQL as the system of record
- PostGIS for geospatial capabilities
- JSONB only where flexibility is justified
- Redis for cache/coordination, never authoritative transactional state
- Amazon S3 for object storage
- CloudFront for media delivery

## Cloud / Infrastructure

- AWS
- Docker
- ECS Fargate
- SQS / SNS / EventBridge where appropriate
- Terraform
- GitHub Actions
- OpenTelemetry

## External Providers

- Auth0 / OIDC for identity
- Stripe Connect for marketplace payments and payouts
- Stripe Tax is an evaluated option subject to legal/accounting approval
- Delivery provider adapters, with the first provider selected by ADR

Do not introduce MongoDB, DynamoDB, Kafka, Kubernetes, another ORM, another frontend framework, or another cloud provider merely because an alternative is popular.

Any new infrastructure dependency requires justification and, when architectural, an ADR.

---

# 4. Architecture Style

The baseline architecture is:

```text
Modular Monolith
+
Transactional Outbox
+
Selective Event-Driven Integration
```

The initial backend is one deployable Spring Boot application with clear business-domain modules.

Do not create a separate microservice for every domain.

Potential future extraction is allowed only when justified by factors such as:

- scale
- operational isolation
- independent deployment requirements
- team ownership
- external integration requirements
- security boundaries
- clearly demonstrated performance requirements

Do not convert a modular monolith into microservices merely because the domain contains many modules.

---

# 5. Repository Structure

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

# 6. Application Boundaries

Cheffy Bites has three primary user experiences.

Phase-1 deployment exception: accepted ADR-025 uses one deployed Next.js app
in `apps/customer-web` for the LP-01 public surface and protected
`/app/operator/*` and `/app/chef/*` routes. The three experience boundaries and
backend authorization rules remain distinct; `business-web` and `chef-web`
remain reserved for later independent deployment.

## Entrepreneur

```text
business.cheffybites.com
business mobile application
```

Responsible for:

- organization/business
- kitchens
- kitchen spaces
- equipment
- operating hours
- rates
- cleaning requirements
- kitchen bookings
- chef relationships
- entrepreneur promotions

## Chef

```text
chef.cheffybites.com
chef mobile application
```

Responsible for:

- chef profile
- food listings
- cuisines
- ingredients
- nutrition
- recipes
- media
- availability
- chef promotions
- customer orders
- ChefOrderGroups
- food-request responses
- fulfillment

## Customer

```text
cheffybites.com
customer mobile application
```

Responsible for:

- food discovery
- cart
- checkout
- orders
- payments
- ratings
- chat
- wishlist
- food requests
- delivery tracking

Do not mix authorization rules between these experiences.

---

# 7. Backend Module Boundaries

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
- Cross-domain calls should go through application services/use cases or domain events.
- One module must not directly access another module's persistence internals.
- Do not expose JPA entities directly from REST APIs.
- Avoid circular module dependencies.
- Shared/common code must remain genuinely generic and must not become a dumping ground for business logic.

---

# 8. Domain Ownership

Each business fact must have a clear owning domain.

Examples:

```text
Kitchen
    → kitchen domain

Kitchen Space
    → kitchen domain

Booking
    → booking domain

Food Listing
    → food/catalog domain

Cart
    → cart domain

Order
    → order domain

ChefOrderGroup
    → order domain

Promotion
    → promotion domain

Payment
    → payment domain

Refund
    → refund domain

Payout
    → payout domain

Delivery
    → delivery domain
```

Do not create duplicate authoritative representations of the same business fact across domains.

Read models/projections may exist, but the owning domain remains authoritative.

---

# 9. Critical Business Invariants

These are non-negotiable unless an approved business or architecture decision changes them.

## 9.1 One Kitchen Per Customer Order

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

The frontend may provide early validation for UX, but the backend remains authoritative.

---

# 10. ChefOrderGroup Is First-Class

`ChefOrderGroup` is the first-class Chef operational boundary, Chef promotion evaluation boundary, financial allocation/reference boundary, refund traceability boundary, payout traceability boundary, and reporting boundary for one Chef's portion of a Kitchen Order.

It represents one Chef's portion of a Kitchen Order.

It is the authoritative Chef-level boundary for:

- Chef-specific order history
- Chef preparation state
- Chef promotion evaluation
- Chef financial allocation references
- Chef refund traceability
- Chef payout traceability
- Chef analytics/reporting
- Chef operational status

Expected relationship:

```text
Order
  1
  │
  └── N ChefOrderGroup
            1
            │
            └── N OrderItem
```

Every food OrderItem must belong to exactly one ChefOrderGroup.

Every ChefOrderGroup belongs to:

- exactly one Order
- exactly one Chef
- exactly one Kitchen through the parent Order

ChefOrderGroup must remain independently queryable for:

```text
Chef order history
Chef reporting
Chef preparation
Chef refund traceability
Chef payout traceability
Chef analytics
```

The financial domain owns `Payment`, `PaymentAllocation`, `Refund`, `RefundLine`, `Payout`, `PayoutLine`, and Ledger entries/transactions. Those financial records reference `chef_order_group_id` where applicable; this reference does not make ChefOrderGroup the owner of a financial aggregate.

Never calculate a Chef's historical financial position merely by querying all items in the parent Order without using financial records that preserve the ChefOrderGroup allocation/reference boundary.

---

# 11. One Delivery Per Kitchen Order

A multi-Chef Order from one Kitchen uses one delivery workflow and one delivery fee, subject to the delivery policy.

```text
Order
 └── Kitchen
      ├── ChefOrderGroup A
      ├── ChefOrderGroup B
      └── ChefOrderGroup C
             │
             ▼
        One Delivery
```

Delivery status is associated with the Kitchen Order / Delivery aggregate.

ChefOrderGroups track independent food preparation status.

The system must not create separate delivery fees merely because multiple Chefs are represented in the same Kitchen Order.

---

# 12. Cart Rules

A customer may maintain multiple carts.

Each cart is associated with one Kitchen.

A single checkout/order may contain multiple ChefOrderGroups only when all selected items belong to the same Kitchen.

The backend must validate the Kitchen invariant during:

- cart item addition
- cart update
- checkout
- order creation

Client-side cart validation is not sufficient.

---

# 13. Pricing Authority

The backend is authoritative for:

- item price
- quantity
- promotions
- discounts
- fees
- tax
- delivery charges
- payment amount
- refund amount
- payout amount

The frontend must never be trusted for final pricing.

A client-submitted total is informational only.

At checkout, the backend must recalculate authoritative pricing.

---

# 14. Promotion Rules

Current business rules:

### Chef promotions

Chef promotions are scoped to the Chef.

A Chef promotion may apply to:

- one food item
- multiple food items
- the Chef's menu
- quantity-based conditions
- BOGO
- first-time order rules
- other approved promotion types

A Chef promotion must only evaluate eligible items belonging to that Chef's ChefOrderGroup.

Example:

```text
Chef A
2 qualifying items
→ Chef A promotion qualifies

Chef B
2 items
→ Chef A promotion must NOT consider Chef B's items
```

Never use the entire Order total or quantities from another Chef to satisfy a Chef promotion.

### Platform promotions

Platform promotions may apply at the overall transaction level subject to promotion configuration.

Platform and Chef promotions may coexist when their scopes and compatibility rules permit:

```text
Platform promotion
       +
Chef promotion
       =
Allowed
```

Chef A and Chef B are independent promotion domains because each Chef's promotions are evaluated within that Chef's own ChefOrderGroup. Promotions belonging to different ChefOrderGroups may coexist.

Within one ChefOrderGroup:

- Item-level promotions on different items may coexist.
- Item-level and ChefOrderGroup-level promotions may coexist when their monetary scopes do not overlap and compatibility permits.
- Competing promotions targeting the same monetary scope are resolved through compatibility and exclusivity rules, priority, customer savings, and the deterministic tie-breaker defined by ADR-014.

Do not use one global `stackable` boolean as the canonical promotion compatibility rule.

### Promo codes

Current rules:

- At most one customer-entered promo code may be applied during one Order checkout. This is Order-level entered-code cardinality, not a global one-use limit for the code.
- A specific customer may successfully redeem a specific customer-entered promo code at most once. Consuming redemption-state uniqueness applies to the `(promo_code_id, customer_id)` pair; do not implement ordinary codes using general `UNIQUE(promo_code_id)`.
- A promo code may define optional `max_global_uses`. `NULL` or absent means no configured global redemption cap; `max_global_uses = 1` makes that code globally one-time. Do not introduce `max_uses_per_customer`; the per-customer successful-redemption limit is fixed at one.
- Customer-entered redemption lifecycle is `RESERVED → REDEEMED` or `RESERVED → RELEASED`. `RESERVED` temporarily consumes per-customer and optional global capacity. `REDEEMED` permanently consumes customer eligibility and global capacity. `RELEASED` is terminal historical evidence that no longer consumes capacity and permits a later attempt using a new redemption record.
- Full or partial refunds do not transition `REDEEMED` to `RELEASED`, restore customer eligibility, or overwrite original redemption evidence.
- Automatically applied promotions do not create promo-code redemption records merely because they apply. Their PromotionApplication may have no `promo_code_id`, and compatible automatic promotions may coexist under the promotion-engine rules.
- For an authoritative global-cap reservation, lock the relevant PromoCode row before counting/checking capacity. `RESERVED` and `REDEEMED` count against `max_global_uses`; `RELEASED` does not. Concurrent requests must not exceed the global cap, and consuming-state uniqueness must prevent concurrent duplicate claims for the same customer/code.
- Platform promotions may be restricted to specific users.
- Promotions expire according to their configured validity.
- An expired promotion must not be applied at checkout.
- A promotion becoming invalid after an item is removed/refunded must trigger the configured recalculation behavior.

---

# 15. Promotion Evaluation and Snapshots

Promotion evaluation must be deterministic and auditable.

At checkout, preserve enough information to reconstruct:

- promotion ID
- promotion version/configuration
- qualifying items
- quantities
- discount calculation
- discount amount
- timestamp
- applicable ChefOrderGroup
- applicable Order

Historical orders must not depend on the current live promotion configuration.

Do not recalculate historical financial facts using today's promotion rules.

---

# 16. Partial Refund Rules

When an Order is partially refunded:

1. Identify the refunded OrderItems.
2. Remove their financial contribution from the remaining Order.
3. Recalculate the affected ChefOrderGroup where required.
4. Re-evaluate promotion validity according to the approved promotion rules.
5. Calculate the refund/adjustment.
6. Record the resulting financial adjustment.
7. Never overwrite the original finalized financial transaction.

Example:

```text
Buy 2 Get 1 Free

Original:
Item A
Item B
Item C
```

If the refund causes the order to no longer satisfy the promotion:

```text
Promotion becomes invalid
        ↓
Recalculate remaining price
        ↓
Create financial adjustment/refund
```

The exact calculation must be implemented according to the promotion contract/ADR.

---

# 17. Financial Immutability

Financial history is append-only from a business perspective.

Do not overwrite finalized financial facts.

Corrections must use new transactions/adjustments/events.

Example:

```text
Original Charge
      +
Refund
      +
Adjustment
```

Historical financial records must remain auditable.

---

# 18. Money

Never use floating point for monetary calculations.

Prefer:

```text
amount_minor = 1050
currency = CAD
```

Use integer minor units or the approved monetary abstraction.

Every financial calculation must explicitly preserve currency.

Never silently mix currencies.

---

# 19. Payment Rules

Stripe Connect is the baseline marketplace payment provider.

Never trust a frontend "payment successful" page as proof of payment.

Payment status must be verified by the backend/provider webhook flow.

Webhook handling must be:

- signature verified
- idempotent
- persisted/auditable
- retry-safe

Never log:

- secret keys
- raw card data
- payment credentials
- sensitive payment tokens

Payment provider identifiers should be persisted where required for reconciliation and audit.

---

# 20. Payout Rules

For food Orders:

```text
Order
 ├── ChefOrderGroup A → payout allocation for Chef A
 ├── ChefOrderGroup B → payout allocation for Chef B
 └── ChefOrderGroup C → payout allocation for Chef C
```

`ChefOrderGroup` is the originating operational and financial-reference boundary for Chef payout calculation. The financial domain owns the resulting Payout and PayoutLine records.

Payout line items should preserve references to:

- Order
- ChefOrderGroup
- Chef Business
- currency
- gross amount
- discounts
- fees
- taxes where applicable
- adjustments
- refunds
- net payable amount
- calculation snapshot

Historical payouts must not be recalculated from current menu prices or promotion configuration.

---

# 21. Idempotency

Idempotency is required for operations that can create externally observable or financial side effects.

Examples:

```text
Create Order
Payment creation
Payment capture
Refund
Payout creation
Webhook processing
Booking confirmation
Delivery creation
Notification dispatch where duplication is harmful
```

Use appropriate idempotency keys and/or unique constraints.

Retries must not create:

- duplicate orders
- duplicate payments
- duplicate refunds
- duplicate payouts
- duplicate bookings
- duplicate external side effects

---

# 22. Booking Concurrency

Kitchen spaces and equipment are time-based resources.

Availability must account for:

- requested time
- existing bookings
- temporary holds
- cleaning duration
- operating hours
- blackout periods
- equipment quantity
- cancellation/release rules

Never implement booking availability as a simple boolean lookup.

Booking confirmation must be concurrency-safe.

The database must enforce the appropriate uniqueness/exclusion constraints or transactional locking strategy defined by the architecture.

Two concurrent requests must not be able to confirm the same unavailable resource.

For the bounded Phase-1 Chef-to-Kitchen pilot, accepted ADR-024 applies:

- `KitchenBooking` owns the request; no second BookingRequest aggregate exists.
- `REQUESTED` is non-reserving; `CONFIRMED` reserves under ADR-007.
- The pilot does not enter `HELD` and creates no Payment or checkout state.
- `RentalOffer` is the sole current Space rental-term authority.
- Space availability uses `AVAILABLE|BLOCKED` and `ONE_TIME|WEEKLY`, with a
  blocked rule and `HELD|CONFIRMED` occupancy taking precedence.

Implementation planning must follow the exact ERD, API, and event contracts;
this summary does not replace those canonical representations.

---

# 23. Order State Rules

Order states must be explicit and validated. Every parent Order has one immutable fulfillment type:

```text
PICKUP
DELIVERY
```

Pickup lane:

```text
PAID
→ PENDING_CHEF_ACCEPTANCE
→ ACCEPTED
→ PREPARING
→ READY_FOR_FULFILLMENT
→ PICKED_UP
→ COMPLETED
```

Delivery lane:

```text
PAID
→ PENDING_CHEF_ACCEPTANCE
→ ACCEPTED
→ PREPARING
→ READY_FOR_FULFILLMENT
→ DELIVERY_REQUESTED
→ DRIVER_ASSIGNED
→ DRIVER_PICKED_UP
→ OUT_FOR_DELIVERY
→ DELIVERED
→ COMPLETED
```

`PICKED_UP` means completed handoff to the customer or the customer's authorized pickup party only. `DRIVER_PICKED_UP` means the delivery driver has taken possession of a delivery Order. Pickup-only and delivery-only transitions must not cross lanes.

Rejected, cancelled, failed, refunded, and partially refunded paths must be explicitly modeled.

Do not allow arbitrary state transitions.

---

# 24. ChefOrderGroup State Rules

ChefOrderGroup has its own preparation state machine:

```text
PENDING_ACCEPTANCE → ACCEPTED
PENDING_ACCEPTANCE → REJECTED
ACCEPTED → PREPARING
ACCEPTED → CANCELLED
PREPARING → READY
PREPARING → CANCELLED
```

ChefOrderGroup does not own `PICKED_UP`, `DRIVER_PICKED_UP`, `OUT_FOR_DELIVERY`, `DELIVERED`, or `COMPLETED`. Those are parent Order fulfillment responsibilities.

Refund processing is owned by the financial domain and references `chef_order_group_id` where applicable. Refund states are not ChefOrderGroup preparation states; this separation preserves Chef-level refund traceability without making ChefOrderGroup the Refund aggregate owner.

ChefOrderGroup state must not be inferred solely from the parent Order state.

The parent Order and ChefOrderGroup state machines must remain consistent without incorrectly forcing all ChefOrderGroups to have identical states.

---

# 25. Delivery State Rules

Delivery is a separate operational concern coordinated through the parent Order. The delivery fulfillment lane is:

```text
READY_FOR_FULFILLMENT
→ DELIVERY_REQUESTED
→ DRIVER_ASSIGNED
→ DRIVER_PICKED_UP
→ OUT_FOR_DELIVERY
→ DELIVERED
→ COMPLETED
```

`DRIVER_PICKED_UP` means delivery-driver possession. `PICKED_UP` must not be used for delivery-driver possession.

External delivery providers must be accessed through an adapter interface.

Do not couple the Order domain directly to DoorDash, Uber, or a specific delivery vendor.

Provider-specific statuses must be translated into Cheffy Bites domain states.

---

# 26. Food Requests / Wishlist

Customers may create food requests.

Rules:

- Requests are associated with the customer unless explicitly configured otherwise.
- Requests may be visible to nearby eligible Chefs.
- Customers cannot make anonymous requests under the current business rule.
- Multiple Chefs may respond to the same request.
- Customers may be notified when multiple Chefs add the requested food.
- Similar requests may be automatically grouped.
- Customers may like/vote on another customer's request.
- A customer may add another customer's requested food to their own wishlist.
- The system should maintain demand counts for requested food.
- A request may automatically close when the food becomes available.
- The originating customer may re-enable or delete the wishlist/request according to the product rules.
- Customers may subscribe to notifications for requested food.
- Chef-to-customer chat must follow the approved messaging/authorization rules.

Do not invent additional visibility or messaging behavior.

---

# 27. Database Rules

## 27.1 PostgreSQL Is the System of Record

Use PostgreSQL for transactional business data.

Use PostGIS for geospatial requirements.

Do not add a NoSQL database unless an approved ADR identifies a demonstrated workload that benefits from it.

---

# 28. JSONB Policy

JSONB is allowed only when domain flexibility is genuinely required.

Good candidates include:

- provider-specific metadata
- flexible promotion conditions
- calculation snapshots
- food-specific optional metadata
- flexible ingredient/nutrition structures where justified
- external webhook payload metadata
- user-selected product options

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

Use normalized relational structures for authoritative business relationships.

---

# 29. IDs

Use a consistent UUID strategy, preferably UUIDv7 or another approved time-sortable identifier approach.

Do not introduce multiple incompatible identifier strategies without an ADR.

---

# 30. Time and Time Zones

Timezone modeling follows accepted ADR-011. Its status remains governed by the standalone ADR.

Distinguish real instants from business-local schedules:

- Concrete events that happened or will happen at a specific moment are real instants. Examples include booking occurrence boundaries, order, payment, refund, payout, and delivery timestamps, concrete food-availability occurrences, and event `occurredAt` values.
- Persist real instants in PostgreSQL as `TIMESTAMPTZ` and use an appropriate instant- or offset-aware application type. PostgreSQL `TIMESTAMPTZ` represents a real instant; it does not preserve the caller's original IANA timezone name or textual offset.
- Recurring Kitchen operating hours, Chef availability, Kitchen availability, and similar schedule rules use business-local date/time semantics plus an IANA timezone. Do not store recurring schedules only as UTC instants. Resolve and materialize concrete occurrences as real instants for specific dates.

The Kitchen timezone is authoritative for Kitchen-based booking, operating-hours, Chef-availability, and similar business rules. Store it as an IANA timezone identifier such as `America/Toronto`; values such as `EST`, `EDT`, or `UTC-5` are not sufficient business timezone identities. A customer, device, or browser timezone may control display but must not override the Kitchen timezone for these rules. Persist a customer-address timezone only when an independently approved business requirement needs it.

API fields representing real instants must contain `Z` or an explicit UTC offset, for example `2026-08-27T15:00:00Z` or `2026-08-27T11:00:00-04:00`. Never silently interpret an offset-free value such as `2026-08-27T11:00:00` as UTC for an instant field. Detailed API representation remains canonical in `docs/04-api-contracts.md`.

When resolving business-local input in correctness-sensitive booking, order, financial, or similar workflows:

- Reject nonexistent local times in a daylight-saving gap rather than silently shifting them.
- Reject ambiguous local times in a daylight-saving overlap unless sufficient information identifies the intended offset; do not silently guess an earlier or later offset.

Changing a Kitchen's configured timezone affects future local scheduling semantics but does not rewrite historical or already-materialized real instants. Existing bookings, orders, financial timestamps, and materialized availability occurrences retain their original instants unless an explicit business operation changes them. If auditability requires timezone configuration history or effective dating, model it explicitly rather than rewriting history.

Use Java time types according to their semantics:

- `Instant`: an absolute machine timestamp or real instant.
- `OffsetDateTime`: a timestamp carrying an explicit offset where API or provider semantics require it.
- `ZonedDateTime`: a local date/time interpreted in a named `ZoneId` when resolving business schedules.
- `ZoneId`: the IANA timezone identity.
- `LocalDate`, `LocalTime`, and `LocalDateTime`: business-local values that are not yet resolved to a real instant.

Do not use `LocalDateTime` as the canonical representation of a resolved cross-system instant, and do not require every layer to expose every Java time type.

---

# 31. Database Migrations

All schema changes require Flyway migrations.

Rules:

- Never edit an applied migration silently.
- Add a new migration for a schema change.
- Prefer backward-compatible migrations for rolling deployments.
- Add indexes and constraints intentionally.
- Destructive migrations require explicit planning.
- Do not use Hibernate auto-DDL as the source of production schema truth.

---

# 32. API Rules

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
- Maintain backwards compatibility within an API version where practical.
- Breaking API changes require explicit versioning or an approved migration strategy.

---

# 33. API Client Generation

OpenAPI is the source of truth for generated API clients.

Do not manually duplicate API contracts in:

```text
frontend types
mobile types
backend DTO definitions
shared TypeScript types
```

where generated OpenAPI types/clients can safely be used.

If a shared domain type is required for reasons beyond API transport, clearly distinguish it from the generated API contract.

---

# 34. Event Rules

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
- Publish events asynchronously.
- Consumers must be idempotent.
- External webhook handlers must be idempotent.
- Event processing must tolerate retries.
- Do not use asynchronous messaging to replace simple in-process calls inside the modular monolith without a reason.
- Event schemas must be versionable.
- Do not change an event contract incompatibly without an explicit migration strategy.

---

# 35. Authorization Rules

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
- Chef can manage their Chef Business and listings.
- Customer can only access their own carts, orders, requests, and profile data.
- Admin functions require elevated permissions.

Never rely solely on frontend route protection.

Every resource access must verify ownership/authorization at the backend boundary.

---

# 36. Multi-Tenant / Organization Isolation

Business data must remain isolated by organization/business ownership.

When a resource belongs to an organization:

```text
Authenticated User
       ↓
Organization
       ↓
Resource
```

must be validated.

Never trust an organization ID supplied by the client without authorization verification.

Cross-organization access must fail securely.

Repository queries should include the appropriate ownership/tenant constraints where required.

---

# 37. Security Requirements

At minimum:

- HTTPS everywhere
- OIDC/OAuth2
- MFA support
- Secure token handling
- AWS Secrets Manager or equivalent
- Encryption in transit and at rest
- Input validation
- Output encoding
- Rate limiting
- Restricted CORS
- SSRF protection for external URL processing
- Secure file upload validation
- Malware scanning strategy where required
- Audit logging for privileged and financial actions
- OWASP-aligned secure coding

Never store secrets in source code.

Never commit:

```text
.env
API keys
private keys
OAuth client secrets
Stripe secrets
database passwords
AWS credentials
```

---

# 38. Auditability

Audit important business and administrative actions.

At minimum, consider audit records for:

- authentication/security changes
- organization membership changes
- kitchen ownership/configuration changes
- booking cancellation/override
- promotion creation/change/deactivation
- payment state changes
- refund actions
- payout actions
- administrative actions
- permission changes

Audit records should be append-only and should preserve actor, timestamp, action, target, and relevant context without storing prohibited secrets or sensitive payment data.

---

# 39. Observability

Use OpenTelemetry for distributed tracing/telemetry.

Important workflows must have correlation/trace identifiers.

At minimum, observability should allow investigation of:

```text
Customer
  ↓
Order
  ↓
ChefOrderGroup
  ↓
Payment
  ↓
Delivery
  ↓
Payout
```

Do not log sensitive information.

Logs should be structured where practical.

Financial and security events must be auditable independently of ordinary application logs.

---

# 40. Testing Requirements

Every new business capability must include appropriate tests.

## Order

- Multiple Chefs from the same Kitchen can share one Order.
- Items from different Kitchens cannot share one Order.
- Each OrderItem belongs to exactly one ChefOrderGroup.
- ChefOrderGroups remain independently queryable.
- Delivery remains one Kitchen-level workflow.

## Promotions

- Chef promotion is evaluated only against that Chef's group.
- Multiple Chef promotions do not stack.
- Platform + Chef stacking follows configured rules.
- The same customer cannot successfully redeem the same customer-entered promo code twice.
- A `RELEASED` redemption attempt permits that customer to try the code again with a new redemption record.
- Two different customers may redeem the same code when optional global capacity permits.
- `max_global_uses = 1` permits only one `RESERVED` or `REDEEMED` redemption globally.
- Concurrent customers cannot make consuming redemptions exceed `max_global_uses`, and concurrent duplicate claims for one customer/code are rejected.
- Full or partial refunds do not restore eligibility for a `REDEEMED` code.
- Automatic promotions apply without creating PromoCodeRedemption records merely because they qualify.
- Expired promotions fail at checkout.
- Partial refunds recalculate promotions correctly.
- Promotion snapshots preserve historical calculations.

## Booking

- Overlapping Space bookings cannot both be confirmed.
- Cleaning time blocks the required occupancy period.
- Equipment capacity cannot be exceeded.
- Concurrent booking attempts are handled safely.

## Payments

- Duplicate payment attempts do not create duplicate charges.
- Replayed webhooks are safe.
- Refund processing is idempotent.
- Payment status cannot be fabricated by the client.

## Payouts

- ChefOrderGroup allocation is correct.
- Refunds affect the correct ChefOrderGroup.
- Fees/adjustments are allocated according to the approved rules.
- Historical payout calculations remain reproducible.

## Authorization

- Cross-organization access is rejected.
- Customers cannot access other customers' orders.
- Chefs cannot mutate other Chefs' ChefOrderGroups.
- Entrepreneurs cannot modify another organization's kitchens.

---

# 41. AI Coding Workflow

The AI must work incrementally.

Do not generate the entire platform in one step.

For each non-trivial feature:

```text
Read relevant docs
        ↓
Inspect repository
        ↓
Confirm impacted domains
        ↓
Identify existing implementation
        ↓
Define/verify domain model
        ↓
Create/update database migration
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
Review changes
        ↓
Update documentation/ADR if required
```

Do not skip architectural review for business-critical features.

---

# 42. Plan Before High-Risk Implementation

For the following features, the AI must plan before editing code:

```text
Payment
Refund
Payout
Promotion Engine
ChefOrderGroup
Order lifecycle
Booking concurrency
Authorization
Authentication
Database schema redesign
Event contract changes
External provider integration
```

The plan should identify:

- impacted modules
- database changes
- transaction boundaries
- authorization implications
- API changes
- events
- idempotency
- tests
- risks

Use the project's Plan Mode or equivalent read-only planning workflow when available.

---

# 43. Before Changing Existing Code

Before modifying code, inspect:

1. Existing repository structure.
2. Existing module boundaries.
3. Existing Flyway migrations.
4. Existing OpenAPI contracts.
5. Existing tests.
6. Existing ADRs.
7. Related implementation in adjacent modules.
8. Existing configuration and dependency versions.

Do not recreate files unnecessarily.

Do not overwrite existing business logic without understanding its role.

Prefer minimal, focused changes over unrelated refactoring.

---

# 44. Avoid Unrelated Refactoring

When implementing a feature:

- Do not rename unrelated classes.
- Do not reorganize unrelated packages.
- Do not upgrade dependencies unless required.
- Do not change architectural patterns without justification.
- Do not reformat the entire repository.
- Do not modify unrelated UI components.
- Do not "clean up" unrelated code merely because it is not ideal.

If unrelated technical debt is discovered, report it separately unless it blocks the requested feature.

---

# 45. IntelliJ IDEA and VS Code

Both IntelliJ IDEA and VS Code are supported development environments.

## Backend

IntelliJ IDEA is the preferred environment for:

- Spring Boot development
- Java debugging
- JUnit execution
- Gradle tasks
- Spring configuration inspection
- database mapping inspection

VS Code may also be used for backend development and AI-assisted implementation.

## Frontend / Mobile

VS Code is fully supported for:

- React
- Next.js
- React Native
- Expo
- TypeScript
- pnpm workspaces
- ZOO Code

The project must remain buildable and testable from the command line and CI regardless of IDE.

Do not make IDE-specific configuration the only way to build or test the application.

---

# 46. ZOO Code Usage

ZOO Code is one of the supported AI coding agents for this repository.

When using ZOO Code:

- The current workspace `AGENTS.md` is the primary project instruction source.
- Use `@` references for important architecture files/folders when precision is needed.
- Use Plan Mode or equivalent low-risk execution mode before high-risk changes.
- Keep long-running implementation tasks within a continuous task/session when practical.
- Use commands/skills for repeatable workflows such as code review and testing.
- Do not rely on agent memory as the authoritative source of architecture.

The repository documents remain authoritative.

ZOO Code currently reads the workspace-root `AGENTS.md`; do not assume nested `AGENTS.md` files will automatically be merged.

---

# 47. AI Context Strategy

For architecture-sensitive tasks, explicitly reference the relevant documents.

Examples:

```text
@AGENTS.md
@docs/02-detailed-architecture.md
@docs/03-database-erd.md
@docs/04-api-contracts.md
@docs/adr/
```

Do not unnecessarily provide every document for a small task.

For cross-domain work, provide enough architecture context to prevent the AI from making incorrect assumptions.

---

# 48. Git Rules

Use small, focused commits when practical.

Recommended commit style:

```text
feat(kitchen): add kitchen space creation
fix(order): reject cross-kitchen cart items
test(promotion): cover chef scoped quantity rule
docs(adr): define postgres jsonb policy
```

Do not commit:

- secrets
- local credentials
- IDE caches
- build artifacts
- `node_modules`
- `.gradle`
- application logs
- local databases
- provider credentials

Do not force-push or rewrite shared history unless explicitly requested.

---

# 49. Definition of Done

A feature is not complete until:

- Business requirements are implemented.
- Backend authorization is implemented.
- Database migration is present where required.
- API contract is updated.
- Event contract is updated where required.
- Tests pass.
- Error states are handled.
- Loading/empty states are handled where UI is involved.
- Observability is addressed.
- Audit requirements are addressed where applicable.
- Idempotency is addressed where required.
- No critical security issue is known.
- CI passes.
- Documentation is updated where behavior or architecture changed.

---

# 50. Architecture Change Protocol

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

Database impact:

API impact:

Event impact:

Migration impact:

Operational impact:

Required ADR:
```

The AI must wait for architectural approval before making a material architectural change unless the user explicitly authorizes the change in the current task.

---

# 51. ADR Protection

Approved ADRs are architectural constraints.

The AI must not silently:

- modify an approved ADR
- delete an ADR
- reinterpret an ADR to justify an unrelated implementation

If implementation reveals a problem with an ADR:

1. Stop if the issue materially affects architecture.
2. Explain the conflict.
3. Propose an ADR amendment or new ADR.
4. Wait for approval.
5. Implement the approved change.

---

# 52. AI Response Format for Non-Trivial Implementation Tasks

For non-trivial implementation requests, structure the response as:

## 1. Understanding

Briefly state the feature and impacted domains.

## 2. Architecture impact

State whether the existing architecture is sufficient.

## 3. Files to change

List files before changing them.

## 4. Database changes

List Flyway migrations and schema impact.

## 5. Backend changes

Describe domain/application/API changes.

## 6. Frontend/mobile changes

Describe impacted applications/packages.

## 7. Events

List new or modified events.

## 8. Tests

List unit/integration/E2E coverage.

## 9. Verification

List commands/tests that should be run.

## 10. Architecture change notice

Only include this section when proposing an architectural change.

---

# 53. Prohibited AI Behaviors

Do not:

- Invent business rules.
- Silently change architecture.
- Replace PostgreSQL with NoSQL.
- Create microservices merely to separate classes.
- Put core business data into JSONB merely to avoid schema design.
- Put authoritative business rules in the frontend.
- Trust frontend payment success.
- Store raw payment credentials.
- Bypass Flyway.
- Modify applied migrations silently.
- Disable tests to make the build pass.
- Remove validation to simplify implementation.
- Ignore authorization because a screen is already protected.
- Create duplicate APIs when an existing contract can be safely extended.
- Break the one-Kitchen-per-Order invariant.
- Merge ChefOrderGroups across Chefs.
- Apply Chef A promotions using Chef B's items.
- Calculate Chef payouts from the entire Order without ChefOrderGroup allocation.
- Recalculate historical financial records using current pricing or promotion configuration.
- Introduce a new infrastructure technology merely because it is popular.
- Perform unrelated refactoring.
- Change an approved ADR silently.

---

# 54. First Implementation Principle

Do not start with all screens.

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

---

# 55. Final Principle

The AI is an implementation assistant, not the owner of the architecture.

The human-approved architecture, business requirements, ADRs, database model, API contracts, and event contracts remain authoritative.

When uncertain:

```text
STOP
  ↓
READ
  ↓
IDENTIFY CONFLICT / AMBIGUITY
  ↓
EXPLAIN
  ↓
PROPOSE
  ↓
WAIT FOR APPROVAL
```

Correctness is more important than speed.
Maintainability is more important than code volume.
Explicit business rules are more important than AI assumptions.
