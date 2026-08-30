# ADR-021: Authorized Multi-Context Conversation Architecture

## Status

Proposed

## Context

Cheffy Bites needs durable marketplace communication for several approved business relationships without becoming a public social network or unrestricted direct-messaging platform. Current and anticipated contexts include controlled Food Request interactions, concrete Food Order and ChefOrderGroup fulfillment, DietitianClientEngagement, and Dietitian Appointment relationships. Additional context families may be approved later.

These contexts differ in who may communicate, why access exists, when write access begins and ends, whether a grace period applies, and which history may remain visible. Authentication, Organization membership, commercial-provider status, or discoverability alone cannot answer those questions.

The architecture must preserve the following foundational distinctions:

```text
AUTHENTICATED ACCOUNT
!= AUTHORIZED CONVERSATION PARTICIPANT

CONVERSATION
!= MESSAGE

CONVERSATION
!= BUSINESS SOURCE OF TRUTH

MESSAGE
!= ORDER INSTRUCTION
!= APPOINTMENT STATE CHANGE
!= FINANCIAL INSTRUCTION
!= PROMOTION REDEMPTION

CONVERSATION AUTHORIZATION
!= PERMANENT ACCESS

BUSINESS CONTEXT ENDED
!= MESSAGE HISTORY IMMEDIATELY DELETED
```

ADR-017 owns professional identity and professional-to-Organization authorization. ADR-018 owns DietitianClientEngagement and Appointment. ADR-013 owns ChefOrderGroup. ADR-020 owns authoritative commercial and Financial facts. This ADR consumes those typed facts as authorization evidence where appropriate; it does not redesign or absorb their aggregates.

This ADR establishes conceptual Conversation architecture. It deliberately does not finalize exact tables, columns, foreign keys, REST endpoints, WebSocket payloads, event contracts, moderation APIs, legal retention periods, or privacy deletion schedules.

## Decision

Cheffy Bites will implement an authorized, context-bound Conversation capability inside the modular monolith. A Conversation exists only because an approved marketplace relationship or workflow context authorizes identified participants to communicate. There is no generic public direct-message capability.

The capability conceptually owns:

- Conversation as a durable communication container;
- ConversationParticipant as durable, typed participation and authorization history;
- Message as a durable communication fact inside one Conversation;
- current read and write authorization evaluation;
- context-specific lifecycle, grace, and read-only behavior;
- blocking, reporting, moderation, and privileged-access controls;
- message edit, visibility, redaction, retention, and deletion coordination;
- attachment authorization and security boundaries;
- durable local persistence before asynchronous delivery and notification effects; and
- privacy-minimized operational telemetry.

It does not own the lifecycle or canonical facts of FoodRequest, Order, ChefOrderGroup, Appointment, Subscription, Promotion, Payment, Refund, Payout, earning recognition, Ledger, review, reputation, or taxonomy.

### No Unsolicited Messaging

The Platform will not permit arbitrary user-to-user messaging merely because two authenticated accounts exist or one user is discoverable.

- A Chef cannot send unsolicited direct messages to arbitrary Customers.
- Seeing a FoodRequest does not grant a Chef access to the requesting Customer's private Conversation.
- A Dietitian cannot send unsolicited direct messages to arbitrary Customers merely because either party is discoverable.
- An Organization administrator does not receive Customer messaging access merely by administering an Organization.
- A commercial provider, settlement beneficiary, employer, clinic, or other Organization does not become a participant merely because it has a business relationship with the actual professional.
- Public Conversation discovery and arbitrary user search for direct-message initiation are prohibited.

Messaging access must be created from an approved context and its explicit authorization rule.

## Required Invariants

1. `AUTHENTICATED ACCOUNT != AUTHORIZED CONVERSATION PARTICIPANT`.
2. `CONVERSATION != MESSAGE`.
3. `CONVERSATION != BUSINESS SOURCE OF TRUTH`.
4. `MESSAGE != ORDER STATE CHANGE`.
5. `MESSAGE != APPOINTMENT STATE CHANGE`.
6. `MESSAGE != PAYMENT / REFUND / EARNING INSTRUCTION`.
7. `ORGANIZATION ADMIN != PROFESSIONAL CONVERSATION PARTICIPANT`.
8. `HISTORICAL PARTICIPANT != CURRENTLY AUTHORIZED SENDER`.
9. `CONVERSATION AUTHORIZATION != PERMANENT WRITE ACCESS`.
10. `BUSINESS CONTEXT ENDED != HISTORY IMMEDIATELY DELETED`.
11. `CHAT MESSAGE != LEDGER ENTRY`.
12. `CHAT SENTIMENT != RATING`.
13. `EXTERNAL REAL-TIME TRANSPORT != MESSAGE SOURCE OF TRUTH`.
14. `DIETITIAN CONVERSATION != EMR / CLINICAL RECORD`.
15. Conversation identity alone is never sufficient authorization for Conversation, Message, or attachment access.
16. Every send operation rechecks current write authorization at mutation time.
17. Read access follows current visibility and access policy, including any permitted historical rights; it is not permanent merely because participation once existed.
18. Participant identity and participation role remain historically attributable and are not recomputed solely from current Organization membership.
19. Durable Message persistence in PostgreSQL is authoritative; event, WebSocket, SSE, push, email, and provider delivery order are not authoritative Message history.
20. UUID ordering is not authoritative Message chronology.

## Authorization Context Model

### Approved Context Requirement

Each Conversation requires an approved, domain-valid authorization context. Legitimate context families may include:

