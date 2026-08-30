# Cheffy Bites — Database ERD & Data Model

> **Canonical ownership:** This document is the canonical relational and persistence model for Cheffy Bites.
> Accepted ADRs under `docs/adr/` govern architectural decisions; Proposed ADRs remain Proposed until explicitly accepted.
> [`02-detailed-architecture.md`](02-detailed-architecture.md) may summarize persistence architecture for integrated understanding, but it must not override the tables, relationships, constraints, or persistence representation defined here.

---

## Database ERD

**Financial schema legend:** The schema-neutral business labels `PAYMENTS`, `PAYMENT_ATTEMPTS`, `PAYMENT_ALLOCATIONS`, `REFUNDS`, `REFUND_LINES`, `PAYOUTS`, `PAYOUT_LINES`, `IDEMPOTENCY_KEYS`, `PROVIDER_EVENTS`, `LEDGER_TRANSACTIONS`, and `LEDGER_ENTRIES` in the Mermaid ERD map to exactly the 11 foundational tables in the canonical `financial.*` persistence schema. They do not define or imply a competing `payment.*` schema. Pricing, fee, tax, promotion, and delivery-price evidence remains with its owning domain rather than becoming `financial.*` persistence.

```mermaid
erDiagram
    USERS ||--o{ ORGANIZATION_MEMBERS : belongs_to
    ORGANIZATIONS ||--o{ ORGANIZATION_MEMBERS : has
    ROLES ||--o{ ORGANIZATION_MEMBERS : assigns

    ORGANIZATIONS ||--o{ LOCATIONS : owns
    LOCATIONS ||--o{ KITCHENS : contains
    KITCHENS ||--o{ KITCHEN_SPACES : contains

    KITCHEN_SPACES ||--o{ SPACE_EQUIPMENT : includes
    EQUIPMENT_CATALOG_ITEMS ||--o{ SPACE_EQUIPMENT : referenced_by
    KITCHEN_SPACES ||--o{ EQUIPMENT_RENTALS : offers
    EQUIPMENT_CATALOG_ITEMS ||--o{ EQUIPMENT_RENTALS : rented_as

    KITCHENS ||--o{ KITCHEN_AVAILABILITIES : defines
    KITCHEN_SPACES ||--o{ KITCHEN_SPACE_AVAILABILITIES : defines
    KITCHEN_SPACES ||--o{ KITCHEN_BOOKINGS : reserved
    CHEF_PROFILES ||--o{ KITCHEN_BOOKINGS : creates
    KITCHEN_BOOKINGS ||--o{ EQUIPMENT_BOOKINGS : requests
    EQUIPMENT_RENTALS ||--o{ EQUIPMENT_BOOKINGS : requested_as
    KITCHEN_BOOKINGS ||--o{ EQUIPMENT_ALLOCATIONS : reserves
    EQUIPMENT_RENTALS ||--o{ EQUIPMENT_ALLOCATIONS : allocated_as

    CHEF_BUSINESSES ||--o{ CHEF_MEMBERS : has
    USERS ||--o{ CHEF_MEMBERS : participates
    CHEF_BUSINESSES ||--o{ MENUS : owns
    MENUS ||--o{ MENU_ITEMS : contains
    FOOD_LISTINGS ||--o{ MENU_ITEMS : referenced
    CHEF_BUSINESSES ||--o{ FOOD_LISTINGS : owns
    KITCHENS ||--o{ FOOD_LISTINGS : owns
    MASTER_FOODS ||--o{ FOOD_LISTINGS : based_on

    CUISINES ||--o{ MASTER_FOODS : classifies
    MASTER_FOODS ||--o{ FOOD_INGREDIENTS : has
    INGREDIENTS ||--o{ FOOD_INGREDIENTS : used_in
    MASTER_FOODS ||--o{ NUTRITION_PROFILES : has
    FOOD_LISTINGS ||--o{ FOOD_NUTRITION_OVERRIDES : customizes
    FOOD_LISTINGS ||--o{ FOOD_AVAILABILITY_RULES : defines
    FOOD_LISTINGS ||--o{ FOOD_AVAILABILITY_OCCURRENCES : materializes
    FOOD_AVAILABILITY_RULES ||--o{ FOOD_AVAILABILITY_OCCURRENCES : produces

    USERS ||--o| CUSTOMER_PROFILES : has
    CUSTOMER_PROFILES ||--o{ CUSTOMER_ADDRESSES : has

    CUSTOMER_PROFILES ||--o{ CARTS : owns
    CARTS ||--o{ CART_ITEMS : contains
    FOOD_LISTINGS ||--o{ CART_ITEMS : added
    KITCHENS ||--o{ CARTS : scopes

    CARTS ||--o| ORDERS : converts_to
    KITCHENS ||--o{ ORDERS : fulfills
    ORDERS ||--o{ CHEF_ORDER_GROUPS : contains
    CHEF_ORDER_GROUPS ||--o{ ORDER_ITEMS : contains
    ORDERS ||--o{ ORDER_ITEMS : contains
    FOOD_LISTINGS ||--o{ ORDER_ITEMS : purchased
    ORDERS ||--o{ ORDER_STATUS_HISTORY : changes

    PROMOTIONS ||--o{ PROMOTION_RULES : has
    PROMOTIONS ||--o{ PROMOTION_TARGETS : targets
    PROMOTIONS ||--o{ PROMO_CODES : exposes
    PROMO_CODES ||--o{ PROMO_CODE_REDEMPTIONS : redeemed_through
    CUSTOMER_PROFILES ||--o{ PROMO_CODE_REDEMPTIONS : claims
    ORDERS ||--o{ PROMO_CODE_REDEMPTIONS : consumes
    ORDERS ||--o{ PROMOTION_APPLICATIONS : receives
    CHEF_ORDER_GROUPS ||--o{ PROMOTION_APPLICATIONS : scopes
    ORDER_ITEMS ||--o{ PROMOTION_APPLICATIONS : item_scopes
    PROMOTIONS ||--o{ PROMOTION_APPLICATIONS : applied
    PROMO_CODES ||--o{ PROMOTION_APPLICATIONS : code_backed
    PROMO_CODE_REDEMPTIONS ||--o{ PROMOTION_APPLICATIONS : evidenced_by
    PROMOTIONS ||--o{ PROMOTION_SNAPSHOTS : snapshots
    PROMOTION_SNAPSHOTS ||--o{ PROMOTION_APPLICATION_ITEMS : allocates

    ORDERS ||--o{ PRICING_SNAPSHOTS : priced
    CHEF_ORDER_GROUPS ||--o{ PRICING_SNAPSHOTS : optionally_scopes
    ORDERS ||--o| PAYMENTS : food_order_payment
    PAYMENTS ||--o{ PAYMENT_ATTEMPTS : attempts
    PAYMENTS ||--o{ PAYMENT_ALLOCATIONS : allocates
    ORDERS ||--o{ PAYMENT_ALLOCATIONS : food_source_reference
    CHEF_ORDER_GROUPS ||--o{ PAYMENT_ALLOCATIONS : referenced_by
    ORDERS ||--o{ REFUNDS : food_refund_reference
    PAYMENTS ||--o{ REFUNDS : refunded_by
    REFUNDS ||--o{ REFUND_LINES : lines
    PAYMENT_ALLOCATIONS ||--o{ REFUND_LINES : optionally_traced_by
    CHEF_ORDER_GROUPS ||--o{ REFUND_LINES : referenced_by

    ORDERS ||--o{ FEE_LINE_ITEMS : charged
    ORDERS ||--o{ TAX_LINE_ITEMS : taxed

    PAYOUTS ||--o{ PAYOUT_LINES : contains
    PAYMENT_ALLOCATIONS ||--o{ PAYOUT_LINES : optionally_referenced_by
    CHEF_ORDER_GROUPS ||--o{ PAYOUT_LINES : referenced_by
    LEDGER_TRANSACTIONS ||--|{ LEDGER_ENTRIES : contains

    ORDERS ||--o| DELIVERIES : may_have
    DELIVERIES ||--o{ DELIVERY_EVENTS : emits

    USERS ||--o{ CHAT_PARTICIPANTS : participates
    CHAT_CONVERSATIONS ||--o{ CHAT_PARTICIPANTS : has
    CHAT_CONVERSATIONS ||--o{ CHAT_MESSAGES : contains

    ORDERS ||--o{ RATINGS : enables
    ORDERS ||--o{ REVIEWS : enables
    FOOD_LISTINGS ||--o{ RATINGS : receives
    CHEF_BUSINESSES ||--o{ RATINGS : receives

    CUSTOMER_PROFILES ||--o{ FOOD_REQUESTS : creates
    MASTER_FOODS ||--o{ FOOD_REQUESTS : normalized_to
    FOOD_REQUESTS ||--o{ FOOD_REQUEST_INTERESTS : aggregates
    CUSTOMER_PROFILES ||--o{ FOOD_REQUEST_INTERESTS : expresses
    FOOD_REQUESTS ||--o{ FOOD_REQUEST_SUBSCRIPTIONS : watched
    CUSTOMER_PROFILES ||--o{ FOOD_REQUEST_SUBSCRIPTIONS : owns
    FOOD_REQUESTS ||--o{ FOOD_REQUEST_RESPONSES : answered
    CHEF_BUSINESSES ||--o{ FOOD_REQUEST_RESPONSES : responds

    OUTBOX_EVENTS {
        uuid id PK
        string aggregate_type
        uuid aggregate_id
        string event_type
        int event_version
        uuid correlation_id NULL
        uuid causation_id NULL
        jsonb payload
        timestamptz occurred_at
        timestamptz published_at NULL
        int attempts
        text last_error NULL
        timestamptz next_attempt_at NULL
        timestamptz created_at
    }

    USERS {
        uuid id PK
        string auth_subject UK
        string status
        timestamptz created_at
        timestamptz updated_at
    }

    ORGANIZATIONS {
        uuid id PK
        string type
        string name
        string status
        timestamptz created_at
        timestamptz updated_at
    }

    ORGANIZATION_MEMBERS {
        uuid id PK
        uuid organization_id FK
        uuid user_id FK
        uuid role_id FK
        string status
        timestamptz created_at
    }

    ROLES {
        uuid id PK
        string code UK
        string name
    }

    LOCATIONS {
        uuid id PK
        uuid organization_id FK
        string name
        string address_line1
        string city
        string province
        string postal_code
        string country_code
        geography point
    }

    KITCHENS {
        uuid id PK
        uuid location_id FK
        string name
        text description
        string status
        string iana_timezone_id "authoritative Kitchen business timezone"
        timestamptz published_at
    }

    KITCHEN_SPACES {
        uuid id PK
        uuid kitchen_id FK
        string name
        text description
        numeric size_value
        string size_unit
        int capacity
        bigint hourly_rate_minor
        string currency_code
        int minimum_booking_minutes
        int maximum_booking_minutes
        int cleaning_minutes
        string status
        int version
    }

    KITCHEN_AVAILABILITIES {
        uuid id PK
        uuid kitchen_id FK
        string day_of_week
        time local_start_time
        time local_end_time
        string recurrence_rule NULL
        bool active
    }

    KITCHEN_SPACE_AVAILABILITIES {
        uuid id PK
        uuid kitchen_space_id FK
        string day_of_week
        time local_start_time
        time local_end_time
        string recurrence_rule NULL
        bool active
    }

    EQUIPMENT_CATALOG_ITEMS {
        uuid id PK
        string category
        string name
        text description
        string image_url
        bool active
    }

    SPACE_EQUIPMENT {
        uuid id PK
        uuid kitchen_space_id FK
        uuid equipment_catalog_item_id FK
        int quantity
        bool included
        bool rental_available
    }

    EQUIPMENT_RENTALS {
        uuid id PK
        uuid kitchen_space_id FK
        uuid equipment_catalog_item_id FK
        bigint hourly_rate_minor
        string currency_code
        int quantity_available
        string status
        timestamptz created_at
        timestamptz updated_at
    }

    KITCHEN_BOOKINGS {
        uuid id PK
        uuid kitchen_space_id FK
        uuid chef_profile_id FK
        timestamptz start_at
        timestamptz cooking_end_at
        timestamptz occupancy_end_at
        tstzrange occupancy_range "generated stored from start_at and occupancy_end_at"
        timestamptz hold_expires_at
        string status
        string cancellation_reason
        int version
    }

    EQUIPMENT_BOOKINGS {
        uuid id PK
        uuid kitchen_booking_id FK
        uuid equipment_rental_id FK
        int quantity
        string status
        timestamptz created_at
        timestamptz updated_at
    }

    EQUIPMENT_ALLOCATIONS {
        uuid id PK
        uuid equipment_rental_id FK
        timestamptz start_at
        timestamptz end_at
        int quantity
        uuid kitchen_booking_id FK
        timestamptz created_at
    }

    CHEF_PROFILES {
        uuid id PK
        uuid user_id FK
        uuid chef_business_id FK
        string display_name
        text biography
        geography service_location
        string status
    }

    CHEF_BUSINESSES {
        uuid id PK
        uuid organization_id FK
        string name
        string status
    }

    MENUS {
        uuid id PK
        uuid chef_business_id FK
        string name
        text description
        string status
    }

    MASTER_FOODS {
        uuid id PK
        uuid cuisine_id FK
        string canonical_name
        text description
        bool active
    }

    FOOD_LISTINGS {
        uuid id PK
        uuid chef_business_id FK
        uuid kitchen_id FK
        uuid master_food_id FK
        string name
        text description
        bigint price_minor
        string currency_code
        int preparation_minutes
        int serving_size
        string status
    }

    FOOD_AVAILABILITY_RULES {
        uuid id PK
        uuid food_listing_id FK
        string day_of_week
        time local_start_time
        time local_end_time
        int cutoff_minutes
        string recurrence_rule
        bool active
    }

    FOOD_AVAILABILITY_OCCURRENCES {
        uuid id PK
        uuid food_listing_id FK
        uuid availability_rule_id FK NULL
        timestamptz start_at
        timestamptz end_at
        int cutoff_minutes
        string status
    }

    CUSTOMER_PROFILES {
        uuid id PK
        uuid user_id FK
        string display_name
        string default_currency_code
    }

    CUSTOMER_ADDRESSES {
        uuid id PK
        uuid customer_profile_id FK
        string label
        string address_line1
        string city
        string province
        string postal_code
        string country_code
        geography point
    }

    CARTS {
        uuid id PK
        uuid customer_profile_id FK
        uuid kitchen_id FK
        string status
        timestamptz expires_at
        int version
    }

    CART_ITEMS {
        uuid id PK
        uuid cart_id FK
        uuid kitchen_id FK
        uuid food_listing_id FK
        int quantity
        jsonb selected_options
        timestamptz added_at
    }

    ORDERS {
        uuid id PK
        uuid customer_profile_id FK
        uuid kitchen_id FK
        uuid cart_id FK
        string fulfillment_type "NOT NULL; PICKUP or DELIVERY"
        string status
        bigint subtotal_minor
        bigint discount_minor
        bigint delivery_fee_minor
        bigint fee_minor
        bigint tax_minor
        bigint total_minor
        string currency_code
        int version
        timestamptz placed_at
        timestamptz completed_at
    }

    CHEF_ORDER_GROUPS {
        uuid id PK
        uuid order_id FK
        string conceptual_actual_chef_performer_ref "PLACEHOLDER; exact typed FK pending ADR-017"
        string status
        bigint subtotal_minor
        bigint discount_minor
        bigint net_minor
        string currency_code
        int version
        uuid latest_pricing_snapshot_id FK
        uuid latest_promotion_snapshot_id FK
    }

    ORDER_ITEMS {
        uuid id PK
        uuid order_id FK
        uuid kitchen_id FK
        uuid chef_order_group_id FK
        uuid food_listing_id FK
        string product_name_snapshot
        bigint unit_price_minor
        int quantity
        bigint gross_minor
        bigint discount_minor
        bigint net_minor
        bigint tax_minor
        string currency_code
        jsonb item_snapshot
    }

    PROMOTIONS {
        uuid id PK
        string conceptual_typed_owner_ref "PLACEHOLDER; not an arbitrary type plus UUID"
        string promotion_scope "NOT NULL"
        string promotion_type "NOT NULL"
        string name "NOT NULL"
        timestamptz valid_from "NOT NULL"
        timestamptz valid_to NULL
        int priority "NOT NULL; default 0"
        string qualifying_basis NULL
        string compatibility_group NULL
        string exclusivity_group NULL
        string status "NOT NULL; default ACTIVE"
        jsonb conditions "NOT NULL; default empty object"
        timestamptz created_at
        timestamptz updated_at
    }

    PROMOTION_RULES {
        uuid id PK
        uuid promotion_id FK
        string rule_type
        string scope
        string qualifying_basis
        int priority
        jsonb parameters
        string status
    }

    PROMOTION_TARGETS {
        uuid id PK
        uuid promotion_id FK "NOT NULL"
        string conceptual_typed_target_ref "PLACEHOLDER; exact typed relationship deferred"
        timestamptz created_at "NOT NULL"
    }

    PROMOTION_SNAPSHOTS {
        uuid id PK
        uuid promotion_id FK
        uuid order_id FK
        uuid chef_order_group_id FK
        int promotion_version
        string scope
        string qualifying_basis
        bigint qualifying_subtotal_minor
        bigint discount_minor
        string applied_status
        string rejection_reason
        jsonb snapshot_evidence
        timestamptz created_at
    }

    PROMOTION_APPLICATION_ITEMS {
        uuid id PK
        uuid promotion_snapshot_id FK
        uuid order_item_id FK
        string allocation_type
        bigint discount_minor
    }

    PRICING_SNAPSHOTS {
        uuid id PK
        uuid order_id FK
        uuid chef_order_group_id FK NULL
        int snapshot_version
        bigint subtotal_minor
        bigint discount_minor
        bigint delivery_fee_minor
        bigint fee_minor
        bigint tax_minor
        bigint total_minor
        string currency_code
        jsonb calculation_evidence
        timestamptz created_at
    }

    PROMO_CODES {
        uuid id PK
        uuid promotion_id FK
        string code_hash UK
        string display_code
        int max_global_uses NULL
        timestamptz valid_from
        timestamptz valid_to
        string status
        timestamptz created_at
        timestamptz updated_at
    }

    PROMO_CODE_REDEMPTIONS {
        uuid id PK
        uuid promo_code_id FK
        uuid customer_id FK
        uuid order_id FK
        string status
        timestamptz reserved_at
        timestamptz redeemed_at NULL
        timestamptz released_at NULL
        timestamptz created_at
        timestamptz updated_at
    }

    PROMOTION_APPLICATIONS {
        uuid id PK
        uuid promotion_id FK
        uuid order_id FK
        uuid chef_order_group_id FK NULL
        uuid order_item_id FK NULL
        uuid promo_code_id FK NULL
        uuid promo_code_redemption_id FK NULL
        bigint discount_minor
        jsonb calculation_snapshot
    }

    PAYMENTS {
        uuid id PK
        uuid order_id FK,UK NULL "FOOD specialization only; exact typed source pending ADR-020"
        string status
        bigint amount_minor
        string currency_code
        timestamptz created_at
        timestamptz updated_at
    }

    PAYMENT_ATTEMPTS {
        uuid id PK
        uuid payment_id FK
        int attempt_sequence
        string provider_name
        string provider_payment_reference NULL
        string provider_status
        bigint amount_minor
        string currency_code
        jsonb provider_evidence
        timestamptz attempted_at
        timestamptz completed_at NULL
    }

    PAYMENT_ALLOCATIONS {
        uuid id PK
        uuid payment_id FK
        uuid order_id FK NULL "FOOD source trace only"
        uuid chef_order_group_id FK NULL
        uuid delivery_id FK NULL
        uuid tax_line_item_id FK NULL
        string allocation_type
        bigint amount_minor
        string currency_code
        jsonb allocation_evidence
        timestamptz created_at
    }

    REFUNDS {
        uuid id PK
        uuid payment_id FK
        uuid order_id FK NULL "FOOD context only; generalized typed source pending ADR-020"
        string reason
        string status
        bigint requested_amount_minor
        bigint approved_amount_minor NULL
        string currency_code
        string provider_name NULL
        string provider_refund_reference NULL
        jsonb provider_metadata
        timestamptz requested_at
        timestamptz processed_at NULL
    }

    REFUND_LINES {
        uuid id PK
        uuid refund_id FK
        uuid payment_allocation_id FK NULL
        uuid order_item_id FK NULL
        uuid chef_order_group_id FK NULL
        string line_type
        bigint amount_minor
        string currency_code
        jsonb refund_evidence
        timestamptz created_at
    }

    FEE_LINE_ITEMS {
        uuid id PK
        uuid order_id FK
        string fee_type
        bigint amount_minor
        string currency_code
        jsonb calculation_snapshot
    }

    TAX_LINE_ITEMS {
        uuid id PK
        uuid order_id FK
        string tax_type
        string jurisdiction
        decimal rate
        bigint amount_minor
        string currency_code
    }

    PAYOUTS {
        uuid id PK
        string conceptual_settlement_beneficiary_ref "PLACEHOLDER; exact typed relationship pending ADR-020"
        string status
        bigint amount_minor
        string currency_code
        string provider_name
        string provider_payout_id NULL
        jsonb provider_metadata
        timestamptz created_at
        timestamptz updated_at
    }

    PAYOUT_LINES {
        uuid id PK
        uuid payout_id FK
        uuid payment_allocation_id FK NULL
        string conceptual_source_obligation_ref "PLACEHOLDER; exact typed relationship pending ADR-020"
        string settlement_context
        uuid order_id FK NULL
        uuid chef_order_group_id FK NULL
        uuid kitchen_booking_id FK NULL
        string line_type
        bigint gross_minor
        bigint fee_minor
        bigint adjustment_minor
        bigint net_minor
        string currency_code
        jsonb calculation_snapshot
    }

    LEDGER_TRANSACTIONS {
        uuid id PK
        string currency_code
        string status "DRAFT or POSTED"
        string posting_type
        string source_type
        uuid source_id
        uuid compensates_ledger_transaction_id FK NULL
        int entry_count
        bigint total_debit_minor
        bigint total_credit_minor
        timestamptz created_at
        timestamptz posted_at NULL
    }

    LEDGER_ENTRIES {
        uuid id PK
        uuid ledger_transaction_id FK
        string account_code
        uuid order_id FK NULL
        uuid chef_order_group_id FK NULL
        uuid payout_id FK NULL
        uuid payout_line_id FK NULL
        uuid payment_id FK NULL
        uuid refund_id FK NULL
        string entry_type
        bigint amount_minor
        string direction
        string source_type
        uuid source_id
        jsonb entry_snapshot
        timestamptz created_at
    }

    DELIVERIES {
        uuid id PK
        uuid order_id FK
        string provider
        string provider_delivery_id
        string status
        bigint quoted_fee_minor
        string currency_code
    }

    DELIVERY_EVENTS {
        uuid id PK
        uuid delivery_id FK
        string external_event_id
        string event_type
        string status
        jsonb payload_metadata
        timestamptz event_at
    }

    FOOD_REQUESTS {
        uuid id PK
        uuid customer_profile_id FK
        uuid master_food_id FK
        string requested_name
        text description
        geography location
        string status
        bool notify_when_available
        timestamptz created_at
    }

    IDEMPOTENCY_KEYS {
        uuid id PK
        string operation_type
        string idempotency_key
        string request_hash
        string result_type NULL
        uuid result_id NULL
        jsonb response_snapshot
        string status
        timestamptz created_at
        timestamptz updated_at
    }

    PROVIDER_EVENTS {
        uuid id PK
        string provider_name
        string provider_event_id
        string aggregate_type
        uuid aggregate_id NULL
        jsonb payload
        timestamptz received_at
        timestamptz processed_at NULL
        string status
    }

```

