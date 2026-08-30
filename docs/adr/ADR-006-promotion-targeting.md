# ADR-006 — Promotion Targeting Model

## Status

Accepted

**Supersedes:** The legacy embedded architecture decision "Chef-Level Promotion Isolation".

## Amendment History

This Accepted ADR was correctively amended to separate promotion ownership from monetary calculation scope. The original wording incorrectly listed `PLATFORM` as an evaluation/calculation scope. `PLATFORM` is a promotion ownership domain; the current food-order calculation scopes are `ITEM`, `CHEF_ORDER_GROUP`, `DELIVERY`, and `ORDER`. This correction does not change the accepted relational targeting design, target types, target relationships, or uniqueness rules.

This Accepted ADR was subsequently amended additively to extend Promotion targeting across newly approved commercial domains. The extension preserves the original food-targeting decision and adds a typed target-family and referential-integrity principle for additional domains. It does not supersede the original food target semantics, transfer Promotion-engine behavior from ADR-014, or change this ADR's Accepted status.

This Accepted ADR was subsequently amended additively to support Organization-operated marketplace supply. The amendment adds `ORGANIZATION` as a typed Promotion-owner identity and establishes a domain-aware commercial-provider Organization target semantic without redefining `CHEF_BUSINESS`, weakening relational integrity, changing existing food-target constraints, or moving evaluation or financial behavior into ADR-006.

## Context

The promotion system needs to support various targeting strategies:
- Chef targeting specific food listings (not just entire menus)
- Chef targeting their entire business (all current and future listings)
- Platform targeting specific categories or all categories
- Platform targeting specific chefs or businesses

The current API contract in `04-api-contracts.md` only shows `MENU` targeting in examples, and the ERD lacks a `PROMOTION_TARGETS` table definition.

The current model also lacks explicit promotion scope, qualifying basis, compatibility, exclusivity, and snapshot evidence for deterministic pricing and refund recalculation.

## Decision

We will create a `PROMOTION_TARGETS` table to explicitly define what each food-domain promotion applies to, with the following structure:

### Existing Accepted Food Promotion Targets Table

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

The generic name `promotion_targets` in this original accepted decision denotes the accepted food-target-family relationship. This amendment does not silently redefine its historical target identity, nullable-target, uniqueness, or lifecycle semantics as a universal cross-domain polymorphic target table.

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
- `DIETITIAN`
- `ORGANIZATION`

Neither `CHEF`, `PLATFORM`, `ENTREPRENEUR`, `DIETITIAN`, nor `ORGANIZATION` is a food-order calculation scope.

For **Chef promotions** (promotions created by chefs):
- The `promotion.owner_id` must equal the `chef_business_id` of the target
- Engine must reject Chef promotions where `owner_id` ≠ target's `chef_business_id`
- This prevents chefs from targeting other chefs' food listings or businesses
- A `CHEF`-owned promotion is evaluated only against that Chef's applicable items and ChefOrderGroup boundaries. This isolation does not prevent compatible promotions belonging to different ChefOrderGroups from coexisting.

For **Platform promotions** (promotions created by the platform/admin):
- No ownership restrictions apply
- Can target authorized combinations of approved, typed commercial resources
- A `PLATFORM`-owned promotion uses a separately selected supported food-order calculation scope according to its definition. For example, `owner_type = PLATFORM` with `calculation_scope = ORDER` is valid; `calculation_scope = PLATFORM` is invalid.

For **Entrepreneur promotions**:
- `ENTREPRENEUR` is an ownership domain, not a food-order calculation scope.
- Kitchen booking, Space, equipment-rental, and Kitchen-subscription targeting uses the applicable typed booking/rental target family described by this amendment. This does not make those resources part of food-order calculation scope.

For **Dietitian promotions**:
- `DIETITIAN` is an ownership domain, not a calculation scope.
- A Dietitian-owned Promotion may target approved Dietitian commercial services such as the Dietitian's `ConsultationOffering`.
- A Dietitian recommendation or referral does not grant Promotion ownership or targeting control over a Chef's `FoodListing`, `ChefMealPlan`, or commercial pricing. Recommendation/referral attribution is separate from Promotion targeting.

