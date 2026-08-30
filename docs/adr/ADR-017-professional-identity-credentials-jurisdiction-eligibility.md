# ADR-017: Professional Identity, Credentials and Jurisdiction Eligibility

## Status

Proposed

## Context

Cheffy Bites supports professional services performed by Chefs and Dietitians. Those services may be offered independently or through an Organization, including a one-person business, a multi-professional food business, a clinic, or Cheffy Operations during temporary supply bootstrap. The architecture must identify the person who actually performed a service without confusing that person with the account that authenticated, the Organization through which the person was authorized to act, the commercial provider, or the settlement beneficiary.

The current architecture already requires a `ChefOrderGroup` to identify one durable actual Chef performer inside one concrete food Order. It also requires a practicing Dietitian to have a durable `DietitianProfessionalProfile` that identifies the actual practitioner even when a clinic or another Organization is commercially involved. The exact professional identity, credential, jurisdiction-eligibility, and effective-dated Organization-authorization architecture has remained deferred.

Authentication, professional practice, Organization administration, commercial supply, and settlement have different lifecycles and authority. An Auth0/OIDC subject can change identity-provider context without changing completed-service history. A professional can join, leave, or concurrently operate through different Organizations without becoming a different professional. An Organization administrator may manage operational resources without being qualified to perform a professional service. An Organization may be commercially responsible for a service without being the human performer, and the performer is not necessarily the party owed marketplace settlement.

Professional credentials introduce a further distinction. A submitted or self-attested qualification is not independently verified. A verified credential is not by itself a complete legal or policy decision that a professional may provide a particular service in the applicable jurisdiction at a particular time. Original credential documents are sensitive evidence and must not become public-profile content merely because selected credential metadata may be shown to Customers.

Historical Orders, Appointments, service records, and future verified-experience reputation must remain explainable after account, profile, credential, eligibility, or Organization relationships change. Current Organization membership cannot be the source of truth for historical service attribution.

This decision therefore establishes common architecture rules without creating one universal `Professional`, `Provider`, `Worker`, `Employee`, `Payable`, or `Subscription` aggregate and without replacing role-specific professional identities.

## Decision

### 1. Core Identity Separation

Cheffy Bites will preserve the following distinct identities:

```text
AUTHENTICATED ACCOUNT / PRINCIPAL
!= PROFESSIONAL SERVICE PERFORMER
!= ORGANIZATION
!= COMMERCIAL PROVIDER
!= SETTLEMENT BENEFICIARY
```

Each identity answers a different question:

- **Authenticated account/principal:** Who can authenticate and present a platform security principal?
- **Professional service performer:** Who actually provides the professional service and bears individual professional or operational accountability?
- **Organization:** Which legal, commercial, administrative, or operating entity is involved?
- **Commercial provider:** Which approved marketplace business commercially supplies the service?
- **Settlement beneficiary:** Which approved party is owed marketplace settlement under the applicable commercial arrangement?

The identities may coincide in a particular arrangement, but no equivalence is assumed or made irreversible by this ADR.

The following invariants are mandatory:

```text
AUTHENTICATED PRINCIPAL
!= PROFESSIONAL PERFORMER

PROFESSIONAL PERFORMER
!= ORGANIZATION

PROFESSIONAL ORGANIZATION AUTHORIZATION
!= COMMERCIAL PROVIDER

PROFESSIONAL ORGANIZATION AUTHORIZATION
!= SETTLEMENT BENEFICIARY

PROFESSIONAL ORGANIZATION AUTHORIZATION
!= PAYROLL

ORGANIZATION ADMIN
!= PROFESSIONAL PERFORMER

CREDENTIAL SUBMITTED
!= CREDENTIAL VERIFIED

SELF_ATTESTED
!= PLATFORM_VERIFIED

CREDENTIAL VERIFIED
!= CURRENT JURISDICTION ELIGIBILITY

CURRENT ORGANIZATION MEMBERSHIP
!= HISTORICAL SERVICE ATTRIBUTION

PRIVATE CREDENTIAL EVIDENCE
!= PUBLIC PROFESSIONAL PROFILE
```

Service-performer identity must remain historically durable.

### 2. Authentication Identity and Domain Identity

Auth0/OIDC authenticates a principal; it does not own the canonical Chef or Dietitian business identity. An external identity-provider `sub` must not be used as the canonical professional identifier or embedded as the durable identity of historical professional service.

The identity/security capability binds an authenticated account to authorized internal platform roles and role-specific durable domain identities. The exact binding persistence is later canonical ERD work, but it must preserve the internal domain identity independently of an external issuer or subject lifecycle.

One authenticated user may hold more than one authorized platform role where product, legal, and authorization policy permit. The architecture must not inherently prevent one person from having all of the following at the same time:

- a Chef identity;
- a `DietitianProfessionalProfile`; and
- an Organization-administrator role.

Those identities and capabilities remain distinct. Possessing one does not automatically create, activate, or authorize another.

### 3. Role-Specific Professional Identities

Cheffy Bites will not require a giant universal `Professional`, `ProfessionalProfile`, `ServiceProvider`, or `Provider` aggregate merely because Chef and Dietitian share credential, eligibility, or Organization-authorization concerns.