- FoodRequest and an approved request-response or consent relationship;
- an accepted Customer-Chef Food Request interaction;
- a concrete Food Order and, where relevant, ChefOrderGroup participation;
- DietitianClientEngagement;
- a qualifying Dietitian Appointment relationship; and
- another marketplace relationship explicitly approved in future product and canonical architecture work.

“Multi-context Conversation Architecture” means that one common capability can support multiple approved context families. It does not mean that one Conversation should own many unrelated business contexts.

### Primary Context and Explainability

Each Conversation should have a clear primary authorization/business context. Explicit typed related-context references may be added only where a genuine workflow requires them.

The system must preserve enough durable evidence to explain:

- why the Conversation was authorized;
- which typed business context governed access;
- who was authorized and in which business role;
- when each participant joined, gained authorization, lost write access, or was removed or revoked;
- when a context transition changed the authorization basis; and
- why later read, write, moderation, or attachment access was allowed or denied.

The current state of a source aggregate may inform current access, but historical authorization must not be reconstructed solely from current state or Organization membership.

### Typed Relational Integrity

The canonical model must use domain-valid, typed relationships with enforceable referential integrity and cardinality where practical. This ADR does not approve an unconstrained canonical relationship such as:

```text
context_type
context_id UUID
```

It also does not approve:

- a universal ConversationContextRegistry created merely to support arbitrary domains;
- one giant Conversation table with a nullable foreign key for every possible context;
- `entity_type`, `entity_id`, `metadata JSON`, or `attributes JSON` as the canonical model for context or participant authorization; or
- generic polymorphic metadata as a substitute for domain-valid relationships.

The later canonical ERD may evaluate typed context-family association records, domain-specific authorization relationships, or another strongly typed strategy. JSONB may be used later only for genuinely extensible, non-relational provider or transport metadata.

### Context Transition

A business workflow may progress from one context to another, for example:

```text
FoodRequest
    ↓
accepted food arrangement
    ↓
concrete Order
```

Product policy may continue an existing Conversation when continuity is appropriate, or create a new context-bound Conversation. This ADR does not impose one universal transition policy.

If a Conversation continues, the architecture must preserve which context authorized each relevant period and participant. If a new Conversation is created, the relationship may be linked explicitly without merging unrelated history. The system must not create a permanent universal thread that combines FoodRequest, Order, Appointment, and Subscription merely because the same two people participate.

### Conversation Continuity

Lifetime one-thread-per-user-pair is not the canonical model. The same people may have multiple Conversations over time for different approved contexts. Their business purpose, authorization periods, participant roles, and historical context must remain explainable.

## Conversation / Participant / Message Model

### Conversation

A Conversation is a durable communication container. Conceptually it preserves:

- stable Conversation identity;
- approved primary authorization context and any explicitly permitted typed context history;
- participant identities and participation history;
- creation real instant;
- lifecycle equivalent to `ACTIVE`, `READ_ONLY`, and `CLOSED`;
- applicable grace or read-only information;
- access restriction, blocking, and moderation state where appropriate; and
- retention and deletion-policy references where required.

A Conversation is not an Order, ChefOrderGroup, FoodRequest, Appointment, professional engagement, Subscription, or Financial source of truth.

### ConversationParticipant

ConversationParticipant records whether a durable domain identity is authorized to participate in a specific Conversation and under which business role/context. Conceptually it preserves:

- actual participant identity;
- participant role or capability in that Conversation;
- typed authorization basis;
- authorized/joined real instant;
- removed, revoked, or write-disabled real instant where applicable;
- read-state reference where required;
- participant-specific visibility, block, or moderation state where applicable; and
- historical attribution sufficient to explain access decisions.

Customer, Chef professional, Dietitian professional, Organization representative, and privileged Platform actor must not be collapsed into one untyped business identity. Exact typed relational persistence is deferred to canonical ERD work.

### Authenticated Account Versus Participant

Authentication answers:

```text
WHO is using the Platform?
```

ConversationParticipant authorization answers:

```text
IS THIS DOMAIN IDENTITY AUTHORIZED
TO READ OR PARTICIPATE IN THIS CONVERSATION NOW?
```

Auth0 `sub` is an external authentication subject and is not the canonical marketplace Conversation participant identity. Authorization resolves the authenticated principal to durable domain identity and verifies the applicable context, role, capability, lifecycle, and resource ownership.

### Multi-Role Accounts

One authenticated account may hold several domain roles, including Customer, Chef, Dietitian, or Organization administrator. The Conversation must preserve the business identity and role under which that person participates.

Account equality does not collapse role scope. A person authenticated through one account does not gain every Conversation capability associated with all roles without satisfying the relevant context authorization. Reads, sends, participant changes, and moderation operations must authorize the intended acting identity and role explicitly.

### Message

A Message is a durable communication fact within one authorized Conversation. Conceptually it may preserve:

- stable Message identity;
- sender ConversationParticipant identity and actual acting identity;
- creation real instant and explicit deterministic ordering semantics;
- body or controlled content reference;
- content type;
- reply or reference metadata;
- controlled attachment references;
- moderation, redaction, and ordinary-visibility state;
- edit or user-delete state where policy permits;
- audit/history references where required; and
- client/request idempotency evidence.

This is not an exact column or aggregate specification.

### Message Idempotency and Ordering

Message creation must support client/request idempotency so transport retries or an offline-client retry do not create unintended duplicate Messages. Provider push-delivery IDs are not canonical Message identities.

Conversation display requires deterministic ordering based on explicit creation/order semantics. ADR-010 identifiers may improve locality but UUID ordering alone is not business chronology. Outbox, broker, WebSocket, push, or notification order is not canonical Message order.

### Concurrent Mutation

