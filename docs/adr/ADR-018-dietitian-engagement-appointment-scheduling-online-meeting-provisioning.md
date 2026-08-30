# ADR-018: Dietitian Engagement, Appointment Scheduling and Online Meeting Provisioning

## Status

Proposed

## Context

Cheffy Bites supports Dietitian professional services that may be delivered independently or through an authorized Organization or clinic. The architecture must support a one-time consultation without requiring ongoing care, while also supporting longer professional relationships, recurring offered availability, safe appointment scheduling, rescheduling, cancellation, no-show classification, in-person locations, and online meeting provisioning.

These concerns have related but different identities, lifecycles, authority, concurrency requirements, privacy boundaries, and failure modes. A person who is professionally authorized to practice is not an Appointment. A catalog offering is not the concrete occurrence booked from it. A Customer-Dietitian relationship is not a subscription. Payment collection is not service delivery. A provider-created meeting is not the Appointment and must not become Cheffy's scheduling source of truth.

ADR-017 owns the durable actual Dietitian professional identity, professional-to-Organization authorization, credential status, jurisdiction eligibility, and historical professional attribution. This ADR consumes those decisions; it does not redefine professional identity or introduce another generic Professional, Provider, or Staff aggregate.

The scheduling design must prevent two concurrent Cheffy workflows from double-booking the same Dietitian. Application-only read-before-write checks are insufficient. PostgreSQL remains the transactional system of record, and the proven database-enforced interval-exclusion principle from ADR-007 is applicable even though Dietitian Appointment scheduling is a separate domain concern.

External calendar and online meeting providers are useful integrations but are not transaction participants in Cheffy's PostgreSQL commit. Provider outages, duplicate delivery, or retries must not corrupt authoritative Appointment state. The Accepted transactional-outbox architecture provides the required asynchronous boundary.

This ADR establishes conceptual aggregate and integration boundaries. It does not finalize SQL, tables, columns, API endpoints, event names or payloads, provider selection, legal jurisdiction rules, cancellation fee calculations, earning recognition, or payout eligibility.

## Decision

### 1. Core Domain Separation

Cheffy Bites will preserve these domain distinctions:

```text
DIETITIAN PROFESSIONAL IDENTITY
!= DIETITIAN CLIENT ENGAGEMENT
!= CONSULTATION OFFERING
!= APPOINTMENT
!= PAYMENT
!= ONLINE MEETING
```

Each concept answers a separate question:

- **DietitianProfessionalProfile:** Who is the actual practicing Dietitian?
- **DietitianClientEngagement:** What ongoing or bounded professional relationship exists between this Customer and this actual Dietitian?
- **ConsultationOffering:** What versioned professional service is offered for booking?
- **Appointment:** What concrete scheduled professional-service occurrence was reserved?
- **Payment:** What provider-neutral Financial collection workflow exists for an approved billable context?
- **OnlineMeeting:** What external-meeting provisioning resource is associated with an eligible online Appointment?

The concepts may reference one another where appropriate, but they are not one aggregate and do not share one universal lifecycle.

The actual performer, Organization context, and future commercial parties also remain separate:

```text
ACTUAL DIETITIAN PERFORMER
!= ORGANIZATION
!= COMMERCIAL PROVIDER
!= SETTLEMENT BENEFICIARY
```

### 2. Conceptual Ownership

ADR-018 owns the architecture for:

- `DietitianClientEngagement`;
- `ConsultationOffering`;
- `Appointment` and its scheduling lifecycle;
- professional offered availability and one-off additions or blocks;
- temporary scheduling holds and their expiry;
- Dietitian Appointment overlap protection;
- time-only rescheduling and material-change classification;
- operational cancellation, completion, and no-show classification;
- `ConsultationLocation`;
- external-calendar free/busy input; and
- provider-neutral `OnlineMeeting` provisioning.

ADR-018 does not own:

- Dietitian identity, credentials, eligibility, or professional-to-Organization authorization, which remain ADR-017 concerns;
- Payment and Refund orchestration, which remain ADR-012 concerns;
- PricingSnapshot, Tax, Promotion, ledger, earning, payout, or settlement rules;
- subscription lifecycle or subscription fulfillment;
- Conversation or Message persistence;
- review, reputation, or reliability scoring; or
- clinical/EMR records.

The owning capabilities coordinate through explicit modular-monolith application interfaces and transactional-outbox integration where asynchronous work is justified. One capability must not duplicate another capability's authoritative facts.

### 3. DietitianClientEngagement

`DietitianClientEngagement` is a first-class professional relationship between exactly one Customer and exactly one actual `DietitianProfessionalProfile`. It is not merely a Customer-to-Organization relationship. An applicable Organization context may be referenced, but it never replaces the actual Dietitian party.

Supported conceptual engagement types include:

```text
SINGLE_CONSULTATION
ONGOING_CARE
CARE_PROGRAM
```

Equivalent final names may be chosen in canonical contract work, but all three product meanings must remain representable.

The minimal conceptual engagement lifecycle is:

```text
ACTIVE → COMPLETED
ACTIVE → TERMINATED
```

The model preserves the effective start, applicable end, the authorized ending actor where policy requires it, and a completion or termination reason where appropriate. Actor relationships must remain typed and authorized; this requirement does not approve an unconstrained actor type plus arbitrary UUID.

Either authorized side may terminate an engagement according to product and professional policy. A Dietitian may mark the engagement complete where the bounded care or program objective is complete. Completion and termination do not delete history and do not retroactively change individual Appointment outcomes.

An engagement may contain or reference:

- one or more Appointments;
- versions of `DietitianMealPlan`; and
- an authorization context for Customer-Dietitian professional interaction.

After completion or termination and any configured grace period, policy may restrict new professional actions while retaining historical access according to authorization, privacy, and retention rules. ADR-021 owns future Conversation, Participant, Message, write-access, read-only, and retention architecture; this ADR creates none of those schemas.

The relationship is explicitly not a subscription:

```text
DIETITIAN CLIENT ENGAGEMENT
!= SUBSCRIPTION
!= MEAL SUBSCRIPTION
```

No recurring billing, entitlement, renewal, pause, or subscription occurrence is implied by an engagement.

### 4. Single-Consultation Booking

