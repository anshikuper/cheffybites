# P1-IMP-01 — Phase-1 Implementation Plan and Vertical-Slice Backlog

**Status:** READY FOR IMPLEMENTATION

**Prepared:** 2026-09-02

**Repository baseline:** `develop` at `89773ca42136af1ce08971ca52bf24017ef4c941`

**Scope:** Phase-1 Chef ↔ Kitchen Pilot Marketplace; planning only

This document converts the frozen P1 product and architecture into an implementation-ready sequence. It creates no production code, migration, branch, commit, deployment, or external account. P1-ARCH-01 remains unchanged.

## 1. Decision and boundaries

**Result: P1-IMP-01 READY FOR IMPLEMENTATION.**

No unresolved product or architecture contradiction blocks implementation. The following remain release gates before REAL R3 data, not blockers for R0–R2 coding: approved retention/deletion and participant-rights policy; final privacy/terms and exact-address disclosure wording; operator agreement and authority affirmation; production Auth0/email configuration; support and incident contacts; Quebec French review; stage copy/metadata approval; security/privacy review; backup/restore exercise; and service-provider disclosures.

Non-negotiable implementation boundaries:

- `KitchenBooking` is the sole request/booking aggregate. No `BookingRequest` aggregate is created.
- `REQUESTED` does not reserve capacity; Phase-1 `CONFIRMED` does. `HELD` remains inactive in P1.
- The ADR-007 half-open GiST exclusion over `HELD|CONFIRMED` is the final Space-capacity authority.
- P1 creates no Payment, tax, refund, payout, ledger, promotion, checkout, provider token, or paid equipment allocation.
- `RentalOffer` is the only live Space commercial-term authority. Money is integer minor units plus currency.
- Availability is Space-owned, Kitchen-local civil time, and limited to `AVAILABLE|BLOCKED` × `ONE_TIME|WEEKLY`.
- Concrete request and booking boundaries are real instants. ADR-011 gap, overlap, and history rules are mandatory.
- DEMO never becomes REAL. Scope, environment, stage, publication, and pilot authorization remain separate controls.
- Exact address/access data is protected and absent from discovery, events, analytics, and ordinary logs.
- Request-time terms and context are immutable; transition history is append-only; operator approval is final.
- One backend/OpenAPI contract serves web and later mobile. P1 web lives only in `apps/customer-web`.

## 2. Repository audit

The repository currently contains canonical documentation and empty/scaffold application directories. `backend/`, `packages/`, `infrastructure/`, and all six `apps/*` directories contain no build manifests, source trees, migrations, Docker configuration, or CI workflows. There is no implementation to migrate or convention to preserve beyond the documented target structure. The worktree was clean at the stated baseline.

Therefore P1-S00 is a real greenfield foundation slice. It must establish conventions once, minimally, and without scaffolding Phase-2 modules or separate P1 web deployments.

## 3. Delivery and Git strategy

`main` remains stable/release; `develop` remains reviewed integration. Every slice uses a feature branch created from the latest accepted `develop`:

```text
develop -> feature/p1-<slice-name> -> reviewed merge to develop
develop -> release/p1-rN (only when preparing a release) -> main after approval
```

Rules:

1. Rebase or merge the latest `develop` before work starts; record branch, HEAD, and clean/dirty status.
2. One branch has one active writer. Zoo Code, Codex, Copilot, and humans must not concurrently edit the same working tree or branch. A second agent uses a separate worktree/branch and review-only input unless ownership is explicitly transferred.
3. A slice normally contains its domain rule, Flyway migration, persistence, application service, authorization, API/OpenAPI, regenerated client, responsive UI, tests, observability, and implementation notes.
4. Merge only after its exit gate and standard Definition of Done pass. Never merge directly to `main`.
5. Use one coherent checkpoint commit per independently reviewable outcome; use a second commit only when generated artifacts or a migration are materially easier to review separately. Avoid transcript-like micro-commits.
6. Suggested commit forms: `build(p1): establish foundation`, `feat(kitchen): add operator kitchen drafts`, `feat(booking): confirm requests safely`, `test(booking): prove concurrent acceptance`.
7. Agents do not commit or push unless explicitly instructed. The human/reviewer owns merge approval.

A slice must be split before implementation when it has more than one independently demonstrable business outcome, crosses more than two owning domains without a single transaction/use case, combines a high-risk schema/concurrency change with unrelated UI, cannot be reviewed as one coherent diff, or cannot be reverted without also reverting an unrelated capability. Generated-client changes do not count as a separate outcome.

An ADR is required when implementation would change an Accepted decision, introduce a new infrastructure/runtime dependency, alter a domain owner or aggregate/transaction boundary, create a new authoritative datastore, change the P1 web topology, change concurrency/time semantics, or make a material incompatible API/event choice. A local class layout, query tuning, component decomposition, or adapter implementation consistent with current contracts is an ordinary implementation decision and should be documented near the code.

## 4. Local development environment

### Chosen topology

Run PostgreSQL/PostGIS and the email sandbox in Docker Compose; run Spring Boot and Next.js directly on the host. This gives fast reloads, simple VS Code/IntelliJ debugging, and production-like database semantics without containerizing source code. Do not add Redis, Kafka, Kubernetes, a real AWS service, or real production credentials to the P1 local critical path. The outbox uses a local broker adapter for end-to-end development and a real SQS/EventBridge adapter from R1; both sit behind the same port and mark publication only after adapter acknowledgement.

### Required tools and pinned inputs

- Git and Docker Desktop/Engine with Compose v2.
- Java 21 LTS; Gradle Wrapper is repository-owned, so no global Gradle is required.
- A Spring Boot 4.x patch proven by P1-S00 against Java 21, Spring Security, JPA/Hibernate, Flyway, Actuator/OpenTelemetry, OpenAPI tooling, and Testcontainers. Pin exact versions in the build; do not guess them from memory.
- A supported Node LTS pinned in `.nvmrc`/tool metadata and pnpm pinned through Corepack/package metadata.
- A non-production Auth0 development tenant/application/API with localhost callbacks and test users; tests use signed local JWT fixtures or a security test profile, never production tokens.
- Compose services: a version-pinned PostGIS image with `postgis` and `btree_gist`, and Mailpit or an equivalent SMTP sandbox. A local object-storage test adapter may be added in the media slice; S3 remains the production adapter.

### Configuration and secrets

Commit `.env.example` containing names and safe defaults only. Local `.env*`, Auth0 secrets, SMTP credentials, database passwords outside disposable local defaults, and AWS credentials remain ignored. Use typed Spring configuration properties and server-only Next.js environment access. Expected variables include database URL/user/password, active Spring profile, allowed origins, Auth0 issuer/audience/client identifiers, web base URL, SMTP host/port, outbox adapter/mode, media adapter/bucket, OpenTelemetry exporter mode, and explicit data-scope/stage fixture identifiers. No frontend-exposed variable may contain a secret.

### Standard developer flow

The foundation slice should expose these stable repository commands (task aliases may wrap them, but CI runs the same underlying commands):

```text
corepack enable
pnpm install --frozen-lockfile
docker compose up -d postgres mailpit
./gradlew :backend:bootRun
pnpm --filter @cheffybites/customer-web dev

./gradlew test
./gradlew integrationTest
pnpm lint
pnpm test
pnpm test:e2e
pnpm openapi:validate
pnpm api-client:generate
pnpm api-client:check
git diff --check

docker compose down
```

If the Gradle root is `backend/` rather than a multi-project root, the wrapper commands are `cd backend && ./gradlew ...`; P1-S00 must choose one layout and document it in the root README so later tasks do not guess. Resetting disposable database volumes is a separately named command such as `pnpm dev:db:reset`, must show the exact Compose project/volume it targets, and is never the same operation as the application DEMO reset.

Operational flow: verify tool versions and create the ignored local environment file from `.env.example`; start healthy dependencies; let Flyway migrate on backend start while Hibernate uses schema validation, never production auto-DDL; generate the client before starting the web app; use Auth0 dev accounts or the documented local/test security profile; run the checks above; stop Compose normally.

DEMO reset is an application command introduced in P1-S12. It accepts one explicit resettable DEMO scope, verifies the fixture-manifest version, preserves the DataScope/reset audit/admin identity, and can never target REAL. Before that slice, test data is recreated only through isolated test databases and repeatable test builders.

## 5. Backend package/module plan

The one Spring Boot deployable uses `com.cheffybites.<module>/{api,application,domain,infrastructure}` where practical:

| P1 concern | Owning module | Notes |
|---|---|---|
| Auth0 binding, User, ParticipantProfile, platform grants | `identity` | Authentication is Auth0; application identity/status/locale are local. |
| Organization/Profile/Membership/manager assignment/Location | `organization` | Location owns exact and coarse address fields. |
| Kitchen, Space, operating hours, requirements, publication, authorization, media association, RentalOffer, availability | `kitchen` | Canonical persistence may span `kitchen` and `media`; no new module per table. |
| Equipment catalog and descriptive Space equipment | `equipment` | Paid EquipmentRental paths stay inactive. |
| ChefProfile and controlled categories | `chef` | Category seed is a bounded P1 catalog, not adoption of ADR-022. |
| KitchenBooking lifecycle, snapshots, receipts, history | `booking` | Uses Kitchen query/application ports; never another aggregate. |
| In-app/email records and delivery worker | `notification` | Consumes booking integration events idempotently. |
| FeedbackSubmission | `feedback` package or bounded capability under `administration` | Prefer a small `feedback` module because it owns private triage data; do not confuse it with Review/Chat. |
| Pilot stage, reset, privileged inspection/commands | `administration` | Uses owning-module application ports; no direct cross-module repository access. |
| Outbox, IDs, clock, errors, correlation, generic audit plumbing | `common` infrastructure | Must remain generic; domain histories remain in owning modules. |

Do not scaffold inactive food/order/financial/delivery domains merely to mirror the long-term tree. Reserve package names and add them only when an approved slice needs them.

## 6. Flyway migration order

Exact filenames are assigned sequentially on the branch after checking `develop`; the logical order is fixed:

| Order | Migration group | Required content / special handling |
|---:|---|---|
| 1 | schemas and extensions | Create only P1 schemas; `CREATE EXTENSION IF NOT EXISTS postgis` and `btree_gist`. Verify managed-RDS support. Extension ownership may need an environment bootstrap role; document transaction behavior. |
| 2 | platform/outbox | `platform.data_scopes`, pilot stage state/history, `outbox.outbox_events` and ADR-009 indexes. Do not add an outbox status column. |
| 3 | identity/RBAC | Users, ParticipantProfiles, user status history, roles, permissions, role-permission, platform-role grants; seed stable role/permission codes idempotently. |
| 4 | organization/location | Organizations/Profile/Memberships, exact/coarse Location fields, scope-safe keys, manager assignment prerequisites. |
| 5 | kitchen/space/media | Kitchens, operating hours, requirements, Spaces, publication history, pilot authorization, MediaAsset and typed media joins; active-authorization partial uniqueness and privacy indexes. |
| 6 | offer/equipment | RentalOffers with basis checks; equipment catalog and descriptive SpaceEquipment uniqueness/modes. No EquipmentRental/Allocation P1 activation. |
| 7 | availability | Space availability rules and weekday children; schedule-kind checks, effective dates, overlap offset fields, query indexes. No occurrence table. |
| 8 | chef profile | ChefProfiles, controlled category catalog/join, `OTHER` validation, scope-safe user relation. |
| 9 | booking request | `kitchen.kitchen_bookings`, request/offer snapshots, requirement declarations/reviews, equipment needs, command receipts, status history; immutability, idempotency, scope, state and interval checks. |
| 10 | booking exclusion | Generated `[)` `occupancy_range`, GiST exclusion for `HELD|CONFIRMED`, supporting indexes. Keep separate from group 9 so the constraint is isolated and concurrency-tested. |
| 11 | notifications | Notification and delivery tables, event/recipient/type idempotency and per-channel uniqueness. |
| 12 | feedback/reset controls | Feedback typed-link checks and DemoResetRuns/control indexes. Add no generic business-data delete mechanism. |

Every migration is exercised from an empty database and by migration-forward tests. Never edit an applied migration. Production seeds contain only controlled codes/reference data; fictional participant fixtures live in a versioned DEMO fixture manifest invoked by the reset command, not in unconditional Flyway data migrations.

## 7. OpenAPI and generated-client strategy

The machine-readable OpenAPI file is established in P1-S00 and checked in as the transport source used by server contract tests and `packages/api-client`. Each slice updates canonical human-readable documentation only if an implementation clarification is approved, updates OpenAPI in the same branch, regenerates the TypeScript client, and fails CI on generated drift. Web/mobile code imports generated transport types instead of re-declaring them.

Operation grouping by slice is listed in the backlog. Common response/error, cursor pagination, RFC 3339 instant, locale, correlation, `Idempotency-Key`, and `If-Match` schemas are delivered first. Management DTOs and discovery-safe DTOs remain separate so private Location data cannot leak through serialization reuse.

## 8. Event/outbox activation

The outbox mechanism is built in P1-S00 but production events activate only with a justified consumer:

| Event | Producer slice | First actual consumer | Activation rule |
|---|---|---|---|
| `KitchenPublished.v1` | P1-S06 | Privacy-approved product lifecycle analytics/operational evidence | Activate in S06 only with the safe analytics consumer; otherwise S06 cannot claim event completion. It never means requestable. |
| `KitchenBookingRequested.v1` | P1-S08 | Notification consumer for Chef confirmation/operator notice; approved safe analytics | Activate with the notification persistence consumer contract, even if email delivery UI lands in S11. |
| `KitchenBookingConfirmed.v1` | P1-S09 | Notification consumer for both parties | Same transaction as successful confirmation only. |
| `KitchenBookingDeclined.v1` | P1-S09 | Notification consumer for both parties | Controlled reason only; no free text. |
| `KitchenBookingWithdrawn.v1` | P1-S10 | Notification consumer for both parties | First-valid-transition semantics. |
| `KitchenBookingCancelled.v1` | P1-S10 | Notification consumer for both parties | Required controlled reason and safe actor role. |

Availability, RentalOffer, feedback, dashboard reads, and search do not gain events in P1. Consumers deduplicate by `eventId`, enforce `dataScopeId`, validate complete type/version, and never alter producer outbox state. Local delivery may use a broker-port adapter; R1 uses the selected AWS adapter with retry/DLQ monitoring.

## 9. Layered test strategy

| Layer | Mandatory P1 coverage |
|---|---|
| **Unit** | RentalOffer basis/field/estimate validation; availability precedence and bounded recurrence; ADR-011 gap/overlap and civil-day logic; booking transition table; authorization helpers; safe projection/template rules; scope/reset guards. |
| **PostgreSQL integration** | Every Flyway migration from empty DB; repository constraints and indexes; PostGIS queries; scope-safe foreign keys; active-authorization uniqueness; snapshot immutability; command receipts; outbox retry/claiming; BLOCKED-versus-booking constraint behavior. Use Testcontainers with the same PostGIS major line as local/staging. |
| **Concurrency** | Multiple overlapping REQUESTED rows; two independent simultaneous accept transactions produce exactly one CONFIRMED and leave the loser REQUESTED; rollback leaves no losing history/outbox; cleaning-only overlap fails; `[)` back-to-back succeeds; accept/withdraw race is first-valid-transition-wins; repeated commands remain idempotent. |
| **API/contract** | OpenAPI request/response/error validation; all role/scope/state combinations; cursor behavior; If-Match and idempotency reuse; offset-free instant rejection; snapshot stability; private Location fields absent from discovery/events; stable localized error codes. |
| **Web/component** | Critical forms, role routing, loading/empty/error/conflict states, locale continuity, DEMO labels, safe management/discovery DTO use, accessible dialogs and availability editor including the non-grid path. |
| **End to end** | Operator onboards/builds/previews/publishes; Chef profiles/searches/submits; operator accepts/declines; both dashboards agree; withdraw/cancel/address reveal; notification/email; reset and replay. These automate product Flows 1–8 and 10 at their release gate. |
| **DEMO/REAL** | Bidirectional search/read/write/reference isolation; DEMO cannot mutate to REAL; stage/publication/authorization matrix; scoped reset rejects REAL/non-resettable and preserves control/audit records; all demo surfaces remain labelled. |
| **Timezone** | Toronto DST gap and overlap, explicit offset selection, weekly dated exception, civil-day estimate, JVM/session timezone independence, and Kitchen timezone edit not rewriting submitted/confirmed instants. Include a non-Toronto zone to prevent hard-coding. |
| **Security/privacy** | Cross-Organization and assigned-Kitchen denial; IDOR; admin/ordinary operator separation; role self-escalation; exact-address matrix; safe logs/events/analytics/templates; token/session/cache/CORS/CSRF; rate limits; upload validation; dependency and secret scans. |