The design must tolerate concurrent send, edit, redact, moderation, and participant-revocation operations. Authorization is rechecked at mutation time and must not rely on a stale client view. Exact optimistic-locking, version, conflict, and API behavior remain later ERD/API work.

## Food Request / Order Conversation

### Food Request Authorization

FoodRequest visibility does not grant direct-message access. Communication requires the approved request workflow and Customer authorization or consent. Conceptually:

```text
Customer publishes FoodRequest
    ↓
eligible nearby Chef responds or expresses interest
under Food Request product rules
    ↓
Customer approval/consent or accepted request relationship
    ↓
Conversation becomes authorized
```

The Food Request domain owns exact workflow and state names. ADR-021 owns the resulting communication authorization and participant access. A Chef cannot use request discovery as a pretext for arbitrary outreach.

### Order-Related Authorization

Customer-Chef communication may be authorized for a concrete Order or ChefOrderGroup relationship when needed for fulfillment or approved post-fulfillment support. Authorization must validate actual participation in the relevant Order context.

For a multi-Chef Order, product policy may choose:

- separate Customer-Chef Conversations per ChefOrderGroup; or
- an explicitly authorized shared Order Conversation.

Both are supported conceptually. The choice must preserve actual Chef identity, least privilege, and protection against accidental disclosure of unrelated private communication. Chef Ravi does not automatically gain access to a separate private Conversation belonging only to Chef Maria. Every participant in a shared Conversation requires an explicit context-valid authorization reason.

Commercial-provider, settlement-beneficiary, employer, or Organization identity does not automatically equal participant access. An approved future Organization representative may participate only through an explicit, role-scoped operational authorization.

### ChefOrderGroup Boundary

ADR-013 remains authoritative for ChefOrderGroup. This ADR may use ChefOrderGroup as typed authorization context or evidence for relevant Order communication, but:

```text
CHEFORDERGROUP
!= CONVERSATION

CHEFORDERGROUP
!= PARTICIPANT

CHEFORDERGROUP
!= MESSAGE THREAD
```

No ChefOrderGroup ownership, state, financial-reference, refund-traceability, payout-traceability, or reporting rule is redesigned here.

## Dietitian Conversation

Customer-Dietitian communication may be authorized by an approved professional context, including:

- an `ACTIVE` DietitianClientEngagement;
- a qualifying Appointment relationship; or
- a configured, context-specific grace period after relevant professional service.

ADR-017 remains authoritative for actual Dietitian professional identity, credentials, and jurisdiction eligibility. ADR-018 remains authoritative for DietitianClientEngagement, Appointment, and Appointment lifecycle. Organization identity does not replace the actual Dietitian participant.

### Professional and Organization History

If Dietitian D leaves Clinic A, or Chef Ravi leaves ABC Food Group, historical Conversation participation remains attributable to the actual professional and applicable business context. Current Organization membership or configuration must not rewrite historical participants, authorization basis, sender identity, or Message attribution.

An Organization administrator does not automatically gain access to Customer-Dietitian professional communication. Future support, operations, or Organization-representative access requires explicit policy, capability, least privilege, and audit. It must not masquerade as ordinary participant membership.

### Dietitian Privacy Boundary

A Dietitian Conversation does not automatically expose:

- a complete DietitianMealPlan;
- diagnoses;
- medications;
- laboratory results;
- clinical records; or
- private credential evidence.

The Customer controls which permitted requirements or information are shared according to approved product and privacy policy. Conversation authorization is not blanket authorization to access every professional, engagement, Appointment, meal-plan, credential, or Customer data record.

```text
DIETITIAN CONVERSATION
!= EMR / CLINICAL RECORD
```

Dietitian participation does not by itself classify the capability as a medical or clinical record system. If regulated clinical communication or special retention duties are introduced, explicit legal, privacy, security, and product decisions are required. This ADR leaves that future decision open and makes no automatic recording or transcription requirement.

## Lifecycle / Grace / Read-Only

### Conceptual Lifecycle

Conversation lifecycle uses a small set of meanings equivalent to:

```text
ACTIVE
READ_ONLY
CLOSED
```

- `ACTIVE`: currently authorized participants may send Messages, subject to participant-specific authorization, blocks, moderation, and policy.
- `READ_ONLY`: authorized history may remain visible, but ordinary new Messages are not allowed.
- `CLOSED`: ordinary participant access or use has ended according to policy, while retention rules may preserve necessary historical data.

Moderation, suspension, participant revocation, retention hold, or visibility restrictions may be separate facts rather than an enormous Conversation workflow state machine.

### Business Context End

When an authorization-producing context ends, the system must not preserve indefinite write access by default. Examples include:

- FoodRequest rejected, fulfilled, or closed;
- Order completed or cancelled;
- DietitianClientEngagement completed or terminated; and
- Appointment completed or cancelled.

Context-specific product policy determines whether the Conversation becomes immediately read-only, remains writable for a limited grace period, or closes. Ending the context does not itself mandate immediate destruction of Message history.

### Grace Period

Grace policy is configurable by context and must not use one universal duration. Examples include a short post-Order window for legitimate fulfillment/support issues or a post-service Dietitian window allowed by an active engagement or approved professional communication policy.

A grace period extends only the capabilities explicitly allowed by policy. It does not extend unrelated Order, Appointment, Subscription, Financial, Promotion, or professional-eligibility state.

### Historical Visibility Versus Current Write Access

Losing authorization to send new Messages does not necessarily remove all historical visibility immediately. Subject to privacy, safety, legal retention, blocking, moderation, account state, and deletion policy, a former participant may retain read-only access.