For **Organization promotions**:
- `ORGANIZATION` identifies the approved accountable Organization that owns/manages the Promotion; it is not an individual Chef, fake Chef business, Entrepreneur property owner, Dietitian, or Platform identity.
- Organization ownership is permitted only for commercial domains and resources for which business policy and authorization allow that Organization to manage Promotions.
- The requesting User must be authorized to act for the declared Organization owner. Authenticated User identity and Promotion owner identity are not equivalent.

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

## Additive Amendment — Cross-Domain Typed Targeting

### Scope and Compatibility with the Accepted Food Model

The original food target model remains accepted and valid, including:

- The existing `FOOD_LISTING`, `MENU`, `CHEF_BUSINESS`, `CATEGORY`, and `CATEGORY_ALL` meanings.
- Ordinary target-row uniqueness on `(promotion_id, target_type, target_id)` when `target_id` is present.
- The partial uniqueness rule allowing at most one `CATEGORY_ALL` row for the applicable Promotion/target family under the accepted nullable-target semantics.
- Existing target identity and history semantics.

Cross-domain support extends alongside this accepted food model. Existing food targeting is not forced to migrate merely for structural symmetry. Any later persistence reconciliation needed to provide stronger relational enforcement for the accepted food relationships must preserve these semantics and be made explicitly in the canonical ERD rather than silently reinterpreting this ADR.

### Distinct Promotion Concepts

The following concepts are distinct and must not be collapsed:

```text
Promotion owner
!= Promotion commercial domain
!= Calculation scope
!= Benefit type
!= Funding source
!= Target
```

The Promotion commercial domain identifies the business capability that evaluates the Promotion. The target identifies the concrete commercial resource or typed family of resources within that domain that is eligible. `PLATFORM` is an owner and possible funding source; it is not a commercial domain or calculation scope merely because the Platform owns or funds the Promotion.

For example, a Platform-funded Dietitian consultation subsidy may have `PLATFORM` ownership and funding, the `DIETITIAN_CONSULTATION` commercial domain, a target identifying an approved `ConsultationOffering`, and a consultation-domain-specific calculation scope. Those facts remain separate.

A target association answers **what commercial resource is eligible**. It does not determine whether the benefit is a `CUSTOMER_PRICE_DISCOUNT` or a `PLATFORM_FEE_DISCOUNT`/waiver, how the benefit is calculated, or who economically funds it. Benefit, calculation, funding, application, and financial consequences remain governed by ADR-014 and later financial architecture.

### Typed Commercial-Domain Target Families

Promotion targeting is typed by commercial domain or target family. Only target families corresponding to approved commercial resources are introduced; this amendment does not assume every domain supports every target type.

Approved target families may include, where applicable:

- **Food order/catalog:** the existing food target family, `ChefMealPlan`, and `MealSubscriptionOffer`.
- **Kitchen/rental:** an approved Kitchen Booking or Kitchen Space commercial target, `EquipmentRental`, and `KitchenSubscriptionOffer`.
- **Professional service:** a Dietitian `ConsultationOffering`.

Every concrete target association identifying a canonical business resource must be capable of enforcing a real relationship to that resource. The architecture therefore uses domain-specific typed target associations or an equivalent strongly typed relational strategy for cross-domain targeting.

The cross-domain extension does **not** adopt:

- One unconstrained universal `target_type` plus arbitrary UUID `target_id` relationship for unrelated aggregates.
- One giant target table with a nullable foreign-key column for every possible target resource.
- One universal target-registry table created merely to simulate polymorphism.
- JSONB as the canonical relationship to targeted business entities.

Exact table names and the final physical layout for new target families belong to the later canonical ERD pass. This ADR decides the typed-targeting and referential-integrity principle without prematurely fixing those names.

A Promotion target referencing a canonical domain resource must not rely only on application code to prove that a UUID belongs to the declared target type when a relationally enforceable association is practical. Database-enforceable referential integrity is favored. Any exception requires an explicit documented decision rather than becoming the default.

### Domain-Level and All-Target Semantics

`CATEGORY_ALL` retains exactly its accepted meaning and nullable-target uniqueness behavior in the existing food target model.

