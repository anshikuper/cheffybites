# ADR-007 — Booking Concurrency Control

## Status

Accepted

## Amendment History

The equipment-concurrency portion was correctively amended after review found that the original aggregate `SUM`-plus-`INSERT` algorithm was not serialized under PostgreSQL `READ COMMITTED`. A transaction containing only capacity read, overlap sum, comparison, and allocation insert can race with another transaction performing the same sequence. This amendment replaces that defective equipment-capacity design with row-level serialization on the authoritative EquipmentRental resource. The accepted Kitchen Space GiST exclusion design is unchanged.

This Accepted ADR was subsequently amended additively to define how subscription-funded, recurring-materialization, and booking-replacement workflows compose with the accepted concrete KitchenBooking and EquipmentRental concurrency model. The amendment does not supersede the existing Space range/exclusion, hold, cleaning-occupancy, equipment-capacity, or locking decisions. Kitchen-subscription aggregate, entitlement-policy, recurrence, billing, rollover, renewal, and commercial-term design remain deferred to the later subscription architecture decision and canonical contracts.

## Context

Kitchen spaces are time-based resources that must prevent double-booking. The system needs to ensure that a kitchen space cannot be booked for overlapping time periods, even when multiple chefs try to book simultaneously.

The original architecture sketch in `02-detailed-architecture.md` §14 proposed using PostgreSQL's `EXCLUDE` constraint with GiST index, but the ERD lacked the `tstzrange` generated column needed for this constraint to work.

Additionally, equipment bookings require a different approach since equipment has quantity limits (e.g., 5 stand mixers available), which cannot be handled by simple exclusion constraints.

The current architecture should represent booking holds using booking status plus a hold-expiry timestamp rather than adding advisory locks by default.

## Decision

### Kitchen Space Booking Concurrency

We will use PostgreSQL's `EXCLUDE` constraint with GiST index to prevent double-booking of kitchen spaces:

```sql
-- Create the btree_gist extension (required for EXCLUDE on non-geometric types)
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- Add generated column for occupancy range
ALTER TABLE kitchen.kitchen_bookings
  ADD COLUMN occupancy_range tstzrange
  GENERATED ALWAYS AS (tstzrange(start_at, occupancy_end_at, '[)')) STORED;

-- Add exclusion constraint to prevent overlapping bookings
ALTER TABLE kitchen.kitchen_bookings
  ADD CONSTRAINT kitchen_bookings_no_overlap
  EXCLUDE USING gist (
    kitchen_space_id WITH =,
    occupancy_range WITH &&
  )
  WHERE (status IN ('HELD', 'CONFIRMED'));
```

**Range Semantics:**
- Uses `[)` (inclusive start, exclusive end) to allow back-to-back bookings
- Booking from 09:00-12:00 and 12:00-15:00 are valid (no overlap)
- Booking from 09:00-12:00 and 11:00-14:00 conflict (overlap at 11:00-12:00)

**Status Filtering:**
- Only `HELD` and `CONFIRMED` bookings participate in the exclusion
- `CANCELLED` and `COMPLETED` bookings are excluded from the constraint
- This allows historical analysis of bookings while preventing future conflicts

### Equipment Booking Concurrency

Equipment has quantity limits, requiring allocation records and explicit serialization on a stable authoritative inventory resource. PostgreSQL does not provide a simple `CHECK` or GiST exclusion constraint that directly enforces a sum of overlapping quantities less than or equal to a capacity greater than one.

#### Equipment Inventory Model

- `EquipmentCatalogItem` is the reusable equipment type/catalog definition.
- `SpaceEquipment` associates equipment with a specific Kitchen Space and primarily represents baseline/included equipment assignment.
- `EquipmentRental` is the current rentable inventory offer for one Kitchen Space. It references one EquipmentCatalogItem and owns price, currency, lifecycle/status, and the authoritative `quantity_available`.
- `equipment_rentals.id`, exposed to the API as `equipmentRentalId`, is the stable resource and serialization key for additional equipment rental.
- `EquipmentBooking` is a booking request/line for an EquipmentRental and references `equipment_rental_id`.
- `EquipmentAllocation` is committed capacity consumption for an EquipmentRental associated with a KitchenBooking and references `equipment_rental_id` and `kitchen_booking_id`.