CI tiers: pull requests run format/lint/static analysis, unit, OpenAPI/client drift, security smoke, and Testcontainers integration/concurrency suites. E2E may be sharded but is required before merge for affected critical paths. Nightly/staging runs full browser, fault-injection, backup/restore, accessibility automation, and supported-device tests when mobile exists. Manual keyboard, screen-reader spot checks, Quebec French review, incident exercises, and legal/privacy approvals are recorded release evidence rather than falsely automated assertions.

## 10. Standard Definition of Done

Every slice must satisfy all applicable items:

- Relevant canonical product, architecture, ERD, API, event, and Accepted ADR sections were read; Proposed ADRs were not silently treated as accepted.
- No unauthorized architecture change, adjacent slice, P2 capability, or financial side effect was added.
- Domain/application/persistence/API/web behavior is complete; no placeholder business logic.
- A forward-only Flyway migration exists where needed and passes empty/upgrade-path tests; Hibernate auto-DDL is disabled outside throwaway tests.
- OpenAPI is valid, server behavior matches it, generated client is refreshed, and drift checks pass.
- Unit, integration, authorization, API/contract, UI, and E2E tests assigned by the slice pass, including negative paths.
- Backend authorization, ownership, data-scope, state, idempotency, and privacy are tested; no sensitive values appear in responses, events, analytics, or logs.
- Structured logs/metrics/traces and audit/history evidence exist for the new workflow, with expected 4xx conflicts separated from 5xx failures.
- Loading, empty, error, conflict, responsive, keyboard, locale, and DEMO-label behavior is complete where UI applies.
- Format/lint/static analysis/security checks, build, `git diff --check`, and the relevant CI workflow pass.
- Diff was reviewed; only intended files changed; migrations/API/events/tests are reported; acceptance criteria were demonstrated.
- Documentation was updated for approved implementation detail changes. Any material architecture conflict stopped the slice and followed the Architecture Change Protocol.

## 11. Vertical-slice backlog

### P1-S00 — Foundation and high-risk proof spine

- **Goal / business outcome:** Establish one reproducible local/CI path and prove the stack choices that every feature depends on.
- **Dependencies:** P1-ARCH-01 frozen baseline only.
- **Branch:** `feature/p1-foundation`.
- **Canonical sources:** AGENTS §§3–7, 27–40, 45–49; master §§46–63, 65–79; architecture §§1–14, 22–25, 41–53, 64; ERD outbox/time/scope rules; API §§22–24; ADR-001/002/003/007/009/011/016/025. ADR-010 is Proposed and may inform a proof, not be treated as accepted.
- **Backend scope:** Java 21/Spring Boot 4 modular-monolith skeleton; health/readiness; Spring profiles; typed configuration; validation/error mapper; UTC clock/time utilities; correlation IDs; safe structured logging; OpenTelemetry hooks; application-side RFC 9562 UUIDv7 generator behind an ID port; outbox persistence/poller/broker port; ArchUnit/module-boundary tests.
- **Database/migration:** Migration groups 1–2 only: schemas, PostGIS, `btree_gist`, data scope, pilot stage/history, canonical outbox. Add a test-only GiST proof table or roll it back within tests—do not invent production booking DDL early.
- **API/OpenAPI:** Machine-readable OpenAPI baseline, common error/cursor/locale/instant/idempotency/ETag components, health API if exposed; generate `packages/api-client` and prove drift detection.
- **Web:** pnpm workspace; `apps/customer-web` Next.js App Router shell; public/protected route groups; Tailwind/design tokens; TanStack Query provider; generated client; en-CA/fr-CA framework; accessible error/loading primitives. No marketplace screen.
- **Mobile:** None. Keep mobile directories untouched except workspace exclusions/readmes if required.
- **Events/outbox:** Implement ADR-009 storage, envelope validation, retry/backoff, single-worker and `SKIP LOCKED` safety tests, local broker adapter, and test event only. No catalog event is activated.
- **Authorization/security:** Deny-by-default Spring Security skeleton, CORS/CSRF decision documented for browser architecture, secret redaction, dependency/secret scanning. No production credentials.
- **Tests:** Build smoke; context; Flyway empty DB; PostGIS query; GiST concurrent constraint spike; RFC3339 offset rejection; DST gap/overlap spike; outbox retry/envelope mismatch; generated-client compile; Next public/private cache-boundary test.
- **Observability:** Trace/correlation propagation, JSON log schema, health metrics, outbox backlog/retry metrics with no payload logging.
- **Demo/fixture impact:** Create only one safe local DEMO scope and PRE_PILOT stage seed/config; no participant fixture.
- **Explicit exclusions:** Auth0 business bootstrap, profiles, domain CRUD, production AWS/Terraform, Redis, S3/media, notifications, booking, mobile, payments, and Phase 2.
- **Acceptance criteria:** One command starts dependencies; backend and web start on host; clean database migrates; OpenAPI regenerates identical client; CI runs build/lint/unit/integration; PostGIS/GiST/DST/Auth/UUID library compatibility spikes have recorded pass/fail outcomes; generated IDs are UUIDv7 with correct RFC variant and stay stable; no protected page is publicly cached.
- **Model / complexity:** Zoo High for scaffolding; Extra High only for Spring Boot compatibility, security/session, GiST, DST, and outbox review. Codex independently reviews boundaries and reproducibility. **L**.
- **Exit gate:** Pinned dependency matrix and spike report pass; first real slice can build on stable conventions without choosing a framework, error, ID, time, contract, or test pattern.

### P1-S01 — Identity, participant profile, roles, and scope

- **Goal / business outcome:** An Auth0-authenticated user is safely bound to a scoped Cheffy identity, can manage their profile/locale, and receives only authorized roles.
- **Dependencies:** P1-S00.
- **Branch:** `feature/p1-auth-profile`.
- **Canonical sources:** P1-MVP §§4, 8–10, 26, 30, 32, 34; P1-ARCH §§9–12; master §§10.5, 11, 61–63; architecture §§7.1–7.2, 43–44, 64.1–64.4; ERD profile/RBAC/scope rules; API §§22–25; ADR-011/025.
- **Backend scope:** OIDC JWT validation; first-authorized-access User binding; ParticipantProfile; `OPERATOR_OWNER|OPERATOR_MANAGER|CHEF|ADMIN`; permission evaluator; account state; locale; immutable scope resolver. Admin is never self-selected.
- **Database/migration:** Group 3 identity/RBAC tables and stable permission seed. Enforce profile/user uniqueness and platform-vs-organization role scope.
- **API/OpenAPI:** `GET /me`, `PATCH /me/profile`; document/implement localized stable errors.
- **Web:** Auth/session boundary, `/app/sign-in`, callback/logout, onboarding intent, role-aware navigation shell, profile/locale editor, preserved equivalent route on locale switch.
- **Mobile:** None; token/API design remains client-neutral.
- **Events/outbox:** None.
- **Authorization/security:** JWT issuer/audience/signature checks; deactivated-user denial; self-only profile writes; role escalation tests; server-only session secrets; CSRF/CORS enforcement.
- **Tests:** JWT success/failure, duplicate subject race, profile validation, locale, deactivation, role-scope integrity, protected route redirect, cache leakage, authorization helpers.
- **Observability:** Safe auth/bootstrap result classes and latency; never log token/email/phone.
- **Demo/fixture impact:** Fictional DEMO_OPERATOR/DEMO_CHEF identity bindings for local/test only; visibly scoped.
- **Explicit exclusions:** Organization creation, manager invitations, production tenant setup/MFA policy, Kitchen/Chef profile behavior, admin UI.
- **Acceptance criteria:** Valid dev user bootstraps once; invalid/deactivated user is denied; role/permission/scope come from backend; profile locale persists; cross-scope identifiers cannot be selected by clients.
- **Model / complexity:** Extra High for auth/security, High for profile UI. **L**.
- **Exit gate:** Identity threat-model review and all broken-auth/role-escalation tests pass.

### P1-S02 — Operator Organization and private Location