For a new commercial domain or target family, "all eligible resources in this domain/family" is a typed domain-level targeting concept. A missing concrete target does not mean `ALL` unless that domain's target contract explicitly defines that meaning. Concrete references retain typed referential integrity, and the canonical ERD will finalize each domain's representation. The food model's nullable `CATEGORY_ALL` pattern must not be copied blindly into unrelated domains where `NULL` would acquire a different or ambiguous meaning.

### Subscription Product Boundaries

The following product boundaries remain explicit:

```text
ChefMealPlan != MealSubscriptionOffer != MealSubscription
KitchenSubscriptionOffer != ChefKitchenSubscription != KitchenBooking
```

Promotions generally target the commercial offer/product definition or an approved billable commercial context. A Customer's active `MealSubscription` or a Chef's active `ChefKitchenSubscription` is not a normal Promotion catalog target unless a future explicit business requirement approves that capability. Application or redemption against a subscription billing cycle is outside this ADR and remains governed by ADR-014 and later financial architecture.

### Equipment Target Identity

`EquipmentCatalogItem` is standardized equipment type/reference data. `EquipmentRental` is the actual per-Space commercial rental offering. A Promotion for a concrete rentable equipment offering targets the `EquipmentRental` or another explicitly approved rental scope, not the `EquipmentCatalogItem`, unless a future approved product rule introduces catalog-category targeting. Targeting must not turn equipment master metadata into commercial inventory ownership.

### Advance-Booking Eligibility Is Not a Target

`ADVANCE_BOOKING` is not a target type. Advance-booking lead time is an eligibility condition evaluated under ADR-014. A Promotion continues to target its commercial resource—for example, a `FoodListing`, `ChefMealPlan`, or eligible Kitchen commercial resource—while a lead-time rule may require the request to occur a configured duration before fulfillment or use.

### Target Lifecycle and Historical Evidence

Renaming, retiring, unpublishing, or deprecating a target resource must not silently retarget a Promotion's historical applications or rewrite historical application evidence. Promotion application, snapshot, repricing, refund, and financial evidence remain governed by ADR-014 and the Pricing/Financial architecture; this ADR does not define snapshot persistence.

### Responsibility Boundary with ADR-014

ADR-006 owns Promotion targeting structure, target identity, typed target-family relationships, ownership constraints related to targeting, and target referential integrity.

ADR-014 owns Promotion evaluation, commercial-domain calculation behavior, compatibility and exclusivity, benefit calculation, funding behavior, redemption lifecycle, advance-booking qualification, application and snapshot behavior, and refund/repricing behavior. This amendment does not move those engine responsibilities into ADR-006 and does not introduce a global `stackable` boolean.

## Additive Amendment — Organization Promotion Ownership and Targeting

### Typed Owner Semantics

A Promotion owner answers: **who owns or manages this Promotion as the approved business, professional, or Platform actor?** The valid conceptual owner identities include `CHEF`, `PLATFORM`, `ENTREPRENEUR`, `DIETITIAN`, and `ORGANIZATION`. This amendment does not convert every existing owner into an Organization: an authorized independent Chef may own an eligible Chef Promotion, an independent Dietitian may own an eligible consultation Promotion, an individual Entrepreneur may own an eligible Promotion where product policy supports that actor independently of an Organization, and a Platform Promotion remains `PLATFORM`-owned.

The following identities are separate even when one commercial arrangement makes some of them align:

```text
PROMOTION OWNER
!= SERVICE PERFORMER
!= COMMERCIAL PROVIDER
!= SETTLEMENT BENEFICIARY
!= CALCULATION SCOPE
!= TARGET
!= FUNDING SOURCE
```

Owner identity therefore does not by itself identify who performed the service, supplied the transaction commercially, funds the benefit, receives marketplace settlement, or provides the calculation scope. ADR-014 owns benefit/funding evaluation semantics. Future ADR-020 owns accepted commercial-provider obligations, settlement-beneficiary relationships, Promotion/subsidy financial consequences, earning, and payout eligibility.

Owner identity must use canonical, typed business identities with database-enforceable relationships where practical. This amendment does not authorize an unconstrained `owner_type` plus arbitrary UUID relation and does not create a universal `Provider`, `CommercialParty`, `PromotionParty`, or `TargetRegistry` aggregate. The exact owner tables, join structures, foreign-key columns, and enum representation remain canonical-ERD work.

