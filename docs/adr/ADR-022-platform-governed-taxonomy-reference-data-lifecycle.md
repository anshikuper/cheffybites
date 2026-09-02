# ADR-022: Platform-Governed Taxonomy and Reference-Data Lifecycle

## Status

Proposed

## Context

Cheffy Bites requires shared, stable vocabulary for marketplace discovery, filtering, Promotion targeting, localization, analytics, and cross-application consistency. Applicable examples include food categories, cuisine, meal/course/type classifications, dietary classifications, discoverability classifications, Chef specialties, Dietitian professional specialties, and selected Kitchen or equipment reference classifications. Additional Platform-owned reference families may be approved when shared governance is genuinely required.

Without a governed reference-data lifecycle, equivalent concepts can fragment across spelling, capitalization, language, provider terminology, and free text. A label such as "veggie" could become disconnected from the approved semantic concept `VEGETARIAN`; a renamed or retired category could make historical Orders or Promotion evidence difficult to explain; and independently maintained web, mobile, search, and analytics labels could acquire inconsistent meaning.

Governance must not become a universal metadata system. Classification is not a substitute for domain modeling, state machines, configuration, authorization, safety evidence, credentials, Financial facts, or physical inventory. Families that happen to have labels do not necessarily share hierarchy, assignment, verification, policy, or lifecycle semantics.

The architecture must distinguish the following concepts:

- **Canonical reference value:** a Platform-governed semantic identity in one typed reference family, approved for the uses allowed by its lifecycle and owning-domain policy.
- **Free text:** unstructured user, provider, administrator, or system text that is not globally authoritative reference data.
- **Search alias:** an approved input or discoverability term that maps to a canonical value without becoming a separate business identity.
- **User/provider suggestion:** a proposed missing term, mapping, translation, or canonical concept awaiting governed disposition.
- **AI classification suggestion:** model- or process-produced advice about classification, mapping, duplication, or localization; it is not a governance decision.
- **Historical business snapshot:** transaction-time evidence of the identity, meaning, display, assignment, or decision relevant to a durable business fact. It is not the mutable current reference record.

The foundational principle is:

```text
TAXONOMY
!= ARBITRARY DOMAIN DATA MODEL
```

This ADR establishes conceptual ownership, identity, lifecycle, governance, and integration boundaries. It deliberately does not finalize tables, columns, foreign keys, indexes, constraints, SQL, REST endpoints, OpenAPI schemas, event names, event payloads, localization storage, or user-interface behavior.

## Decision

Cheffy Bites will maintain Platform-governed, typed, domain-aware reference families inside the modular monolith, with PostgreSQL as the authoritative store. Shared governance principles may be reused, but each family retains its semantic meaning, valid relationships, assignment rules, and domain ownership.

Canonical reference values provide stable semantic identities independent of mutable labels, translations, aliases, and URL slugs. Their lifecycle supports governed proposal, review, activation, rejection, retirement, and replacement where appropriate. Retired values remain available to explain durable historical references and are normally unavailable for new assignment.

The Platform may accept suggestions from authorized users and providers and may use AI to assist classification and governance. Neither a suggestion nor AI output becomes globally canonical without an explicit governed policy and an authorized lifecycle transition.

Owning business domains retain authority over assignment and consequences. Reference data identifies or classifies; it does not decide all policy associated with a classification.

## Required Invariants

1. `CANONICAL REFERENCE VALUE != FREE TEXT`.
2. `CANONICAL REFERENCE VALUE != ALIAS`.
3. `CANONICAL REFERENCE VALUE != SUGGESTION`.
4. `AI SUGGESTION != CANONICAL GOVERNANCE DECISION`.
5. `CANONICAL SEMANTIC IDENTITY != DISPLAY LABEL`.
6. `CANONICAL SEMANTIC IDENTITY != URL SLUG`.
7. `RETIRED != DELETED FROM HISTORY`.
8. `REFERENCE VALUE VALID != DOMAIN ASSIGNMENT VALID`.
9. `DIETARY CLASSIFICATION != ALLERGEN SAFETY GUARANTEE`.
10. `SPECIALTY REFERENCE VALUE != PROFESSIONAL CREDENTIAL`.
11. `SPECIALTY REFERENCE VALUE != LICENSE / JURISDICTION ELIGIBILITY`.
12. `REFERENCE DATA != BUSINESS CONFIGURATION`.
13. `REFERENCE DATA != DOMAIN STATE MACHINE`.
14. `SEARCH INDEX != REFERENCE-DATA SOURCE OF TRUTH`.
15. `REDIS != REFERENCE-DATA SOURCE OF TRUTH`.
16. `TAXONOMY != AUTHORIZATION`.
17. `TAXONOMY != FINANCIAL FACT`.
18. `TAXONOMY != RATING / REPUTATION`.
19. `CONVERSATION FREE TEXT != CANONICAL TAXONOMY`.
20. `REFERENCE LABEL != VERIFIED CREDENTIAL / CERTIFICATION`.
21. `NUTRITION MEASUREMENT != TAXONOMY VALUE`.
22. `REFERENCE CATEGORY != PHYSICAL INVENTORY / BOOKING CAPACITY`.
23. `SEARCH QUERY INTERPRETATION != TAXONOMY MUTATION`.
24. `HISTORICAL BUSINESS SNAPSHOT != CURRENT REFERENCE VALUE`.

## Reference Families and Ownership

### Typed Reference Families

Every canonical reference value belongs to an explicit semantic family. Families may include, where approved:

- FoodCategory;
- Cuisine;
- DietaryClassification;
- MealType, CourseType, PreparationMethod, or another approved food/discovery family;
- ChefSpecialty;
- ProfessionalSpecialty, including Dietitian specialty values used for profile, discovery, or offering classification;
- EquipmentCategory;
- KitchenAmenity or FacilityCapability classification; and
- another specifically approved Platform-owned reference family.

Cuisine, DietaryClassification, FoodCategory, and ProfessionalSpecialty may reuse governance capabilities while remaining distinct concepts. They are not collapsed merely because each has a label, alias, status, or localized display.

