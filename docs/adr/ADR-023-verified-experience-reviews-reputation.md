# ADR-023: Verified-Experience Reviews and Reputation

## Status

Proposed

## Context

Cheffy Bites needs trusted marketplace feedback for Food, Chefs, Dietitians, Kitchens, equipment, Delivery, and other approved service contexts. Public feedback influences discovery, provider accountability, Customer decisions, moderation, and future marketplace quality. It therefore requires stronger evidence and attribution than an arbitrary public comment attached to a profile.

A Review must arise from durable evidence that the reviewer actually participated in an eligible Cheffy Bites marketplace transaction or service relationship. Account existence, authentication, profile viewing, search, Conversation participation, Payment activity, current Organization membership, and unstructured sentiment do not prove that relationship or authorize feedback about an arbitrary subject.

Marketplace identity is also multi-dimensional. The actual Chef or Dietitian who performed a service may differ from the Organization acting as commercial provider and from the settlement beneficiary. A physical Kitchen may differ from its operator and property owner. Multi-Chef Orders contain separate ChefOrderGroups. Review architecture must preserve these distinctions rather than transferring reputation to whichever Organization, listing owner, worker, beneficiary, or account happens to be current.

Subjective ratings must remain separate from objective service-performance facts and derived reliability signals. A poor rating does not reverse fulfillment, and a high rating does not prove contractual or legal performance. Reviews also do not create Payment, Refund, earning, payout eligibility, Payout, or Ledger consequences. Those facts remain owned by their source domains and the Financial architecture.

This ADR defines conceptual architecture for verified-experience eligibility, Review evidence, typed subjects, reviewer identity, ratings, publication, moderation, editing, provider response, aggregates, reputation, reliability, historical attribution, privacy, and projection boundaries. It deliberately does not finalize exact tables, columns, foreign keys, SQL, APIs, rating scales, formulas, moderation states, review windows, event names, or event payloads.

## Decision

Cheffy Bites will implement Reviews and Reputation as a capability inside the modular monolith, with PostgreSQL as the authoritative store. A Review is an explicit user operation authorized by durable, typed verified-experience evidence. Arbitrary unverified public reviews and anonymous unverified marketplace ratings are prohibited.

Review authorization will bind an actual authorized marketplace participant to one typed qualifying experience and one or more explicit typed subjects that the experience can legitimately support. One experience may authorize separate Reviews of several subjects, but it will not require one giant Review containing every subject. Repeated qualifying experiences remain separately attributable and may create new review opportunities under product policy.

Canonical Review evidence remains distinct from publication state, moderation evidence, provider commentary, aggregate ratings, reputation projections, reliability measures, search indexes, caches, and AI summaries. Those derived or editorial concerns may evolve or update asynchronously without replacing the underlying Review and verified-experience evidence.

Reviews may be edited, withdrawn, redacted, removed, reinstated, or otherwise treated under explicit product, moderation, privacy, and legal policy. They are not Financial-ledger-style immutable. Sufficient durable history will nevertheless be retained where lawful and necessary for deterministic aggregate updates, audit, moderation, abuse investigation, and publication history.

## Required Invariants

1. `REVIEW != ARBITRARY COMMENT`.
2. `VERIFIED EXPERIENCE != PAYMENT RECEIVED`.
3. `VERIFIED EXPERIENCE != CONVERSATION PARTICIPATION`.
4. `REVIEWER ACCOUNT != REVIEW ELIGIBILITY`.
5. `REVIEW SUBJECT != COMMERCIAL PROVIDER BY DEFAULT`.
6. `SERVICE PERFORMER != COMMERCIAL PROVIDER`.
7. `CHEFORDERGROUP != REVIEW`.
8. `CHEFORDERGROUP != RATING`.
9. `CHEFORDERGROUP != REPUTATION`.
10. `RATING != SERVICE PERFORMANCE FACT`.
11. `RATING != RELIABILITY SCORE`.
12. `REVIEW != FINANCIAL FACT`.
13. `REFUND != AUTOMATIC REVIEW INVALIDATION`.
14. `CONVERSATION MESSAGE != REVIEW`.
15. `CHAT SENTIMENT != RATING`.
16. `REVIEW != REPUTATION`.
17. `AGGREGATE RATING != REVIEW SOURCE OF TRUTH`.
18. `SEARCH INDEX != REVIEW SOURCE OF TRUTH`.
19. `REDIS != REVIEW SOURCE OF TRUTH`.
20. `NO REVIEWS != AVERAGE RATING`.
21. `TAXONOMY != REVIEW / REPUTATION`.
22. `PROVIDER FAILURE != COMPLETED SERVICE`.
23. `VOLUNTARY NON-USE != OPERATOR NONPERFORMANCE`.
24. `REVIEW RESPONSE != ORIGINAL REVIEW`.
25. `AI SUMMARY != ORIGINAL REVIEW`.

## Verified Experience / Eligibility

### Verified Experience

A **Verified Experience** is durable evidence that the reviewer actually participated in an eligible marketplace transaction or service relationship and that the owning domain reached the policy-defined qualifying outcome for the proposed Review subject and rating dimensions.

Potential typed sources include, where current or future product policy permits:

- a completed Food Order;
- fulfilled ChefOrderGroup participation;
- a fulfilled OrderItem for Food-level feedback;
- a completed Delivery for Delivery-level feedback;
- a completed KitchenBooking or qualifying Kitchen service;
- an actually allocated or rented EquipmentRental experience;
- a completed Dietitian Appointment;
- a fulfilled MealFulfillmentOccurrence or concrete Food fulfillment;
- a completed or otherwise qualifying Kitchen Subscription service occurrence or service period where explicit policy permits; and
- another future marketplace context approved through product and architecture governance.

Eligibility is not created solely by:

- account existence or authentication;
- profile view, search, or discovery activity;
- Conversation existence, participation, Message content, or sentiment;
- a Payment attempt, authorization, capture, or receipt;
- a wishlist, favorite, or FoodRequest visibility;
- current Organization membership;
- a Subscription purchase before the relevant service is supplied; or
- another non-performance signal that does not establish authorized participation in the subject experience.

Payment may be corroborating transaction evidence, but it is neither sufficient nor always necessary to prove a qualifying service experience. Eligibility consumes objective identity, participation, subject, and outcome evidence from the owning domains.

### Typed Verified-Experience Sources

Verified-experience relationships must be typed and domain-valid. Exact relational representation belongs to `docs/03-database-erd.md`, but the selected design must support database-enforceable referential integrity where practical and must reject cross-context or dangling references.

The canonical model must not settle on an unconstrained relationship such as:

```text
experience_type
experience_id UUID
```