A Customer does not need to subscribe to ongoing care or create an artificial long-lived care relationship to book one consultation. When product rules require an engagement context, the ordinary booking workflow may create or associate a `SINGLE_CONSULTATION` engagement.

That engagement may be bounded to one Appointment and completed according to policy, but it remains conceptually distinct from the Appointment. A failed, cancelled, or rescheduled Appointment does not silently rewrite the engagement into another product type.

### 5. ConsultationOffering

`ConsultationOffering` is the Dietitian-owned, versioned professional-service catalog definition. It identifies the actual `DietitianProfessionalProfile`, not only an Organization. It may also identify an authorized Organization or service context when the Dietitian offers the service through that context.

Supported service modes are at least:

```text
ONLINE
IN_PERSON
```

Conceptual offering terms may include:

- actual Dietitian professional identity;
- authorized Organization/service context where applicable;
- service mode;
- Customer-facing service duration;
- currency and Customer price;
- active or inactive state;
- cancellation, no-show, and rescheduling policy reference/version;
- allowed location and jurisdiction constraints;
- scheduling buffers;
- booking horizon; and
- minimum booking lead time where approved product policy requires it.

An offering is not a concrete reserved occurrence. Versioning must allow an Appointment to preserve or reference the terms accepted at booking rather than interpreting a historical service only through the current offering configuration.

An active professional-to-Organization engagement under ADR-017 establishes authorization to perform through the Organization. It does not automatically determine that the Organization is the commercial provider or settlement beneficiary. Those commercial decisions remain ADR-020 work.

### 6. Appointment

`Appointment` is the concrete scheduled professional-service occurrence and the approved non-Order billable context for a Dietitian consultation. It must durably retain or reference enough typed and historical evidence to identify:

- the Customer;
- the actual `DietitianProfessionalProfile` performer;
- the applicable `ConsultationOffering` and accepted version/terms;
- the service mode;
- scheduled start instant;
- service end instant;
- occupancy end or scheduling-block end when buffers apply;
- applicable Organization context, if any;
- applicable `ConsultationLocation` for in-person service;
- lifecycle status and operational cancellation/no-show classification;
- accepted cancellation/no-show/rescheduling policy evidence; and
- immutable PricingSnapshot and Promotion evidence required by the owning Pricing, Promotion, and Financial architecture.

This is a conceptual requirement, not a final column list or physical schema.

The actual Dietitian performer is durable Appointment history. Historical attribution must not be reconstructed from current Organization staff, membership, or professional-to-Organization engagement. If Dietitian D later leaves Clinic A, Appointment A1 performed by D through Clinic A remains attributable to D and retains the applicable historical clinic/service context.

An Appointment is neither a food Order nor a KitchenBooking:

```text
DIETITIAN APPOINTMENT
!= FOOD ORDER
!= KITCHEN BOOKING
```

Cheffy must not fabricate an Order, ChefOrderGroup, or food relationship to collect payment for a Dietitian service.

### 7. Confirmation Eligibility and Historical Decision Evidence

Before confirmation, the Appointment workflow must obtain the applicable ADR-017 authorization decision and validate at least:

1. the `DietitianProfessionalProfile` is active;
2. required credentials satisfy the approved service policy;
3. jurisdiction eligibility is current for the service mode, applicable service context, and relevant time; and
4. the professional-to-Organization engagement is effective and `ACTIVE` when the Appointment is provided through an Organization.

Appointment scheduling must preserve this distinction:

```text
AVAILABLE TIME != PROFESSIONALLY ELIGIBLE
PROFESSIONALLY ELIGIBLE != AVAILABLE TIME
```

Both capacity and eligibility are required for confirmation. A calendar opening cannot authorize professional practice, and professional authorization cannot reserve time.

The Appointment must preserve enough durable decision evidence or typed historical references to explain the confirmation-time performer, Organization context, policy, credential/eligibility outcome, and jurisdiction context. A future profile, credential, eligibility, or Organization-engagement change governs future decisions but does not silently rewrite a completed Appointment.

Material changes or reschedules that can change the applicable jurisdiction or performer require eligibility re-evaluation. Examples include changing an in-person `ConsultationLocation`, changing the online Customer/service jurisdiction, replacing the Dietitian, or changing service type. ADR-018 does not invent the legal rules used by that decision.

### 8. Appointment Lifecycle

The minimal conceptual lifecycle supports states equivalent to:

```text
HELD
CONFIRMED
COMPLETED
CANCELLED
NO_SHOW
```

An expiry outcome or other narrowly necessary operational state may be added for a temporary hold, but the Appointment lifecycle must remain focused on professional-service scheduling and delivery. It must not become a giant lifecycle shared with KitchenBooking, Payment, OnlineMeeting, or external providers.

- **HELD:** Time is temporarily protected during an active confirmation or checkout workflow. The professional service is not confirmed.
- **CONFIRMED:** Cheffy's local capacity, eligibility, authorization, and required commercial confirmation workflow has established a valid Appointment.
- **COMPLETED:** The professional service was actually delivered according to the Appointment completion policy.
- **CANCELLED:** The Appointment was ended before delivery through a formal cancellation operation, with cause and actor classification preserved.
- **NO_SHOW:** The scheduled service was not delivered because the Customer or provider/professional did not attend, with the responsible side explicitly classified.

Payment and meeting states are not Appointment states:

```text
PAYMENT SUCCESS != APPOINTMENT COMPLETED
ONLINE MEETING PROVISIONED != APPOINTMENT COMPLETED
```

Structured booking, cancellation, completion, rescheduling, and no-show operations drive state. Chat text, provider calendar state, Payment success, or meeting provisioning alone cannot perform those transitions.

### 9. Offered Availability

Dietitian availability is offered availability: it indicates when the Dietitian is willing to accept Cheffy bookings under specified constraints. It is not a concrete Appointment, capacity guarantee, eligibility decision, or requirement to expose the Dietitian's full private-practice calendar.

The scheduling model supports conceptually:

- recurring weekly/local offered schedules;
- mode-specific availability where needed;
- `ConsultationLocation`-specific availability where needed;
- one-off additions;
- one-off blocks or unavailability;
- external-calendar busy intervals;
- pre-session and post-session buffers;
- booking horizons; and
- minimum lead times where approved policy requires them.

A block affects prospective availability. It must not silently cancel, replace, or delete an already confirmed Appointment. Resolving that conflict requires an explicit operational workflow and, where necessary, cancellation/remediation.