The architecture does not approve one universal `TaxonomyValue` aggregate or table that owns all classifications across Food, Chef, Dietitian, Kitchen, Equipment, Promotion, Conversation, Order, Financial, and Review domains. It also does not approve an unrestricted universal ReferenceRegistry.

Canonical relationships must preserve domain-valid semantics and enforceable relational integrity where practical. An unconstrained combination such as:

```text
reference_type
reference_id UUID
```

is not the default canonical relationship when it would erase meaningful relational integrity. The canonical ERD may define typed family records and typed assignment relationships, or an equivalently strong domain-aware model. It must not use a giant nullable-foreign-key assignment design or generic polymorphic metadata as an escape hatch.

### Vocabulary Ownership Versus Assignment Ownership

ADR-022 owns the governance principles for canonical shared vocabulary. The business domain that owns an entity owns whether and how that entity may use a value:

- Catalog/Food owns classification assignment to Master Food, FoodListing, ChefMealPlan, or other food resources under its rules.
- Professional-profile domains own specialty assignment to Chef or Dietitian profiles.
- Kitchen and Equipment domains own assignment of approved reference classifications to their resources.
- Promotion owns target configuration and evaluation boundaries under ADR-006 and ADR-014.
- Search owns query interpretation and index projection, not canonical taxonomy.

An `ACTIVE` reference value is eligible for consideration; it does not prove that any requested assignment is truthful, authorized, safe, or valid. Assignment validation may include ownership, allowed family, cardinality, evidence, moderation, and family-specific policy.

The taxonomy/reference capability does not own the lifecycle of every Food, Chef, Dietitian, Kitchen, Equipment, Promotion, Conversation, Order, Financial, Review, or other domain aggregate.

### No Generic Metadata Escape Hatch

Canonical reference identity and assignments must not be implemented through generic `entity_type`, `entity_id`, `metadata JSON`, or `attributes JSON` relationships. JSONB may be used only for genuinely extensible non-relational metadata where justified; it must not replace canonical typed identity, ownership, assignment, or referential integrity.

## Canonical Identity

### Stable Semantic Identity

A canonical value conceptually has:

- stable identity governed by ADR-010;
- one explicit reference family;
- canonical semantic meaning;
- lifecycle/status;
- a machine-safe stable code where appropriate;
- default display label;
- localized displays where supported;
- aliases or synonyms where appropriate;
- creation and governance history; and
- retirement, replacement, or supersession metadata where appropriate.

This is a conceptual record, not an exact column specification.

Stable identity must be independent of mutable display labels, localized text, aliases, and SEO slugs. A stable machine code, where a family uses one, represents semantic identity and is not generated at runtime from a mutable translated label. Display edits do not silently change API or business identity. Neither URL slug nor display text is required to be canonical database identity or a foreign key.

### Semantic Identity Versus Display

Canonical semantic identity remains constant through ordinary display changes. For example:

```text
canonical semantic concept: VEGETARIAN

English display: Vegetarian
French display: Végétarien
```

Capitalization, corrected spelling, improved wording, or translation does not necessarily create a new semantic identity. A language-specific display is a representation of one identity, not a duplicate canonical value.

### Material Semantic Change

A materially incompatible change in business meaning must not silently repurpose an existing canonical identity. The governing family must create a new semantic value, semantic version, or governed replacement according to its approved model and preserve the old meaning for historical explanation.

For example, changing a display from "Vegetarian" to an equivalent preferred translation is a display change. Redefining the concept to include a materially different set of foods is a semantic change requiring explicit governance and historical treatment. Exact family-specific semantic versioning representation is deferred to ERD and implementation design.

### External Vocabulary Mapping

Cheffy may map a canonical value to an external standard or curated vocabulary. Where useful, the mapping preserves the external source and version. An external identifier does not automatically become Cheffy's permanent internal business identity, and runtime correctness must not depend on an external service unless separately approved.

External vocabulary changes must not silently repurpose Cheffy's historical meaning. Mappings may evolve, retire, or be versioned while Cheffy's durable canonical and historical references remain explainable.

## Lifecycle / Governance

### Conceptual Lifecycle

Reference-family lifecycle must distinguish at least:

- not approved for canonical use;
- active and approved for allowed current use;
- rejected from canonical promotion;
- no longer available for ordinary new assignment; and
- historically retained.

A conceptual workflow may use meanings equivalent to:

```text
DRAFT / PROPOSED
    ↓
UNDER_REVIEW
    ├── ACTIVE
    └── REJECTED

ACTIVE
    ↓
RETIRED
```

Families do not have to use identical physical enum values or every intermediate state. A suggestion lifecycle may also differ from a canonical-value lifecycle. The minimum meanings above must remain distinguishable.

### Governance Responsibilities

Platform reference-data governance is responsible for:

- approving or rejecting proposed canonical values;
- preventing unintended duplicates and near-duplicates;
- deciding whether proposals represent a new concept, alias, localization, or existing value;
- managing default and localized displays;
- managing aliases and synonyms;
- controlling semantic changes;
- retiring, replacing, superseding, or merging values;
- reviewing provider/user suggestions;
- reviewing AI suggestions where policy requires it; and
- preserving sufficient governance and historical evidence.

Governance may include automated validation, normalization, duplicate detection, low-risk policy automation, and batch administration. It does not require every edit to be performed manually forever. Automation remains controlled by explicit policy and authorization.

### Privileged Authorization

Canonical taxonomy administration is privileged. Ordinary Chef, Dietitian, Organization, or Customer capabilities do not include direct promotion of globally canonical values through normal profile, listing, request, or Conversation APIs. Every governance mutation requires authenticated role/capability checks and least privilege. Exact Platform administration roles and authorization mechanics remain later implementation work.

### Duplicate Prevention and Concurrency

The architecture must support detecting duplicate and near-duplicate proposals such as "South Indian", "south-indian", and "South-Indian Cuisine". Case folding, punctuation handling, diacritic-aware matching, locale-aware normalization, search similarity, and AI may assist review, but normalized text alone does not define semantic identity. Textually similar concepts must not be merged when their meanings differ.