Role-specific professional identities retain their own invariants and lifecycle:

- The existing durable Chef professional/operational identity remains the actual Chef performer identity.
- `DietitianProfessionalProfile` remains the actual practicing Dietitian identity.

Common credential, evidence, eligibility, and Organization-authorization capabilities may provide shared policy and services. Their exact persistence must nevertheless preserve typed referential integrity to the applicable role-specific identity. This ADR does not approve an unconstrained `professional_type + professional_id UUID`, `entity_type + entity_id`, or arbitrary metadata relationship as the final relational model.

The canonical ERD may select role-specific tables, typed association tables, explicit foreign keys, or another relationally enforceable design. It must not force all role-specific identities into one universal aggregate solely to make a generic foreign key convenient. This ADR does not finalize those tables, keys, cardinalities, or constraints.

### 4. Chef Actual-Performer Identity

ADR-013 remains authoritative for the `ChefOrderGroup` aggregate. One `ChefOrderGroup` represents one concrete Order plus one durable actual Chef performer/operational Chef identity. A common employer, Organization, commercial provider, settlement beneficiary, connected account, or Organization engagement must not merge Ravi's and Maria's separate `ChefOrderGroup` records.

Conceptually:

```text
Order O1 at Kitchen K1
    ├── ChefOrderGroup Ravi  → actual Chef Ravi
    └── ChefOrderGroup Maria → actual Chef Maria

Organization context: ABC Food Group
```

The applicable Chef must be authorized and eligible in the service context when the owning Order workflow requires that decision. ADR-017 supplies the durable performer and Organization-authorization semantics needed for that validation; it does not redesign `ChefOrderGroup` or choose the final physical performer reference.

In particular, this ADR does not require the eventual `ChefOrderGroup` foreign key to be named `chef_id`, `chef_profile_id`, `professional_profile_id`, `membership_id`, or any other specific column. The later canonical ERD reconciliation must select a typed representation that enforces the actual-performer invariant without binding the Chef's identity to current Organization membership.

### 5. Dietitian Professional Identity

A practicing Dietitian has one durable individual `DietitianProfessionalProfile` per practicing user under the current product rule. That profile identifies **who actually provides the professional service**. It is not replaced by a clinic, practice, employer, Organization, Organization membership, or Organization administrator.

The Organization identifies the business, clinic, practice, legal, commercial, or operating context through which the Dietitian may be authorized. Every Dietitian professional service must retain the actual practicing Dietitian identity even when an Organization later becomes commercial provider or settlement beneficiary under a separately approved arrangement.

An Organization administrator who lacks an active, qualified `DietitianProfessionalProfile` and applicable professional authorization must not be treated as the practicing Dietitian merely because that person administers the clinic. Professional accountability and future verified-experience reputation remain attached to the actual Dietitian performer.

### 6. Conceptual Ownership

Ownership is divided without duplicating authoritative facts:

#### Identity/security capability owns

- authenticated account/principal binding;
- external identity-provider linkage; and
- platform account-role authorization at the security boundary.

#### Role-specific professional domains own

- the durable Chef professional/operational identity and Chef-specific lifecycle; and
- `DietitianProfessionalProfile` and Dietitian-specific professional lifecycle.

#### Professional eligibility capability owns

- professional credentials and credential lifecycle;
- private credential-evidence references and access policy;
- verification status and evidence of verification; and
- jurisdiction- and service-aware eligibility decisions.

#### Organization capability owns

- Organization identity and lifecycle;
- Organization administrative authority;
- Organization resource and operational authority where applicable; and
- the professional-to-Organization authorization/engagement context.

Module boundaries may expose explicit internal application interfaces and may publish integration events through the transactional outbox when decoupling is justified. One module must not maintain a duplicate authoritative copy of another module's credential, professional identity, or Organization-engagement truth.

### 7. Professional-to-Organization Authorization

Cheffy Bites will model a first-class conceptual professional-to-Organization authorization relationship. This ADR calls it `ProfessionalOrganizationEngagement` for clarity. The exact aggregate and table name remains subject to the later canonical ERD.

The engagement answers:

- Is this specific role-qualified professional authorized to provide services through this Organization?
- Which professional role and approved capabilities are authorized?
- During which effective period is the authorization valid?
- Was it active in the Organization context applicable to a historical service?

It does not define worker compensation or commercial economics.

```text
PROFESSIONAL ORGANIZATION ENGAGEMENT
!= EMPLOYMENT / PAYROLL RECORD

AUTHORIZED TO PERFORM THROUGH ORGANIZATION
!= COMMERCIAL PROVIDER
!= SETTLEMENT BENEFICIARY
```

An engagement associates one role-specific professional identity with one Organization and one typed professional role/capability context. The final relational model must preserve whether the authorized performer is a Chef or Dietitian using typed referential integrity rather than an arbitrary UUID reference.

Organization membership or administrative authority may coexist with a professional engagement, but neither substitutes for it. Administrative authorization answers whether a user may manage Organization resources. Professional-service authorization answers whether a qualified professional may perform a particular professional role through that Organization.

### 8. Engagement Lifecycle and Effective Dating

