# ADR-007 — Booking Concurrency Control

## Status

Accepted

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

Equipment has quantity limits, requiring a different approach using allocations:

```sql
-- Equipment bookings track the request
CREATE TABLE equipment.equipment_bookings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    kitchen_booking_id UUID NOT NULL REFERENCES kitchen.kitchen_bookings(id),
    equipment_id UUID NOT NULL REFERENCES equipment.master_equipment(id),
    quantity INT NOT NULL CHECK (quantity > 0),
    status VARCHAR(20) NOT NULL DEFAULT 'RESERVED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Equipment allocations track confirmed quantities per time slot
CREATE TABLE equipment.equipment_allocations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    equipment_id UUID NOT NULL REFERENCES equipment.master_equipment(id),
    kitchen_space_id UUID NOT NULL REFERENCES kitchen.kitchen_spaces(id),
    start_at TIMESTAMPTZ NOT NULL,
    end_at TIMESTAMPTZ NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    kitchen_booking_id UUID NOT NULL REFERENCES kitchen.kitchen_bookings(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

**Concurrency Control for Equipment:**
```sql
-- Function to check and reserve equipment atomically
CREATE OR REPLACE FUNCTION equipment.reserve_equipment(
    p_equipment_id UUID,
    p_kitchen_space_id UUID,
    p_start_at TIMESTAMPTZ,
    p_end_at TIMESTAMPTZ,
    p_quantity INT,
    p_kitchen_booking_id UUID
) RETURNS BOOLEAN AS $$
DECLARE
    v_allocated INT;
    v_available INT;
    v_max_quantity INT;
BEGIN
    -- Get max quantity for equipment
    SELECT quantity INTO v_max_quantity
    FROM equipment.master_equipment
    WHERE id = p_equipment_id;

    -- Calculate already allocated quantity for overlapping time
    SELECT COALESCE(SUM(ea.quantity), 0) INTO v_allocated
    FROM equipment.equipment_allocations ea
    WHERE ea.equipment_id = p_equipment_id
      AND ea.kitchen_space_id = p_kitchen_space_id
      AND tstzrange(ea.start_at, ea.end_at, '[)') &&
         tstzrange(p_start_at, p_end_at, '[)');

    v_available := v_max_quantity - v_allocated;

    IF v_available < p_quantity THEN
        RETURN FALSE; -- Not enough equipment available
    END IF;

    -- Reserve the equipment
    INSERT INTO equipment.equipment_allocations (
        equipment_id, kitchen_space_id, start_at, end_at, quantity, kitchen_booking_id
    ) VALUES (
        p_equipment_id, p_kitchen_space_id, p_start_at, p_end_at, p_quantity, p_kitchen_booking_id
    );

    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;
```

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
- Atomic reservation via PostgreSQL constraints
- Equipment allocation function is atomic and prevents race conditions
- Cleaning time is explicitly modeled

### Negative
- Requires `btree_gist` extension
- Generated column adds storage overhead
- Equipment reservation function requires careful transaction management
- Complex to modify booking times (may need to cancel and rebook)
- Booking holds must expire deterministically; do not rely on advisory locks unless load testing later proves they are necessary.

## Implementation Notes

- Add `btree_gist` to database initialization script
- Add Flyway migration for `occupancy_range` column and EXCLUDE constraint
- Add `equipment_bookings` and `equipment_allocations` tables
- Implement `reserve_equipment` function
- Update kitchen booking service to calculate `occupancy_end_at` including cleaning time
- Model booking holds with `status = HELD` and a `hold_expires_at` field on the booking row
- Add API endpoint for checking equipment availability
- Handle constraint violations gracefully in application code

## Alternatives Considered

1. **Application-level locking with `SELECT ... FOR UPDATE`**
   - Rejected: Race condition window between check and insert
   - PostgreSQL constraint is atomic and guaranteed

2. **Redis for booking locks**
   - Rejected: Redis is not authoritative for transactional data
   -违反 "backend is authoritative" principle

3. **Separate booking slots (fixed time blocks)**
   - Rejected: Inflexible for varying chef needs
   - Real kitchens have variable cooking times

4. **Event sourcing for availability**
   - Rejected: Over-engineering for MVP
   - Simple exclusion constraint is sufficient