Concurrent proposals or administrative approvals must not create duplicate canonical identities for the same intended semantic value. Appropriate transactions, uniqueness rules, database constraints, and conflict handling are required; user-interface duplicate checks alone are insufficient. Exact keys and constraints belong to the canonical ERD.

### Idempotency

Proposal submission, approval, activation, rejection, retirement, replacement, assignment, and asynchronous propagation must support idempotent processing where retry could otherwise duplicate values, assignments, lifecycle actions, audit evidence, or emitted integration intent. Exact idempotency-key representation and uniqueness rules are deferred to implementation and ERD work.

## Aliases / Synonyms

Aliases and synonyms are approved discoverability or input aids that map user terms to canonical semantic meaning. For example:

```text
"veggie"
"vegetarian food"
    ↓
VEGETARIAN
```

An alias is not a separate canonical business identity merely because users search for it. Aliases may be locale-aware, may have their own lifecycle or moderation state where needed, and may support search, normalization, import, and duplicate review.

Changing, adding, or retiring an alias does not change the identity of its canonical value. Alias mapping must not silently merge semantically distinct concepts simply because normalized text is similar.

Free-text search terms may resemble aliases without becoming governed aliases. An observed query becomes an alias only through an approved process or explicit governed automation policy.

## Localization

Canonical identity is independent of language. One canonical value may have localized:

- display name;
- description where needed; and
- aliases or synonyms.

A translated label does not create a second semantic identity. Localization changes do not rewrite the canonical identity or durable historical references. Default locale and fallback behavior must be explicit in later API and UI contracts so a missing translation does not create an ambiguous new value.

Input normalization and rendering must preserve legitimate localized distinctions, diacritics, script, and meaning. Lowercased user text is not canonical identity. Exact localization tables, fields, fallback order, translation workflow, and API representation remain outside this ADR.

## Suggestions / Proposals

Chefs, Dietitians, Organizations, Customers, administrators, import processes, and other approved actors may propose missing values, aliases, mappings, or translations where product policy allows. A suggestion remains separate from canonical reference data.

A suggestion may be:

- reviewed;
- normalized for comparison without losing original input;
- mapped or merged with an existing canonical value;
- approved as a new canonical semantic value;
- approved only as an alias or localized display;
- rejected;
- retained as local/free-text input where that domain permits; or
- retained as historical suggestion evidence according to policy.

Approval must identify the intended typed family and semantic meaning. A provider cannot create globally visible filter values merely by submitting a listing or profile. A rejected or merged suggestion is not silently treated as an active canonical value.

Suggestions are untrusted input. Labels, descriptions, translations, and source metadata require validation, safe rendering, length/content controls, and authorization. Suggestions must not be used to store private medical information, credential evidence, chat content, or other sensitive user data.

## AI-Assisted Classification

AI may assist with:

- suggesting likely canonical classifications for free text or structured resources;
- mapping search or provider text to existing canonical values;
- identifying aliases and synonyms;
- identifying duplicate or near-duplicate proposals;
- recommending localized text;
- quality and moderation checks; and
- routing ambiguous proposals for review.

AI output is advisory unless an explicit low-risk assignment policy authorizes automatic assignment. Model output never becomes the source of truth and must not silently create, activate, promote, retire, merge, or repurpose globally canonical values.

Where AI-assisted classification is used, the later architecture should permit provenance appropriate to risk, including source model or process, confidence, proposed canonical mapping, and human/system approval status where required. Provider-specific model schema, confidence thresholds, and model selection are deferred.

Future policy may permit automatic assignment of an existing canonical value above configured confidence and quality thresholds for low-risk uses. Such assignment remains subject to owning-domain validation, monitoring, correction, and audit where material.

```text
AUTOMATIC CLASSIFICATION ASSIGNMENT
!= AUTOMATIC CANONICAL VALUE CREATION
```

Ambiguous, safety-relevant, regulated, credential-related, or materially commercial classifications require the level of review defined by owning policy. AI cannot convert a dietary label into allergen evidence, a specialty into licensure, or a claim into certification.

## Food / Cuisine / Dietary Classification

### Distinct Food Reference Families

Platform-governed food vocabulary may support:

- FoodCategory hierarchy where needed;
- Cuisine;
- MealType, CourseType, PreparationMethod, or other approved type families;
- DietaryClassification; and
- discovery/filter classifications.

These families do not automatically share hierarchy, cardinality, lifecycle, evidence, or assignment rules. Cuisine may behave differently from FoodCategory, and DietaryClassification may behave differently from both.

A Food, Master Food, or FoodListing may legitimately have multiple applicable typed classifications, for example:

```text
Cuisine = INDIAN
DietaryClassification = VEGETARIAN
MealType = DINNER
FoodCategory or discovery classification = STREET_FOOD
```

This does not create one universal multi-label category. Each assignment belongs to its typed family and is validated by the owning Catalog/Food policy.

### Food Category Hierarchy

Where FoodCategory requires parent/child organization, the canonical model supports explicit hierarchical semantics. It must prevent self-reference and invalid recursive cycles through appropriate relational and transactional integrity. Hierarchy path text is not canonical identity.

This ADR does not require unlimited depth. Exact depth, whether multiple parents are supported, ordering, path derivation, constraints, and recursive query strategy remain canonical ERD and product work.

### Cuisine

Cuisine is a governed semantic vocabulary where it supports discovery, filtering, targeting, or other approved business logic. Canonical Cuisine assignment must be explicit or performed through an approved classification process. It must not be inferred solely from Chef nationality, Organization country, Kitchen location, or free-text description.

Nationality, geography, cuisine, style, and provider identity may correlate but are not interchangeable facts. Search may propose a cuisine mapping from text; that interpretation neither mutates taxonomy nor proves the assignment valid.

### Dietary Classification

Dietary classifications such as `VEGETARIAN`, `VEGAN`, and other approved product concepts are governed reference values. The family remains extensible rather than hard-coded to only two labels.