Conversely, prior participation never proves current write access:

```text
HISTORICAL PARTICIPANT
!= CURRENTLY AUTHORIZED SENDER
```

Every send rechecks current participant and context authorization. Every read evaluates current visibility/access policy plus any permitted historical rights. Former participants do not receive unconditional permanent access to every item of content.

### Participant Removal and Revocation

Participant membership and access may be removed, revoked, suspended, or reduced to read-only according to context and safety policy. The system preserves historical participation and attribution as policy requires without treating the removed participant as currently authorized.

Revocation must affect future ordinary Conversation and attachment access where required, while leaving authoritative business records unchanged. Re-authorization, where allowed, requires a new valid authorization decision rather than assuming permanent membership.

## Blocking / Moderation

### Blocking

User safety blocking can prevent new ordinary communication where policy requires. Blocking may affect participant visibility, sending, notification, or discovery behavior without mutating owning-domain facts.

Blocking does not:

- erase an Order;
- cancel or reschedule an Appointment;
- change a Refund;
- alter a Payment, earning, payout, or other Financial obligation;
- restore or consume a Promotion;
- change a Subscription; or
- automatically delete evidence required by policy.

If an active commercial or service workflow still requires communication, necessary information may be routed through controlled support or transactional notifications rather than unrestricted chat. Exact user experience is product-policy work.

### Reporting and Moderation

The capability supports reporting of Conversations, Messages, and participant behavior. Authorized moderation may:

- flag content or behavior;
- restrict or suspend messaging access;
- redact or remove content from ordinary visibility;
- apply rate or safety restrictions;
- escalate to Platform operations; and
- preserve evidence according to policy.

Moderation does not itself mutate Order, Appointment, Subscription, Promotion, review, reputation, or Financial state. Any domain consequence requires an explicit authorized operation in the owning domain.

### Moderation Audit

Moderation and privileged access should be auditable. Conceptually preserve:

- action;
- actual actor;
- reason or category;
- Conversation, Message, participant, or access target;
- real timestamp; and
- resulting visibility or access state.

Internal moderation notes and sensitive safety information must not be exposed indiscriminately to marketplace participants. Exact moderation schema and policy catalog remain later work.

### Content Safety and Abuse

The architecture leaves room for spam prevention, harassment reporting, prohibited-content controls, rate limits, link and file restrictions, and Platform safety interventions. It does not introduce a public feed, followers, public groups, arbitrary direct messaging, or other social-network architecture.

### Privileged Platform Access

Platform support or moderation staff may access a Conversation only through an explicit privileged capability governed by least privilege and audit. Privileged access must not silently add Platform administrators as ConversationParticipants.

Organization administrators and Platform operators cannot impersonate a Chef, Dietitian, Customer, or other participant to send ordinary marketplace Messages. Any exceptional support communication must preserve the actual actor, capability, reason, and audit trail.

## Message Edit / Delete / Retention

### Policy-Controlled Editing

Messages need not be permanently immutable merely because auditability matters. Product policy may allow editing within a configured window or under configured content rules.

Where required, edit history or audit evidence is retained. Edits must not rewrite authoritative domain facts, and moderation, investigation, legal, or safety evidence remains available according to policy. Exact edit window, version representation, and participant-facing behavior are deferred.

### Deletion, Hiding, and Redaction

User deletion is not necessarily physical database erasure. The model must support distinct meanings equivalent to, where required:

```text
USER_HIDDEN / DELETED_FOR_USER
REDACTED
MODERATION_REMOVED
SYSTEM_REMOVED
```

These meanings may differ in who can see content, whether audit evidence remains, and whether eventual physical deletion is allowed. A Message may be removed from ordinary display without pretending it never existed.

### No Blanket Ledger-Style Immutability

```text
CHAT MESSAGE
!= FINANCIAL LEDGER ENTRY
```

Messages do not require ADR-015's immutable-POSTED semantics. Moderation, redaction, legal deletion, privacy handling, and policy-controlled editing must remain possible. The system must neither promise permanent retention nor promise immediate irreversible deletion where safety, legal, dispute, or privacy obligations require different treatment.

### Retention

Retention is policy-governed rather than unlimited by default. It may depend on:

- active or historical business relationship;
- support and dispute requirements;
- safety and moderation needs;
- privacy obligations;
- legal obligations;
- account state and deletion requests; and
- applicable retention or legal-hold policy.

This ADR defines neither a forever-retention period nor immediate deletion. Exact duration, deletion schedule, legal hold, regional requirements, and policy authority require approved privacy/legal work.

### Account Deletion and Privacy Requests

Account deletion or privacy requests must not corrupt historical business records. Where lawful and appropriate:

- participant identifiers may be anonymized or pseudonymized;
- ordinary access may be removed;
- Message content may be deleted or redacted according to policy; and
- evidence required by law, safety, disputes, or audit may be retained under controlled access.

Exact legal and privacy behavior remains policy work and must distinguish participant identity, ordinary visibility, content retention, and owning-domain history.

## Attachments / Privacy / Security

### Controlled Storage

Attachments may be supported only through controlled object/file storage. Raw binary content must not be stored directly in Message rows.

Attachment architecture must support:

- private object storage;
- durable attachment metadata and Message reference;
- authorization-controlled access;
- file-type and size restrictions;
- malware and security scanning where applicable;
- content and link safety controls;
- safe rendering and download behavior; and
- policy-governed retention and deletion.

The exact attachment schema, scanning provider, upload flow, and API contract remain future work.

### Attachment Privacy

Permanent public object URLs are prohibited. Attachment fetch must validate Conversation and participant authorization rather than treating possession of a URL or object key as authorization.

