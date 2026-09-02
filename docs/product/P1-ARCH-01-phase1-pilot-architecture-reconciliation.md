# P1-ARCH-01 — Phase-1 Pilot Architecture Reconciliation

**Record type:** Architecture reconciliation and audit record  
**Date:** 2026-09-02  
**Status:** ARCHITECTURE RECONCILED — READY FOR IMPLEMENTATION PLANNING

This report is not a new source of domain truth. Product requirements remain
owned by the approved product specifications; architecture decisions by
Accepted ADRs; exact persistence, API, and event representation by the
canonical documents `03`, `04`, and `05`.

## 1. Executive Result

The Phase-1 Chef-to-Kitchen pilot is reconciled with the canonical Cheffy Bites
architecture. All eight reported architecture blockers now have explicit,
implementation-plannable decisions. The pilot is a request-first,
operator-approved marketplace with no payment or financial side effect.
KitchenBooking owns request history, RentalOffer owns rental terms, explicit
Space rules own availability, ADR-007 remains the concurrency guarantee, and
ADR-011 is now Accepted.

No production code, test code, migration, deployment change, external message,
or operator contact was created or performed.

## 2. Files Inspected

Read completely before reconciliation:

- `AGENTS.md`
- `docs/product/P1-MVP-01-chef-kitchen-pilot-marketplace-spec.md`
- `docs/product/LP-01-public-credibility-surface-spec.md`
- `docs/01-master-spec.md`
- `docs/02-detailed-architecture.md`
- `docs/03-database-erd.md`
- `docs/04-api-contracts.md`
- `docs/05-event-contracts.md`
- `docs/adr/ADR-001-modular-monolith.md`
- `docs/adr/ADR-002-transactional-outbox.md`
- `docs/adr/ADR-003-nextjs.md`
- `docs/adr/ADR-005-order-fulfillment-types.md`
- `docs/adr/ADR-006-promotion-targeting.md`
- `docs/adr/ADR-007-booking-concurrency.md`
- `docs/adr/ADR-009-outbox-schema.md`
- `docs/adr/ADR-010-uuidv7-identifiers.md`
- `docs/adr/ADR-011-timezone-modeling.md`
- `docs/adr/ADR-012-payment-marketplace-settlement.md`
- `docs/adr/ADR-013-chef-order-group-aggregate-financial-boundary.md`
- `docs/adr/ADR-014-promotion-engine.md`
- `docs/adr/ADR-015-financial-ledger-reconciliation.md`
- `docs/adr/ADR-016-event-versioning.md`
- `docs/adr/ADR-017-professional-identity-credentials-jurisdiction-eligibility.md`
- `docs/adr/ADR-018-dietitian-engagement-appointment-scheduling-online-meeting-provisioning.md`
- `docs/adr/ADR-019-subscription-entitlement-materialized-occurrence-architecture.md`
- `docs/adr/ADR-020-commercial-obligations-earning-recognition-payable-source-financial-model.md`
- `docs/adr/ADR-021-authorized-multi-context-conversation-architecture.md`
- `docs/adr/ADR-022-platform-governed-taxonomy-reference-data-lifecycle.md`
- `docs/adr/ADR-023-verified-experience-reviews-reputation.md`

Repository application directories and Git state were also inspected to verify
that the web-topology decision did not displace existing implementation. The
web application directories are scaffolding; there is no sunk production web
implementation requiring a migration.

## 3. Product Requirements Reconciled

The reconciliation covers the complete approved P1 boundary:

- Auth0-authenticated operator and Chef accounts with database-owned profiles.
- Organization membership through Location, Kitchen, and Space.
- Kitchen publication and separate platform pilot authorization.
- Coarse authenticated pilot discovery with protected exact address/access
  information.
- Space equipment modes and all six RentalOffer bases.
- One-time/weekly availability and blocked exceptions.
- Persisted Chef request, operator decision, withdrawal, and cancellation.
- Concurrency-safe confirmed occupancy including cleaning time.
- Immutable request/offer evidence and append-only transition history.
- DEMO/REAL separation, controlled-pilot stage, feedback, durable
  notifications, bilingual locale support, and minimal administration.