A dietary classification is not an allergen safety guarantee, medical conclusion, certification, or legal assurance. It does not by itself establish ingredient completeness, cross-contamination controls, facility practices, or suitability for a particular person.

### Ingredient and Allergen Boundary

Ingredient and allergen information is safety-relevant structured product data. Allergen handling must not be reduced to casual tags. If canonical allergen reference values are used, their assignments and displays must preserve structured ingredient/allergen evidence, provenance, applicable product controls, and legal requirements.

Taxonomy classification must not claim medical or legal safety certification. Exact allergen evidence, cross-contact, disclosure, verification, and compliance requirements remain explicit product, safety, and legal work.

### Nutrition Boundary

Nutrition values and measurements are structured food facts, not taxonomy values. Calories, protein grams, serving size, and similar quantities do not become generic tags merely because discovery filters use them. Nutrition facets may use derived ranges or approved reference labels for search, but authoritative measurements, units, serving basis, and provenance remain structured nutrition data.

### Claims and Certification Boundary

A reference label does not itself prove halal certification, kosher certification, organic certification, a verified medical claim, or regulatory compliance. Where certification or evidence is required, the appropriate credential, verification, claim, safety, or compliance capability owns that proof and provenance.

## Professional Specialty Boundary

### Chef Specialty

Chef specialty values may support profile classification, discovery, search, and matching. A Chef may be associated with approved cuisine or culinary specialty values according to profile policy.

Chef specialty is not a professional credential, certification, legal authorization, verified competence, Organization membership, or commercial-provider authority. Selecting "Indian cuisine" does not create verified certification or establish the truth of every Food assignment.

### Dietitian Specialty

ProfessionalSpecialty may support Dietitian profile discovery, matching, or Appointment/ConsultationOffering classification. ADR-017 remains authoritative for durable professional identity, credentials, verification, licenses, Organization authorization, and jurisdiction eligibility.

```text
SPECIALTY REFERENCE VALUE
!= CREDENTIAL
!= LICENSE
!= JURISDICTION ELIGIBILITY
```

A specialty cannot determine whether a Dietitian is legally permitted to practise or deliver a service in a jurisdiction. The professional and Appointment domains must evaluate current credentials, authorization, service policy, and jurisdiction eligibility independently.

ADR-018 remains authoritative for DietitianClientEngagement, Appointment scheduling, ConsultationOffering, and online-meeting provisioning. A specialty may classify a profile or offering but does not create an engagement, confirm an Appointment, or alter Appointment state.

### DietitianMealPlan Boundary

DietitianMealPlan may reference approved dietary or food vocabulary where useful, subject to Customer authorization and privacy. ADR-022 does not redesign DietitianMealPlan. It does not assign a Chef, select a seller, create food-sale attribution, create Promotion attribution, or create a Dietitian commission or Financial claim.

Private diagnoses, medical notes, private meal-plan content, customer health details, and credential evidence must not be copied into canonical taxonomy. Customer-authorized marketplace requirements remain distinct from the private professional record.

## Kitchen / Equipment Reference Boundary

Kitchen or Equipment domains may use shared controlled vocabularies where Platform-wide meaning is valuable, including:

- equipment category;
- amenity or capability classification; and
- approved facility feature.

An EquipmentCategory or `EquipmentCatalogItem` can describe standardized equipment meaning. It is not the concrete equipment inventory, included Space equipment, rentable equipment offering, authoritative quantity, condition, availability, price, or booking capacity.

```text
REFERENCE CATEGORY
!= PHYSICAL EQUIPMENT INVENTORY
!= BOOKING CAPACITY
```

ADR-007 remains authoritative for Space and Equipment occupancy, reservable resources, capacity, holds, cleaning intervals, locking, and booking concurrency. A Kitchen capability tag does not reserve capacity or grant booking authorization. Taxonomy must not turn concrete Equipment inventory into reference records.

## Promotion / Search / Analytics Integration

### Promotion Targeting and Evaluation

Promotion may reference approved canonical categories or other typed reference values when the applicable commercial domain permits that target. Stable identity prevents display rename, localization, or alias changes from altering target identity.

ADR-006 remains authoritative for Promotion targeting structure, typed target semantics, ownership constraints, and target referential integrity. ADR-014 remains authoritative for evaluation, eligibility, calculation scope, compatibility, exclusivity, priority, application, redemption, snapshot, repricing, and refund behavior.

```text
TAXONOMY IDENTITY
!= PROMOTION ELIGIBILITY DECISION
```

An active category does not make every Food eligible for a Promotion, and ADR-022 does not evaluate Promotion rules.

Historical Promotion evidence must preserve the canonical identity and sufficient transaction-time meaning used when evaluation occurred. A rename, alias change, replacement, merge, or retirement must not make an application or redemption inexplicable. Historical results must not be recomputed from current taxonomy.

Taxonomy may influence an explicitly supported Pricing or Promotion rule, but it does not own price. PricingSnapshot remains canonical historical pricing evidence. This ADR does not create a `TaxonomyPricingSnapshot` or replace Promotion snapshots.

### Search and Discovery

Search may use canonical values, localized displays, approved aliases, synonyms, and mappings. User query text may be normalized and interpreted, for example:

```text
"veggie Indian meals"
    ↓
DietaryClassification = VEGETARIAN
Cuisine = INDIAN
```

Query interpretation does not mutate taxonomy or make the inferred assignments authoritative. Search results remain subject to owning-domain assignment validity and visibility policy.

PostgreSQL remains the reference-data source of truth. A search index is a derived projection that may be temporarily stale and must be rebuildable from authoritative data. This ADR does not introduce OpenSearch or Elasticsearch solely for taxonomy; a future search dependency requires demonstrated need and an approved architecture decision.

Human-readable slugs may support URLs and SEO but are representations, not canonical identity. Slug changes must not require replacement of canonical database identity. Exact slug uniqueness, redirects, locale handling, and lifecycle remain later API/web work.

### Analytics