Revoked or closed Conversation access must prevent ordinary future attachment access where policy requires. Attachment retention may differ from Message display or Conversation lifecycle, but it remains governed and non-public.

### Operation-Level Authorization

Every Conversation read, Message read, Message send, edit/delete request, participant mutation, and attachment fetch enforces authenticated, context-aware authorization. Conversation IDs, Message IDs, attachment IDs, and opaque URLs are locators, not access grants.

Authorization includes least privilege, participant/context checks, lifecycle and grace rules, current block/moderation restrictions, and resource ownership. No cross-Conversation data leakage is acceptable.

### Security Controls

The capability follows Platform security standards, including:

- authenticated access;
- authorization on every operation;
- least privilege;
- input validation and safe output rendering;
- rate limiting and abuse controls;
- controlled links and attachments;
- encryption in transit and at rest;
- auditability for privileged access; and
- secrets and credentials kept outside Message bodies, telemetry, and ordinary logs.

This ADR does not claim end-to-end encryption. Any future end-to-end encryption design requires a separate decision addressing key management, moderation, multi-device access, legal obligations, recovery, and support implications.

### Observability

Privacy-minimized telemetry may include Message persistence/delivery latency, failure counts, notification outcomes, authorization denials, moderation counts, and transport health. Full private Message bodies must not be logged by default for observability. Sensitive content and attachment URLs must not be copied into traces or ordinary application logs.

## Business-Domain Boundaries

### Order and Fulfillment

Chat may discuss an Order, but Message content does not itself change canonical:

- Order status;
- ChefOrderGroup preparation state;
- pickup or delivery state;
- cancellation; or
- Refund state.

ADR-005 and ADR-013 remain authoritative. Structured actions occur through authorized owning-domain APIs/workflows. Natural-language content must not be parsed as the primary authoritative workflow trigger.

### Appointment

Chat may discuss an Appointment, but a Message does not confirm, reschedule, cancel, classify no-show, or complete an Appointment. ADR-018 remains authoritative, and structured Appointment operations remain required.

### Subscription

Chat may discuss Meal or Kitchen Subscription service, but a Message does not activate a Subscription, reserve or consume entitlement, confirm an occurrence, create a KitchenBooking, pause a Subscription, or terminate it. ADR-019 remains authoritative.

### Financial

ADR-012, ADR-015, and ADR-020 remain authoritative for Payment, Refund, payout, ledger, commercial obligation, earning recognition, and related Financial facts.

```text
CHAT / MESSAGE
!= AUTHORITATIVE FINANCIAL SOURCE FACT
```

A Message saying “refund me” does not create a Refund. A Message saying “I'll pay you” does not create a Payment or commercial obligation. A Message saying “service completed” does not establish earning recognition or payout eligibility. Financial changes require explicit structured, authorized, idempotent operations and owning-domain evidence.

### Promotion

Messages may contain human discussion of a Promotion, but chat does not create Promotion ownership, make a code valid, apply or redeem a Promotion, restore redemption, establish funding party, or change compatibility. ADR-006 and ADR-014 remain authoritative.

### Review and Reputation

Conversation moderation or Message sentiment does not automatically become a rating, review, reliability score, or reputation signal:

```text
CHAT SENTIMENT
!= RATING
!= REPUTATION
```

Future ADR-023 owns verified-experience review and reputation architecture. ADR-021 may provide authenticated interaction evidence only where an approved future review or moderation policy explicitly uses it. Message content or sentiment never defines reputation automatically.

### Taxonomy

Future ADR-022 remains the owner of Platform-governed taxonomy and reference-data lifecycle. Message hashtags, free text, labels, or extracted concepts do not become canonical taxonomy values.

### No Message-Driven Workflow Engine

Arbitrary natural-language Messages are not the primary trigger for Order, Appointment, Refund, payout, Promotion, booking, Subscription, entitlement, or earning state. Explicit structured commands and owning-domain validation remain necessary.

AI may later assist with spam detection, safety classification, moderation triage, translation, suggested replies, or suggested structured actions. AI output must not directly create authoritative business or Financial state. This ADR requires no AI moderation provider.

## Real-Time Delivery, Events / Notifications

### PostgreSQL Source of Truth

Messages are durably persisted in PostgreSQL as part of the authoritative local operation before or atomically with recording required integration intent. A temporarily offline participant must not lose a committed Message.

WebSocket, SSE, push, and other real-time mechanisms are transport concerns. They may provide near-real-time delivery but are not authoritative Message persistence, ordering, or participant authorization.

```text
EXTERNAL REAL-TIME TRANSPORT
!= MESSAGE SOURCE OF TRUTH
```

The architecture does not require durable distributed-presence infrastructure. Read receipts, typing indicators, presence, delivery indicators, and similar features are optional product capabilities and are not correctness-critical business state.

### Notifications

New Messages may trigger push, email, or in-app notification according to user preferences, privacy, blocks, moderation state, and policy. Notification delivery is not Message persistence. A failed, duplicated, delayed, or out-of-order notification must not create, delete, reorder, or authorize a Message.

### Transactional Outbox

Asynchronous integration effects use the accepted transactional-outbox architecture. Conceptual event meanings may include:

```text
MessageCreated
ConversationAccessChanged
ConversationBecameReadOnly
ModerationActionApplied
```

These names are examples only. This ADR does not finalize event names, aggregate types, payloads, publication rules, consumers, or versions. Exact event contracts belong to `docs/05-event-contracts.md`, follow ADR-009 outbox persistence, and follow ADR-016 event versioning.

