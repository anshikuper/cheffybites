# P1-MVP-01 — Chef ↔ Kitchen Pilot Marketplace Product Specification

**Document:** Phase-1 Pilot MVP canonical product specification
**Version:** 1.0
**Status:** Ready for architecture reconciliation
**Overall result:** P1-MVP-01 READY FOR ARCHITECTURE RECONCILIATION
**Phase:** Chef ↔ Kitchen capacity booking only
**Prepared:** 1 September 2026
**Market-evidence status:** Product definition only; no market hypothesis is validated by this document or by working software
**Required next gate:** Reconcile the canonical architecture, persistence, API, event, and ADR sources before production coding

This specification is authoritative for the Phase-1 Pilot MVP product boundary. It does not change an Accepted ADR, the canonical persistence model, API contracts, event contracts, LP-01, or any market-intelligence hypothesis status. Proposed deltas in Sections 38–40 are inputs to a later architecture-reconciliation task, not approved implementation contracts or DDL.

Priority meanings used throughout:

| Priority | Meaning |
|---|---|
| **P0** | Required for the complete operator demonstration and controlled real pilot. |
| **P1** | Valuable for early pilot operation; may follow the complete web P0 slice. |
| **P2** | Deliberately deferred beyond the narrow pilot. |

Release criteria, rather than priority alone, determine when a capability must be deployed. A P0 capability may be incomplete in R0 or R1 but must be complete before R3.

---

## 1. Executive objective

Build a real but narrow two-sided application in which a Kitchen operator can create and publish actual rentable KitchenSpace capacity and a Chef or food business can find that capacity, send a persisted request, receive an operator decision, and see the resulting booking state.

The application is not a click-through prototype. It uses real authenticated accounts, durable records, server-side authorization and validation, real availability rules, and concurrency-safe confirmed bookings. It is also not the long-term Cheffy Bites platform: there is no food ordering, payment processing, payout, review, marketplace chat, recommendation engine, or Phase-2 persona.

The product has five irreducible outcomes:

1. A real authorized operator can onboard an Organization, Kitchen, one or more KitchenSpaces, equipment, rental offers, and availability.
2. The operator can preview and explicitly publish that inventory without Cheffy Bites silently publishing it.
3. A real Chef can search published participating capacity and submit a persisted request for a concrete date and time.
4. The operator can accept or decline the request; acceptance atomically creates capacity-reserving confirmed booking state.
5. Both parties can see the same authoritative booking state through clients using one backend contract.

**REQ-OBJ-01 — P0:** Deliver the complete real-account, real-listing, real-availability, real-request, real-decision workflow.
**REQ-OBJ-02 — P0:** Keep payment, deposit collection, refund, payout, tax calculation, and financial-ledger behavior outside the pilot.
**REQ-OBJ-03 — P0:** Treat software usage as product evidence only and never as market validation by itself.
**REQ-OBJ-04 — P0:** Preserve one backend source of business truth for responsive web and native mobile clients.

## 2. Relationship to LP-01

LP-01 remains authoritative for the public credibility surface, evidence-safe claims, Kitchen-operator-first language, founder credibility, bilingual public content, privacy and trust direction, SEO/social metadata, accessibility, public route structure, and separation of open-marketplace and managed-capacity hypotheses.

P1-MVP-01 supersedes only LP-01's prohibition on authentication, persistence, live listings, availability, booking requests, dashboards, and backend marketplace behavior for the authenticated pilot application. It does not retroactively edit LP-01 or transform the fictional LP-01 demo into evidence of a participating operator.

The controlled stage transition is:

| Stage | Public surface | Authenticated application | Inventory rule |
|---|---|---|---|
| **PRE-PILOT** | LP-01 research-stage wording remains in force; no public live-inventory claim. | R0–R2 may contain isolated fictional demo data and invited test accounts. | Only records marked DEMO may appear in demo experiences. No real operator is presented as participating. |
| **CONTROLLED PILOT** | Stage wording must be deliberately reviewed and updated before any claim that real requests are possible. Public marketing pages remain evidence-safe and do not imply broad Montreal supply. | Invited authenticated participants may access authorized REAL records. | A real listing is requestable only after operator publication, pilot authorization, and environment/stage gates all pass. |

Transitioning the platform stage is an auditable administrator action. It does not publish a Kitchen. Publishing remains a separate operator action. Public visibility, authenticated-pilot visibility, and Kitchen publication are separate decisions.

**REQ-LP-01 — P0:** Retain all LP-01 public routes and claims controls until a separately approved stage-copy change.
**REQ-LP-02 — P0:** Enforce PRE-PILOT versus CONTROLLED PILOT behavior in configuration and server-side queries, not only in page copy.
**REQ-LP-03 — P0:** Require explicit operator publication plus platform pilot authorization before a REAL Kitchen is requestable.
**REQ-LP-04 — P0:** Keep the fictional demo route clearly fictional, non-indexed as inventory, and isolated from REAL records.
**REQ-LP-05 — P0:** Never merge the open-marketplace request model with managed or committed-capacity rights; no sublicense or scheduling right is inferred.

## 3. Pilot validation boundary

The application may generate evidence about whether people can use the workflow and where the product fails. It does not establish that:

- any Kitchen operator will participate;
- advertised Montreal facilities have bookable capacity;
- Chefs want or will pay for the service;
- any launch cell has sufficient liquidity;
- a 30 km radius is viable;
- hourly pricing is the dominant or viable format;
- Cheffy Bites has a right to re-rent, sublicense, or schedule third parties;
- open-marketplace or managed-capacity economics work;
- bilingual operations, compliance, insurance, contracting, or incident handling are solved.

All MI-01 hypotheses remain at their authoritative status. MI-02 propositions are research leads, not seed inventory. MI-07A remains preparation only; no operator interview or participation is implied.

Pilot product analytics answer questions such as “Did request submission fail?” or “Where did an invited tester abandon onboarding?” They cannot, without the registered research method and evidence, change an MI-01 hypothesis to VALIDATED.

**REQ-VAL-01 — P0:** Display no market-validation claim based only on account, listing, search, request, booking, or feedback counts.
**REQ-VAL-02 — P0:** Keep market hypotheses and product analytics in separate reporting namespaces and language.
**REQ-VAL-03 — P0:** Use only fictional seed records; never seed a real researched operator without authorization.
**REQ-VAL-04 — P0:** Do not select or imply a Montreal launch cell or service radius in product defaults.
**REQ-VAL-05 — P1:** Capture structured no-result and rejection reasons for later research analysis without automatically interpreting them as validation.

## 4. Personas and permissions

### 4.1 Authenticated personas

| Persona | Minimum authority |
|---|---|
| **Kitchen operator owner** | Create/manage its Organization profile, Locations, Kitchens, Spaces, equipment, rental offers, availability, publication, requests, and bookings. Invite/team management is P1. |
| **Kitchen operator manager** | Manage only assigned Organization/Kitchen resources. Cannot change Organization ownership or platform pilot authorization. |
| **Chef / food business** | Manage own profile, search participating Kitchens, submit and withdraw pending requests, view own bookings, and cancel a confirmed booking under the minimal pilot rule. |
| **Platform administrator** | Perform limited pilot operations: inspect state, authorize pilot visibility, unpublish unsafe content, deactivate accounts, and view feedback/notification failures. |

One authenticated user may hold more than one role. Role membership does not merge the Organization, Chef identity, or resource ownership paths. The backend authorizes every operation through user, Organization membership, permission, resource ownership, and current business state.

### 4.2 P0 permission set

| Permission | Operator owner | Operator manager | Chef | Admin |
|---|:---:|:---:|:---:|:---:|
| profile:read/write:self | ✓ | ✓ | ✓ | — |
| organization:read | owned | assigned | own Chef business if present | pilot scope |
| kitchen:create/read/write | owned | assigned | published read only | pilot scope |
| kitchen:publish/unpublish | owned | assigned if granted | — | emergency unpublish |
| availability:manage | owned | assigned | — | inspect only |
| booking-request:create | — | — | self | — |
| booking-request:read | owned Kitchens | assigned Kitchens | self | pilot scope |
| booking-request:decide | owned Kitchens | assigned if granted | — | no ordinary decision |
| booking:cancel | owned Kitchens | assigned if granted | self booking | exceptional/admin control |
| pilot:authorize-real-listing | — | — | — | ✓ |
| account:deactivate | — | — | self close request only | ✓ |

**REQ-PERM-01 — P0:** Enforce role, Organization membership, assignment, ownership, and resource state on the server.
**REQ-PERM-02 — P0:** Prevent cross-Organization reads and writes, including guessed identifiers.
**REQ-PERM-03 — P0:** Prevent an admin from impersonating an operator decision in the normal workflow; emergency action must be separate and audited.
**REQ-PERM-04 — P1:** Support operator invitations and per-Kitchen manager assignment.
**REQ-PERM-05 — P2:** Defer custom role builders and enterprise permission administration.

## 5. Release levels R0–R4

| Release | Entry criteria | Required capabilities | Exit criteria |
|---|---|---|---|
| **P1-MVP-R0 — Local demo** | Architecture reconciliation for the implemented slice is approved; local dependencies work. | Full P0 happy path with fictional DEMO accounts/data, persisted locally; deterministic reset; concurrency test. | All ten acceptance flows have assigned release targets; flows 1–5, 7 and 10 pass locally; no real operator identity. |
| **P1-MVP-R1 — Private staging** | R0 critical tests pass; non-production Auth0, database, media and email sandbox configured. | Deployed protected web app; founder/invited testers; isolated DEMO dataset; basic observability and admin inspection. | Security/authorization test pass; email and error monitoring work; backup/restore exercise; no staging record can appear in production. |
| **P1-MVP-R2 — Operator demo** | R1 stable; demo script and bilingual demo UI reviewed for material screens. | Presentation-quality end-to-end fictional operator and Chef workflow; visible DEMO treatment; repeatable data reset. | Founder can complete flows 1–8 and 10 without database intervention; no false live-marketplace claim. |
| **P1-MVP-R3 — Controlled pilot** | At least one operator explicitly agrees; privacy/terms/security/French and support gates complete; platform set to CONTROLLED PILOT. | Authorized REAL operator listing, invited real Chef request, actual accept/decline, both dashboards, email/in-app notification, feedback, minimal admin, operational runbook. | One real workflow can be completed safely; failures and conflicts are observable; data rights and support paths operate; no claim of validation. |
| **P1-MVP-R4 — Early multi-operator pilot** | R3 evidence justifies expansion; operating workload and reliability risks are reviewed. | Multiple authorized operators, P1 usability/operations items selected by evidence, native mobile core if justified. | Predefined pilot decision review; no automatic geography or product expansion. |

No release may be named “validated.” R4 is not automatic after a successful demonstration or first booking.

**REQ-REL-01 — P0:** Maintain strict environment and DEMO/REAL isolation across R0–R3.
**REQ-REL-02 — P0:** Gate REAL listing visibility on R3 operational and authorization checks.
**REQ-REL-03 — P0:** Provide deterministic fictional demo reset without deleting REAL records.
**REQ-REL-04 — P1:** Add release health and pilot-cohort reporting for R4 decisions.
**REQ-REL-05 — P2:** Defer open self-service public marketplace launch.

## 6. End-to-end operator journey

1. Sign up/sign in through Auth0/OIDC.
2. Complete contact preferences and create or join an operator Organization.
3. Create a Location and a DRAFT Kitchen.
4. Enter facility description, area/address, timezone, access, intended-use and operator-requirement information.
5. Create at least one active KitchenSpace with capacity and operating constraints.
6. Select catalog equipment and classify how it is available.
7. Add at least one rental offer with truthful basis, amount or inquiry treatment, commitment and notes.
8. Add recurring and/or one-time Space availability plus blocked exceptions.
9. Preview exactly what a Chef will see, including address redaction and pricing disclaimers.
10. Publish explicitly. In R3, the listing becomes requestable only if platform pilot authorization is also active.
11. Receive a persisted request and in-app/email notification.
12. Review Chef-entered context, time, Space, estimate, requirements and current conflicts.
13. Accept or decline. Acceptance revalidates current availability and atomically attempts the ADR-007 confirmed-capacity transition.
14. View confirmed upcoming and past bookings; cancel only with a reason and notification.