Recurring rules are planning inputs expressed with business-local schedule semantics. They are not expanded into infinite future rows and do not themselves reserve every future recurrence. A Customer-selected slot is resolved into concrete real instants before it can become a held or confirmed Appointment.

### 10. Timezone and Materialized Instants

ADR-011 remains authoritative for temporal semantics and daylight-saving behavior.

For recurring online availability, the Dietitian may have an authoritative professional scheduling timezone represented by an IANA timezone identifier. For recurring in-person availability, the applicable `ConsultationLocation` timezone is authoritative. Organization headquarters, server default, JVM default, browser timezone, and device timezone are not silently authoritative scheduling zones.

Concrete Appointment times are real instants. The architecture distinguishes:

```text
APPOINTMENT START
SERVICE END
OCCUPANCY END
```

For example, a 60-minute Customer consultation may have a 15-minute post-session buffer. The Customer service ends after 60 minutes, while Dietitian capacity remains occupied until the later occupancy end. A pre-session buffer may similarly extend the effective occupancy start when policy requires it.

```text
SERVICE END MAY DIFFER FROM OCCUPANCY END
```

Buffer occupancy is not automatically billable Customer service time. Price comes from the accepted ConsultationOffering/Pricing decision and must not be derived directly from the occupancy interval.

Local schedule resolution, DST gaps, and DST overlaps follow ADR-011. Nonexistent local times are rejected rather than silently shifted, and ambiguous local times require sufficient offset/context rather than an arbitrary guess. Once materialized, historical Appointment instants are not rewritten because a professional or location timezone later changes.

### 11. ConsultationLocation

`ConsultationLocation` is a first-class professional-service location for in-person ConsultationOfferings and Appointments. It is not a free-text city label and is not the Organization itself.

Conceptually, it supports:

- normalized location identity;
- structured address information;
- city or locality;
- region, province, or state;
- country;
- geographic coordinates where discovery requires them;
- authoritative IANA timezone;
- active or inactive state; and
- applicable Organization/location context.

PostgreSQL/PostGIS remains the MVP baseline for normalized geographic discovery. OpenSearch is not introduced solely for Dietitian discovery. Specialty and taxonomy governance remains ADR-022 work.

Customer-facing discovery need not expose every exact private-practice address before booking. Product and privacy policy may expose a city or general area before confirmation and exact directions or address only after an authorized confirmed booking. This ADR does not finalize exact address redaction, disclosure timing, retention, or private-location policy.

An online Appointment does not require an in-person `ConsultationLocation`. It may still require approved Customer/service-location context for jurisdiction eligibility. The Dietitian's timezone is not regulatory jurisdiction and must not be used as a substitute for ADR-017 eligibility policy.

### 12. Temporary Scheduling Holds

The scheduling workflow supports an optional temporary hold before confirmation. A hold protects the applicable Dietitian occupancy interval while checkout or confirmation is in progress.

A hold:

- has an explicit expiry instant;
- reserves capacity only while active and unexpired;
- is not a confirmed Appointment;
- does not imply successful Payment;
- may be explicitly released when abandoned; and
- must become non-reserving when it expires.

```text
HELD != CONFIRMED
SCHEDULING HOLD != PAYMENT AUTHORIZATION
```

The implementation must transition or otherwise remove expired holds from the database-enforced capacity-reserving set deterministically, through on-demand expiry and/or an expiry process. Stale holds may not remain capacity-authoritative indefinitely. Expiry processing is idempotent and preserves the required historical/audit evidence.

Capacity checks include active, unexpired `HELD` intervals and `CONFIRMED` intervals. Redis, an in-memory timer, a distributed lock, or a long-lived card authorization is not the source of truth for the hold.

Scheduling hold and Payment workflows are coordinated but separate. Payment success does not override expired capacity, overlap protection, or eligibility. If provider and local outcomes diverge, ADR-012 and future ADR-020 govern the idempotent Financial compensation/remediation workflow; this ADR does not create a distributed transaction or invent a refund amount.

### 13. Database-Enforced Overlap Protection

PostgreSQL is authoritative for preventing double booking of the same actual Dietitian. The canonical persistence design must use a transactional database constraint based on interval exclusion, following the proven principle used by ADR-007 for exclusive Kitchen Space occupancy.

The capacity interval is conceptually half-open:

```text
[effective_occupancy_start_at, occupancy_end_at)
```

The effective occupancy start may include a pre-session buffer. The occupancy end may include service duration plus post-session buffer. Half-open semantics permit back-to-back occupancy when one interval ends exactly as another begins.

The exclusion applies per actual Dietitian professional identity to all capacity-reserving Appointment representations/states, including active unexpired `HELD` and `CONFIRMED` Appointments. The later canonical ERD will select exact columns, ranges, predicates, expiry representation, constraints, and indexes.

The correctness mechanism is database-enforced exclusion, not:

- an availability response generated earlier;
- an application read followed by an unprotected write;
- advisory locking;
- Redis/distributed locking; or
- external calendar conflict detection.

Application checks remain useful for Customer experience but are non-authoritative. Constraint conflicts must become safe domain errors and preserve idempotency.

ADR-007 remains authoritative only for Kitchen Space, EquipmentRental, KitchenBooking, and its approved amendments. ADR-018 reuses the interval-exclusion architectural pattern but independently owns Dietitian Appointment concurrency. Appointment is not a KitchenBooking subtype, and no universal Booking or BookableResource aggregate is introduced.

### 14. Confirmation Atomicity and External Boundaries

Within Cheffy's PostgreSQL boundary, confirmation atomically persists:

- the authoritative local Appointment transition;
- the local capacity-reserving representation required for overlap protection;
- applicable local authorization, policy, and audit evidence; and
- transactional-outbox records for required asynchronous work.

If the local transaction fails, no partial confirmed Appointment or partial capacity reservation commits. Provider calls do not occur inside this transaction.

Payment providers, online meeting providers, external calendar providers, and notification providers are not participants in the PostgreSQL transaction. Work across those boundaries uses idempotent application orchestration, transactional outbox, authenticated callbacks where applicable, retries, reconciliation, and explicit failure states rather than distributed ACID transactions.

### 15. Time-Only Rescheduling

A reschedule that preserves the same substantive ConsultationOffering and service terms while changing only time normally preserves the same Appointment business identity.

