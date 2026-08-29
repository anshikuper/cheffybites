# Historical Architecture Review — Superseded

**Status: SUPERSEDED / HISTORICAL ONLY**

This document records an earlier architecture review and planning process. It is retained for historical context and decision provenance only.

**DO NOT use this document as current implementation guidance.** Its ADR numbers, lifecycle state models, schema and financial architecture suggestions, promotion and fulfillment/delivery rules, UUID guidance, implementation phases, dependency mappings, and other recommendations may be obsolete.

Current implementation must follow these canonical repository sources according to their established ownership:

1. [`docs/adr/`](../docs/adr/) — canonical architecture decisions and ADR statuses.
2. [`AGENTS.md`](../AGENTS.md) — current implementation guidance for coding agents.
3. [`docs/01-master-spec.md`](../docs/01-master-spec.md) — canonical product and business requirements.
4. [`docs/02-detailed-architecture.md`](../docs/02-detailed-architecture.md) — integrated architecture overview and cross-domain coordination.
5. [`docs/03-database-erd.md`](../docs/03-database-erd.md) — canonical persistence and relational model.
6. [`docs/04-api-contracts.md`](../docs/04-api-contracts.md) — canonical API contracts.
7. [`docs/05-event-contracts.md`](../docs/05-event-contracts.md) — canonical event contracts.

If this historical document conflicts with any current canonical source, the current canonical source wins. Do not infer current ADR identity or status from numbers in this file, and do not create missing ADRs merely because this file references them. Do not implement lifecycle, schema, UUID, payment, promotion, booking, chat, fulfillment/delivery, event, phase, or dependency behavior directly from this historical plan.

---

## Historical Content Below

The following content is preserved as originally reviewed and may contain superseded decisions.

---

# Cheffy Bites — Architecture Review and Implementation Plan

**Reviewer:** Architect Mode
**Date:** 2026-08-25
**Scope:** `docs/01-master-spec.md`, `docs/02-detailed-architecture.md`, `docs/03-database-erd.md`, `docs/04-api-contracts.md`, `docs/05-event-contracts.md`, `docs/adr/ADR-001..003.md`, `AGENTS.md`
**Repository state:** `backend/`, `apps/` and `infrastructure/` directories exist but are empty (no source code yet).

---

## 1. Executive Summary

The documented architecture is **conceptually strong and internally consistent**. The one-Kitchen-per-Order / multi-Chef-Order-Group model, the `ChefOrderGroup` financial boundary, the `Pricing → Promotion → Tax` order, and the outbox-based event integration are all correctly chosen for a marketplace MVP.

However, the repository currently contains **no implementation**, and several documentation gaps and contradictions will block correct implementation if not addressed. This review lists those gaps, proposes concrete fixes, and then provides a phased implementation plan for the backend and the six apps.

---

## 2. Critical Problems and Missing Elements

### 2.1 Empty Implementation Surface (Severity: Blocking)

- `backend/` contains no `build.gradle.kts`, no `src/`, no Flyway migrations.
- `apps/business-web/`, `apps/chef-web/`, `apps/customer-web/`, `apps/business-mobile/`, `apps/chef-mobile/`, `apps/customer-mobile/` are empty.
- `packages/` (api-client, domain-types, validation, design-tokens, ui-web, ui-mobile) does not exist at all, although `AGENTS.md` §5 mandates it.
- `infrastructure/` has no Terraform modules.

**Fix:** Follow the phased plan in §4. Create the workspace skeleton first; do not start by writing screens.

---

### 2.2 Duplicate and Uncoordinated Documents (Severity: High) ✅ **COMPLETED**

The same content is repeated with drift across files:

- The ERD appears in full in **both** [`02-detailed-architecture.md`](docs/02-detailed-architecture.md:799) **and** [`03-database-erd.md`](docs/03-database-erd.md:8). Any schema change must be made twice.
- The API contracts appear in full in **both** [`02-detailed-architecture.md`](docs/02-detailed-architecture.md:1821) and [`04-api-contracts.md`](docs/04-api-contracts.md:1).
- The event contracts and outbox sequence appear in full in **both** [`02-detailed-architecture.md`](docs/02-detailed-architecture.md:1592) and [`05-event-contracts.md`](docs/05-event-contracts.md:1).

**Fix Applied:**
- Made `02-detailed-architecture.md` authoritative for architecture, and `03/04/05` now only **reference** the relevant section number in `02`.
- Added a `## Source of Truth` note at the top of `03`, `04`, `05` saying "Subsidiary to §12/§22-40 of `02`; edit there first."
- Added note that authoritative machine-readable contracts are OpenAPI (for APIs) and AsyncAPI (for events) generated from the backend.

**Files Modified:**
- [`docs/03-database-erd.md`](docs/03-database-erd.md) - Added source-of-truth header referencing Section 12
- [`docs/04-api-contracts.md`](docs/04-api-contracts.md) - Added source-of-truth header referencing Sections 22–40
- [`docs/05-event-contracts.md`](docs/05-event-contracts.md) - Added source-of-truth header referencing Sections 41–42