as final referential integrity. This ADR does not approve a universal ExperienceRegistry created merely to point at unrelated aggregates, one giant nullable-foreign-key Review table, or generic metadata/JSON identity. The later ERD may use typed eligibility records, typed source relationships, domain-specific eligibility contributors, or an equivalently strong model while preserving a coherent Review capability.

### Durable Review Authorization Evidence

The architecture will represent review eligibility or equivalent durable authorization evidence capable of explaining:

- the actual reviewer business identity;
- the typed verified-experience source;
- the explicit eligible Review subject or subjects;
- the qualifying outcome and context relevant to permitted dimensions;
- when eligibility was created;
- when eligibility expires, if context-specific policy defines a window;
- whether an authorization was consumed or used where applicable;
- legitimate revocation or invalidation;
- a moderation or abuse hold where applicable; and
- sufficient policy/version context to explain the decision.

These are conceptual requirements, not final table, column, or state names. Eligibility windows may differ among Food, Kitchen, Appointment, Delivery, equipment, and other contexts; this ADR defines no universal duration.

### One Experience and Multiple Subjects

One verified experience may legitimately authorize separate Reviews for more than one explicit subject. A multi-Chef Food Order may support Chef Ravi, Chef Maria, their respective Food items, Delivery, and potentially a Kitchen or Organization service subject where product policy separately permits each relationship.

The architecture does not require all subjects to be placed in one Review. Eligibility for one subject does not imply eligibility for every participant, provider, worker, listing, Organization, or resource associated with the broader transaction. Exact UX, solicitation, and number-of-review rules remain product and API work.

### One Subject and Multiple Experiences

A Customer or Chef may have multiple qualifying experiences with the same Chef, Food, Dietitian, Kitchen, Organization, Space, equipment offering, Delivery service, or other subject over time. Each experience remains separately evidenced. The architecture will not collapse them into one lifetime entitlement unless an explicit product policy later requires that behavior.

A supported policy may allow one Review per reviewer, experience, and subject; an editable Review representing that experience; and another Review only after another qualifying experience. Unlimited duplicate Reviews from one experience are prohibited.

### Timing and Qualifying Outcomes

A rating must not become final public experience feedback before the underlying experience reaches its policy-defined qualifying state. Food feedback normally follows fulfilled or completed evidence, Dietitian feedback follows a qualifying Appointment outcome, and Kitchen feedback follows a qualifying booking or service outcome. Payment receipt alone never advances the experience to reviewable status.

Cancelled and failed experiences require context-specific treatment as described below. Where current product requirements require completion, a non-completed experience is not made ordinarily reviewable by this ADR. A future explicit product policy may authorize limited reliability-focused feedback for verified provider-caused failure without representing the service as completed.

### Idempotency and Concurrency

Review submission must be idempotent where retry could duplicate a Review. Database-safe uniqueness and transaction controls must prevent duplicate creation for the same allowed reviewer, verified experience, and subject combination when product policy permits only one. UI hiding, Redis coordination, or a preflight query alone is insufficient.

The design must also prevent double consumption of one authorization and protect concurrent Review edits, moderation actions, responses, removals, and projection updates from corrupting canonical state. Exact uniqueness keys, optimistic versions, row locks, isolation choices, and request-idempotency representations remain ERD and implementation work.

## Reviewer Identity

A reviewer must be the actual authorized marketplace participant in the qualifying experience. For Customer Reviews, the qualifying transaction must be associated with the actual Customer business identity. For Chef Reviews of Kitchens, Spaces, hosts, or rented equipment, the relevant booking or allocation must establish that Chef's participation and authorization.

Authentication establishes the caller but does not by itself establish Review eligibility. Auth0 `sub` is not canonical Customer, Chef, professional, or reviewer business identity. The system resolves authenticated identity to the applicable internal business identity and then validates typed participation, subject authorization, conflicts, eligibility status, and ownership.

Multi-role accounts require explicit context validation. A person acting as a Customer cannot use another role or account relationship to review themselves, their own professional identity, or their own Organization when identity and business relationships establish a prohibited conflict. Current Organization membership alone cannot manufacture Customer participation.

Canonical reviewer identity is retained internally for eligibility, abuse prevention, moderation, audit, and lawful operations even when public presentation uses a display name, first name, initials, pseudonym, or anonymized form.

## Typed Review Subjects

A Review subject is explicit, typed, and supported by the verified experience. Potential subjects include, where product policy permits:

- the actual Chef performer;
- a Food item, FoodListing, ChefMealPlan, or concrete meal subject supported by fulfillment evidence;
- Delivery as a distinct service subject;
- the physical Kitchen;
- a Kitchen Space or actually rented equipment offering;
- the Kitchen operating Organization or Entrepreneur/host service;
- the actual Dietitian professional;
- a professional service or Appointment experience; and
- another explicitly approved marketplace service or provider subject type.

Subject types have different semantics, dimensions, authorization, aggregation, visibility, and historical attribution. The canonical model must not use an unconstrained `subject_type` plus arbitrary `subject_id UUID` as final referential integrity. It must not use a universal SubjectRegistry, a giant nullable-FK Review table, or generic metadata JSON as a shortcut around typed domain relationships. Exact ERD strategy remains deferred.

### Actual Performer Versus Organization

The actual service performer is not automatically the commercial provider, settlement beneficiary, employer, or operating Organization. For example:

```text
Order
  commercial provider: ABC Food Group
  actual performer: Chef Ravi
  actual performer: Chef Maria
```

The verified experience may authorize a Ravi-specific Review, a Maria-specific Review, and a separate Organization/provider service Review if product policy permits each subject. Ravi's rating is not automatically assigned to ABC Food Group, and ABC Food Group's rating is not automatically assigned to Ravi or Maria.

Organization reputation must not be computed merely by permanently summing all worker ratings without explicit product semantics. Individual performer and Organization/service-provider reputation remain distinguishable even where both are displayed together.

## Food / Multi-Chef Reviews

Food Review eligibility consumes actual Customer, Order, fulfillment, ChefOrderGroup, OrderItem, Food, Kitchen, and actual-performer evidence as applicable. The one-Kitchen-per-Order invariant remains unchanged.

For Chef-level feedback, ChefOrderGroup may provide durable proof that one actual Chef participated in the Customer's Food Order. ADR-013 remains authoritative for ChefOrderGroup operational and financial-reference boundaries. ChefOrderGroup is evidence for eligibility; it is not itself a Review, Rating, Reputation record, or aggregate.

For a multi-Chef Order containing Chef Ravi and Chef Maria:

- Ravi may be reviewed only because Ravi's ChefOrderGroup establishes actual participation;
- Maria may be reviewed only because Maria's ChefOrderGroup establishes actual participation;
- Ravi's participation does not authorize a Review of an unrelated Chef;
- item-level feedback must reference an item that belongs to the relevant ChefOrderGroup;
- the actual Customer must be associated with the parent Order; and
- current Organization membership, current listing ownership, commercial-provider identity, or settlement-beneficiary identity must not be used to infer the historical performer.