## Timezone and Availability Semantics

Timezone modeling follows the repository ADR-011 decision while ADR-011 remains Proposed until explicitly accepted. `kitchen.kitchens.iana_timezone_id` stores the Kitchen's authoritative IANA timezone identity, for example `America/Toronto`. Abbreviations and fixed offsets such as `EST`, `EDT`, or `UTC-5` are not sufficient authoritative timezone identities. Kitchen Spaces, bookings, and availability records resolve Kitchen-based business rules through their owning Kitchen rather than duplicating its timezone by default.

PostgreSQL `TIMESTAMPTZ` represents a real instant and does not preserve the original IANA timezone name or textual offset. Concrete booking boundaries, orders, payments, refunds, payouts, deliveries, outbox events, and materialized food-availability occurrence boundaries use `TIMESTAMPTZ` conceptually.

`KITCHEN_AVAILABILITIES`, `KITCHEN_SPACE_AVAILABILITIES`, and `FOOD_AVAILABILITY_RULES` are recurring business-local rules. Their day/recurrence and local time-of-day values are interpreted using the authoritative owning Kitchen's IANA timezone; they are not UTC instants. `FOOD_AVAILABILITY_OCCURRENCES` represents concrete occurrences for specific dates. Its `start_at` and `end_at` are real instants materialized by resolving a rule under the Kitchen timezone, or supplied directly as real instants. A rule and an occurrence are distinct records and must not be treated as one ambiguous representation.

