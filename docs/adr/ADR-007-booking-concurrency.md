# ADR-007 — Booking Concurrency Control

## Status

Accepted

## Amendment History

The equipment-concurrency portion was correctively amended after review found that the original aggregate `SUM`-plus-`INSERT` algorithm was not serialized under PostgreSQL `READ COMMITTED`. A transaction containing only capacity read, overlap sum, comparison, and allocation insert can race with another transaction performing the same sequence. This amendment replaces that defective equipment-capacity design with row-level serialization on the authoritative EquipmentRental resource. The accepted Kitchen Space GiST exclusion design is unchanged.

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

## Consequences

### Positive
- Database-level guarantee of no double-booking (not just application-level check)
- GiST index is efficient for range queries
- Equipment reservations competing for the same EquipmentRental serialize on one authoritative relational row
- Equipment capacity validation and all requested allocations commit atomically with the capacity-reserving booking transition
- Cleaning time is explicitly modeled

### Negative
- Requires `btree_gist` extension
- Generated column adds storage overhead
- Equipment reservation requires deterministic row-lock ordering and careful transaction management
- Complex to modify booking times (may need to cancel and rebook)
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