- **Goal / business outcome:** An operator can create and manage their Organization and exact/coarse Location without cross-Organization or discovery leakage.
- **Dependencies:** P1-S01.
- **Branch:** `feature/p1-operator-organization-location`.
- **Canonical sources:** P1-MVP §§4, 6, 10–11, 34; P1-ARCH §§10, 12; master §§5, 7, 10.3, 61–62; architecture §§7.2–7.3, 44, 64.2; ERD Organization/Location/RBAC/address rules; API §§25.3, 26 Location; ADR-025.
- **Backend scope:** Organization/Profile/Membership and Location use cases; owner creation; manager assignment model; exact/coarse projection separation; PostGIS point validation.
- **Database/migration:** Group 4, including scope-safe ownership and assignment integrity.
- **API/OpenAPI:** Organization POST/GET/PATCH; Location POST/list/GET/PATCH exactly as API §26.
- **Web:** Guided operator onboarding, Organization and Location forms, privacy explanations, validation/recovery, operator shell.
- **Mobile:** None.
- **Events/outbox:** None.
- **Authorization/security:** Owner/manager membership checks on every query; client Organization ID never grants access; exact DTO unavailable to discovery code.
- **Tests:** Cross-Organization IDOR, membership status, location validation/coarsening, exact/private serializer tests, PostGIS persistence, accessible forms.
- **Observability:** Safe creation/update outcome and authorization-denial metrics; no address fields in logs/traces.
- **Demo/fixture impact:** Fictional Organization/Location builder; invented address only.
- **Explicit exclusions:** Kitchen/Space, invitations, public location pages, automatic geocoder/provider, REAL pilot authorization.
- **Acceptance criteria:** Operator completes durable onboarding; another Organization receives non-leaking denial; exact and coarse projections are structurally distinct.
- **Model / complexity:** Extra High review for tenant/privacy, High implementation. **M**.
- **Exit gate:** Ownership query review and address-leak test suite pass.

### P1-S03 — Kitchen and Space draft management

- **Goal / business outcome:** An authorized operator can build durable DRAFT Kitchens with operating constraints, requirements, active exclusive Spaces, and validated media.
- **Dependencies:** P1-S02.
- **Branch:** `feature/p1-kitchen-space-drafts`.
- **Canonical sources:** P1-MVP §§6, 11–13, 17; P1-ARCH §§12–13; master §§7–9; architecture §§7.3–7.4, 45, 64.2; ERD Kitchen/Space/hour/requirement/media rules; API §26 management/hour/requirement/space/media; ADR-024/025.
- **Backend scope:** Kitchen/Space CRUD, optimistic versioning, operating hours, operator requirements, activation rules, media upload/confirmation port and safe association projections.
- **Database/migration:** Group 5 except publication/authorization behavior may remain dormant; all canonical physical/privacy checks and indexes.
- **API/OpenAPI:** Kitchen POST/management list/GET/PATCH/preview; operating-hour and requirement CRUD; Space POST/list/GET/PATCH; media upload request/confirm and Kitchen/Space association.
- **Web:** Operator Kitchen/Space wizard, saved drafts, non-grid operating-hour form, requirements editor, media upload/alt text, Chef-view preview skeleton.
- **Mobile:** None.
- **Events/outbox:** None.
- **Authorization/security:** Organization ownership/assignment; upload ownership/MIME/size/checksum/status; private fields excluded from preview.
- **Tests:** Validations, optimistic conflicts, inactive Space history, future-confirmed deactivation guard using fixtures, upload abuse/SSRF boundaries, preview privacy, keyboard/320px forms.
- **Observability:** Creation/validation/media failure classes and safe IDs; no private copy/access instructions.
- **Demo/fixture impact:** Fictional Kitchen/Space/media fixture builders, persistent DEMO label components.
- **Explicit exclusions:** Publication, requestability, RentalOffer, equipment, availability, booking, real S3 deployment.
- **Acceptance criteria:** Draft survives reload, supports multiple Spaces, rejects invalid/unauthorized edits, shows only validated discovery-safe media, and preview never reveals exact address.
- **Model / complexity:** High; Extra High for upload/security review. **L**.
- **Exit gate:** Draft onboarding and preview tests pass with no publication path enabled.

### P1-S04 — RentalOffer and descriptive equipment

- **Goal / business outcome:** Operators can honestly describe Space equipment and all six rental formats without creating payment or capacity facts.
- **Dependencies:** P1-S03.
- **Branch:** `feature/p1-offers-equipment`.
- **Canonical sources:** P1-MVP §§13–14; P1-ARCH §§6, 13; master §§7.3, 8; architecture §§7.3–7.4, 64.5/64.8; ERD RentalOffer/equipment constraints; API §26 RentalOffer and §27; ADR-024.
- **Backend scope:** RentalOffer aggregate/validation/estimator and equipment catalog/Space offering modes. Estimates use use interval only and never hidden rounding.
- **Database/migration:** Group 6 and bounded catalog seeds.
- **API/OpenAPI:** RentalOffer POST/PATCH/DELETE/list; equipment catalog GET and Space equipment GET/PUT.
- **Web:** Offer editor with basis-specific fields and disclaimer; equipment catalog picker/mode/quantity/notes; preview cards.
- **Mobile:** None.
- **Events/outbox:** None.
- **Authorization/security:** Space authority, optimistic version checks, bounded notes, safe equipment projection.
- **Tests:** Every basis and invalid field combination; exact minor-unit formula; rounding-to-`REQUIRES_CONFIRMATION`; civil-day DST; deactivation/history; four modes.
- **Observability:** Validation/estimate status metrics only; no operator free text in logs.
- **Demo/fixture impact:** Fictional offers/equipment across all bases; no researched operator terms.
- **Explicit exclusions:** Dynamic pricing, tax, deposits collected, payments, promotions, EquipmentRental/allocation, price comparison across bases.
- **Acceptance criteria:** Every basis round-trips; deterministic estimates are correct; ambiguous calculations are not invented; no financial tables/SDKs exist.
- **Model / complexity:** Extra High for money/DST estimator, High otherwise. **L**.
- **Exit gate:** Basis matrix and no-financial-side-effect tests pass.

### P1-S05 — Space availability and timezone-safe evaluation

- **Goal / business outcome:** Operators can express and preview requestable Space time with explicit available/block rules and correct DST behavior.
- **Dependencies:** P1-S03, P1-S04.
- **Branch:** `feature/p1-availability`.
- **Canonical sources:** P1-MVP §15 and Flow 6; P1-ARCH §§7–8; master §§9, 10.6; architecture §§14, 64.5; ERD availability/time rules; API §26 availability and §28 availability; ADR-007/011/024.
- **Backend scope:** Rule CRUD, bounded resolver, operating-hours/available/block/occupancy/offer precedence, requestability classification and reason codes.
- **Database/migration:** Group 7; no materialized Space occurrence table.
- **API/OpenAPI:** Availability-rule POST/PATCH/DELETE/list; `GET /kitchen-spaces/{spaceId}/availability`.
- **Web:** Accessible calendar plus equivalent list/form path; local timezone and viewer-time labels; dated DST correction errors; block conflict display.
- **Mobile:** None.
- **Events/outbox:** None.
- **Authorization/security:** Managed-Space mutation; safe public reason codes; no private conflict-party data.
- **Tests:** One-time/weekly AVAILABLE/BLOCKED precedence; operating hours do not create availability; overnight rejection/splitting guidance; DST gap/overlap; timezone edits preserve historical instants; cleaning overlap/back-to-back; conflicting block rejection.
- **Observability:** Evaluation latency and controlled reason counts; DST exception counts; no searched raw location.
- **Demo/fixture impact:** Deterministic rules around ordinary and DST dates.
- **Explicit exclusions:** Unrestricted recurrence, external calendars, recurring bulk exceptions UI, occurrence persistence, holds.
- **Acceptance criteria:** Preview is deterministic and bounded; gaps/overlaps never guess; block veto wins; protected occupancy wins; calendar and non-grid paths are keyboard usable.
- **Model / complexity:** Extra High. **XL**.
- **Exit gate:** ADR-011 matrix and availability precedence integration suite pass before discovery work begins.

### P1-S06 — Publication, pilot authorization, and requestability gates