The operator retains final approval. A REQUESTED record is not a booking and does not reserve the Space.

**REQ-OJ-01 — P0:** Support the full journey above without founder database edits.
**REQ-OJ-02 — P0:** Autosave or explicitly save durable drafts and show validation progress.
**REQ-OJ-03 — P0:** Provide a Chef-view preview before publication.
**REQ-OJ-04 — P0:** Preserve confirmed bookings when a Kitchen is unpublished.
**REQ-OJ-05 — P2:** Defer native-mobile Kitchen creation and complex availability management.

## 7. End-to-end Chef journey

1. Sign up/sign in through Auth0/OIDC.
2. Complete the minimum Chef/food-business profile.
3. Search by general location and optionally concrete date, start time, duration, rental format, equipment, or comparable price.
4. Review results labelled MATCHES REQUEST, POSSIBLE — OPERATOR CONFIRMATION REQUIRED, or NO MATCH.
5. Open a participating Kitchen detail page and select one Space.
6. Enter a concrete Kitchen-local date/start/end and operational context.
7. Review the selected rental offer and any valid estimate disclaimer.
8. Submit an idempotent persisted request; no payment is requested.
9. See REQUESTED immediately in My requests/bookings.
10. Receive in-app/email outcome and see CONFIRMED or DECLINED consistently.
11. Withdraw before decision, or cancel a confirmed future booking with a reason under the pilot rule.

**REQ-CJ-01 — P0:** Support the full responsive-web Chef journey above.
**REQ-CJ-02 — P0:** Never label request submission as a confirmed reservation.
**REQ-CJ-03 — P0:** Make the Kitchen timezone and converted viewer time clear at every date/time decision.
**REQ-CJ-04 — P1:** Deliver the native-mobile Chef core journey using the same APIs.
**REQ-CJ-05 — P2:** Defer wishlists, recommendations, ratings, reviews, and chat.

## 8. Operator account/profile

Account identity and Kitchen information are separate. Auth0 owns authentication credentials; the Cheffy domain binds the external subject to the internal user and profile.

### 8.1 Minimum account fields

| Field | Rule | Priority |
|---|---|:---:|
| Contact/display name | Required; private except where explicitly displayed as listing contact. | P0 |
| Business email | Required and verified through identity flow; not public. | P0 |
| Phone | Optional; private; used only for authorized pilot operations. | P1 |
| Preferred language | Required: en-CA or fr-CA. | P0 |
| Role/title | Optional short text. | P1 |
| General business information | Optional short text; not Kitchen content. | P1 |
| Account status | ACTIVE or DEACTIVATED for pilot. | P0 |

Do not collect permits, insurance, contracts, banking, tax, or identity documents during account creation.

**REQ-OPR-01 — P0:** Bind the account to an internal user and operator Organization membership.
**REQ-OPR-02 — P0:** Keep business email, phone and unpublished business data private.
**REQ-OPR-03 — P0:** Allow profile and language updates without rewriting historical booking identity.
**REQ-OPR-04 — P2:** Defer compliance-document upload and verification.

## 9. Chef account/profile

### 9.1 Minimum profile fields

| Field | Rule | Priority |
|---|---|:---:|
| Name/display name | Required and visible to the reviewing operator. | P0 |
| Business/trading name | Optional. | P0 |
| Business email | Required and verified; visible only to authorized parties according to pilot privacy policy. | P0 |
| Phone | Optional; not public search content. | P1 |
| Profile image | Optional; safe media controls. | P1 |
| Food/business category | Required controlled multi-select plus Other text. | P0 |
| Description/intended activity | Required concise text for operator context. | P0 |
| Preferred language | Required: en-CA or fr-CA. | P0 |
| General operating area | Optional coarse area; never a home address requirement. | P1 |

### 9.2 Requirements and credential posture

The pilot does not claim regulatory verification. For an operator-defined requirement, record only the minimum status:

- **NOT_PROVIDED** — the Chef has not entered an assertion.
- **DECLARED** — the Chef entered or affirmed the requested information; Cheffy Bites has not independently verified it.
- **REVIEWED_OUTSIDE_PLATFORM** — the operator/admin states that review occurred outside the platform; this is not a Cheffy verification claim.

No original credential or insurance document is stored in P0. The request may include acknowledgements and non-sensitive reference text, but the interface warns against confidential data.

**REQ-CHP-01 — P0:** Persist the minimum profile and expose only the fields needed for operator review.
**REQ-CHP-02 — P0:** Label declared information accurately and never display “verified” without a separate approved process.
**REQ-CHP-03 — P0:** Allow request-level operator requirements to be answered with safe minimal statuses.
**REQ-CHP-04 — P2:** Defer document verification, background checks, and credential evidence storage.

## 10. Organization/operator model

The existing hierarchy is retained:

~~~text
User
  → OrganizationMembership
    → Organization
      → Location
        → Kitchen
          → KitchenSpace
~~~

One user is not one Kitchen. One Organization may operate multiple Locations and Kitchens, and multiple authorized users may manage the same Organization. Property ownership is not inferred from operating authority. The pilot records the Organization represented by the onboarding operator; it does not implement real-estate lease management or certify operating rights.

For R3, the operator must affirm that they are authorized to submit and publish the listing. This affirmation is evidence of the user's declaration, not legal verification. Admin pilot authorization records that Cheffy has permitted controlled-pilot visibility; it is not a property-right or compliance determination.

**REQ-ORG-01 — P0:** Reuse Organization, membership, Location, Kitchen, and KitchenSpace ownership paths.
**REQ-ORG-02 — P0:** Allow an Organization to own/manage multiple Kitchens.
**REQ-ORG-03 — P0:** Record operator publication affirmation and admin pilot authorization separately.
**REQ-ORG-04 — P1:** Support multiple managers with assigned Kitchen permissions.
**REQ-ORG-05 — P2:** Defer lease, sublicense, payroll, provider/payee and managed-capacity contracting models.

## 11. Kitchen management

### 11.1 Fields

| Group | P0 fields |
|---|---|
| Identity | Name, operator Organization, description, facility type, status, record mode DEMO/REAL. |
| Location | Location/address, geocoded coordinates, general public area, country/province/city, authoritative IANA timezone. |
| Public presentation | Photos, intended/permitted-use categories as operator-entered statements, public area, accessibility/access summary. |
| Operations | Operating-hours summary, access/orientation notes, loading/parking, storage summary, facility constraints and requirements. |
| Control | DRAFT/PUBLISHED/UNPUBLISHED, publication timestamp/actor, pilot authorization, visibility level. |

Exact street address is private by default in search and public detail. P0 displays a general area to authenticated Chefs. The exact address may be revealed only to parties with a CONFIRMED booking or through an operator-approved detail policy. The implementation must not put exact coordinates or private access instructions into analytics, logs, metadata, sitemap, or unauthenticated structured data.

### 11.2 Lifecycle

~~~text
DRAFT → PUBLISHED → UNPUBLISHED
  ↑          ↓           │
  └──────────┴───────────┘ by explicit operator action where valid
~~~

- DRAFT is editable and never requestable.
- PUBLISHED is eligible for discovery only when its active Space, offer, availability, stage, record-mode, and pilot-authorization gates also pass.
- UNPUBLISHED receives no new requests. Existing requests and confirmed bookings remain visible and actionable.
- Re-publishing revalidates completeness.

No REVIEW, SUSPENDED, ARCHIVED, or other Kitchen status is added for P0. Emergency admin unpublish uses the same UNPUBLISHED product state plus an audit reason.

**REQ-KIT-01 — P0:** Create, read, update, preview, publish and unpublish a Kitchen.
**REQ-KIT-02 — P0:** Validate at least one active Space, usable offer, future availability, timezone, general area, description and required visibility fields before publication.
**REQ-KIT-03 — P0:** Enforce the three-state lifecycle and separate record mode/pilot authorization.
**REQ-KIT-04 — P0:** Protect exact address, access details and operator contact information according to visibility policy.
**REQ-KIT-05 — P0:** Validate and safely serve Kitchen photos.
**REQ-KIT-06 — P1:** Allow drag ordering and richer photo captions.
**REQ-KIT-07 — P2:** Defer public self-service listing pages outside the controlled pilot.

## 12. KitchenSpace management

A KitchenSpace is the bookable exclusive-capacity unit. The whole Kitchen is never assumed to be the only rentable resource.

### 12.1 Fields

| Field | Rule | Priority |
|---|---|:---:|
| Name and description | Required. | P0 |
| Capacity | Required positive number with operator-facing explanation of what it means. | P0 |
| Size/unit | Optional. | P1 |
| Photos | Optional in P0; inherits Kitchen photos if absent. | P1 |
| Access | Required summary; sensitive instructions remain private. | P0 |
| Storage | Required categorical/notes treatment: none, available, shared, discuss. | P0 |
| Operating constraints | Required free text with character limit and no sensitive data. | P0 |
| Cleaning minutes | Required non-negative; contributes to protected occupancy. | P0 |
| Active/inactive | Required independent of Kitchen publication. | P0 |
| Exclusivity mode | P0 supports EXCLUSIVE_SPACE only for concurrency. Shared fractional capacity is deferred. | P0 |

An inactive Space is not discoverable and accepts no new request, while history remains. Deactivation is rejected if it would conceal active confirmed bookings; the operator must keep the Space visible in management and is warned about upcoming commitments.

**REQ-SPC-01 — P0:** Create, update, activate and deactivate multiple Spaces per Kitchen.
**REQ-SPC-02 — P0:** Treat each active Space as the ADR-007 overlap-protected resource.
**REQ-SPC-03 — P0:** Include cleaning time in occupancy and conflict display.
**REQ-SPC-04 — P0:** Preserve booking history for inactive Spaces.
**REQ-SPC-05 — P2:** Defer fractional/shared-capacity booking within one Space.

## 13. Equipment

The pilot reuses the EquipmentCatalogItem and SpaceEquipment concepts. Catalog items provide controlled names/categories; the Space association expresses the operator's actual offering.

### 13.1 P0 availability modes

| Mode | Meaning |
|---|---|
| **INCLUDED** | Available in the Space and included in displayed rental terms, subject to operator rules. |
| **SHARED** | Present but shared; a request must not imply guaranteed exclusive access. |
| **EXTRA_DISCUSS** | Potentially available for an additional operator-arranged charge or condition; not priced or reserved by Cheffy Bites. |
| **UNAVAILABLE** | Not offered for this Space; normally omitted from the public listing. |

Space association is inherently SPACE_SPECIFIC. P0 records quantity where useful and optional notes/condition, but it does not reserve equipment quantities or implement EquipmentRental payment. A Chef may state equipment needs in the request. Operator acceptance confirms the complete request operationally; it does not create a separate paid equipment transaction.

The existing EquipmentRental/EquipmentAllocation architecture remains available for a future approved scope, but the advanced additional-equipment rental engine is P2 here.

**REQ-EQP-01 — P0:** Select active catalog items and associate them with a Space.
**REQ-EQP-02 — P0:** Display mode, quantity and notes honestly on result/detail/request screens.
**REQ-EQP-03 — P0:** Allow Chef equipment needs in the request and show them to the operator.
**REQ-EQP-04 — P1:** Add admin catalog maintenance if secure tooling is insufficient.
**REQ-EQP-05 — P2:** Defer finite equipment allocation, independent equipment intervals, promotions and payment.

## 14. Rental formats and pricing

One universal hourly-rate field is insufficient. The smallest honest model is a set of operator-authored RentalOffers attached to a Space.

### 14.1 RentalOffer fields

