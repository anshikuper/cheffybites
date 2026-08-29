# ADR-006 — Promotion Targeting Model

## Status

Accepted

**Supersedes:** The legacy embedded architecture decision "Chef-Level Promotion Isolation".

## Amendment History

This Accepted ADR was correctively amended to separate promotion ownership from monetary calculation scope. The original wording incorrectly listed `PLATFORM` as an evaluation/calculation scope. `PLATFORM` is a promotion ownership domain; the current food-order calculation scopes are `ITEM`, `CHEF_ORDER_GROUP`, `DELIVERY`, and `ORDER`. This correction does not change the accepted relational targeting design, target types, target relationships, or uniqueness rules.

## Context

The promotion system needs to support various targeting strategies:
- Chef targeting specific food listings (not just entire menus)
- Chef targeting their entire business (all current and future listings)
- Platform targeting specific categories or all categories
- Platform targeting specific chefs or businesses

The current API contract in `04-api-contracts.md` only shows `MENU` targeting in examples, and the ERD lacks a `PROMOTION_TARGETS` table definition.

The current model also lacks explicit promotion scope, qualifying basis, compatibility, exclusivity, and snapshot evidence for deterministic pricing and refund recalculation.

## Decision

We will create a `PROMOTION_TARGETS` table to explicitly define what each promotion applies to, with the following structure:

### Promotion Targets Table

```sql
CREATE TABLE promotion.promotion_targets (
    id UUID PRIMARY KEY,
    promotion_id UUID NOT NULL REFERENCES promotion.promotions(id) ON DELETE CASCADE,
    target_type VARCHAR(20) NOT NULL CHECK (target_type IN ('FOOD_LISTING', 'MENU', 'CHEF_BUSINESS', 'CATEGORY', 'CATEGORY_ALL')),
    target_id UUID NULL, -- NULL for CATEGORY_ALL, otherwise references the specific entity
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(promotion_id, target_type, target_id), -- Prevent duplicate ordinary targeting rows
    CHECK (
        (target_type = 'CATEGORY_ALL' AND target_id IS NULL)
        OR (target_type <> 'CATEGORY_ALL' AND target_id IS NOT NULL)
    )
);

CREATE UNIQUE INDEX uq_promotion_targets_category_all
    ON promotion.promotion_targets (promotion_id, target_type)
    WHERE target_type = 'CATEGORY_ALL';
```

The application supplies `id` through the repository-approved identifier-generation layer. The persisted database type remains `UUID`; this schema does not prescribe a database-generated random UUID default.

Because PostgreSQL treats `NULL` values as distinct in a normal `UNIQUE` constraint, `UNIQUE(promotion_id, target_type, target_id)` alone does not prevent multiple `CATEGORY_ALL` rows whose `target_id` is `NULL`. The partial unique index ensures that each promotion has at most one `CATEGORY_ALL` targeting row. This model does not use a sentinel UUID and does not require `NULLS NOT DISTINCT`.

### Target Type Semantics