Stable canonical identity supports aggregation across label, localization, alias, and ordinary display changes. Historical analytics must remain interpretable after rename, retirement, replacement, or merge.

When values are superseded or merged, analytics may provide two explicit views:

- historical-original reporting based on the identities and meanings recorded at the time; and
- current-normalized reporting that maps historical identities to a current preferred concept.

Current normalization is a derived analytical choice and must not overwrite historical source facts. Reports must make the selected interpretation explicit where the distinction is material.

## Historical Stability / Retirement

### Retirement

Retiring a canonical value normally means:

- it is unavailable for ordinary new assignment;
- it may be omitted from ordinary creation or current-filter UI where appropriate;
- its identity, meaning, aliases, localization, and governance evidence remain retained as required; and
- durable historical references remain valid and explainable.

Existing Orders, OrderItems, Promotion applications/redemptions, Reviews, listings, assignments, snapshots, analytics evidence, and Financial records are not rewritten merely because current terminology changed.

Hard deletion of a canonical value already referenced by durable business history should generally be prohibited or tightly controlled. Retirement, tombstoning, or replacement is preferred. Physical deletion may remain possible for never-published erroneous drafts when no durable reference, audit, event, cache, search, or external dependency would be corrupted. Exact deletion rules belong to persistence and governance policy.

### Replacement, Supersession, and Merge

A governed lifecycle may record that old canonical value A is superseded or replaced by preferred current value B. Historical records may continue to reference A. Search and current discovery may optionally map A or its aliases to B under explicit policy.

Replacement does not silently rewrite historical identity. If a migration of current mutable assignments is desirable, it requires an explicit family/domain policy, authorization, validation, audit, idempotency, and impact review. Commercial, regulatory, safety, Promotion, Order, Review, and Financial evidence must not be bulk-reinterpreted by default.

### Assignment History

The architecture preserves assignment history or transaction-time snapshots where classification materially affects:

- a commercial decision or price;
- Promotion qualification or targeting;
- regulatory, safety, or claim behavior;
- historical user experience or explanation;
- verified-experience eligibility in future review work; or
- analytics interpretation.

This ADR does not require immutable history for every low-risk profile or discovery assignment. The owning domain decides the necessary evidence based on materiality. Historical Orders and OrderItems must remain explainable even when current classifications change, but the exact snapshot/reference strategy is canonical ERD and owning-domain work.

### Historical Business Snapshot Distinction

A historical business snapshot captures transaction-time evidence and is not canonical vocabulary administration. Updating current display or lifecycle state must not rewrite snapshot meaning. Conversely, a snapshot does not become an active canonical value merely because an old record displays it.

Taxonomy changes must not retroactively rewrite Payment, CommercialObligation, Earning, Refund, Payout, Ledger, or other Financial history. ADR-020 remains authoritative for commercial obligations, earning recognition, payable source, and Financial boundaries.

## Reference Data Versus Other Concerns

### Reference Data Versus Configuration

Reference data identifies shared concepts. Fee percentages, grace durations, cancellation windows, operational limits, provider credentials, secrets, and feature flags are business or operational configuration, not taxonomy merely because they are configurable.

### Reference Data Versus State Machines

Stable lifecycle models such as Order, Payment, Appointment, Subscription, Conversation, and booking states remain owning-domain state machines. They are not automatically replaced by administrator-editable taxonomy values.

### Reference Data Versus Policy

A reference value classifies; it does not define every consequence. For example, `DietaryClassification = VEGAN` does not alone define Promotion eligibility, legal food claims, allergen safety, pricing, refund policy, or professional guidance. Owning policies consume reference identity as one validated input where explicitly approved.

### Reference Data Versus Authorization

Taxonomy grants no permissions. ProfessionalSpecialty does not authorize practice, Kitchen capability does not grant booking access, and Chef cuisine specialty does not grant Organization membership. Authorization remains an authenticated, role/capability-, ownership-, context-, and resource-aware decision.

### Reference Data Versus Financial Facts

ADR-020 remains authoritative for Financial source facts and commercial obligations. Reference values cannot establish Payment, earning, payout eligibility, Refund, Ledger posting, or settlement beneficiary. Changing a category cannot rewrite Financial history.

### Reference Data Versus Conversation

ADR-021 remains authoritative for Conversation, participant authorization, and Message lifecycle. Message text, hashtags, labels, metadata, or extracted concepts do not become canonical reference values automatically. Chat may supply untrusted input to an explicit suggestion or classification workflow, but that workflow must apply governance, privacy, authorization, and domain validation.

### Reference Data Versus Reviews and Reputation

Future ADR-023 remains free to define verified-experience eligibility, review subjects, ratings, aggregation, moderation, reputation, reliability metrics, and historical evidence. Taxonomy may later provide typed review-topic, subject, filter, or moderation vocabulary only when explicitly approved.

```text
TAXONOMY
!= RATING
!= REPUTATION
```

ADR-022 does not pre-accept, preempt, or draft ADR-023.

## Security / Audit / Operations

### Security and Privacy

Governance and assignment operations require input validation, safe localized rendering, least privilege, rate or abuse controls where appropriate, and secure audit handling. User-, provider-, import-, and AI-supplied labels and descriptions are untrusted input.

Canonical shared vocabulary should generally contain non-sensitive concepts. It must not store diagnoses, medical notes, Customer private health details, private DietitianMealPlan content, private credential evidence, or Conversation content. Sensitive provenance, if ever required by an owning workflow, belongs in the appropriate protected domain rather than canonical taxonomy labels.

### Auditability

Governance actions that affect shared canonical meaning should be auditable. Conceptual evidence includes:

- actual actor or authorized process;
- action;
- affected typed reference value, alias, localization, or proposal;
- before/after or semantic-change evidence where appropriate;
- real timestamp; and
- reason where appropriate.

Audit history must distinguish display edits from semantic changes and ordinary assignment changes from canonical governance. Exact audit persistence, retention, and presentation remain ERD, security, and operations work.

### Identifiers and Time