Food Item or FoodListing feedback must derive from the applicable fulfilled OrderItem or concrete fulfillment evidence. A Meal Subscription payment or future cycle charge does not authorize feedback on meals not yet supplied. A fulfilled MealFulfillmentOccurrence or resulting fulfilled Food Order may create the relevant authorization.

Delivery remains independently reviewable where product policy permits and completed Delivery evidence exists. Third-party Delivery failure must not automatically reduce the Chef's Food or service rating. Similarly, a Chef Review must not silently become a Kitchen, Organization, or Delivery Review.

## Dietitian Reviews

Dietitian Review eligibility consumes ADR-018 Appointment evidence. Where the professional is the Review subject, the durable actual Dietitian professional identity is used; clinic, Organization, commercial-provider, administrator, or settlement identity does not replace that professional.

A separate Organization, clinic, Appointment-service, or provider Review may exist only where product policy explicitly permits that typed subject and the experience supports it. Professional rating dimensions concern the service experience, such as professionalism, communication, helpfulness, or another approved dimension; public ratings must not be presented as proof of diagnosis, treatment efficacy, clinical outcome, licensure, or medical correctness.

Review content and public projections must not automatically expose diagnosis, medication, clinical history, private DietitianMealPlan content, medical documents, credential evidence, private Appointment details, or private Conversation content. ADR-023 does not create an electronic medical record or medical-record review system. Privacy and moderation workflows must support detection, holding, redaction, removal, and lawful handling of sensitive disclosures.

## Kitchen / Subscription Reviews

Kitchen-related eligibility may derive from a qualifying completed KitchenBooking, actual Space experience, host/operator service, or actually allocated/rented EquipmentRental as defined by product policy. Typed subjects may distinguish:

- the physical Kitchen;
- the Kitchen Space or Unit;
- an actually supplied equipment/rental offering;
- the operating Organization; and
- the Entrepreneur or host service where policy defines that subject.

Kitchen property owner, Kitchen operator, host, and commercial provider are not presumed to be the same identity. An operator-service Review must not automatically attach to the property owner. Equipment feedback applies to the actual supplied/rental offering and experience, not merely the master EquipmentCatalogItem classification.

Kitchen Subscription review eligibility consumes ADR-019 service semantics. A paid entitlement or unused reservation opportunity is not automatically evidence of operator failure. Where accepted capacity or access was genuinely made available and the Chef voluntarily did not use it, non-use must not become a provider nonperformance penalty.

If product policy later permits a Review of Subscription service reliability or availability, eligibility must use the accepted service obligation and typed operator-performance evidence, including whether capacity was supplied, unavailable, cancelled by the operator, or voluntarily unused. A billing event alone does not establish that later service outcome.

Meal Subscription eligibility similarly depends on qualifying materialized meal fulfillment or concrete Food experience. Creating a Subscription or billing a future cycle cannot authorize a positive or negative performance Review of meals not yet supplied.

## Cancellation / Failure Semantics

Review eligibility for cancelled, no-show, and failed experiences is context-specific. Owning domains must preserve cause and actor evidence sufficient to distinguish at least:

- Customer cancellation;
- Customer no-show;
- provider or professional cancellation;
- provider or professional no-show;
- mutual cancellation;
- administrative cancellation; and
- another approved failure or correction classification.

The architecture rejects both universal rules that every cancellation is fully reviewable and that no cancellation can ever be reviewed. The applicable policy must identify the context, eligible subject, permitted dimensions, timing, evidence, and publication treatment.

A provider-caused cancellation, no-show, or nonperformance may itself be a verified marketplace relationship relevant to reliability. Subject to explicit product policy, it may authorize limited reliability-focused Review or feedback dimensions even though the promised service was not completed. It must not be represented as an ordinary completed-service rating, and dimensions about Food quality, treatment experience, Kitchen cleanliness, or other unperformed service attributes remain unauthorized.

Customer-caused cancellation or no-show must not automatically create provider nonperformance, cancellation, no-show, or reliability penalties. Absence of completion does not establish fault. Reliability derivation consumes the owning domain's typed cause, actor, obligation, and outcome evidence rather than inferring cause from Review text or missing completion.

If an objective source outcome is later corrected by its owning domain, Review eligibility and reliability derivation may be re-evaluated through explicit policy and an auditable operation. A reviewer statement such as "the Chef never delivered" may trigger support or moderation review but does not itself change canonical Order or fulfillment state.

## Rating Dimensions / Review Content

### Typed Rating Dimensions

Rating dimensions are typed by subject and experience semantics rather than assumed to be one universal star value. Potential examples include:

- Food or Chef: quality, taste, accuracy, preparation/reliability, and communication where policy permits;
- Kitchen: cleanliness, equipment condition, listing accuracy, and operational reliability;
- Dietitian: professionalism, communication, and service experience;
- Delivery: timeliness, handoff, or other approved Delivery dimensions; and
- provider-failure contexts: only the approved reliability or cancellation-behavior dimensions supported by the evidence.

This ADR does not finalize the rating-dimension catalog. ADR-022 governed reference vocabulary may supply labels, topic classification, or moderation reason vocabulary where appropriate, but reference data does not own Review semantics, eligibility, scale, or consequences.

### Overall Rating and Scale

An overall rating may exist where product policy requires it. It need not be a simple arithmetic average of all dimensions. Weighting, minimum sample sizes, confidence treatment, subject-specific formulas, missing-dimension behavior, and display precision remain later product and API work.

The architecture permits a conventional rating scale but does not hard-code exact scale values. Scale and dimension rules must be validated consistently for the applicable subject and policy version rather than accepted as arbitrary client input.

### Optional Text Review

Optional text may accompany structured ratings. Review text is user-generated commentary, not authoritative domain, performance, medical, or Financial fact. It requires:

- length, format, and content validation;
- safe rendering and output encoding;
- moderation and abuse reporting;
- privacy and sensitive-data controls;
- rate and duplicate controls where appropriate; and
- preservation of prior content where lawful and needed for audit or investigation.

Conversation Messages remain separate. Chat sentiment is not automatically imported as Review text, rating, reputation, reliability evidence, or verified-experience evidence. A user must perform an explicit authorized Review operation.

## Lifecycle / Publication / Editing

### Submission Versus Publication

Review submission and public visibility are separate concerns. The architecture supports policy such as immediate publication with post-moderation, pre-publication moderation for selected risks, pending investigation, holding, removal, redaction, withdrawal, or reinstatement. It does not require one universal workflow for every subject or risk class.

A conceptual lifecycle must distinguish meanings equivalent to:

- draft, where product policy permits;
- submitted;
- published or otherwise publicly visible;
- held or under review;
- removed, redacted, withdrawn, or otherwise non-public; and
- reinstated where policy permits.

These concerns do not have to be forced into one giant enum. Authoring lifecycle, moderation disposition, visibility, legal/privacy handling, and aggregate-counting policy may be modeled separately where that is clearer. Exact states and transitions remain ERD and product work.

### Editing

Review editing is policy-controlled and may be limited by context-specific windows or moderation status. Reviews are not ledger-style permanently immutable. A permitted edit may update rating dimensions, overall rating, or text while retaining sufficient prior evidence for audit, abuse investigation, moderation, and deterministic aggregate changes.

Concurrent edits and moderation must not silently overwrite each other or publish a stale version. Exact version checks and conflict behavior remain implementation work. Editing does not alter verified-experience identity or create a second entitlement.

### Withdrawal, Removal, Redaction, and Deletion

The architecture distinguishes:

- reviewer withdrawal or hiding;
- moderation removal;
- legal or privacy removal;
- partial redaction;
- reinstatement where permitted; and
- physical deletion where lawful and appropriate.

Making a Review non-public does not always require immediate physical deletion. Conversely, this ADR does not promise permanent retention when privacy, legal, safety, or retention policy requires deletion. Canonical and audit data must be minimized and retained only for justified purposes.

## Moderation / Abuse Prevention

### Moderation

Review moderation must support reporting, investigation, and action for at least:

- harassment, hate, threats, or abuse;
- spam and irrelevant content;
- extortion, coercion, or retaliation;
- privacy disclosure, medical/private information, or doxxing;
- prohibited or unsafe content;
- fraudulent, coordinated, or collusive reviewing;
- incentivized-review policy violations; and
- another governed moderation reason.

Moderation actions are authorized, auditable, idempotent where retried, and attributable to the actual actor or approved automated process. Audit evidence should preserve action, target Review/version, reason, real timestamp, and relevant before/after visibility or disposition without unnecessarily duplicating private content. Exact reason catalog, workflow, SLA, appeal process, and jurisdiction-specific legal handling remain product, policy, and implementation work.

### Anti-Abuse Controls

The architecture must support prevention and detection of:

- duplicate Reviews or repeated attempts for one entitlement;
- self-review across multi-role accounts;
- Organization staff manufacturing Customer Reviews for their own Organization or professionals;
- collusive review rings and fake accounts;
- retaliatory reviewing;
- extortion and provider coercion;
- coordinated rating manipulation; and
- undisclosed or sentiment-conditioned incentives.

Verified participation remains authoritative even when a user belongs to an Organization. Organization membership cannot manufacture a qualifying Customer transaction. Multi-role identity and business-relationship checks must prevent a provider or professional from reviewing themselves through another role/account context where the conflict is established.

This ADR does not require one fraud or machine-learning vendor. Database constraints, authorization, identity relationships, risk rules, rate controls, moderation, observability, and optional analytical models may work together.

### Incentivized Reviews

Review incentives are not required MVP functionality. If future policy permits an incentive, it must be governed and disclosed as required, must not require positive sentiment, and must not change the submitted rating value. A Promotion may incentivize completion of an honest Review only under approved compliance and disclosure policy; providers cannot purchase positive ratings. ADR-006 and ADR-014 remain authoritative for Promotion structure and evaluation.

### No Pay-to-Remove

A provider cannot remove a legitimate unfavorable Review by issuing a Refund or credit, paying the Platform, purchasing a Subscription, changing provider plan, or creating a Promotion. Commercial remediation may affect publication only through an explicit neutral Review policy applied independently of sentiment and commercial pressure.

## Provider Response

The architecture allows an authorized Review subject or provider representative to post a response where product policy permits. Response authorization is explicit and subject-aware. Organization membership alone does not necessarily authorize a person to respond as the reviewed Chef or Dietitian, and a professional response must not impersonate another person.

The actual responder identity, represented role/capability, authorization context, response history, moderation state, and timestamps must remain sufficiently attributable. Public presentation may identify the responder according to product policy without exposing private internal identity details.

A provider response is commentary. It does not alter the original Review or rating, verified-experience evidence, Order/Appointment/service outcome, earning, Refund, Payout, or other Financial fact. Response editing, withdrawal, moderation, reporting, and exact response cardinality remain product/API policy; current MVP product direction may limit public provider response cardinality without making that a universal architectural constraint.

## Refund / Financial Boundary

### Refund and Remediation

A Refund does not automatically invalidate eligibility, delete a Review, remove it from aggregates, or erase the verified experience. A legitimate experience may remain real even when the Customer receives full or partial remediation.

Explicit policy may re-evaluate eligibility or visibility for a duplicate or fraudulent transaction, a fully voided experience before service, proven Review abuse, a corrected source outcome, or a legal/privacy requirement. Such action is independent, reasoned, and auditable; it must not infer that every Refund means the experience never occurred.

### Financial Separation

A Review or rating does not create, negate, or alter:

- Payment;
- Refund;
- CommercialObligation;
- Earning;
- PayoutEligibility;
- Payout;
- Platform fee;
- settlement beneficiary;
- Ledger transaction or entry; or
- another Financial fact.

ADR-020 remains authoritative for commercial obligations, earning recognition, payable source, and Financial boundaries. ADR-012 remains authoritative for marketplace Payment/Refund direction, and ADR-015 remains authoritative for Ledger and reconciliation. Any economic consequence requires an explicit operation in the owning business and Financial domains using structured evidence and approved policy.

A Review dispute, moderation action, provider response, aggregate change, or reputation result does not directly change Financial or service-performance facts. Financial remediation may coexist with editorial action, but neither silently controls the other.

### Rating Versus Service Performance

Subjective feedback is not structured service-performance evidence. A one-star Review does not prove that service was not performed, and a five-star Review does not prove contractual, legal, clinical, or commercial performance.

Order/fulfillment, Appointment, MealFulfillmentOccurrence, Kitchen service/access, Delivery, and other owning domains remain authoritative for objective outcomes. ADR-020 consumes those structured facts, not Review sentiment, rating averages, or reputation projections.

## Aggregate Ratings

Aggregate ratings are derived state calculated only from Reviews that are eligible and countable under the applicable publication, moderation, withdrawal, and product policy. Potential projections include:

- average or weighted overall rating;
- count of counted Reviews;
- dimension-specific aggregates;
- rating distributions;
- verified-experience volume where separately defined; and
- time-windowed or recency-aware metrics where future policy requires them.

This ADR does not finalize formulas, weighting, precision, confidence adjustment, minimum sample sizes, time decay, or publication thresholds. Old Reviews need not disappear solely because they are old.

