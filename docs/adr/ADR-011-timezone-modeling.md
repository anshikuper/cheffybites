# ADR-011 --- Timezone Modeling Strategy

## Status

Proposed

## Context

Timezone handling is critical for Cheffy Bites because:

-   Kitchens operate according to local business hours.
-   Chefs create availability in kitchen-local time.
-   Customers may browse from another timezone.
-   Orders, bookings, preparation, pickup, and delivery use real
    instants.
-   Daylight-saving transitions can make local timestamps ambiguous or
    invalid.

The architecture therefore needs to distinguish:

1.  A real instant on the global timeline.
2.  A local wall-clock business time.
3.  The IANA timezone whose rules give that local time meaning.

## Decision

We adopt a **UTC instants + IANA business timezone** model.

### Core Principles

-   Persist real instants in PostgreSQL using `TIMESTAMPTZ`.
-   Treat those instants canonically as UTC in application and API
    logic.
-   Store an IANA timezone ID where local business semantics are
    required.
-   Model recurring local schedules as local date/time values plus an
    IANA timezone, not as permanently converted UTC timestamps.
-   Never use a numeric UTC offset as a substitute for an IANA timezone.
-   Do not accept ambiguous offset-free timestamps for API fields that
    represent real instants.

## PostgreSQL Semantics

PostgreSQL `TIMESTAMPTZ` represents an instant.

It does not preserve the original timezone name or original textual
offset supplied by the caller.

Therefore, when the original business timezone matters, it must be
stored separately.

Example:

``` sql
CREATE TABLE kitchen.kitchens (
    id UUID PRIMARY KEY,
    timezone_id VARCHAR(100) NOT NULL
);
```

Example value:

``` text
America/Toronto
```

Timezone IDs must be validated against the IANA timezone database in the
application layer.

## Instant-Based Data

Fields representing events that occurred or fixed future instants use
`TIMESTAMPTZ`.

Examples:

``` text
created_at
updated_at
payment_succeeded_at
order_placed_at
booking_start_at
booking_end_at
delivery_requested_at
delivered_at
hold_expires_at
```

Example:

``` sql
start_at TIMESTAMPTZ NOT NULL,
end_at   TIMESTAMPTZ NOT NULL
```

## Local Business Schedules

Recurring business schedules must preserve local wall-clock intent.

Examples include:

-   Kitchen opening hours.
-   Weekly recurring availability.
-   Repeating preparation windows.
-   Other schedules defined as "every Monday at 09:00 local time."

These should be represented using local values and a reference IANA
timezone.

Conceptually:

``` text
day_of_week
local_start_time
local_end_time
timezone_id
```

They should not be permanently converted to UTC because DST changes the
corresponding UTC offset throughout the year.

When a recurring schedule is materialized into a concrete occurrence,
the system resolves the local date/time using the applicable IANA
timezone rules and persists the resulting instant as `TIMESTAMPTZ`.

## Reference Timezone

The Kitchen is the source of truth for kitchen-local business time.

A timezone may be copied into an immutable or historical snapshot when
preserving the timezone used for a past or already-materialized business
decision is necessary.

Do not denormalize `reference_timezone` into every table by default.

Denormalization requires a concrete historical, audit, or performance
reason.

## Food Availability

If food availability is a concrete interval, persist:

``` sql
start_at TIMESTAMPTZ NOT NULL,
end_at   TIMESTAMPTZ NOT NULL
```

and derive display timezone from the relevant Kitchen unless the
availability must preserve an immutable timezone snapshot.

If availability is recurring, store the recurrence using local schedule
semantics and materialize concrete UTC instants when required.

## Customer Addresses

A customer address does not require a persisted timezone merely because
it is a delivery address.

If delivery logic requires the address timezone, it may be derived from
geolocation and stored only when there is a defined business
requirement.

Kitchen timezone remains the authoritative timezone for kitchen
operating rules.

## API Rules

### Instant Fields

API fields representing real instants must use RFC 3339 / ISO 8601
timestamps with either:

``` text
Z
```

or an explicit numeric offset.

Valid:

``` text
2026-09-05T16:00:00Z
2026-09-05T12:00:00-04:00
```

Invalid for an instant field:

``` text
2026-09-05T12:00:00
```

The API must not silently assume UTC for offset-free timestamps
representing real instants.