Changing a Kitchen's configured timezone affects future business-local schedule interpretation. It does not rewrite historical or already-materialized booking, order, financial, or availability occurrence instants. Explicit configuration history or effective dating may be introduced only if independently required; this model does not duplicate timezone history by default.

## Booking Concurrency and Equipment Allocation

ADR-007 governs booking concurrency. Concrete booking occupancy values are real instants represented by `TIMESTAMPTZ`; the database-protected occupied interval is a PostgreSQL `tstzrange`. Kitchen business-local scheduling and timezone interpretation remain governed by ADR-011.

### Kitchen Space Occupancy

`kitchen.kitchen_bookings.start_at` is the occupied interval start. `cooking_end_at` is the booked usage end. `occupancy_end_at` includes the mandatory cleaning duration after booked usage, so cleaning is part of the database-protected interval rather than informational metadata.

Conceptually, ADR-007 requires:

```sql
CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE kitchen.kitchen_bookings
    ADD COLUMN occupancy_range tstzrange
    GENERATED ALWAYS AS (
        tstzrange(start_at, occupancy_end_at, '[)')
    ) STORED;

ALTER TABLE kitchen.kitchen_bookings
    ADD CONSTRAINT kitchen_bookings_no_overlap
    EXCLUDE USING gist (
        kitchen_space_id WITH =,
        occupancy_range WITH &&
    )
    WHERE (status IN ('HELD', 'CONFIRMED'));
```