- One responsive pilot web deployment while preserving the shared backend API
  for web and mobile.

The product documents were not changed. No contradiction within either
approved product document required correction.

## 4. Booking / Request Decision

KitchenBooking is authoritative from submission onward. “Booking request” is
an API/UI representation of `KitchenBooking(status = REQUESTED)`, not another
persistent aggregate.

```text
REQUESTED -> CONFIRMED
REQUESTED -> DECLINED
REQUESTED -> WITHDRAWN
CONFIRMED -> CANCELLED
```

`REQUESTED`, `DECLINED`, `WITHDRAWN`, and `CANCELLED` are non-reserving.
`CONFIRMED` reserves the complete use-plus-cleaning occupancy. `HELD` remains a
long-term ADR-007 state but is not used by this pilot.

Confirmation locks/reloads and revalidates the request, actor, current gates,
offer, rules, requirements, and occupancy in one transaction. ADR-007's GiST
exclusion is final authority. Exactly one competing confirmation can commit;
the loser gets `409 BOOKING_CONFLICT` and remains `REQUESTED`. Withdrawal versus
decision is first-valid-transition-wins. Operator approval remains final, and
neither publication nor confirmation proves property ownership or a
sublicense/re-rental right.

## 5. Payment Decoupling

P1 request submission, confirmation, decline, withdrawal, and cancellation do
not create Payment, PaymentAttempt, PaymentAllocation, provider tokens, tax,
refund, payout, promotion, or ledger records. Rental estimates, deposits, and
additional-charge notes are informational only.

The long-term payment architecture remains preserved. A later paid
Kitchen-booking flow requires accepted financial/legal decisions and a
separate checkout contract, may use `HELD`, and must not redefine the pilot
request endpoint or make Payment prerequisite to KitchenBooking existence.

## 6. RentalOffer Decision

RentalOffer belongs to one KitchenSpace and is the sole canonical live source
of rental pricing and commitment terms. KitchenSpace no longer owns a
competing hourly price, currency, or minimum booking duration.

Supported bases:

```text
HOURLY
FIXED_BLOCK
DAILY
RECURRING_HOURS
MONTHLY_HOURS
PRIVATE_LONG_TERM_INQUIRY
```

Monetary offers use integer minor units plus ISO currency.
`PRIVATE_LONG_TERM_INQUIRY` may omit both. Basis-specific blocks, included
units, duration, and commitment fields are validated. Informational deposit,
additional-charge, and terms notes do not create a charge. The chosen
offer/version and estimate evidence are frozen on request submission.
Estimate duration is the use interval, excluding cleaning occupancy; a
Kitchen-local calendar day is a civil date rather than a fixed 24-hour chunk.

## 7. Availability Decision

Availability is attached to KitchenSpace. One rule model has orthogonal
dimensions `AVAILABLE|BLOCKED` and `ONE_TIME|WEEKLY`, plus local times,
effective dates, active state, and version. Weekly recurrence uses explicit ISO
weekdays; unrestricted recurrence syntax is not introduced.

Precedence is:

1. data scope, pilot stage, lifecycle, publication, and pilot authorization;
2. Kitchen operating constraints;
3. at least one matching active AVAILABLE rule;
4. any matching BLOCKED rule vetoes;
5. overlapping HELD/CONFIRMED protected occupancy vetoes;
6. RentalOffer duration/commitment and declared requirements.

Operating hours constrain but do not create availability. Search is advisory;
submission and confirmation recheck. A later rule edit never silently changes
or cancels a confirmed booking.
Hard offer failures are no match; equipment/shared/operator-specific conditions
that require review produce `POSSIBLE_OPERATOR_CONFIRMATION_REQUIRED`.

## 8. Timezone / ADR-011 Decision

ADR-011 is Accepted. It is mature and consistent with P1 and the canonical
contracts: local schedules use business-local values plus the authoritative
Kitchen IANA timezone; concrete request/booking boundaries are real instants.
It explicitly prohibits JVM-default semantics, silent gap shifting, silent
overlap selection, and rewriting historical instants after a timezone change.