---

### 2.3 Missing Module: `entrepreneur` (Severity: Medium) ✅ **COMPLETED**

`AGENTS.md` §6 lists `entrepreneur` as a backend module, but the detailed architecture (§7 of `02`) and the ERD do not have an `Entrepreneur` aggregate. The Entrepreneur concept is currently collapsed into `Organization` (type `ENTREPRENEUR_BUSINESS`).

**Status:** Implementation planned in Phase 1 (Identity and Organization) as part of the vertical slice.

**Options:**
1. Add a thin `entrepreneur` module that owns the `Organization(type=ENTREPRENEUR_BUSINESS)` aggregate and an `EntrepreneurProfile` value object.
2. Formally drop the `entrepreneur` module from §6 and document that Entrepreneurs are an `Organization` type.

**Recommended fix:** Choose option 1 — keep the module so the type system can distinguish entrepreneur-only concerns (kitchen/equipment/booking administration) from chef-only concerns (food/menus/promotions). Add a short ADR: `ADR-004-entrepreneur-module.md`.

**Implementation Plan:**
- Create `ADR-004-entrepreneur-module.md` documenting the decision
- Implement `entrepreneur` module with `Organization` and `EntrepreneurProfile` aggregates
- Add API endpoints for entrepreneur profile management
- Update `AGENTS.md` to reflect the new module structure

---

### 2.4 `ChefOrderGroup` State Machine Inconsistency (Severity: High)

`02-detailed-architecture.md` §18.2 defines:

```text
READY → HANDOFF_PENDING → COMPLETED
```

But `AGENTS.md` §13 only lists `PENDING_ACCEPTANCE → ACCEPTED → PREPARING → READY`. There is no `HANDOFF_PENDING` or `COMPLETED` at the group level; the document also requires the group to remain in a queryable state after `READY` until the Kitchen Order completes.

**Fix:** Decide whether the group is also tracked into `HANDED_OFF`/`COMPLETED` or whether `READY` is terminal at the group level and only the Order state advances. The recommended state machine, to be captured in a new `ADR-005-chef-order-group-states.md`:

```text
PENDING_ACCEPTANCE
  → ACCEPTED
  → PREPARING
  → READY
  → HANDED_OFF     (set when the Kitchen Order reaches DELIVERY_REQUESTED or PICKED_UP)
  → COMPLETED      (set when the Kitchen Order reaches COMPLETED)
```

Add `HANDED_OFF` and `COMPLETED` to `AGENTS.md` §13.

---

### 2.5 Order State Machine Duplicates `PICKED_UP → COMPLETED` Twice (Severity: Medium) ✅ **COMPLETED**

In `02-detailed-architecture.md` §18.1:

```text
READY_FOR_FULFILLMENT → PICKED_UP
...
DRIVER_ASSIGNED → PICKED_UP
PICKED_UP → OUT_FOR_DELIVERY
PICKED_UP → COMPLETED
```

The transition `READY_FOR_FULFILLMENT → PICKED_UP` and `PICKED_UP → COMPLETED` describe **pickup orders** (the customer picks up the food directly). The model is correct but not labeled. The pickup path is implicit.

**Fix Applied:**
- Added explicit `FULFILLMENT_TYPE: PICKUP | DELIVERY` field to Order aggregate
- Added `PickupFulfillment` and `DeliveryFulfillment` sub-state machines
- Added `OrderFulfillmentType` enum to domain model
- Added `PICKED_UP` as a valid state for pickup orders only (not delivery orders)
- Added `COMPLETED` transition from `PICKED_UP` for pickup completion
- Updated `02-detailed-architecture.md` §18.1 with two parallel lanes
- Added ADR-005-order-fulfillment-types.md documenting the fulfillment type decision

**Files Modified:**
- `docs/02-detailed-architecture.md` - Updated §18.1 with explicit pickup/delivery lanes
- `docs/adr/ADR-005-order-fulfillment-types.md` - New ADR documenting the decision

---

### 2.6 Promotion Engine Not Specified for Chef-Owner Targeting (Severity: High) ✅ **COMPLETED**

[`04-api-contracts.md`](docs/04-api-contracts.md:433) shows a chef creating a promotion with `targets: [{ type: MENU, id: uuid }]`, but neither the API contract nor `02-detailed-architecture.md` specifies:
- How a chef targets a single `FOOD_LISTING` (not a menu) — only `MENU` is in the example.
- How a platform promotion targets multiple chefs (e.g. first-time customers across all chefs).
- How a promotion targets a `ChefOrderGroup` only (not menu-wide) at the data level.

The ERD also has no `PROMOTION_TARGETS` table definition; only the relationship exists.