- **Goal / business outcome:** Operators publish explicitly while admins separately control pilot eligibility; nothing silently becomes requestable.
- **Dependencies:** P1-S03–S05.
- **Branch:** `feature/p1-publication-pilot-gates`.
- **Canonical sources:** P1-MVP §§2, 11, 26, 28–29; P1-ARCH §§9, 12, 16; master §§4.5, 10.5; architecture §§64.1, 64.3–64.4; ERD stage/publication/authorization rules; API §26 publication and §28B relevant admin commands; events `KitchenPublished.v1`; ADR-009/016/024/025.
- **Backend scope:** Completeness evaluator; PUBLISH/UNPUBLISH; admin authorization/revoke and stage command; emergency unpublish; history/audit; requestability conjunction.
- **Database/migration:** Activate publication/authorization/history constraints from group 5 and stage controls from group 2; no new duplicate status.
- **API/OpenAPI:** publication action; admin stage GET/PUT; pilot authorization/revoke; emergency unpublish.
- **Web:** Publish checklist/affirmation, truthful requestability banner, preview comparison; narrow admin commands may be secure operational UI/tooling.
- **Mobile:** None.
- **Events/outbox:** Activate `KitchenPublished.v1` with a privacy-approved safe lifecycle analytics/operational consumer; no requestability inference.
- **Authorization/security:** Operator publication vs dedicated admin permission; reason/version/idempotency; REAL requires CONTROLLED_PILOT and active authorization; history remains readable after revoke/unpublish.
- **Tests:** Missing completeness, stale version, admin impersonation denial, scope/stage matrix, active-authorization uniqueness, emergency audit, event safety.
- **Observability:** Publication failure reasons, stage/authorization audits, safe lifecycle counter.
- **Demo/fixture impact:** DEMO publication works only in explicit demo context; REAL fixture remains invisible until every gate passes.
- **Explicit exclusions:** Open public live inventory, automatic authorization, public stage-copy change, general admin console.
- **Acceptance criteria:** Published alone is insufficient; REAL requestability requires every canonical gate; unpublish preserves history/confirmed commitments; event contains no private data.
- **Model / complexity:** Extra High for authorization/isolation; High UI. **L**.
- **Exit gate:** Full DEMO/REAL × stage × publication × authorization decision-table test passes.

### P1-S07 — Chef profile and safe discovery

- **Goal / business outcome:** A Chef completes a bounded profile and discovers only eligible Space inventory with honest classifications and protected location data.
- **Dependencies:** P1-S01, P1-S04–S06.
- **Branch:** `feature/p1-chef-discovery`.
- **Canonical sources:** P1-MVP §§7, 9, 16–17, 23–24; P1-ARCH §§10, 12; master §§10.3, 11; architecture §§7.6, 46–47, 64; ERD Chef/address/search relations; API §§25.4, 26 search/detail, 28 availability; ADR-011/024/025.
- **Backend scope:** ChefProfile/categories; Postgres/PostGIS structured discovery; Space-centric safe projections; match/possible/no-match evaluation and same-basis price filtering.
- **Database/migration:** Group 8 plus search/GiST/full-text indexes justified by query plans.
- **API/OpenAPI:** category GET; own Chef profile GET/PATCH; Kitchen search and safe detail; reuse availability preview.
- **Web:** Chef onboarding, search/filter/results/detail/request-start affordance, persistent operator-approval/DEMO labels and empty states.
- **Mobile:** None; responsive layouts are mobile-usable.
- **Events/outbox:** None. Search/view analytics are synchronous privacy-approved counters, not integration events.
- **Authorization/security:** Authenticated invited Chef only; strict scope/stage gating; separate safe DTO; rate limits; no exact point/contact/access data.
- **Tests:** Category/OTHER rules, search eligibility, price basis comparability, reason classification, private field snapshots, no-result wording, IDOR, 320px/keyboard/a11y.
- **Observability:** Latency/result-count bands/filter-presence only; no raw location or free text.
- **Demo/fixture impact:** Fictional searchable listing and visible DEMO badge/email-safe labels.
- **Explicit exclusions:** Recommendations, ratings, social proof, popularity/scarcity, saved searches, public SEO inventory.
- **Acceptance criteria:** Only eligible Spaces appear; every label remains approval-qualified; direct detail does not bypass gates; exact location cannot be serialized.
- **Model / complexity:** Extra High for privacy/query review; High otherwise. **L**.
- **Exit gate:** Address/privacy and eligibility test suites pass against real PostGIS.

### P1-S08 — Idempotent booking request submission

- **Goal / business outcome:** A Chef submits one durable non-reserving request with historically complete evidence and sees it immediately.
- **Dependencies:** P1-S05–S07.
- **Branch:** `feature/p1-booking-request`.
- **Canonical sources:** P1-MVP §§18–20 and Flows 2/7; P1-ARCH §§4, 11; master §§10.1–10.4; architecture §§7.5, 64.6–64.7; ERD request/snapshot/receipt/history rules; API §28 submission/read/list; events `KitchenBookingRequested.v1`; ADR-002/009/011/016/024.
- **Backend scope:** REQUESTED creation, re-evaluation, immutable request/offer snapshots, declarations/equipment needs, status history, booking-domain command receipt and Chef/operator reads.
- **Database/migration:** Group 9, excluding GiST activation until S09; all immutability/scope/idempotency indexes.
- **API/OpenAPI:** POST booking requests; request detail; Chef/operator request lists; outside-review action may land here or S09 but must remain append-only.
- **Web:** Request form/review/disclaimer, success detail, Chef pending list, operator inbox row, client idempotency-key lifecycle.
- **Mobile:** None.
- **Events/outbox:** Activate `KitchenBookingRequested.v1`; notification persistence consumer creates idempotent in-app records/delivery intents. Email sending may remain stubbed until S11.
- **Authorization/security:** Chef ownership; operator managed-Kitchen read; current gate recheck; bounded free text; key hashing; no text/address in event/log.
- **Tests:** Identical retry replay, changed-payload key rejection, two overlapping REQUESTED allowed, complete snapshot equality/immutability, requirement cardinality, scope/IDOR, no financial rows/components.
- **Observability:** Submission result classes/idempotent replay/outbox timing; no request text.
- **Demo/fixture impact:** Flow-2 request fixture and deterministic request IDs through manifest/reference mapping.
- **Explicit exclusions:** Capacity reservation, accept/decline/withdraw/cancel, payment, negotiation, multi-Space request.
- **Acceptance criteria:** Exactly one REQUESTED record/snapshot/history/event is committed per logical command; repeats do not duplicate; no capacity or financial fact exists.
- **Model / complexity:** Extra High. **XL**.
- **Exit gate:** Atomicity, idempotency, snapshot, and no-reservation tests pass.

### P1-S09 — Operator decision and ADR-007 concurrency

- **Goal / business outcome:** An authorized operator confirms or declines a request; at most one overlapping request becomes confirmed.
- **Dependencies:** P1-S08.
- **Branch:** `feature/p1-booking-decision`.
- **Canonical sources:** P1-MVP §§19–21 and Flows 3–5; P1-ARCH §4; master §10.2; architecture §§14, 64.6; ERD concurrency/history rules; API §28 confirm/decline/outside-review; events confirmed/declined; ADR-007/009/011/016/024.
- **Backend scope:** Row lock/reload, expected-version and first-valid-transition logic, current reauthorization/revalidation, DECLINED and CONFIRMED, requirement outside-review, constraint-to-error mapping.
- **Database/migration:** Group 10 generated range/GiST exclusion. Verify migration privileges and transaction handling on the target PostgreSQL/PostGIS image.
- **API/OpenAPI:** confirm, decline, outside-review action and canonical 409 errors.
- **Web:** Operator inbox/detail/live conflict view, accept confirmation, decline form, stale/conflict reload; Chef decision status.
- **Mobile:** None.
- **Events/outbox:** Activate confirmed/declined events and both-party notification consumers, same transaction as committed transition.
- **Authorization/security:** Only authorized operator decides; admin cannot use ordinary decision; no other Organization; event reasons controlled/free text excluded.
- **Tests:** Independent concurrent transactions: two overlapping REQUESTED, simultaneous accept, exactly one CONFIRMED and loser REQUESTED; cleaning-only overlap; exact back-to-back success; stale duplicate decision; availability change; rollback removes losing history/outbox; cross-org denial.
- **Observability:** Separate expected `BOOKING_CONFLICT`/state 409 metrics from 5xx; decision latency and constraint identity.
- **Demo/fixture impact:** Deterministic conflicting pair for demo/test.
- **Explicit exclusions:** HELD, payment, alternatives, auto-accept, shared capacity, paid equipment.
- **Acceptance criteria:** Database—not precheck—guarantees one winner; losing request is unchanged; decline never affects capacity; dashboards converge on committed state.
- **Model / complexity:** Zoo GPT-5.6 Sol Extra High; mandatory independent Codex review. **XL**.
- **Exit gate:** Repeated real-PostgreSQL concurrency suite and transaction/diff review pass before management work.

### P1-S10 — Booking management, withdrawal, cancellation, and address reveal