The P1 clarification rejects nonexistent local times and requires explicit
occurrence/offset disambiguation for overlaps. Bounded weekly resolution
returns a dated, correctable exception instead of silently guessing; Phase 1
persists no Space-availability occurrence rows.

## 9. DEMO / REAL Decision

Pilot roots belong to an immutable data scope classified `DEMO` or `REAL`.
Relationships cannot cross scopes. DEMO cannot be user-promoted or mutated to
REAL; real onboarding creates fresh REAL records. Local/staging identity and
data remain separated from production.

Demo reset accepts one explicit, resettable DEMO scope, is restricted and
audited, and has no REAL path. The platform stage is versioned
`PRE_PILOT|CONTROLLED_PILOT`. Operator `PUBLISHED` status and platform pilot
authorization are independent. REAL discovery requires both plus every other
active/requestability gate. Emergency admin unpublish records the admin actor
and reason rather than impersonating an operator.

## 10. Profile / Organization Decision

Auth0 owns credentials and authentication. The database owns User status and a
one-to-one ParticipantProfile with display/contact data and `en-CA|fr-CA`
locale. ChefProfile owns an optional business display/trading label,
description/intended activity, coarse operating area, and controlled
multi-select business categories.

The bounded pilot role codes are `OPERATOR_OWNER`, `OPERATOR_MANAGER`, `CHEF`,
and `ADMIN`: operator roles are Organization-membership scoped, Chef/Admin are
platform grants, and manager Kitchen assignment is explicit. Each maps to
granular permissions and resource-context checks. The Chef business
display/trading label is presentation data, not a duplicate Organization or
ChefBusiness legal name.

The operator path is:

```text
User -> OrganizationMembership -> Organization -> Location -> Kitchen -> Space
```

One User is not one Kitchen, and multiple authorized members may manage an
Organization/assigned Kitchen. No permit, insurance, identity-document,
credential, lease-management, or payroll subsystem is pulled into P1. The
narrow Chef category catalog does not silently accept Proposed ADR-022.

## 11. Request Snapshot / History Decision

Submission writes explicit immutable request and RentalOffer snapshot rows in
the same transaction as the `REQUESTED` KitchenBooking. Evidence includes
Kitchen/Space labels, timezone, concrete boundaries, cleaning, Chef/business
labels, activity, requirement declarations, selected offer/version, terms,
estimate formula/status, and disclaimer version.

Requirement and equipment-need rows snapshot their displayed version/labels
and offering context while retaining typed normalized relationships.

Core relationships and requirements remain normalized; no giant JSON document
or live-profile reconstruction is used. Every state transition appends actor,
role, from/to state, time, and bounded reason evidence. Live offer, listing,
profile, or timezone edits never rewrite historical request evidence.

The submitted Space, Chef, offer, use/occupancy boundaries, and snapshots are
immutable in Phase 1. Booking-domain command receipts provide request-hash
idempotency independently of Financial records and commit with successful
state/history/outbox changes.

## 12. Address / Privacy Decision

Location owns the exact street/postal address, precise point, and access
instructions. Authenticated pilot discovery uses a distinct coarse area label
and optional intentionally coarse point/distance band. Exact fields are
available only to authorized parties of a confirmed booking under the
disclosure policy.

Phase 1 fixes the policy to `CONFIRMED_PARTIES_ONLY`; broader disclosure
options require a later explicit contract change.

Exact address/coordinates, access instructions, private contacts, request free
text, and feedback text are excluded from public search, sitemaps, SEO,
integration events, analytics payloads, and ordinary logs.

## 13. Equipment Decision

Phase-1 Space equipment uses exactly `INCLUDED`, `SHARED`, `EXTRA_DISCUSS`, or
`UNAVAILABLE`, with optional display quantity and bounded notes.
`EXTRA_DISCUSS` is operational context; it is not exclusive capacity, a price,
reservation, or financial obligation.

The existing EquipmentRental/EquipmentAllocation architecture remains
preserved but inactive for this pilot. Its concurrency/payment capabilities are
not triggered by a Space equipment mode.

## 14. Feedback Decision

Pilot feedback is authenticated, private, bounded internal triage input with a
controlled role context, category, locale, route context, and at most one
verified typed related resource. It is not a public Review or Chat message.
Free text is length-limited and excluded from events, analytics, and logs.
Triage status is `NEW|IN_REVIEW|RESOLVED|DISMISSED`.

