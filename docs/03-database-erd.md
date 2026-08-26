# Cheffy Bites — Database ERD & Data Model

> **Source of Truth:** This document is **subsidiary** to [`02-detailed-architecture.md`](docs/02-detailed-architecture.md) Section 12.  
> All schema changes must be made in `02-detailed-architecture.md` first.  
> This document exists for convenient reference and will be regenerated from `02` during CI.

---

## Database ERD

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
    FOOD_LISTINGS ||--o{ FOOD_AVAILABILITIES : schedules

    USERS ||--o| CUSTOMER_PROFILES : has
    CUSTOMER_PROFILES ||--o{ CUSTOMER_ADDRESSES : has

    CUSTOMER_PROFILES ||--o{ CARTS : owns
    CARTS ||--o{ CART_ITEMS : contains
    FOOD_LISTINGS ||--o{ CART_ITEMS : added
    KITCHENS ||--o{ CARTS : scopes

    CARTS ||--o| ORDERS : converts_to
    KITCHENS ||--o{ ORDERS : fulfills
    ORDERS ||--o{ CHEF_ORDER_GROUPS : contains
    CHEF_BUSINESSES ||--o{ CHEF_ORDER_GROUPS : fulfills
    CHEF_ORDER_GROUPS ||--o{ ORDER_ITEMS : contains
    ORDERS ||--o{ ORDER_ITEMS : contains
    FOOD_LISTINGS ||--o{ ORDER_ITEMS : purchased
    ORDERS ||--o{ ORDER_STATUS_HISTORY : changes

    PROMOTIONS ||--o{ PROMOTION_RULES : has
    PROMOTIONS ||--o{ PROMOTION_TARGETS : targets
    PROMOTIONS ||--o{ PROMO_CODES : exposes
    PROMOTIONS ||--o{ PROMOTION_USAGE : used
    ORDERS ||--o{ PROMOTION_APPLICATIONS : receives
    CHEF_ORDER_GROUPS ||--o{ PROMOTION_APPLICATIONS : scopes
    PROMOTIONS ||--o{ PROMOTION_APPLICATIONS : applied
    PROMOTIONS ||--o{ PROMOTION_SNAPSHOTS : snapshots
    PROMOTION_SNAPSHOTS ||--o{ PROMOTION_APPLICATION_ITEMS : allocates

    ORDERS ||--o{ PRICING_SNAPSHOTS : priced
    ORDERS ||--o{ FINANCIAL_SNAPSHOTS : captures
    CHEF_ORDER_GROUPS ||--o{ FINANCIAL_SNAPSHOTS : captures
    ORDERS ||--o{ PAYMENTS : paid_by
    PAYMENTS ||--o{ PAYMENT_ATTEMPTS : attempts
    PAYMENTS ||--o{ PAYMENT_TRANSACTIONS : transactions
    PAYMENTS ||--o{ PAYMENT_ALLOCATIONS : allocates
    ORDERS ||--o{ REFUNDS : refunded
    REFUNDS ||--o{ REFUND_TRANSACTIONS : transactions
    REFUNDS ||--o{ REFUND_LINES : lines

    ORDERS ||--o{ FEE_LINE_ITEMS : charged
    ORDERS ||--o{ TAX_LINE_ITEMS : taxed
    ORDERS ||--o{ LEDGER_ENTRIES : recorded

    PAYOUTS ||--o{ PAYOUT_LINES : contains
    PAYOUT_LINES ||--o{ LEDGER_ENTRIES : settles

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
        string timezone
    }

    KITCHENS {
        uuid id PK
        uuid location_id FK
        string name
        text description
        string status
        string timezone
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
    }

    KITCHEN_BOOKINGS {
        uuid id PK
        uuid kitchen_space_id FK
        uuid chef_profile_id FK
        timestamptz start_at
        timestamptz cooking_end_at
        timestamptz occupancy_end_at
        timestamptz hold_expires_at
        string timezone
        string status
        string cancellation_reason
        int version
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

    FOOD_AVAILABILITIES {
        uuid id PK
        uuid food_listing_id FK
        timestamptz start_at
        timestamptz end_at
        int cutoff_minutes
        string recurrence_rule
        bool active
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
        string fulfillment_type
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
        uuid chef_business_id FK
        string status
        bigint subtotal_minor
        bigint discount_minor
        bigint net_minor
        string currency_code
        int version
        uuid latest_financial_snapshot_id FK
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
        string owner_type
        uuid owner_id
        string promotion_scope
        string promotion_type
        string name
        timestamptz valid_from
        timestamptz valid_to
        int priority
        string qualifying_basis
        string compatibility_group
        string exclusivity_group
        string status
        jsonb conditions
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
        uuid promotion_id FK
        string target_type
        uuid target_id
        string status
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

    FINANCIAL_SNAPSHOTS {
        uuid id PK
        uuid order_id FK
        uuid chef_order_group_id FK NULL
        int snapshot_version
        string snapshot_type
        jsonb snapshot_evidence
        timestamptz created_at
    }

    PROMO_CODES {
        uuid id PK
        uuid promotion_id FK
        string code_hash UK
        string display_code
        int max_global_uses
        int max_uses_per_customer
        timestamptz valid_from
        timestamptz valid_to
        string status
    }

    PROMOTION_APPLICATIONS {
        uuid id PK
        uuid promotion_id FK
        uuid order_id FK
        uuid chef_order_group_id FK
        uuid promo_code_id FK
        bigint discount_minor
        jsonb calculation_snapshot
    }

    PAYMENTS {
        uuid id PK
        uuid order_id FK
        string provider
        string provider_payment_intent_id UK
        string idempotency_key
        string status
        bigint amount_minor
        string currency_code
        jsonb provider_metadata
    }

    PAYMENT_ATTEMPTS {
        uuid id PK
        uuid payment_id FK
        string provider_attempt_id
        string status
        bigint amount_minor
        string currency_code
        jsonb provider_payload
        timestamptz attempted_at
    }

    PAYMENT_ALLOCATIONS {
        uuid id PK
        uuid payment_id FK
        uuid order_id FK
        uuid chef_order_group_id FK
        string allocation_type
        bigint amount_minor
        string currency_code
        jsonb allocation_evidence
    }

    REFUNDS {
        uuid id PK
        uuid payment_id FK
        uuid order_id FK
        string idempotency_key
        string reason
        string status
        bigint requested_minor
        bigint approved_minor
        string currency_code
        jsonb provider_metadata
    }

    REFUND_LINES {
        uuid id PK
        uuid refund_id FK
        uuid order_item_id FK NULL
        uuid chef_order_group_id FK NULL
        string line_type
        bigint amount_minor
        string currency_code
        jsonb refund_evidence
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
        string recipient_type
        uuid recipient_id
        string idempotency_key
        string status
        bigint amount_minor
        string currency_code
        string provider_reference
        jsonb provider_metadata
        timestamptz created_at
    }

    PAYOUT_LINES {
        uuid id PK
        uuid payout_id FK
        uuid order_id FK
        uuid chef_order_group_id FK
        uuid kitchen_booking_id FK NULL
        string line_type
        bigint gross_minor
        bigint fee_minor
        bigint adjustment_minor
        bigint net_minor
        string currency_code
        jsonb calculation_snapshot
    }

    LEDGER_ENTRIES {
        uuid id PK
        uuid order_id FK NULL
        uuid chef_order_group_id FK NULL
        uuid payout_id FK NULL
        uuid payout_line_id FK NULL
        uuid payment_id FK NULL
        uuid refund_id FK NULL
        string entry_type
        string entry_scope
        bigint amount_minor
        string currency_code
        string direction
        jsonb entry_snapshot
        timestamptz created_at
    }

    DELIVERIES {
        uuid id PK
        uuid order_id FK
        string provider
        string provider_delivery_id
        string status
        bigint fee_minor
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
        string idempotency_key UK
        uuid actor_user_id FK
        string request_hash
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
        uuid aggregate_id
        jsonb payload
        timestamptz received_at
        timestamptz processed_at
        string status
    }

    BOOKING_HOLDS {
        uuid id PK
        uuid kitchen_space_id FK
        uuid chef_profile_id FK
        timestamptz hold_expires_at
        string status
        jsonb hold_evidence
        timestamptz created_at
    }
```

---

**Authoritative source:** [`02-detailed-architecture.md`](docs/02-detailed-architecture.md) — Section 12 (Database Strategy) and Section 13 (Detailed Database ERD).