| Field | Rule |
|---|---|
| Rate basis | HOURLY, FIXED_BLOCK, DAILY, RECURRING_HOURS, MONTHLY_HOURS, or PRIVATE_LONG_TERM_INQUIRY. |
| Amount minor/currency | Exact integer minor units and ISO currency when a price is stated; CAD is the pilot default, not a hidden assumption. Amount may be absent only for PRIVATE_LONG_TERM_INQUIRY and must display “Contact operator / confirmation required.” |
| Block/included quantity | Block minutes, day definition, weekly hours, or monthly included hours as applicable. |
| Minimum duration | Required where the selected basis uses a request duration. |
| Minimum commitment | Optional structured count/unit plus clear notes; required when recurring/monthly terms depend on it. |
| Deposit display | Optional informational amount/text only; never charged or collected by Cheffy Bites. |
| Additional charges/notes | Optional operator-entered text; must state what is or is not included. |
| Active | Controls new display/request use; request snapshots preserve historical terms. |

### 14.2 Estimate rules

An estimate is shown only when deterministic from the chosen offer and request:

- HOURLY: requested billable duration × hourly amount, respecting the minimum.
- FIXED_BLOCK: whole allowed blocks × block amount only when the block definition applies exactly.
- DAILY: number of operator-defined eligible days × daily amount only when unambiguous.
- RECURRING_HOURS and MONTHLY_HOURS: show the stated plan/commitment, not a misleading one-off prorated estimate.
- PRIVATE_LONG_TERM_INQUIRY: no calculated amount.

Canonical pilot wording:

> **Estimated rental amount: CAD $X.** This estimate uses the operator-entered rate shown above. Taxes, deposits and additional charges are not included unless explicitly stated. Cheffy Bites does not process payment in this pilot. The operator must approve the request and confirm final terms.

A request stores a read-only snapshot of the selected offer and calculated estimate so later edits do not rewrite what either party reviewed.

**REQ-PRC-01 — P0:** Support all six rate bases without a general-purpose pricing engine.
**REQ-PRC-02 — P0:** Use exact minor-unit amounts and currency.
**REQ-PRC-03 — P0:** Calculate an estimate only where mathematically valid and label it as non-final.
**REQ-PRC-04 — P0:** Persist a request-time offer/estimate snapshot.
**REQ-PRC-05 — P0:** Display optional deposits as operator information only.
**REQ-PRC-06 — P2:** Defer taxes, dynamic pricing, fees, promo codes, checkout and collections.

## 15. Availability model

Availability expresses what an operator is currently willing to receive requests for. It is not a booking, hold, guarantee, or payment reservation.

### 15.1 Availability inputs

| Kind | Example | Priority |
|---|---|:---:|
| **ONE_TIME_AVAILABLE** | 2026-09-15, 08:00–16:00 | P0 |
| **RECURRING_AVAILABLE** | Monday, 08:00–14:00, effective 2026-09-01 through 2026-12-31 | P0 |
| **BLOCKED** | 2026-09-22, 10:00–13:00 or a recurring closure | P0 for one-time blocks; recurring blocks P1 |

Explicit one-time BLOCKED time is required in P0. Absence of availability remains unavailable, but blocks provide safe exceptions to a broader recurring rule without destructive rule editing.

Availability attaches to KitchenSpace. Kitchen operating hours are a facility constraint and do not by themselves make a Space requestable. The owning Kitchen's IANA timezone defines all local availability rules.

### 15.2 Evaluation precedence

For a concrete request interval:

1. Kitchen/Space must be published/active and in the correct stage/mode.
2. Interval must fit operating constraints and at least one active availability rule.
3. A matching BLOCKED interval makes it unavailable.
4. Existing HELD or CONFIRMED occupancy makes it unavailable according to ADR-007.
5. Equipment and operator requirements may reduce the result from MATCHES to POSSIBLE.

Search is an informational snapshot. Submission re-evaluates requestability. Acceptance performs the authoritative database capacity transition.

### 15.3 Time semantics

- Rules are entered and displayed primarily in the Kitchen's IANA timezone.
- Concrete requests/bookings are resolved to explicit-offset/UTC instants.
- DST gaps are rejected rather than silently shifted.
- DST overlaps require explicit disambiguation.
- Changing a Kitchen timezone affects future rule interpretation and does not rewrite existing concrete request/booking instants.
- ADR-011 remains Proposed; this product requirement cannot be implemented as an Accepted architecture decision until reconciliation resolves its status.

**REQ-AVL-01 — P0:** Create, edit, disable, re-enable and remove one-time/recurring Space availability.
**REQ-AVL-02 — P0:** Create one-time blocked exceptions.
**REQ-AVL-03 — P0:** Preview resolved availability for a bounded date range in Kitchen-local time.
**REQ-AVL-04 — P0:** Apply explicit precedence and server-side evaluation.
**REQ-AVL-05 — P0:** Recheck availability on request submission and operator acceptance.
**REQ-AVL-06 — P1:** Support recurring blocks and bulk exception entry.
**REQ-AVL-07 — P2:** Defer external calendar synchronization and unbounded recurrence.

## 16. Kitchen discovery/search

Search returns only active, published, stage-eligible, requestable participating Spaces. It uses PostgreSQL structured filters/full text and PostGIS; no recommendation engine or OpenSearch is required.

### 16.1 P0 inputs

- general location text or coordinates selected by the user;
- optional requested local date;
- optional start time and duration/end time;
- optional rental format;
- optional equipment catalog filters;
- optional comparable maximum price for same-basis offers only.

No default 30 km service radius is shown. A pilot-configured search bound may protect performance, but it must not be described as validated service coverage.

### 16.2 Result classification

| Classification | Rule |
|---|---|
| **MATCHES REQUEST** | The concrete time fits an active availability rule, no block or capacity reservation conflicts, hard filters match, and the selected offer can accept the duration. Operator approval is still required. |
| **POSSIBLE — OPERATOR CONFIRMATION REQUIRED** | Listing meets basic filters but one or more non-computable/shared/operator-specific conditions require review, or no concrete time was supplied. |
| **NO MATCH** | A specific inspected Space fails a hard location/time/format/equipment/price constraint. General search normally omits it, while a direct-detail recheck may explain the result. |

Search must never say “available” without the operator-approval qualifier. If pricing bases are not comparable, do not sort or filter them as though they were.

**REQ-SCH-01 — P0:** Search real eligible published Spaces using the P0 inputs.
**REQ-SCH-02 — P0:** Return the three honest compatibility labels with reason codes.
**REQ-SCH-03 — P0:** Avoid a fixed service-radius claim and expose no exact private coordinates.
**REQ-SCH-04 — P0:** Treat search availability as informational and non-reserving.
**REQ-SCH-05 — P1:** Add saved recent search inputs for the same user.
**REQ-SCH-06 — P2:** Defer personalization, recommendation, popularity, and demand-ranking features.

## 17. Kitchen listing/detail

### 17.1 Result card

Each card shows:

- Kitchen and Space name;
- general area and facility/Space type;
- selected equipment highlights;
- supported rental format(s);
- operator-entered price or inquiry wording;
- MATCHES/POSSIBLE label for the current request;
- “Operator approval required”;
- DEMO badge where applicable.

No ratings, reviews, social proof, booking counts, urgency messages, or “people booked today” devices are permitted.

### 17.2 Detail

The detail view shows operator-entered Kitchen information, photos, safe location, Spaces, equipment mode/quantity, storage, access summary, rental offers, price/estimate rules, operator requirements, availability/request guidance, operator-supplied cancellation/terms notes, and the approval rule.

Exact address, access codes, private contact information, internal notes, unpublished terms, and other confidential operator data are excluded from public/authenticated discovery. Operator-entered compliance claims are attributed and not transformed into Cheffy verification.

**REQ-LST-01 — P0:** Render complete result cards and details from REAL or DEMO records without hard-coded operator content.
**REQ-LST-02 — P0:** Attribute operator-entered information and protect confidential fields.
**REQ-LST-03 — P0:** Show persistent operator-approval and estimate disclaimers.
**REQ-LST-04 — P0:** Omit all fake reputation, scarcity and activity claims.
**REQ-LST-05 — P1:** Reveal exact address automatically to confirmed parties only if the operator's visibility policy allows it.

## 18. Booking request

The Chef selects exactly one KitchenSpace and a concrete date/start/end, then provides:

- food/business activity;
- equipment needs;
- storage needs;
- setup/cleanup needs;
- safe requirement declarations where requested;
- optional message to operator.

The request is idempotently persisted. No payment or capacity hold occurs.

### 18.1 Aggregate decision

To avoid a duplicate competing entity, the P0 product treats the request as the initial non-reserving state of the existing KitchenBooking aggregate:

~~~text
KitchenBooking status REQUESTED
  = persisted operator-review request, no capacity reservation

KitchenBooking status CONFIRMED
  = accepted reservation, ADR-007 capacity protection applies
~~~

The UI calls the record a “request” until CONFIRMED and a “booking” after confirmation. Availability rules remain separate supply expressions.

The record snapshots Space/Kitchen names, request instants/timezone, selected offer terms/estimate, Chef activity context, and relevant requirement responses. Mutable listing/profile fields may be linked for current display but must not rewrite the historical reviewed request.

**REQ-BRQ-01 — P0:** Persist each valid submission as REQUESTED with an idempotency key.
**REQ-BRQ-02 — P0:** Store the concrete instant interval, Kitchen timezone identity used, context and pricing snapshot.
**REQ-BRQ-03 — P0:** Authorize the Chef to create/read only their own requests and the operator to read only requests for managed Kitchens.
**REQ-BRQ-04 — P0:** Send submission confirmation without implying acceptance.
**REQ-BRQ-05 — P0:** Never create payment, deposit, tax, ledger, payout, or equipment-allocation records.
**REQ-BRQ-06 — P2:** Defer multi-Space requests and request negotiation threads.

## 19. Booking state model

### 19.1 P0 statuses

| Status | Capacity | Meaning | Terminal |
|---|---|---|:---:|
| **REQUESTED** | Does not reserve | Awaiting operator decision. | No |
| **CONFIRMED** | Reserves | Operator accepted and the atomic capacity transition succeeded. | No |
| **DECLINED** | Does not reserve | Operator declined with optional reason code/message. | Yes |
| **WITHDRAWN** | Does not reserve | Chef withdrew before operator decision. | Yes |
| **CANCELLED** | Does not reserve after transition | An authorized party cancelled a confirmed future booking; history and reason remain. | Yes |

“Past” is derived from the booking end/occupancy end for dashboard grouping. A separate COMPLETED transition is not required for P0; the existing architecture may retain it if reconciliation shows it is needed without creating user work.

### 19.2 Deferred statuses

- **EXPIRED — P1:** automatic or operator-configured expiry requires response-window policy and notifications.
- **ALTERNATIVE_PROPOSED — P2:** requires proposal versioning, Chef acceptance/rejection, capacity-hold policy, and more notifications.
- **HELD:** retained by Accepted ADR-007 for approved hold/checkout or later workflows, but not used by the P0 request/approval flow.
- **PAYMENT_PENDING:** incompatible with this no-payment pilot flow and not a P1-MVP-01 state.

### 19.3 Allowed transitions

~~~text
REQUESTED → CONFIRMED
REQUESTED → DECLINED
REQUESTED → WITHDRAWN
CONFIRMED → CANCELLED
~~~

No transition may return a terminal request to REQUESTED. A new time requires a new request in P0.

**REQ-STA-01 — P0:** Enforce the five P0 statuses and transition ownership server-side.
**REQ-STA-02 — P0:** Preserve status history, actor, timestamp and optional reason.
**REQ-STA-03 — P0:** Reject stale/duplicate decisions with 409 Conflict.
**REQ-STA-04 — P1:** Add configurable request expiry only after policy approval.
**REQ-STA-05 — P2:** Defer alternative proposals and rescheduling-in-place.

## 20. Concurrency/overlap handling

Accepted ADR-007 remains authoritative. Confirmed exclusive KitchenSpace occupancy uses a half-open interval from start to occupancy end, where occupancy end includes cleaning. PostgreSQL's GiST exclusion constraint, not a client-side calendar check or Redis lock, is the final guarantee.

P0 behavior:

1. Multiple overlapping REQUESTED records are allowed and visible to the operator.
2. REQUESTED records do not consume Space capacity.
3. When an operator accepts, the server locks/reloads the request, verifies authorization and status, re-evaluates availability, calculates occupancy, and attempts the REQUESTED → CONFIRMED transaction.
4. The first compatible acceptance may commit.
5. A concurrent or later incompatible acceptance receives 409 BOOKING_CONFLICT. Its request remains REQUESTED; it is not silently declined or modified.
6. The operator is shown the conflicting confirmed time and may decline the remaining request. Propose-alternative is not offered in P0.
7. Back-to-back bookings are allowed only when the later start is at or after the previous occupancy end, including cleaning.

Application-level conflict previews improve UX but never replace the database constraint. Idempotency prevents a retry from creating a second booking or repeated notification side effects.

**REQ-CON-01 — P0:** Preserve ADR-007 database-enforced exclusive-Space overlap behavior.
**REQ-CON-02 — P0:** Keep REQUESTED non-reserving and allow overlapping requests.
**REQ-CON-03 — P0:** Make acceptance an atomic, idempotent, revalidated transition.
**REQ-CON-04 — P0:** Return a clear 409 conflict and leave the losing request unchanged.
**REQ-CON-05 — P0:** Include cleaning time and half-open interval semantics in UI and tests.
**REQ-CON-06 — P2:** Defer shared fractional-capacity and cross-Space pooled-resource concurrency.

## 21. Operator decision flow

The Pending requests view orders actionable requests by requested start and received time. Each row and detail screen shows:

- Chef and optional business name;
- Kitchen/Space;
- requested start, use end, cleaning and occupancy end;
- Kitchen timezone and, when helpful, operator device-time conversion;
- duration and intended activity;
- equipment, storage, setup and cleanup needs;
- safe requirement declarations;
- selected rental offer and request-time estimate/terms snapshot;
- Chef message;
- current availability and confirmed-booking conflicts;
- request age and status.

### 21.1 Accept

Accept requires a confirmation dialog summarizing the exact Space/time and explaining that a confirmed booking will be created without payment collection. The server performs the atomic transition in Section 20. Success:

- changes status to CONFIRMED;
- creates status/audit history;
- preserves the request and price snapshots;
- produces in-app notification immediately;
- schedules email asynchronously;
- makes the booking visible in both dashboards.

If current availability no longer permits the request or a confirmed overlap exists, acceptance fails without partial state. The request stays REQUESTED and the operator sees a specific reason.

### 21.2 Decline

Decline requires confirmation. A controlled reason is optional but encouraged:

- NO_CAPACITY;
- SCHEDULE_MISMATCH;
- EQUIPMENT_MISMATCH;
- REQUIREMENT_MISMATCH;
- ACTIVITY_NOT_SUPPORTED;
- PRICE_OR_FORMAT_MISMATCH;
- OTHER.

An optional short message is visible to the Chef. Internal notes are not part of P0. Decline changes the record to DECLINED and releases no capacity because none was reserved.

### 21.3 Alternative

PROPOSE ALTERNATIVE is P2. In P0 the operator may decline with a short safe explanation and the Chef can submit a new request. This avoids proposal versions, two-party acceptance, temporary capacity holds and expiry policy.

**REQ-DEC-01 — P0:** List and display all operator-review fields and live conflicts.
**REQ-DEC-02 — P0:** Accept through the atomic transition and notify both parties.
**REQ-DEC-03 — P0:** Decline with optional reason/message and immutable history.
**REQ-DEC-04 — P0:** Prevent decision actions on a stale or non-REQUESTED record.
**REQ-DEC-05 — P2:** Defer alternative-proposal workflow.

## 22. Chef booking management

The Chef can:

- view all own records;
- filter/group as Pending, Confirmed, Declined, Cancelled, and Past;
- open request/booking details and status history;
- withdraw a REQUESTED record;
- cancel a future CONFIRMED booking;
- see operator-visible request content and the request-time pricing snapshot.

### 22.1 Withdrawal

WITHDRAW is P0 while status is REQUESTED. It requires confirmation and an optional reason. It does not require operator approval and sends an operator notification. An operator decision racing with withdrawal is resolved by the first valid atomic transition; the second receives 409 and reloads state.

### 22.2 Cancellation

Pilot cancellation is a status change and communication mechanism, not a financial policy. Either the Chef or authorized operator may cancel a future CONFIRMED booking with a required reason. The UI states:

> Cheffy Bites does not process payment, deposits or refunds in this pilot. Cancelling here updates the pilot booking record and notifies the other party. Any off-platform terms must be handled directly under the parties' applicable agreement.

Cancellation releases capacity under ADR-007 and preserves history. P0 has no penalty, refund, fee, deadline, or automatic dispute workflow. A past booking cannot be cancelled through the ordinary UI.

**REQ-CBM-01 — P0:** Provide the five groupings and full detail.
**REQ-CBM-02 — P0:** Allow a Chef to withdraw a pending request atomically.
**REQ-CBM-03 — P0:** Allow authorized cancellation of a future confirmed booking with a reason and notification.
**REQ-CBM-04 — P0:** State the no-financial-policy boundary at cancellation.
**REQ-CBM-05 — P2:** Defer penalties, refunds, disputes and rescheduling.

## 23. Operator dashboard

The responsive-web dashboard contains:

1. **My Kitchens** — status, active Spaces, availability health, requestability and actions.
2. **Pending requests** — real count and ordered worklist.
3. **Upcoming confirmed bookings** — next bookings with Space/time and Chef.
4. **Past bookings** — date-derived history, including cancelled/declined filters where useful.
5. **Availability management** — direct entry to each Space calendar/rules.
6. **Profile/account** — user and Organization settings.

Counts must be computed from the user's authorized real data and may never be synthetic. The dashboard shows DEMO or REAL context persistently. A banner warns when a PUBLISHED Kitchen is not requestable because pilot authorization, offers or future availability are missing.

**REQ-ODB-01 — P0:** Deliver all six dashboard areas from authorized data.
**REQ-ODB-02 — P0:** Show truthful requestability and real counts.
**REQ-ODB-03 — P0:** Make urgent pending/conflict items keyboard reachable and not color-only.
**REQ-ODB-04 — P1:** Add lightweight reason-code summaries for pilot operations.
**REQ-ODB-05 — P2:** Defer revenue, utilization and predictive analytics.

## 24. Chef dashboard

The responsive-web dashboard contains:

1. My requests/bookings;
2. Pending;
3. Confirmed;
4. Declined;
5. Cancelled;
6. Past;
7. Profile/account.

These may be tabs or accessible filters over one list; they are not required to be separate routes. Each record includes Kitchen/Space, general area, date/time/timezone, status, estimate snapshot and the next permitted action.

**REQ-CDB-01 — P0:** Deliver all seven dashboard capabilities from authorized data.
**REQ-CDB-02 — P0:** Update state after decisions without requiring a fresh login.
**REQ-CDB-03 — P0:** Provide empty states that do not imply unavailable market supply or traction.
**REQ-CDB-04 — P1:** Add native-mobile dashboard parity for the core filters.
**REQ-CDB-05 — P2:** Defer spending summaries and loyalty features.

## 25. Notifications

P0 uses both in-app and email for the small set of decision-critical events:

| Trigger | Chef | Operator | Channels |
|---|---|---|---|
| Request submitted | Confirmation | New-request notice | In-app + email |
| Request withdrawn | Confirmation | Withdrawal notice | In-app + email |
| Request confirmed | Decision notice | Confirmation | In-app + email |
| Request declined | Decision notice | Confirmation | In-app + email |
| Confirmed booking cancelled | Cancellation notice/confirmation | Cancellation notice/confirmation | In-app + email |

Email is asynchronous through the existing Notification boundary and outbox where justified. A notification failure never rolls back a valid request or decision. Failed delivery is observable and retryable. In-app status is sourced from the authoritative record even if every notification fails.

Notification content includes safe identifiers, general Kitchen name/Space and date/time, but no access code, private credential content, confidential operator note, or unnecessary personal data. Links require authentication. Locale follows the recipient's preferred language at send time; the decision record remains language-neutral.

No SMS is included. Push notification is P1 with native mobile.

**REQ-NOT-01 — P0:** Create localized in-app and email notifications for the five triggers.
**REQ-NOT-02 — P0:** Decouple delivery failures from domain-transaction success.
**REQ-NOT-03 — P0:** Record delivery attempt/status without sensitive payload logging.
**REQ-NOT-04 — P1:** Add native push for new requests and decisions.
**REQ-NOT-05 — P2:** Defer SMS and notification-preference complexity beyond essential service messages.

## 26. Demo mode

### 26.1 Isolation pattern

Use two protections:

1. **Environment isolation:** local/staging demo data is held in databases and identity tenants that cannot be queried by production.
2. **Record classification:** every Kitchen, account and booking seed has a non-user-editable DEMO classification. Real production-created records use REAL. Server queries require an explicit permitted mode.

DEMO is not a normal operator-editable badge. A user cannot change DEMO to REAL. Promotion of data between modes is prohibited; create a fresh REAL record through the real onboarding flow.

### 26.2 Demo identities and display

Fictional DEMO_OPERATOR and DEMO_CHEF accounts may be provisioned through a safe non-production process. All fictional facility names, images, addresses and terms must be invented. Food Factory, Kitchub, BocoLoc, Co-Work, Kitchen Six, Le Kitch and other researched entities must not appear as participants.

The header, listing cards, detail, dashboards, email templates and printed screenshots carry a persistent “Demonstration — fictional data” treatment. The LP-01 public demo remains separate and non-persistent; it may link to a safe demo entry point but does not become a real authenticated record automatically.

### 26.3 Reset

R0–R2 demo reset acts only on known demo tenant/data scopes, is restricted and audited, and never uses a broad delete condition. R3 production has no demo-reset action over REAL data.

**REQ-DMO-01 — P0:** Enforce environment plus immutable record-mode separation.
**REQ-DMO-02 — P0:** Mark every demo surface and generated notification visibly and accessibly.
**REQ-DMO-03 — P0:** Use only fictional operator/facility data.
**REQ-DMO-04 — P0:** Provide a scoped deterministic reset that cannot affect REAL data.
**REQ-DMO-05 — P2:** Defer converting demo records to real records.

## 27. Feedback

Authenticated users can submit lightweight contextual feedback from dashboards and key workflow screens.

### 27.1 Fields

- category: Availability, Pricing, Kitchen listing, Booking request, Operator workflow, Chef workflow, Requirements/compliance, or Other;
- free text;
- current role;
- locale;
- optional product route/context and related resource type/id;
- user and Organization identifiers captured server-side;
- created timestamp;
- triage status visible only to admin tooling.

The form warns: “Do not include confidential business information, access codes, health information, identity documents or payment information.” It links to the privacy notice and explains purpose, access and retention at the point of collection. Feedback is visible only to authorized pilot administrators, not automatically to the other marketplace party.

Feedback supplements, but never replaces, MI-06/MI-07 interviews. A feedback count or sentiment tag is not an MI-01 validation result.

**REQ-FBK-01 — P0:** Persist categorized free-text feedback with authenticated context.
**REQ-FBK-02 — P0:** Display collection/privacy guidance and prohibit sensitive submissions.
**REQ-FBK-03 — P0:** Restrict feedback access and exclude text from general analytics/logs.
**REQ-FBK-04 — P1:** Add admin triage/export and resolution notes.
**REQ-FBK-05 — P2:** Defer public voting, support tickets and automated sentiment analysis.

## 28. Minimum admin

P0 administration is deliberately small.

### 28.1 Required controls

- list/search pilot users and account status;
- list/search Kitchens with Organization, publication, mode and pilot-authorization state;
- inspect request/booking state and history read-only;
- grant/revoke REAL pilot visibility authorization;
- emergency-unpublish a problematic Kitchen with reason;
- deactivate/reactivate an account with reason;
- inspect feedback;
- inspect failed notification/outbox/operational state;
- distinguish DEMO and REAL everywhere.

Admin does not accept or decline ordinary operator requests, edit operator listing copy as though it were the operator, or change booking history silently.