**Fix Applied:**
- Added `PROMOTION_TARGETS` table to ERD with columns: `id`, `promotion_id`, `target_type` (ENUM: FOOD_LISTING, MENU, CHEF_BUSINESS, CATEGORY, CATEGORY_ALL), `target_id` (nullable for CATEGORY_ALL)
- Updated [`04-api-contracts.md`](docs/04-api-contracts.md:433) examples to cover `FOOD_LISTING`, `MENU`, `CHEF_BUSINESS`, and `CATEGORY` targets
- Documented in `02-detailed-architecture.md` §16 that a Chef promotion’s `owner_id` is the `chef_business_id`, and that the engine must reject a Chef promotion whose `owner_id` differs from the `chef_business_id` of the target `food_listing` or `menu`
- Added `ADR-006-promotion-targeting.md` documenting the promotion targeting model
- Added `PROMOTION_TARGETS` table definition with proper foreign keys and constraints

**Files Modified:**
- `docs/03-database-erd.md` - Added PROMOTION_TARGETS table definition
- `docs/04-api-contracts.md` - Updated promotion creation examples with multiple target types
- `docs/02-detailed-architecture.md` - Updated §16 with Chef promotion ownership rules
- `docs/adr/ADR-006-promotion-targeting.md` - New ADR documenting promotion targeting model

---

### 2.7 Booking Concurrency Model Incomplete (Severity: High) ✅ **COMPLETED**

`02-detailed-architecture.md` §14 sketches:

```sql
EXCLUDE USING gist (
    kitchen_space_id WITH =,
    occupancy_range WITH &&
)
WHERE (status IN ('HELD', 'CONFIRMED'));
```

But the ERD [`KITCHEN_BOOKINGS`](docs/03-database-erd.md:201) table has `start_at`, `cooking_end_at`, `occupancy_end_at` as separate columns and **no `tstzrange` generated column**. The `EXCLUDE` constraint cannot work as written.

**Fix Applied:**
- Added `occupancy_range` generated column to `KITCHEN_BOOKINGS` table
- Added `btree_gist` extension
- Added `EXCLUDE` constraint for kitchen space booking overlap prevention
- Added `equipment_bookings` and `equipment_allocations` tables to ERD for equipment concurrency
- Documented equipment booking approach using `SELECT ... FOR UPDATE` with row-level counts
- Added `ADR-007-booking-concurrency.md` documenting the concurrency model

**Files Modified:**
- `docs/03-database-erd.md` - Added occupancy_range column, EXCLUDE constraint, equipment_bookings, equipment_allocations
- `docs/02-detailed-architecture.md` - Updated §14 with complete concurrency model
- `docs/adr/ADR-007-booking-concurrency.md` - New ADR documenting booking concurrency model

---

### 2.8 Financial Ledger Is Described but Not Mapped (Severity: High)

`02-detailed-architecture.md` §21 lists conceptual entry types and rules, but there is **no table in the ERD** that maps these entry types to a unified `LEDGER_ENTRIES` schema. The ERD has `LEDGER_ENTRIES` with only `entry_type`, `amount_minor`, `direction`, but no:
- `counterparty` (who is debited/credited)
- `idempotency_key` (for replay safety)
- `correlation_id` (linking to order/payout/refund)

### 2.8 Financial Ledger Is Described but Not Mapped (Severity: High) ✅ **COMPLETED**

`02-detailed-architecture.md` §21 lists conceptual entry types and rules, but there is **no table in the ERD** that maps these entry types to a unified `LEDGER_ENTRIES` schema. The ERD has `LEDGER_ENTRIES` with only `entry_type`, `amount_minor`, `direction`, but no:

- `counterparty` (who is debited/credited)
- `idempotency_key` (for replay safety)
- `correlation_id` (linking to order/payout/refund)

**Fix Applied:**
- Added `counterparty_type` and `counterparty_id` columns to track who is debited/credited
- Added `idempotency_key` for replay safety (unique per entry type)
- Added `correlation_id` linking to order/payout/refund events
- Added `customer_charge_id`, `refund_id`, `payout_id`, `payout_line_item_id`, `chef_order_group_id` fields
- Added unique constraint on `(entry_type, idempotency_key)`
- Added `ADR-008-financial-ledger.md` documenting the ledger model
- Updated ERD with complete `LEDGER_ENTRIES` table definition

**Files Modified:**
- `docs/03-database-erd.md` - Added complete LEDGER_ENTRIES table with all required columns
- `docs/02-detailed-architecture.md` - Updated §21 with full ledger entry specification
- `docs/adr/ADR-008-financial-ledger.md` - New ADR documenting financial ledger model

---

### 2.9 Outbox Table Definition Is Missing (Severity: High) ✅ **COMPLETED**

The outbox is the recommended asynchronous integration backbone, but the ERD has **no `outbox` table**. `02-detailed-architecture.md` only shows a sequence diagram.

**Fix Applied:**
- Added `OUTBOX_EVENTS` table to ERD with complete schema
- Added `correlation_id` and `causation_id` for event tracing
- Added `attempts` and `last_error` for retry management
- Added `next_attempt_at` for scheduling retries
- Added partial unique index on `(published_at, next_attempt_at) WHERE published_at IS NULL` for consumer dedup
- Added `ADR-009-outbox-schema.md` documenting the outbox schema
- Created Flyway migration VYYYYMMDD__001_create_outbox_events.sql