## 15. Notification Decision

Booking transitions produce durable, idempotent in-app Notifications and
separate per-channel delivery records. Phase 1 supports in-app and email only.
Email is asynchronous and retryable; provider failure never rolls back a
committed booking transition.

Notifications snapshot locale and schema-validated safe template arguments.
They exclude sensitive/free text and exact address. Opening the in-app detail
reloads current authoritative booking state. SMS is outside the pilot. Push
remains a later native P1 channel driven by the same booking events; its exact
transport contract is deferred to that slice.

## 16. Web Topology Decision

Accepted ADR-025 selects one Phase-1 Next.js deployment in
`apps/customer-web`:

```text
/                         LP-01 public credibility surface
/app/operator/*           protected operator workspace
/app/chef/*               protected Chef workspace
```

Public and authenticated cache/authorization boundaries are separate.
Backend role, membership, resource, and scope authorization remains
authoritative. `apps/business-web` and `apps/chef-web` are reserved for future
independent deployments. Web/native clients consume the same versioned
backend API/OpenAPI client. The topology is reviewed after R3/R4 evidence.

## 17. Database Changes

The canonical ERD now defines:

- data scopes, pilot stage/history, Kitchen publication history, and separate
  pilot authorization, scoped reset evidence, and account-status history;
- ParticipantProfile, OrganizationProfile, revised ChefProfile, controlled Chef
  categories/joins, role/permission grants, and manager Kitchen assignments;
- coarse discovery-safe versus exact private Location fields;
- Kitchen presentation, operating-hour rules, operator requirements, media
  assets/typed associations, and complete Space operating fields, including
  optional size and maximum physical use duration;
- RentalOffer and one-time/weekly AVAILABLE/BLOCKED Space rules, including DST
  overlap disambiguation;
- equipment offering modes;
- KitchenBooking `REQUESTED` lifecycle, offer FK, real boundaries, partial
  GiST reservation, immutable request/offer snapshots, normalized
  requirements/equipment needs, append-only outside-review evidence, and
  append-only status history plus booking-domain command receipts;
- bounded feedback and durable notification/delivery tables;
- explicit constraints for money, recurrence, typed links, scope isolation,
  idempotency, history, and privacy.

No Flyway migration was created; migration design belongs to P1-IMP-01.

## 18. API Changes

The canonical API now includes participant/Chef profiles, controlled
categories, Organization/Location management, complete Kitchen/Space
management and preview, operating-hour/requirement management, validated media
association, operator management lists, authenticated Space-centric safe
Kitchen search, RentalOffer CRUD, Space availability-rule
CRUD, equipment modes, protected confirmed-party address retrieval, request
submit/read/list, outside-review evidence, confirm/decline/withdraw/cancel,
pilot feedback, notifications, pilot stage/authorization, account state,
emergency unpublish, inspection, and scoped demo reset.

The former payment-coupled `POST /kitchen-bookings` and
`/kitchen-bookings/quote` are not Phase-1 contracts. P1 submission is
`POST /kitchen-booking-requests`, returns `REQUESTED`, and never returns a
client secret. Error contracts cover `BOOKING_CONFLICT`, state conflict, DST
gap/overlap, and authorization without private-data leakage.

## 19. Event Changes

Reused and fully defined:

- `KitchenPublished.v1`
- `KitchenBookingConfirmed.v1`
- `KitchenBookingCancelled.v1`

Added and justified for notifications and approved controlled analytics:

- `KitchenBookingRequested.v1`
- `KitchenBookingDeclined.v1`
- `KitchenBookingWithdrawn.v1`

All use the ADR-009 outbox and ADR-016 envelope/version rule. Payloads contain
safe typed identifiers, state/scope, controlled actor/reason fields where
applicable, and timestamps. No exact address, contact details,
request/feedback free text, access instructions, or arbitrary reason text is
emitted. Availability/RentalOffer CRUD and feedback events are deferred
because no Phase-1 integration need justifies them.

## 20. ADR Changes / New ADRs