Secure database/operations tooling may satisfy read-only inspection, feedback export and failure inspection during R0–R3. Pilot authorization, emergency unpublish and account deactivation require narrow application commands or equivalent audited controls; ad hoc row editing is not an acceptable normal operation.

**REQ-ADM-01 — P0:** Provide the required controls through secure tooling and minimal audited commands.
**REQ-ADM-02 — P0:** Require dedicated admin permissions, MFA where supported and reason capture for mutations.
**REQ-ADM-03 — P0:** Audit actor, action, resource, before/after state, time and correlation ID.
**REQ-ADM-04 — P1:** Add a minimal internal web console if operational frequency justifies it.
**REQ-ADM-05 — P2:** Defer a general-purpose administration platform.

## 29. Public LP-01 integration

### 29.1 Public routes

Retain:

- /
- /kitchens
- /chefs
- /kitchens/join
- /demo/kitchen
- /privacy
- /terms
- their approved French equivalents.

### 29.2 Authenticated route recommendation

For the narrow pilot, use one Next.js deployment and a protected route family:

- /app/sign-in
- /app/operator/...
- /app/chef/...
- /app/admin/... only if a P1 admin UI is built.

This reduces R0–R3 deployment and design-system duplication while maintaining role-aware navigation and server authorization. It is a conscious pilot recommendation that differs from the master specification's separate business/chef/customer web applications and subdomains. It therefore requires explicit architecture reconciliation; it is not silently accepted by this product document. A future split can reuse the shared API client and design system.

Public pages do not expose authenticated navigation until the founder approves the stage copy and privacy/terms changes. In CONTROLLED PILOT, “Sign in” may be available to invited participants without implying open registration or broad inventory.

### 29.3 Stage wording

Before R3, public copy continues to say there is no live inventory/booking. Before enabling REAL requestability, LP-01 copy and metadata must be reviewed deliberately so the statements remain true. Safe controlled-pilot wording should say that participation is limited/invitation-only, operator approval is required, and no broad marketplace availability is claimed.

**REQ-PUB-01 — P0:** Preserve and localize the public route set and demo protections.
**REQ-PUB-02 — P0:** Separate public and protected route authorization/cache behavior.
**REQ-PUB-03 — P0:** Complete an approved stage-copy/metadata review before REAL requestability.
**REQ-PUB-04 — P0:** Do not publish REAL inventory in SEO structured data or public sitemaps by default.
**REQ-PUB-05 — P1:** Revisit separate web applications/subdomains after pilot evidence.

## 30. Authentication/authorization

Auth0/OIDC remains the identity provider. The application does not store passwords or invent a second identity system.

### 30.1 P0 flow

1. OIDC authentication returns a validated principal.
2. The backend bootstraps/binds an internal User on first authorized access.
3. Onboarding selects Chef or Operator intent; an admin role is never self-selected.
4. Operator onboarding creates/joins an Organization membership.
5. Chef onboarding creates the durable ChefProfile/food-business context.
6. Every API applies scope/permission plus resource ownership and state rules.

Proposed roles/capabilities are OPERATOR_OWNER, OPERATOR_MANAGER, CHEF and ADMIN, mapped to granular permissions. Existing canonical role names must be reconciled rather than duplicated if their semantics already match.

Organization IDs supplied by a client are never trusted as authorization. Admin claims require server-recognized role/permission assignment. Access tokens are short-lived, browser sessions/refresh behavior are secure, and protected pages cannot leak through static caching.

**REQ-AUT-01 — P0:** Use Auth0/OIDC, token validation and internal user binding.
**REQ-AUT-02 — P0:** Support Chef, operator owner/manager and admin authorization semantics.
**REQ-AUT-03 — P0:** Enforce Organization/resource ownership on every protected read and mutation.
**REQ-AUT-04 — P0:** Prevent role self-escalation and protect admin assignment.
**REQ-AUT-05 — P1:** Support account invitations and step-up/MFA policy for admins/operators where configured.
**REQ-AUT-06 — P2:** Defer social-role linking complexity and enterprise SSO.

## 31. Mobile MVP

Native mobile uses React Native, Expo and TypeScript and consumes the same versioned backend APIs/OpenAPI-generated client as web. It contains no independent availability, pricing, decision or concurrency rules.

### 31.1 Recommendation

Complete responsive web through R2 before native mobile. Native mobile is P1, targeted for R3/R4 only if it helps a real participant.

**Chef native P1:**

- sign in;
- search;
- Kitchen/Space detail;
- submit request;
- view request/booking status;
- withdraw/cancel;
- receive push where configured.

**Operator native P1:**

- pending-request push;
- pending list and detail;
- accept/decline;
- upcoming confirmed bookings.

**Web-first through the pilot:**

- Organization onboarding;
- Kitchen and Space creation/editing;
- photo management;
- equipment configuration;
- rental-offer configuration;
- complex availability rule/block management;
- admin controls.

The responsive web experience remains usable on mobile, so native apps are not required to operate R0–R2.

**REQ-MOB-01 — P0:** Keep backend/OpenAPI behavior client-neutral and responsive web mobile-usable.
**REQ-MOB-02 — P1:** Deliver the Chef native core.
**REQ-MOB-03 — P1:** Deliver the operator decision/upcoming-bookings core.
**REQ-MOB-04 — P1:** Prove Flow 9 against the same production-like backend.
**REQ-MOB-05 — P2:** Defer native Kitchen/availability administration.

## 32. English/French requirements

The public site remains fully English/Quebec French under LP-01. For R3, every participant-facing authenticated P0 path must also be bilingual:

- navigation and account onboarding;
- profile fields and validation;
- Kitchen/Space/equipment/rental-offer management;
- availability/calendar labels and timezone help;
- search, result labels and Kitchen detail;
- request, decision, withdrawal and cancellation flows;
- status names, reason codes and errors;
- dashboard empty/loading/conflict states;
- in-app/email notifications;
- privacy/collection notices and feedback;
- accessibility labels and media alt text.

User-entered listing, message and feedback content is not machine translated in P0. The interface labels it as operator/participant-entered and displays the original language. Operators should be able to provide bilingual listing text fields where available; a missing translation must not be fabricated.

Use en-CA and fr-CA. Professional Quebec French review is a gate before R3 and before any broader public use. The review includes transactional errors and emails, not only marketing pages. Founder limitations in French remain an operating hypothesis and must not be hidden by bilingual UI.

**REQ-I18N-01 — P0:** Externalize and localize all participant-facing P0 system content.
**REQ-I18N-02 — P0:** Preserve language across equivalent routes/actions and notification links.
**REQ-I18N-03 — P0:** Complete qualified Quebec French review before R3.
**REQ-I18N-04 — P0:** Never claim end-to-end French operations merely because UI text exists.
**REQ-I18N-05 — P1:** Support operator-provided bilingual listing fields and translation-completeness indicators.

## 33. Accessibility

WCAG 2.2 AA remains the direction. P0 acceptance includes:

- full keyboard operation for onboarding, calendars, dialogs, search, decisions and dashboards;
- logical focus and visible focus;
- semantic headings, landmarks, labels, descriptions and error summaries;
- status/conflict/demo meaning conveyed with text/iconography, not color alone;
- calendar alternatives that do not require pointer or visual grid use;
- screen-reader announcement of selected date/time, timezone, duration and conflicts;
- no horizontal scrolling at 320 CSS pixels for core flow;
- usable 200% zoom and reflow;
- minimum target-size direction and accessible touch interactions;
- localized accessible names and errors;
- photo alt text and captions appropriate to real/demo status.

Time selection must always state the Kitchen timezone. If a viewer-time conversion is shown, the two values must be labelled rather than relying on order or color.

**REQ-A11Y-01 — P0:** Meet the listed keyboard, semantic, responsive, timezone and non-color criteria.
**REQ-A11Y-02 — P0:** Provide a non-grid path for availability entry and request selection.
**REQ-A11Y-03 — P0:** Test automated checks plus manual keyboard and screen-reader spot checks for flows 1–10.
**REQ-A11Y-04 — P1:** Complete broader assistive-technology coverage on iOS and Android native apps.

## 34. Security/privacy

### 34.1 Security minimum

- authenticated protected routes and APIs;
- short-lived OIDC tokens and secure refresh/session handling;
- HTTPS and explicit CORS/CSRF strategy;
- server-side input, state and authorization validation;
- rate limiting for sign-in-adjacent, search, request and feedback endpoints;
- secrets outside source control;
- S3 pre-signed uploads, MIME/size validation and safe delivery;
- output encoding and safe rich-text policy;
- no access tokens, secrets, exact private addresses, access codes or sensitive free text in logs;
- audit-relevant timestamps and actors;
- dependency/backup/restore and incident-response basics proportional to a pilot.

### 34.2 Privacy minimum

Collect only information necessary for account, listing, request, decision, notification, feedback and pilot operations. Publish bilingual privacy information and just-in-time notices. Define purpose, access, correction, deletion/retention, service providers, processing locations, incident contact and privacy-responsible contact before R3.

Address/access privacy follows Section 11. Requirement declarations are not verification records. No identity, permit, insurance, health, payment or confidential contract documents are stored in P0. Free text is character-limited and accompanied by “do not include sensitive information” guidance.

Account deactivation prevents new access/transactions but does not erase booking/audit history automatically. Retention/deletion policy must distinguish operational records, legal/audit needs and feedback; exact periods are R3 policy gates, not invented here.

**REQ-SEC-01 — P0:** Implement the security minimum above and test broken-access-control cases.
**REQ-SEC-02 — P0:** Apply data minimization, field-level visibility and safe logging.
**REQ-SEC-03 — P0:** Complete a proportionate privacy assessment and approved notices before R3.
**REQ-SEC-04 — P0:** Define retention/deletion and participant-rights handling before REAL data collection.
**REQ-SEC-05 — P1:** Add malware scanning where upload risk/provider capability justifies it.
**REQ-SEC-06 — P2:** Defer sensitive credential-document collection.

## 35. Product analytics

Product analytics are privacy-gated operational evidence, not market validation. Prefer first-party or minimal collection, pseudonymous/internal IDs, no free text, no exact address/coordinates, and no sensitive requirement values.

### 35.1 P0 event vocabulary

| Event | Minimum safe properties |
|---|---|
| operator_signup_completed | locale, release, demo_real |
| kitchen_draft_created | kitchen type, release, demo_real |
| kitchen_published | release, demo_real, active_space_count |
| availability_created | kind, space scope, release |
| chef_signup_completed | locale, release, demo_real |
| kitchen_search_performed | filter-presence flags, result-count band, release; no raw location |
| kitchen_viewed | internal Kitchen/Space IDs, request-context-present |
| booking_request_started | Space ID, request-context-present |
| booking_request_submitted | request ID, format basis, release, demo_real |
| booking_request_confirmed | request ID, format basis, conflict-retry flag |
| booking_request_declined | request ID, controlled reason only |
| feedback_submitted | category, route/context, locale |

Lifecycle timestamps in authoritative domain/audit records remain the source for operational truth. Client analytics do not define booking state or completed actions. Dashboards and reports must call these product events, not validations, commitments, demand or supply proof.

**REQ-ANA-01 — P0:** Instrument the listed events with privacy-safe properties after privacy approval.
**REQ-ANA-02 — P0:** Use server/domain confirmation for completed lifecycle events.
**REQ-ANA-03 — P0:** Exclude messages, feedback text, exact location and requirement content.
**REQ-ANA-04 — P0:** Keep analytics nomenclature/reporting separate from MI-01 status.
**REQ-ANA-05 — P1:** Add consent/preference behavior if required by the approved analytics configuration.

## 36. Observability

The pilot must make the five core transactions diagnosable without exposing participant data: authentication/bootstrap, Kitchen draft/publication, availability mutation/evaluation, request submission, and operator decision/notification.

### 36.1 Required signals