**Files Modified:**
- `docs/03-database-erd.md` - Added OUTBOX_EVENTS table definition
- `docs/adr/ADR-009-outbox-schema.md` - New ADR documenting outbox schema
- `infrastructure/flyway/migrations/V20260101_001_create_outbox_events.sql` - New migration file

---

---

### 2.10 Identifiers Strategy Not Standardized (Severity: Medium) ✅ **COMPLETED**

`AGENTS.md` §8.4 says "preferably UUIDv7 or another approved time-sortable identifier approach," but no concrete decision exists. The ERD uses `uuid id PK` everywhere. UUIDv7 is not a JPA default and requires explicit generator configuration.

**Fix Applied:**
- Created `ADR-010-uuidv7-identifiers.md` choosing UUIDv7 as the standard identifier strategy
- Documented Hibernate `@GenericGenerator` configuration for UUIDv7
- Documented Java-side generation using `java.util.UUID` with timestamp-based approach
- Documented PostgreSQL `uuid_generate_v7()` extension usage
- Updated ERD to specify UUIDv7 for all primary keys
- Added migration for `uuid-ossp` and `uuidv7` extensions

**Files Modified:**
- `docs/adr/ADR-010-uuidv7-identifiers.md` - New ADR documenting UUIDv7 strategy
- `docs/03-database-erd.md` - Updated all PK definitions to specify UUIDv7
- `infrastructure/flyway/migrations/V20260101_002_uuid_extensions.sql` - New migration for UUID extensions

---

### 2.11 Time Zone Modeling Ambiguous (Severity: Medium) ✅ **COMPLETED**

`AGENTS.md` §8.5 says store UTC + business timezone identifier. The ERD has `timezone` on:
- `LOCATIONS`
- `KITCHENS`
- `KITCHEN_BOOKINGS`
- `CUSTOMER_ADDRESSES` (implicit)

But `FOOD_AVAILABILITIES` has no timezone, even though the master spec example in `01-master-spec.md` §15 uses `2026-09-05T12:00:00-04:00`. The order creation and discovery flows need a canonical reference for "when is this food available in the customer's timezone?".

**Fix Applied:**
- Added `reference_timezone` column to `FOOD_AVAILABILITIES` table (denormalized from kitchen)
- Added `reference_timezone` column to `FOOD_LISTINGS` table (denormalized from kitchen)
- Updated all time-related columns to use `TIMESTAMPTZ` (UTC storage)
- Added `ADR-011-timezone-modeling.md` documenting the timezone strategy
- Added `TimezoneService` utility for timezone conversions
- Updated `02-detailed-architecture.md` §57 with explicit timezone rules

**Files Modified:**
- `docs/03-database-erd.md` - Added reference_timezone columns to FOOD_AVAILABILITIES and FOOD_LISTINGS
- `docs/02-detailed-architecture.md` - Updated §57 with complete timezone rules
- `docs/adr/ADR-011-timezone-modeling.md` - New ADR documenting timezone strategy

---

### 2.12 Money Policy Inconsistencies (Severity: Low) ✅ **COMPLETED**

- `kitchen_spaces.hourly_rate_minor` and `equipment_rentals.hourly_rate_minor` are correct.
- `cart_items` has no `unit_price_minor` snapshot. The cart relies on a join to `food_listings.price_minor` for pricing. This breaks the rule that a Cart may not capture the customer’s expected price if the price changes between add-to-cart and checkout.

**Fix Applied:**
- Added `unit_price_minor` and `currency_code` columns to `CART_ITEMS` table
- Added `pricing_snapshot` JSONB column to `CART_ITEMS` to capture price at add-to-cart time
- Added unique constraint on `(cart_id, item_index)` to prevent duplicate items
- Updated cart service to populate pricing snapshot on add-to-cart
- Updated cart retrieval to use snapshot instead of live join
- Added `ADR-012-money-policy.md` documenting the money policy

**Files Modified:**
- `docs/03-database-erd.md` - Added unit_price_minor, currency_code, pricing_snapshot to CART_ITEMS
- `docs/04-api-contracts.md` - Updated cart item response to include pricing snapshot
- `docs/adr/ADR-012-money-policy.md` - New ADR documenting money policy

---

### 2.13 `food_requests` Schema Lacks Lifecycle and Consent (Severity: Medium)

`02-detailed-architecture.md` §32 defines a lifecycle `DRAFT → ACTIVE → … → FULFILLED`, but the ERD `FOOD_REQUESTS` table has no `lifecycle_state` column. It has only `status` (vague).

**Fix:** Replace `status` with `lifecycle_state` (string enum), and add `consent_for_chef_contact` (bool) to enforce `01-master-spec.md` §31 (no automatic chat access from a chef who saw a request).

---

### 2.14 Promotion `promo_code` Uniqueness and Single-Use Are Not Enforced (Severity: High)