The conceptual engagement lifecycle is:

```text
PENDING → ACTIVE → SUSPENDED → ACTIVE
                  └──────────→ ENDED

PENDING ─────────────────────→ ENDED
ACTIVE  ─────────────────────→ ENDED
```

The lifecycle is intentionally small and is not an HR lifecycle.

- **PENDING:** The Organization-side request, invitation, assignment, or professional identity-binding process is incomplete. It authorizes no new Organization-context professional service.
- **ACTIVE:** The required onboarding and acceptance/binding process is complete, the effective start has been reached, no applicable end has occurred, and the engagement may authorize new Organization-context service subject to all other eligibility rules.
- **SUSPENDED:** New Organization-context service is not authorized while suspension is effective. History remains intact. Reactivation may restore authorization prospectively according to policy.
- **ENDED:** The authorization has ended or been revoked. It is not usable for new Organization-context service and is retained as historical evidence.

Only an effective `ACTIVE` engagement authorizes new professional service through that Organization. The model must preserve, conceptually:

- effective start;
- effective end or revocation where applicable;
- status-transition timing;
- suspension periods or equivalent durable evidence when historical explanation requires them;
- professional and Organization identities;
- authorized role/capabilities; and
- audit actor/reason context appropriate to policy.

Ending or suspending an engagement must not delete the professional identity, prior engagement history, or historical service attribution. This ADR does not introduce employee termination, leave, benefits, payroll suspension, or other HR states.

### 9. Professional Consent and Activation

Ordinary Organization administration must not silently publish or impersonate a real person's professional identity by creating a staff row.

Activation requires an authorized identity-binding and onboarding outcome conceptually equivalent to:

```text
Organization invitation/request/authorized assignment
    + professional account/profile acknowledgement
      or another approved identity-binding flow
    + applicable onboarding and authorization checks
    = ACTIVE professional Organization engagement
```

The exact invitation and acceptance UX is deferred. The process must establish that the durable professional identity is bound to the consenting or otherwise lawfully authorized account/profile. Controlled administrative correction or data-migration exceptions may exist for authorized Platform operations, but they must be auditable and must not turn ordinary Organization administration into a professional-impersonation capability.

### 10. Multiple Organizations and Independent Practice

A professional is not restricted by architecture to exactly one Organization. Subject to product, legal, conflict, Kitchen, and professional policy, a professional may hold concurrent or sequential active engagements, for example:

```text
Dietitian D
    ├── authorized through Clinic A
    ├── authorized through Clinic B
    └── may also practice independently where permitted
```

The same principle applies to a Chef operating through different qualified Organizations over time or concurrently where policy allows. Current Organization membership or engagement is not part of the professional's immutable identity.

Professional identity does not require current membership in an external Organization. An independent professional may operate independently where product and law permit. If the independent professional operates through a one-person business, the ordinary Organization model may represent that business. The professional performer and the Organization remain separate identities even when controlled by the same person.

### 11. Organization-Operated and Cheffy-Operated Supply

Organization-operated supply uses the same model as independent supply:

```text
ABC Food Group
    ├── authorizes Chef Ravi
    └── authorizes Chef Maria
```

Ravi and Maria remain separate actual professional performers. Their active engagements establish that they may perform through ABC Food Group in the approved context. Whether ABC Food Group is also the commercial provider or settlement beneficiary is a separate ADR-020 and commercial-policy decision.

This must not be represented as an individual marketplace payout to Ravi or Maria that is redirected to ABC Food Group. Employment or contractor compensation remains outside this ADR.

Cheffy's temporary bootstrap supply uses the ordinary Organization model:

```text
Cheffy Operations Organization
    ├── authorizes Chef A
    ├── authorizes Chef B
    └── may authorize qualified Dietitians where approved
```

No `CheffyProfessional`, `CheffyEmployeeProfessional`, `internal_provider`, special performer identity, or `if organization == CHEFFY` architecture is introduced. Later reduction or elimination of Cheffy-operated supply must require no professional-identity redesign.

### 12. Durable Historical Service Identity and Authorization

Historical service facts must not be recomputed from current account links, profile state, credentials, eligibility, Organization membership, or engagement state.

For a historical `ChefOrderGroup`, Appointment, professional review source, or other service record, the architecture must preserve enough durable references or immutable decision evidence to answer:

1. Who actually performed the service?
2. Through which authorized Organization context, if any, was it performed?
3. Was the professional authorized and eligible when the service was confirmed or performed according to the owning workflow's policy?

If Ravi later leaves ABC Food Group, joins another Organization, or begins independent practice, old Orders performed by Ravi through ABC Food Group must continue to identify Ravi as performer and ABC Food Group as the applicable historical Organization context. They must not be rewritten to Ravi's new Organization or explained using current membership.

Current display information may change, and current/latest references may exist for convenience. Historical service evidence must retain stable professional identity and applicable transaction-time authorization/eligibility context. The exact snapshot, decision-record, version, reference, and denormalized display strategy remains later canonical ERD and owning-domain work.

### 13. Professional Credential Concept

The professional eligibility capability will maintain durable professional credential facts without making credentials the professional identity itself.