A valid time-only reschedule must:

1. validate the Appointment and actor are eligible for rescheduling;
2. validate the requested slot against offered availability and applicable policy;
3. recheck professional/jurisdiction eligibility where the applicable time or context requires it;
4. establish non-overlapping replacement capacity using database-safe overlap protection;
5. preserve original capacity until the replacement can safely commit;
6. atomically establish the new schedule and release the old interval where practical;
7. append immutable reschedule history and actor/reason evidence;
8. preserve accepted Pricing, Promotion, and policy context unless an approved policy requires re-evaluation; and
9. request any applicable external calendar, meeting, and notification updates asynchronously.

An atomic update or equivalent database-safe replacement operation must leave the original confirmed Appointment and occupancy intact if the new interval conflicts or any required local validation fails. The workflow must not cancel or release the original first and then attempt to reserve the replacement.

The Appointment's prior schedule is retained in immutable audit/reschedule evidence; changing current scheduled instants must not erase what was originally confirmed or who requested the change.

Customer-requested rescheduling follows the policy accepted for the Appointment. Dietitian-requested rescheduling must be classified separately so downstream policy can ensure it does not consume a Customer allowance or penalize the Customer. If an approved replacement cannot be established, the original remains valid unless a separate formal provider-cancellation operation occurs.

### 16. Promotion Behavior on Reschedule

A time-only reschedule of the same Appointment is not a new acquisition or first-use event:

```text
RESCHEDULE != NEW FIRST-USE PROMOTION
```

The original valid Promotion context is preserved where the captured campaign/domain policy permits. A second first-consultation or first-use benefit must not be issued merely because the service time changed.

ADR-014 remains authoritative for Promotion eligibility, evaluation, application, redemption, material-change revalidation, and provider-failure restoration. ADR-018 only classifies the scheduling change and supplies the relevant Appointment context.

### 17. Material Service Change

A change that materially changes the contracted service may require cancellation and rebooking, modification/rebook, or equivalent re-contracting rather than an in-place time reschedule.

Examples include:

- a different ConsultationOffering;
- a different Dietitian;
- materially different service duration;
- online to in-person where terms, price, policy, location, or eligibility materially changes;
- in-person to online where those terms materially change; or
- a different ConsultationLocation or service jurisdiction when it materially changes the service.

Material change may require new eligibility validation, Pricing, Promotion evaluation, Payment/remediation behavior, and accepted policy evidence. ADR-014 owns Promotion re-evaluation; ADR-012 owns Payment/Refund orchestration; ADR-020 will own commercial obligations and remediation. ADR-018 does not force every material change into the in-place reschedule path.

### 18. Cancellation and Capacity Release

Cancellation is a formal Appointment operation that records who caused or requested it, when it became effective, and the applicable policy classification. It does not delete the Appointment.

When a cancellation becomes effective, the future capacity-reserving interval is released coherently in the same local state transition. Historical Appointment, performer, Customer, schedule, policy, Pricing, Promotion, cause, actor, and audit evidence remain available according to policy.

Customer cancellation, Dietitian/provider cancellation, Customer no-show, and Dietitian/provider no-show remain distinct operational outcomes. They must not be inferred only from Payment, calendar, or meeting-provider state.

### 19. Cancellation and No-Show Policy Evidence

An Appointment must preserve enough immutable evidence to apply the cancellation, no-show, and rescheduling policy accepted at booking time. A policy/version reference or equivalent immutable historical evidence is required.

Historical consequences must not be calculated solely from the current ConsultationOffering configuration. Updating an offering's cancellation window or fee policy cannot rewrite what the Customer accepted for an existing Appointment.

ADR-018 owns operational classification and Appointment state. It does not calculate ledger entries, marketplace earning, payout eligibility, subsidy reversal, or final commercial remediation.

### 20. Customer Cancellation

Customer cancellation consequences depend on the accepted policy and timing. Policy may lead to a full refund, partial refund, cancellation charge, or no refundable amount, subject to approved consumer and professional-service rules.

ADR-018 records the formal cancellation, responsible side, timing, and policy classification. ADR-020 owns the resulting commercial obligation, earning/remediation, refundability, and payout-eligibility decision. ADR-012 owns provider-neutral Refund orchestration. ADR-015 owns immutable ledger posting and reconciliation.

A Platform Promotion or subsidy does not automatically fund cancellation penalties, no-show penalties, or Customer misconduct charges unless an explicit Promotion and commercial policy permits it:

```text
PLATFORM SUBSIDY != AUTOMATIC PENALTY FUNDING
```

ADR-014 remains authoritative for that applicability.

### 21. Dietitian or Provider Cancellation

When the professional/provider cancels and the Customer cannot receive the confirmed service, ADR-018 records provider-caused nonperformance and releases future capacity through the explicit cancellation workflow.

The canonical product direction is that the Customer receives full appropriate refund/remediation, bears no cancellation penalty, and the provider does not earn for the unprovided service. Unused Platform subsidy is released or reversed according to Financial policy, and Promotion restoration follows ADR-014's captured provider-nonperformance policy.

ADR-018 supplies the operational cause and evidence. ADR-020 owns the final earning, obligation, remediation, refundability, and payout-eligibility rules; ADR-012 orchestrates any approved Refund; ADR-015 records Financial postings. No ledger posting or final amount is calculated here.

### 22. Customer and Dietitian No-Show

No-show is distinct from cancellation and must identify the responsible side:

```text
CUSTOMER NO-SHOW != DIETITIAN NO-SHOW
NO-SHOW != COMPLETED PROFESSIONAL SERVICE
```

A Customer no-show follows the no-show policy captured at booking. ADR-018 records the `NO_SHOW` Appointment outcome and Customer-responsible classification. It does not automatically treat the professional service as completed or decide commercial recognition.

A Dietitian/provider no-show is provider-caused nonperformance. The canonical product direction is that the Customer receives full appropriate refund/remediation, the professional/provider does not earn for service not delivered, and Promotion restoration may follow provider-nonperformance policy. ADR-018 records the provider-responsible classification; ADR-020 owns final economics and ADR-014 owns Promotion restoration policy.

Review, reliability, scoring, and reputation consequences are not designed here. ADR-023 owns that architecture.

### 23. Completed Appointment

