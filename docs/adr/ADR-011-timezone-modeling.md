# ADR-011 — Timezone Modeling Strategy

## Status

Proposed

## Context

Timezone handling is critical for a food marketplace where:
- Chefs operate in specific kitchens with local business hours
- Customers browse from different timezones
- Orders need to be scheduled for preparation and delivery
- Food availability slots need to be meaningful in local context

The master spec example in `01-master-spec.md` §15 shows `2026-09-05T12:00:00-04:00` (EDT), indicating that timezone-aware timestamps are expected.

However, the ERD shows inconsistent timezone handling:
- Some tables have explicit `timezone` columns
- Others rely on implicit assumptions
- `FOOD_AVAILABILITIES` lacks timezone despite needing to represent local availability

## Decision

We will adopt a **"UTC storage with reference timezone"** approach:

### Core Principle
- **Store all timestamps in UTC** (using `TIMESTAMPTZ` or `TIMESTAMP WITH TIME ZONE`)
- **Denormalize the reference timezone** from the kitchen to entities that need local context
- **Perform timezone conversion at presentation/UI layer**
- **Maintain canonical UTC for all business logic and storage**

### Implementation

#### 1. Kitchen Timezone (Source of Truth)
```sql
CREATE TABLE kitchen.kitchens (
    id UUID PRIMARY KEY,
    -- ... other columns
    timezone VARCHAR(50) NOT NULL DEFAULT 'UTC',  -- IANA timezone name
    -- ... other columns
);
```

#### 2. Food Availability (with reference timezone)
```sql
CREATE TABLE food.food_availabilities (
    id UUID PRIMARY KEY,
    food_listing_id UUID NOT NULL REFERENCES food.food_listings(id),
    start_at TIMESTAMPTZ NOT NULL,  -- UTC time
    end_at TIMESTAMPTZ NOT NULL,    -- UTC time
    reference_timezone VARCHAR(50) NOT NULL,  -- Denormalized from kitchen
    -- ... other columns (spicy, vegetarian, etc.)
    
    -- Constraint: reference_timezone must match the food listing's kitchen
    CONSTRAINT fk_food_availability_timezone
        FOREIGN KEY (food_listing_id) 
        REFERENCES food.food_listings(id)
        -- Would need a trigger or application logic to validate timezone match
);
```

#### 3. Food Listing (with reference timezone)
```sql
CREATE TABLE food.food_listings (
    id UUID PRIMARY KEY,
    chef_business_id UUID NOT NULL REFERENCES chef.chef_businesses(id),
    kitchen_id UUID NOT NULL REFERENCES kitchen.kitchens(id),
    reference_timezone VARCHAR(50) NOT NULL,  -- Denormalized from kitchen
    -- ... other columns (name, description, price, etc.)
    
    -- Constraint: reference_timezone must match the kitchen's timezone
    CONSTRAINT fk_food_listing_timezone
        FOREIGN KEY (kitchen_id) 
        REFERENCES kitchen.kitchens(id)
        -- Would need a trigger or application logic to validate timezone match
);
```

#### 4. Customer Addresses (for delivery timezone)
```sql
CREATE TABLE customer.customer_addresses (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL REFERENCES customer.customers(id),
    -- ... address columns
    timezone VARCHAR(50) NOT NULL,  -- IANA timezone from geocoding
    -- ... other columns
);
```

### Timezone Conversion Strategy

#### UI Layer (Customer-facing)
1. Detect customer's browser timezone (via Intl API or explicit selection)
2. For displaying kitchen hours/availability:
   - Convert UTC timestamps to customer's timezone
   - Show local time with appropriate timezone abbreviation
3. For displaying kitchen local times:
   - Convert UTC timestamps to kitchen's reference timezone
   - Show local time with kitchen's timezone abbreviation

#### Business Logic Layer
1. All comparisons, calculations, and storage use UTC
2. When checking if a time slot is available:
   - Convert request time to UTC using kitchen's reference timezone
   - Compare against UTC start/end times
3. When validating business hours:
   - Convert current time to kitchen's reference timezone
   - Check against opening/closing times

#### API Layer
1. Accept timestamps in ISO 8601 format (with or without timezone offset)
2. If no offset provided, assume UTC
3. Store as UTC in database
4. Return timestamps in ISO 8601 format with 'Z' suffix (UTC)
5. Include timezone metadata in responses when needed:
   ```json
   {
     "start_at": "2026-09-05T16:00:00Z",
     "end_at": "2026-09-05T20:00:00Z",
     "reference_timezone": "America/New_York",
     "local_start_at": "2026-09-05T12:00:00-04:00",
     "local_end_at": "2026-09-05T16:00:00-04:00"
   }
   ```

### Special Cases

#### Daylight Saving Time
- UTC storage handles DST transitions naturally
- Reference timezone ensures correct local time representation
- UI libraries handle DST-aware formatting

#### Timezone Changes
- If a kitchen changes timezone:
  - Existing future availability slots keep their original reference_timezone
  - New slots use the new timezone
  - Consider adding effective_date to timezone for historical accuracy (future enhancement)

### Implementation Notes

- Add `TimezoneService` utility with methods:
  - `ZonedDateTime toUtc(LocalDateTime localTime, String timezone)`
  - `LocalDateTime fromUtc(ZonedDateTime utcTime, String timezone)`
  - `String formatLocalTime(ZonedDateTime utcTime, String timezone)`
- Use Java's `java.time` package (ZoneId, ZonedDateTime, OffsetDateTime)
- Validate timezone names against IANA database
- Add database constraints or triggers to ensure reference_timezone consistency
- Update API documentation to specify timestamp format
- Add frontend utility functions for timezone conversion

## Consequences

### Positive
- Eliminates timezone confusion in storage and business logic
- Clear separation of concerns: storage (UTC) vs presentation (local)
- Handles DST correctly
- Enables accurate cross-timezone comparisons
- Consistent with modern Java/Python datetime best practices

### Negative
- Slight storage overhead for reference_timezone columns
- Need to maintain denormalized timezone data
- UI layer must handle conversion correctly
- Initial learning curve for developers

## Alternatives Considered

1. **Store everything in local time with explicit timezone**
   - Rejected: Makes cross-timezone comparisons difficult
   - Complicates storage and indexing

2. **Store UTC only, derive timezone from kitchen on demand**
   - Rejected: Performance impact from joins
   - Risk of inconsistency if kitchen timezone changes

3. **Use offset-date-time (TIMESTAMP WITH TIME ZONE) everywhere**
   - Rejected: Still requires knowing the reference timezone for local display
   - Doesn't solve the core problem of needing local context

4. **Application-level timezone service with caching**
   - Considered: Adds complexity
   - Chosen approach is simpler and more explicit

## References

- Java `java.time` documentation
- IANA Time Zone Database
- PostgreSQL TIMESTAMP WITH TIME ZONE
- RFC 3339/ISO 8601 timestamp format