| Signal | Minimum measure/alert |
|---|---|
| Failed sign-ins/bootstrap | Count/rate by environment and safe reason class; identity-provider outage visibility. |
| Failed Kitchen creation/publication | Count by validation, authorization, media and server failure. |
| Availability failures | Mutation errors, recurrence/DST resolution errors and evaluation latency. |
| Booking-request failures | Validation, idempotency, authorization and persistence error rates. |
| Operator-decision failures | 409 overlap/stale-state counts separated from 5xx; transaction latency. |
| Notification failures | Queue age, attempts, permanent failures and provider health. |
| Server health | Request latency/error rate, database pool/latency, outbox backlog, storage errors and background-job health. |

Each request carries a trace ID and, where applicable, correlation ID. Structured logs use internal IDs sparingly and never contain access tokens, email/phone, exact private address, messages, requirement answers or feedback text. Operational dashboards separate DEMO and REAL.

**REQ-OBS-01 — P0:** Instrument the required signals, traces and safe structured logs.
**REQ-OBS-02 — P0:** Distinguish expected 409 booking conflicts from platform failures.
**REQ-OBS-03 — P0:** Alert on request/decision/notification failure and outbox backlog appropriate to pilot hours.
**REQ-OBS-04 — P0:** Provide a correlation path from user-visible error to server trace without exposing internals.
**REQ-OBS-05 — P1:** Define pilot service-level targets after R2 measurements rather than inventing production-scale targets.

## 37. Existing architecture reconciliation

This section classifies each MVP concept against the canonical baseline. “Needs MVP detail” means the concept exists but does not currently satisfy this product workflow. “Conflict” means implementation must stop until the owning canonical source is reconciled.

| MVP concept | Classification | Existing source/reuse | Finding/action |
|---|---|---|---|
| Modular backend | **EXISTING** | ADR-001; Kitchen, Booking, Equipment, Organization, Chef, Notification and Administration modules | Reuse one Spring Boot modular monolith. |
| Async integration | **EXISTING** | ADR-002, ADR-009, ADR-016; outbox | Reuse selectively for notification/analytics side effects. |
| Web stack | **EXISTING** | ADR-003; React/Next.js/TypeScript | Reuse. |
| Mobile stack | **EXISTING** | Master/detailed architecture | Reuse React Native/Expo/TypeScript with shared client. |
| Identity | **EXISTING BUT NEEDS MVP DETAIL** | Auth0/OIDC, User, role/permission model | Bootstrap/profile fields and exact pilot roles/permissions need reconciliation. |
| Organization/multiple Kitchens | **EXISTING** | Organization → Location → Kitchen → Space | Direct reuse; no one-user/one-Kitchen assumption. |
| Operator account profile | **NEW PRODUCT REQUIREMENT** | User table has only auth subject/status | Profile/preferred-language/contact persistence is missing or unspecified. |
| Chef profile | **EXISTING BUT NEEDS MVP DETAIL** | ChefProfile exists | Business category, preferred language, safe contact/requirement fields are incomplete. |
| Kitchen lifecycle | **EXISTING BUT NEEDS MVP DETAIL** | Kitchen status/publication | Product fixes DRAFT/PUBLISHED/UNPUBLISHED; API only exposes publish, not full CRUD/unpublish/preview. |
| DEMO/REAL and pilot authorization | **NEW PRODUCT REQUIREMENT** | No canonical persistence/API concept found | Required to prevent fictional/real confusion and silent publication. |
| Kitchen photos/media | **EXISTING BUT NEEDS MVP DETAIL** | S3/media architecture | Exact Kitchen/Space media relation and APIs are absent from canonical ERD/API. |
| Address/privacy visibility | **NEW PRODUCT REQUIREMENT** | Location and security principles exist | Coarse-area versus exact-address visibility needs explicit contract. |
| KitchenSpace | **EXISTING BUT NEEDS MVP DETAIL** | KitchenSpace and create-Space API | Access/storage/constraints/active behavior need detail; exclusive Space is reused. |
| Equipment catalog/Space equipment | **EXISTING BUT NEEDS MVP DETAIL** | EquipmentCatalogItem, SpaceEquipment | P0 availability modes INCLUDED/SHARED/EXTRA_DISCUSS need representation. |
| Advanced equipment rental | **DEFERRED IN EXISTING ARCHITECTURE** for this pilot | EquipmentRental/Booking/Allocation and ADR-007 | Do not implement in P0/P1. |
| Rental formats | **CONFLICT / NEW PRODUCT REQUIREMENT** | KitchenSpace stores hourly rate/minutes; create-Space API assumes hourly | Mixed hourly/block/daily/recurring/monthly/inquiry offers require new canonical model. |
| Pricing estimate | **EXISTING BUT NEEDS MVP DETAIL** | Pricing module/quote exists | Existing quote includes promotion/fee/tax/payment; pilot needs limited non-financial estimate and snapshot. |
| Recurring availability | **EXISTING BUT NEEDS MVP DETAIL** | Kitchen/Space availability tables | Existing day/time/recurrence supports a base rule but lacks explicit effective dates/precedence detail. |
| One-time availability | **NEW PRODUCT REQUIREMENT** | Not represented in Kitchen availability tables | Persistence/API delta required. |
| Blocked exceptions | **NEW PRODUCT REQUIREMENT** | Closed/blackout concept in master only | Persistence/API/evaluation precedence required. |
| Timezone model | **CONFLICT IN DECISION STATUS** | ADR-011 is Proposed while ERD/API already follow it | Resolve ADR status and canonical consistency before implementation; do not silently accept. |
| Search | **EXISTING BUT NEEDS MVP DETAIL** | Kitchens search filters, PostgreSQL/PostGIS | Eligibility/mode/approval labels and honest price comparability need contract detail. |
| Booking request | **CONFLICT** | Existing create-booking returns PAYMENT_PENDING plus payment secret | Pilot requires persisted REQUESTED state with no payment/hold. Canonical API and state model must change/add a separate request use case on the same aggregate. |
| Request context/snapshot | **NEW PRODUCT REQUIREMENT** | KitchenBooking table lacks request message/activity/offer snapshot | Persistence delta required. |
| Operator decision | **NEW PRODUCT REQUIREMENT / CONFLICT** | Existing booking flow is checkout/payment confirmation | Accept/decline API/state transitions and authorization need canonical definition. |
| Booking concurrency | **EXISTING** | Accepted ADR-007; GiST exclusion | Reuse exactly for CONFIRMED capacity; REQUESTED is non-reserving. |
| HELD | **EXISTING, NOT USED IN P0** | ADR-007 | Retain for other approved workflows; do not force into operator-request flow. |
| Confirm/cancel events | **EXISTING** | KitchenBookingConfirmed.v1 and Cancelled.v1 | Reuse after payload review. |
| Request/decline/withdraw events | **NEW PRODUCT REQUIREMENT** | Not in event catalogue | Add only if selected for async notification/analytics. |
| Dashboards | **DEFERRED/UNDERSPECIFIED** | Client containers and API principles exist | Product/API query contracts needed. |
| Notifications | **EXISTING BUT NEEDS MVP DETAIL** | Notification module/event-driven guidance | Exact record/delivery model and pilot events are missing from canonical ERD/API. |
| Feedback | **NEW PRODUCT REQUIREMENT** | No canonical concept found | Minimal protected persistence/API/admin access required. |
| Admin | **EXISTING BUT NEEDS MVP DETAIL** | Administration module/APIs | Pilot controls for authorization/unpublish/deactivation are absent. |
| Bilingual authenticated UI | **NEW PRODUCT REQUIREMENT** relative to authenticated baseline | LP-01 governs public bilingual content | Shared locale/error/email contracts needed. |
| Accessibility | **EXISTING BUT NEEDS MVP DETAIL** | LP-01 WCAG 2.2 AA direction | Calendar/decision/timezone criteria added here. |
| Product analytics | **NEW PRODUCT REQUIREMENT** | Analytics/event infrastructure is generic | Privacy-safe event vocabulary required; no validation semantics. |

### 37.1 Conflicts that architecture reconciliation must resolve

1. **Payment-coupled booking contract:** POST /api/v1/kitchen-bookings currently returns PAYMENT_PENDING and provider clientSecret. That contract cannot implement the no-payment request/approval pilot.
2. **Booking lifecycle:** canonical persistence protects HELD/CONFIRMED and mentions CANCELLED/COMPLETED; the pilot additionally requires REQUESTED, DECLINED and WITHDRAWN with non-reserving semantics.
3. **Pricing cardinality/model:** KitchenSpace's single hourly rate cannot represent the evidence-required mixed format set honestly.
4. **Availability representation:** existing tables do not clearly represent one-time availability, blocks, effective dates and precedence.
5. **Timezone authority status:** ADR-011 is Proposed despite canonical ERD/API text relying on it.
6. **Pilot web topology:** this spec recommends protected /app routes for a narrow pilot, while the master architecture describes separate business and Chef web applications/subdomains.

No conflict is resolved by this document alone.

## 38. Proposed database delta

This is a product-level persistence delta, not DDL. The architecture task must choose exact schemas, tables, keys, constraints and migrations.

### 38.1 Existing entities required unchanged in concept

- User/identity-provider binding;
- Organization, OrganizationMember and Role;
- Location;
- Kitchen;
- KitchenSpace;
- EquipmentCatalogItem and SpaceEquipment;
- KitchenAvailability and KitchenSpaceAvailability concepts;
- KitchenBooking;
- ChefProfile/Chef business relationship where applicable;
- OutboxEvent;
- audit and notification concepts where canonical representation is established.

### 38.2 Extensions or missing concepts

| Concept | Required product data | Why existing model is insufficient |
|---|---|---|
| User/profile detail | display/contact name, preferred locale, optional phone, account status | USERS lacks participant profile/contact/locale fields. |
| Organization business profile | operator-facing name and optional general business information | Organization name/type/status alone may not cover onboarding profile. |
| Pilot classification/control | immutable DEMO/REAL, pilot authorization, stage eligibility, actor/time/reason | No safe demo/real or controlled-pilot gate exists. |
| Kitchen publication/visibility detail | three product states, visibility policy, operator affirmation, publish/unpublish actor/time | Current Kitchen has status/published_at but not the full control/audit semantics. |
| Media association | Kitchen/Space media reference, ordering, alt/caption, visibility/status | S3 architecture exists; relational association is unspecified. |
| General-area/address visibility | coarse public area and exact-address reveal policy | Location stores exact address/point without a product disclosure boundary. |
| KitchenSpace detail | access/storage/constraints/exclusivity/active semantics | Existing fields do not cover these P0 attributes. |
| Space equipment mode | INCLUDED/SHARED/EXTRA_DISCUSS, notes and display quantity | Existing included/rental flags do not honestly express all pilot modes. |
| RentalOffer | Space, basis, amount/currency, block/included units, duration, commitment, deposit display, charges/notes, active/version | Single hourly rate on KitchenSpace cannot represent market formats. |
| Availability rule/exception | Space, ONE_TIME_AVAILABLE/RECURRING_AVAILABLE/BLOCKED, local date/day/time, effective dates, recurrence data, active/version | Existing recurring tables lack complete one-time/block/effective representation. |
| Request context on KitchenBooking | activity, equipment/storage/setup/cleanup needs, message, requirement responses, submitted_at | Existing KitchenBooking contains resource/time/status only. |
| Request snapshots | Kitchen/Space labels, timezone, RentalOffer/version, amount/currency/estimate formula/disclaimer, relevant terms | Later listing/rate edits must not rewrite reviewed history. |
| Booking decision/history | transition, actor, timestamp, controlled reason, party-visible message | One current status/reason is inadequate audit/history. |
| Feedback | user, Organization/role, category, text, locale, context/resource, created time, triage state | Missing concept. |
| Notification/delivery attempt | recipient, type, locale, safe template data/reference, channel, status/attempt/error timing | Notification is conceptual but exact pilot persistence is absent from ERD. |
| Minimal requirement declaration | request/operator requirement reference, NOT_PROVIDED/DECLARED/REVIEWED_OUTSIDE_PLATFORM, safe note | Full credential system is out of scope; no minimal pilot representation exists. |

### 38.3 Required invariants for later persistence design