`COMPLETED` means the scheduled professional service was actually delivered according to the approved Appointment completion policy. It must be attributable to the actual Dietitian performer.

An Organization administrator cannot perform or complete the Appointment as the professional, or appear as the reviewed Dietitian, unless that person independently holds the required `DietitianProfessionalProfile`, credentials, eligibility, and authorization for that service context. Organization operational settings and administrative authority do not create professional performer authority.

Payment success confirms only a Financial collection outcome. Meeting provisioning confirms only an integration resource outcome. Neither completes professional service.

A completed Appointment is expected to provide verified-experience evidence for future Dietitian review eligibility. ADR-023 owns review storage, moderation, scoring, aggregation, and reputation.

### 24. External Calendar Boundary

External calendars such as Google Calendar or Microsoft calendars may provide free/busy input. Cheffy should consume the minimum information required to identify busy intervals and should not import private event titles, descriptions, participants, or arbitrary event content unless a separately approved requirement needs it.

External busy intervals are normalized to real instants under ADR-011 and may constrain offered availability according to freshness and integration policy. They do not become Appointments, professional availability policy, or Cheffy's transaction log.

Cheffy remains authoritative for:

- ConsultationOffering;
- offered professional availability policy;
- active scheduling holds;
- confirmed Appointment;
- rescheduling;
- cancellation and no-show state; and
- Customer booking history.

```text
EXTERNAL CALENDAR != APPOINTMENT SOURCE OF TRUTH
```

A calendar-provider outage or sync failure must not cancel, downgrade, or corrupt a confirmed Cheffy Appointment. It may produce retry, an operational alert, stale-input handling, or reconciliation work. A blocked external interval also must not silently overwrite or delete a confirmed Appointment.

Outbound calendar synchronization is eventually consistent. Duplicate delivery must be idempotent, and an external event ID must not become the Appointment identity.

### 25. Provider-Neutral OnlineMeeting

`OnlineMeeting` is a provider-neutral local resource/integration associated with an eligible confirmed online Appointment. Zoom, Google Meet, Microsoft Teams, or another approved provider is implemented behind a meeting-provider adapter and is not part of the core Appointment domain model.

A confirmed online Appointment may request meeting provisioning. Provisioning begins asynchronously only after the relevant Appointment confirmation state commits. The confirmation transaction writes the applicable transactional-outbox record; provider invocation occurs afterward.

External meeting API success is never part of the Appointment database transaction. The provider's meeting identifier is integration evidence and not the Appointment identity.

The conceptual provisioning lifecycle supports states equivalent to:

```text
PENDING
PROVISIONED
FAILED_RETRYABLE
FAILED_FINAL
CANCELLED
```

Conceptual OnlineMeeting data may include:

- typed Appointment reference;
- provider-neutral meeting reference;
- provider identifier;
- Customer join-access metadata;
- Dietitian host/professional-access metadata where needed;
- provisioning state and timestamps; and
- bounded failure, retry, and reconciliation metadata.

Exact tables, columns, encryption representation, state transitions, provider selection, API representations, and event payloads remain later implementation and canonical-contract work.

### 26. Online Meeting Security and Privacy

Meeting access data is sensitive. Cheffy must protect it at rest and in transit according to approved security policy, expose it only to authorized Appointment participants and operational support roles, and prevent it from appearing in logs, analytics, or public responses.

The Customer receives only the join information appropriate to the Appointment. The Dietitian receives only the professional/host access needed for delivery. Provider API credentials, host tokens, reusable provider secrets, and adapter credentials are never returned through public APIs.

Automatic recording, recording storage, transcription, and transcript analysis are out of scope for MVP. They must not be enabled by default. Any future recording or transcription requires separate product, privacy, consent, retention, security, and clinical/professional-policy decisions.

### 27. Meeting Provisioning Failure and Reschedule

A meeting-provider failure after Appointment confirmation does not erase, cancel, or silently downgrade the Appointment. Retryable failures use asynchronous retry with idempotency. A final failure creates an explicit operational state and support/reconciliation path.

Approved fallback policy may allow another retry, an alternate provider, manually supplied authorized meeting details, or Customer/Dietitian support intervention. This ADR does not choose the automatic fallback order and does not invent an automatic refund solely because a meeting API failed. ADR-020 owns commercial remediation policy.

For a time-only reschedule of the same eligible online Appointment, the adapter supports updating or replacing the external meeting as provider capabilities require while preserving the Appointment's business identity. Old access must be invalidated or cancelled when required by security and provider behavior. Duplicate reschedule events must not create uncontrolled duplicate meetings.

Cancellation of an online Appointment asynchronously requests cancellation or invalidation of the associated external meeting. External cancellation failure does not reverse the authoritative local Appointment cancellation; it creates retry/reconciliation work and requires access-risk handling.

### 28. Integration Events, Outbox, and Idempotency

Important lifecycle changes may later produce events conceptually equivalent to:

- Appointment confirmed;
- Appointment rescheduled;
- Appointment cancelled;
- Appointment completed;
- Appointment no-show classified;
- online meeting provisioning requested;
- online meeting provisioned; and
- online meeting provisioning failed.

These examples are not final event names or payloads. Exact contracts belong to `docs/05-event-contracts.md` and must follow ADR-016's event-versioning rules.

When an event is required, the Appointment state change and outbox record commit in the same local PostgreSQL transaction. Consumers and adapters are idempotent and retry-safe. Duplicate outbox delivery must not create uncontrolled duplicate meetings, duplicate calendar events, contradictory Appointment transitions, or repeated side effects.

Meeting and calendar operations use stable Cheffy operation identity and provider idempotency support where available. Provider identifiers are retained as integration evidence but do not replace Cheffy's idempotency, Appointment identity, or reconciliation state.

### 29. Payment, Pricing, Promotion, and Financial Boundaries

Appointment is an approved non-Order billable commercial context. ADR-018 may initiate or reference an authorized Financial workflow, but it does not own `Payment`, `PaymentAttempt`, `Refund`, provider interaction, financial command idempotency, ledger, earning, payout, or settlement.

ADR-012 remains authoritative for Payment and Refund orchestration. Payment state remains separate from Appointment state. No fake food Order is created.

PricingSnapshot remains the immutable pricing authority. Appointment booking retains or references the pricing evidence required to explain Customer price, applicable Promotion, Platform subsidy, fees, tax where applicable, and later refund/remediation. This ADR introduces neither `AppointmentFinancialSnapshot` nor `FinancialSnapshot`.