Kitchen Space ownership is determined through `EquipmentRental.kitchen_space_id`. An implementation does not need a redundant `kitchen_space_id` on EquipmentAllocation. If one is retained, database integrity must guarantee that it matches the EquipmentRental's Kitchen Space.

The master equipment catalog is not reservable inventory, and `MASTER_EQUIPMENT` is not the capacity source or lock target. The current model is per Kitchen Space and does not imply a shared Kitchen-wide pool. Equipment shared across multiple Spaces requires a separate approved business and architecture model.

#### Row-Level Serialization

A plain capacity read, overlapping-allocation `SUM`, comparison, and `INSERT` is unsafe under PostgreSQL `READ COMMITTED`, even when the sequence is wrapped in one transaction. Concurrent transactions can observe the same committed allocation state and both insert unless they first serialize on a shared authoritative row.

The primary equipment-reservation strategy is therefore:

1. Determine all requested `equipment_rental_id` values.
2. Sort the identifiers in deterministic order.
3. Acquire PostgreSQL row locks equivalent to `SELECT ... FOR UPDATE` on every corresponding EquipmentRental row, in that order.
4. Only after all required locks are acquired, validate every requested resource and capacity.

While holding each EquipmentRental row lock, the transaction must:

1. Validate that the rental is active/reservable.
2. Validate that it belongs to the Kitchen Space being booked.
3. Read its authoritative `quantity_available`.
4. Calculate capacity-consuming EquipmentAllocations for the same `equipment_rental_id` whose half-open intervals overlap the requested interval.
5. Reject when existing overlapping allocated quantity plus requested quantity exceeds `quantity_available`.
6. Otherwise create the EquipmentAllocation.

The overlap calculation must occur after lock acquisition. A result read before the lock must not be reused. Transactions competing for the same EquipmentRental wait on the same row; after acquiring it, a waiting transaction recalculates against the latest committed state. Transactions for different EquipmentRental rows do not serialize on one shared equipment lock.

Deterministic multi-resource lock ordering minimizes application-created deadlock cycles but does not make deadlocks impossible. Normal row-lock waiting is expected serialization behavior, not an error or retry condition. A bounded internal retry may handle a PostgreSQL deadlock error according to normal infrastructure policy while preserving request idempotency.

#### Equipment Interval and Capacity-Reserving States

The equipment capacity-consuming interval is the Kitchen Booking's complete half-open occupancy interval:

```text
[booking.start_at, booking.occupancy_end_at)
```

It includes mandatory cleaning occupancy. The current product and API do not select an independent equipment-rental interval. Such an interval would require a separate approved product and API decision.

`HELD` and `CONFIRMED` Kitchen Bookings reserve equipment capacity, consistent with the Kitchen Space occupancy rule. A HELD booking therefore consumes the required equipment capacity. When a HELD booking expires, or a booking is cancelled, its allocations must cease to count as active capacity according to the booking/allocation lifecycle.

#### Atomic Reservation Transaction

For a reservation requiring equipment, one local PostgreSQL transaction must:

1. Acquire all requested EquipmentRental row locks in deterministic identifier order.
2. Validate rental ownership and lifecycle/status.
3. Calculate overlapping capacity-consuming allocations after locking.
4. Validate all requested equipment quantities before committing any requested resource.
5. Create or transition EquipmentBooking line records to their capacity-consuming state, as applicable.
6. Create all required EquipmentAllocations.
7. Create or transition the KitchenBooking to `HELD` or `CONFIRMED` when that transition establishes the reservation.
8. Commit the complete reservation together.

If any capacity check fails, the reservation attempt is rejected. No partial EquipmentAllocation, partial capacity-consuming EquipmentBooking state, or associated `HELD`/`CONFIRMED` KitchenBooking transition may commit. This is one local database transaction; no distributed transaction is introduced.