- DEMO cannot be changed to REAL.
- REAL requestability requires published Kitchen, active Space/offer/availability, CONTROLLED PILOT stage and pilot authorization.
- REQUESTED/DECLINED/WITHDRAWN do not participate in the ADR-007 exclusion constraint.
- CONFIRMED does participate; CANCELLED does not.
- A decision/history row is append-only in normal use.
- A request snapshot is immutable after submission.
- RentalOffer and availability edits use optimistic concurrency/versioning where appropriate.
- One-time/recurring rules are local business-time data; concrete request/booking boundaries are real instants.
- Feedback text and private address/access fields are never analytics payloads.

## 39. Proposed API delta

All endpoints are under /api/v1, use OIDC bearer authentication where protected, standard errors, pagination for lists, correlation IDs and idempotency on reservation-sensitive mutations. Exact paths must be reconciled into docs/04; the list below defines minimum use cases.

### 39.1 Common/profile

| Method/use case | Proposed endpoint |
|---|---|
| Get current principal/profile/permissions | GET /me — extend existing response |
| Update own participant profile/locale | PATCH /me/profile |
| Submit feedback | POST /feedback |
| List own notifications | GET /me/notifications |
| Mark in-app notification read | POST /me/notifications/{id}/read |

### 39.2 Operator/Organization/Kitchen

| Method/use case | Proposed endpoint |
|---|---|
| Create/get/update Organization | Existing POST /organizations plus GET/PATCH /organizations/{id} |
| Create/update Location | POST/PATCH /organizations/{id}/locations |
| List/create/get/update Kitchen | GET/POST /kitchens; GET/PATCH /kitchens/{id} with authorization |
| Preview Kitchen | GET /kitchens/{id}/preview |
| Publish/unpublish | POST/DELETE /kitchens/{id}/publication or an explicitly reconciled action contract |
| Create/update/deactivate Space | POST /kitchens/{id}/spaces; GET/PATCH /kitchen-spaces/{id} |
| Manage Space equipment modes | GET/PUT /kitchen-spaces/{id}/equipment |
| Manage RentalOffers | GET/POST /kitchen-spaces/{id}/rental-offers; PATCH/DELETE /rental-offers/{id} |
| Manage availability rules/blocks | GET/POST /kitchen-spaces/{id}/availability-rules; PATCH/DELETE /availability-rules/{id} |
| Preview resolved availability | GET /kitchen-spaces/{id}/availability?from=&to= |
| List requests for managed Kitchens | GET /operator/kitchen-booking-requests?status=&kitchenId= |
| Decide request | POST /kitchen-booking-requests/{id}/decision with ACCEPT or DECLINE and Idempotency-Key |
| List operator bookings | GET /operator/kitchen-bookings?view=upcoming|past |
| Cancel confirmed booking | POST /kitchen-bookings/{id}/cancellation |

### 39.3 Chef/discovery/request

| Method/use case | Proposed endpoint |
|---|---|
| Search eligible Spaces/Kitchens | GET /kitchens with expanded safe filters/status |
| Get eligible Kitchen detail | GET /kitchens/{id} |
| Submit non-reserving request | POST /kitchen-booking-requests with Idempotency-Key |
| List/get own requests/bookings | GET /me/kitchen-bookings; GET /kitchen-bookings/{id} |
| Withdraw pending request | POST /kitchen-booking-requests/{id}/withdrawal |
| Cancel confirmed booking | POST /kitchen-bookings/{id}/cancellation |

The term kitchen-booking-request in the URL is a product/API view of a KitchenBooking in REQUESTED state; it does not require a second persistence aggregate. The architecture task may select another path if it preserves the semantics clearly.

### 39.4 Admin

| Method/use case | Proposed endpoint/control |
|---|---|
| Inspect users/Kitchens/bookings/feedback | Narrow GET /admin/pilot/... endpoints or secure operational tooling |
| Authorize/revoke REAL pilot listing | POST/DELETE /admin/pilot/kitchens/{id}/authorization |
| Emergency unpublish | POST /admin/pilot/kitchens/{id}/unpublication with reason |
| Deactivate/reactivate account | POST /admin/pilot/users/{id}/status with reason |

### 39.5 Canonical API conflict treatment

The existing POST /kitchen-bookings checkout contract, quote fields for promo/tax/payment and PAYMENT_PENDING response are not implemented by this pilot. Architecture reconciliation must either:

- add the request resource/use case while preserving the later paid-booking endpoint as a distinct future contract; or
- version/redefine booking creation so request/approval and later checkout cannot be confused.

The P1-MVP-01 API must never return clientSecret, paymentId, fee, tax, promo or checkout fields.

## 40. Proposed event delta

Synchronous application logic owns validation, persistence and the decision response. Events are used only for reliable downstream notification and privacy-approved product analytics; they do not make booking state eventually consistent.

### 40.1 Reuse

- **KitchenPublished.v1** — existing; emit when an eligible publication transaction commits, subject to payload review.
- **KitchenBookingConfirmed.v1** — existing; emit in the same transaction as successful REQUESTED → CONFIRMED.
- **KitchenBookingCancelled.v1** — existing; emit with an authorized cancellation.

### 40.2 Proposed new events

| Event | Justification | Minimum safe payload |
|---|---|---|
| **KitchenBookingRequested.v1** | Notify operator and record lifecycle analytics reliably. | bookingId, Kitchen/Space IDs, Chef internal ID, start/useEnd/occupancyEnd, occurredAt; no message/free text. |
| **KitchenBookingDeclined.v1** | Notify Chef and record outcome. | bookingId, Kitchen/Space IDs, Chef internal ID, controlled reason code, resulting status. |
| **KitchenBookingWithdrawn.v1** | Notify operator and record outcome. | bookingId, Kitchen/Space IDs, Chef internal ID, resulting status. |

No event is required for:

- availability creation/edit during P0 unless a real consumer needs it;
- search/view analytics if synchronous privacy-safe capture is sufficient;
- feedback submission;
- dashboard reads;
- price estimate calculation.

All selected events must use ADR-016 envelope/version semantics and ADR-009 outbox persistence. Event insertion is in the same local transaction as the authoritative state change. Consumers are idempotent. Free text, exact address, contact details and requirement responses are omitted.

## 41. P0/P1/P2 requirements matrix

Every functional requirement in this document carries an inline priority. This matrix is the completeness index.

### 41.1 P0 — required for operator demo/controlled pilot

| Capability | Requirement IDs | P0 outcome |
|---|---|---|
| Objective/boundary | REQ-OBJ-01–04; REQ-VAL-01–04 | Real narrow workflow; no payments or false validation. |
| LP-01/stage | REQ-LP-01–05; REQ-PUB-01–04 | Evidence-safe public surface and controlled stage transition. |
| Personas/security | REQ-PERM-01–03; REQ-AUT-01–04; REQ-SEC-01–04 | Authenticated, organization-aware, privacy-safe authorization. |
| Releases/demo | REQ-REL-01–03; REQ-DMO-01–04 | Isolated fictional demo through authorized REAL pilot. |
| Journeys | REQ-OJ-01–04; REQ-CJ-01–03 | Complete responsive-web operator and Chef flows. |
| Profiles/Organization | REQ-OPR-01–03; REQ-CHP-01–03; REQ-ORG-01–03 | Minimum account/profile and multi-Kitchen Organization model. |
| Kitchen/Space | REQ-KIT-01–05; REQ-SPC-01–04 | Durable listing, preview/publication, multiple exclusive Spaces. |
| Equipment | REQ-EQP-01–03 | Catalog-backed honest equipment representation. |
| Pricing | REQ-PRC-01–05 | Mixed formats and safe non-payment estimates/snapshots. |
| Availability | REQ-AVL-01–05 | One-time, recurring, blocks, preview and authoritative recheck. |
| Search/listing | REQ-SCH-01–04; REQ-LST-01–04 | Real eligible discovery with honest match labels/detail. |
| Request/state | REQ-BRQ-01–05; REQ-STA-01–03 | Persisted non-reserving request and small state machine. |
| Concurrency/decision | REQ-CON-01–05; REQ-DEC-01–04 | Atomic accept/decline and database overlap safety. |
| Booking management | REQ-CBM-01–04 | View, withdraw and non-financial cancellation. |
| Dashboards | REQ-ODB-01–03; REQ-CDB-01–03 | Real data for both parties. |
| Notifications | REQ-NOT-01–03 | In-app/email essential service notices. |
| Feedback/admin | REQ-FBK-01–03; REQ-ADM-01–03 | Research feedback and minimal audited pilot operations. |
| Client strategy | REQ-MOB-01 | Shared backend and mobile-usable responsive web. |
| Bilingual/accessibility | REQ-I18N-01–04; REQ-A11Y-01–03 | R3 English/French and WCAG-direction core flows. |
| Analytics/operations | REQ-ANA-01–04; REQ-OBS-01–04 | Privacy-safe product signals and diagnosable pilot. |

### 41.2 P1 — early-pilot follow-ons

| Capability | Requirement IDs | Outcome |
|---|---|---|
| Research/operations | REQ-VAL-05, REQ-REL-04, REQ-ODB-04, REQ-FBK-04, REQ-ADM-04 | Reason-coded learning and efficient pilot administration. |
| Team/media | REQ-PERM-04, REQ-ORG-04, REQ-KIT-06, REQ-EQP-04 | Small-team and listing usability improvements. |
| Availability/search | REQ-AVL-06, REQ-SCH-05, REQ-LST-05 | Recurring blocks, convenience and controlled address reveal. |
| Lifecycle | REQ-STA-04 | Request expiry after explicit policy. |
| Notifications/mobile | REQ-CJ-04, REQ-NOT-04, REQ-MOB-02–04, REQ-CDB-04, REQ-A11Y-04 | Native Chef/operator decision core and push. |
| Auth/i18n/privacy | REQ-AUT-05, REQ-I18N-05, REQ-SEC-05, REQ-ANA-05 | Invitations, bilingual listing support and operational hardening. |
| Architecture evolution | REQ-PUB-05, REQ-OBS-05 | Evidence-based client topology and service targets. |

### 41.3 P2 — explicitly deferred

| Capability | Requirement IDs | Deferred outcome |
|---|---|---|
| Public/enterprise expansion | REQ-REL-05, REQ-PERM-05, REQ-AUT-06 | Open public marketplace, role builders and enterprise SSO. |
| Compliance/contracting | REQ-OPR-04, REQ-CHP-04, REQ-ORG-05, REQ-SEC-06 | Document verification, lease/sublicense/provider/payee complexity. |
| Capacity/pricing | REQ-SPC-05, REQ-EQP-05, REQ-PRC-06, REQ-AVL-07, REQ-CON-06 | Fractional/shared capacity, paid equipment, finance and calendars. |
| Discovery/social | REQ-CJ-05, REQ-KIT-07, REQ-SCH-06, REQ-CDB-05 | Public self-service listing expansion, recommendations, reputation, wishlists and spending/loyalty. |
| Negotiation/lifecycle | REQ-BRQ-06, REQ-STA-05, REQ-DEC-05, REQ-CBM-05 | Multi-Space requests, alternatives, rescheduling, disputes/penalties. |
| Native management | REQ-OJ-05, REQ-MOB-05 | Complex operator setup in native apps. |
| Communications/admin | REQ-NOT-05, REQ-FBK-05, REQ-ADM-05 | SMS, public feedback/support systems and large admin platform. |
| Demo | REQ-DMO-05 | Demo-to-real record promotion. |

If a row contains an ID already assigned P1 elsewhere, the inline requirement text is authoritative; no P2 implementation may be pulled forward merely because an existing long-term architecture concept exists.

## 42. User acceptance scenarios

Each scenario must be automated at the appropriate API/integration/E2E layers and manually checked for accessibility/localization where stated.

### Flow 1 — Operator registers, builds and publishes

**Release:** R0–R2 with DEMO; R3 with authorized REAL.
**Given** a new authenticated operator with no Organization,
**when** they create an Organization and Location, complete a DRAFT Kitchen, add an active exclusive Space, included/shared equipment, one active RentalOffer and future availability, preview and publish,
**then** all data persists, the Kitchen is PUBLISHED, the preview matches Chef-visible safe fields, and discovery includes it only if mode/stage/pilot authorization allow.
**And** publication fails with field-level errors if timezone, Space, offer or future availability is missing.