Credential categories must support at least:

```text
DEGREE
LICENSE
CERTIFICATION
REGISTRATION
OTHER
```

Governed reference data may later provide safe extensibility. A credential may conceptually retain:

- the typed role-specific professional identity;
- credential category/type;
- issuer;
- jurisdiction where relevant;
- credential or reference number where appropriate and permitted;
- issue date;
- expiry date where applicable;
- submitted metadata;
- credential/verification status;
- references to private supporting evidence and verification evidence;
- verification actor/provider and verification time where applicable; and
- created and updated timestamps.

This is a conceptual model, not a final table or column specification.

Credential requirements are role-, service-, Organization-, Platform-policy-, and jurisdiction-specific. This ADR does not imply that every Chef is subject to the same regulated-profession model as a Dietitian.

Examples include:

- **Dietitian:** degree, license, professional registration, or other evidence required by the applicable jurisdiction and service policy.
- **Chef:** food-handler certification, culinary qualification, business evidence, or local permit evidence where required by applicable policy or jurisdiction.

Cheffy may require additional Platform qualifications beyond a legal minimum where approved. Those requirements must remain explicit policy rather than being inferred merely from the credential category.

### 14. Credential Status and Truthful Verification

Credential representation must distinguish at least these concepts:

```text
SUBMITTED
SELF_ATTESTED
PLATFORM_VERIFIED
REJECTED
```

- **SUBMITTED:** Metadata or evidence has been received for review. Submission is not verification.
- **SELF_ATTESTED:** The professional has asserted the information, but Cheffy or an approved verifier has not independently completed the required verification. It must not be presented as verified.
- **PLATFORM_VERIFIED:** Cheffy or an approved verification provider actually completed the defined verification procedure and retained appropriate verification evidence or reference. Document upload alone must never produce this status.
- **REJECTED:** Review did not satisfy the applicable requirement, evidence was unacceptable, or the claim was otherwise rejected under policy. Rejection does not imply a legal conclusion beyond the reviewed policy scope.

Operational concepts such as `NEEDS_REVIEW`, `EXPIRED`, or `REVOKED` may be introduced during detailed lifecycle design if policy requires them. The design must distinguish expiry or revocation from the historical fact that a credential was previously verified. Status history or equivalent evidence must remain auditable; a current status update must not erase the earlier verification decision that explained a completed service.

Verification scope must be explicit. `PLATFORM_VERIFIED` means only that the defined verification was actually performed; it is not a blanket warranty of professional competence, legal authority in every jurisdiction, or current eligibility for every service.

### 15. Private Credential Evidence and Public Representation

Credential metadata and private credential evidence are separate concerns.

Private evidence may include:

- an uploaded degree;
- a professional-license image or PDF;
- a registration certificate;
- identity-supporting evidence; and
- other sensitive verification material.

Private evidence must be stored in controlled private object/file storage with authorization checks, least privilege, auditable access, controlled time-limited object access where needed, and no public bucket or object exposure. Public object URLs, private object keys, and raw evidence must not be exposed to Customers or embedded in public-profile JSON.

Customer-facing professional representations may contain only policy-approved safe information, for example:

- qualification label;
- applicable jurisdiction;
- truthful verification status;
- safe issuer information; and
- current validity status.

Organization authorization does not grant Organization administrators unrestricted access to all private professional documents. An authorized Organization actor may receive the minimum operational decision or metadata needed to know whether required credentials exist, whether the professional is eligible, and whether verification or expiry requirements are satisfied. Raw evidence remains restricted unless explicit policy and legal authority permit access for a defined purpose.

The following data boundaries remain separate:

```text
PUBLIC PROFESSIONAL PROFILE
!= PRIVATE CREDENTIAL METADATA / EVIDENCE
!= CLINICAL OR CUSTOMER RECORDS
```

Credential evidence is sensitive operational identity/professional data. Access, disclosure, retention, correction, and deletion require approved privacy and legal policy. This ADR does not decide exact retention or deletion periods.

### 16. Jurisdiction Eligibility Is a Separate Decision

Cheffy Bites will maintain a conceptual `ProfessionalJurisdictionEligibility` decision or equivalent capability. The exact aggregate/table name remains later ERD work.

It answers:

> Can this role-specific professional currently provide this professional service in the applicable jurisdiction and service context?

Eligibility may depend on:

- professional role and profile state;
- required credential presence and verification state;
- credential validity, expiry, suspension, or revocation;
- applicable jurisdiction or jurisdictions;
- service type and mode;
- regulatory policy;
- Platform policy; and
- active Organization authorization where the service is provided through an Organization.

Credential verification is an input to eligibility; it is not eligibility itself.

```text
VERIFIED CREDENTIAL
!= CURRENT PROFESSIONAL JURISDICTION ELIGIBILITY
```

Eligibility must be effective-dated and capable of representing concepts equivalent to:

- not yet evaluated;
- eligible from an effective time;
- eligible until an expiry/effective end where applicable;
- suspended; and
- ineligible.

The model must retain the policy basis, applicable professional role/service/jurisdiction context, decision timing, and evidence/version references needed for audit and historical explanation. A later eligibility change must govern future decisions without rewriting completed-service history.