ADR-010 remains authoritative for identifier strategy. Stable IDs are independent of labels, translations, slugs, aliases, and external vocabulary IDs. Display text must not be the sole foreign key.

Governance creation, review, activation, rejection, retirement, replacement, merge, and audit timestamps are real instants governed by ADR-011. Localized labels do not introduce timezone semantics. Future effective-dated business-local behavior, if genuinely required, must be explicitly designed rather than inferred from display locale.

### Cache and Update Propagation

Reference data is read-heavy and may be cached. Redis may cache canonical or localized projections but is never authoritative. Correctness must not depend solely on Redis, and cache loss must not lose canonical values.

Canonical updates commit durably to PostgreSQL first. Cache, search, analytics, clients, and other projections may update asynchronously. Temporary stale display or search data must not corrupt stable canonical identity, create a second identity, or authorize invalid assignment. Propagation needs version/invalidation strategy, observability, retries, and rebuild capability.

### Transactional Outbox and Events

Important reference lifecycle changes may publish integration events through the accepted transactional-outbox architecture. Conceptual meanings may include activation, retirement, replacement, or alias change. These are examples only; this ADR does not finalize event names, payloads, aggregate types, routing, publication criteria, or versions.

Canonical mutation and required outbox intent are persisted atomically according to ADR-009. Consumers are idempotent and tolerate retries. ADR-016 remains authoritative for event versioning and compatibility. Exact contracts belong to `docs/05-event-contracts.md`.

### Seed and Import Data

The Platform may seed initial values from curated, version-controlled or migration-controlled sources. Seeded values remain governed, identifiable, reviewable, and historically stable. Once runtime canonical data exists in PostgreSQL, seed files are not an independent permanently authoritative source that can overwrite runtime identity or lifecycle.

Imports from external standards must preserve mapping provenance where useful, use idempotent matching, avoid duplicate identities, and respect local governance. An import update must not silently redefine historical Cheffy meaning.

### Observability

Privacy-minimized metrics may include proposal volume, approval/rejection rate, duplicate suggestions, unmapped search terms, AI confidence distributions, classification correction rates, retired-value usage, propagation lag, cache/index failures, and localization gaps.

Observability must not log private Message bodies, diagnoses, private meal-plan content, Customer health details, private credential evidence, or unnecessary raw user text merely to build taxonomy metrics. Correlation and actor references must follow security and privacy policy.

## Modular Monolith Boundary

ADR-001 remains authoritative. Taxonomy/reference capabilities remain within the Spring Boot modular monolith and PostgreSQL system of record, with clear domain/module boundaries and in-process collaboration where appropriate.

This ADR does not require a taxonomy microservice, reference-data microservice, classification microservice, separate reference database, OpenSearch cluster, or separate AI classification service. Future extraction requires a separate approved architecture decision justified by demonstrated scale, operational isolation, deployment, ownership, or security needs.

## Consequences

### Positive

- Shared vocabulary remains stable across web, mobile, API, search, Promotions, and analytics.
- Typed reference families preserve semantic and relational integrity instead of creating universal polymorphic shortcuts.
- Search and filtering can use canonical values, aliases, synonyms, and localizations predictably.
- Promotion targets can reference stable identities without inheriting label or localization changes.
- Localized displays do not duplicate canonical meaning.
- Provider and user suggestions can improve coverage without becoming globally authoritative immediately.
- AI can assist classification, duplicate detection, and localization without becoming the source of truth.
- Retirement and replacement preserve historical Order, Promotion, analytics, and business explainability.
- Semantic changes are explicit rather than silently repurposing old identities.
- Stable identity supports historical-original and current-normalized analytical views.
- Domain-owned assignment validation prevents an active value from becoming a universal claim.
- Reference capabilities can grow without forcing every application concept into one taxonomy engine.

### Negative / Costs

- Governance workflows, privileged administration, and audit capabilities require product and implementation work.
- Typed families and assignments require more deliberate ERD and application design than a universal key/value registry.
- Alias, localization, slug, and replacement lifecycle require management and propagation.
- Duplicate and near-duplicate detection requires normalization, review, and concurrency-safe database controls.
- Material semantic changes require explicit migration and historical-reporting decisions.
- Search indexes, caches, analytics projections, and clients require invalidation, versioning, and stale-data handling.
- High-risk or ambiguous classifications may require human review.
- Assignment history and transaction-time evidence add storage and query complexity where classifications affect commercial, regulatory, safety, or historical behavior.
- External vocabulary imports require mapping, provenance, version, and drift management.
- Exact ERD, API, admin UI, authorization, event, and operational procedures remain additional work after approval.

## Alternatives Considered / Rejected

### 1. Every Text Label Is a Canonical Taxonomy Value

Rejected because free text, observed queries, labels, and canonical semantic identities have different authority and lifecycle.

### 2. Providers Directly Create Globally Canonical Values

Rejected because ordinary listing/profile capabilities must not bypass Platform governance, duplicate control, authorization, and semantic review.

### 3. AI Suggestions Automatically Become Canonical Values

Rejected because model output is not a governance decision or source of truth. Automatic low-risk assignment of an existing value does not authorize canonical-value creation.

### 4. Chat Hashtags or Messages Become Taxonomy Automatically

Rejected because ADR-021 owns Conversation, private free text is untrusted, and explicit governed classification is required.

### 5. One Universal TaxonomyValue Model Owns All Reference Families

Rejected because Cuisine, DietaryClassification, ProfessionalSpecialty, Kitchen classifications, Promotion, lifecycle states, and unrelated domain concepts have different semantics, relationships, and validation.

### 6. Arbitrary Reference Type Plus UUID Is Canonical Referential Integrity

Rejected because it permits dangling or cross-family-invalid relationships and removes database-enforceable semantic integrity.

### 7. Universal ReferenceRegistry for Unrelated Domains

Rejected because registry membership does not establish domain meaning, cardinality, assignment validity, or ownership.

### 8. One Giant Nullable-FK Assignment Table

Rejected because it creates invalid combinations, sparse schema, weak constraints, and unsafe evolution across unrelated families.