ADR-006 owns typed Promotion owner and target identity. ADR-014 owns eligibility, evaluation, application, redemption, compatibility, material-change revalidation, penalty applicability, and provider-failure restoration. ADR-018 does not duplicate a Promotion engine.

ADR-015 owns append-only ledger posting and reconciliation. ADR-020 will own commercial obligations, earning recognition, unfulfilled value, refundability, remediation, settlement beneficiary, and payout eligibility. Operational Appointment outcomes are inputs to those decisions; they are not ledger calculations.

### 30. Dietitian Professional-Service Simplification

Dietitian Appointment economics remain professional-service economics only. This ADR does not introduce or reintroduce:

- `DietitianChefAssociation`;
- a Dietitian-Chef commercial agreement;
- Dietitian food-sale commission;
- Meal Subscription commission;
- referral commission;
- Chef-purchase attribution; or
- a Dietitian deduction from Chef proceeds.

A `DietitianMealPlan` may belong to or be associated with a `DietitianClientEngagement`, but it remains private professional Customer guidance. It is not a ConsultationOffering, Appointment, ChefMealPlan, MealSubscription, Chef-selection mechanism, Promotion-attribution source, or food-sale commission source. Detailed MealPlan schema is outside this ADR.

### 31. Clinical, Conversation, and Review Boundaries

Neither Appointment nor DietitianClientEngagement is an electronic medical record. This ADR does not model diagnoses, medication lists, laboratory results, clinical charts, medical-record interchange, insurance claims, prescriptions, clinical notes, or professional record retention. Introducing professional notes or clinical records requires separate privacy, legal, security, consent, access, and product decisions.

A confirmed Appointment or active professional engagement may become an authorization context for future Customer-Dietitian communication. ADR-021 owns Conversation, Participant, Message, message retention, post-engagement grace, and write-access architecture.

A completed Appointment may become verified-experience evidence for a future Dietitian review. ADR-023 owns review eligibility implementation, storage, moderation, scoring, aggregation, and reputation.

### 32. Modular-Monolith and Typed-Relationship Boundaries

This architecture remains inside the ADR-001 modular monolith. It introduces no Appointment, Dietitian scheduling, meeting, or calendar microservice and no separate database.

Shared temporal, interval, provider-adapter, idempotency, or scheduling utilities are acceptable where genuinely generic. Domain aggregates remain distinct:

```text
APPOINTMENT != KITCHEN BOOKING
APPOINTMENT != EQUIPMENT BOOKING
APPOINTMENT != SUBSCRIPTION OCCURRENCE
```

No universal `Booking`, `Reservation`, `BookableResource`, or `AvailabilityResource` aggregate is approved. No universal `Provider`, `Professional`, or `Staff` aggregate is introduced. ADR-017's role-specific `DietitianProfessionalProfile` remains the actual typed professional identity.

Canonical core relationships must not use unconstrained `entity_type + entity_id`, `source_type + source_id`, arbitrary UUID metadata, or JSONB as a substitute for typed relational integrity. Exact typed relationships belong to the later canonical ERD.

ADR-010 remains authoritative for identifier direction. This ADR introduces no new UUID strategy and does not infer business chronology from UUID ordering.

## Detailed Invariants

1. `DIETITIAN PROFESSIONAL != ORGANIZATION`.
2. `DIETITIAN CLIENT ENGAGEMENT != APPOINTMENT`.
3. `CONSULTATION OFFERING != APPOINTMENT`.
4. `APPOINTMENT != PAYMENT`.
5. `APPOINTMENT != ONLINE MEETING`.
6. `AVAILABLE != ELIGIBLE`.
7. `HELD != CONFIRMED`.
8. `PAYMENT SUCCESS != APPOINTMENT COMPLETED`.
9. `ONLINE MEETING PROVISIONED != APPOINTMENT COMPLETED`.
10. `EXTERNAL CALENDAR != APPOINTMENT SOURCE OF TRUTH`.
11. `CUSTOMER NO-SHOW != DIETITIAN NO-SHOW`.
12. `RESCHEDULE != NEW FIRST-USE PROMOTION`.
13. Service end may differ from occupancy end.
14. `DIETITIAN CLIENT ENGAGEMENT != SUBSCRIPTION`.
15. `DIETITIAN APPOINTMENT != FOOD ORDER`.
16. A DietitianClientEngagement always identifies one Customer and one actual DietitianProfessionalProfile.
17. A ConsultationOffering and Appointment identify the actual Dietitian, even when an Organization context applies.
18. Current Organization staffing cannot determine a historical Appointment performer.
19. A recurring or one-off availability rule is not a concrete Appointment.
20. Active unexpired HELD and CONFIRMED intervals reserve Dietitian capacity.
21. Expired or released holds do not reserve capacity.
22. Dietitian overlap correctness is enforced by PostgreSQL interval exclusion, not application checks, advisory locks, Redis, or external calendars.
23. A failed time-only reschedule leaves the original valid Appointment and capacity intact.
24. Reschedule history is append-preserved and auditable.
25. Cancellation releases future capacity without deleting Appointment history.
26. Customer cancellation, provider cancellation, Customer no-show, and Dietitian/provider no-show remain distinguishable.
27. Payment, Pricing, Promotion, and OnlineMeeting state do not substitute for Appointment lifecycle state.
28. Provider calls never participate in the Appointment PostgreSQL transaction.
29. Meeting and calendar retries are idempotent and reconciliation-capable.
30. Online Appointment jurisdiction is not inferred from the Dietitian's timezone.
31. Scheduling buffer does not automatically become Customer service duration or billable time.
32. Organization administration does not confer Dietitian performer authority.
33. Appointment relationships use typed domain references rather than unconstrained type-plus-UUID links.

## Out of Scope

This Proposed ADR does not decide:

- final tables, columns, foreign keys, range types, indexes, exclusion predicates, or migration SQL;
- exact API endpoints, request/response fields, error codes, or OpenAPI schemas;
- exact event names, payloads, topics, consumers, or publication rules;
- external calendar or meeting provider selection;
- exact hold duration, booking horizon, minimum lead time, buffer duration, cancellation window, fee, or no-show charge;
- legal or regulatory eligibility rules for a jurisdiction;
- exact private-address disclosure policy;
- recording, recording storage, transcription, or transcript analysis;
- clinical notes, EMR, insurance, prescriptions, laboratory data, or medical-record interchange;
- Conversation, Participant, Message, chat retention, or post-engagement messaging rules;
- review, rating, moderation, scoring, reliability, or reputation;
- subscription, entitlement, recurring billing, renewal, pause, or fulfillment occurrence architecture;
- DietitianMealPlan persistence details;
- commercial-provider or settlement-beneficiary selection;
- earning recognition, payout eligibility, ledger postings, Refund amount, or final remediation economics;
- Promotion evaluation, redemption, restoration, or targeting implementation; or
- a universal scheduling or professional identity model.

## Consequences

### Positive

- The actual practicing Dietitian remains durable and traceable independently of Organization staffing changes.
- Professional engagement is separate from individual Appointments and can support bounded or ongoing care without becoming a subscription.
- One-time consultations remain simple and do not require ongoing-care enrollment.
- Online and in-person professional services use one coherent Appointment model while preserving mode-specific location and jurisdiction inputs.
- ConsultationOffering versions and policy evidence make historical bookings explainable.
- PostgreSQL interval exclusion prevents concurrent double booking at the authoritative data boundary.
- Expiring holds support checkout without treating Payment or distributed locks as calendar capacity.
- Service duration and scheduling occupancy remain correctly distinguishable.
- Time-only rescheduling preserves Appointment identity, original capacity on failure, audit history, and first-use Promotion semantics.
- Customer, provider, cancellation, and no-show causes remain available for correct downstream policy.
- External calendars remain privacy-minimized free/busy inputs rather than the Appointment database.
- Meeting providers remain replaceable adapters and their outages do not corrupt Appointment state.
- Transactional outbox and idempotent retries avoid distributed transactions with meeting, calendar, Payment, and notification providers.
- Payment, Pricing, Promotion, Financial, professional identity, Conversation, review, and subscription ownership remain in their canonical domains.
- Jurisdiction eligibility is enforced separately from availability and timezone.

### Negative / Costs

- Separate engagement, offering, availability, hold, Appointment, location, reschedule-history, and meeting concepts create more explicit state and authorization paths.
- PostgreSQL temporal exclusion constraints and hold expiry require careful persistence design, migrations, and concurrency integration tests.
- Confirmation must coordinate capacity, eligibility, policy, commercial workflow, and outbox state without a distributed transaction.
- Rescheduling requires database-safe replacement semantics rather than simple cancel-and-create behavior.
- Policy/version and historical authorization evidence require deliberate snapshots or immutable references.
- External calendar and meeting integrations require idempotency, retry, alerting, reconciliation, and operational support.
- Provider-final meeting failure requires a manual/operational fallback policy.
- Location discovery and meeting access data require careful privacy, redaction, authorization, and audit controls.
- Jurisdiction policy must be confirmed for each launch context; architecture cannot infer legal rules.

## Alternatives Considered / Rejected

### 1. Appointment Is Just a Payment Record

Rejected because Payment describes provider-neutral collection, while Appointment owns professional-service scheduling and delivery state. Payment success neither reserves capacity nor proves service completion.

### 2. Appointment Is a KitchenBooking Subtype

Rejected because Dietitian professional availability, eligibility, Appointment lifecycle, rescheduling, location, no-show, and meeting concerns differ from Kitchen Space and Equipment concurrency. ADR-007 does not own Dietitian scheduling.

### 3. One Universal Booking Aggregate for All Resources

Rejected because Kitchen Space, equipment quantity, and individual professional time have different ownership, capacity, policy, and lifecycle invariants. Shared scheduling primitives do not require a universal business aggregate.

### 4. External Calendar Is the Source of Truth

Rejected because Cheffy must own holds, confirmed Appointments, policy, rescheduling, cancellation, and Customer history. Provider outage, account disconnection, or sync delay must not corrupt those facts.

### 5. Application Read-Before-Write Prevents Double Booking

Rejected because concurrent transactions can both observe an available interval and both write. PostgreSQL database-enforced interval exclusion is required for authoritative Dietitian overlap protection.

### 6. Advisory or Distributed Locks Are the Correctness Mechanism

Rejected because PostgreSQL is the system of record and can enforce interval exclusion transactionally. Redis, in-memory, or advisory locks add failure modes and are not authoritative capacity evidence.

### 7. Meeting Provider Call Occurs Inside the Appointment Transaction

Rejected because an external API cannot participate in the local PostgreSQL transaction. It would extend locks, create ambiguous failures, and couple confirmation availability to provider uptime. Provisioning occurs asynchronously through the outbox.

### 8. Meeting Provider ID Is the Appointment Identity

Rejected because providers may change, meetings may be replaced, and provisioning may fail or be retried. Appointment has a stable Cheffy identity independent of external meeting resources.

### 9. Reschedule Cancels the Old Appointment Before Securing New Time

Rejected because a conflict or later validation failure would destroy the Customer's valid booking. The original capacity remains protected until a database-safe replacement commits.

### 10. Current ConsultationOffering Policy Determines Historical Cancellation Fees

Rejected because offering policy changes must not rewrite accepted terms. The Appointment retains a policy/version reference or equivalent immutable evidence from booking time.

### 11. Customer No-Show and Dietitian No-Show Are the Same Outcome

Rejected because responsibility, Customer protection, Promotion restoration, commercial consequences, and future reliability evidence differ. The responsible side must remain explicit.

### 12. Every Dietitian-Client Relationship Is a Subscription

Rejected because single consultations and bounded professional care do not inherently create recurring billing, renewal, entitlement, or subscription obligations.

### 13. Organization Is the Dietitian Performer

Rejected because a clinic is not the actual practicing person. ADR-017 requires `DietitianProfessionalProfile` to remain durable on professional-service history.

### 14. Online Appointment Jurisdiction Is the Dietitian Timezone

Rejected because timezone expresses scheduling rules, not legal eligibility. Online service may require Customer/service-location and other policy context under ADR-017.

### 15. Recording and Transcription Are Enabled Automatically

Rejected because recording creates material consent, privacy, retention, security, and professional/clinical obligations that are not approved for MVP.

### 16. Dietitian Food Commission Is Part of Appointment Economics