- ADR-007 remains Accepted and unchanged. Its `HELD|CONFIRMED` partial GiST
  protection composes with the new non-reserving REQUESTED state.
- ADR-011 changed from Proposed to Accepted and received an explicit P1
  Kitchen-pilot application section.
- ADR-024 was created and Accepted for the KitchenBooking request lifecycle,
  payment boundary, RentalOffer authority, availability precedence, and
  Phase-1 equipment semantics.
- ADR-025 was created and Accepted for the single-deployment Phase-1 web
  topology and reevaluation boundary.
- ADR-017 and ADR-019 through ADR-023 received cross-reference-only corrections
  so they describe ADR-011's new Accepted status accurately; their own Proposed
  statuses and decisions did not change.
- All other Proposed ADRs remain Proposed. None was silently accepted.

## 21. Long-Term Architecture Preserved

The modular monolith, transactional outbox, selective event integration,
PostgreSQL/PostGIS, Auth0/OIDC, three long-term experiences, React Native
clients, ADR-007 `HELD` support, advanced EquipmentRental capacity, and the
financial/order/delivery architecture remain available. The pilot narrows
active workflows; it does not erase or counterfeit future decisions.

In particular, future paid Kitchen booking can add a distinct checkout after
the legal/accounting/provider model is approved, without changing the request
aggregate, historical evidence, or concurrency guarantee.

## 22. Deferred Pilot Features

Deferred from web-first P0 or from the pilot entirely, as noted:

- payments, refunds, payouts, ledger, tax, promotions, and subscriptions;
- food ordering, delivery, Dietitians, ratings/reviews, chat, and wishlist;
- advanced equipment rental/allocation and independent equipment intervals;
- dynamic pricing, external calendars, instant booking, and proposal
  negotiation;
- lease/sublicense verification, master-lease financial workflow, payroll,
  credential documents, and general administration;
- SMS and unrestricted recurrence; native push remains sequenced to its later
  P1 implementation slice;
- open public marketplace or automatic R4 expansion.

## 23. Cross-Document Consistency Findings

Fixed contradictions:

- payment-oriented Kitchen booking and `PAYMENT_PENDING` versus request-first
  P1;
- KitchenSpace hourly price/minimum duration versus RentalOffer ownership;
- recurring-only/ambiguous availability versus one-time/weekly available and
  blocked rules with precedence;
- ADR-011 references that still described it as Proposed;
- generic Kitchen publication treated as requestability versus separate
  platform pilot authorization;
- equipment included/rental booleans versus four honest pilot modes;
- separate pilot web deployments versus an explicit bounded unified topology;
- event catalog omissions for request, decline, and withdrawal;
- detailed architecture's stale statement that ADR-018–ADR-023 did not exist;
- conceptual-only Kitchen/Space media, operating details, operator
  requirements, account state, and demo-reset audit behavior that still lacked
  exact canonical persistence/API representation;
- an unnecessary `useEnd` field alternative; canonical persistence now retains
  ADR-007's `cooking_end_at` convention and the HTTP/event field remains
  `endAt`;
- an event decline-reason example that did not match the canonical API enum;
  it now uses `REQUIREMENT_MISMATCH`, and optional decline/withdraw reason
  omission plus cancellation actor semantics are explicit;
- missing exact role codes, management-list endpoints, booking-command
  idempotency persistence, Location disclosure policy, request-interval
  snapshot evidence, and complete Kitchen/Space operating fields;
- over-broad master/integrated-architecture gate wording that could hide
  historical request/booking views after unpublish or pilot-authorization
  revocation;
- a stale integrated-architecture Space-equipment `POST` summary that now
  matches the canonical catalog-keyed `GET`/`PUT` contract; and
- wording that incorrectly put native P1 push outside the pilot instead of
  sequencing it after the web-first P0 while keeping SMS out of scope.

Valid long-term contexts retained and labelled as such include Payment,
`HELD`, EquipmentRental allocations, three independent web experiences, and
the broader future marketplace modules. `PAYMENT_PENDING` remains valid for
Food Order/payment state and is not a P1 KitchenBooking state. “Hourly rate”
remains valid inside an EquipmentRental or a `HOURLY` RentalOffer, not on
KitchenSpace.