- **Goal / business outcome:** Both parties manage the full P1 lifecycle and see consistent current/history views; confirmed parties can retrieve protected Location details.
- **Dependencies:** P1-S09.
- **Branch:** `feature/p1-booking-management`.
- **Canonical sources:** P1-MVP §§22–24 and Flow 7; P1-ARCH §§4, 12; master §§10.2–10.3; architecture §64.6/64.8; ERD history/address rules; API §26 confirmed address and §28 lists/withdraw/cancel; events withdrawn/cancelled; ADR-007/009/016/024.
- **Backend scope:** WITHDRAWN/CANCELLED transitions, first-winner race, future-only cancellation, capacity release, derived past grouping, confirmed-party address projection.
- **Database/migration:** No new table expected; add only query indexes proven necessary. Never mutate snapshots/history.
- **API/OpenAPI:** Chef/operator booking lists; withdraw; cancel; location details.
- **Web:** Chef/operator dashboards, status/history/detail, withdraw/cancel confirmations and no-financial-policy wording, upcoming/past groupings, protected address view.
- **Mobile:** None.
- **Events/outbox:** Activate withdrawn/cancelled events and both-party notification consumers.
- **Authorization/security:** Chef owns request; operator manages Kitchen; exact address only CONFIRMED parties; past ordinary cancel denied; non-leaking 403/404.
- **Tests:** Withdraw/accept race, repeat commands, future vs past cancel, capacity released, confirmed booking remains after unpublish, address matrix, all five state groupings.
- **Observability:** Transition outcomes and address-access denials without address data.
- **Demo/fixture impact:** REQUESTED/CONFIRMED/DECLINED/WITHDRAWN/CANCELLED/past fixture set.
- **Explicit exclusions:** Reschedule, penalties/refunds, completion state/user action, disputes, alternative proposals.
- **Acceptance criteria:** Both dashboards agree; only valid actions render and succeed; cancel frees range; exact address is inaccessible except to confirmed parties.
- **Model / complexity:** Extra High for races/privacy, High otherwise. **L**.
- **Exit gate:** State/authorization/address decision tables and full lifecycle E2E pass.

### P1-S11 — Durable in-app and email notifications

- **Goal / business outcome:** All five booking triggers produce reliable localized notices without controlling booking success.
- **Dependencies:** P1-S08–S10 event producers; may be developed behind their agreed contract but merged after them.
- **Branch:** `feature/p1-notifications`.
- **Canonical sources:** P1-MVP §25; P1-ARCH §15; master §§10.8, 65–68; architecture §§7.22, 42, 64.8; ERD notification/outbox rules; API §28A; P1 events; ADR-002/009/016.
- **Backend scope:** Idempotent event handlers, Notification/Delivery persistence, locale snapshot, safe template registry, Mailpit/local and provider adapter, retries/permanent failures.
- **Database/migration:** Group 11.
- **API/OpenAPI:** own notification list/read.
- **Web:** Notification center, unread/read states, authenticated route targets, localized templates and DEMO treatment.
- **Mobile:** Push remains deferred; API/event design leaves channel extension point.
- **Events/outbox:** Consume requested/confirmed/declined/withdrawn/cancelled. No new event. Consumer inbox/dedup strategy must be explicit.
- **Authorization/security:** Recipient-only reads; safe template-arg schema; authenticated links; no address/contact/message/reason-note payload.
- **Tests:** Duplicate event, retry/restart, provider failure after domain commit, locale snapshot, scope mismatch rejection, safe-template validation, mark-read authorization.
- **Observability:** Queue/outbox age, delivery attempts/status/provider health and permanent failure alerts; redact payloads.
- **Demo/fixture impact:** DEMO subject/body treatment and Mailpit reset policy.
- **Explicit exclusions:** SMS, notification preferences, marketing mail, native push/device APIs.
- **Acceptance criteria:** Each trigger creates one recipient/channel record; duplicate delivery is harmless; provider outage does not roll back or change booking state.
- **Model / complexity:** High; Extra High for delivery/idempotency review. **L**.
- **Exit gate:** Fault-injection and retry/idempotency tests pass end to end.

### P1-S12 — Feedback, scoped DEMO reset, and minimum administration

- **Goal / business outcome:** Pilot users submit private feedback; admins safely inspect/control pilot state and deterministically reset only fictional data.
- **Dependencies:** P1-S06, P1-S10–S11.
- **Branch:** `feature/p1-pilot-operations`.
- **Canonical sources:** P1-MVP §§26–28, 35–36 and Flow 10; P1-ARCH §§9, 14; master §10.5/10.7; architecture §64.4/64.8; ERD feedback/reset/admin rules; API §§28A–28B; ADR-024/025.
- **Backend scope:** FeedbackSubmission; typed related-resource authorization; admin read models; account status command; failure inspection; DemoResetRun and manifest executor.
- **Database/migration:** Group 12 and any missing admin query indexes; use canonical histories rather than inventing a generic audit aggregate.
- **API/OpenAPI:** feedback POST; admin pilot users/Kitchens/bookings/feedback/failures; user status; scoped reset. Reuse stage/authorization/emergency commands from S06.
- **Web:** Contextual feedback; minimal protected admin operational screens only where secure tooling is insufficient; DEMO mode persistent treatment.
- **Mobile:** None.
- **Events/outbox:** Feedback/reset emit no P1 integration events.
- **Authorization/security:** Dedicated admin permissions/MFA configuration gate; reason/idempotency/version; related-resource visibility; reset hard-denies REAL/non-resettable before writes.
- **Tests:** Typed-link constraint, feedback text exclusion, admin separation, account deactivation, cross-scope inspection, reset safety/rollback/repeatability and manifest counts.
- **Observability:** Admin mutation audit, reset run counts/status, feedback count by controlled category only.
- **Demo/fixture impact:** Versioned invented fixture manifest; exact scoped reset contract; preserve scope/reset/admin/audit records.
- **Explicit exclusions:** Public reviews/chat, sentiment analysis, broad admin platform, REAL delete/reset, importing researched operators.
- **Acceptance criteria:** R0 can reset and replay the fictional happy path without database edits; no command can relabel or delete REAL; feedback remains private and absent from logs/events/analytics.
- **Model / complexity:** Extra High for destructive-safety/admin; High otherwise. **L**.
- **Exit gate:** Adversarial DEMO/REAL reset and admin authorization suite passes.

### P1-S13 — Public shell, bilingual/accessibility, security, and R0–R2 hardening

- **Goal / business outcome:** Deliver a presentation-quality, evidence-safe responsive web experience and close cross-cutting acceptance gaps for local demo/private staging/operator demo.
- **Dependencies:** P1-S00–S12.
- **Branch:** `feature/p1-web-pilot-hardening`.
- **Canonical sources:** LP-01 in full before implementation; P1-MVP §§2, 5, 29, 32–36, 42; P1-ARCH §16/24; master §§4.5, 78–80; architecture §§52, 55, 64.1; API all P1 sections; ADR-003/025.
- **Backend scope:** Rate limits, final safe product analytics, security headers/CORS/CSRF hardening, complete telemetry/error mapping; no new business behavior.
- **Database/migration:** None expected; performance indexes only from measured plans and normal forward migration.
- **API/OpenAPI:** Full P1 contract regression and generated-client drift gate.
- **Web:** Implement/preserve LP-01 public routes; isolated public/protected caches; complete responsive operator/Chef journeys; en-CA/fr-CA strings, error/status labels, DEMO banners, non-grid calendar path, manual a11y fixes.
- **Mobile:** None; responsive web must operate on mobile browsers.
- **Events/outbox:** Validate registry, unknown-version behavior, privacy-safe analytics consumers and backlog operations; no new events.
- **Authorization/security:** Full IDOR/cross-org/admin/address test pass; CSP/security headers, dependency/upload/secret checks; public routes expose no live inventory/structured data.
- **Tests:** Flows 1–8 and 10 E2E; axe plus manual keyboard/zoom/reflow/screen-reader spot checks; locale continuity; security regression; load smoke for search/decision/outbox.
- **Observability:** R0/R1 dashboards for auth, publication, availability, submission, decision, notification and outbox; DEMO/REAL separation.
- **Demo/fixture impact:** Complete repeatable fictional script/screenshots/email; no real-company data.
- **Explicit exclusions:** R3 policy approval, production AWS deployment, public live-marketplace copy, native apps, P2.
- **Acceptance criteria:** Founder completes flows 1–8 and 10 without DB intervention; all material screens are bilingual and accessible to stated criteria; claims remain evidence-safe.
- **Model / complexity:** High; Extra High for security/a11y final review; Copilot suitable for reviewed localization/CSS/test repetition. **XL**.
- **Exit gate:** R0 and R2 checklists pass; release candidate is stable enough to configure R1 infrastructure.

