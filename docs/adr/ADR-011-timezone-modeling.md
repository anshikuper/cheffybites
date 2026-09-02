# ADR-011 --- Timezone Modeling Strategy

## Status

Accepted

Accepted during `P1-ARCH-01` architecture reconciliation on 2026-09-01.
The review found the decision mature, internally consistent, and compatible
with the canonical product, persistence, API, and event contracts. Domain
contracts still own their recurrence shapes, but they may not override this
ADR's instant, IANA-zone, DST-disambiguation, or history-preservation rules.

## Context

Timezone handling is critical for Cheffy Bites because:

-   Kitchens operate according to local business hours.
-   Chefs create availability in kitchen-local time.
-   Dietitians offer recurring online and location-specific in-person
    professional availability.
-   Kitchen and Meal Subscription recurrence rules require local civil-time
    interpretation before materialization.
-   Customers may browse from another timezone.
-   Orders, KitchenBookings, Appointments, materialized meal fulfillment,
    preparation, pickup, and delivery use real instants.
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
-   Require one explicit authoritative scheduling context and valid IANA
    timezone before materializing a business-local rule.
-   Do not accept ambiguous offset-free timestamps for API fields that
    represent real instants.

## PostgreSQL Semantics

PostgreSQL `TIMESTAMPTZ` represents an instant.

It does not preserve the original timezone name or original textual
offset supplied by the caller.

Therefore, when the original business timezone matters, it must be
stored separately.

``` text
TIMESTAMPTZ != IANA timezone identity
```

The presence of business-local schedules does not justify converting
resolved instant columns into local timestamps. Preserve IANA zone identity
separately where recurrence interpretation or historical explanation requires
it; exact domain columns remain a persistence-design decision.

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

Abbreviations such as `EST`, `PST`, and `CST` are not authoritative schedule
zones because they are ambiguous and do not express complete daylight-saving
rules. UTC remains appropriate for globally neutral system timestamps, but it
must not replace a required business-local IANA scheduling zone.

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
KitchenBooking.start_at
KitchenBooking.occupancy_end_at
Appointment.start_at
Appointment.service_end_at
Appointment.occupancy_end_at
MealFulfillmentOccurrence.fulfillment_start_at
MealFulfillmentOccurrence.fulfillment_end_at
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
-   Recurring Kitchen or Space booking requests such as every Tuesday
    08:00-12:00.
-   Dietitian recurring online availability such as Monday 09:00-17:00.
-   In-person ConsultationLocation availability.
-   Recurring offered meal-fulfillment windows.
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

A recurring or business-local schedule is a rule, not a concrete scheduled
fact. Database overlap protection for concrete resources operates on resolved
instants or ranges; it must not compare recurring local-time rules directly.

## Authoritative Scheduling Contexts

### Kitchen Scheduling Context

`Kitchen.iana_timezone_id` is the source of truth for Kitchen-local business
time. This includes operating hours, recurring Kitchen booking requests,
Space recurrence, and Kitchen-local production scheduling. A rule such as
"every Tuesday 08:00-12:00" means that local interval in the Kitchen's
authoritative IANA timezone, not a fixed UTC interval.

A recurring Kitchen-subscription reservation is separate from its
materialized KitchenBooking occurrences. Each occurrence is resolved to real
instants before ADR-007 applies Space, cleaning, EquipmentRental, hold, and
concurrency protection. One fixed UTC time-of-day must not represent the
recurring Kitchen rule.

### Dietitian Professional Scheduling Context

A Dietitian professional may maintain a configured professional scheduling
timezone represented by a valid IANA timezone identifier.

-   Recurring online Consultation availability normally uses the Dietitian's
    professional scheduling timezone unless an offering or schedule has
    another explicitly approved authoritative scheduling context.
-   In-person availability uses the applicable `ConsultationLocation`
    timezone. The location must have or derive an authoritative IANA timezone
    from normalized location/geography.
-   When a Dietitian works at multiple physical locations in different
    timezones, each location-specific schedule uses that location's timezone.
-   An Organization headquarters timezone is not silently authoritative for
    a practicing Dietitian unless that Organization or location is the actual
    scheduling context.
-   Customer browser, device, or selected timezone is presentation and input
    context, not the source of truth for provider recurring availability.