The unchanged P1 product specification intentionally retains its
pre-reconciliation conflict table and statements such as “ADR-011 remains
Proposed.” Those passages are dated audit inputs that explicitly delegate their
resolution to P1-ARCH-01; they are not a competing post-reconciliation ADR
status or persistence/API contract. The standalone ADR and canonical
representation documents record the resolved state.

Final repository-search classifications:

- **FIX:** stale payment-coupled P1 booking, KitchenSpace pricing,
  availability, ADR-011 status, publication/requestability, equipment-mode,
  event, role/API/idempotency, and unauthenticated live-discovery statements
  listed above were reconciled in their owning canonical documents.
- **VALID LONG-TERM CONTEXT:** `HELD`, paid KitchenBooking concepts,
  EquipmentRental hourly price/allocation, Food Order `PAYMENT_PENDING`, and
  the three eventual web applications remain explicitly outside the bounded
  no-payment pilot path.
- **DEFERRED CONTEXT:** paid Kitchen-booking checkout, Space-availability
  occurrence materialization, unrestricted recurrence, equipment allocation,
  SMS, and independent `business-web`/`chef-web` deployment require their later
  owning decisions or slices. Native push is separately sequenced to the later
  P1 mobile slice and reuses the same domain events.
- **FALSE POSITIVE:** `BookingRequestSnapshot`,
  `KitchenBookingRequested.v1`, request-oriented HTTP/UI names, unrelated
  domains' status names, unrelated Proposed ADRs, and the protected P1
  product's historical conflict findings do not define a second aggregate or
  contradict the reconciled pilot model.

Final repository validation passed: `git diff --check` is clean; P1-MVP-01,
LP-01, `market-intelligence/`, and ADR-007 are unchanged; all changed paths are
documentation under `docs/` or `AGENTS.md`; no production source or migration
was created; nothing is staged; and branch `develop` plus baseline HEAD
`7f11a446de8890e0c1740ab2c075074ab348cf7f` remain unchanged.

### Quality-gate verification (36/36)

1. The P1 pilot product specification remains unchanged.
2. The LP-01 public credibility specification remains unchanged.
3. `market-intelligence/` remains unchanged.
4. `KitchenBooking` is the sole P1 request aggregate.
5. No parallel `BookingRequest` aggregate was introduced.
6. `REQUESTED` is non-reserving.
7. `CONFIRMED` is the P1 reserving state protected by the database.
8. Accepted ADR-007 remains authoritative and unchanged.
9. The P1 Kitchen booking workflow is explicitly no-payment.
10. The separate future paid-booking architecture remains possible.
11. `PAYMENT_PENDING` is excluded from the P1 KitchenBooking state machine.
12. RentalOffer is the sole P1 source of commercial price and terms.
13. KitchenSpace has no competing authoritative hourly-price fields.
14. One-time availability rules are canonical and DST-safe.
15. Weekly recurring availability rules are canonical and safely resolvable.
16. `BLOCKED` precedence and confirmed-booking conflict behavior are explicit.
17. Kitchen-local timezone authority and instant representation are explicit.
18. DST gaps are rejected and overlaps require an explicit offset.
19. DEMO records cannot be promoted, relabelled, or referenced by REAL data.
20. Kitchen publication and platform pilot authorization are distinct gates.
21. Exact address data is private; authenticated pilot discovery uses coarse
    location data.
22. Booking confirmation snapshots preserve historical commercial terms.
23. Booking transition history is append-only and independently auditable.
24. Equipment offering modes match the bounded pilot model.
25. Pilot feedback uses controlled categories and remains private.
26. Notification delivery failure cannot roll back booking state transitions.
27. Events conform to ADR-009 envelopes and ADR-016 causation/correlation.
28. Event payloads avoid private address, free text, and sensitive snapshots.
29. The bounded unified P1 web topology and its route ownership are explicit.
30. No Phase-2 feature was silently pulled into the pilot.
31. Existing Proposed ADRs remain Proposed; only ADR-011 was deliberately
    accepted, with ADR-024 and ADR-025 added as Accepted P1 decisions.
32. The master spec, architecture, ERD, API, events, ADRs, and agent guidance
    agree on every P1 architecture decision.