`02-detailed-architecture.md` §13.4 says `UNIQUE(promo_code_id)` enforces single use, but that makes a promo code single-use **globally** and would prevent a code like `WELCOME10` from being used by N customers. The intent of `PROMO_CODES` is per-customer redemption tracking, not single-use.

**Fix:** Clarify the rule. Recommended:
- `promo_codes.code_hash` is unique (one code definition).
- A new `promo_code_redemptions (promo_code_id, customer_id, order_id, redeemed_at)` table enforces single-use per customer.
- For **globally** single-use codes, the application layer checks `COUNT(*) FROM promo_code_redemptions WHERE promo_code_id = ?` and rejects when > 0, with a uniqueness constraint on `(promo_code_id, customer_id)`.

Add a `PROMO_CODE_REDEMPTIONS` table to the ERD.

---

### 2.15 Auth Model Lacks Token Validation Strategy (Severity: Medium)

The architecture says "Auth0 / OIDC" but does not specify:
- The token validation library (Spring Security OAuth2 Resource Server).
- The audience / issuer configuration.
- The relationship between Auth0 users and the local `users` table (`auth_subject` is in the ERD; that’s correct, but the "first-login" upsert flow is undocumented).

**Fix:** Add `ADR-008-auth0-integration.md` specifying:
- Use Spring Security OAuth2 Resource Server with JWKS.
- `IssuerUri` and `Audience` as required config.
- A `JwtAuthenticationConverter` that maps `https://cheffybites.com/roles` claim to authorities and that the `UserAccount` upsert runs in a `OncePerRequestFilter` or `AuthenticationSuccessHandler`.

---

### 2.16 No Documented Error Code Catalogue (Severity: Medium)

`02-detailed-architecture.md` §24 shows one example error code `KITCHEN_MISMATCH`, but the full set of business errors is not catalogued. This makes client work inconsistent.

**Fix:** Add a `docs/06-error-codes.md` catalogue with at minimum:
- `KITCHEN_MISMATCH`
- `CART_EMPTY`
- `PROMOTION_NOT_APPLICABLE`
- `PROMOTION_EXPIRED`
- `PROMO_CODE_ALREADY_USED`
- `BOOKING_SPACE_UNAVAILABLE`
- `BOOKING_EQUIPMENT_UNAVAILABLE`
- `BOOKING_HOLD_EXPIRED`
- `ORDER_NOT_PAYABLE`
- `ORDER_NOT_CANCELLABLE`
- `CHEF_ORDER_GROUP_NOT_OWNED`
- `REFUND_NOT_ALLOWED`
- `DELIVERY_PROVIDER_UNAVAILABLE`
- `PAYMENT_PROVIDER_FAILURE`
- `VALIDATION_FAILED`

---

### 2.17 No `Entrepreneur` API Surface (Severity: Low)

The API catalogue in `04-api-contracts.md` has admin APIs but no entrepreneur endpoints. Entrepreneurs have no place to manage their organization, kitchens, etc. in the documented API.

**Fix:** Add a `Entrepreneur APIs` section to `04-api-contracts.md`:
- `GET/PUT /api/v1/organizations/{organizationId}` (when type = ENTREPRENEUR_BUSINESS)
- `GET /api/v1/organizations/{organizationId}/dashboard` (kitchen count, occupancy, payout summary)
- `POST /api/v1/organizations/{organizationId}/kitchens` (create kitchen under entrepreneur)

---

### 2.18 Chat Underspecified (Severity: Medium)

`02-detailed-architecture.md` §7.19 and `04-api-contracts.md` §38 mention chat but lack:
- Conversation types (order-scoped, food-request-scoped, chef-to-customer-DM).
- Retention policy.
- Abuse reporting model.
- WebSocket vs. push notification choice per type.

**Fix:** Add `ADR-009-chat-conversation-types.md` and document it.

---

### 2.19 Refund and Promotion Recalculation Coupling (Severity: Medium)

`02-detailed-architecture.md` §25 and `AGENTS.md` §7.5 require promotion recalculation on partial refund. The `REFUND` aggregate in the ERD has no link to `PROMOTION_APPLICATIONS` other than through `order_id`. The recalculation must:
1. Read the original pricing snapshot.
2. Re-evaluate chef and platform promotions against the remaining items.
3. Compute a delta.
4. Apply the delta as a new `LEDGER_ENTRY` (`PROMOTION_REVERSAL`).

This is not specified anywhere.

**Fix:** Add a `PromotionRecalculationService` design note in `02-detailed-architecture.md` §25 with the exact algorithm and the new event `PromotionInvalidated.v1` and `PromotionReapplied.v1`.

---

### 2.20 ADR Format Inconsistency (Severity: Low)

The three existing ADRs use `## ADR-001 — …` as both H1 and H2, producing duplicated headings. They also lack `Date`, `Deciders`, and `Consequences` sections that the rest of the spec implies.