Example:

``` text
Dietitian professional scheduling timezone: America/Toronto
Recurring online availability: Monday 09:00-12:00
Customer display timezone: America/Vancouver
```

The rule remains Monday 09:00-12:00 in `America/Toronto`; the Customer UI may
display its corresponding Vancouver local time.

`ConsultationOffering` does not have to own a duplicated timezone in every
case. It uses availability through the scheduling context that defines the
local recurrence: normally the Dietitian professional scheduling timezone for
online service or the ConsultationLocation timezone for in-person service.
The exact reference, snapshot, and foreign-key representation belongs to the
owning domain and canonical ERD.

### Meal-Subscription Fulfillment Context

A recurring offered fulfillment-window definition is distinct from a
materialized `MealFulfillmentOccurrence` window. If a Chef defines a recurring
window such as Tuesday 12:00-15:00, the later subscription architecture must
identify one explicit authoritative IANA timezone context from the applicable
business, production, or fulfillment context. It must not default blindly to
the Customer timezone.

ADR-011 does not decide whether that recurring window's timezone is owned by
`ChefMealPlan`, `MealSubscriptionOffer`, Kitchen, or another approved
aggregate. A confirmed concrete MealFulfillmentOccurrence window is
materialized as real start and end instants.

### Historical Scheduling Context

A timezone may be copied into an immutable or historical snapshot when
preserving the timezone used for a past or already-materialized business
decision is necessary.

Do not denormalize `reference_timezone` into every table by default.

Denormalization requires a concrete historical, audit, or performance
reason.

Historical display and audit must not depend solely on the current timezone
configuration when the authoritative zone used for the original agreement may
later change. The owning domain may preserve the applicable zone identity in
version, snapshot, or other evidence without requiring a timezone column on
every record.

## Appointment Materialization

A recurring or one-off local availability rule is not an Appointment. When a
Customer selects an actual slot, its local occurrence is resolved under the
authoritative professional scheduling context into concrete real instants.

Conceptually, `Appointment.start_at`, `Appointment.service_end_at`, and
`Appointment.occupancy_end_at` are real instants. Applicable before/after
buffers may affect the occupancy interval according to the professional
scheduling architecture without changing this timezone principle.

`HELD` and `CONFIRMED` Appointment overlap protection operates on concrete
resolved instant ranges. This ADR does not decide the Appointment concurrency
mechanism or database constraint.

## External Calendar Busy Intervals

Future external-calendar busy intervals are normalized into real instants.
They constrain availability but do not become the authoritative timezone for
Cheffy recurring professional availability. Cheffy Appointment facts remain
authoritative within Cheffy Bites.

Timezone resolution does not require importing private external event titles
or details. Provider-specific calendar APIs and integration mechanics are
outside this ADR.

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

Concrete scheduled facts may additionally expose the authoritative IANA
timezone when clients need to display the agreed business-local
interpretation. Clients must not infer an authoritative schedule timezone
from a numeric offset alone.

### Local Schedule Fields

When an API intentionally accepts local business time, it must also
provide the applicable IANA timezone or obtain it from an unambiguous
domain scheduling context such as the Kitchen, Dietitian professional
scheduling context, or ConsultationLocation.

Recurring schedule creation or editing requires sufficient information to
identify the applicable local date or day-of-week, local wall-clock time, and
authoritative IANA timezone or scheduling context. Exact API fields remain
owned by the API contract.

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

Do not use `LocalDateTime` alone as the persisted representation of an already
resolved global instant. Do not hand-roll timezone or DST arithmetic; use
Java's standards-based `java.time` and timezone-database support. Exact types
per DTO or entity remain an implementation detail consistent with these
semantics.

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

The owning scheduling domain must define and document its deterministic
materialization policy. Modules must not independently inherit undocumented
library defaults.

### Nonexistent Local Times

For explicit user-created or edited schedules and one-time occurrences,
reject a local time in a spring-forward gap with a clear validation error
unless an approved domain policy defines another deterministic behavior. Do
not silently shift a nonexistent time, such as 02:30 on an affected date, to
another local time.

Recurring materialization must apply the owning scheduling or subscription
policy's documented deterministic gap rule. ADR-011 favors explicit failure
or exception handling over silent clock shifting.