Consumers and notification dispatch must be idempotent. Distributed transport ordering must not substitute for authoritative Conversation state or deterministic Message ordering.

## Search and Optional Product Features

Message search is not a launch architecture requirement. If introduced, every result and query must respect Conversation authorization, participant visibility, privacy, blocking, moderation, and retention policy.

PostgreSQL search capabilities are sufficient until measured requirements prove otherwise. OpenSearch or another search infrastructure dependency must not be introduced solely for MVP chat.

Read receipts, typing indicators, presence, delivery indicators, translations, suggested replies, and similar features remain optional. They must not become authoritative business state or force durable distributed presence infrastructure.

## Modular Monolith Boundary

ADR-001 remains authoritative. Conversation capability stays inside the single Spring Boot modular monolith and PostgreSQL system of record with clear module boundaries and infrastructure adapters.

This ADR does not require a:

- chat microservice;
- Conversation microservice;
- Message microservice;
- moderation microservice; or
- separate Conversation database.

Real-time delivery, object storage, scanning, and notification providers remain adapters or transport concerns. Future extraction requires a separate approved architecture decision based on demonstrated scale, operational isolation, security, or deployment needs.

## Identifiers and Time

ADR-010's identifier direction applies when persistence is designed. External provider identifiers and push-delivery identifiers do not define Conversation or Message identity. UUID order is not authoritative Message chronology.

Conversation creation, participant authorization/revocation, Message creation/edit/redaction, moderation, grace, and access changes are real instants. ADR-011 governs their temporal semantics. JVM/server local timezone is not authoritative. Any future business-local scheduling policy uses the owning context's approved timezone semantics rather than silently interpreting local timestamps.

## Consequences

### Positive

- Unsolicited marketplace messaging and public direct messaging are prohibited by architecture rather than only user-interface convention.
- Conversation authorization follows real, typed marketplace relationships.
- Actual Chef and Dietitian identities remain traceable independently of account and Organization membership.
- Organization administrators do not gain accidental access to private professional or Customer communication.
- Historical participant attribution survives Organization and business-context changes.
- Conversation history can survive domain lifecycle changes in controlled read-only form without granting indefinite write access.
- Multiple approved marketplace context families can reuse a common Conversation capability.
- Separate Conversation, participant, and Message concepts support least privilege and clear history.
- Context-specific grace policy supports legitimate post-service communication without creating permanent access.
- Moderation, blocking, redaction, policy-controlled editing, privacy handling, and legal deletion remain possible.
- Chat cannot silently mutate Order, Appointment, Subscription, Promotion, or Financial state.
- PostgreSQL durability remains separate from real-time transport and notification delivery.
- Typed authorization rules allow future contexts without approving arbitrary polymorphic references or universal social messaging.
- Multi-Chef communication can support separate or explicitly shared patterns while preserving privacy.

### Negative / Costs

- Every context family requires explicit read/write authorization rules and tests.
- Participant lifecycle and historical authorization evidence add persistence and application complexity.
- Read authorization, write authorization, read-only history, blocking, and moderation cannot be represented by one simple membership boolean.
- Context transitions require product policy and durable historical explanation.
- The canonical ERD needs carefully typed relationships without a universal polymorphic shortcut or giant nullable-FK table.
- Multi-role users require explicit acting identity and role resolution.
- Multi-Chef shared versus separate Conversation policy must avoid accidental disclosure.
- Moderation, redaction, legal retention, privacy deletion, and edit history require coordinated policy.
- Attachment storage, malware controls, authorization, and lifecycle introduce security and operational costs.
- Message editing and deletion races require version/conflict design without losing required audit evidence.
- Near-real-time delivery and offline synchronization add transport complexity even though PostgreSQL remains authoritative.
- Privileged support access requires dedicated capabilities, least privilege, and audit.

## Alternatives Considered / Rejected

### 1. Any Authenticated User May Directly Message Any Other User

Rejected because authentication does not establish a marketplace relationship or participant authorization and would enable unsolicited outreach.

### 2. Organization Membership Automatically Grants Professional Conversation Access

Rejected because Organization administration and professional private participation are distinct capabilities. Membership alone violates least privilege and Customer privacy.

### 3. Organization or Commercial Provider Identity Replaces the Actual Professional Participant

Rejected because the actual Chef or Dietitian must remain attributable for authorization, communication, safety, and history even when an Organization is the provider or beneficiary.

### 4. One Permanent DM Thread per User Pair

Rejected because two people may interact under different FoodRequest, Order, Appointment, engagement, or other contexts with different authorization periods and privacy boundaries.

### 5. Arbitrary Context Type Plus UUID as Canonical Referential Integrity

Rejected because it permits dangling, cross-type, and semantically invalid context relationships and cannot enforce context-specific cardinality or authorization meaning.

### 6. Universal ConversationContextRegistry

Rejected because a registry created merely to simulate polymorphism obscures domain ownership and does not itself enforce each context's authorization semantics.

### 7. Giant Nullable-FK Conversation Table

Rejected because unrelated nullable context columns create invalid combinations, weak constraints, and unsafe evolution.

### 8. Message Text Is Authoritative Order, Appointment, or Financial State

Rejected because unstructured content cannot replace validated, authorized, idempotent owning-domain operations.

### 9. Chat Commands Directly Create Refund, Payout, or Earning

Rejected because ADR-012, ADR-015, and ADR-020 require structured Financial authority, evidence, and audit. Natural-language requests are not Financial facts.

### 10. All Messages Are Immutable Forever Like Ledger Entries

Rejected because moderation, redaction, privacy, legal deletion, and policy-controlled editing differ from immutable POSTED Financial history.