The half-open range permits a subsequent booking to begin exactly when the previous booking's complete occupancy, including cleaning, ends. Only `HELD` and `CONFIRMED` participate in overlap prevention. `CANCELLED` and `COMPLETED` do not block new occupancy. No additional blocking state is implied.

The GiST exclusion constraint is the canonical concurrency protection for Kitchen Space overlap. Application checks may improve error handling but do not replace the database guarantee. ADR-007 does not use advisory locks as the default strategy.

ADR-007 models a temporary hold on the booking row with `status = 'HELD'` and `hold_expires_at`. A held booking participates in the same exclusion constraint as a confirmed booking. Hold expiration must be deterministic; this ERD does not invent behavior beyond ADR-007.

### Equipment Booking and Allocation

`equipment.equipment_catalog_items` contains reusable equipment type/catalog definitions; catalog rows are not finite reservable capacity. `equipment.space_equipment` associates catalog equipment with one Kitchen Space and primarily represents baseline/included equipment. `equipment.equipment_rentals` represents additional rentable inventory offers for one Kitchen Space. Each EquipmentRental owns its catalog reference, price and currency, lifecycle/status, and authoritative finite `quantity_available`.

The current resource scope is per Kitchen Space: `equipment.equipment_rentals.id` is the authoritative reservation and serialization key. The model does not contain a duplicate `MASTER_EQUIPMENT` capacity entity and does not infer a shared Kitchen-wide pool. Equipment shared across Spaces requires a separate approved business and architecture model.

`equipment.equipment_bookings` is the request/line for an EquipmentRental quantity within a Kitchen Booking and references `equipment_rental_id`. A request row consumes capacity only while it has capacity-reserving semantics under ADR-007. Its reserving/non-reserving status enum remains to be defined by the booking lifecycle; this contract does not invent status values.

`equipment.equipment_allocations` is the committed relational capacity-consumption fact for an EquipmentRental and Kitchen Booking. It references `equipment_rental_id`, not a catalog or generic master-equipment identifier. Kitchen Space ownership is derived through the EquipmentRental, so no redundant `kitchen_space_id` is required on EquipmentAllocation.

An EquipmentAllocation's `start_at` and `end_at` are derived/snapshotted from its Kitchen Booking's complete half-open occupancy interval, `[kitchen_booking.start_at, kitchen_booking.occupancy_end_at)`, and must match those boundaries. The interval includes mandatory cleaning; the current model has no independently selected equipment-rental interval.

The equipment capacity invariant is transactional: for one `equipment_rental_id`, the sum of capacity-consuming allocations whose half-open intervals overlap the requested booking occupancy, plus the new requested quantity, must not exceed that EquipmentRental's `quantity_available`. `HELD` and `CONFIRMED` Kitchen Bookings reserve both Space and required equipment capacity. Allocations for expired holds or cancelled bookings cease participating as active capacity according to the booking/allocation lifecycle.