### Entrepreneur, Organization, and Kitchen Authority

`ENTREPRENEUR` is not automatically equivalent to a Kitchen property owner and is not automatically the same identity as an Organization. Where an Entrepreneur operates commercially through an Organization, the Organization may be the actual Promotion owner. Where approved product policy permits an individual Entrepreneur to own a Promotion independently, `ENTREPRENEUR` remains valid. Real-estate ownership does not determine Promotion ownership.

For Kitchen Booking, Equipment Rental, or Kitchen Subscription Promotions, the authorized Kitchen operator Organization may own or provide the target context even when another party owns the real estate. This ADR does not model leases, property-owner payments, or lease administration.

### Domain-Aware Commercial-Provider Organization Target

ADR-006 adds a conceptual typed target semantic, named `COMMERCIAL_PROVIDER_ORGANIZATION` here, meaning **qualifying commercial offerings supplied through the identified Organization**. The semantic name is authoritative at ADR level; the canonical ERD may choose a different physical table, enum, or column name while preserving its meaning and typed Organization reference.

This is a concrete target with a real Organization identity. It must not use `CATEGORY_ALL`-style null-target semantics. The accepted `CATEGORY_ALL` meaning and null-target uniqueness rule remain unchanged, as do the target-ID requirement and duplicate-target protection for ordinary concrete food target rows.

`COMMERCIAL_PROVIDER_ORGANIZATION` is not an unconstrained global target. A domain evaluator or typed target family may permit Organization-level targeting only where approved business policy allows it, potentially including `FOOD_ORDER`, `KITCHEN_BOOKING`, `EQUIPMENT_RENTAL`, `DIETITIAN_CONSULTATION`, `MEAL_SUBSCRIPTION`, or `KITCHEN_SUBSCRIPTION`. This list does not make every Organization eligible in every domain. Authorization, operating authority, and domain-valid resource relationships must be checked.

A target answers **what entity or group of commercial offerings is eligible**. A commercial provider answers **which approved marketplace business supplies the transaction**. An Organization may fill both roles in one case, but commercial-provider identity must not be derived solely from target identity.

`CHEF_BUSINESS` retains its accepted Chef-business meaning. It is not silently redefined as any Organization that employs or engages Chefs. Organization-operated supply spanning distinct Chef performers uses the Organization target semantic rather than a fake shared Chef or an overloaded `CHEF_BUSINESS` target.

An Organization-level target may be combined with approved typed domain criteria such as selected `FoodListing` resources, categories, meal-plan or offer eligibility, temporal conditions, and advance-booking eligibility. ADR-014 remains authoritative for evaluation, compatibility, exclusivity, and application behavior; this ADR does not decide stacking.

### Organization-Operated Food Examples

ABC Food Group employs or engages Chef Ravi and Chef Maria. Promotion P1 may have:

```text
OWNER: ABC Food Group Organization
TARGET: qualifying food offerings commercially supplied by ABC Food Group
ORDER O1 CALCULATION PORTIONS:
  ChefOrderGroup Ravi
  ChefOrderGroup Maria
COMMERCIAL PROVIDER: ABC Food Group
```

ADR-014 applies the Promotion to eligible Item/ChefOrderGroup portions. The Organization owner/target does not collapse Ravi's and Maria's ChefOrderGroups, erase actual-Chef identity, or make either employee the Promotion owner, funder, settlement beneficiary, or shared fake Chef.

An Organization-owned Promotion need not be Organization-wide. Promotion P2 may have ABC Food Group as owner while targeting only Ravi's qualifying `FoodListing` resources. Maria's items do not qualify. This demonstrates:

```text
OWNER != TARGET != CALCULATION SCOPE
CHEF OWNER != CHEFORDERGROUP CALCULATION SCOPE != ORGANIZATION COMMERCIAL PROVIDER
```

The actual Chef performer remains governed by ADR-013. Current or historical Organization employment or membership must not be used to merge ChefOrderGroups.

### Professional, Subscription, Platform, and Payroll Boundaries