Canonical Review records and their applicable versions/dispositions remain source evidence. Aggregates are rebuildable and may update asynchronously. If a Review stops counting because of moderation, proven abuse, legal removal, policy-controlled withdrawal, redaction affecting countability, or another explicit disposition, affected aggregates update deterministically without destructively rewriting unrelated Reviews.

Review count represents actual counted Reviews under the selected policy. It is not fabricated from Orders, Payments, profile views, Messages, eligible-but-unused authorizations, or verified-experience volume. A subject with no counted Reviews has a no-reviews state, not a fabricated neutral or positive average; product UI may present the subject as "New" or equivalent.

## Reputation / Reliability

### Reputation

Reputation is a derived marketplace interpretation of historical eligible, moderated Review evidence and, where explicitly approved, structured operational evidence. It is not a source record and does not replace Review, verified-experience, performer, Organization, credential, authorization, or service facts.

Typed reputation presentations may include aggregate Customer rating, counted Review volume, verified-experience volume, dimension summaries, and selected product-governed quality indicators. This ADR does not define one universal score or formula.

Chef quality, Food quality, Kitchen quality, Dietitian service quality, Delivery quality, Organization service reputation, and operational reliability have different meanings. The architecture must not force them into one Platform-wide number. Shared calculation infrastructure is permitted, but subject-specific semantics remain explicit.

High reputation does not grant a professional license, credential, jurisdiction eligibility, Organization authorization, Kitchen access, Conversation access, Promotion privilege, settlement status, or payout eligibility.

### Reliability

Reliability is distinct from subjective rating and is derived from typed operational evidence. Potential signals include:

- provider-caused cancellation rate;
- acceptance and fulfillment reliability;
- provider no-show rate;
- late readiness or fulfillment;
- Kitchen availability or access failure;
- Delivery timeliness; and
- another approved structured outcome.

Exact reliability metrics, denominators, windows, thresholds, exclusions, minimum samples, and presentation remain product and analytical policy. Review text or sentiment does not become an objective reliability event.

### Reliability Attribution

Reliability must be attributed to the correct actor and subject using durable source evidence. Chef Ravi's cancellation must not automatically penalize Chef Maria. Kitchen operator failure must not automatically penalize the property owner. Delivery-provider failure must not automatically penalize the Chef. Customer cancellation or no-show must not automatically penalize a provider.

Current Organization membership, current operator, current listing ownership, commercial-provider configuration, or current settlement beneficiary must not rewrite historical attribution. Reliability projections must be rebuildable from typed source outcomes and historical attribution rather than inferred from current relationships.

## Historical Attribution

Review and eligibility evidence must preserve enough durable subject identity and experience context to explain who or what was reviewed at the time. Historical attribution must not be recomputed from current:

- Organization membership;
- employment or professional engagement;
- provider or beneficiary configuration;
- listing ownership;
- Kitchen operator or property owner;
- subject display name; or
- another mutable relationship.

If Chef Ravi leaves ABC Food Group, Ravi's historical Chef Reviews remain Ravi's. They do not become the Organization's Reviews or move to another Chef. Separate Organization-level Reviews, if supported, remain associated with that Organization subject.

If an Organization changes staff, individual worker ratings do not automatically disappear into or permanently define Organization reputation. If a Kitchen changes operating Organization, historical Kitchen and operator Reviews remain attributable to the subject actually reviewed and are not silently transferred to the new operator or property owner.

If a Food listing, provider, professional, Kitchen, reference subject, or Organization is deactivated, retired, renamed, or merged, historical Reviews remain attributable to their original typed identity. Current discovery may map or redirect under explicit policy, but distinct people or businesses must not silently inherit one another's Reviews. Subject merges require careful identity, moderation, legal, product, and historical-analysis policy; ordinary rename does not change identity.

## Taxonomy / Conversation / AI Boundaries

### Taxonomy

ADR-022 may provide controlled vocabulary for rating-dimension labels, Review topic classification, moderation reasons, filtering, or search. Taxonomy is optional vocabulary, not Review eligibility, Review evidence, rating value, publication, moderation decision, reliability evidence, or Reputation.

Rating scale, eligibility, publication, and Reputation semantics must not become freely editable reference values without an explicit future decision. Generic taxonomy assignments cannot replace typed Review subjects or verified-experience source relationships.

### Conversation

ADR-021 remains authoritative for Conversation authorization and Message lifecycle. A Conversation Message is not a Review. Participation does not create eligibility, and chat sentiment does not automatically become rating, Review, Reputation, reliability, verified-experience evidence, service performance, or Financial evidence.

Conversation content may enter a report or support workflow only through explicit authorization, privacy, and policy. Private Messages must not be copied into public Reviews or projections automatically.

### AI Assistance and Summaries

AI may assist with moderation, spam/fraud triage, sensitive-data detection, topic classification, Review summarization, coordinated-abuse detection, and routing cases for human review. AI must not silently:

- invent a rating or Review;
- submit a Review on behalf of a participant;
- change verified-experience, performer, subject, or service facts;
- determine provider earning, Refund, Payout, or another Financial fact; or
- fabricate Reputation or reliability evidence.

An AI-generated summary is derived presentation. It must be attributable as generated content, regenerable from the permitted counted source set, and subject to safety and quality controls. It does not replace, mutate, or become the canonical original Review. Exact model, provider, prompts, confidence thresholds, and human-review policy remain deferred.

## Privacy / Security

### Public Reviewer Presentation

Public presentation may use a first name, initials, display name, pseudonym, or anonymized presentation according to product and privacy policy. Public exposure of the reviewer's full legal identity is not required. Internal canonical reviewer linkage remains protected and available only as needed for eligibility, moderation, abuse prevention, audit, support, and lawful obligations.

### Private Data

Public Review records and projections must not unnecessarily expose or duplicate:

- payment-provider identifiers or Financial credentials;
- private contact details or exact home address;
- diagnosis, medication, clinical history, or other private health information;
- private DietitianMealPlan or medical documents;
- credential evidence;
- private Conversation content;
- internal fraud signals or moderation notes; or
- secrets, tokens, or security-sensitive identifiers.

Moderation and privacy controls must support redaction or removal of sensitive data while retaining only justified evidence. Public Review access, internal moderation access, provider response, reports, appeals, and administrative actions require separate least-privilege authorization.

### Security Controls

Review operations require authenticated access, typed reviewer resolution, resource and subject authorization, eligibility validation, input validation, safe rendering, rate and abuse controls, CSRF/CORS protections as applicable, moderation audit, and privileged-access controls. Client-submitted reviewer, subject, experience, visibility, or aggregate values are never trusted without backend validation.

Review disputes and moderation should leave room for provider response, reporting, appeal, reasoned removal, and reinstatement where policy permits. This ADR does not hard-code a jurisdiction-specific legal dispute or takedown process.

