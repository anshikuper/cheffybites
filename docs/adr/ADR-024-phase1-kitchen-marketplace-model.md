# ADR-024 — Phase-1 Kitchen Marketplace Request, Offer, and Availability Model

## Status

Accepted

Accepted during `P1-ARCH-01` architecture reconciliation on 2026-09-01.

## Context

The Phase-1 Chef-to-Kitchen pilot validates discovery and operator-reviewed
rental requests before Cheffy Bites introduces paid Kitchen booking. The
earlier architecture coupled KitchenBooking creation to checkout, stored a
single hourly rate on KitchenSpace, and described recurring availability
without a complete precedence model. That baseline could not represent the
approved pilot product, which needs multiple rental bases, explicit request
decisions, one-time and recurring availability and blocks, and safe concurrent
operator acceptance without payment.

ADR-007 already supplies the accepted database authority for concrete Space
occupancy. ADR-011 supplies the accepted timezone model. This ADR defines the
pilot aggregate and the way the new product concepts compose with those
decisions.

## Decision

### KitchenBooking Owns the Request Lifecycle

`KitchenBooking` is the authoritative aggregate from request submission
through cancellation. There is no separate persistent `BookingRequest`
aggregate. “Booking request” is an API and UI view of a KitchenBooking whose
status is `REQUESTED`.

The Phase-1 transitions are:

```text
REQUESTED -> CONFIRMED
REQUESTED -> DECLINED
REQUESTED -> WITHDRAWN
CONFIRMED -> CANCELLED
```

`REQUESTED`, `DECLINED`, `WITHDRAWN`, and `CANCELLED` do not reserve Space.
`CONFIRMED` reserves Space. `HELD` remains a valid long-term capacity-reserving
status under ADR-007 but is not entered by the Phase-1 request workflow.

Confirmation occurs in one local transaction. The application reauthorizes
the actor, locks/reloads the request, revalidates stage/publication/pilot
authorization, RentalOffer and Space availability, calculates cleaning-aware
occupancy, records the state transition and outbox event, and attempts the
transition to `CONFIRMED`. The ADR-007 GiST exclusion constraint is the final
authority. If another confirmation wins, the API returns HTTP 409
`BOOKING_CONFLICT` and the losing request remains `REQUESTED`. Withdrawal and
operator decision races use the same first-valid-transition rule.

The requested Space, Chef, selected offer, use/occupancy boundaries, and
request/offer snapshots are immutable in Phase 1. Request submission and
transition commands use booking-domain idempotency receipts scoped by data
scope, actor, operation, key hash, and request hash. They do not reuse the
Financial domain's idempotency records. A successful command's receipt,
aggregate transition, history, and outbox event commit atomically.

### Payment Is Outside the Phase-1 Request Workflow

Creating, confirming, declining, withdrawing, or cancelling a pilot request
does not create or require Payment, PaymentAttempt, PaymentAllocation, tax,
payout, ledger, checkout, or provider client-secret state. A future paid
Kitchen-booking workflow must be introduced as a distinct checkout capability
after the applicable financial ADRs and business policies are accepted. It may
use `HELD`, but payment must not become a prerequisite for the existence of a
KitchenBooking request.

### RentalOffer Is the Pricing Authority

`RentalOffer` belongs to exactly one KitchenSpace and is the sole canonical
source of current Space rental terms. `KitchenSpace` does not own a competing
hourly rate or minimum booking duration. Supported bases are:

```text
HOURLY
FIXED_BLOCK
DAILY
RECURRING_HOURS
MONTHLY_HOURS
PRIVATE_LONG_TERM_INQUIRY
```

Monetary offers use integer minor units and an explicit ISO-4217 currency.
`PRIVATE_LONG_TERM_INQUIRY` may intentionally omit an amount and currency.
Basis-specific block, included-unit, and minimum-commitment fields are
validated by database and application constraints. Deposit information and
additional-charge notes are informational in Phase 1 and do not create
financial obligations.

Estimates are calculated only without hidden interpretation or rounding:
HOURLY when the duration formula yields an exact integer minor-unit value,
FIXED_BLOCK for whole configured blocks, and DAILY for whole configured local
day definitions. Recurring/monthly offers display their stated plan without a
one-off proration; private inquiries have no amount. All other cases are
`REQUIRES_CONFIRMATION`.