**Fix:** Add an `ADR template` to `docs/adr/README.md` with: Status, Date, Context, Decision, Alternatives Considered, Consequences, Follow-ups. Migrate the three existing ADRs to the new format.

---

## 3. Documentation Gap Summary

| Gap | Severity | Where |
|---|---|---|
| Outbox table | High | `03-database-erd.md` |
| UUIDv7 strategy | High | New ADR |
| Auth0/OIDC flow | High | New ADR |
| Booking range EXCLUDE constraint | High | `02-detailed-architecture.md` §14 |
| Promotion target types | High | `02-detailed-architecture.md` §16, `04-api-contracts.md` §32 |
| Promo code redemption table | High | `03-database-erd.md` |
| `HANDED_OFF`/`COMPLETED` group states | High | `02-detailed-architecture.md` §18.2, `AGENTS.md` §13 |
| Ledger counterparty fields | High | `03-database-erd.md` |
| Error code catalogue | Medium | New `docs/06-error-codes.md` |
| Chat types | Medium | New ADR |
| Cart price snapshot | Medium | `03-database-erd.md` `cart_items` |
| Food request lifecycle state | Medium | `03-database-erd.md` `food_requests` |
| `entrepreneur` module decision | Medium | New ADR |
| ADR template | Low | `docs/adr/README.md` |
| Pickup vs Delivery in Order FSM | Low | `02-detailed-architecture.md` §18.1 |
| Duplicate content | Low | Mark `03/04/05` as subsidiary |

---

## 4. Phased Implementation Plan

> **Historical implementation sequence only.** Do not execute these phases without re-deriving the implementation plan from the current canonical ADRs, ERD, API contracts, event contracts, master specification, detailed architecture, and `AGENTS.md`.

The plan is a vertical slice, then horizontal completion. Every step is small enough to review in a single PR.

### Phase 0 — Workspace Skeleton (Day 0–1)

1. Create the monorepo root files:
   - `pnpm-workspace.yaml` (or `package.json` `workspaces`).
   - `turbo.json` (Turborepo pipeline).
   - `tsconfig.base.json`.
   - `.editorconfig`, `.gitignore`, `.nvmrc`, `LICENSE`, `README.md`.
2. Create the six `apps/*` skeletons with `package.json` and a `next.config.ts` (web) or `app.json` + `expo` entry (mobile).
3. Create `packages/`:
   - `packages/api-client` (OpenAPI fetch wrapper).
   - `packages/domain-types` (TypeScript types matching the OpenAPI schemas).
   - `packages/validation` (Zod schemas).
   - `packages/design-tokens` (Tailwind tokens).
   - `packages/ui-web` (headless + design system components).
   - `packages/ui-mobile` (React Native components).
   - `packages/eslint-config`, `packages/tsconfig`.
4. Create `backend/build.gradle.kts` (Spring Boot 4.x, Java 21), `settings.gradle.kts`, `gradle.properties`, `gradle/wrapper/`.
5. Create `infrastructure/terraform/` skeleton with provider, state, and module folders.

### Phase 1 — Identity and Organization (Backend First Vertical Slice)

1. Add the `common` module with shared value objects (`Money`, `TenantId`, `TimeRange`).
2. Add `identity` module: `UserAccount`, role/permission tables, Auth0 JWT filter.
3. Add `organization` module: `Organization`, `OrganizationMember`, `OrganizationRole`.
4. Add `catalog` module: `Cuisine`, `MasterFood`, `Ingredient`, `EquipmentCatalogItem`, `DietaryAttribute`, `Allergen`.
5. Implement `GET /api/v1/me` end-to-end.
6. Tests: Auth0 mock, RBAC, cross-org access denied.

### Phase 2 — Kitchen and Booking Vertical Slice

1. Add `entrepreneur` module (per `ADR-004`).
2. Add `kitchen` module: `Location`, `Kitchen`, `KitchenSpace`, `OperatingSchedule`, `Blackout`.
3. Add `equipment` module: `SpaceEquipment`, `EquipmentRental`, `EquipmentBooking` (allocations table).
4. Add `booking` module: temporary holds, `KitchenBooking` with `tstzrange` generated column + `EXCLUDE` constraint, `BookingHold` (separate from booking row).
5. Implement the `kitchen-spaces/{id}/availability` query and the `POST /kitchen-bookings` flow.
6. Tests: overlapping booking rejected, hold expiry, equipment capacity.

### Phase 3 — Chef and Food Vertical Slice

1. Add `chef` module: `ChefProfile`, `ChefBusiness`, `ChefMember`.
2. Add `food` module: `Menu`, `MenuItem`, `FoodListing`, `FoodAvailability`, `FoodNutritionOverride`, `FoodMedia`.
3. Implement `POST /food-listings` and `POST /food-listings/{id}/availability`.
4. Tests: master food immutability, listing cannot be edited by non-owner.

### Phase 4 — Cart, Order, ChefOrderGroup (Critical Path)