### 17. Applicable Service Jurisdiction

The applicable regulatory jurisdiction is determined from service context and approved legal/policy rules. It must not be hard-coded universally to Organization headquarters, an account address, an authenticated user's device timezone, or a browser location.

Examples of potentially relevant context include:

- Chef food service: the physical Kitchen or service jurisdiction;
- in-person Dietitian consultation: the `ConsultationLocation` and applicable professional-service rules; and
- online Dietitian consultation: the Customer/service location and any other jurisdiction made applicable by approved regulatory rules.

These examples identify required architecture inputs; they do not decide law. Before launch in a jurisdiction, Cheffy must confirm applicable professional-title, licensing, privacy, credential, and service-authorization requirements. The architecture records and enforces approved policy decisions but does not invent legal eligibility.

A self-attested credential must not be treated as satisfying a legal verification or licensing requirement unless an approved policy, based on confirmed legal requirements, explicitly permits that evidence level for the specific requirement. Architecture cannot convert attestation into legal authorization.

### 18. Service-Time Authorization Decision

Before a professional service becomes confirmable or executable through a particular professional/Organization context, the owning workflow must be able to obtain an authoritative authorization decision validating at least:

1. the role-specific professional profile/identity is active;
2. applicable credential requirements are satisfied at the required evidence level;
3. jurisdiction- and service-specific eligibility is current for the applicable time and context; and
4. the professional-to-Organization engagement is effective and `ACTIVE` where the service is provided through that Organization.

The professional eligibility capability supplies authoritative identity, credential, eligibility, and Organization-authorization decisions. The owning service domain orchestrates the check and owns the service transition:

- the Chef Order workflow owns food-service confirmation/execution decisions;
- future ADR-018 owns Dietitian Appointment confirmation and scheduling workflow; and
- a future subscription occurrence workflow owns its applicable occurrence decision.

Authorization at one point does not invent policy for what happens if eligibility or engagement changes before performance. The owning service and legal policy must define whether to prevent execution, cancel, reassign, remediate, or require review. Any such action must preserve the original decision and historical evidence rather than rewriting it.

### 19. Recruiting and Candidate Boundary

A `CandidateApplication` or other recruiting record is separate from a marketplace professional identity, public profile, credential-verification record, Organization engagement, or service authorization.

A job applicant's resume, private application, credential documents, interview data, and hiring assessments must not automatically:

- create or activate a Chef identity or `DietitianProfessionalProfile`;
- publish a public professional profile;
- create an `ACTIVE` Organization engagement;
- authorize professional service; or
- become marketplace credential evidence without an authorized purpose and required consent.

If hired or engaged, the candidate proceeds through the normal professional onboarding, identity binding, credential, eligibility, and Organization-authorization process. If not hired, Cheffy may invite the person to join the marketplace only through explicit opt-in and ordinary onboarding. This ADR does not create an applicant-tracking-system schema.

### 20. Commercial, Financial, Promotion, and Payroll Boundaries

An active professional Organization engagement establishes only professional authorization through that Organization. It does not establish the commercial provider, settlement beneficiary, connected account, earning owner, payout recipient, Promotion owner, Promotion target, or Promotion funding source.

Future ADR-020 owns commercial-provider obligations, settlement-beneficiary relationships, earning recognition, and payout eligibility. ADR-012 owns provider-neutral Payment and Refund orchestration. ADR-015 owns immutable ledger posting and reconciliation. ADR-006 owns typed Promotion owner and target identity. ADR-014 owns Promotion evaluation, benefit, compatibility, funding behavior, and application evidence.

ADR-017 adds no ownership of:

- `Payment` or `PaymentAllocation`;
- `Refund` or financial remediation;
- `Payout` or payout grouping;
- a universal `Payable` or `Earning` aggregate;
- `LedgerTransaction` or `LedgerEntry`; or
- Promotion calculation or funding.

The boundaries are explicit:

```text
AUTHORIZED TO PERFORM THROUGH ORGANIZATION
!= COMMERCIAL PROVIDER
!= SETTLEMENT BENEFICIARY

PROFESSIONAL ORGANIZATION ENGAGEMENT
!= EMPLOYMENT / PAYROLL RECORD

MARKETPLACE SETTLEMENT
!= EMPLOYEE / CONTRACTOR PAYROLL
```

This ADR does not define salary, hourly wage, payroll frequency, withholding, payroll taxes, benefits, employee commission, employee bonus, contractor invoice, timesheet, pay period, or compensation calculation. Marketplace service evidence may be relevant to a separate lawful worker-compensation process, but that process is not part of professional authorization or marketplace Financial architecture.

Professional Organization engagement also does not imply Promotion ownership, target, calculation scope, or funding source. Those remain independently authorized decisions under ADR-006 and ADR-014.

### 21. Review, Reputation, and Conversation Boundaries

The durable actual-professional identity established here must be stable enough for future ADR-023 to attach verified-experience review eligibility and reputation to the actual performer. Chef Ravi's individual service reputation must not become ABC Food Group's individual-Chef reputation, and a Dietitian Appointment review must remain associated with the actual Dietitian. This ADR does not define review aggregation, scoring, moderation, or Organization reputation.