### 9. Display Label Is Canonical Identity

Rejected because spelling, capitalization, wording, and translation change without necessarily changing semantic meaning.

### 10. Each Localized Label Is a Separate Semantic Identity

Rejected because translation represents one concept and should not fragment assignment, Promotion targeting, or analytics by language.

### 11. URL Slug Is Canonical Database Identity

Rejected because SEO and human-readable URL wording evolve independently of semantic identity.

### 12. Renaming a Label Rewrites Historical Business Meaning

Rejected because ordinary display changes must not mutate historical identity, snapshots, Promotion evidence, Orders, or analytics facts.

### 13. Retiring a Value Deletes Historical References

Rejected because retirement stops ordinary new use while retaining historical explanation.

### 14. Current Taxonomy Recomputes Historical Promotions

Rejected because ADR-014 requires historical Promotion evidence and deterministic transaction-time calculation; current taxonomy cannot rewrite past outcomes.

### 15. Search Index Is the Source of Truth

Rejected because an index is asynchronous, derived, and rebuildable. PostgreSQL remains authoritative.

### 16. Redis Cache Is the Source of Truth

Rejected because Redis is non-authoritative cache/coordination infrastructure and cache loss or staleness cannot define business identity.

### 17. Cuisine Is Inferred Solely from Nationality or Location

Rejected because nationality, provider identity, Organization country, Kitchen geography, and Cuisine are not interchangeable facts.

### 18. Dietary Tag Is an Allergen Safety Guarantee

Rejected because allergen safety requires structured ingredient/allergen evidence, provenance, operational controls, and applicable legal/product policy.

### 19. Specialty Label Proves Credential, License, or Jurisdiction Eligibility

Rejected because ADR-017 owns professional credentials, verification, and eligibility; specialty is profile/discovery classification only.

### 20. Taxonomy Owns Lifecycle or Status Enums for All Domains

Rejected because Order, Payment, Appointment, Subscription, Conversation, and other statuses are validated domain state machines, not generic editable reference data.

### 21. Taxonomy Owns Pricing, Fees, or Business Configuration

Rejected because values such as fees, grace periods, limits, credentials, and feature flags are policy/configuration, while Pricing remains owned by Pricing and Promotion rules.

### 22. Taxonomy Directly Grants Authorization

Rejected because classification cannot replace authenticated role/capability, ownership, context, and resource authorization.

### 23. Taxonomy Change Rewrites Financial History

Rejected because ADR-020 and Financial architecture require durable source facts; taxonomy is not Payment, obligation, earning, Refund, Payout, or Ledger evidence.

### 24. Concrete Equipment Inventory Is Modeled as Taxonomy

Rejected because ADR-007 owns reservable resources and capacity. Equipment reference meaning is distinct from physical inventory and rental availability.

### 25. A Taxonomy Microservice Is Required for MVP

Rejected because ADR-001 establishes a modular monolith, and current consistency and operational needs do not justify a separate service.

### 26. Generic JSON Metadata Is the Canonical Relationship Model

Rejected because JSON cannot replace typed assignment, ownership, referential integrity, lifecycle, and historical meaning.

### 27. Normalized Lowercase Text Alone Defines Semantic Identity

Rejected because normalization can aid duplicate detection but can destroy language distinctions and merge semantically different concepts.

### 28. External Vocabulary IDs Become Cheffy's Internal Identity

Rejected because external sources, versions, and identifiers may change; mappings must not control Cheffy's durable business identity or historical meaning.

### 29. All Assignments Require Universal Immutable History

Rejected because evidence requirements depend on materiality. Commercial, safety, regulatory, Promotion, and historical decisions need stronger evidence than every low-risk discovery edit.

### 30. Taxonomy Defines Review Ratings and Reputation

Rejected because future ADR-023 owns verified experience, review, rating, aggregation, moderation, reputation, and reliability semantics.

## Dependencies / Related ADRs

- **ADR-001 — Modular Monolith First (Accepted):** taxonomy/reference capabilities remain inside the modular monolith; no taxonomy, reference-data, or classification microservice is required.
- **ADR-006 — Promotion Targeting Model (Accepted):** Promotion target identity, target-family relationships, and targeting semantics remain ADR-006-owned. ADR-022 supplies stable typed reference identity where an approved target uses it.
- **ADR-007 — Booking Concurrency (Accepted):** concrete EquipmentRental and Space capacity, holds, occupancy, and concurrency remain separate from equipment or Kitchen reference classification.
- **ADR-009 — Outbox Table Schema (Accepted):** important asynchronous lifecycle propagation uses accepted transactional-outbox persistence where appropriate.
- **ADR-010 — UUIDv7 Identifier Strategy (Proposed):** stable canonical and governance identities follow repository identifier direction and remain independent of mutable labels.
- **ADR-011 — Timezone Modeling Strategy (Accepted):** governance timestamps are real instants; localization does not create timezone semantics.
- **ADR-014 — Promotion Engine (Proposed):** Promotion evaluation, eligibility, compatibility, snapshots, repricing, and refunds remain Promotion-engine concerns and are not recomputed from current taxonomy.
- **ADR-016 — Event Versioning (Accepted):** future reference-data integration events require explicit compatible versioning.
- **ADR-017 — Professional Identity, Credentials and Jurisdiction Eligibility (Proposed):** specialty classification remains distinct from professional identity, credential verification, licensure, and jurisdiction eligibility.
- **ADR-018 — Dietitian Engagement, Appointment Scheduling and Online Meeting Provisioning (Proposed):** specialty may support discovery or offering classification but cannot create an engagement or alter Appointment lifecycle.
- **ADR-019 — Subscription, Entitlement and Materialized Occurrence Architecture (Proposed):** reference classifications may describe approved offers or discovery facets but do not own Subscription, entitlement, occurrence, capacity, or fulfillment state.
- **ADR-020 — Commercial Obligations, Earning Recognition and Payable-Source Financial Model (Proposed):** taxonomy is not commercial or Financial evidence and cannot rewrite obligation, earning, Refund, Payout, or Ledger history.
- **ADR-021 — Authorized Multi-Context Conversation Architecture (Proposed):** Conversation free text, hashtags, metadata, and extracted concepts do not become canonical taxonomy without an explicit governed workflow.