### Local Schedule Fields

When an API intentionally accepts local business time, it must also
provide the applicable IANA timezone or obtain it from an unambiguous
domain source such as the Kitchen.

Example:

``` json
{
  "localStart": "09:00:00",
  "localEnd": "17:00:00",
  "timezoneId": "America/Toronto"
}
```

### Responses

Canonical instant fields should normally be returned in UTC:

``` json
{
  "startAt": "2026-09-05T16:00:00Z",
  "endAt": "2026-09-05T20:00:00Z",
  "timezoneId": "America/Toronto"
}
```

Clients may render those instants in:

-   The Kitchen timezone.
-   The customer's selected timezone.
-   The device timezone.

The UI must clearly communicate which timezone is being displayed when
ambiguity could affect the user.

## Java Time Types

Use:

``` text
Instant
OffsetDateTime
ZonedDateTime
ZoneId
LocalDate
LocalTime
LocalDateTime
```

according to semantics.

Recommended rules:

-   Persisted global instant: `Instant` or `OffsetDateTime`.
-   Business timezone: `ZoneId`.
-   Recurring local wall-clock time: `LocalTime` plus `ZoneId`.
-   Local date/time awaiting timezone resolution: `LocalDateTime` plus
    `ZoneId`.

Do not create a helper named `toUtc()` that returns `ZonedDateTime` if
the domain really needs an `Instant`.

Prefer explicit APIs such as:

``` java
Instant resolveInstant(LocalDateTime localDateTime, ZoneId zoneId);
ZonedDateTime displayAtZone(Instant instant, ZoneId zoneId);
```

## Daylight-Saving Time

DST requires explicit handling.

When converting a local date/time to an instant:

-   A local time may not exist during a spring-forward gap.
-   A local time may map to two instants during a fall-back overlap.

The application must define a validation policy.

Default policy:

-   Reject nonexistent local times and require the caller to choose a
    valid time.
-   Reject ambiguous local times unless the request includes enough
    information to choose the intended offset.

Do not silently guess during booking, ordering, or financial workflows.

## Kitchen Timezone Changes

Changing a Kitchen's timezone affects future local scheduling semantics.

Rules:

-   Historical instants remain unchanged.
-   Already-materialized concrete booking/order instants remain
    unchanged unless explicitly rescheduled.
-   Recurring schedules use the Kitchen timezone rules defined for their
    effective configuration.
-   If historical timezone configuration must be auditable, model
    timezone changes with effective dating or immutable configuration
    history rather than rewriting historical records.

## Consequences

### Positive

-   Correct distinction between instants and local business time.
-   Correct DST behavior.
-   Clear API semantics.
-   Avoids unnecessary timezone denormalization.
-   Preserves Kitchen-local scheduling intent.
-   Supports customers viewing times in different zones.

### Negative

-   Requires developers to distinguish local schedules from instants.
-   DST gaps and overlaps require explicit validation.
-   Historical timezone configuration may require effective-dated
    records if Kitchen timezone changes become a supported operation.

## Alternatives Considered

### Store Everything as UTC Instants

Rejected for recurring local schedules because "09:00 every Monday" is a
local-time rule whose UTC offset may change with DST.

### Store Everything as Local Time

Rejected because global ordering, comparisons, integrations, and
auditing require real instants.

### Store Numeric Offset Only

Rejected because an offset such as `-04:00` does not contain future or
historical DST rules.

### Denormalize Timezone to Every Time-Based Entity

Rejected as the default because it creates unnecessary duplication and
consistency problems. Snapshot the timezone only where historical
semantics require it.

## Implementation Notes

1.  Use `TIMESTAMPTZ` for real instants.
2.  Use IANA timezone IDs such as `America/Toronto`.
3.  Validate timezone IDs with `ZoneId`.
4.  Reject offset-free API timestamps when the field represents an
    instant.
5.  Model recurring schedules using local-time semantics.
6.  Add tests for DST gaps and overlaps.
7.  Document the display timezone in customer-facing UI.
8.  Update the ERD and API contracts to distinguish instant fields from
    local schedule fields.

## References

-   Java `java.time`
-   IANA Time Zone Database
-   PostgreSQL `TIMESTAMP WITH TIME ZONE`
-   RFC 3339 / ISO 8601