For each reservation transaction, the implementation must:

1. Collect all requested `equipment_rental_id` values and sort them in deterministic identifier order.
2. Acquire PostgreSQL row locks equivalent to `SELECT ... FOR UPDATE` on the corresponding `equipment.equipment_rentals` rows in that order.
3. After all locks are held, validate that each rental is active/reservable and belongs to the Kitchen Space being booked, then read its `quantity_available`.
4. Recalculate overlapping capacity-consuming EquipmentAllocations for each same `equipment_rental_id`.
5. Reject when existing overlapping quantity plus requested quantity exceeds `quantity_available`; otherwise create the allocations.

The overlap `SUM` and capacity check must occur after lock acquisition. A pre-lock availability calculation or quote is informational and is not authoritative for reservation.

One local PostgreSQL transaction covers deterministic EquipmentRental row locking, rental validation, post-lock capacity calculation, validation of all requested rentals, applicable capacity-consuming EquipmentBooking state changes, creation of all EquipmentAllocations, and the KitchenBooking transition to `HELD` or `CONFIRMED` when that transition establishes the reservation. If any requested rental fails validation or capacity, no partial allocation, capacity-consuming EquipmentBooking state, or associated reserving KitchenBooking transition may commit. No distributed transaction or advisory-lock default is introduced.

Normal row-lock waiting is expected serialization behavior. Deterministic ordering minimizes but does not eliminate deadlocks; a bounded internal retry may handle PostgreSQL deadlock errors while request idempotency prevents duplicate side effects. `SERIALIZABLE` with mandatory retry is a technically valid alternative, not the current primary strategy.

All booking and allocation identifiers remain PostgreSQL `UUID` columns. This section defines no database UUID default and introduces no UUIDv7 database function.

## Order Fulfillment Type Constraint

The `order.orders.fulfillment_type` column is required and has the conceptual database constraint:

```sql
CHECK (fulfillment_type IN ('PICKUP', 'DELIVERY'))
```

`fulfillment_type` is immutable after Order creation. No pickup- or delivery-specific columns are introduced by this decision.

Fulfillment-state semantics are unambiguous:

- `PICKED_UP` means completed handoff to the customer or the customer's authorized pickup party.
- `DRIVER_PICKED_UP` means the delivery driver has taken possession of the Order.
- `PICKED_UP` must not be used to represent delivery-driver possession.

## ChefOrderGroup Actual-Performer Identity

The canonical invariant is one `ChefOrderGroup` per one concrete food Order plus one durable actual-Chef performer/operational identity. The `conceptual_actual_chef_performer_ref` label in the Mermaid ERD is an explicit conceptual placeholder, not a selected physical column or string-valued relationship. Exact typed relational representation and the uniqueness key are deferred to ADR-017 and the later full ERD reconciliation. The selected design must provide database-enforceable referential integrity and durable historical performer explainability without deriving identity from current employment or Organization membership.

A Chef-specific business or storefront identity may remain meaningful, but `chef_business_id`, `organization_id`, `commercial_provider_id`, `settlement_beneficiary_id`, or `connected_account_id` cannot by itself define ChefOrderGroup uniqueness when distinct actual Chefs may share that identity. Chef Business is not simultaneously the actual performer, employer Organization, commercial provider, and settlement beneficiary.

For example, ABC Food Group may employ or engage Ravi and Maria, commercially provide their food, and be the approved settlement beneficiary for the relevant marketplace obligations. Order O1 still has separate ChefOrderGroup Ravi and ChefOrderGroup Maria records. Their common Organization/provider/beneficiary identity does not merge the groups and does not require a separate external payout recipient for either performer.

Every `OrderItem` continues to belong to exactly one ChefOrderGroup. Every concrete food Order continues to reference exactly one physical Kitchen, and a ChefOrderGroup cannot establish a different Kitchen from its parent Order. The actual performer, commercial provider, and settlement beneficiary remain distinct where applicable:

```text
SERVICE PERFORMER
!= COMMERCIAL PROVIDER
!= SETTLEMENT BENEFICIARY

CHEFORDERGROUP
!= PAYOUT RECIPIENT
!= CONNECTED ACCOUNT
```

Exact effective-dated Chef/Organization engagement and authorization persistence remains ADR-017 work. This narrow reconciliation does not create a final Employment, Worker, ProfessionalMembership, or ProfessionalOrganizationEngagement schema.

## Promotion Model and Targeting

The canonical promotion tables remain `promotion.promotions` and `promotion.promotion_targets`. Core ownership, calculation scope, priority, compatibility, exclusivity, and targeting relationships remain relational. Flexible and extensible promotion conditions use `promotion.promotions.conditions JSONB`; owner relationships and target lists are not stored in JSONB.

Approved conceptual Promotion owner identities are `CHEF`, `PLATFORM`, `ENTREPRENEUR`, `DIETITIAN`, and `ORGANIZATION`, subject to domain policy and authorization. A Dietitian owner is valid only for approved professional-service Promotions such as consultation offerings; this does not create Dietitian food-sale, Meal Subscription, referral, recommendation, or Chef-purchase commission. An Organization owner identifies an authorized accountable Organization rather than a fake Chef, property owner by default, or authenticated User by implication.

Canonical owner persistence must use typed owner relationships, domain-valid owner identities, database-enforceable referential integrity where practical, and durable historical owner explainability. An unconstrained universal `owner_type` plus arbitrary UUID `owner_id` is not sufficient canonical relational integrity. The `conceptual_typed_owner_ref` label in the Mermaid ERD is not a physical column. Exact owner tables, foreign keys, and enum/check representation are deferred to the later full ERD reconciliation after the relevant ADR set is stable.

The accepted food target meanings remain:

- `FOOD_LISTING` — one concrete food listing.
- `MENU` — one concrete menu.
- `CHEF_BUSINESS` — one concrete Chef-specific business/storefront; it is not redefined as any employer or commercial-provider Organization.
- `CATEGORY` — one concrete food category.
- `CATEGORY_ALL` — the accepted special all-category semantic with no concrete target identity.

The typed, domain-aware conceptual target `COMMERCIAL_PROVIDER_ORGANIZATION` must also be representable where the Promotion domain permits it. It means qualifying commercial offerings supplied through an identified Organization. It requires a real Organization identity, is not equivalent to `CATEGORY_ALL`, is not a fake Chef, and does not silently broaden `CHEF_BUSINESS`.

Canonical target persistence must provide a typed relationship to each concrete domain resource, database-enforceable referential integrity where practical, and domain-aware validation. An unconstrained universal `target_type` plus arbitrary UUID `target_id` is not sufficient. The `conceptual_typed_target_ref` label in the Mermaid ERD is not a physical column. Exact typed relational representation is deferred to the later full ERD reconciliation after ADR-017 through ADR-020 are stable. This deferral does not authorize a universal TargetRegistry, one giant nullable-FK table, or JSONB as the canonical target relationship.

Existing duplicate-target semantics are preserved: a Promotion cannot contain duplicate references to the same concrete typed target. `CATEGORY_ALL` retains its special null-identity behavior and separate uniqueness protection so one Promotion cannot contain duplicate `CATEGORY_ALL` rows. Concrete targets, including `COMMERCIAL_PROVIDER_ORGANIZATION`, require real typed identity and do not use the `CATEGORY_ALL` null case. Exact replacement SQL will be finalized with the typed target layout; this task does not weaken the accepted uniqueness behavior.