## Events / Projections

### Canonical Transactions and Transactional Outbox

Important Review lifecycle changes may publish integration intent through the transactional outbox. Conceptual meanings may include Review submitted, published, updated, removed, or responded to. These examples do not finalize event names, payloads, aggregate types, routing, publication criteria, or versions.

Canonical Review mutation and required outbox intent are persisted atomically under ADR-009. Consumers are idempotent and retry-safe. ADR-016 remains authoritative for event versioning and compatibility, and `docs/05-event-contracts.md` owns exact event contracts.

### Eventual Aggregates and Reputation Projections

Aggregate rating, Reputation, reliability, moderation-search, analytics, and public Review projections may update asynchronously after the canonical transaction. Temporary projection lag must not lose, duplicate, or overwrite canonical Review evidence. Projection consumers require idempotency, ordering/staleness handling, observability, replay or rebuild capability, and deterministic inclusion policy.

PostgreSQL remains authoritative. Search indexes may consume published Review projections, and Redis may cache ratings or Reputation, but neither is the Review source of truth. Cache loss and index rebuild must not lose eligibility, Review, moderation, or historical attribution.

This ADR does not introduce OpenSearch or another search dependency solely for Reviews. A new infrastructure dependency requires demonstrated need and a separate approved architecture decision.

### Identifiers and Time

ADR-010 remains authoritative for identifier direction. Review, eligibility, moderation, response, and projection identities must not introduce a second ID strategy, and chronological ordering must not be inferred from UUID value alone.

Submission, edit, moderation, publication, withdrawal, removal, response, and audit timestamps are real instants governed by ADR-011. JVM/server local timezone is not authoritative. Any future business-local review deadline must be defined with explicit timezone semantics and resolved safely rather than inferred from server time.

### Observability

Privacy-minimized operational metrics may include Review submission rate, eligibility conversion, moderation and appeal rate, abuse detection, response rate, rating distributions, provider-caused cancellation/reliability measures, projection lag, rebuild failures, and idempotency conflicts.

Logs and telemetry must not unnecessarily contain full Review text, private moderation evidence, health information, payment identifiers, private Messages, contact details, credentials, or internal fraud features. Correlation and actor references must follow security and retention policy.

## Modular Monolith Boundary

ADR-001 remains authoritative. Review, Rating, moderation, Reputation, and reliability capabilities remain inside the Spring Boot modular monolith with clear domain boundaries and PostgreSQL as the system of record. In-process calls and selective outbox-backed integration are used according to existing architecture.

This ADR does not require a Review microservice, rating microservice, Reputation microservice, fraud microservice, separate Review database, or separate search cluster. Future extraction requires a separate approved decision justified by demonstrated scale, operational isolation, independent deployment, ownership, or security needs.

## No Generic Metadata Escape Hatch

Canonical reviewer identity, verified-experience evidence, Review subject identity, and core authorization relationships must not be implemented through generic `entity_type`, `entity_id`, `metadata JSON`, or `attributes JSON` fields. JSONB may be used only for genuinely extensible, non-relational moderation/provider metadata where justified; it must not replace typed ownership, participant identity, subject identity, source evidence, or referential integrity.

## Consequences

### Positive

- Reviews are tied to real marketplace experiences rather than arbitrary accounts or public comments.
- Fake and unverified Review surface is reduced through typed eligibility and participant validation.
- Actual performer, Organization, operator, property owner, commercial provider, and settlement beneficiary remain distinguishable.
- Multi-Chef Orders preserve Chef-specific and item-specific subject authorization.
- One experience can support several legitimate typed subjects without authorizing unrelated subjects or requiring one giant Review.
- Repeated experiences remain separately attributable.
- Provider-caused failure can inform limited reliability policy without pretending an unperformed service was completed.
- Customer-caused cancellation or no-show does not automatically penalize a provider.
- Ratings remain separate from objective performance, reliability, Refund, and Financial facts.
- Moderation, removal, editing, and provider response do not rewrite owning-domain truth.
- Historical attribution survives membership, staffing, operator, listing, and provider changes.
- Aggregate ratings and Reputation can evolve and be rebuilt from durable evidence.
- No-review state, Review count, rating, reliability, and Reputation retain distinct meanings.
- Privacy-preserving public identity can coexist with secure internal verification and abuse controls.
- The architecture avoids a universal cross-domain Reputation shortcut and premature microservices.

### Negative / Costs

- Typed eligibility and source relationships require deliberate ERD and application design.
- Multi-subject authorization is more complex than attaching one rating to an Order or provider account.
- Historical subject and actual-performer attribution require durable evidence across domain changes.
- Context-specific review windows, cancellation rules, and rating dimensions require explicit policy.
- Duplicate, self-review, collusion, retaliation, coercion, and incentive abuse require layered controls.
- Moderation, reports, appeals, redaction, reinstatement, and response authorization require operational workflows and audit.
- Review editing and removal require history and deterministic aggregate correction without ledger-style immutability.
- Aggregate, Reputation, and reliability projections require replay, rebuild, idempotency, lag monitoring, and typed attribution.
- Privacy-sensitive professional Reviews require careful content controls and restricted moderation access.
- Provider-failure reliability requires accurate cause and actor evidence from owning domains.
- Exact ERD, API, product policy, moderation, event, and UI work remains after this ADR is approved.

## Alternatives Considered / Rejected

### 1. Any Authenticated User May Review Any Provider

Rejected because authentication proves account control, not marketplace participation or subject eligibility, and would enable arbitrary unverified Reviews.

### 2. Payment Receipt Alone Creates Review Eligibility

Rejected because Payment does not prove fulfillment, actual performer, subject relationship, or policy-defined qualifying outcome.

### 3. Conversation Participation Creates Review Eligibility

Rejected because Conversation is communication, not verified service performance or Review authorization.

### 4. One Arbitrary Subject Type Plus UUID Is the Canonical Review Relation

Rejected because it permits dangling and cross-domain-invalid subjects and removes meaningful typed referential integrity.

### 5. A Universal ExperienceRegistry Points to Every Domain

Rejected because registry membership does not prove typed participation, subject eligibility, outcome, ownership, or domain-valid integrity.

### 6. One Giant Nullable-FK Review Table Supports Every Source and Subject

Rejected because it creates sparse rows, invalid combinations, weak constraints, and unsafe evolution across unrelated contexts.

### 7. Auth0 `sub` Is Canonical Reviewer Identity

Rejected because an authentication subject is not the actual Customer, Chef, Dietitian, or other business participant identity.

### 8. Current Organization Membership Determines Historical Review Subject

Rejected because membership changes and cannot explain the actual performer, operator, or subject at experience time.

### 9. Organization Provider Identity Replaces Actual Chef or Dietitian Subject