### 11. All Messages Are Physically Deleted When a Business Context Ends

Rejected because read-only history, support, disputes, safety, legal duties, and privacy policy require context-sensitive retention decisions.

### 12. External Notification or WebSocket Transport Is the Source of Truth

Rejected because transports can fail, duplicate, delay, reorder, or lose ephemeral delivery. PostgreSQL Message persistence is authoritative.

### 13. Auth0 `sub` Is the Canonical Marketplace Participant Identity

Rejected because an authentication subject is not a durable typed Customer, Chef, Dietitian, or other domain identity and cannot express multi-role participation scope.

### 14. Platform or Organization Administrators Silently Impersonate Professionals

Rejected because it destroys actor attribution, violates least privilege, and creates safety and audit risk.

### 15. External Provider IDs Define Message Identity

Rejected because providers are adapters, may change, and do not own canonical Message identity or idempotency.

### 16. UUID Ordering Defines Message Chronology

Rejected because ADR-010 identifiers do not replace explicit deterministic Message creation/order semantics.

### 17. Public Object URLs for Attachments

Rejected because URL possession would bypass current Conversation authorization, revocation, privacy, and retention policy.

### 18. Automatic Recording, Transcription, or Clinical Archiving for Dietitian Chat

Rejected because ADR-021 does not create an EMR or decide regulated clinical communication. Such behavior requires explicit legal, privacy, security, and product approval.

### 19. Chat Sentiment Determines Review, Reputation, Service Performance, or Earning

Rejected because unstructured sentiment is not a verified rating, structured service fact, or Financial source. ADR-023 and the owning business/Financial domains remain authoritative.

### 20. A Generic Social-Messaging Microservice Is Required for MVP

Rejected because ADR-001 establishes the modular-monolith baseline, and current Conversation correctness benefits from the shared PostgreSQL consistency boundary and explicit domain interfaces.

### 21. Historical Participation Is Recomputed from Current Organization Membership

Rejected because professionals may change Organizations and administrators may change roles. Current membership must not rewrite historical authorization or sender attribution.

### 22. A Historical Participant May Always Continue Sending

Rejected because write authorization depends on current context, lifecycle, grace, block, moderation, and participant state.

### 23. Every Multi-Chef Order Uses One General Group Chat

Rejected because product policy may require separate ChefOrderGroup-scoped communication, and schema convenience cannot override least privilege or privacy.

### 24. Every Multi-Chef Order Requires Separate Conversations

Rejected because an explicitly authorized shared Order Conversation may be appropriate. The architecture supports both product choices without assuming access.

### 25. Generic Metadata Is the Canonical Authorization Model

Rejected because `entity_type`, `entity_id`, or JSON metadata cannot replace typed participant/context relationships and enforceable authorization meaning.

### 26. OpenSearch Is Required for MVP Chat

Rejected because search is optional, PostgreSQL is sufficient initially, and a new infrastructure dependency requires demonstrated need and an approved architecture decision.

### 27. Blocking Mutates or Cancels the Underlying Business Workflow

Rejected because safety communication controls do not own Order, Appointment, Refund, Subscription, or Financial state.

### 28. Message Parsing Is a General Workflow Engine

Rejected because authoritative state changes require explicit structured commands. AI suggestions may assist users later but cannot bypass owning-domain authorization and validation.

## Dependencies / Related ADRs

- **ADR-001 — Modular Monolith First (Accepted):** Conversation remains within the modular monolith; no chat, Message, Conversation, or moderation microservice is required.
- **ADR-005 — Order Fulfillment Type Separation (Proposed):** Order pickup and delivery lanes remain Order-owned. Conversation cannot change fulfillment state.
- **ADR-006 — Promotion Targeting Model (Accepted):** Promotion ownership and targeting remain separate from Conversation and Message content.
- **ADR-009 — Outbox Table Schema (Accepted):** asynchronous Conversation, delivery, moderation, and notification effects use transactional-outbox persistence where appropriate.
- **ADR-010 — UUIDv7 Identifier Strategy (Proposed):** future Conversation records follow the repository identifier direction without treating UUID order as Message chronology.
- **ADR-011 — Timezone Modeling (Proposed):** Conversation and Message occurrences use real-instant semantics; server local time is not authoritative.
- **ADR-012 — Payment Marketplace Settlement (Proposed):** Payment, Refund, Payout, and provider orchestration remain Financial concerns and cannot be created from chat text.
- **ADR-013 — ChefOrderGroup Aggregate + Financial Boundary (Proposed):** ChefOrderGroup may provide typed Order communication authorization evidence but does not become Conversation, participant, or Message thread.
- **ADR-014 — Promotion Engine (Proposed):** Promotion evaluation, application, redemption, restoration, and funding semantics remain Promotion-owned.
- **ADR-015 — Financial Ledger and Reconciliation (Proposed):** ledger immutability and reconciliation remain separate; a Chat Message is not a LedgerEntry.
- **ADR-016 — Event Versioning (Accepted):** future Conversation event contracts require explicit compatible versioning.
- **ADR-017 — Professional Identity, Credentials and Jurisdiction Eligibility (Proposed):** durable professional identity and Organization authorization are consumed as inputs but do not automatically grant Conversation access.
- **ADR-018 — Dietitian Engagement, Appointment Scheduling and Online Meeting Provisioning (Proposed):** DietitianClientEngagement and Appointment may provide typed authorization context while retaining their own lifecycle and authority.
- **ADR-019 — Subscription, Entitlement and Materialized Occurrence Architecture (Proposed):** Subscription contexts remain separate, and Messages cannot mutate agreement, entitlement, occurrence, or KitchenBooking state.
- **ADR-020 — Commercial Obligations, Earning Recognition and Payable-Source Financial Model (Proposed):** Conversation content is not authoritative commercial, earning, refund, payout, or Financial evidence.