The billable duration is the requested cooking/use interval. Cleaning extends
protected occupancy but is not silently billed. A
`KITCHEN_LOCAL_CALENDAR_DAY` counts a Kitchen-local civil date rather than a
fixed 24-hour duration through daylight-saving changes. Informational notes do
not alter either rule.

Every submitted request snapshots the selected offer's identifier, version,
terms, estimate method, estimate/disclaimer evidence, relevant Space/Kitchen
labels, timezone, cleaning occupancy, and Chef-supplied request context.
Historical decisions never depend on the live offer or profile after
submission. Core relationships and requirement declarations remain normalized;
the snapshot is not an untyped JSON dump.

### Space Availability Uses Explicit Rules

Availability belongs to KitchenSpace. A single rule model uses two orthogonal
dimensions:

- `availabilityKind`: `AVAILABLE` or `BLOCKED`.
- `scheduleKind`: `ONE_TIME` or `WEEKLY`.

This represents one-time availability, recurring weekly availability,
one-time blocked periods, and recurring weekly blocked periods. Weekly rules
use explicit weekdays, local start/end times, effective start/end dates,
`active`, and `version`; Phase 1 does not introduce an unrestricted recurrence
language. One-time rules use a local date and local start/end times. All rules
are interpreted in the owning Kitchen's IANA timezone under ADR-011.

Kitchen operating hours constrain availability; they never create offerable
availability by themselves. Evaluation precedence is:

1. permitted data scope and pilot stage;
2. operator `PUBLISHED` Kitchen state, active Space, and independent active
   platform pilot authorization;
3. Kitchen operating hours;
4. at least one matching active `AVAILABLE` rule;
5. any matching active `BLOCKED` rule vetoes availability;
6. overlapping `HELD` or `CONFIRMED` occupancy vetoes availability;
7. current RentalOffer duration/commitment and declared requirement checks.

A hard offer-duration or commitment failure is no match. Equipment,
shared-resource, and other operator-specific conditions that require human
review produce `POSSIBLE_OPERATOR_CONFIRMATION_REQUIRED` rather than a false
deterministic match.

Search results are advisory. Submission rechecks eligibility. Confirmation is
authoritative and concurrency-safe. Later rule changes do not cancel or move an
existing confirmed booking. Creating or activating a BLOCKED rule that matches
future `HELD`/`CONFIRMED` occupancy is rejected; the operator must use the
explicit cancellation workflow first. Pending requests remain `REQUESTED` but
are re-evaluated and shown as currently incompatible where applicable.

### Equipment in the Pilot

Space equipment uses the controlled mode `INCLUDED`, `SHARED`,
`EXTRA_DISCUSS`, or `UNAVAILABLE`. `EXTRA_DISCUSS` is request context, not a
price, reservation, allocation, or financial commitment. The long-term
EquipmentRental capacity model in ADR-007 remains available to a future paid
workflow and is not activated by the Phase-1 pilot.

## Consequences

- The pilot can test marketplace demand without unresolved payment policy.
- Request history and rental terms remain auditable after live data changes.
- Competing operator acceptances are resolved by the database without losing
  the rejected request.
- Rental pricing has one owner and can express non-hourly arrangements.
- Availability is predictable, timezone-safe, and supports explicit vetoes.
- A later paid workflow must add its own accepted financial decision and API
  rather than silently changing the pilot request contract.

## Alternatives Considered

### Separate BookingRequest Aggregate

Rejected because it creates two identities and a conversion boundary for one
business interaction without adding a distinct lifecycle owner.

### Reserve Capacity on Submission

Rejected because a non-binding request would block legitimate requests and
would make inbox behavior an accidental reservation system.

### Keep Hourly Price on KitchenSpace

Rejected because it competes with multi-basis RentalOffer terms and cannot
represent fixed, recurring, monthly, or private-inquiry arrangements.

### Encode Availability as Operating Hours or a Boolean

Rejected because operating hours are only a constraint and a boolean cannot
represent dated availability, explicit blocks, recurrence, DST, or precedence.

## References

- `docs/product/P1-MVP-01-chef-kitchen-pilot-marketplace-spec.md`
- ADR-007 — Booking Concurrency Control
- ADR-011 — Timezone Modeling Strategy
- `docs/03-database-erd.md`
- `docs/04-api-contracts.md`
- `docs/05-event-contracts.md`