An individual Dietitian may own an eligible consultation Promotion. Where legally, professionally, commercially, and operationally permitted, an approved clinic/Organization may instead own the commercial consultation Promotion while the Appointment retains the actual Dietitian performer. Organization ownership does not create Dietitian–Chef association, food recommendation/referral attribution, food-purchase financial attribution, or Dietitian food-sale or Meal Subscription commission.

`DietitianMealPlan` may guide Customer-authorized marketplace discovery. It is not a Promotion owner, automatic food target, referral target, commission source, or Chef financial attribution. It must not become a food Promotion target merely because a Customer used it to discover food. Approved ordinary Customer-authorized dietary attributes may remain eligibility criteria under their owning rules.

The existing product boundary remains:

```text
ChefMealPlan != MealSubscriptionOffer != MealSubscription
```

An Organization operating employed or engaged Chefs may own Promotions for eligible Meal Subscription offers or transactions where policy permits. Organization ownership or targeting does not erase the actual Chef identity on later fulfillment occurrences.

Platform Promotions remain `PLATFORM`-owned, including approved Platform-funded Customer subsidies involving an Organization commercial provider. They do not become `ORGANIZATION`-owned merely because an Organization supplies the service. Conversely, Promotion owner and funding source are not required to be the same identity.

```text
MARKETPLACE PROMOTION ECONOMICS != EMPLOYEE PAYROLL
```

An Organization-owned Promotion may affect that Organization's accepted commercial economics. It does not automatically change a performer's salary, hourly wage, employee sales commission, payroll deduction, or bonus. ADR-006 does not design worker compensation.

Cheffy Operations uses the same ordinary `ORGANIZATION` owner and domain-aware target semantics when normal commercial policy permits. No `owner == CHEFFY`, Cheffy-specific target, or equivalent special branch is introduced. Exiting direct supply therefore requires no Promotion-targeting redesign.

### Authorization and Historical Meaning

The application must verify that the requesting actor is authorized to create or manage Promotions for the declared owner—for example, an Organization administrator for an Organization Promotion, an authorized Chef for an eligible Chef Promotion, an authorized Dietitian for an eligible consultation Promotion, or a Platform operator for a Platform Promotion. Exact RBAC and API representation remain later architecture/API work.

Historical Promotion owner and target evidence must remain explainable if a Chef leaves an Organization, an Organization changes name, a Kitchen operator changes, owner membership changes, or the commercial-provider relationship ends. Historical ownership and targeting must not be reinterpreted from current Organization membership. Exact immutable snapshot/reference representation remains canonical ERD and Pricing work.

### Organization Amendment Responsibility Boundary

ADR-006 owns typed Promotion owner identity, typed Promotion target identity, owner/target relational semantics, domain-valid targeting structure, and the database-enforceable owner/target referential-integrity requirement. Organization/business architecture owns Organization lifecycle, membership, commercial-provider identity, and Kitchen operating authority. ADR-013 owns actual Chef performer and ChefOrderGroup identity. ADR-012 owns provider-neutral Payment and settlement orchestration. ADR-014 owns Promotion evaluation, calculation scope, compatibility/exclusivity, benefit, funding behavior, and application/redemption semantics. Future ADR-020 owns commercial-provider financial obligations, settlement beneficiaries, Promotion/subsidy financial consequences, earning, and payout eligibility. ADR-015 owns ledger posting and reconciliation.

The exact Organization-owner table, target table, owner/target foreign-key columns, enum implementation, Chef/Organization membership representation, commercial-provider or settlement-beneficiary model, connected-account topology, APIs, events, SQL, and migrations remain outside this amendment and deferred to their canonical owners.

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

The original rejection of separate tables applies to creating one table for every individual target type in the accepted food model. It does not reject the amendment's domain-specific typed target associations or an equivalent strongly typed relational strategy for otherwise unrelated commercial target families.

## Implementation Notes

- Add `promotion_targets` table to ERD in `03-database-erd.md`
- Update `04-api-contracts.md` examples to show multiple target types
- Add ownership validation rules to `02-detailed-architecture.md` §16
- Create Flyway migration for the new table
- Update promotion evaluation service to join with promotion_targets
- Add API validation for target ownership rules