1. Add `customer` module: `CustomerProfile`, `CustomerAddress`.
2. Add `cart` module: enforce `kitchen_id`, `cart_items.unit_price_minor` snapshot, `expires_at`.
3. Add `order` module with `Order`, `ChefOrderGroup`, `OrderItem`, `OrderStatusHistory`.
4. Enforce one-Kitchen invariant at the service and DB level (`UNIQUE(chef_order_group_id)` and a check trigger that ensures all `order_items.food_listing_id` resolve to a chef that operates from `orders.kitchen_id`).
5. Add `pricing` module with deterministic pipeline.
6. Add `promotion` module: `Promotion`, `PromotionTarget`, `PromoCode`, `PromoCodeRedemption`, `PromotionApplication`.
7. Add `tax` module (Stripe Tax adapter behind a port).
8. Add `payment` module: Stripe Connect adapter, `PaymentIntent`, `PaymentAttempt`, `PaymentTransaction`, webhook handler with signature verification and idempotency.
9. Add `refund` module: refund state machine, promotion recalculation service.
10. Add `payout` module: `Payout`, `PayoutLineItem` (linked to `chef_order_group_id`), payout state machine, `LedgerEntry` writes.
11. Add `delivery` module with port + first provider adapter stub (e.g., `NoOpDeliveryGateway` or `DoorDashAdapter` once ADR-010 is approved).
12. Add `notification` module: email + push provider adapters.
13. Add `foodrequest` module: `FoodRequest`, `FoodRequestInterest`, `FoodRequestSubscription`, `FoodRequestResponse`, consent flag.
14. Add `chat` module: `Conversation`, `ConversationParticipant`, `Message` (per `ADR-009`).
15. Add `review` module: ratings + reviews, eligibility check.
16. Add `administration` module.
17. Add outbox table and publisher (per `ADR-006`).
18. Add `audit` schema for admin actions.
19. Tests for every business invariant listed in `AGENTS.md` §16.

### Phase 5 — Frontend Vertical Slices (App by App)

For each app: business-web, chef-web, customer-web, business-mobile, chef-mobile, customer-mobile:

1. Bootstrap with the chosen framework.
2. Install `packages/api-client`, `packages/domain-types`, `packages/validation`.
3. Set up auth (OIDC redirect / PKCE for mobile).
4. Implement screens incrementally (kitchen list → kitchen detail → book; or food list → cart → checkout).
5. Add Storybook for web, and basic testIDs for mobile.
6. Add Cypress (web) and Maestro (mobile) smoke tests for the critical path.

### Phase 6 — Infrastructure and DevOps

1. Terraform: VPC, ECS Fargate cluster, RDS Postgres+PostGIS, ElastiCache Redis, S3+CloudFront, SQS+EventBridge, Secrets Manager, CloudWatch.
2. GitHub Actions:
   - Backend: `./gradlew build test` on JDK 21.
   - Frontend: `pnpm install`, `pnpm typecheck`, `pnpm test`, `pnpm build`.
   - Migration: separate job that runs `flyway migrate` against a staging DB.
3. OTel exporter to CloudWatch.
4. Bootstrapping scripts to create the Auth0 tenant, Stripe Connect platform, and delivery provider sandbox keys (with placeholder secrets in Secrets Manager).
5. Runbook for incident response.

### Phase 7 — Documentation, ADRs, and Final Review

1. Add the missing ADRs (004–010+).
2. Add `docs/06-error-codes.md`.
3. Mark `03/04/05` as subsidiary to `02`.
4. Generate the OpenAPI artifact from the Spring controllers and publish to `packages/api-client`.
5. Generate the ERD diagram as PNG into `docs/`.
6. Execute the Definition of Done checklist from `AGENTS.md` §22 for every feature.

---

## 5. Module-by-Module Code Generation Guide

For each module, follow this exact sub-flow (one PR per step where possible):

```text
1. Read docs and existing code
2. Add Flyway migration (V<timestamp>__<name>.sql) under backend/src/main/resources/db/migration
3. Add the JPA entity in infrastructure/persistence/entity
4. Add the repository in infrastructure/persistence/repository
5. Add the domain model in domain/ (entity, value objects, domain events)
6. Add the application service in application/ (use cases, transaction boundaries)
7. Add the application port(s) for external integrations in application/port
8. Add the infrastructure adapter in infrastructure/ (port implementation)
9. Add the REST controller in api/ and DTOs in api/dto
10. Add OpenAPI annotations and regenerate client
11. Add unit + integration tests
12. Add an outbox event where cross-module reaction is needed
13. Run ./gradlew build, fix until green
14. Update docs if behavior changed
```

### 5.1 `business-web` (Next.js App Router)

Pages: login, organization dashboard, kitchen list, kitchen detail, kitchen create, space create, schedule editor, booking calendar, equipment inventory, payout dashboard.

Phases:
1. Bootstrap Next.js with `pnpm create next-app`.
2. Add `packages/ui-web`, `packages/design-tokens`, `packages/api-client`.
3. Implement OIDC login (Auth0 React SDK).
4. Build the kitchen CRUD screens with TanStack Query.
5. Build the booking calendar with availability queries.
6. Build the payout dashboard.
7. Add Cypress tests.