### P1-S14 — Private staging and controlled-pilot operational readiness

- **Goal / business outcome:** Operate the approved web workflow safely in private staging and prove the non-code gates needed to enable one controlled REAL pilot.
- **Dependencies:** P1-S13.
- **Branch:** `feature/p1-pilot-hardening`.
- **Canonical sources:** P1-MVP §§5, 28–36, 45; P1-ARCH §24; master/architecture security/deployment/CI/observability sections; ADR-001/002/009/025.
- **Backend scope:** Production-profile configuration, SQS/EventBridge adapter, email provider adapter, operational jobs/alerts, backup/restore runbook hooks. No financial/provider expansion.
- **Database/migration:** No business schema expected; validate migrations on staging RDS-compatible PostGIS and backup/restore.
- **API/OpenAPI:** No new product endpoints; deployment smoke/contract tests.
- **Web:** Stage-specific safe copy/configuration after approval; error/support/privacy links; no open registration unless approved.
- **Mobile:** Conditional planning only.
- **Events/outbox:** Real broker, DLQ/parking policy, replay runbook, unsupported-version alerts and consumer lag.
- **Authorization/security:** Production-like Auth0 tenant separation, MFA for admins where supported, secrets manager, least privilege, environment/scope isolation, incident exercise.
- **Tests:** Staging E2E, backup/restore, broker/email fault injection, rollback, security scan/pen test fixes, REAL gate dry run without collecting REAL data.
- **Observability:** Alerts, trace-to-user-error path, dashboards, on-call/support runbook and pilot-hours thresholds.
- **Demo/fixture impact:** Staging DEMO isolated from production; reset rehearsal; production REAL path has no broad reset.
- **Explicit exclusions:** Terraform beyond minimum approved staging need if separately scoped; multi-operator/open launch, native requirement, payments/P2.
- **Acceptance criteria:** R1 technical gates pass; R3 enablement requires explicit recorded product/legal/operations approvals and active platform stage/authorization—not a deployment side effect.
- **Model / complexity:** Extra High for security/ops; High normal infrastructure. **XL**.
- **Exit gate:** R1 checklist, backup/restore, incident, email, alerts, French/privacy/terms/stage-copy approval evidence satisfy the release owner; then and only then R3 may be enabled.

### P1-S15 — Conditional Chef native core

- **Goal / business outcome:** If a named R3/R4 participant need justifies native, a Chef uses the same API for sign-in, search, request, status, withdraw/cancel, and push.
- **Dependencies:** P1-S13 complete; P1-S14 API stable; explicit mobile trigger approval.
- **Branch:** `feature/p1-chef-mobile`.
- **Canonical sources:** P1-MVP §§31–33 and Flow 9; P1-ARCH §§15–16; master mobile sections; API P1 Chef/booking operations; booking events; ADR-003/025 where relevant.
- **Backend scope:** Only approved device registration/push-channel contract, which must be added to canonical API/ERD/event docs or a focused ADR if material. No client-specific rules.
- **Database/migration:** Only approved device/push records; otherwise none.
- **API/OpenAPI:** Reuse generated P1 operations; add push APIs only after contract approval.
- **Web:** No feature change.
- **Mobile:** Expo/React Native Chef sign-in, search/detail/request, lists/detail, withdraw/cancel, refresh/push, en/fr and accessibility.
- **Events/outbox:** Existing booking events feed push adapter; no parallel mobile event.
- **Authorization/security:** Secure token storage, deep-link authentication, recipient/device ownership, lost/revoked device handling.
- **Tests:** Shared client contract, iOS/Android critical path, offline/retry/idempotency, push/deep links, accessibility, Flow 9 with web operator.
- **Observability:** Crash/network/push delivery signals without sensitive payloads.
- **Demo/fixture impact:** Same DEMO scope/labels; no mobile-only seed.
- **Explicit exclusions:** Native Kitchen/availability admin, payment, chat, ratings, P2.
- **Acceptance criteria:** Same booking ID/state across mobile and web; offline retries do not duplicate; no business-rule divergence.
- **Model / complexity:** High; Extra High for auth/push security. **L**.
- **Exit gate:** Flow 9 passes on supported iOS/Android targets and shared OpenAPI drift remains zero.

### P1-S16 — Conditional operator native decision core

- **Goal / business outcome:** If participant evidence justifies it, an operator receives push and safely reviews/accepts/declines requests and sees upcoming bookings.
- **Dependencies:** P1-S15 platform/mobile foundation and P1-S09/S10 stable decision API.
- **Branch:** `feature/p1-operator-mobile`.
- **Canonical sources:** P1-MVP §31 and Flow 9; P1-ARCH §§15–16; API §28; booking events; ADR-007/024/025.
- **Backend scope:** None beyond approved push/device behavior from S15.
- **Database/migration:** None expected.
- **API/OpenAPI:** Reuse operator request/decision/booking operations.
- **Web:** No feature change.
- **Mobile:** Pending notification/list/detail, confirm/decline conflict handling, upcoming bookings, en/fr, accessibility.
- **Events/outbox:** Existing requested/decision events only.
- **Authorization/security:** Organization membership/assignment remains server authoritative; local role routing is UX only.
- **Tests:** Stale/conflict reload, cross-org denial, duplicate tap/idempotency, push deep link, iOS/Android accessibility.
- **Observability:** Decision result/latency and crash signals; expected 409 separate from failures.
- **Demo/fixture impact:** Same DEMO records/labels.
- **Explicit exclusions:** Native Organization/Kitchen/Space/offer/equipment/availability/media/admin management and all P2.
- **Acceptance criteria:** Mobile cannot bypass decision rules; web and mobile immediately converge on the same authoritative request.
- **Model / complexity:** High with Extra High decision-security review. **M**.
- **Exit gate:** Operator half of Flow 9 passes and no duplicate implementation of domain logic exists.

## 12. Dependency order and release mapping

```text
S00 -> S01 -> S02 -> S03 -> S04 -> S05 -> S06 -> S07 -> S08 -> S09 -> S10 -> S11 -> S12 -> S13 -> S14
                                      S08/S09/S10 event contracts ----------------------^
S13 + S14 stable + explicit participant need -> S15 -> S16
```

| Release | Minimum slices | Gate/outcome |
|---|---|---|
| **R0 local demo** | S00–S12, with the core responsive screens from each slice | Fictional persisted flows 1–5, 7, and 10; deterministic reset; concurrent acceptance; local email/outbox. |
| **R1 private staging** | R0 + S13 + S14 technical staging subset | Protected deployed web, non-prod Auth0/email/broker, isolated DEMO, observability, security tests, backup/restore. |
| **R2 operator demo** | R1 + S13 presentation/bilingual/accessibility exit gate | Founder completes flows 1–8 and 10 without DB intervention; presentation-quality fictional workflow and no false live claim. |
| **R3 controlled pilot** | R2 + full S14 and all recorded operational/legal/product gates | One authorized REAL operator/Chef workflow with notifications, feedback, admin, support and incident readiness. S15/S16 are optional unless participant need explicitly makes native part of R3. |

Native work starts only after: R2 responsive web is stable; P1 OpenAPI has no planned breaking change; production-like auth and notification paths work; a named pilot need outweighs maintenance cost; and the release owner approves Flow 9 scope. Otherwise it moves to R4. No native Kitchen or availability administration enters P1.

## 13. Risk-first proof register

| Risk | Prove by | Pass/fail outcome |
|---|---|---|
| Spring Boot 4 ecosystem compatibility | S00 compile/test matrix | Exact pinned versions support Java 21, Security, JPA, Flyway, OpenAPI, OTel, Testcontainers; otherwise change only compatible patch/tooling or raise ADR if stack changes. |
| Auth0 + Next session/cache boundary | S00 spike, S01 completion | Valid issuer/audience flow, secure callback/session, no protected shared caching, testable backend JWT path. |
| PostGIS + `btree_gist` | S00 smoke, S09 production DDL | Extensions install in local/Testcontainers/target RDS class and concurrent exclusion works. |
| UUIDv7 | S00 proof | P1 uses a standards-compliant application-side generator and verifies v7/variant/uniqueness/stability. Pin the proven library/API; do not hand-roll bits. ADR-010 remains Proposed, so this P1 convention is not presented as a platform-wide Accepted ADR. PostgreSQL columns remain UUID with no invented DB default. |
| ADR-007 concurrency | S00 bounded spike, S09 acceptance | Real independent transactions yield one confirmed winner, loser REQUESTED, cleaning conflict and back-to-back success. |
| ADR-011 timezone/DST | S00 spike, S04/S05 suites | Gaps rejected, overlaps require offset, civil-day estimates correct, timezone edits preserve instants. |
| Weekly recurrence evaluation | S05 | Bounded resolver handles effective dates/weekdays and returns dated correction errors without occurrence persistence. |
| Generated OpenAPI client | S00 | Deterministic generation, compile, server contract tests and drift failure. |
| DEMO/REAL isolation/reset | S01/S06 early, S12 destructive test | Bidirectional query/relationship denial and structurally impossible REAL reset/promotion. |
| Email/outbox | S00 mechanism, S08/S11 consumers | Commit atomicity, dedup, retry and failure isolation proven before R0. |
| Availability editor accessibility | S03/S05 prototype, S13 audit | Keyboard and non-grid path work at 320px/200% zoom and announce timezone/errors. |