Rejected because commercial-provider identity does not erase the person who actually performed the service. Separate Organization Reviews may coexist only as separately authorized subjects.

### 10. One Food Order Rating Applies Equally to Every Chef

Rejected because each Chef's participation and items are evidenced separately through the relevant ChefOrderGroup.

### 11. ChefOrderGroup Is the Review Aggregate

Rejected because ChefOrderGroup owns an operational boundary and supplies actual-performer evidence; it is not Review, Rating, moderation, or Reputation.

### 12. Meal Subscription Billing Authorizes Reviews of Unfulfilled Meals

Rejected because future-cycle funding does not prove concrete meal fulfillment or Food experience.

### 13. Unused Kitchen Entitlement Automatically Means Provider Failure

Rejected because voluntary non-use differs from operator inability to provide accepted service.

### 14. Every Cancelled Experience Is Reviewable as Completed Service

Rejected because cancellation does not prove performance of dimensions concerning the unperformed service.

### 15. No Cancelled Experience Is Ever Reviewable

Rejected because verified provider-caused cancellation or no-show may be relevant to limited reliability policy even though service was not completed.

### 16. Rating Equals Service-Performance Fact

Rejected because rating is subjective feedback and cannot alter authoritative fulfillment, Appointment, Delivery, or service evidence.

### 17. Rating Directly Determines Earning or Payout

Rejected because ADR-020 and Financial domains require explicit structured source facts and operations.

### 18. Refund Automatically Deletes or Invalidates Review

Rejected because remediation does not erase a legitimate verified experience or grant editorial control to economic action.

### 19. Provider Payment or Refund Removes a Negative Review

Rejected because pay-to-remove architecture compromises editorial independence and permits coercion.

### 20. Chat Sentiment Automatically Creates Rating or Reputation

Rejected because Message sentiment is unstructured communication, not an explicit authorized Review or structured operational evidence.

### 21. One Universal Reputation Score Covers Every Subject Type

Rejected because Chef quality, Food quality, Kitchen service, Dietitian service, Delivery, Organization reputation, and reliability have different semantics.

### 22. Worker Ratings Automatically Become Organization Rating

Rejected because actual performer and Organization/provider subjects are distinct and may have different evidence and policies.

### 23. Organization Rating Automatically Becomes Each Worker's Rating

Rejected because an Organization-level experience cannot be attributed indiscriminately to every current or former worker.

### 24. Search Index Is the Review Source of Truth

Rejected because search is derived, asynchronous, potentially stale, and rebuildable; PostgreSQL remains authoritative.

### 25. Redis Is the Review Source of Truth

Rejected because Redis is non-authoritative cache/coordination infrastructure and cannot preserve canonical eligibility or Review history.

### 26. Reviews Are Immutable Forever Like Ledger Entries

Rejected because product, moderation, privacy, legal, and editing policy may require update, withdrawal, redaction, removal, or deletion. Required history is durable but not Financial-ledger immutability.

### 27. Removed Reviews Are Immediately Physically Erased in Every Case

Rejected because moderation, abuse, appeal, audit, legal-hold, and aggregate correction may require retained evidence, while lawful deletion remains possible under explicit policy.

### 28. AI Summary Replaces Original Review

Rejected because summaries are derived, may omit nuance, and must remain regenerable without replacing canonical user evidence.

### 29. AI May Invent Reviews or Ratings

Rejected because AI is not the verified participant and cannot fabricate marketplace evidence or user sentiment.

### 30. Review, Rating, Reputation, and Fraud Microservices Are Required for MVP

Rejected because ADR-001 establishes a modular monolith and no demonstrated operational need justifies those services.

## Dependencies / Related ADRs

- **ADR-001 — Modular Monolith First (Accepted):** Review and Reputation remain capabilities inside the modular monolith; no Review, rating, Reputation, or fraud microservice is required.
- **ADR-005 — Order Fulfillment Type Separation (Proposed):** pickup and Delivery fulfillment evidence retains its lane-specific meaning; Review text cannot alter Order state.
- **ADR-006 — Promotion Targeting Model (Accepted):** Promotion ownership/targeting remains separate from Review subjects, and providers cannot purchase positive ratings.
- **ADR-007 — Booking Concurrency Control (Accepted):** KitchenBooking, Space, EquipmentRental, capacity, cancellation, and provider-failure evidence remain owned by booking/Kitchen domains and may support typed eligibility or reliability.
- **ADR-009 — Outbox Table Schema (Accepted):** important Review lifecycle propagation uses accepted transactional-outbox persistence where appropriate.
- **ADR-010 — UUIDv7 Identifier Strategy (Proposed):** Review-related identities follow repository identifier direction; time ordering is never inferred from UUID alone.
- **ADR-011 — Timezone Modeling Strategy (Proposed):** Review lifecycle timestamps are real instants, and any future local deadline requires explicit timezone semantics.
- **ADR-012 — Payment / Marketplace Settlement (Proposed):** Payment and Refund remain separate from Review eligibility, publication, and editorial control.
- **ADR-013 — ChefOrderGroup Aggregate + Financial Boundary (Proposed):** ChefOrderGroup supplies Food actual-performer and item-boundary evidence but is not Review, Rating, or Reputation.
- **ADR-014 — Promotion Engine (Proposed):** Promotion and incentive policy cannot alter rating value or purchase positive sentiment; Review incentives are not required MVP functionality.
- **ADR-015 — Financial Ledger / Reconciliation (Proposed):** Review and rating cannot create or rewrite Ledger facts, earning, Refund, Payout, or reconciliation evidence.
- **ADR-016 — Event Versioning (Accepted):** future Review integration events require explicit compatible versioning; exact contracts remain `docs/05-event-contracts.md` work.
- **ADR-017 — Professional Identity, Credentials and Jurisdiction Eligibility (Proposed):** durable actual-performer identity and Organization authorization support historical subject attribution; reputation does not grant credentials or eligibility.
- **ADR-018 — Dietitian Engagement, Appointment Scheduling and Online Meeting Provisioning (Proposed):** qualifying Appointment and actual-Dietitian evidence support typed eligibility; ADR-023 owns Review and Reputation semantics.
- **ADR-019 — Subscription, Entitlement and Materialized Occurrence Architecture (Proposed):** Meal fulfillment and Kitchen service/availability evidence distinguish performed service, voluntary non-use, and provider failure.
- **ADR-020 — Commercial Obligations, Earning Recognition and Payable-Source Financial Model (Proposed):** structured service and Financial facts remain authoritative; Review and Reputation cannot create commercial or Financial consequences directly.
- **ADR-021 — Authorized Multi-Context Conversation Architecture (Proposed):** Conversation participation, Message content, and sentiment do not create eligibility, Review, rating, reliability, or Reputation.
- **ADR-022 — Platform-Governed Taxonomy and Reference-Data Lifecycle (Proposed):** optional controlled vocabulary may support labels and moderation classification, but ADR-023 owns Review semantics.