These dimensions remain separate and must not be forced equal by the eventual table layout:

```text
PROMOTION OWNER
!= TARGET
!= CALCULATION SCOPE
!= SERVICE PERFORMER
!= COMMERCIAL PROVIDER
!= SETTLEMENT BENEFICIARY
!= FUNDING SOURCE
```

### Promo-Code Redemption

Promo-code redemption is distinct from PromotionApplication and PromotionSnapshot pricing evidence. Customer-entered codes create relational redemption attempts; automatic promotions do not create redemption rows merely because they apply.

The canonical redemption table is conceptually:

```sql
CREATE TABLE promotion.promo_code_redemptions (
    id UUID PRIMARY KEY,
    promo_code_id UUID NOT NULL REFERENCES promotion.promo_codes(id),
    customer_id UUID NOT NULL REFERENCES customer.customer_profiles(id),
    order_id UUID NOT NULL REFERENCES "order".orders(id),
    status VARCHAR(20) NOT NULL,
    reserved_at TIMESTAMPTZ NOT NULL,
    redeemed_at TIMESTAMPTZ NULL,
    released_at TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (status IN ('RESERVED', 'REDEEMED', 'RELEASED'))
);

CREATE UNIQUE INDEX uq_promo_code_redemptions_customer_consuming
    ON promotion.promo_code_redemptions (promo_code_id, customer_id)
    WHERE status IN ('RESERVED', 'REDEEMED');

CREATE UNIQUE INDEX uq_promo_code_redemptions_order_consuming
    ON promotion.promo_code_redemptions (order_id)
    WHERE status IN ('RESERVED', 'REDEEMED');

CREATE INDEX ix_promo_code_redemptions_promo_status
    ON promotion.promo_code_redemptions (promo_code_id, status);

CREATE INDEX ix_promo_code_redemptions_customer
    ON promotion.promo_code_redemptions (customer_id);

CREATE INDEX ix_promo_code_redemptions_status
    ON promotion.promo_code_redemptions (status);
```

The current checkout flow materializes the Order before payment completion, so `order_id` is required from `RESERVED` onward. A future pre-Order reservation flow requires a separate contract change. The valid transitions are `RESERVED → REDEEMED` and `RESERVED → RELEASED`. `RESERVED` and `REDEEMED` consume per-customer and optional global capacity; `RELEASED` is terminal historical evidence that does not consume future capacity. A later attempt after release inserts a new row rather than deleting or overwriting history.

Every customer may successfully redeem a specific promo code at most once. The customer/code partial unique index also prevents a second concurrent active claim. A released row permits a later claim, but a `REDEEMED` row permanently prevents reuse by that customer. There is no `max_uses_per_customer` column or configurable per-customer policy. `promotion.promo_codes.max_global_uses` is optional; `NULL` means no configured global cap and `1` defines a globally one-time code. General `UNIQUE (promo_code_id)` is not used.

The order partial unique index enforces at most one capacity-consuming customer-entered code per Order while allowing historical `RELEASED` attempts followed by another valid attempt. It does not convert the promo code itself into a globally single-use code.

For global-cap concurrency, all reservation paths use the stable `promotion.promo_codes` row. One local PostgreSQL transaction locks that row with `SELECT ... FOR UPDATE` before validating status, validity/expiry, customer eligibility, existing active/successful customer redemption, and the count of `RESERVED` plus `REDEEMED` rows. If `max_global_uses` is non-null and that post-lock count is greater than or equal to the cap, reservation is rejected; otherwise the transaction inserts `RESERVED`. A pre-lock count is not authoritative because concurrent `READ COMMITTED` transactions could otherwise claim the final use. No advisory or distributed lock is introduced.

After authoritative successful payment/checkout completion, an idempotent local transaction changes `RESERVED` to `REDEEMED`. Definitive checkout abandonment/expiry, cancellation, or final payment failure changes it idempotently to `RELEASED`; a retryable provider-attempt failure does not release an active checkout. The provider callback and PostgreSQL update are not one ACID transaction. New reservations after code expiry are prohibited; this model does not invent a separate post-reservation expiry transition.

Full and partial refunds do not change `REDEEMED` to `RELEASED`, restore customer eligibility, or overwrite original evidence. Refund recalculation creates new immutable promotion and financial adjustment evidence.

### Promotion Application References

`promotion.promotion_applications.promotion_id` and `order_id` are required for Order pricing applications. `chef_order_group_id` is nullable for `ORDER` and `DELIVERY` scopes, and `order_item_id` is nullable for non-`ITEM` scopes. `promo_code_id` is nullable because automatic promotions have no entered code. `promo_code_redemption_id` is nullable and links a code-backed application to its customer redemption when applicable. Order-, Delivery-, and Platform-level promotions are not forced into ChefOrderGroup.

PromotionApplication and PromotionSnapshot records remain immutable calculation evidence. PromoCodeRedemption is the separate usage, cardinality, and concurrency fact; neither replaces the other.

Promotion ownership and food-order calculation scope are distinct:

- Approved conceptual ownership domains are `CHEF`, `PLATFORM`, `ENTREPRENEUR`, `DIETITIAN`, and `ORGANIZATION`, subject to domain authorization.
- Food-order calculation scopes are `ITEM`, `CHEF_ORDER_GROUP`, `DELIVERY`, and `ORDER`.
- `PLATFORM` is an ownership domain, not a calculation scope.
- Entrepreneur booking and equipment promotions remain in the booking/rental promotion domain; `ENTREPRENEUR` is not a food-order calculation scope.
- Dietitian consultation ownership and Organization ownership do not make those owner identities food-order calculation scopes.

Chef-owned promotions are evaluated within the relevant `ChefOrderGroup`. Chef A and Chef B are independent promotion domains within the same Order. Item-level and ChefOrderGroup-level promotions may coexist when their monetary scopes do not overlap and compatibility rules allow it.

Promotion compatibility does not use a global `stackable`, `is_stackable`, or `can_stack` boolean. Deterministic selection uses compatibility groups/rules, exclusivity groups, priority, customer savings, and stable tie-breakers. The relational model represents compatibility group, exclusivity group, and priority; extensible eligibility and rule conditions remain in `conditions JSONB` where appropriate.

## Canonical Financial Schema

The canonical financial schema is `financial.*` only. It contains:

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

There is no competing `payment.*` schema and no required `financial.ledger_accounts` table. The uppercase ERD entity names above represent these 11 current foundational `financial.*` tables. They are not a declaration that this set is permanently sufficient; ADR-020 may require additional non-ledger Financial records. PricingSnapshots, PromotionSnapshots, FeeLineItems, TaxLineItems, and Delivery quoted-fee evidence remain with their owning domains.

### Payment and Allocation Model