## 14. AI/IDE execution policy

- **Zoo Code, GPT-5.6 Sol Extra High:** Use for S00 compatibility/security proofs; S01 auth; S05 recurrence/DST; S08 snapshot/idempotency transaction; S09 concurrency/state machine; S12 reset/admin security; cross-module or high-risk schema work. Require a written plan before edits.
- **Zoo Code High/strong standard model:** Normal vertical slices, ordinary migrations, REST/OpenAPI/web integration, notification adapters, and integration tests where architecture is already explicit.
- **Copilot/free models:** Only bounded mechanical work with tests and human review: DTO/property mapping, generated-client call sites, localized string entry from approved copy, simple CSS, fixture builders, repetitive test cases, formatting and documentation cleanup. They must not decide schema, authorization, state transitions, time/money formulas, privacy projection, event payload, or concurrency.
- **Codex:** Prefer as an independent reviewer for every L/XL or security-sensitive slice and as an alternative implementer only on a separate branch/worktree with explicit ownership. Review against canonical docs, diff, migrations, tests and negative paths. Codex never edits the active Zoo working tree concurrently.
- **VS Code:** Primary cross-stack workspace and Zoo/Copilot environment.
- **IntelliJ IDEA:** Prefer for Spring Boot dependency diagnosis, Java refactors, JPA mappings, transaction/lock debugging, Flyway/database inspection, and JUnit/Testcontainers profiling. Command-line build/CI remains authoritative.

Ownership handoff requires the current writer to stop, save/commit only if authorized, report exact branch/HEAD/dirty files, and let the next writer independently verify status. Reviewers receive a patch/commit and run read-only checks; they do not “quick fix” the reviewed branch without explicit transfer.

## 15. Reusable agent task templates

### Zoo Code vertical-slice template

```text
TASK: <slice/task ID and name>
BRANCH: <feature branch>; base it on the current reviewed develop.

Before editing: report git branch, HEAD, status, existing files/build/migrations/tests, and read AGENTS.md plus only the canonical sources listed for this slice. Treat P1-MVP-01 and P1-ARCH-01 as frozen. State a bounded implementation plan, impacted modules, migration/API/event/authorization/idempotency/test/observability effects, and intended files.

Implement only this slice end to end. Preserve KitchenBooking as the request aggregate, backend authority, data-scope/privacy rules, no-payment P1 boundary, and Accepted ADRs. Do not invent missing behavior or change architecture. If sources conflict materially, stop and report the conflict using the Architecture Change Protocol. Do not implement adjacent slices. Do not commit, merge, or push unless explicitly told.

Update Flyway/OpenAPI/generated client/UI/tests together where applicable. Run targeted and full relevant tests, format/lint/static analysis, OpenAPI drift, secret/security checks, and git diff --check. Review the diff for unrelated files and sensitive logging.

Report: result; files changed; migrations; API/OpenAPI/client changes; events/consumers; authorization/privacy; tests and exact results; observability; assumptions; unresolved issues. Do not claim complete while any exit-gate item fails.
```

### Mechanical Copilot/free-model template

```text
TASK: <bounded mechanical change>. Read AGENTS.md and the named file/contract only. Do not alter behavior, schema, authorization, business rules, public API/event shapes, dependencies, or architecture. Touch only <explicit paths>. Follow the existing pattern and approved copy/mapping. Run <named focused checks> and git diff --check. Do not commit/push. Report changed files and any ambiguity; stop rather than guess.
```

### Independent Codex review template

```text
REVIEW: <slice ID>, branch/commit <ref>. Work read-only unless separately authorized. Read AGENTS.md and the slice's canonical sources, then inspect the full diff, migrations, OpenAPI/generated drift, tests and logs. Look specifically for architecture drift, wrong domain ownership, missing database guarantees, authorization/IDOR, DEMO/REAL crossing, private-data leakage, idempotency/races, DST/time/money errors, unsafe event payloads, P2 scope, and missing negative tests. Run relevant verification. Rank findings by severity with exact file/line evidence; state no findings when appropriate. Do not modify the implementer's active working tree, commit, merge, or push.
```

## 16. Checkpoint strategy and first task

The first implementation slice is **P1-S00 — Foundation and high-risk proof spine**, because the repository has no executable baseline and every later slice depends on proven Spring Boot 4, PostGIS/GiST, OpenAPI generation, web session/cache, outbox, time, and CI conventions.

The exact first coding task is:

```text
P1-S00-T01 — Bootstrap the reproducible local/CI foundation and compatibility proof
Branch: feature/p1-foundation
```

Its first checkpoint should contain only the runnable workspace/build/Compose/CI/OpenAPI skeleton and documented compatibility proof; the second, if review clarity warrants it, may contain canonical foundation migrations/outbox and tests. It must not start identity or Kitchen behavior.

## 17. Planning quality gate

1. Baseline branch `develop` and commit `89773ca42136af1ce08971ca52bf24017ef4c941` were verified.
2. P1-ARCH-01 and every product, architecture, ERD, API, event, and ADR source remain unchanged.
3. No production code was created or modified.
4. No Flyway migration was created or modified.
5. Every slice states its dependencies.
6. Every slice has independently verifiable acceptance criteria and an exit gate.
7. Every slice assigns unit, integration, contract, UI, E2E, concurrency, timezone, scope, or security tests as applicable.
8. Every slice states explicit exclusions and does not absorb adjacent or Phase-2 work.
9. Every slice cites canonical sources, with Accepted and Proposed ADR status respected.
10. Every slice states model tier and complexity.
11. Branching, checkpoint commits, review, merge, and no-agent-commit rules are explicit.
12. The local toolchain, services, configuration, secrets, start/test/stop flow, and DEMO reset boundary are explicit.
13. Zoo Code owns implementation, with Extra High reserved for the named high-risk proofs and changes.
14. Copilot/free models are limited to mechanical, contract-constrained assistance and cannot decide domain, schema, authorization, financial, concurrency, or time semantics.
15. Codex is assigned independent review, adversarial proof, and reproducibility checks on a separate branch/worktree when it edits.
16. VS Code is the primary cross-stack IDE; IntelliJ is recommended for Java/Spring/JPA, transaction, and database debugging.
17. One active writer per branch/working tree is mandatory; agents do not edit the same working tree concurrently.
18. The forward-only Flyway migration order, extension proof, immutability, and clean/forward migration tests are explicit.
19. OpenAPI is the HTTP transport source, generated-client drift fails CI, and safe discovery DTOs remain separate from management DTOs.
20. Each activated event names its producer, first consumer, safe payload boundary, outbox transaction, and idempotent handling; unjustified CRUD events remain absent.
21. R0–R3 release composition and entry/exit evidence are explicit.
22. Native mobile remains conditional and follows responsive-web/API stabilization; it is not on the initial critical path.
23. Payment and every other Phase-2 marketplace capability remain excluded from P1.
24. DEMO cannot become REAL; scope, environment, stage, publication, authorization, address, and reset protections are mandatory and tested.
25. ADR-007 GiST capacity enforcement and race proofs occur in S00/S09, before broad pilot acceptance.
26. ADR-011 gap, overlap, civil-day, instant, and history proofs occur in S00/S05/S08, before broad scheduling work.
27. The plan adds no unstated domain behavior; unresolved material contradictions would stop the affected slice under the Architecture Change Protocol.
28. `P1-S00-T01` and `feature/p1-foundation` are specific enough to begin the first implementation branch without another planning decision.

## 18. Remaining blockers

**None for implementation.** R3 operational/legal/product approvals listed in §1 and §12 remain mandatory before collecting REAL pilot data or changing public stage claims.