No related ADR status is changed by this Proposed ADR.

## Future ADR Relationship

- **ADR-023 — Verified-Experience Reviews and Reputation:** remains future work and owns review eligibility, verified experience, review subject, ratings, aggregation, moderation, reputation, and reliability metrics. ADR-023 may consume typed reference values where useful but is not constrained to treat them as rating or reputation semantics.

This ADR does not draft, accept, or change the status of ADR-023.

## Out of Scope

This Proposed ADR does not finalize or introduce:

- exact taxonomy/reference tables, columns, foreign keys, indexes, constraints, migration SQL, hierarchy depth, or assignment layout;
- exact administration, suggestion, classification, search, or public REST endpoints and OpenAPI schemas;
- exact event names, payloads, aggregate types, routing, publication criteria, or versions;
- exact localization persistence, fallback behavior, slug lifecycle, or URL design;
- exact AI model/provider, provider-specific schema, confidence thresholds, or automated-assignment policy;
- exact allergen, certification, food-claim, credential, professional-practice, or legal-compliance policy;
- exact assignment-history or historical-snapshot representation for every domain;
- exact review, rating, moderation, reputation, or reliability architecture;
- DietitianMealPlan redesign, Chef assignment, food-sale attribution, or Dietitian commission;
- PricingSnapshot, PromotionSnapshot, Financial snapshot, or Order snapshot redesign;
- a universal TaxonomyValue, ReferenceRegistry, generic polymorphic relationship, giant nullable-FK assignment table, or metadata-JSON identity model;
- a taxonomy/reference/classification microservice;
- OpenSearch or Elasticsearch solely for taxonomy; or
- application code, migrations, SQL, API changes, or event-contract changes.

## Implementation / Propagation Notes

This Proposed ADR does not authorize implementation by itself. After approval, canonical propagation and implementation planning must:

1. Reconcile `docs/03-database-erd.md` with explicit typed reference families, canonical identity, aliases, localized displays, proposals, governance evidence, retirement/replacement, and typed assignments without a universal registry or polymorphic UUID shortcut.
2. Preserve the existing canonical Catalog/Food, EquipmentCatalogItem, professional identity, Promotion, Order, Subscription, Conversation, and Financial boundaries while introducing only the reference capabilities each family needs.
3. Define stable IDs and optional stable machine codes independently of mutable labels, localized text, aliases, external IDs, and slugs under ADR-010.
4. Define family-specific lifecycle meanings that distinguish non-approved, active, rejected, retired, and historically retained values without forcing identical physical enums.
5. Define semantic-change, replacement, supersession, merge, and retirement policy that preserves historical-original meaning and prevents ordinary new assignment where appropriate.
6. Define typed assignment cardinality, ownership, validation, and history policy for each approved family; do not infer assignment validity from reference lifecycle alone.
7. Define cycle-safe FoodCategory hierarchy and practical depth rules without using a path string as canonical identity.
8. Define alias, synonym, localization, normalization, diacritic, duplicate-detection, and fallback behavior without duplicating semantic identity per language.
9. Define suggestion submission and governance authorization, including merge-to-existing, approve-as-alias, approve-as-value, reject, and local/free-text outcomes.
10. Define AI provenance, confidence, approval, correction, monitoring, and any low-risk automatic-assignment policy while prohibiting automatic canonical-value creation.
11. Keep ingredient, allergen, nutrition, certification, credential, and jurisdiction evidence structured and owned by their appropriate domains rather than reducing them to casual tags.
12. Preserve ADR-006 and ADR-014 Promotion targeting/evaluation authority, including transaction-time snapshots and no recomputation of historical results from current taxonomy.
13. Define selective historical assignment/snapshot evidence where classification affects commercial, Promotion, regulatory, safety, review-eligibility, historical UX, or analytics behavior.
14. Define privileged governance capabilities, input validation, safe localized rendering, audit, least privilege, and protection against private medical, credential, meal-plan, and Conversation content entering canonical records.
15. Define transaction, uniqueness, concurrency, and idempotency controls so concurrent proposals/approvals and retries cannot create duplicate semantic identities or lifecycle actions.
16. Keep PostgreSQL authoritative; define rebuildable cache/search/analytics projections and stale-data-safe propagation. Redis and search indexes remain non-authoritative.
17. Use ADR-009 transactional outbox and ADR-016 event versioning only after exact integration needs and contracts are approved in `docs/05-event-contracts.md`.
18. Update `docs/04-api-contracts.md` only after exact administration, suggestion, assignment, localization, and discovery representations are approved.
19. Treat curated seeds and imports as governed, identifiable inputs to PostgreSQL rather than independent runtime authorities; preserve external source/version mappings where useful.
20. Define privacy-minimized observability for governance quality, duplicate handling, AI assistance, retired usage, localization gaps, and propagation without logging unnecessary private content.

At minimum, future tests must prove that free text, aliases, provider suggestions, AI output, chat text, localized labels, and slugs cannot become or replace canonical semantic identity without approved governance; display and localization edits retain identity; incompatible semantic change cannot silently reuse an old identity; retired values reject ordinary new assignment while historical references remain explainable; replacement does not rewrite historical source facts; typed family and assignment integrity reject invalid cross-family references; concurrent approvals cannot create unintended duplicate identities; FoodCategory cycles are rejected; Cuisine is not inferred solely from nationality or location; dietary labels do not claim allergen safety; specialty does not establish credentials, licensure, or jurisdiction eligibility; Equipment reference values do not become reservable capacity; Promotion history is not recomputed from current taxonomy; search and Redis remain rebuildable projections; taxonomy cannot grant authorization or mutate domain state machines; taxonomy changes cannot rewrite Financial history; and no universal registry, generic metadata identity, giant nullable-FK assignment table, taxonomy microservice, or exact ADR-023 review/reputation behavior is introduced.