Quotes are informational and non-authoritative and do not reserve equipment capacity. An EquipmentBooking request row may precede allocation only when its state is explicitly non-reserving; this ADR does not add a new status name. Capacity is guaranteed only after the locking, validation, allocation, and booking-transition transaction commits.

Normal API/request idempotency semantics apply so retries do not create duplicate bookings or allocations. `SERIALIZABLE` transactions with mandatory retry are a technically valid alternative, but they are not the primary strategy. Advisory locks are not the default.

### Cleaning Time Blocks

Kitchen bookings include cleaning time. The `occupancy_end_at` should include the cleaning duration:

```sql
-- When creating a booking, calculate end times
start_at: 09:00 (chef arrives)
cooking_end_at: 12:00 (cooking done)
cleaning_duration_minutes: 30 (configurable per kitchen)
occupancy_end_at: 12:30 (space available for next booking)
```

## Additive Amendment — Subscription-Funded and Recurring KitchenBookings

### Scope of This Amendment

ADR-007 remains authoritative specifically for concrete Kitchen Space occupancy, EquipmentRental capacity, and concrete KitchenBooking concurrency, including subscription-funded bookings and materialized recurring occurrences. It is not generalized into a universal scheduling ADR; Dietitian Appointment concurrency is outside its scope and requires its own architecture decision.

The later Kitchen-subscription ADR owns `KitchenSubscriptionOffer`, `ChefKitchenSubscription`, `KitchenEntitlementCycle`, recurrence rules and materialization architecture, subscription billing and grace, rollover, renewal, and entitlement policy. ADR-007 governs only the concurrency and atomicity boundary when those workflows create, hold, confirm, replace, or cancel concrete KitchenBookings and associated Equipment allocations.

### Entitlement and Physical Capacity Are Independent

Kitchen-subscription entitlement answers whether a Chef has commercial entitlement available to request or consume a booking. ADR-007 answers whether the concrete physical Kitchen Space and requested EquipmentRental capacity can actually be reserved.

```text
ENTITLEMENT AVAILABLE != BOOKING AVAILABLE
BOOKING PHYSICALLY AVAILABLE != SUBSCRIPTION ENTITLEMENT AVAILABLE
```

Having entitlement does not guarantee a calendar slot. A subscription-funded KitchenBooking still must satisfy normal Space occupancy, operating-rule, cleaning-occupancy, EquipmentRental-capacity, hold/confirmation, and concurrency requirements. Conversely, physically available Space or equipment does not establish that sufficient subscription entitlement exists.

### Atomic Subscription-Funded Reservation

When a concrete KitchenBooking is funded or authorized by a `ChefKitchenSubscription` entitlement cycle, the successful local reservation transaction must safely coordinate:

1. Subscription entitlement validation and reservation or consumption.
2. Space capacity reservation under the accepted half-open GiST/exclusion model.
3. EquipmentRental capacity reservation under the accepted deterministic row-lock model where equipment is requested.
4. The KitchenBooking transition to `HELD` or `CONFIRMED`.

The operation must establish one coherent booking state or leave no partial committed entitlement or physical-capacity result. It must not commit Space without required entitlement state, entitlement while the booking fails, partial Equipment capacity while Space fails, or Space while required Equipment capacity fails. The exact entitlement persistence layout belongs to the later subscription ADR and canonical ERD.

Subscription entitlement must be protected against concurrent double spend using a database-enforceable or database-serialized strategy appropriate to the final entitlement representation. This may include locking the relevant entitlement-cycle or balance row, or another deterministic database concurrency mechanism. A plain read-balance, application-check, and later-update sequence without concurrency protection is not sufficient. The selected entitlement strategy must compose atomically with concrete KitchenBooking and EquipmentRental reservation.

### Entitlement-Delta Replacement

A booking replacement evaluates effective entitlement for that replacement operation as:

```text
effective entitlement available
= normally available entitlement
 + entitlement released by the booking being replaced
   within the applicable entitlement cycle
```