- `financial.payments` remains the foundational logical collection aggregate for one approved billable commercial context. For the food specialization, one concrete food Order has at most one logical Payment; relational uniqueness equivalent to `UNIQUE (order_id)` applies to that specialization, and food split tender or multiple independent Payments per Order are not modeled.
- Payment is not universally Order-owned. Approved non-Order contexts include KitchenBooking, separately billable EquipmentRental, Dietitian Appointment/consultation, Meal Subscription billing cycle, and Kitchen Subscription billing cycle. These remain their own domain aggregates and must not be converted into fake Orders. The exact Payment cardinality for each non-food context is not inferred from the food rule.
- The `order_id` shown on `PAYMENTS` in the Mermaid ERD is the current food-specialization relationship, not a universal required source column. Exact typed Payment-to-context relationships, foreign keys, and replay-safe cardinalities are deferred to ADR-020 and later canonical ERD reconciliation. This deferral does not approve an unconstrained universal `source_type` plus arbitrary UUID, a universal BusinessSource aggregate, a giant nullable-FK table, or JSONB as canonical source identity.
- Retryable provider interactions are separate `financial.payment_attempts` records, each belonging to exactly one Payment and carrying its own identity and a sequence unique within that Payment. `provider_name` is required when the attempt uses a provider. `provider_payment_reference` may be null before provider creation when the flow requires it; once non-null, a partial unique constraint on `(provider_name, provider_payment_reference)` prevents one provider interaction from mapping ambiguously to multiple attempts.
- The provider-neutral PaymentGateway initiation result is `PaymentInitiationResult`, not a provider SDK PaymentIntent object. Provider names, generic payment references, statuses, and metadata are integration evidence. Stripe Connect may be an adapter, but Stripe is not the financial domain model.
- Merchant-of-Record, tax-remittance, legal, chargeback, and refund-liability responsibilities remain unresolved and are not asserted by this ERD.
- A successful Payment can have multiple internal `financial.payment_allocations`. PaymentAllocation is the payment-side logical distribution/reference for approved shares and may preserve typed source/economic traceability across approved billable contexts. It is not universally a Food Order plus Chef Business recipient relationship. Exact allocation categories and typed generalized source/provider relationships remain ADR-020 work rather than being finalized here.
- Every allocation references its Payment and stores its approved allocation type, positive integer minor-unit amount, and currency. Food allocations may reference Order, ChefOrderGroup, Delivery, TaxLineItem, and other typed evidence where applicable. Non-food contexts use their applicable future typed relationships rather than a fake Order or ChefOrderGroup. Type-specific database constraints must enforce required references after the final relational design is approved; canonical polymorphic JSONB relationships remain prohibited.
- `ChefOrderGroup` is an actual-performer source and traceability reference where applicable. It does not by itself identify the commercial provider, settlement beneficiary, connected-account holder, or external payout recipient. Multiple ChefOrderGroups may contribute source/economic evidence to one Organization commercial obligation, and one PaymentAllocation recipient per performer is not required.
- PaymentAllocation is not earning recognition, payout eligibility, Payout, PayoutLine, provider transfer/external settlement, or LedgerEntry. The Financial domain owns Payment and PaymentAllocation aggregates.
- Currency must match across the Payment and each PaymentAllocation; cross-table enforcement is required in the transaction or by an explicitly designed database mechanism.

### Refund Model

- `financial.refunds` and `financial.refund_lines` create new append-only financial facts; original payments and allocations are not rewritten.
- Refund requires its Payment and applicable typed billable-context evidence; it is not universally Order-owned. `requested_amount_minor` is known at request time; `approved_amount_minor` remains nullable until provider approval/confirmation determines it. `provider_refund_reference` is generic and nullable until the provider creates one.
- A food Refund may reference Order, ChefOrderGroup, OrderItem, PaymentAllocation, and immutable Pricing/Promotion evidence as applicable. Kitchen Booking, Dietitian Appointment, Meal Subscription billing, and Kitchen Subscription billing may produce Refund facts without fake Orders. Exact typed generalized Refund source foreign keys and cardinalities are deferred to ADR-020 and later canonical ERD reconciliation.
- Refund lines may reference the original PaymentAllocation, OrderItem, and `chef_order_group_id` where applicable, enabling allocation-, item-, group-, and order-level partial refunds. Their currency must match the parent Refund and original financial flow.
- Original Payment, PaymentAllocation, PricingSnapshot, PromotionSnapshot, and posted ledger history remain immutable evidence. Provider refund retries/interactions remain generic provider workflow evidence rather than a provider-specific aggregate. Refund processing may create new compensating ledger postings.

### Payout Model

- The supported payout lifecycle is `PENDING → ELIGIBLE → PROCESSING → SUCCESS`, with `FAILED` and `ON_HOLD` where applicable.
- `financial.payouts` and `financial.payout_lines` remain Financial-owned. PayoutLine must ultimately reference an approved relational source commercial obligation and prevent duplicate settlement in the applicable context, but PaymentAllocation does not itself establish earning recognition or payout eligibility. The `conceptual_source_obligation_ref` and `conceptual_settlement_beneficiary_ref` labels in the Mermaid ERD are placeholders, not physical string columns.
- Exact commercial-obligation, earning-recognition, generalized financial-source, settlement-beneficiary, payout-eligibility, grouping, and duplicate-settlement relationships are deferred to ADR-020 and later canonical ERD reconciliation. A Payout is not required per Chef, ChefOrderGroup, Order, Appointment, Booking, subscription billing cycle, or fulfillment occurrence; one Payout may aggregate multiple eligible obligations when the future approved model permits it.
- `ChefOrderGroup` may be a food source/traceability reference on applicable financial evidence but does not own a Payout and is not the Payout recipient. An actual Chef performer is not required to hold a connected account or receive a direct external marketplace Payout. Multiple ChefOrderGroups may contribute to one approved Organization commercial-provider/settlement-beneficiary obligation.
- Payout creation records an approved payout-execution/lifecycle fact, not the source commercial obligation, earning-recognition decision, payout-eligibility calculation, or proof that provider transfer completed. Execution is provider-neutral and supports provider-assisted or automated execution through an adapter; no manual-only Chef payout workflow is assumed.

```text
MARKETPLACE SETTLEMENT
!= EMPLOYEE / CONTRACTOR PAYROLL
```

Marketplace Payout persistence does not model wages, salary, worker bonuses, payroll deductions, withholding, payroll tax, or other employee/contractor compensation. If ABC Food Group employs Ravi, Ravi's wages are not represented as a Chef marketplace Payout redirected to ABC.

For Kitchen commerce, the approved commercial operator/settlement beneficiary need not be the Kitchen property owner. Exact operating-right persistence remains future architecture/ERD work; this ERD introduces no lease accounting, landlord billing, or property-management schema.

This narrow reconciliation does not add the future professional, Appointment, subscription, occurrence, entitlement, commercial-obligation, earning-recognition, or payable-source tables. Exact professional identity and effective-dated Organization authorization remain ADR-017 work; Dietitian Appointment/consultation persistence remains ADR-018 work; and subscription persistence remains ADR-019 work. The later subscription model must preserve `ChefMealPlan`, `MealSubscriptionOffer`, `MealSubscription`, and `MealFulfillmentOccurrence` separately from `KitchenSubscriptionOffer`, `ChefKitchenSubscription`, `KitchenEntitlementCycle`, and `KitchenBooking`; no universal Subscription table is introduced here. Exact generalized Financial relationships remain ADR-020 work.

### Idempotency and Provider Events

- `financial.idempotency_keys` stores operation identity, idempotency key, request hash, result reference/response snapshot, status, and timestamps.
- Reusing the same operation/key with a different request hash must be rejected. A key-only comparison is insufficient. Cheffy idempotency remains independent of provider idempotency keys.
- `financial.provider_events` persists immutable inbound provider webhook/event evidence. The canonical deduplication constraint is `UNIQUE (provider_name, provider_event_id)` and must be database enforced rather than dependent on application memory. Duplicate callbacks cannot duplicate aggregate transitions, allocations, refunds, payouts, ledger postings, or outbox events.