### Ambiguous Local Times

For an explicit one-time request during a fall-back overlap, require enough
information to resolve the intended occurrence, such as an explicit offset,
a selected occurrence, or another deterministic contract approved by the
scheduling domain. Reject the request when ambiguity cannot be resolved; do
not guess.

Recurring materialization must use a documented deterministic overlap policy.
It must not rely on an implicit JVM or library default for choosing the
earlier or later offset.

## Timezone Configuration Changes

Changing a Kitchen's authoritative timezone, a Dietitian professional
scheduling timezone, or a ConsultationLocation timezone may affect future
unmaterialized local scheduling semantics.

Rules:

-   Historical instants remain unchanged.
-   Already-materialized concrete KitchenBooking, Appointment,
    MealFulfillmentOccurrence, and Order instants remain unchanged unless
    explicitly rescheduled through the applicable business workflow.
-   Recurring schedules use the Kitchen timezone rules defined for their
    effective configuration or the corresponding effective Dietitian/location
    scheduling context.
-   If historical timezone configuration must be auditable, model
    timezone changes with effective dating or immutable configuration
    history rather than rewriting historical records.

For example, a confirmed Appointment remains fixed to its original instant if
the Dietitian later changes professional scheduling timezone, and a confirmed
KitchenBooking remains fixed if the Kitchen timezone is corrected or changed.

For future unmaterialized recurrences, the owning scheduling or subscription
architecture decides whether a timezone change affects future materialization,
requires versioning, or requires explicit user confirmation. ADR-011 never
silently rewrites already materialized occurrences.

## Phase-1 Kitchen Pilot Application

The Phase-1 Kitchen Marketplace applies this decision as follows:

-   A Kitchen's IANA timezone is authoritative for operating hours and Space
    availability rules.
-   One-time and weekly Space rules are stored as business-local values with
    effective dates; they are not stored as permanently converted UTC rules.
-   A booking request carries concrete `startAt`, `endAt`, and
    `occupancyEndAt` instants, plus a snapshot of the Kitchen timezone used to
    interpret and explain the request.
-   Explicit local input in a DST gap is rejected. Explicit local input in an
    overlap must identify the intended offset/occurrence or is rejected.
-   Phase 1 resolves weekly rules on demand over bounded date ranges. A gap or
    unresolved overlap is returned as a dated validation exception for operator
    correction; it never shifts or chooses an offset silently. Any future
    persistent materializer requires its own exact occurrence/error contract.
-   Changing a Kitchen timezone affects future unmaterialized rules only. It
    does not rewrite submitted requests or confirmed bookings.

This application is a clarification of the accepted model, not a separate
timezone policy.

## Scope and Responsibility Boundary

ADR-011 decides the distinction between real instants and business-local
schedules, authoritative named IANA scheduling contexts, materialization,
DST disambiguation requirements, timezone-change history, and transport/time
type semantics.

It does not decide Appointment database concurrency, subscription aggregate
boundaries, entitlement calculations, recurrence-table schema, KitchenBooking
GiST mechanics, domain-specific recurrence policies, exact API fields, exact
new-domain ERD columns, frontend components, calendar-provider APIs, or
financial accounting rules beyond normal instant semantics. ADR-007 remains
authoritative for concrete KitchenBooking and EquipmentRental concurrency
after Kitchen recurrence has materialized to real instants.

## Consequences

### Positive

-   Correct distinction between instants and local business time.
-   Correct DST behavior.
-   Clear API semantics.
-   Avoids unnecessary timezone denormalization.
-   Preserves Kitchen-local scheduling intent.
-   Defines explicit professional online and in-person scheduling contexts.
-   Supports multiple Dietitian locations across timezones.
-   Preserves Meal Subscription window ownership for its later domain
    architecture while requiring an authoritative IANA context.
-   Supports customers viewing times in different zones.

### Negative

-   Requires developers to distinguish local schedules from instants.
-   DST gaps and overlaps require explicit validation.
-   Historical timezone configuration may require effective-dated
    records if Kitchen timezone changes become a supported operation.
-   Professional and subscription scheduling domains must define explicit
    DST recurrence behavior and historical scheduling evidence where needed.

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