No related ADR status is changed by this Proposed ADR.

## Future ADR Relationships

- **ADR-022 — Platform-Governed Taxonomy and Reference-Data Lifecycle:** remains independent. Message text, hashtags, or extracted concepts do not become canonical taxonomy.
- **ADR-023 — Verified-Experience Reviews and Reputation:** may use authenticated interaction evidence only where an approved future review or moderation policy explicitly permits it. Conversation content and sentiment do not automatically define review eligibility, rating, reliability, reputation, service performance, or earning.

This ADR does not draft, pre-accept, or change the status of ADR-022 or ADR-023.

## Out of Scope

This Proposed ADR does not decide or introduce:

- a public social network, feed, followers, or public Conversation discovery;
- arbitrary user-to-user direct messages;
- voice calling;
- video calling implementation;
- meeting-provider implementation;
- automatic recording or transcription;
- EMR, clinical records, diagnoses, medication, laboratory, or clinical-document architecture;
- Financial operations through natural-language chat;
- review or reputation scoring;
- taxonomy lifecycle;
- exact moderation policy catalog;
- exact legal retention duration or legal-hold policy;
- exact privacy deletion schedule;
- exact attachment malware/scanning provider;
- an end-to-end encryption claim or key-management design;
- exact tables, columns, foreign keys, indexes, constraints, or migration SQL;
- exact REST endpoints, request/response fields, errors, pagination, or OpenAPI schemas;
- exact WebSocket/SSE protocol or payload;
- exact domain/integration event names, payloads, aggregate types, publication rules, or versions;
- exact Conversation transition, grace duration, edit window, deletion, or participant-facing moderation UX;
- a universal ConversationContextRegistry;
- a giant nullable-FK Conversation table;
- generic polymorphic participant/context authorization; or
- a chat, Conversation, Message, moderation, presence, or search microservice.

## Implementation / Propagation Notes

This Proposed ADR does not authorize application code, migrations, SQL, API changes, or event-contract changes by itself. After approval, canonical propagation and implementation planning must:

1. Reconcile `docs/03-database-erd.md` with typed Conversation, ConversationParticipant, Message, authorization-context, context-history, participant-history, moderation, read-state, attachment, and idempotency relationships.
2. Select typed context-family relationships with enforceable domain meaning and referential integrity where practical; do not use unconstrained `context_type + context_id`, a universal context registry, a giant nullable-FK table, or generic JSON metadata as canonical authorization.
3. Preserve actual Customer, Chef, and Dietitian domain identity and acting role without using Auth0 `sub` as canonical participant identity.
4. Define context-specific authorization for FoodRequest consent, Order/ChefOrderGroup participation, DietitianClientEngagement, Appointment, and any later approved context.
5. Define the product policy for separate versus explicitly shared multi-Chef Order Conversations and test least-privilege disclosure boundaries.
6. Define context-transition policy, including when FoodRequest-to-Order continuity reuses a Conversation or creates a new one, while preserving historical authorization evidence.
7. Define current send authorization separately from current/historical read authorization, including participant removal, revocation, blocking, account state, moderation, and lifecycle.
8. Define context-specific grace and read-only policy without one universal duration.
9. Define policy-controlled Message editing, hiding, redaction, moderation removal, legal/privacy deletion, retention, and required audit evidence without ledger-style blanket immutability.
10. Define privileged support/moderation capabilities, least privilege, actor attribution, audit, and explicit prohibition of ordinary participant impersonation.
11. Define private attachment upload, scanning, access, expiry, revocation, retention, deletion, and safe rendering without public permanent object URLs.
12. Define deterministic Message ordering independent of UUID, event, push, and WebSocket delivery order.
13. Define client/request Message idempotency and database uniqueness needed to make retries safe.
14. Define concurrency controls for send, edit, redact, moderation, and participant revocation with mutation-time authorization checks.
15. Keep PostgreSQL Message persistence authoritative and real-time, push, email, and in-app delivery asynchronous transport concerns.
16. Update `docs/04-api-contracts.md` only after exact authorized REST and real-time representations are approved.
17. Update `docs/05-event-contracts.md` only after exact events are approved, following ADR-009 and ADR-016.
18. Define privacy-minimized telemetry that excludes full private Message bodies by default.
19. Add tests for cross-Conversation isolation, stale authorization, role confusion, multi-Chef privacy, Organization-admin denial, professional history, blocking, moderation, attachment access, idempotency, deterministic ordering, context end, grace, read-only history, and offline delivery.

At minimum, future tests must prove that arbitrary authenticated users cannot initiate direct messages; FoodRequest discovery alone does not authorize Chef outreach; Dietitian discoverability alone does not authorize outreach; Organization membership does not grant professional Conversation access; actual professional identity remains attributable after Organization changes; historical participation does not imply current send authority; Conversation IDs and attachment URLs do not bypass authorization; multi-role accounts act under an explicit role; multi-Chef Conversations do not expose unrelated communication; context end follows context-specific grace/read-only policy without immediate forced deletion; blocking and moderation do not mutate business or Financial state; Message text cannot change Order, Appointment, Subscription, Promotion, Refund, payout, earning, review, reputation, or taxonomy; durable Messages survive offline delivery and notification failure; retries do not duplicate Messages; UUID and transport order do not define chronology; and no generic social messaging, polymorphic metadata authorization, public attachment URL, or chat microservice is introduced.