For example, with 40 hours allocated, zero ordinarily free hours, and an existing 4-hour booking:

```text
old 4h -> new 4h: additional entitlement required = 0h
old 4h -> new 6h: additional entitlement required = 2h
old 4h -> new 3h: 1h becomes releasable under the entitlement policy
```

The exact commercial definition of entitlement units remains owned by the subscription offer and later subscription ADR.

A replacement is not implemented as cancellation of the old booking followed by creation of an unrelated new booking. The original confirmed KitchenBooking remains authoritative and capacity-consuming until the replacement passes all required checks and the replacement transition commits.

A failed replacement leaves all of the following unchanged or absent:

- Original KitchenBooking and its confirmed state.
- Original Space occupancy.
- Original Equipment allocations.
- Original entitlement allocation.
- No orphan replacement entitlement reservation.
- No orphan replacement Space or Equipment capacity.

Conceptually, replacement coordinates validation and locking of the existing booking and applicable entitlement context, old and new entitlement consumption, per-cycle entitlement delta, new Space eligibility and availability, normal ADR-007 Space protection, deterministic locking and validation of requested EquipmentRentals, additional entitlement reservation where required, establishment of replacement state, release of original Space and obsolete Equipment capacity, and release or transfer of original entitlement allocation in one coherent local outcome.

This conceptual description is not a mandatory physical statement order. Detailed design must define and test deterministic lock acquisition ordering across the existing KitchenBooking, entitlement context, new Space reservation, and all EquipmentRental rows. Existing deterministic identifier ordering for multiple EquipmentRental locks remains mandatory. Bounded internal retry may handle database deadlock errors while preserving idempotency; advisory locks remain non-default, and `SERIALIZABLE` remains a valid alternative rather than the default merely for convenience.

### Approval-Required Replacement

When the applicable offer uses `ENTREPRENEUR_APPROVAL_REQUIRED`, the original booking remains confirmed and protected while replacement approval is pending. A proposed replacement may hold new physical capacity using the existing `HELD` semantics. Temporary coexistence of original and proposed physical reservations must not double-charge entitlement merely because both are capacity-consuming during approval.

The replacement must carry explicit context linking it to the original booking, or equivalent evidence. Rejection or expiry releases only the proposed replacement hold and associated proposed equipment capacity and leaves the original booking and entitlement unchanged. Approval atomically establishes the replacement and releases the original booking's Space, obsolete Equipment allocations, and applicable entitlement allocation under the approved workflow. This ADR does not prescribe a replacement-request table.

### Cross-Entitlement-Cycle and Space Moves

Entitlement delta is calculated per entitlement cycle, not globally. A move from a 4-hour September booking to a 4-hour October booking releases the applicable September entitlement and independently requires 4 hours of available October entitlement. If October has insufficient entitlement, the move fails and the September booking remains unchanged. Old-cycle entitlement must not be borrowed to bypass new-cycle rules unless a separately approved rollover or transfer policy allows it; rollover is outside ADR-007.

A replacement may move between Spaces that the subscription offer defines as eligible. Even when entitlement delta is zero, the new Space must independently pass normal occupancy checks, requested EquipmentRental capacity checks, and subscription-offer Space eligibility. Entitlement equality does not imply physical substitutability. Future weighted Space credits are outside this ADR.

### Equipment Changes During Replacement

When replacement changes EquipmentRentals, the transaction uses the existing authoritative `EquipmentRental` capacity model and deterministic EquipmentRental locking order. Original allocations remain valid until replacement succeeds. A rejected or expired proposed replacement releases only its proposed equipment hold. A successful replacement releases obsolete original allocations and commits all new allocations atomically with the booking transition. Partial equipment success is prohibited.

`EquipmentCatalogItem` remains reusable master/reference data, `SpaceEquipment` remains the actual included-equipment association, and `EquipmentRental` remains the additional per-Space commercial and capacity-bearing resource with authoritative `quantity_available`. This amendment does not introduce cross-Space shared equipment; such a pool still requires a separate architecture decision.