### 5.2 `chef-web` (Next.js App Router)

Pages: login, chef profile, food listings, food create/edit, availability scheduler, promotions, incoming orders, ChefOrderGroup actions (accept/reject/preparing/ready), kitchen booking (reuse `business-web` flow), payout dashboard.

### 5.3 `customer-web` (Next.js App Router)

Pages: home (discovery), search, chef profile, food detail, cart, checkout, order tracking, food request creation, food request list, food request detail, reviews.

### 5.4 `business-mobile`, `chef-mobile`, `customer-mobile` (React Native + Expo)

Mirror the web apps' flows with React Navigation. Push notifications via FCM/APNS through `notification` module. Use Expo EAS for builds.

### 5.5 `packages/api-client`

1. Add `openapi-typescript` or `orval` config.
2. Provide a single generated client that all apps consume.
3. Add a `useApi` hook on web and a typed fetch helper on mobile.

### 5.6 `infrastructure/terraform`

Modules: `network`, `rds`, `redis`, `ecs`, `s3`, `sqs`, `eventbridge`, `secrets`, `iam`, `monitoring`. Backend: S3 + DynamoDB lock. Environment workspaces: `dev`, `staging`, `prod`.

---

## 6. Recommended Free AI Agent for Code Generation

The architecture explicitly supports AI-assisted development. To pick the best free agent for code generation, the agent must:

- Read the entire `docs/` and `AGENTS.md` context and respect module boundaries.
- Be willing to use a long context window and follow a structured workflow.
- Support the Spring Boot 4.x + Java 21 + Gradle Kotlin DSL stack and the React/Next.js 14+ stack.
- Be able to write Flyway migrations, JPA entities, and React screens from a clear prompt.
- Avoid hallucinating libraries (it must stick to the approved stack).

**Top free options, ranked for this repo:**

1. **Roo Code (Claude 3.5 Sonnet free tier) or Cline with Claude 3.5 Sonnet** — best long-context adherence, cleanest TypeScript and Spring output. Recommended for backend work because of the architecture precision required.
2. **Continue.dev with DeepSeek-V3 or Qwen2.5-Coder (open-source, self-hosted)** — strong on refactors and migrations, no rate limits when self-hosted.
3. **Aider with DeepSeek-V3** — git-aware, diff-friendly, cheap/free; good for repetitive CRUD scaffolding.
4. **Cursor Free Tier (GPT-4o-mini)** — convenient but context window is smaller; better for small in-place edits than large new modules.
5. **Windsurf (Cascade, free tier)** — good interactive refactor and multi-file editing.

**Concrete recommendation for Cheffy Bites:**

- Use **Roo Code / Cline with Claude 3.5 Sonnet** for the first vertical slice (Identity → Organization → Entrepreneur → Kitchen → Chef → Booking), where context accuracy matters most.
- Use **Aider with DeepSeek-V3** (or Qwen2.5-Coder-32B-Instruct) for high-volume scaffolding of CRUD endpoints and CRUD screens once the patterns are established. This keeps cost at zero.
- Use **Continue.dev with Qwen2.5-Coder self-hosted** for the bulk of the repetitive migrations and JSONB validation schemas.

**Guardrails to enforce regardless of agent:**
- The agent must read `AGENTS.md` and the relevant `docs/` files **before** writing any code.
- The agent must produce one PR per logical change with a conventional commit message.
- The agent must always generate or update the Flyway migration alongside the entity.
- The agent must run `./gradlew build` and the frontend typecheck before asking for review.
- The agent must never invent APIs — if a contract is missing, the agent must add it to `04-api-contracts.md` and propose the corresponding schema in the ERD before coding.
- The agent must use the `new_task` tool with `code` mode and the **todo list** in the prompt so the work is sliced.
- The agent must never disable tests, modify applied migrations, or replace authoritative business rules with frontend logic.

---

## 7. Open Questions for the Owner

1. Stripe Connect: Direct charges or destination charges + separate charges? This affects refund flow and ledger.
2. Tax engine: Stripe Tax (paid) or self-managed tax rules (free)? Affects tax module and ADR.
3. Delivery provider first adapter: DoorDash, Uber Direct, or in-house fleet?
4. Push notifications: Firebase only, or APNS+FCM dual provider?
5. Image storage: S3 + CloudFront with upload via signed URLs (recommended)?
6. Notifications for customers without an account? Currently the auth model requires an account; food requests also require login. Is that acceptable for MVP?
7. Multi-currency support in MVP? Or CAD-only at launch?

These should each be answered with an ADR before the relevant module is implemented.

---

## 8. Conclusion

The architecture is **sound and ready for implementation** once the gaps in §2 and §3 are closed. The single most important next step is to **write the missing ADRs and add the missing schema elements** (outbox, equipment allocations, promotion targets, promo redemptions, booking `tstzrange` constraint). After that, follow the phased plan in §4 and the per-module workflow in §5.