Rejected because current Dietitian economics are limited to legitimate professional services. Food-sale, Meal Subscription, referral, Chef-purchase, and Chef-proceeds claims are explicitly outside the product baseline.

### 17. Payment Success Overrides an Expired Hold or Capacity Conflict

Rejected because collection and capacity are separate. Provider success cannot create an overlapping Appointment or bypass professional eligibility; divergence requires explicit idempotent Financial remediation.

### 18. One Giant Professional or Provider Aggregate

Rejected because ADR-017 deliberately preserves role-specific professional identities and separates performer, Organization, commercial provider, and beneficiary. Appointment references the typed actual Dietitian identity.

### 19. Generic Source Type Plus UUID for Core Relationships

Rejected because it weakens referential integrity and permits dangling or cross-type references. Final ERD work must establish typed relational relationships.

### 20. Meeting Provisioning Failure Automatically Cancels and Refunds the Appointment

Rejected because integration failure does not by itself decide service impossibility or commercial remediation. Retry and operational fallback occur first; ADR-020 owns approved economic consequences.

## Dependencies and Related ADRs

- **ADR-001 — Modular Monolith First:** Appointment, availability, engagement, location, calendar, and meeting capabilities remain within the modular monolith; this ADR introduces no microservice.
- **ADR-002 — Event-Driven Integration Through Outbox:** Meeting, calendar, notification, and other asynchronous integration follows the Accepted transactional-outbox pattern.
- **ADR-006 — Promotion Targeting Model:** Typed Promotion owner and target identity, including ConsultationOffering targeting, remain ADR-006 concerns.
- **ADR-007 — Booking Concurrency Control:** ADR-018 reuses its PostgreSQL interval-exclusion principle where appropriate but independently owns Dietitian Appointment concurrency. ADR-007 continues to own KitchenBooking and EquipmentRental concurrency only.
- **ADR-010 — UUIDv7 Identifier Strategy:** The repository identifier direction applies when persistence is designed; ADR-018 introduces no new strategy and does not change ADR-010's status.
- **ADR-011 — Timezone Modeling:** Recurring local schedules, IANA scheduling zones, real Appointment instants, DST handling, and historical instant rules follow ADR-011; ADR-018 does not change its status.
- **ADR-012 — Payment Marketplace Settlement:** Payment, PaymentAttempt, provider interaction, command idempotency, and Refund orchestration remain Financial-owned; ADR-018 does not change ADR-012's status.
- **ADR-014 — Promotion Engine:** Promotion eligibility, evaluation, application, redemption, penalty applicability, material-change revalidation, first-use semantics, and restoration remain ADR-014 concerns; ADR-018 does not change its status.
- **ADR-015 — Financial Ledger and Reconciliation:** Financial posting, immutability, correction, and reconciliation remain ADR-015 concerns; ADR-018 does not change its status.
- **ADR-016 — Event Versioning:** Any future Appointment or OnlineMeeting event contract follows ADR-016's Accepted versioning and compatibility rules.
- **ADR-017 — Professional Identity, Credentials and Jurisdiction Eligibility:** ADR-018 consumes actual Dietitian identity, professional-to-Organization authorization, credentials, jurisdiction eligibility, and historical performer attribution without redefining them or changing ADR-017's Proposed status.

No related ADR status is changed by this Proposed ADR.

## Future ADR Dependencies

- **ADR-020 — Commercial Obligations, Earning Recognition and Payable-Source Financial Model:** will own Appointment commercial obligation, earning recognition, unfulfilled value, refund/remediation policy, settlement beneficiary, and payout eligibility. ADR-018 supplies typed operational outcome evidence but does not draft those rules.
- **ADR-021 — Authorized Multi-Context Conversation Architecture:** will own Customer-Dietitian Conversation authorization, participants, messages, grace/read-only behavior, and retention. ADR-018 supplies only possible Appointment/engagement authorization context.
- **ADR-023 — Verified-Experience Reviews and Reputation:** will own Dietitian review eligibility implementation, review storage, moderation, scoring, aggregation, and reputation. ADR-018 supplies completed-Appointment and actual-performer evidence.

ADR-019 may define subscription-like care products if explicitly approved in the future, but this ADR does not turn DietitianClientEngagement or Appointment into a subscription, entitlement, or fulfillment occurrence.

## Implementation and Propagation Notes

This Proposed ADR does not authorize application code, migrations, SQL, API changes, or event-contract changes by itself. After acceptance, implementation planning must:

1. Reconcile the canonical ERD with typed DietitianClientEngagement, ConsultationOffering/version, availability, Appointment/hold, ConsultationLocation, reschedule-history, and OnlineMeeting relationships.
2. Design PostgreSQL half-open interval exclusion for active unexpired HELD and CONFIRMED Dietitian capacity without depending on advisory locks or application checks.
3. Define deterministic hold expiry/release behavior and prove that expired holds cease reserving capacity.
4. Define confirmation and reschedule transaction boundaries, idempotency, conflict mapping, audit evidence, and rollback behavior.
5. Define ADR-017 authorization/eligibility decision interfaces and historical evidence for Appointment confirmation and material changes.
6. Define PricingSnapshot, Promotion, Payment, Refund, and future ADR-020 coordination without introducing fake Orders or duplicate Financial ownership.
7. Define provider-neutral calendar and meeting ports, secure credential/access handling, idempotency, retry, reconciliation, alerting, and manual fallback.
8. Update `docs/03-database-erd.md`, `docs/04-api-contracts.md`, and `docs/05-event-contracts.md` only through separately approved canonical propagation work.
9. Apply ADR-016 to every approved event contract and keep provider calls outside local Appointment transactions.
10. Add real-PostgreSQL concurrency tests, preferably with Testcontainers and independent transactions, for overlapping holds, confirmations, expiry, cancellation release, and reschedule rollback.
11. Add authorization and historical-attribution tests proving Organization administration cannot replace the actual Dietitian performer.
12. Add DST, timezone, location privacy, meeting access, provider retry, duplicate delivery, outage, and reconciliation tests.

At minimum, future concurrency tests must prove that two simultaneous overlapping reservations for one Dietitian cannot both become capacity-reserving, an expired hold releases capacity, cancellation releases future capacity without deleting history, a failed reschedule preserves the original interval, non-overlapping half-open intervals may coexist, and duplicate meeting-provisioning delivery does not create uncontrolled duplicate meetings.