### Recurring Booking Materialization

A recurring Kitchen booking rule is not physical capacity and does not directly reserve an unbounded date range merely by existing. Only materialized or otherwise explicitly reserved occurrences consume physical capacity. Materialization is bounded by the subscription booking horizon and occurrence policy owned elsewhere.

Every materialized recurring occurrence becomes an ordinary concrete KitchenBooking and independently passes the complete ADR-007 Space, cleaning, hold/confirmation, EquipmentRental, atomicity, and entitlement protections.

Recurring materialization may apply one of these policy outcomes:

- `ALL_OR_NOTHING`: failure of any required occurrence in the requested materialization batch must not leave unintended partially accepted results for that all-or-nothing operation.
- `BEST_AVAILABLE`: each occurrence may independently succeed or fail, but every successful occurrence receives the full ADR-007 concurrency and entitlement guarantees.

This ADR does not require one large transaction spanning months of future bookings. Exact batch transaction size and long-horizon materialization strategy belong to the later subscription ADR and detailed architecture.

For `THIS_OCCURRENCE`, only the selected materialized KitchenBooking is replaced. For `THIS_AND_FUTURE`, the recurrence definition may change and already-materialized future occurrences may require replacement or reconciliation. Recurrence semantics remain owned by the subscription ADR, while each affected concrete booking obeys the atomic replacement rules here. Original affected bookings remain protected until their individual replacement outcome is established; entitlement and physical capacity must not be double-spent, failures follow the selected series-modification policy, and history is not silently deleted. `THIS_AND_FUTURE` is not required to execute as one giant database transaction.

### Cancellation and Cleaning

Cancellation of a subscription-funded KitchenBooking releases active Space and Equipment capacity according to the existing booking lifecycle. Commercial entitlement restoration or forfeiture follows the captured Kitchen-subscription cancellation policy; ADR-007 does not define a restoration percentage. Capacity release and the entitlement consequence must commit without an internally contradictory state. For Entrepreneur/provider-caused cancellation, applicable Chef entitlement is restored and the Chef is not penalized; refund and remediation economics remain outside ADR-007.

Required cleaning time remains part of `occupancy_end_at` and the physical half-open conflict interval where applicable. A `KitchenSubscriptionOffer` may separately determine whether cleaning consumes entitlement, is commercially included, or creates a separate charge. Those commercial decisions do not shorten or otherwise alter the physical occupancy interval used by ADR-007 concurrency protection.

### Timezone Dependency

Recurring Kitchen rules are interpreted using the authoritative Kitchen IANA timezone according to ADR-011. Once materialized, every KitchenBooking resolves to concrete real instants, and ADR-007 applies its half-open ranges and capacity rules to those instants. DST and recurrence interpretation remain owned by ADR-011 and the subscription recurrence architecture rather than being duplicated here.

## Consequences

### Positive
- Database-level guarantee of no double-booking (not just application-level check)
- GiST index is efficient for range queries
- Equipment reservations competing for the same EquipmentRental serialize on one authoritative relational row
- Equipment capacity validation and all requested allocations commit atomically with the capacity-reserving booking transition
- Cleaning time is explicitly modeled
- Subscription entitlement and physical capacity compose without allowing partial reservation or entitlement double spend
- Atomic replacement protects an existing booking until a valid replacement succeeds
- Recurring rules remain non-capacity facts while each materialized occurrence receives full concurrency protection

### Negative
- Requires `btree_gist` extension
- Generated column adds storage overhead
- Equipment reservation requires deterministic row-lock ordering and careful transaction management
- Subscription-funded replacement requires coordinated locking across booking, entitlement, Space, and Equipment resources
- Series materialization and modification require explicit batch outcome and failure-reporting policy
- Booking holds must expire deterministically; do not rely on advisory locks unless load testing later proves they are necessary.

## Implementation Notes