Professional identity and Organization authorization may be inputs to future conversation authorization. ADR-021 owns `Conversation`, `Participant`, `Message`, context-specific write access, retention, and multi-context conversation architecture. This ADR does not design chat or messaging.

### 22. Scheduling and Appointment Boundary

This ADR does not design Dietitian scheduling or Appointment concurrency. Future ADR-018 owns:

- `ConsultationOffering`;
- `DietitianClientEngagement`;
- Appointment lifecycle;
- availability and scheduling holds;
- overlap and concurrency protection;
- rescheduling, cancellation, and no-show behavior;
- online meeting provisioning and calendar integration; and
- `ConsultationLocation`.

ADR-017 supplies only the role-specific Dietitian identity, credential, jurisdiction-eligibility, and Organization-authorization decisions that the Appointment workflow will reference. ADR-007 remains specific to KitchenBooking and EquipmentRental concurrency and is not generalized here.

### 23. Event and Transactional-Outbox Integration

Important professional authorization changes may require integration events, including conceptually:

- credential status changed;
- jurisdiction eligibility changed;
- professional Organization engagement activated; and
- professional Organization engagement suspended or ended.

The owning module persists its state change and transactional-outbox record in the same local database transaction where an event is required. Consumers must be idempotent and must not reconstruct authoritative current eligibility solely from an incomplete event history.

This ADR does not finalize event names, payloads, schemas, consumers, or publication policy. `docs/05-event-contracts.md` remains authoritative for exact event contracts. Any future event follows Accepted ADR-016, including explicit event versioning and compatible evolution.

### 24. Temporal and Identifier Architecture

Proposed ADR-011 remains authoritative for real instants, civil dates/local schedules, IANA timezone identities, and historical instant handling. ADR-017 does not create a separate timezone rule.

Credential issue or expiry values, engagement effective periods, eligibility effective periods, status changes, and verification times must use types matching their business semantics. A legal issue/expiry date may be a civil date where that is the governing meaning; a transition, verification occurrence, or resolved effective moment is a real instant. Real instants follow ADR-011. Current changes must not rewrite historical resolved instants.

Proposed ADR-010 remains authoritative for the UUIDv7 direction. New entities use the repository-approved identifier architecture when physical persistence is later designed. This ADR introduces no new identifier strategy and does not finalize entity identifiers.

### 25. Modular-Monolith Boundary

This design remains inside the ADR-001 modular monolith. It does not introduce a professional, credential, eligibility, or identity microservice, a distributed transaction, or a separate database.

Role-specific professional, identity/security, Organization, and professional-eligibility modules or capabilities communicate through explicit internal application interfaces. Transactional-outbox events are used selectively where asynchronous decoupling is justified; simple local coordination remains in process.

No universal metadata framework is introduced. Core professional identity, credentials, typed role association, Organization authorization, and eligibility relationships must remain explicit. JSONB may be considered later only for genuinely provider-specific or extensible non-relational metadata, never as a substitute for core ownership or referential integrity.

## Detailed Invariants

1. An authenticated principal is not the canonical professional performer identity.
2. External Auth0/OIDC `sub` is not the canonical Chef or Dietitian business identifier.
3. One authenticated account may be linked to multiple separately authorized platform roles and role-specific identities where policy permits.
4. Chef and Dietitian identities remain role-specific; no universal Professional or Provider aggregate is required.
5. A practicing Dietitian is identified by `DietitianProfessionalProfile`, not by the Organization or its administrator.
6. A `ChefOrderGroup` identifies one durable actual Chef performer under ADR-013; it is not grouped by a shared Organization engagement or payee.
7. Organization administrative authority does not grant Chef or Dietitian performer authority.
8. A professional may have multiple concurrent or sequential Organization engagements where policy permits.
9. A professional identity does not require a current external-Organization engagement.
10. Cheffy Operations uses the ordinary Organization and professional-engagement model with no special performer type or branch.
11. Only an effective `ACTIVE` engagement authorizes new professional service through that Organization.
12. Engagement suspension or ending blocks new authorization while preserving historical attribution.
13. Ordinary Organization administration cannot silently impersonate or activate a real professional.
14. Current Organization membership is not the source of truth for historical service attribution.
15. Submitted, self-attested, verified, rejected, expired, and revoked credential concepts must be represented truthfully and must not be conflated.
16. `PLATFORM_VERIFIED` requires an actual defined verification by Cheffy or an approved verifier.
17. A verified credential is not by itself current jurisdiction eligibility.
18. Eligibility is professional-role-, service-, jurisdiction-, policy-, and time-aware.
19. Architecture records approved legal/policy decisions; it does not invent legal eligibility.
20. Private credential evidence is not public-profile data and is not automatically visible to Organization administrators.
21. Professional Organization authorization does not determine commercial provider, settlement beneficiary, Promotion ownership/funding, or marketplace economics.
22. Professional Organization engagement is not payroll or an employment-compensation aggregate.
23. Historical service records retain enough durable evidence to explain performer, applicable Organization context, and authorization/eligibility decision.
24. Core professional relationships use typed referential integrity rather than arbitrary `type + UUID` or metadata links.