### Flow 2 — Chef searches and submits

**Release:** R0–R3.
**Given** an eligible published Kitchen/Space and authenticated Chef profile,
**when** the Chef searches a concrete time, opens detail, enters valid context and submits with an idempotency key,
**then** one KitchenBooking in REQUESTED state persists, no capacity/payment record is created, both dashboards show it and notifications are queued.
**And** repeating the same idempotent request does not create a duplicate.

### Flow 3 — Operator accepts

**Release:** R0–R3.
**Given** a REQUESTED record whose Space/time is still compatible,
**when** an authorized operator accepts,
**then** one atomic transition produces CONFIRMED, ADR-007 protects the occupancy including cleaning, both dashboards agree, history is recorded and notifications are queued.
**And** no payment, tax, deposit, payout or ledger record exists.

### Flow 4 — Second Chef overlap

**Release:** R0–R3; mandatory database concurrency test.
**Given** two overlapping REQUESTED records for the same exclusive Space and no prior reservation,
**when** two authorized acceptance operations race,
**then** at most one commits CONFIRMED, the other returns 409 BOOKING_CONFLICT and remains REQUESTED.
**And** a back-to-back request beginning exactly at occupancy end may confirm.

### Flow 5 — Operator declines

**Release:** R0–R3.
**Given** a REQUESTED record,
**when** an authorized operator declines with a controlled reason and optional safe message,
**then** it becomes DECLINED, no capacity changes, both sides see the decision/history and the Chef is notified.
**And** a second decision receives 409.

### Flow 6 — Operator modifies future availability

**Release:** R0–R3.
**Given** recurring availability, a pending request and a confirmed booking,
**when** the operator disables/removes future availability or adds a non-conflicting block,
**then** future search results update, the pending request is re-evaluated and warned if incompatible, and the confirmed booking remains confirmed.
**And** adding a block that contradicts an existing confirmed booking is rejected with 409 or requires an explicit booking-cancellation workflow first.

### Flow 7 — Chef sees all states

**Release:** R0–R3.
**Given** Chef-owned REQUESTED, CONFIRMED, DECLINED, WITHDRAWN and CANCELLED records plus a past confirmed interval,
**when** the Chef filters/views dashboard and detail,
**then** each appears in the correct grouping with consistent status, history, timezone, estimate snapshot and allowed actions only.

### Flow 8 — English/French switch

**Release:** R2–R3.
**Given** a participant in any P0 authenticated flow,
**when** they switch between en-CA and fr-CA,
**then** they remain at an equivalent route/state, system labels/errors/status/accessibility names are localized, dates/currency follow locale without changing instants/amounts, and user-entered text is not fabricated or lost.

### Flow 9 — Mobile Chef, web operator

**Release:** P1 in R3/R4.
**Given** native mobile and web clients use the same backend,
**when** a Chef signs in, searches and submits on mobile and the operator accepts on web,
**then** the same request ID becomes CONFIRMED, mobile refresh/push and web dashboard agree, and no client-specific business-rule discrepancy exists.

### Flow 10 — Demo cannot be confused with real

**Release:** R0–R3.
**Given** DEMO and REAL data exist in permitted isolated contexts,
**when** a user searches, opens, receives email, takes a screenshot, invokes reset or attempts to alter mode,
**then** demo surfaces are labelled, server queries do not cross modes, DEMO cannot become REAL, reset affects only scoped demo records, and no researched real company is represented as a participant.

## 43. Out-of-scope

The Pilot MVP explicitly excludes:

- Stripe, checkout and any payment-provider client secret;
- payment processing, deposits, refunds, payouts, settlement and financial ledger;
- tax calculation or remittance;
- food ordering, consumer checkout, menus, delivery and Phase-2 customer functionality;
- Dietitians and professional consultations;
- promotions, coupons and subscriptions;
- ratings, reviews, wishlists and social proof;
- marketplace chat or unrestricted messaging;
- advanced equipment rental, separate equipment time intervals or paid equipment allocation;
- dynamic pricing and a general-purpose pricing engine;
- cancellation penalties, fees, refunds and disputes;
- external calendar integrations;
- sophisticated recommendations, personalization and OpenSearch;
- managed/master-lease financial or contract workflows;
- assumed sublicensing, re-rental or third-party scheduling rights;
- payroll, employment and contractor compensation;
- public open self-service marketplace launch;
- native mobile Kitchen creation/complex availability management;
- compliance-document upload or Cheffy verification claims;
- instant booking and automatic operator acceptance;
- alternative-proposal negotiation in P0/P1.

Existing architecture for an excluded feature does not authorize its implementation in this pilot.

## 44. Implementation sequence

Coding begins only after the relevant architecture reconciliation and canonical contract updates. Implement dependency-aware vertical slices:

1. **Architecture reconciliation gate** — booking request/state/payment conflict, RentalOffer, availability, DEMO/REAL, profile, events, timezone ADR status and web topology.
2. **Foundation** — monorepo/runtime environments, CI, database/outbox baseline, safe configuration, observability skeleton.
3. **Identity and authorization** — Auth0/OIDC bootstrap, roles/permissions, Organization ownership and profile/locale.
4. **Kitchen vertical slice** — Location, Kitchen CRUD, exact/coarse location privacy, media, three-state publication and pilot authorization.
5. **KitchenSpace vertical slice** — exclusive Space fields, activation, cleaning occupancy.
6. **Equipment vertical slice** — catalog plus Space availability modes.
7. **RentalOffer vertical slice** — mixed formats, minor-unit amounts and display/estimate rules.
8. **Availability vertical slice** — recurring/one-time/block rules, timezone/DST behavior, preview and editing.
9. **Chef profile and discovery** — Chef onboarding, eligible search, result cards/detail and match labels.
10. **Request vertical slice** — REQUESTED persistence, snapshots, idempotency and both read views.
11. **Decision/concurrency vertical slice** — accept/decline, ADR-007 constraint, simultaneous acceptance test and history.
12. **Dashboards/management** — operator and Chef groupings, withdrawal/cancellation and requestability warnings.
13. **Notifications** — in-app, outbox/email, retries and localized templates.
14. **Demo/feedback/admin operations** — isolation/reset, feedback, pilot authorization, emergency actions and runbooks.
15. **LP-01 integration** — protected route boundary, stage-copy gate, privacy/terms/SEO review.
16. **Bilingual/accessibility hardening** — complete strings, Quebec French review, keyboard/screen-reader/responsive acceptance.
17. **Native mobile Chef core** — only after stable shared API; complete Flow 9.
18. **Native mobile operator decision core** — pending/detail/accept/decline/upcoming only.
19. **Pilot deployment/QA** — security/privacy review, backup/restore, alerts, email deliverability, support/incident rehearsal and R3 release gate.

Each slice includes domain rule, persistence migration, repository/use case, authorization, API/OpenAPI, shared client, UI, tests, observability and documentation. No placeholder business logic qualifies as completion.

## 45. Open decisions/blockers

### 45.1 Product decisions

There is no unresolved product decision that blocks R0–R2 architecture reconciliation. This specification decides the narrow product behavior: REQUESTED is non-reserving; acceptance confirms atomically; explicit one-time blocks are P0; withdrawal/cancellation are non-financial; alternatives are deferred; web is primary; native mobile is smaller P1.

Before R3 REAL data, the following configurable operating-policy values must be approved. They do not require product-scope expansion:

1. Exact retention/deletion periods and participant-rights procedure.
2. Exact-address reveal policy and any operator-selectable override within the safe boundary.
3. Participant terms/privacy wording, support hours/escalation and incident contact.
4. Whether request expiry is enabled; if not approved it remains absent.
5. Final email provider/from addresses and production Auth0 tenant configuration.

### 45.2 Architecture blockers before coding affected slices

1. Reconcile docs/03 and docs/04 with the REQUESTED/decision/no-payment aggregate and APIs.
2. Reconcile RentalOffer persistence/API with the existing single hourly-rate model.
3. Reconcile one-time/blocked/effective-date availability representation and precedence.
4. Resolve ADR-011's Proposed status or choose another explicitly approved timezone decision while preserving the product's local-time requirements.
5. Define DEMO/REAL and pilot-authorization persistence/query controls.
6. Define exact profile, request snapshot/history, feedback and notification persistence.
7. Approve the /app pilot topology exception or retain canonical separate apps with an equivalent low-duplication plan.
8. Add/reconcile event contracts only for events actually selected in Section 40.

These are architecture-reconciliation tasks, not permission to implement or silently modify canonical documents.

## 46. Implementation-readiness assessment

### 46.1 Result

**P1-MVP-01 READY FOR ARCHITECTURE RECONCILIATION**

The product is defined precisely enough to update the architecture and produce implementation slices. It is not ready for production coding until the Section 45 architecture blockers are reconciled in their owning canonical sources.

### 46.2 Readiness checklist

| Check | Result |
|---|---|
| Real pilot, not click-through | PASS — persistence/auth/authorization required. |
| Operator creates Kitchens/Spaces | PASS — complete P0 journey and fields. |
| Real one-time/recurring/blocked availability | PASS — explicit P0 model. |
| Chef searches real authorized inventory | PASS — stage/mode filters and honest match labels. |
| Persisted request | PASS — REQUESTED KitchenBooking state and snapshots. |
| Operator accepts/declines | PASS — explicit decision flow. |
| Confirmed overlap prevention | PASS — Accepted ADR-007 preserved. |
| Both dashboards show authoritative state | PASS. |
| No payment processing | PASS — explicitly excluded from state/API/events. |
| No real company used as demo | PASS — immutable DEMO/REAL plus fictional seed rule. |
| Demo/production cannot be confused | PASS — environment and record-mode controls. |
| Operator control | PASS — final approval and explicit publication. |
| Open/managed hypotheses separate | PASS — no managed rights inferred. |
| No sublicensing assumed | PASS. |
| Architecture reused/reconciled | PASS — Section 37 reports reuse, gaps and conflicts. |
| LP-01 unchanged | PASS — only authenticated boundary superseded. |
| MI-01 unchanged | PASS — no status update authorized. |
| Product evidence not market evidence | PASS. |
| No Phase-2 leakage | PASS — Section 43. |
| Shared backend for web/mobile | PASS. |
| Mobile intentionally smaller | PASS. |
| Every functional requirement prioritized | PASS — inline IDs plus Section 41 index. |
| One real operator without workflow rewrite | PASS — REAL mode uses the same core flow after R3 gates. |
| Privacy/security identified | PASS — address, profiles, free text, uploads, logs, retention and access. |
| English/French path identified | PASS — R3 gate and qualified review. |
| No Proposed ADR silently accepted | PASS — ADR-011 remains an explicit blocker. |
| Conflicts reported | PASS — payment-coupled booking, state, price, availability, timezone and topology. |
| Detailed enough for implementation planning | PASS — data/API/event deltas, acceptance tests and sequence provided. |

### 46.3 Handoff

The next task should be a focused architecture reconciliation that updates the owning ADR/ERD/API/event documents consistently, confirms migration strategy and produces an implementation plan. Do not begin production coding from this product document alone. Do not contact operators as part of that architecture task.

---

## Source reconciliation record

This specification was reconciled against:

- docs/01-master-spec.md;
- docs/02-detailed-architecture.md;
- docs/03-database-erd.md;
- docs/04-api-contracts.md;
- docs/05-event-contracts.md;
- Accepted ADR-001, ADR-002, ADR-003, ADR-007, ADR-009 and ADR-016;
- Proposed ADR-011 and ADR-017 where relevant, without treating them as Accepted;
- docs/product/LP-01-public-credibility-surface-spec.md;
- market-intelligence/01-validation-hypothesis-register.md;
- market-intelligence/02-market-landscape.md;
- market-intelligence/07a-kitchen-operator-discovery-pack.md.

The source documents were not modified by this task.