No related ADR status is changed by this Proposed ADR.

## Out of Scope

This Proposed ADR does not finalize or introduce:

- a public social feed, follower system, or influencer system;
- arbitrary unverified public Reviews or anonymous unverified marketplace ratings;
- exact star/rating scale, dimension catalog, overall-rating formula, weighting algorithm, confidence model, decay, or universal Reputation formula;
- one cross-domain Reputation score;
- exact eligibility, Review, rating, moderation, response, aggregate, or reliability tables, columns, foreign keys, indexes, constraints, migrations, or SQL;
- exact REST endpoints, OpenAPI schemas, request/response models, review windows, UX, solicitation timing, or response cardinality;
- exact event names, payloads, aggregate types, routing, publication criteria, or versions;
- exact moderation states, reason catalog, SLA, appeal workflow, legal takedown procedure, retention schedule, or physical-deletion rules;
- payroll consequences, Financial settlement changes, automatic payout holds, or rating-driven Financial actions;
- a medical/clinical record Review system or public disclosure of private professional information;
- external review-site syndication, social-media scraping, or imported unverified ratings;
- a required review-incentive program;
- a specific fraud, moderation, AI, search, or analytics provider;
- a Review, rating, Reputation, moderation, or fraud microservice;
- OpenSearch or another search system solely for Reviews;
- a universal ExperienceRegistry or SubjectRegistry;
- an arbitrary polymorphic UUID relationship, giant nullable-FK Review table, or generic metadata identity model; or
- application code, migrations, SQL, API-contract changes, or event-contract changes.

## Implementation / Propagation Notes

This Proposed ADR does not authorize implementation by itself. After approval, canonical propagation and implementation planning must:

1. Reconcile `docs/03-database-erd.md` with typed verified-experience sources, typed reviewer identity, typed Review subjects, eligibility/consumption evidence, Review/version history, moderation/publication concerns, responses, and rebuildable aggregates without a universal registry, arbitrary type-plus-UUID relationship, giant nullable-FK table, or metadata escape hatch.
2. Preserve current canonical product eligibility, including actual completed transactional experience requirements, while defining any future limited provider-failure Review policy explicitly before enabling it.
3. Define domain-valid qualifying outcomes and subject mappings for fulfilled OrderItem, ChefOrderGroup, Delivery, KitchenBooking, EquipmentRental, Dietitian Appointment, MealFulfillmentOccurrence, Kitchen Subscription service evidence, and any future approved source.
4. Resolve authenticated callers to actual Customer, Chef, Dietitian, or other marketplace business identity; never use Auth0 `sub` as canonical reviewer identity.
5. Define database-safe uniqueness, idempotency, transaction, locking/version, and conflict rules for eligibility creation/consumption, Review submission/edit, moderation, removal, response, and projection updates.
6. Define context-specific Review windows, one-Review-per-entitlement policy, edit windows, and repeated-experience behavior without permitting unlimited duplicates from one experience.
7. Preserve ChefOrderGroup actual-performer and OrderItem attribution for multi-Chef Food Orders; one Chef's evidence must not authorize Review of another Chef or their items.
8. Preserve actual Dietitian identity and Appointment evidence while preventing public exposure of health, meal-plan, credential, medical-document, and private Conversation information.
9. Distinguish physical Kitchen, Space, EquipmentRental offering, operating Organization, host service, property owner, and commercial provider; preserve historical operator attribution.
10. Define cancellation/no-show cause and actor inputs so provider failure can be treated under explicit limited policy without marking service completed or penalizing providers for Customer-caused failure.
11. Define subject-specific dimension, scale, validation, overall-rating, and aggregate policy in product/API work without one universal cross-domain score.
12. Separate authoring, publication, moderation, withdrawal, redaction, legal/privacy handling, aggregate countability, and physical deletion where cleaner than one giant state enum.
13. Define provider-response authorization and impersonation prevention using actual responder identity and subject-aware roles/capabilities.
14. Define abuse controls for duplicates, self-review, multi-role conflicts, Organization fabrication, fake accounts, collusion, retaliation, extortion, coercion, and incentives.
15. Preserve Review/Refund and Review/Financial independence; any remediation or Financial action must use explicit owning-domain operations under ADR-012, ADR-015, and ADR-020.
16. Define rebuildable aggregate and typed Reputation/reliability projections, deterministic inclusion/exclusion, no-review presentation, cause attribution, and correction/replay behavior.
17. Define historical subject snapshots/references sufficient to survive Organization membership, staffing, provider, listing, Kitchen operator, and beneficiary changes without silently transferring Reviews.
18. Define public reviewer presentation, privacy minimization, sensitive-content detection, safe rendering, least privilege, moderation audit, lawful retention/deletion, and observability.
19. Update `docs/04-api-contracts.md` only after exact Review, eligibility, moderation, response, aggregate, and error contracts are approved.
20. Update `docs/05-event-contracts.md` only after exact asynchronous needs are approved; use ADR-009 outbox atomicity and ADR-016 event compatibility.
21. Keep PostgreSQL authoritative and make cache, search, public Review, aggregate, Reputation, reliability, analytics, and AI-summary projections idempotent and rebuildable.
22. Define AI assistance as advisory/derived and prohibit invented Reviews, ratings, verified-experience evidence, service facts, Reputation evidence, and Financial decisions.

At minimum, future tests must prove that arbitrary authenticated users cannot review unrelated subjects; Payment and Conversation participation do not create eligibility; reviewer identity resolves to an actual marketplace participant rather than Auth0 `sub`; typed source and subject integrity reject dangling and cross-context references; duplicate and concurrent submissions cannot create prohibited duplicate Reviews or double-consume eligibility; repeated qualifying experiences remain distinguishable; multi-Chef Orders authorize only actual Chef and item subjects; actual Dietitian identity survives Organization context; private health and Conversation data is not exposed; Kitchen operator and property owner remain distinct; Meal billing and voluntary Kitchen non-use do not fabricate performance eligibility or provider failure; Customer-caused failure does not penalize providers; provider-caused failure cannot masquerade as completed service; moderation, edit, withdrawal, redaction, response, and removal update aggregates deterministically; Refund does not automatically invalidate Review; ratings cannot create Financial consequences or rewrite service state; self-review and Organization-fabricated Customer review attempts are rejected; no-review state differs from a numeric average; historical attribution survives membership/operator changes; Conversation sentiment and AI cannot create Reviews; search and Redis remain rebuildable projections; and no universal Reputation score, universal registry, generic metadata relationship, Review microservice, exact API/event schema, or Financial settlement change is introduced.