## Out of Scope

This ADR explicitly does not decide:

- payroll, employee compensation, wages, benefits, taxes, or worker-remuneration calculations;
- commercial earning recognition, commercial-provider obligations, or settlement-beneficiary rules;
- payout eligibility or grouping;
- Payment or Refund orchestration;
- ledger posting or reconciliation;
- Promotion targeting or evaluation;
- Dietitian Appointment scheduling, availability, holds, overlap protection, rescheduling, cancellation/no-show, or concurrency;
- online meeting provisioning or calendar integration;
- subscription lifecycle or fulfillment-occurrence architecture;
- Conversation, Participant, Message, or chat architecture;
- taxonomy architecture;
- review or reputation calculation;
- applicant-tracking-system/recruiting implementation;
- EMR or clinical-record architecture;
- diagnoses, medication data, laboratory results, or clinical notes;
- detailed legal interpretation for any jurisdiction;
- final persistence tables, columns, foreign keys, indexes, or constraints;
- final API endpoints or representations; or
- final event names, schemas, or payloads.

## Consequences

### Positive

- The actual Chef or Dietitian performer remains traceable and accountable.
- Organization-operated supply works without collapsing individual performer identity.
- Independent and Organization-associated professionals use the same architecture.
- Cheffy-operated bootstrap supply requires no special professional identity and can later be removed without redesign.
- Multiple Organization contexts and effective-dated authorization are supported.
- Credential evidence remains private and least-privilege access can be enforced.
- Submitted, self-attested, and actually verified claims remain truthfully distinguishable.
- Jurisdiction and service rules can evolve through explicit effective-dated eligibility policy.
- Completed professional services remain historically explainable after profile, credential, eligibility, or Organization changes.
- ADR-013 can reference the actual Chef performer without treating a common employer or payee as the Chef.
- Future ADR-020 can select commercial provider and settlement beneficiary independently of professional authorization.
- Future verified-experience reputation can attach to the real performer.

### Negative / Costs

- More explicit identity relationships and authorization checks are required.
- Effective-dated engagement and eligibility history add lifecycle and audit complexity.
- Credential submission, verification, expiry, revocation, evidence access, and review require operational processes.
- Service workflows must perform role-, service-, jurisdiction-, time-, and Organization-aware eligibility checks.
- Sensitive evidence requires stronger privacy, authorization, object-storage, audit, and retention controls.
- The later canonical ERD must provide typed relationships across role-specific professional identities without relying on an unconstrained polymorphic reference.
- Historical authorization/eligibility evidence requires deliberate reference or snapshot design rather than convenient current-state joins.
- Jurisdiction launch requires legal and policy decisions that architecture alone cannot supply.

## Alternatives Considered / Rejected

### 1. Organization Is the Professional Identity

Rejected because it erases the actual performer, individual professional accountability, ChefOrderGroup separation, verified-experience eligibility, and professional reputation. A clinic cannot be the practicing Dietitian, and a multi-Chef Organization cannot be the actual Chef for every OrderItem.

### 2. Auth0 Account ID Is the Professional Domain Identity

Rejected because authentication and professional business identity have different lifecycles, one account may hold multiple roles, identity-provider linkage may change, and external `sub` must not define historical service identity.

### 3. One Universal Professional or Provider Aggregate for Every Role

Rejected because Chef and Dietitian have different invariants and lifecycles. A universal aggregate encourages false assumptions that professional performer, commercial provider, settlement beneficiary, payee, worker, and subscription participant are one party.

### 4. Current Organization Membership Is Sufficient for Historical Attribution

Rejected because membership and engagement change. Historical service would become incorrect or unexplained when a professional leaves, joins another Organization, or operates independently.

### 5. Uploaded Credential Means Verified

Rejected because submission, self-attestation, evidence review, and actual verification are different claims. Treating upload as verification is misleading and may create legal, professional, and Customer-trust risk.

### 6. Store Private Credential Documents in Public Profile Data

Rejected because credential evidence is sensitive, requires restricted object access and audit, and must not expose raw documents, object keys, or supporting identity evidence to Customers.

### 7. Organization Administrator Automatically Becomes Authorized Professional

Rejected because authority to manage Organization resources does not establish Chef operational identity, Dietitian qualification, professional consent, credential satisfaction, jurisdiction eligibility, or service authorization.

### 8. Professional Organization Engagement Determines Settlement Beneficiary

Rejected because authorization to perform through an Organization is not a commercial-provider or Financial arrangement. ADR-020 must determine commercial obligations and settlement beneficiaries independently.

### 9. Generic Professional Type Plus Arbitrary UUID for All Roles

Rejected as the final relational model because the database cannot enforce that an ID belongs to the declared role-specific entity, prevent dangling or cross-type references, or preserve strong core-domain referential integrity.

### 10. Build Payroll or HR into Professional Authorization

Rejected because marketplace professional authorization and worker compensation are separate concerns. Employment classification, salary, wages, withholding, benefits, bonuses, contractor invoices, and payroll remittance do not belong to this decision.

### 11. Require Exactly One Organization Per Professional