33. Repository-wide terminology searches have no unresolved FIX-classified
    contradiction.
34. All ten required acceptance flows are supported by the reconciled model.
35. The change set is documentation-only, with no production source or
    migration changes.
36. No unresolved architecture blocker remains for implementation planning.

### Acceptance-flow trace (10/10)

1. Operator registration, Organization/Location/Kitchen/Space setup, preview,
   publication, and independent pilot authorization are represented.
2. Authenticated Chef search and idempotent `REQUESTED` submission create no
   capacity or payment fact.
3. Authorized operator acceptance atomically records `CONFIRMED`, history,
   outbox evidence, and cleaning-aware capacity without financial records.
4. Concurrent overlapping acceptance has at most one confirmed winner; the
   loser receives `BOOKING_CONFLICT` and remains `REQUESTED`.
5. Decline uses a controlled reason, optional bounded message, history, and a
   safe notification event; a repeated decision conflicts.
6. Availability changes update future evaluation, warn pending requests, and
   cannot override a confirmed booking without explicit cancellation.
7. Chef dashboard/read contracts cover pending, confirmed, declined,
   withdrawn, cancelled, and time-derived past groupings.
8. Locale ownership, route continuity, externalized system copy, and immutable
   instant/money semantics support the `en-CA`/`fr-CA` flow.
9. Web and optional later native clients share one backend contract and one
   booking identity; native implementation remains sequenced after responsive
   web as the product specification permits.
10. Immutable scope, bidirectional query isolation, labelled fixtures, and a
    scoped DEMO-only reset prevent demo data from becoming or appearing REAL.

## 24. Remaining Blockers

NONE FOR IMPLEMENTATION PLANNING

Operational/legal R3 release gates remain real and must not be mistaken for
architecture blockers: explicit operator agreement and authority affirmation;
platform authorization; approved privacy/terms and proportionate privacy
assessment; retention/deletion and participant-rights policy; security and
support/runbook readiness; Quebec French review; approved stage copy/metadata;
notification/error monitoring; backup/restore and incident response; data
processing/service-provider disclosures; and no false validation/live-market
claim. These gate REAL controlled-pilot activation, not P1-IMP-01 planning.

## 25. Implementation-Readiness Verdict

**P1-ARCH-01 ARCHITECTURE RECONCILED — READY FOR IMPLEMENTATION PLANNING**

No remaining architectural decision would force an implementation-planning
agent to invent the aggregate, state machine, reservation rule, persistence
ownership, endpoint semantics, event payload, isolation boundary, privacy
boundary, or pilot client topology.

## 26. Recommended Implementation Slices

1. Foundation: module skeleton, data scope, pilot stage, User/ParticipantProfile,
   Auth0 mapping, permissions, audit, locale, and generated contract pipeline.
2. Operator organization: Organization membership, Location privacy model,
   Kitchen/Space lifecycle, publication, pilot authorization, and admin audit.
3. Offer/equipment: RentalOffer bases/validation and Space equipment modes.
4. Availability: operating constraints, one-time/weekly available/blocked
   rules, ADR-011 DST resolution, and requestability evaluator.
5. Chef discovery: ChefProfile/categories, safe search/detail, coarse area,
   estimates/disclaimers, and protected address boundary.
6. Request submission: idempotency, normalized declarations, immutable
   snapshots/history, REQUESTED state, and requested event.
7. Operator inbox/decision: authorization, concurrent confirm, decline,
   withdrawal/accept race, ADR-007 GiST, conflict mapping, and audit.
8. Booking management: upcoming/past views, confirmed address disclosure,
   cancel/release, and transition history.
9. Notifications/feedback: durable in-app/email delivery, retry/idempotency,
   bounded private feedback, and minimal admin inspection.
10. Pilot hardening: DEMO fixtures/reset, full architecture test matrix,
    bilingual/accessibility/security review, observability, backup/restore,
    runbook, and R0–R3 release gates.

Each slice must update migration, implementation, automated tests,
OpenAPI/AsyncAPI artifacts, authorization checks, and operational evidence
together. Responsive web is completed before optional native P1 clients; all
clients use the same backend contracts.