1. **FOOD_LISTING** - Promotion applies to a specific food listing (chef's specific offering)
   - `target_id` references `food.food_listings.id`

2. **MENU** - Promotion applies to an entire menu (all food listings in that menu)
   - `target_id` references `food.menus.id`

3. **CHEF_BUSINESS** - Promotion applies to all current and future listings from a chef's business
   - `target_id` references `chef.chef_businesses.id`

4. **CATEGORY** - Promotion applies to a specific food category (e.g., "vegan", "desserts")
   - `target_id` references `food.categories.id`

5. **CATEGORY_ALL** - Promotion applies to all food in a category across all chefs
   - `target_id` is NULL (applies to entire category globally)

### Ownership Rules

Promotion ownership and calculation scope are separate concepts. `owner_type` identifies the domain that owns the promotion:

- `CHEF`
- `PLATFORM`
- `ENTREPRENEUR`

Neither `CHEF`, `PLATFORM`, nor `ENTREPRENEUR` is a food-order calculation scope.

For **Chef promotions** (promotions created by chefs):
- The `promotion.owner_id` must equal the `chef_business_id` of the target
- Engine must reject Chef promotions where `owner_id` ≠ target's `chef_business_id`
- This prevents chefs from targeting other chefs' food listings or businesses
- A `CHEF`-owned promotion is evaluated only against that Chef's applicable items and ChefOrderGroup boundaries. This isolation does not prevent compatible promotions belonging to different ChefOrderGroups from coexisting.

For **Platform promotions** (promotions created by the platform/admin):
- No ownership restrictions apply
- Can target any combination of entities
- A `PLATFORM`-owned promotion uses a separately selected supported food-order calculation scope according to its definition. For example, `owner_type = PLATFORM` with `calculation_scope = ORDER` is valid; `calculation_scope = PLATFORM` is invalid.

For **Entrepreneur promotions**:
- `ENTREPRENEUR` is an ownership domain, not a food-order calculation scope.
- Kitchen booking and equipment promotions remain in the booking/rental promotion domain unless a future approved architecture decision integrates them into food-order promotion evaluation.

### Calculation Scope and Qualifying Basis

Food-order promotion evaluation must use a `calculation_scope` distinct from `owner_type`. Supported food-order calculation scopes are:

- `ITEM`
- `CHEF_ORDER_GROUP`
- `DELIVERY`
- `ORDER`

A promotion owned by `PLATFORM` may calculate over any applicable supported scope, including `ITEM`, `CHEF_ORDER_GROUP`, `DELIVERY`, or `ORDER`, depending on its approved definition. A promotion owned by `CHEF` remains constrained to that Chef's applicable item or ChefOrderGroup boundary. Ownership values must never be stored or interpreted as calculation-scope values.

Qualifying basis values must include:

- `ALL_ELIGIBLE_ITEMS`
- `NON_DISCOUNTED_ELIGIBLE_ITEMS`
- `SPECIFIC_TARGET_ITEMS`
- `GROUP_SUBTOTAL`
- `DELIVERY_FEE`

### Compatibility and exclusivity

The architecture must not use a single blanket `stackable` flag to resolve all conflicts.
Promotion resolution must be deterministic and use scope, compatibility, exclusivity, priority, savings, and stable tie-breakers.

### Snapshots

Promotion application results must be snapshotted with promotion ID, promotion version, scope, qualifying basis, eligible item IDs, excluded item IDs, qualifying subtotal, discount amount, and rejection reason where applicable.

### Evaluation Logic

When evaluating a promotion for a specific food listing in an order:
1. Find all promotions where the food listing matches a target
2. For FOOD_LISTING targets: direct match on food listing ID
3. For MENU targets: food listing must be in the referenced menu
4. For CHEF_BUSINESS targets: food listing's chef business must match the target
5. For CATEGORY targets: food listing's category must match the target
6. For CATEGORY_ALL targets: food listing must be in the target category (global)

### Examples

#### Chef targeting a specific food listing:
```json
{
  "name": "20% off Spicy Taco Tuesday",
  "owner_id": "chef-business-uuid",
  "promotion_type": "CHEF",
  "discount_type": "PERCENTAGE",
  "discount_value": 20,
  "targets": [
    {
      "type": "FOOD_LISTING",
      "id": "food-listing-uuid-for-spicy-tacos"
    }
  ]
}
```

#### Chef targeting their entire business:
```json
{
  "name": "10% off everything from Chef Maria",
  "owner_id": "chef-business-uuid",
  "promotion_type": "CHEF",
  "discount_type": "PERCENTAGE",
  "discount_value": 10,
  "targets": [
    {
      "type": "CHEF_BUSINESS",
      "id": "chef-business-uuid"
    }
  ]
}
```

#### Platform targeting a category globally:
```json
{
  "name": "Free delivery on all vegan food",
  "owner_id": "platform",
  "promotion_type": "PLATFORM",
  "discount_type": "FIXED_AMOUNT",
  "discount_value": 500, // $5.00 in minor units
  "targets": [
    {
      "type": "CATEGORY",
      "id": "vegan-category-uuid"
    }
  ]
}
```

#### Platform targeting all desserts:
```json
{
  "name": "Happy Hour: 50% off desserts",
  "owner_id": "platform",
  "promotion_type": "PLATFORM",
  "discount_type": "PERCENTAGE",
  "discount_value": 50,
  "targets": [
    {
      "type": "CATEGORY_ALL",
      "id": null
    }
  ]
}
```

## Consequences

### Positive
- Explicit targeting model removes ambiguity
- Supports fine-grained and broad targeting strategies
- Clear ownership rules prevent cross-chef targeting violations
- Extensible for future target types
- Database-level constraints prevent invalid data

### Negative
- Additional table join required for promotion evaluation
- More complex API contract for promotion creation
- Need to migrate existing promotion data (if any)

## Alternatives Considered

1. **JSONB targets field** — Store targets as JSONB in promotions table
   - Rejected: Harder to query, no foreign key constraints, difficult to index

2. **Separate tables per target type** — food_listing_promotions, menu_promotions, etc.
   - Rejected: Proliferation of tables, complex querying across types

3. **String-based target references** — Use strings like "food_listing:uuid" 
   - Rejected: Loss of referential integrity, harder to maintain

## Implementation Notes

- Add `promotion_targets` table to ERD in `03-database-erd.md`
- Update `04-api-contracts.md` examples to show multiple target types
- Add ownership validation rules to `02-detailed-architecture.md` §16
- Create Flyway migration for the new table
- Update promotion evaluation service to join with promotion_targets
- Add API validation for target ownership rules