- Add `btree_gist` to database initialization script
- Add Flyway migration for `occupancy_range` column and EXCLUDE constraint
- Model EquipmentBooking and EquipmentAllocation references with `equipment_rental_id`
- Implement EquipmentRental row locking, post-lock overlap calculation, and all-or-nothing allocation
- Acquire multiple EquipmentRental locks in deterministic identifier order
- Update kitchen booking service to calculate `occupancy_end_at` including cleaning time
- Model booking holds with `status = HELD` and a `hold_expires_at` field on the booking row
- Add API endpoint for checking equipment availability
- Treat availability responses and quotes as non-authoritative until the reservation transaction commits
- Handle capacity conflicts, constraint violations, idempotent retries, and bounded internal deadlock retries gracefully

## Concurrency Test Requirements

Future integration tests must use real PostgreSQL, such as through Testcontainers, with independent concurrent transactions and assertions against committed state. At minimum, verify:

1. Capacity 1 with two concurrent overlapping reservations for the same EquipmentRental allows exactly one to succeed.
2. Capacity N with concurrent requests exceeding N never commits overlapping allocations whose sum exceeds N.
3. Non-overlapping intervals for the same EquipmentRental may both succeed.
4. Requests for different EquipmentRental rows do not unnecessarily serialize each other.
5. A failed or rolled-back transaction consumes no capacity.
6. A waiting transaction recalculates availability after acquiring the row lock.
7. A request for multiple EquipmentRentals uses deterministic lock ordering and commits allocations all-or-nothing.
8. An EquipmentRental belonging to a different Kitchen Space is rejected.
9. An overlap only within mandatory cleaning occupancy still conflicts.
10. An idempotent retry creates no duplicate allocation.
11. Concurrent subscription-funded requests cannot spend the same entitlement balance twice.
12. With zero ordinarily free entitlement, replacing a same-cycle 4-hour booking with another 4-hour booking requires zero additional entitlement and preserves the original until commit.
13. Replacing a same-cycle 4-hour booking with a 6-hour booking requires only the 2-hour delta.
14. Failed replacement preserves the original booking, Space occupancy, Equipment allocations, and entitlement allocation and leaves no orphan proposed capacity.
15. Cross-cycle replacement independently validates new-cycle entitlement and preserves the old-cycle booking when the new cycle lacks capacity.
16. Approval-required replacement expiry releases proposed holds without changing the original booking.
17. Equipment changes during replacement commit all-or-nothing using deterministic EquipmentRental lock ordering.
18. A recurrence rule alone reserves no physical capacity; every materialized occurrence independently passes ADR-007.
19. `ALL_OR_NOTHING` materialization leaves no unintended partial batch result, while every `BEST_AVAILABLE` success satisfies full capacity and entitlement protection.
20. Cleaning commercial treatment never reduces the physical occupancy interval.

## Alternatives Considered

1. **Unlocked application availability check followed by a write**
   - Rejected: checking availability without locking a shared authoritative row leaves a race window before allocation.
   - The current safe design instead uses one database transaction that locks each authoritative EquipmentRental row before calculating overlapping allocation quantity and inserting allocations.

2. **Redis for booking locks**
   - Rejected: Redis is not authoritative for transactional data
   - Violates the backend-authoritative principle

3. **Separate booking slots (fixed time blocks)**
   - Rejected: Inflexible for varying chef needs
   - Real kitchens have variable cooking times

4. **Event sourcing for availability**
   - Rejected: Over-engineering for MVP
   - PostgreSQL remains authoritative for reservation concurrency

5. **`SERIALIZABLE` transactions with mandatory retry**
   - Valid when all reservation paths use the isolation level and correctly retry serialization failures.
   - Not selected as the primary strategy because EquipmentRental provides a narrower stable row-lock target.

6. **Advisory locks**
   - Rejected as the default because the authoritative EquipmentRental row is the natural relational lock target.

7. **Per-physical-unit rows with GiST exclusion constraints**
   - Could support a larger model that assigns specific physical units and excludes overlapping use per unit.
   - Not selected for the current fungible quantity-based EquipmentRental model. GiST alone does not directly enforce a sum of overlapping quantities less than or equal to N.