Rejected because qualified professionals may operate through multiple Organizations over time or concurrently, and may practice independently where policy permits. Current Organization context is not immutable professional identity.

### 12. Create a Special Cheffy Performer or Internal-Provider Type

Rejected because Cheffy Operations is an ordinary Organization participant for temporary bootstrap supply. Special identity or provider branches would make later exit require redesign and would create inconsistent authorization semantics.

### 13. Treat Credential Verification as Universal Practice Eligibility

Rejected because current eligibility depends on role, service, jurisdiction, validity period, regulatory and Platform policy, and applicable Organization authorization. A credential can be genuinely verified yet insufficient, expired, suspended, or irrelevant to a particular service context.

### 14. Copy Recruiting Data Directly into a Marketplace Profile

Rejected because candidate data has a distinct purpose, access boundary, and consent context. Hiring or application submission does not create a public profile or active professional authorization.

### 15. Store Core Professional Relationships in Generic Metadata / JSON

Rejected because identity, credential ownership, Organization authorization, and jurisdiction eligibility are authoritative business relationships that require explicit semantics and typed integrity. Flexible metadata cannot replace them.

## Dependencies and Related ADRs

- **ADR-001 — Modular Monolith First:** This design remains within one deployable modular monolith and uses explicit module interfaces plus selective outbox integration.
- **ADR-006 — Promotion Targeting Model:** Promotion owner and target identity remain separate from professional Organization authorization.
- **ADR-007 — Booking Concurrency Control:** KitchenBooking and EquipmentRental concurrency remain unchanged and are not generalized to professional Appointment scheduling.
- **ADR-010 — UUIDv7 Identifier Strategy:** Existing identifier direction applies when later persistence is designed; this ADR does not change its Proposed status.
- **ADR-011 — Timezone Modeling:** Existing temporal/timezone direction applies to effective periods and historical instants; this ADR does not change its Proposed status.
- **ADR-012 — Payment Marketplace Settlement:** Payment/refund orchestration and performer/provider/beneficiary separation remain outside this ADR; this ADR does not change its Proposed status.
- **ADR-013 — ChefOrderGroup Aggregate + Financial Boundary:** `ChefOrderGroup` continues to represent one actual Chef performer inside one concrete Order. ADR-017 supplies the durable professional and Organization-authorization semantics without changing ADR-013's Proposed status or aggregate boundary.
- **ADR-014 — Promotion Engine:** Promotion evaluation, compatibility, benefit, and funding remain outside this ADR; this ADR does not change its Proposed status.
- **ADR-015 — Financial Ledger and Reconciliation:** Ledger ownership, posting, and reconciliation remain outside this ADR; this ADR does not change its Proposed status.
- **ADR-016 — Event Versioning:** Future professional-authorization events must follow its Accepted versioning rules. This ADR does not change ADR-016's status.

No dependency status is changed by this Proposed ADR.

## Future ADR Dependencies

ADR-017 is intended to be an input to:

- **ADR-018 — Dietitian Engagement, Appointment Scheduling and Online Meeting Provisioning:** references Dietitian performer identity, credentials, jurisdiction eligibility, and Organization authorization while owning scheduling and Appointment lifecycle.
- **ADR-020 — Commercial Obligations, Earning Recognition and Payable-Source Financial Model:** may reference professional/Organization authorization context while independently owning commercial provider, settlement beneficiary, earning recognition, obligations, and payout eligibility.
- **ADR-021 — Authorized Multi-Context Conversation Architecture:** may use professional and Organization authorization as inputs while owning conversation participation and access.
- **ADR-023 — Verified-Experience Reviews and Reputation:** uses durable actual-performer identity and completed-service evidence while owning reputation and review aggregation.

ADR-019 may reference Organization and professional identities where needed but remains focused on subscription lifecycle and fulfillment occurrences.

## Implementation and Propagation Notes

This Proposed ADR does not authorize application code, migrations, or contract changes by itself. After approval, implementation planning must:

1. Reconcile the canonical ERD with typed role-specific professional, credential, eligibility, and Organization-engagement relationships.
2. Select the typed durable Chef performer reference required by ADR-013 without using a common Organization/provider/payee identity.
3. Preserve the canonical `DietitianProfessionalProfile` direction without making Organization the practitioner.
4. Define credential and private-evidence authorization, audit, object-storage, malware-scanning, retention, and disclosure policy before accepting sensitive uploads.
5. Define approved legal/policy requirements per launch jurisdiction before creating eligibility rules.
6. Define service-boundary decision interfaces for Chef Order and future Dietitian Appointment workflows.
7. Define durable historical authorization/eligibility evidence without recomputing completed service from current state.
8. Update the canonical API contract only when exact request/response representations are approved.
9. Update the canonical event contract only when exact event semantics and payloads are approved, following ADR-016.
10. Add authorization, privacy, audit, lifecycle, concurrency, and historical-attribution tests appropriate to the final implementation.

The later ERD and contract work must not introduce an unconstrained `professional_type + professional_id`, universal Provider/Payable/Worker aggregate, public credential-evidence exposure, Cheffy-specific performer branch, or implicit coupling between Organization engagement and commercial settlement.