Conceptually:

```sql
ALTER TABLE financial.idempotency_keys
    ADD CONSTRAINT uq_financial_idempotency_operation_key
    UNIQUE (operation_type, idempotency_key);

ALTER TABLE financial.provider_events
    ADD CONSTRAINT uq_financial_provider_event
    UNIQUE (provider_name, provider_event_id);
```

### Balanced Ledger

`financial.ledger_transactions` is the canonical posting/finalization header and aggregate root for `financial.ledger_entries`. The required `ledger_transaction_id` FK replaces an unparented transaction UUID grouping. One header owns exactly one `currency_code`; entries cannot select or mix currencies independently.

A LedgerTransaction includes source type/identity, optional `compensates_ledger_transaction_id`, status, timestamps, and database-validated `entry_count`, `total_debit_minor`, and `total_credit_minor`. The only normal transition is `DRAFT → POSTED`; POSTED is terminal.

Ledger `source_type`/`source_id` values are controlled posting and correlation metadata under ADR-015; they do not establish an unconstrained universal business-source relationship or replace the typed commercial-obligation/source relationships deferred to ADR-020. No universal BusinessSource aggregate is introduced.

Each LedgerEntry includes required controlled `account_code`, `entry_type`, positive `amount_minor`, `direction`, relational source references, immutable `entry_snapshot`, and `created_at`.

```text
direction = DEBIT | CREDIT
amount_minor > 0
```

Conceptually, each ledger row enforces `CHECK (direction IN ('DEBIT', 'CREDIT'))` and `CHECK (amount_minor > 0)`.

Before POSTED, PostgreSQL database-controlled finalization must calculate persisted entry evidence, require meaningful debit and credit sides, and enforce:

```text
TOTAL DEBITS = TOTAL CREDITS
```

A normal row CHECK cannot enforce this cross-row aggregate. Trigger-equivalent database enforcement must protect finalization independently of application validation and serialize finalization against child mutation.

After POSTED, no LedgerEntry may be inserted, updated, or deleted for that transaction; the header cannot be history-rewritten, returned to DRAFT, or deleted. Corrections use a new balanced transaction linked by `compensates_ledger_transaction_id` rather than mutation of finalized history.

`account_code` is required and controlled by the Financial domain. Application/domain logic and database persistence reject unknown or unauthorized codes; historical posted codes retain stable meanings. Exact database-backed validation representation is deferred to migration/domain design. No ledger-accounts table or full chart-of-accounts subsystem is required.

One local PostgreSQL transaction atomically contains the authoritative financial state change, DRAFT LedgerTransaction header, all LedgerEntries, database balance/finalization, transition to POSTED, and transactional-outbox record for `LedgerTransactionPosted.v1`. If posting fails, none commit. External provider calls remain outside this transaction.

### Pricing, Fee, Tax, and Delivery Evidence

- `pricing.pricing_snapshots` is the sole canonical immutable commercial pricing/calculation snapshot authority; the currently depicted relationship is the food Order and optional ChefOrderGroup slice. `latest_pricing_snapshot_id` is only a convenience pointer. There is no separate FinancialSnapshot persistence concept. Later typed persistence for approved non-Order commercial contexts must extend Pricing-owned evidence rather than duplicate it in Financial.
- FeeLineItems are Pricing-owned calculation evidence. TaxLineItems are provider-neutral Tax/Pricing calculation evidence. Neither is a `financial.*` settlement table, and historical values are not reconstructed from current configuration.
- Financial payment-side shares may be represented by PaymentAllocations, finalized accounting facts by ledger postings, and approved payout execution by PayoutLines where applicable. PaymentAllocation does not itself establish the future commercial-obligation, earning-recognition, or payout-eligibility facts deferred to ADR-020.
- `delivery.deliveries.quoted_fee_minor` is quoted/captured commercial delivery-pricing evidence, not settlement truth. Applicable payment-side allocation, commercial-obligation, ledger, and settlement facts remain distinct.
- Tax provider records are adapter evidence, not the Tax domain model or Cheffy financial system of record.

### Money, Evidence, UUID, and Time Rules

- Monetary amounts use integer minor-unit `BIGINT` fields plus explicit currency codes. Floating-point money is prohibited.
- Currency consistency is required among Payment, monetary PaymentAttempt fields, PaymentAllocation, Refund, RefundLine, Payout, PayoutLine, LedgerTransaction, and its LedgerEntries where they belong to one financial flow.
- Core payments, attempts, allocations, refunds, refund lines, payouts, payout lines, ledger transactions, ledger entries, source references, ChefOrderGroup references, amounts, and currencies remain relational.
- JSONB is limited to provider payloads/metadata, calculation snapshots, response snapshots, and extensible evidence. It does not replace core financial facts.
- Promotion and pricing calculation snapshots are immutable historical evidence. Any latest-snapshot pointer is a convenience only and is never the source of historical truth.
- Identifiers use PostgreSQL `UUID` types without specifying a database UUID-generation function here.
- Real payment, provider, refund, payout, ledger, and idempotency timestamps use `TIMESTAMPTZ`.

### Provider, Legal, and Reconciliation Boundary

Merchant-of-Record, tax/remittance responsibility, chargeback/refund liability, connected-account topology, reserves, negative balances, and country-specific settlement/risk rules remain unresolved legal/accounting/provider gates. Foundational persistence does not encode an answer.

Cheffy financial records and posted ledger transactions are canonical internal financial truth. Provider events and reports are external reconciliation evidence. A mismatch creates auditable reconciliation evidence/investigation and, where correction is required, new compensating financial and ledger records; it never rewrites original posted history.

## Canonical Transactional Outbox

The single canonical transactional outbox table is `outbox.outbox_events`. It has no competing outbox table or alternate schema.

```sql
CREATE SCHEMA IF NOT EXISTS outbox;

CREATE TABLE outbox.outbox_events (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(200) NOT NULL,
    event_version INT NOT NULL DEFAULT 1,
    correlation_id UUID NULL,
    causation_id UUID NULL,
    payload JSONB NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    published_at TIMESTAMPTZ NULL,
    attempts INT NOT NULL DEFAULT 0,
    last_error TEXT NULL,
    next_attempt_at TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_outbox_unpublished
    ON outbox.outbox_events (published_at, next_attempt_at)
    WHERE published_at IS NULL;

CREATE INDEX idx_outbox_aggregate
    ON outbox.outbox_events (aggregate_type, aggregate_id);

CREATE INDEX idx_outbox_correlation
    ON outbox.outbox_events (correlation_id)
    WHERE correlation_id IS NOT NULL;
```

`published_at IS NULL` identifies events not yet published. `attempts`, `last_error`, and `next_attempt_at` support retry scheduling and diagnostics. No separate status field is defined by the canonical outbox decision.

`event_type` and `event_version` are separate fields. Versioned event types include values such as `OrderCreated.v1` and `PaymentSucceeded.v1`; the suffix and numeric version must agree. `schemaVersion` is not a replacement for `event_version`.

All outbox identifiers use PostgreSQL `UUID` columns without defining a database UUID-generation function here. All event occurrence, publication, retry-scheduling, and creation instants use `TIMESTAMPTZ`.

---

**Canonical ownership:** This document governs the persistence and relational representation. [`02-detailed-architecture.md`](02-detailed-architecture.md) provides the integrated architecture context and may summarize this model without overriding it.
