# MI-01 — Validation Hypothesis Register

**Version:** 1.1
**Date:** 31 August 2026
**Decision market:** Montreal, Quebec, Canada
**Phase-1 decision scope:** Chef↔Kitchen marketplace — a Chef can find and book a Kitchen space
**Founder-control state:** LOCKED for MI-01; remaining questions are assigned to later validation gates
**Evidence state:** 99 UNTESTED records; no MI-02–MI-12 research performed

> **Control rule:** Version 1.1 does not validate Cheffy Bites. Founder boundaries are locked inputs, not evidence. A commercial Phase-1 GO authorizes movement toward a tested Chef↔Kitchen MVP only; GO/MODIFY/NO-GO does not decide whether the founder continues software creation.

## 1. Executive framing

MI-01 v1.1 reconciles the 88 authoritative v1.0 records, the MI-01B adversarial review and now-completed founder controls. All 99 existing records, IDs, substantive hypothesis texts, priorities, phases, confidence and status values remain unchanged by MI-01D.

The initial business question is narrower than the designed architecture: can Cheffy Bites create a repeatable, operable and potentially monetizable Montreal Chef↔Kitchen booking loop within the founder’s constraints? Customer food ordering, Dietitian services and later features cannot validate or rescue this Phase-1 wedge.

Architecture is evidence of design intent only. Founder decisions are boundary conditions only. Founder software experience and willingness to continue building do not validate any market claim.

## 2. Founder boundary conditions

| Boundary | Founder decision / constraint |
| --- | --- |
| Phase-1 geography | Supply-led. Do not pre-select a Montreal neighbourhood; first map credible existing commercial/shared/ghost Kitchens and real bookable capacity, then select candidate service cells from evidence. |
| Travel boundary | 30 km is the founder's maximum permissible Chef travel-distance hypothesis—not evidence of willingness and not the service-cell radius. Actual distance, time, frequency and burden remain research variables; the cell should narrow where evidence supports it. |
| Initial Chef target | Chefs already using commercial/shared/ghost Kitchens. Their prior rental behaviour reduces one adoption unknown but does not prove a Cheffy Bites need, transaction or repeat. |
| Initial Kitchen supply | Existing commercial/shared/ghost Kitchen operators; do not build new Kitchen capacity for the initial validation cycle. |
| Booking formats | Capture real operator/Chef overlap across hourly, multi-hour, half-day, full-day, recurring weekly/monthly or other actual formats; do not impose one universal unit or price model. |
| Phase-1 product | Chef can find and book a commercial Kitchen space. Phase 1 is a Chef↔Kitchen marketplace, not food ordering. |
| Operator activation | Free or minimal initial operator pricing may be used only for activation/limited expense. Acceptance of free participation is not monetization evidence; real inventory, bookings, repeat inventory and a future-fee/value test are required. |
| Cash budget | External Phase-1 validation spend remains below CAD $2,000, with a working release ceiling of CAD $1,900. Paid translation and other external validation costs count; spending is not automatic. |
| Founder capacity | Up to 24 hours per week for market validation/operations. Founder labour must be logged, categorized and shadow-priced but does not consume the cash-spend cap. |
| Founder technical capability | 20+ years of software-development experience may support execution but is not evidence of demand, supply, liquidity, pricing, monetization or customer purchase. |
| French capability | Founder currently lacks sufficient personal French operating capability and plans translation/translator assistance. French outreach, terms, notices, support, incidents, turnaround, quality, burden and cost remain a Phase-1 operating hypothesis. |
| Window | Up to four months, with earlier stop/kill tests permitted. |
| Phase-2 providers | After a Phase-1 commercial GO and food-selling readiness, validate Independent Chef→Customer and Restaurant/Organization→Customer as separate provider-supply models. One cannot validate the other. |
| Phase-2 restaurant target | West Island, Montreal Restaurants are a founder targeting hypothesis only. Restaurants use the existing Organization concept; delivery/pickup, value, acquisition, incremental orders, cannibalization and economics require later evidence. |
| Future Chef expansion | Phase 2/later may include emerging food entrepreneurs who meet all applicable qualification, food-safety, training, licensing, authorization, insurance and platform requirements. A generic cooking certificate is not sufficient. |
| Later | Dietitians, multi-Chef food orders, advanced subscriptions, promoted visibility and analytics/tools. |
| Decision boundary | MI-01 governs commercial market validation. GO/MODIFY/NO-GO does not decide whether the founder continues learning, architecture work or software development; a commercial NO-GO still cannot be represented as validation. |

These inputs control scope and experiment design; they remain **ASSUMPTIONS/constraints**, not commercial validation.

### 2.1 Initial Chef targeting and discovery control

The first target is existing users of commercial/shared/ghost Kitchens. Prior rental behaviour demonstrates willingness to rent capacity, **not** need for Cheffy Bites.

| Discovery area | What Phase 1 must learn |
| --- | --- |
| Current discovery and relationship | How Kitchens are found/booked; search effort/friction; social/professional referrals; direct recurring agreements; operator relationship quality and satisfaction with current options. |
| Use pattern and reliability | Recurring versus occasional use; current booking frequency; schedule problems; peak-time shortages; cancellation problems; backup-Kitchen needs and capacity shortages. |
| Format, facility and terms | Current prices; preferred booking format; duration; equipment; storage; setup/cleanup; access and operator requirements. |
| Travel behaviour | Actual distance, time, frequency and burden by location/daypart; 30 km remains only the founder maximum. |
| Cheffy Bites behaviour | Whether discovery is material; willingness to use and transact through another marketplace; repeat through Cheffy; bypass risk after meeting an operator. |

### 2.2 Operator evidence ladder

| Stage | Observable progression | Evidence meaning |
| --- | --- | --- |
| 1 | Operator expresses interest | Weak intent signal only. |
| 2 | Supplies actual facility, equipment, terms and availability information | Operational participation signal. |
| 3 | Exposes genuinely bookable capacity | Reliability-screened supply candidate. |
| 4 | Accepts a real qualified Chef request | Match/terms evidence. |
| 5 | A safe, permitted booking occurs | Behavioural transaction evidence. |
| 6 | Provides inventory again | Retention/persistence evidence. |
| 7 | Accepts a plausible continuing-value and future-fee proposition | Monetization evidence; free listing acceptance alone never reaches this step. |

## 3. Validation-stage model

| Validation phase | Decision meaning |
| --- | --- |
| PHASE_1_CHEF_KITCHEN | Validate the booking wedge: need, usable Kitchen supply, service-cell matching, real booking, repeat, economics, continuing value and operability. |
| PHASE_2_CUSTOMER_FOOD | Only after Phase-1 commercial evidence: validate prepared-food demand and two separate provider paths—Independent Chef→Customer and Restaurant/Organization→Customer—including distinct acquisition, operations, trust, fulfilment, retention and economics. |
| LATER | Validate optional extensions independently; they do not rescue or block a Phase-1 decision unless a genuine cross-phase dependency is identified. |
| CROSS_PHASE | Evidence or requirements that affect more than one phase. Cross-phase records block Phase 1 only when explicitly shown as a Phase-1 gate. |

Priority is interpreted **within the validation phase**. A Phase-2 P0 is existential to Phase 2 but does not block the Phase-1 decision. A CROSS_PHASE record blocks Phase 1 only when explicitly included in the Phase-1 P0 dashboard.

### 3.1 Phase-2 provider strategy

| Provider/control | Founder strategy | Later validation requirement |
| --- | --- | --- |
| Independent Chef provider | Qualified independent Chefs selling prepared food after applicable platform, food-safety, licensing/authorization, insurance and other validated requirements. | Customer job, Chef acquisition/activation, menu and provider economics, trust, fulfilment, repeat, fee tolerance and retention. |
| Restaurant / Organization provider | Existing Restaurants participate through the existing Organization business concept; no restaurant-specific architecture is introduced in MI-01. | West Island targeting assumption, onboarding, real menu/availability, integration friction, acquisition cost, platform economics, pickup/delivery, reliability and retention. |
| Separation rule | Do not aggregate the provider paths before analysis. | Measure provider acquisition, capacity, trust, commission tolerance, operations, delivery, repeat and contribution separately. Success of one path does not validate the other. |
| Incrementality rule | Restaurant marketplace orders must be separated from orders that would have occurred through direct website, phone, pickup, delivery, Google or social channels. | Measure genuinely incremental orders and value; migration of existing direct orders into a paid marketplace may create no Restaurant benefit. |

West Island Restaurants are a founder targeting hypothesis only. Restaurants use the existing Organization concept; this document makes no technical-architecture change. Restaurant participation, delivery and pickup are Phase 2 and do not block Phase-1 GO.

## 4. Evidence and status protocol

- Disconfirm first: design each test to expose failure or forced redesign, and preserve negative and null results.
- Separate market sides and phases: Phase-1 booking evidence cannot validate Phase-2 food demand, and later feature demand cannot rescue a failed booking wedge.
- Prefer behaviour to opinion: signed/committed inventory, real requests, deposits, completed bookings and repeat outrank stated interest.
- Measure practical service cells: never use Montreal-wide supply or demand averages as proof of local liquidity.
- Count reliability-adjusted capacity and fully loaded labour/cash cost; listings, GMV and founder time are not free supply or margin.
- Pre-register decisive thresholds, denominators, windows, sources and owners before results are visible.
- Use authoritative primary sources and qualified interpretation for regulation, insurance, tax, privacy and contracting; this document makes no legal conclusions.
- Apply stop rules before spending: an invalidated Phase-1 P0 pauses further product investment pending MODIFY or NO-GO review.

### 4.1 Claim classes

- **FACT:** attributable evidence supports a descriptive claim; product specifications are facts only about intended design.
- **ESTIMATE:** a value derived using a stated method, inputs, range and limitations.
- **ASSUMPTION:** a belief or boundary condition not yet validated.
- **HYPOTHESIS:** a testable, falsifiable proposition linked to a decision.

### 4.2 Status meanings

| Status | Required meaning |
| --- | --- |
| UNTESTED | No credible evidence has been collected against the registered method and falsification criterion. |
| PARTIALLY TESTED | Relevant evidence exists, but behaviour, sample, segment, service cell, window or decision criterion remains incomplete. |
| VALIDATED | The locked criterion is met with attributable Montreal evidence under stated conditions and limitations. |
| INVALIDATED | The falsification criterion is met, or the claim cannot hold under a commercially plausible model within its phase. |

### 4.3 Regulatory/reference worklist — not legal facts

| Question family | Authoritative source family | Control note |
| --- | --- | --- |
| Phase-1 food-premises/Kitchen requirements | Quebec government / MAPAQ primary sources | MI-01B alert; verify applicability to booking-only model and each operator. |
| Municipal occupancy/use requirements | Ville de Montréal primary sources | MI-01B alert; verify per selected facility/borough. |
| Food-safety training | Quebec government / MAPAQ primary sources | Likely operator/Chef qualification dependency; do not assume uniform applicability. |
| French-language obligations | Office québécois de la langue française and Quebec legislation | Phase-1 operability alert; verify exact notices, contracts and public-facing duties. |
| GST/QST and platform reporting | Revenu Québec and Canada Revenue Agency | Separate booking revenue/fees from later food-sale obligations. |
| Consumer remedies / merchant role | Office de la protection du consommateur and qualified counsel | Verify only against the chosen contracting/payment model. |
| Privacy | Commission d’accès à l’information du Québec | Phase-1 account/payment data first; Dietitian/health data remains later. |

MI-01B supplied the following reference leads. They are preserved for later primary-source verification and are not adopted as legal conclusions:

- [Quebec food permits](https://www.quebec.ca/entreprises-et-travailleurs-autonomes/industrie-bioalimentaire/permis-alimentaires)
- [Quebec preparation/restaurant/retail permit](https://www.quebec.ca/entreprises-et-travailleurs-autonomes/industrie-bioalimentaire/permis-alimentaires/permis-restauration-vente-detail)
- [Ville de Montréal occupancy permit](https://montreal.ca/en/how-to/get-occupancy-permit-commercial-industrial-or-professional-activity)
- [Quebec food hygiene/safety training](https://www.quebec.ca/en/health/nutrition/food-safety-risk-prevention/food-establishments/hygiene-cleaning/mandatory-food-hygiene-safety-training/recognition-equivalents)
- [Charter of the French language](https://www.legisquebec.gouv.qc.ca/en/document/cs/c-11)
- [Revenu Québec GST/QST registration](https://www.revenuquebec.ca/en/businesses/consumption-taxes/gsthst-and-qst/registering-for-the-gst-and-qst/)
- [CRA digital-platform reporting guidance](https://www.canada.ca/en/revenue-agency/programs/about-canada-revenue-agency-cra/compliance/reporting-rules-digital-platforms/guidance-on-reporting-rules.html)
- [Quebec consumer online-purchase cancellation](https://www.opc.gouv.qc.ca/en/consumer/topic/purchase/online-purchase/cancelling)
- [Quebec privacy/Law 25 changes](https://www.cai.gouv.qc.ca/protection-renseignements-personnels/sujets-et-domaines-dinteret/principaux-changements-loi-25)

## 5. Revised full hypothesis register

**Register size:** 99 records = 88 preserved v1.0 records + 11 new v1.1 records. All begin UNTESTED unless a future evidence update records otherwise.

### 5.1 Customer demand, retention and geographic concentration

**Domain rule:** Phase-2 customer-food claims; none validates Phase 1.

#### CUST-001 — Customer demand

| Field | Registered control |
| --- | --- |
| ID | CUST-001 |
| Market Side | Customer demand |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | A reachable Montreal segment has a frequent, important job that independent-Chef prepared food solves better than its current routine. |
| Why It Matters | Without a high-intensity job, interest will remain curiosity rather than recurring purchase behaviour. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Problem interviews followed by a realistic landing-page and deposit-backed preorder test; segment results before pooling. |
| Evidence Needed | Observed workaround, problem frequency, urgency, conversion to a paid or deposit-backed action, and stated switching trigger. |
| Falsification Criterion | Target users report low urgency/frequency and do not take a paid or deposit-backed action at realistic terms. |
| Montreal-Specific Consideration | Test practical neighbourhood clusters and French/English journeys separately; do not infer from a city-wide average. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-05 Customer Discovery |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### CUST-002 — Customer demand

| Field | Registered control |
| --- | --- |
| ID | CUST-002 |
| Market Side | Customer demand |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | At least one target segment prefers Cheffy Bites to restaurants, delivery aggregators, meal kits, grocery prepared food, caterers, private cooks, and informal social sellers for a specific occasion. |
| Why It Matters | The marketplace must win a real occasion, not merely be liked in the abstract. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Forced-choice interviews, substitute mapping, concept comparison, and controlled preorder pages with realistic alternatives. |
| Evidence Needed | Choice share by occasion, reason for choice, current spend, switching barriers, and actual purchase action. |
| Falsification Criterion | No segment/occasion produces a repeatable switching reason or behavioural preference over existing alternatives. |
| Montreal-Specific Consideration | Include culturally specific and mainstream substitutes used within each tested Montreal cluster. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-03 + MI-05 |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### CUST-003 — Customer demand

| Field | Registered control |
| --- | --- |
| ID | CUST-003 |
| Market Side | Customer demand |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | The leading purchase driver can be isolated (for example convenience, home-style authenticity, dietary fit, health, price, variety, customization, or connection) and is strong enough to anchor positioning. |
| Why It Matters | A diffuse value proposition raises acquisition cost and prevents a focused launch wedge. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | HIGH |
| Validation Method | Jobs-to-be-done interviews, ranked trade-offs, message tests, and segment-specific preorder experiments. |
| Evidence Needed | Ranked drivers tied to past behaviour, message-level conversion, and reasons for non-purchase. |
| Falsification Criterion | No driver consistently predicts action, or the winning driver is already better served by substitutes. |
| Montreal-Specific Consideration | Run French and English message variants and check whether driver rankings vary materially by cluster/community. |
| Priority | P1 |
| Status | UNTESTED |
| Primary Evidence Module | MI-05 + MI-08 |
| Version Control | v1.0; v1.0 priority P1; Source priority retained; interpreted within the assigned validation phase. |

#### CUST-004 — Retention / repeat purchase

| Field | Registered control |
| --- | --- |
| ID | CUST-004 |
| Market Side | Retention / repeat purchase |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | Customers who receive a satisfactory first order repeat often enough to support acquisition payback without perpetual discounts. |
| Why It Matters | Repeat purchase is the core demand and economics test; one-off novelty cannot sustain the marketplace. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Concierge pilot with cohort tracking, no forced subscription, and a controlled removal of first-order incentives. |
| Evidence Needed | Second/third purchase timing, cohort repeat, reason for churn, discount dependence, and orders per customer-month. |
| Falsification Criterion | Repeat remains below the level required by the MI-09 payback model after service failures and launch incentives are normalized. |
| Montreal-Specific Consideration | Track by cluster, cuisine/need-state, fulfilment mode, and season rather than reporting one Montreal average. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-05 + MI-09 |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### CUST-005 — Customer demand / pricing

| Field | Registered control |
| --- | --- |
| ID | CUST-005 |
| Market Side | Customer demand / pricing |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | Customers will accept the full checkout total - food, tax, platform/service fee, and pickup or delivery cost - at a basket that permits positive contribution margin. |
| Why It Matters | Food-price interest can disappear when fees are shown; hidden fee intolerance can make the model uneconomic. |
| Current Classification | HYPOTHESIS |
| Current Evidence | The product specification leaves commercial terms configurable/TBD; no willingness-to-pay evidence was supplied. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Van Westendorp/Gabor-Granger as directional work, then real checkout/preorder tests with fully disclosed totals. |
| Evidence Needed | Conversion and abandonment at realistic all-in totals, fee salience, basket changes, and pickup substitution. |
| Falsification Criterion | Conversion collapses at the minimum all-in price required for viable provider and platform economics. |
| Montreal-Specific Consideration | Test realistic taxes/fees and travel times for the selected cluster; do not ask only about menu price. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-08 Pricing / WTP |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### CUST-006 — Customer demand

| Field | Registered control |
| --- | --- |
| ID | CUST-006 |
| Market Side | Customer demand |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | Visible Chef identity and story increase trust, conversion, retention, or willingness to pay versus a generic prepared-food listing. |
| Why It Matters | Individual Chef identity is architecturally central but could add complexity without commercial value. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | HIGH |
| Validation Method | A/B concept and listing tests with identity prominent, neutral, and absent; follow with pilot cohort analysis. |
| Evidence Needed | Incremental conversion, basket value, repeat, saved-Chef behaviour, and trust rating attributable to identity treatment. |
| Falsification Criterion | Chef identity has no meaningful behavioural lift or reduces trust relative to a standardized provider brand. |
| Montreal-Specific Consideration | Test recognition and resonance across Montreal language and cuisine communities; avoid celebrity-Chef bias. |
| Priority | P1 |
| Status | UNTESTED |
| Primary Evidence Module | MI-05 Customer Discovery |
| Version Control | v1.0; v1.0 priority P1; Source priority retained; interpreted within the assigned validation phase. |

#### CUST-007 — Customer demand

| Field | Registered control |
| --- | --- |
| ID | CUST-007 |
| Market Side | Customer demand |
| Validation Phase | LATER |
| Hypothesis | Multi-Chef ordering within one physical Kitchen solves a meaningful customer job and increases basket size or conversion enough to justify coordination complexity. |
| Why It Matters | The one-Kitchen/multi-Chef rule is a distinctive feature, but its benefit is unproven and operationally costly. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | MEDIUM |
| Validation Method | Choice experiment and pilot comparing single-Chef menus with coordinated multi-Chef menus at the same total fulfilment terms. |
| Evidence Needed | Usage rate, incremental items/AOV, conversion, preparation delay, cancellations, and customer comprehension. |
| Falsification Criterion | Customers rarely combine Chefs or any basket lift is outweighed by coordination failures and slower fulfilment. |
| Montreal-Specific Consideration | Test only where multiple active Chefs are genuinely co-located; do not show a fictional city-wide assortment. |
| Priority | P2 |
| Status | UNTESTED |
| Primary Evidence Module | MI-05 + MI-10 |
| Version Control | v1.0; v1.0 priority P2; Source priority retained; interpreted within the assigned validation phase. |

#### CUST-008 — Delivery / pickup behaviour

| Field | Registered control |
| --- | --- |
| ID | CUST-008 |
| Market Side | Delivery / pickup behaviour |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | A material segment will preorder for scheduled pickup or delivery rather than require restaurant-style on-demand fulfilment. |
| Why It Matters | Scheduled demand may be necessary for independent-Chef production and acceptable delivery density. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | HIGH |
| Validation Method | Offer controlled fulfilment windows in a pilot and compare conversion, punctuality tolerance, and repeat by mode. |
| Evidence Needed | Window selection, lead time, abandonment, late-order demand, satisfaction, and repeat by scheduled/on-demand framing. |
| Falsification Criterion | Demand concentrates on immediate delivery and falls below viability when advance ordering or windows are required. |
| Montreal-Specific Consideration | Test weekday lunch, weekday dinner, and weekend occasions in distinct clusters; include winter conditions later. |
| Priority | P1 |
| Status | UNTESTED |
| Primary Evidence Module | MI-05 + MI-10 |
| Version Control | v1.0; v1.0 priority P1; Source priority retained; interpreted within the assigned validation phase. |

#### CUST-009 — Geographic density

| Field | Registered control |
| --- | --- |
| ID | CUST-009 |
| Market Side | Geographic density |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | Demand can be concentrated within at least one practical Montreal service cluster rather than being thinly dispersed across the city. |
| Why It Matters | A marketplace can show city-wide interest while failing locally on choice, wait time, and delivery cost. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Geocoded waitlist/preorder test followed by one-cluster concierge launch; analyze travel time and orders per service window. |
| Evidence Needed | Qualified demand density, conversion, active customers, order timing, and repeat within a defined travel-time boundary. |
| Falsification Criterion | No candidate cluster produces enough concurrent paid demand to support minimum Chef and fulfilment economics. |
| Montreal-Specific Consideration | Define clusters from travel time, demand, Kitchen locations, and community patterns in MI-02; do not preselect by founder preference alone. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-04 + MI-05 + MI-10 |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### CUST-010 — Customer demand

| Field | Registered control |
| --- | --- |
| ID | CUST-010 |
| Market Side | Customer demand |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | Cheffy Bites can communicate its offer and trust model clearly across Montreal's relevant language journeys without fragmenting operations or brand meaning. |
| Why It Matters | Confusion about who cooks, where food is made, and who is accountable can suppress conversion and trust. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | HIGH |
| Validation Method | Bilingual comprehension interviews and first-click/checkout usability tests using identical commercial terms. |
| Evidence Needed | Correct understanding of provider, Kitchen, fees, fulfilment, complaint path, and claim provenance. |
| Falsification Criterion | Material segments misidentify the seller/accountable party or require incompatible value propositions/processes. |
| Montreal-Specific Consideration | Recruit in French and English and record language preference; translation quality alone is not the test. |
| Priority | P1 |
| Status | UNTESTED |
| Primary Evidence Module | MI-05 Customer Discovery |
| Version Control | v1.0; v1.0 priority P1; Source priority retained; interpreted within the assigned validation phase. |

### 5.2 Chef supply

**Domain rule:** Separate Kitchen-booking demand from later customer-acquisition and food-fulfilment claims.

#### CHEF-001 — Independent Chef supply

| Field | Registered control |
| --- | --- |
| ID | CHEF-001 |
| Market Side | Independent Chef supply |
| Validation Phase | PHASE_1_CHEF_KITCHEN |
| Hypothesis | A sufficient pool of qualified independent Chefs has recurring difficulty obtaining compliant commercial Kitchen capacity at useful times. |
| Why It Matters | If Kitchen access is not a severe constraint, the Chef-Kitchen marketplace has weak supply-side demand. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Structured Chef interviews plus evidence review of recent searches, bookings, waitlists, and rejected opportunities. |
| Evidence Needed | Frequency, duration, timing and cost of access problems; current workaround; lost revenue attributable to capacity. |
| Falsification Criterion | Qualified Chefs generally have adequate access, or unmet need occurs only at commercially unusable times. |
| Montreal-Specific Consideration | Sample by cuisine/equipment need, operating stage, language, and target cluster; distinguish home sellers from qualified commercial operators. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-06 Chef Discovery |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### CHEF-002 — Independent Chef supply

| Field | Registered control |
| --- | --- |
| ID | CHEF-002 |
| Market Side | Independent Chef supply |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | Customer acquisition is a costly, persistent pain for qualified independent Chefs, and they want incremental marketplace demand. |
| Why It Matters | Chefs with strong direct channels may resist commissions, standardization, and platform dependence. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Interviews grounded in recent acquisition behaviour, channel-cost diaries, and a waitlist/onboarding commitment test. |
| Evidence Needed | Acquisition channels, time and cash cost, unused production capacity, desired order volume, and commitment to a pilot. |
| Falsification Criterion | Most qualified Chefs have sufficient demand or will not commit inventory even when incremental orders are offered. |
| Montreal-Specific Consideration | Compare community/social sellers, caterers, meal-prep operators, and newer professionals within the same cluster. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-06 Chef Discovery |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### CHEF-003 — Independent Chef supply

| Field | Registered control |
| --- | --- |
| ID | CHEF-003 |
| Market Side | Independent Chef supply |
| Validation Phase | CROSS_PHASE |
| Hypothesis | Qualified Chefs will accept marketplace onboarding, food-safety evidence, identity disclosure, menu standards, order processes, and review accountability. |
| Why It Matters | Nominal interest is irrelevant if compliance and workflow requirements prevent activation. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Progressive onboarding test that reveals requirements before asking for a signed pilot commitment. |
| Evidence Needed | Completion by step, document availability, objections, time-to-activate, and drop-off reason. |
| Falsification Criterion | A majority of otherwise viable pilot candidates refuse or cannot complete critical qualification and process steps. |
| Montreal-Specific Consideration | Test requirements against Quebec/Montreal rules only after authoritative verification; provide French/English onboarding. |
| Priority | P1 |
| Status | UNTESTED |
| Primary Evidence Module | MI-06 + regulatory workstream |
| Version Control | v1.0; v1.0 priority P0; Priority changed for phase-relative control. |

#### CHEF-004 — Independent Chef supply / Kitchen demand

| Field | Registered control |
| --- | --- |
| ID | CHEF-004 |
| Market Side | Independent Chef supply / Kitchen demand |
| Validation Phase | PHASE_1_CHEF_KITCHEN |
| Hypothesis | Chefs will purchase Kitchen capacity in a workable format (hourly, recurring block, or subscription) and at a total cost compatible with menu economics. |
| Why It Matters | Kitchen demand can exist but still fail because booking units, timing, or price do not match production economics. |
| Current Classification | HYPOTHESIS |
| Current Evidence | The product specification leaves commercial terms configurable/TBD; no willingness-to-pay evidence was supplied. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Choice-based pricing test using real Kitchen options, followed by refundable booking deposits for preferred formats. |
| Evidence Needed | Preferred unit, hours/month, lead time, cancellation needs, equipment/storage needs, deposit conversion, and maximum viable cost per production run. |
| Falsification Criterion | No format produces credible commitments at a price acceptable to Kitchen operators and viable for Chef gross margin. |
| Montreal-Specific Consideration | Use actual candidate Kitchen travel times and equipment packages in the selected Montreal cluster. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-06 + MI-08 |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### CHEF-005 — Independent Chef supply / Monetization

| Field | Registered control |
| --- | --- |
| ID | CHEF-005 |
| Market Side | Independent Chef supply / Monetization |
| Validation Phase | PHASE_1_CHEF_KITCHEN |
| Hypothesis | Chefs accept a platform commission or fee when total incremental contribution and service value exceed their direct-channel alternative. |
| Why It Matters | A take rate that funds the platform may destroy Chef participation or be passed into uncompetitive prices. |
| Current Classification | HYPOTHESIS |
| Current Evidence | The product specification leaves commercial terms configurable/TBD; no willingness-to-pay evidence was supplied. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Fee-card choice test, open-book menu model, and pilot settlement statement using actual orders and all deductions. |
| Evidence Needed | Acceptance, menu repricing, contribution per order, churn intent, and objections at different service bundles. |
| Falsification Criterion | The minimum viable platform fee leaves Chef economics unattractive or triggers material supply withdrawal. |
| Montreal-Specific Consideration | Compare independent Chefs with Organization-operated providers; the fee payer and beneficiary may differ. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-08 + MI-09 |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### CHEF-006 — Chef/provider economics

| Field | Registered control |
| --- | --- |
| ID | CHEF-006 |
| Market Side | Chef/provider economics |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | A focused menu can generate enough contribution per production hour and minimum order volume to justify Kitchen, labour, packaging, waste, platform, and fulfilment costs. |
| Why It Matters | Gross sales can look attractive while provider contribution is negative. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Build bottom-up menu models with participating Chefs, then validate against a paid production pilot and waste log. |
| Evidence Needed | Ingredient, packaging, labour, Kitchen/equipment, waste, fee, refund, and fulfilment cost per item/run; capacity per hour. |
| Falsification Criterion | Observed provider contribution is non-positive or requires order volumes that the cluster cannot produce. |
| Montreal-Specific Consideration | Model Quebec labour/tax implications only after expert input; use actual Montreal supplier and Kitchen quotes in MI-09. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-06 + MI-09 |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### CHEF-007 — Chef operations

| Field | Registered control |
| --- | --- |
| ID | CHEF-007 |
| Market Side | Chef operations |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | Chefs can reliably accept, prepare, stage, and hand off pickup/delivery orders within defined windows without harming existing business. |
| Why It Matters | Supply that cannot execute reliably creates refunds, trust loss, and marketplace churn. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | HIGH |
| Validation Method | Shadow workflow and limited paid pilot with timestamped acceptance, preparation, staging, handoff, and exception logs. |
| Evidence Needed | Acceptance rate, prep variance, capacity, lateness, cancellations, handoff errors, and incremental labour. |
| Falsification Criterion | Reliability remains below the service level required for repeat demand or requires uneconomic staffing. |
| Montreal-Specific Consideration | Test in real shared-Kitchen conditions and at the dayparts the target Montreal cluster demands. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-06 + MI-10 |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### CHEF-008 — Organization-affiliated Chef supply

| Field | Registered control |
| --- | --- |
| ID | CHEF-008 |
| Market Side | Organization-affiliated Chef supply |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | Food-provider Organizations will list offerings while preserving individual Chef identity and operational accountability. |
| Why It Matters | Organization supply may scale faster, but provider/payee rules and Chef identity could create governance friction. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | HIGH |
| Validation Method | Interview Organization decision-makers and working Chefs separately; test a responsibility and settlement workflow. |
| Evidence Needed | Decision authority, approval cycle, performer visibility, data/reporting needs, settlement preference, and willingness to pilot. |
| Falsification Criterion | Organizations reject performer-level identity/accountability or the model creates incompatible control and settlement expectations. |
| Montreal-Specific Consideration | Include small caterers/meal-prep businesses and institutional/community organizations operating in Montreal. |
| Priority | P1 |
| Status | UNTESTED |
| Primary Evidence Module | MI-06 Chef Discovery |
| Version Control | v1.0; v1.0 priority P1; Source priority retained; interpreted within the assigned validation phase. |

#### CHEF-009 — Chef supply retention

| Field | Registered control |
| --- | --- |
| ID | CHEF-009 |
| Market Side | Chef supply retention |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | Activated Chefs will reserve availability and remain active when early order flow is uneven, provided expectations and economics are transparent. |
| Why It Matters | Cold-start supply can disappear before demand compounds, making apparent acquisition counts misleading. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Pilot with explicit availability commitments, transparent demand forecasts, and weekly activity/churn interviews. |
| Evidence Needed | Active weeks, listed hours/items, acceptance, order fill, churn reason, and minimum acceptable volume. |
| Falsification Criterion | Chefs stop listing or cancel capacity before the cluster reaches demand density under a financially tolerable activation plan. |
| Montreal-Specific Consideration | Measure active supply by service window and cluster, not cumulative onboarded Chefs across Montreal. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-06 + MI-10 |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### CHEF-010 — Chef supply / retention

| Field | Registered control |
| --- | --- |
| ID | CHEF-010 |
| Market Side | Chef supply / retention |
| Validation Phase | PHASE_1_CHEF_KITCHEN |
| Hypothesis | Qualified Montreal Chefs who complete an initial Kitchen booking make repeat bookings and remain active when capacity, terms, access and support meet their operating needs. |
| Why It Matters | A booking marketplace cannot survive on one-time trials; repeat Chef demand is required for liquidity, acquisition payback and credible operator participation. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal evidence has been collected; the record was added in v1.1 as a validation-control gap. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Track cohorts from qualified inquiry through first completed booking and a pre-registered repeat window; interview non-repeaters and test one controlled rebooking offer without discount dependence. |
| Evidence Needed | Eligible-Chef denominator, first-booking completion, repeat booking, time to repeat, reasons for non-repeat and continued availability. |
| Falsification Criterion | Completed-booking Chefs do not rebook within the locked observation window at a rate compatible with the Phase-1 economic model, or repeat requires uneconomic founder effort or subsidy. |
| Montreal-Specific Consideration | Measure within the same practical service cells; seasonal or event-only use must not be generalized to recurring Montreal demand. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-06 + MI-10 |
| Version Control | v1.1 NEW; v1.0 priority N/A; New v1.1 record created to close a verified control gap. |

### 5.3 Kitchen supply

**Domain rule:** Count only operator-authorized, compatible and reliability-adjusted capacity.

#### KIT-001 — Kitchen supply

| Field | Registered control |
| --- | --- |
| ID | KIT-001 |
| Market Side | Kitchen supply |
| Validation Phase | PHASE_1_CHEF_KITCHEN |
| Hypothesis | Montreal has meaningful unused licensed commercial Kitchen space/unit capacity that is actually available for third-party commercial use. |
| Why It Matters | A Kitchen's existence does not establish rentable capacity; without it the Chef-Kitchen marketplace cannot operate as designed. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Operator inventory interviews, schedule evidence, site verification, and a request for bookable pilot slots. |
| Evidence Needed | Qualified Kitchen/space count, rentable hours, authority to rent, licence/insurance status, and signed pilot availability. |
| Falsification Criterion | Few operators can identify and commit compliant third-party capacity after ownership, licensing and operating authority are checked. |
| Montreal-Specific Consideration | Map supply by travel-time cluster and space type; do not count restaurants or facilities without confirmed external-use authority. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-07 Kitchen Operator Discovery |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### KIT-002 — Kitchen supply / Liquidity

| Field | Registered control |
| --- | --- |
| ID | KIT-002 |
| Market Side | Kitchen supply / Liquidity |
| Validation Phase | PHASE_1_CHEF_KITCHEN |
| Hypothesis | Unused Kitchen capacity occurs at times that overlap Chef production needs and customer demand windows. |
| Why It Matters | Off-hour capacity is commercially useless if it cannot support preparation, staging and fulfilment. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Overlay operator slot calendars with Chef requested production windows and customer demand windows. |
| Evidence Needed | Bookable hours by daypart, setup/cleaning time, access limits, storage, handoff hours, and overlap rate. |
| Falsification Criterion | Available slots have insufficient overlap with economically viable production and fulfilment windows. |
| Montreal-Specific Consideration | Evaluate weekdays/weekends and lunch/dinner separately within each candidate Montreal cluster. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-07 + MI-10 |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### KIT-003 — Kitchen supply

| Field | Registered control |
| --- | --- |
| ID | KIT-003 |
| Market Side | Kitchen supply |
| Validation Phase | PHASE_1_CHEF_KITCHEN |
| Hypothesis | Authorized Kitchen operators are willing to host external independent and Organization-affiliated Chefs under standardized marketplace terms. |
| Why It Matters | Risk, reputation, access control, or competitive concerns may outweigh utilization revenue. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Risk-first interviews followed by a term-sheet and pilot-slot commitment rather than an interest survey. |
| Evidence Needed | Objections, approval authority, acceptable Chef criteria, contract needs, deposit/commitment, and reasons for refusal. |
| Falsification Criterion | Operators express interest but will not commit a real slot once liability, access, cleaning and food-safety terms are disclosed. |
| Montreal-Specific Consideration | Include different facility/operator types and both French/English contracting journeys. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-07 Kitchen Operator Discovery |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### KIT-004 — Kitchen economics

| Field | Registered control |
| --- | --- |
| ID | KIT-004 |
| Market Side | Kitchen economics |
| Validation Phase | PHASE_1_CHEF_KITCHEN |
| Hypothesis | Hourly rates, minimum bookings, cleaning time/fees, deposits and equipment charges can satisfy operators while remaining viable for Chefs. |
| Why It Matters | A two-sided price gap can invalidate the Kitchen marketplace even when both sides report need. |
| Current Classification | HYPOTHESIS |
| Current Evidence | The product specification leaves commercial terms configurable/TBD; no willingness-to-pay evidence was supplied. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Collect real quotes and minimum terms; run bilateral choice tests and attempt paid/refundable pilot bookings. |
| Evidence Needed | Operator floor price and incremental cost, Chef ceiling price, minimum duration, cleaning treatment, deposit and utilization uplift. |
| Falsification Criterion | No overlap exists between operator-required economics and Chef viable production cost for target menus. |
| Montreal-Specific Consideration | Use actual Montreal candidate facilities and local travel/setup burden; avoid generic hourly-rate questions. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-07 + MI-08 + MI-09 |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### KIT-005 — Kitchen operations

| Field | Registered control |
| --- | --- |
| ID | KIT-005 |
| Market Side | Kitchen operations |
| Validation Phase | PHASE_1_CHEF_KITCHEN |
| Hypothesis | Available spaces have the equipment, storage, utilities, receiving, packaging and staging capabilities required by target Chef menus. |
| Why It Matters | Nominal capacity may not be operationally substitutable across cuisines or production models. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | HIGH |
| Validation Method | Site walkthrough with a standardized capability checklist and a supervised production trial. |
| Evidence Needed | Equipment quantity/condition, storage, ventilation, allergens, receiving, waste, staging, cleaning and access constraints. |
| Falsification Criterion | Most committed slots cannot support the target menus without capital additions or material operational restrictions. |
| Montreal-Specific Consideration | Segment by production archetype and cuisine/equipment need; map only capabilities relevant to the chosen Montreal wedge. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-07 Kitchen Operator Discovery |
| Version Control | v1.0; v1.0 priority P1; Priority changed for phase-relative control. |

#### KIT-006 — Kitchen risk / Trust

| Field | Registered control |
| --- | --- |
| ID | KIT-006 |
| Market Side | Kitchen risk / Trust |
| Validation Phase | PHASE_1_CHEF_KITCHEN |
| Hypothesis | Insurance, liability allocation, food-safety controls and incident handling can be made acceptable to operators and providers at tolerable cost. |
| Why It Matters | Operators may reject external use even with unused capacity if downside risk is unclear or uninsurable. |
| Current Classification | ASSUMPTION |
| Current Evidence | The specification flags this as an unresolved launch gate; no authoritative Quebec or municipal verification has been completed. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Operator interviews plus broker/legal/regulatory primary-source review and indicative insurance quotes. |
| Evidence Needed | Required coverage, exclusions, incremental premiums, indemnities, audit/monitoring needs, and operator acceptance. |
| Falsification Criterion | Required controls or insurance are unavailable, unaffordable, or unacceptable to enough operators for cluster liquidity. |
| Montreal-Specific Consideration | Verify Quebec and municipal requirements authoritatively; do not generalize from another Canadian city. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-07 + regulatory workstream |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### KIT-007 — Kitchen operations

| Field | Registered control |
| --- | --- |
| ID | KIT-007 |
| Market Side | Kitchen operations |
| Validation Phase | PHASE_1_CHEF_KITCHEN |
| Hypothesis | Scheduling, access, cleaning verification, equipment turnover and operator support can be administered without erasing utilization gains. |
| Why It Matters | Transaction friction and staffing can make apparently incremental revenue unprofitable. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | HIGH |
| Validation Method | Time-and-motion pilot across booking, access, changeover, exception and closeout; operator cost diary. |
| Evidence Needed | Staff minutes/booking, exception rate, cleaning failures, access incidents, schedule changes and net operator contribution. |
| Falsification Criterion | Incremental admin/staffing and disruption consume the utilization revenue or cause unacceptable operational risk. |
| Montreal-Specific Consideration | Test at the exact access hours proposed for the Montreal launch cluster, including unstaffed/after-hours scenarios if relevant. |
| Priority | P1 |
| Status | UNTESTED |
| Primary Evidence Module | MI-07 + MI-09 |
| Version Control | v1.0; v1.0 priority P1; Source priority retained; interpreted within the assigned validation phase. |

#### KIT-008 — Kitchen supply / Subscription

| Field | Registered control |
| --- | --- |
| ID | KIT-008 |
| Market Side | Kitchen supply / Subscription |
| Validation Phase | PHASE_1_CHEF_KITCHEN |
| Hypothesis | Recurring blocks or subscription-style entitlements improve operator utilization and Chef planning without creating oversubscription or slot-guarantee conflict. |
| Why It Matters | Recurring economics could stabilize supply, but the product design may be more complex than the market needs. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | MEDIUM |
| Validation Method | Offer hourly, recurring-block and entitlement concepts to matched Chef/operator pairs; test signed reservations before building subscription logic. |
| Evidence Needed | Preference, commitment length, cancellation/rollover needs, capacity conflict, and incremental utilization. |
| Falsification Criterion | Parties prefer simple bookings, or recurring products create unacceptable capacity promises and remediation exposure. |
| Montreal-Specific Consideration | Test seasonal and daypart fit in candidate clusters before extrapolating to the rest of Montreal. |
| Priority | P1 |
| Status | UNTESTED |
| Primary Evidence Module | MI-07 + MI-08 |
| Version Control | v1.0; v1.0 priority P2; Priority changed for phase-relative control. |

#### KIT-009 — Kitchen supply / reliability

| Field | Registered control |
| --- | --- |
| ID | KIT-009 |
| Market Side | Kitchen supply / reliability |
| Validation Phase | PHASE_1_CHEF_KITCHEN |
| Hypothesis | Advertised Kitchen availability remains confirmed, accessible and usable when a qualified Chef attempts to book it; reliability-adjusted supply is sufficient for Phase-1 matching. |
| Why It Matters | Listed hours are not marketplace supply if operators withdraw them, change terms, deny access, or provide unusable equipment when requested. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal evidence has been collected; the record was added in v1.1 as a validation-control gap. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Create real availability calendars, reconfirm slots, attempt manual bookings and record availability loss at inquiry, confirmation and arrival. |
| Evidence Needed | Advertised hours, confirmed hours, completed usable hours, reason-coded loss, access failures and equipment/service discrepancies. |
| Falsification Criterion | Reliability-adjusted capacity is too small or unpredictable to meet the locked service-cell match requirement even when headline inventory appears adequate. |
| Montreal-Specific Consideration | Measure by physical Kitchen, daypart and equipment bundle; do not pool peak and off-peak inventory across Montreal. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-07 + MI-10 |
| Version Control | v1.1 NEW; v1.0 priority N/A; New v1.1 record created to close a verified control gap. |

#### KIT-010 — Kitchen supply / concentration

| Field | Registered control |
| --- | --- |
| ID | KIT-010 |
| Market Side | Kitchen supply / concentration |
| Validation Phase | PHASE_1_CHEF_KITCHEN |
| Hypothesis | Phase-1 usable Kitchen capacity is not so concentrated in one operator, facility, schedule or equipment bundle that a single withdrawal makes the pilot non-viable. |
| Why It Matters | Apparent liquidity may depend on one cooperative operator and collapse when that operator changes terms or leaves. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal evidence has been collected; the record was added in v1.1 as a validation-control gap. |
| Confidence | VERY LOW |
| Business Impact if False | HIGH |
| Validation Method | Calculate capacity and completed bookings by operator/facility/service cell; run a loss-of-largest-supplier scenario. |
| Evidence Needed | Share of reliability-adjusted hours and completed bookings by operator, plus alternative capacity that can absorb displaced demand. |
| Falsification Criterion | Removing the largest operator or facility eliminates practical matching with no credible replacement inside the four-month window. |
| Montreal-Specific Consideration | Concentration must be assessed inside the chosen Montreal cluster, not against city-wide facility counts. |
| Priority | P1 |
| Status | UNTESTED |
| Primary Evidence Module | MI-07 + MI-10 |
| Version Control | v1.1 NEW; v1.0 priority N/A; New v1.1 record created to close a verified control gap. |

#### KIT-011 — Kitchen supply / retention

| Field | Registered control |
| --- | --- |
| ID | KIT-011 |
| Market Side | Kitchen supply / retention |
| Validation Phase | PHASE_1_CHEF_KITCHEN |
| Hypothesis | Operators continue exposing genuine bookable capacity after initial onboarding and completed bookings under the proposed workload, economics and risk allocation. |
| Why It Matters | Initial courtesy participation does not establish durable supply; operator withdrawal can destroy service-cell liquidity. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal evidence has been collected; the record was added in v1.1 as a validation-control gap. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Track inventory persistence, completed bookings, post-booking operator interviews and continued-slot commitments without founder-specific concessions. |
| Evidence Needed | Active-operator denominator, retained inventory, repeated availability, reasons for withdrawal, workload and net economics. |
| Falsification Criterion | Operators remove or materially restrict inventory after early use at a level incompatible with the locked supply and concentration requirements. |
| Montreal-Specific Consideration | Separate genuinely recurring capacity from temporary event, renovation or personal-network access. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-07 + MI-10 |
| Version Control | v1.1 NEW; v1.0 priority N/A; New v1.1 record created to close a verified control gap. |

### 5.4 Marketplace liquidity

**Domain rule:** Measure practical service cells; Montreal-wide averages are prohibited.

#### LIQ-001 — Marketplace liquidity

| Field | Registered control |
| --- | --- |
| ID | LIQ-001 |
| Market Side | Marketplace liquidity |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | Each active Chef can receive enough qualified customers per service window to reach viable utilization without excessive paid acquisition. |
| Why It Matters | The customer-to-Chef ratio controls provider economics, retention and menu availability. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Cluster simulation followed by a constrained pilot with fixed Chefs, windows and acquisition channels. |
| Evidence Needed | Qualified visitors, buyers, orders, demand per Chef/window, conversion, fill rate and provider break-even volume. |
| Falsification Criterion | Observed demand per active Chef remains below provider break-even after credible acquisition and organic repeat are included. |
| Montreal-Specific Consideration | Calculate by cluster and daypart; never divide total Montreal customers by total onboarded Chefs. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-10 Marketplace Liquidity |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### LIQ-002 — Marketplace liquidity

| Field | Registered control |
| --- | --- |
| ID | LIQ-002 |
| Market Side | Marketplace liquidity |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | A launch cluster can support the minimum active Chef count and cuisine/menu diversity needed for customers to find an acceptable option. |
| Why It Matters | Too little choice lowers conversion; too much early supply dilutes orders and drives Chef churn. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Assortment choice tests and pilot simulations varying active Chef count, cuisines, price points and availability. |
| Evidence Needed | Search success, no-result rate, conversion, choice overload, order concentration and orders per Chef. |
| Falsification Criterion | No feasible supply configuration simultaneously meets customer choice and Chef utilization requirements. |
| Montreal-Specific Consideration | Define diversity from the chosen need-state/community, not a generic city-wide cuisine count. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-05 + MI-06 + MI-10 |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### LIQ-003 — Marketplace liquidity / Kitchen supply

| Field | Registered control |
| --- | --- |
| ID | LIQ-003 |
| Market Side | Marketplace liquidity / Kitchen supply |
| Validation Phase | PHASE_1_CHEF_KITCHEN |
| Hypothesis | The launch cluster has enough compatible Kitchen spaces and hours to activate and retain its required Chef supply. |
| Why It Matters | Chef demand cannot translate into food supply if capacity or equipment is unavailable at the right times. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Capacity map and scenario model linking each Chef/menu/window to a confirmed space and fallback. |
| Evidence Needed | Compatible hours, simultaneous spaces, equipment, utilization, booking conflicts and fallback coverage. |
| Falsification Criterion | Required Chef/menu coverage cannot be scheduled without chronic conflicts or uneconomic idle capacity. |
| Montreal-Specific Consideration | Use travel-time and actual Kitchen schedules; do not count capacity elsewhere on the Island as interchangeable. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-07 + MI-10 |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### LIQ-004 — Marketplace liquidity

| Field | Registered control |
| --- | --- |
| ID | LIQ-004 |
| Market Side | Marketplace liquidity |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | Customer ordering windows, Chef production schedules, Kitchen slots and fulfilment capacity overlap sufficiently by daypart. |
| Why It Matters | Aggregate weekly supply can hide hourly marketplace failure. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Four-way calendar overlay and live pilot measured at 30- or 60-minute intervals. |
| Evidence Needed | Demand, listing availability, production capacity, Kitchen occupancy, courier/pickup capacity and missed matches by interval. |
| Falsification Criterion | Peak customer demand repeatedly occurs when one or more required supply layers are unavailable. |
| Montreal-Specific Consideration | Analyze weekday lunch/dinner and weekend windows separately; include local travel and handoff constraints. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-10 Marketplace Liquidity |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### LIQ-005 — Geographic density

| Field | Registered control |
| --- | --- |
| ID | LIQ-005 |
| Market Side | Geographic density |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | A practical service radius defined by travel time can balance assortment, food quality, pickup convenience and delivery cost. |
| Why It Matters | A radius that is too small starves choice; one that is too large breaks fulfilment economics and experience. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Travel-time scenario model and staged pilot with distance bands; measure pickup and delivery separately. |
| Evidence Needed | Conversion, ETA, delivery cost, temperature/quality issues, cancellation and repeat by travel-time band. |
| Falsification Criterion | No travel-time boundary yields both adequate assortment/demand and acceptable fulfilment economics/quality. |
| Montreal-Specific Consideration | Model real routes and barriers within Montreal rather than straight-line distance; retest under adverse weather later. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-10 Marketplace Liquidity |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### LIQ-006 — Chef-to-customer matching

| Field | Registered control |
| --- | --- |
| ID | LIQ-006 |
| Market Side | Chef-to-customer matching |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | Customers can find an available, acceptable menu within their price, dietary, cuisine, timing and location constraints often enough to convert. |
| Why It Matters | Headline listing count is meaningless if simultaneous constraints produce no-result searches. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | HIGH |
| Validation Method | Instrumented catalogue prototype and pilot search logs using real menus/availability. |
| Evidence Needed | Qualified search success, zero-result rate, filter exits, substitution, conversion and reason for abandonment. |
| Falsification Criterion | A material share of target searches has no acceptable option at the moment of purchase. |
| Montreal-Specific Consideration | Use bilingual taxonomy and locally relevant cuisine/dietary terms; measure by cluster and window. |
| Priority | P1 |
| Status | UNTESTED |
| Primary Evidence Module | MI-04 + MI-10 |
| Version Control | v1.0; v1.0 priority P1; Source priority retained; interpreted within the assigned validation phase. |

#### LIQ-007 — Kitchen-to-Chef matching

| Field | Registered control |
| --- | --- |
| ID | LIQ-007 |
| Market Side | Kitchen-to-Chef matching |
| Validation Phase | PHASE_1_CHEF_KITCHEN |
| Hypothesis | Chefs can identify and book an acceptable space that matches equipment, schedule, price, location and compliance needs with low failure. |
| Why It Matters | A nominal Kitchen directory does not create booking liquidity. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | HIGH |
| Validation Method | Concierge matching followed by a searchable prototype and real booking attempt. |
| Evidence Needed | Match rate, time-to-match, failed constraints, quote-to-booking conversion, changes and cancellations. |
| Falsification Criterion | Most qualified Chef requests fail or require manual intervention that cannot scale economically. |
| Montreal-Specific Consideration | Measure within the intended operating cluster and use actual transit/vehicle logistics for equipment and supplies. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-06 + MI-07 + MI-10 |
| Version Control | v1.0; v1.0 priority P1; Priority changed for phase-relative control. |

#### LIQ-008 — Delivery liquidity

| Field | Registered control |
| --- | --- |
| ID | LIQ-008 |
| Market Side | Delivery liquidity |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | Order density per pickup point and service window is high enough to keep delivery cost, wait and failure within the viable range. |
| Why It Matters | Low density turns each order into an expensive one-off trip and can erase contribution margin. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Route simulation using pilot orders, courier quotes and batched-versus-single delivery scenarios. |
| Evidence Needed | Orders per Kitchen/window, route distance/time, delivery cost/order, batching rate, lateness and failed handoff. |
| Falsification Criterion | Plausible cluster demand cannot achieve delivery cost/order required for positive contribution without persistent subsidy. |
| Montreal-Specific Consideration | Model each Kitchen as a pickup origin and use Montreal service-window travel times, not city-wide average distance. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-09 + MI-10 |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### LIQ-009 — Cold start / Supply activation

| Field | Registered control |
| --- | --- |
| ID | LIQ-009 |
| Market Side | Cold start / Supply activation |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | Cheffy Bites can sequence Chef, Kitchen and customer activation so that no side churns before useful liquidity appears. |
| Why It Matters | Three-sided cold start is harder than a normal two-sided marketplace and can create self-reinforcing failure. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | One-cluster staged pilot with precommitted supply, controlled customer cohort, explicit activation budget and weekly liquidity dashboard. |
| Evidence Needed | Time to first order/booking, active-to-onboarded ratio, cancellations, supply churn, no-result demand and subsidy required. |
| Falsification Criterion | Even a tightly staged cluster cannot sustain active supply and customer repeat without an unaffordable subsidy or manual effort. |
| Montreal-Specific Consideration | Do not open all Montreal simultaneously; compare at most a few candidate clusters using the same activation protocol. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-10 Marketplace Liquidity |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### LIQ-010 — Multi-Chef liquidity

| Field | Registered control |
| --- | --- |
| ID | LIQ-010 |
| Market Side | Multi-Chef liquidity |
| Validation Phase | LATER |
| Hypothesis | Enough complementary Chefs are concurrently active in the same Kitchen for multi-Chef ordering to be available when customers value it. |
| Why It Matters | The feature depends on co-location and synchronized availability, not total marketplace supply. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | MEDIUM |
| Validation Method | Co-location schedule analysis and pilot observation of real multi-Chef baskets. |
| Evidence Needed | Concurrent Chef-hours by Kitchen, complementary menus, multi-Chef basket rate, delays and incremental AOV. |
| Falsification Criterion | Concurrent supply is rare or the coordination needed to create it is uneconomic. |
| Montreal-Specific Consideration | Measure by one physical Kitchen and service window; city-wide Chef counts are irrelevant. |
| Priority | P2 |
| Status | UNTESTED |
| Primary Evidence Module | MI-10 Marketplace Liquidity |
| Version Control | v1.0; v1.0 priority P2; Source priority retained; interpreted within the assigned validation phase. |

#### LIQ-011 — Marketplace liquidity

| Field | Registered control |
| --- | --- |
| ID | LIQ-011 |
| Market Side | Marketplace liquidity |
| Validation Phase | PHASE_1_CHEF_KITCHEN |
| Hypothesis | Phase-1 liquidity exists within a practical service cell where Chef need, Kitchen, usable capacity, required equipment, daypart, geography, booking duration and compliance constraints overlap simultaneously. |
| Why It Matters | A city can have many Chefs and Kitchens while producing almost no bookable matches once all constraints are conjoined. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal evidence has been collected; the record was added in v1.1 as a validation-control gap. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Collect structured Chef requests and reliability-adjusted operator inventory, then manually match on every required dimension before enabling any software shortcut. |
| Evidence Needed | Request-level constraint vector, eligible supply cells, time to match, match/booking outcome and binding constraint for every failure. |
| Falsification Criterion | The share of qualified requests with a usable match is below the pre-registered Phase-1 requirement, or matches depend on relaxing non-negotiable compliance or operating constraints. |
| Montreal-Specific Consideration | Define cells at a travel radius and daypart appropriate to the selected Montreal cluster; city-wide totals are prohibited. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-10 Marketplace Liquidity |
| Version Control | v1.1 NEW; v1.0 priority N/A; New v1.1 record created to close a verified control gap. |

#### LIQ-012 — Marketplace liquidity / diagnostics

| Field | Registered control |
| --- | --- |
| ID | LIQ-012 |
| Market Side | Marketplace liquidity / diagnostics |
| Validation Phase | PHASE_1_CHEF_KITCHEN |
| Hypothesis | The no-result rate for qualified Phase-1 Chef searches is low enough for continued use, and reason codes reveal remediable rather than structural supply gaps. |
| Why It Matters | Aggregate conversion conceals why matching fails; reason-coded no-results determine whether to add supply, narrow scope, redesign terms or stop. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal evidence has been collected; the record was added in v1.1 as a validation-control gap. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Run a manual search/booking log with one primary and optional secondary failure reason selected from a controlled taxonomy. |
| Evidence Needed | Qualified searches, search result, eligible matches, booking outcome, and reasons such as location, time, price, duration, equipment, compliance, access or operator rejection. |
| Falsification Criterion | The locked no-result ceiling is breached and the dominant reasons cannot be corrected within the budget/time boundary or a credible operating-model modification. |
| Montreal-Specific Consideration | Reason rates must be reported per service cell and request type; repeated requests by one Chef must remain identifiable. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-10 Marketplace Liquidity |
| Version Control | v1.1 NEW; v1.0 priority N/A; New v1.1 record created to close a verified control gap. |

### 5.5 Unit economics and working capital

**Domain rule:** Include all cash, labour, failure and acquisition burdens.

#### ECO-001 — Unit economics / Customer

| Field | Registered control |
| --- | --- |
| ID | ECO-001 |
| Market Side | Unit economics / Customer |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | Observed average order value and items per order are high enough to absorb variable platform and fulfilment costs. |
| Why It Matters | Low baskets make payment, support and delivery costs disproportionately large. |
| Current Classification | HYPOTHESIS |
| Current Evidence | The product specification leaves commercial terms configurable/TBD; no willingness-to-pay evidence was supplied. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Use paid pilot transaction data; model median and distribution, not only mean AOV. |
| Evidence Needed | Food subtotal, items, fees, fulfilment type, discounts, taxes and margin by basket band. |
| Falsification Criterion | Typical baskets cannot reach positive contribution at competitive pricing and plausible take rate. |
| Montreal-Specific Consideration | Report by cluster, occasion and fulfilment mode; do not import restaurant-delivery benchmarks as facts. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-09 Unit Economics |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### ECO-002 — Unit economics / Retention

| Field | Registered control |
| --- | --- |
| ID | ECO-002 |
| Market Side | Unit economics / Retention |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | Orders per customer-month and cohort repeat generate sufficient gross contribution over the chosen payback horizon. |
| Why It Matters | Customer lifetime value is driven by actual repeat, not survey intent. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Cohort model from pilot purchases with sensitivity for churn, pauses, discounts and seasonality. |
| Evidence Needed | Order frequency, retention curve, gross contribution/order, reactivation and cohort acquisition source. |
| Falsification Criterion | Even optimistic but plausible retention fails to repay customer acquisition and onboarding cost in the required horizon. |
| Montreal-Specific Consideration | Separate recurring meal-prep cohorts from episodic food orders and report cluster/season effects. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-05 + MI-09 |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### ECO-003 — Unit economics / Acquisition

| Field | Registered control |
| --- | --- |
| ID | ECO-003 |
| Market Side | Unit economics / Acquisition |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | Blended and marginal customer acquisition cost can fall to a level repayable by cohort contribution without referral assumptions doing all the work. |
| Why It Matters | A good product can still be non-viable if local customer acquisition is too expensive. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Small paid-channel tests, community partnerships, referral tests and fully loaded acquisition-cost accounting. |
| Evidence Needed | Spend, staff time, partner cost, qualified acquisition, first paid order, repeat and CAC by channel/cluster. |
| Falsification Criterion | No credible channel approaches the payback threshold after low-quality leads and incentives are included. |
| Montreal-Specific Consideration | Run channel tests within the actual launch cluster and in both relevant language journeys. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-04 + MI-09 |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### ECO-004 — Unit economics / Chef acquisition

| Field | Registered control |
| --- | --- |
| ID | ECO-004 |
| Market Side | Unit economics / Chef acquisition |
| Validation Phase | CROSS_PHASE |
| Hypothesis | Fully loaded cost to recruit, qualify, onboard and activate a reliable Chef is recoverable from that provider's contribution before likely churn. |
| Why It Matters | Supply acquisition can be document-heavy and operationally expensive. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | HIGH |
| Validation Method | Time-driven activity costing through a pilot onboarding funnel. |
| Evidence Needed | Lead source, staff minutes, verification cost, drop-off, time to first fulfilled order and active lifetime. |
| Falsification Criterion | Activation cost and churn make contribution payback implausible for qualified Chefs. |
| Montreal-Specific Consideration | Separate independent and Organization-affiliated onboarding pathways. |
| Priority | P1 |
| Status | UNTESTED |
| Primary Evidence Module | MI-06 + MI-09 |
| Version Control | v1.0; v1.0 priority P1; Source priority retained; interpreted within the assigned validation phase. |

#### ECO-005 — Unit economics / Kitchen acquisition

| Field | Registered control |
| --- | --- |
| ID | ECO-005 |
| Market Side | Unit economics / Kitchen acquisition |
| Validation Phase | PHASE_1_CHEF_KITCHEN |
| Hypothesis | Fully loaded cost to acquire, verify, configure and activate a Kitchen is recoverable from booking/food volume and retention. |
| Why It Matters | Site verification, contracting and configuration may make sparse Kitchen supply expensive. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | HIGH |
| Validation Method | Activity costing from first operator lead through first successful Chef booking/food order. |
| Evidence Needed | Sales cycle, staff/legal/site time, setup cost, activation, rentable hours and active months. |
| Falsification Criterion | Kitchen acquisition payback exceeds a tolerable horizon at plausible utilization. |
| Montreal-Specific Consideration | Measure by facility type and cluster; avoid assuming one operator's multiple locations have identical cost. |
| Priority | P1 |
| Status | UNTESTED |
| Primary Evidence Module | MI-07 + MI-09 |
| Version Control | v1.0; v1.0 priority P1; Source priority retained; interpreted within the assigned validation phase. |

#### ECO-006 — Unit economics / Payments

| Field | Registered control |
| --- | --- |
| ID | ECO-006 |
| Market Side | Unit economics / Payments |
| Validation Phase | CROSS_PHASE |
| Hypothesis | Payment processing, payout, chargeback, tax handling and reconciliation cost can be covered within the platform take. |
| Why It Matters | Multi-provider allocation and small baskets can create a high financial-operating burden. |
| Current Classification | ASSUMPTION |
| Current Evidence | The product specification leaves commercial terms configurable/TBD; no willingness-to-pay evidence was supplied. |
| Confidence | VERY LOW |
| Business Impact if False | HIGH |
| Validation Method | Provider quote review and transaction-level model including failures, refunds and payout events. |
| Evidence Needed | Per-transaction fixed/variable fees, payout fees, failure rate, disputes, tax tooling and reconciliation labour. |
| Falsification Criterion | Financial-processing cost consumes the viable take rate or requires customer fees that suppress conversion. |
| Montreal-Specific Consideration | Use Canadian/Quebec commercial terms and CAD transaction sizes when researched; do not use US fee assumptions. |
| Priority | P1 |
| Status | UNTESTED |
| Primary Evidence Module | MI-09 Unit Economics |
| Version Control | v1.0; v1.0 priority P1; Source priority retained; interpreted within the assigned validation phase. |

#### ECO-007 — Unit economics / Delivery

| Field | Registered control |
| --- | --- |
| ID | ECO-007 |
| Market Side | Unit economics / Delivery |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | Delivery cost, customer payment and any platform subsidy can reach a sustainable equilibrium at cluster scale. |
| Why It Matters | Delivery can erase contribution even when food and marketplace fees are viable. |
| Current Classification | HYPOTHESIS |
| Current Evidence | The product specification leaves commercial terms configurable/TBD; no willingness-to-pay evidence was supplied. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Courier quotes plus actual route pilot; model single, batched and pickup scenarios with no permanent launch subsidy. |
| Evidence Needed | Delivery price/cost, tip treatment if applicable, subsidy, batching, failure, refund and conversion impact. |
| Falsification Criterion | The lowest credible delivery cost still leaves contribution non-positive or all-in price unacceptable. |
| Montreal-Specific Consideration | Use Montreal origin-destination pairs and seasonal sensitivity; do not average across distant clusters. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-09 + MI-10 |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### ECO-008 — Unit economics / Service recovery

| Field | Registered control |
| --- | --- |
| ID | ECO-008 |
| Market Side | Unit economics / Service recovery |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | Refunds, credits, remakes, cancellations, chargebacks and customer-support cost remain below the margin buffer. |
| Why It Matters | Early marketplace failures are costly and can turn nominal gross margin into losses. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | HIGH |
| Validation Method | Pilot issue taxonomy with full case costing and sensitivity analysis for launch versus steady-state rates. |
| Evidence Needed | Incident rate, resolution type, food loss, delivery loss, refund, support minutes and repeat after recovery. |
| Falsification Criterion | Observed or plausible remediation/support cost eliminates positive contribution or repeat trust. |
| Montreal-Specific Consideration | Record issue origin separately for Chef, Kitchen, delivery, platform and customer. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-09 Unit Economics |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### ECO-009 — Chef/provider economics

| Field | Registered control |
| --- | --- |
| ID | ECO-009 |
| Market Side | Chef/provider economics |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | Independent and Organization-operated providers each retain adequate contribution after all direct costs and Cheffy fees. |
| Why It Matters | Commercial provider identity changes labour, overhead, settlement and pricing economics. |
| Current Classification | HYPOTHESIS |
| Current Evidence | The product specification leaves commercial terms configurable/TBD; no willingness-to-pay evidence was supplied. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Separate open-book models and pilot settlement statements for independent and Organization-operated supply. |
| Evidence Needed | Contribution by provider type, labour model, overhead, waste, fees, payout timing and required volume. |
| Falsification Criterion | Either critical provider type is systematically loss-making at customer-acceptable prices. |
| Montreal-Specific Consideration | Do not treat employee Chef compensation as marketplace payout; model Organization economics separately. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-09 Unit Economics |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### ECO-010 — Kitchen economics

| Field | Registered control |
| --- | --- |
| ID | ECO-010 |
| Market Side | Kitchen economics |
| Validation Phase | PHASE_1_CHEF_KITCHEN |
| Hypothesis | Kitchen rental revenue exceeds incremental staffing, utilities, cleaning, wear, insurance, disruption and platform fees at achieved utilization. |
| Why It Matters | Gross booking revenue may overstate the operator's true incremental value. |
| Current Classification | HYPOTHESIS |
| Current Evidence | The product specification leaves commercial terms configurable/TBD; no willingness-to-pay evidence was supplied. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Operator contribution model validated with a paid pilot and activity-cost log. |
| Evidence Needed | Revenue/hour, booked/occupied hours, incremental costs, cancellations, damage/cleaning and operator contribution. |
| Falsification Criterion | Operator contribution is inadequate or only viable at rates Chefs cannot pay. |
| Montreal-Specific Consideration | Use actual facility cost structures from candidate Montreal operators rather than generic shared-Kitchen rates. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-07 + MI-09 |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### ECO-011 — Platform contribution margin

| Field | Registered control |
| --- | --- |
| ID | ECO-011 |
| Market Side | Platform contribution margin |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | Platform take covers payment, delivery subsidy, support, verification, refunds, variable tooling and other transaction-variable costs at plausible scale. |
| Why It Matters | Gross merchandise value and revenue are not evidence of a sustainable business. |
| Current Classification | HYPOTHESIS |
| Current Evidence | The product specification leaves commercial terms configurable/TBD; no willingness-to-pay evidence was supplied. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Transaction-level contribution model with base/downside cases and reconciliation to pilot actuals. |
| Evidence Needed | Revenue by source and every variable/semi-variable cost per order, booking and appointment. |
| Falsification Criterion | Contribution margin remains at or below zero under plausible prices, volume, failure and subsidy assumptions. |
| Montreal-Specific Consideration | Run separate economics for each Montreal wedge and fulfilment mode before combining them. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-09 Unit Economics |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### ECO-012 — Acquisition payback / Scale

| Field | Registered control |
| --- | --- |
| ID | ECO-012 |
| Market Side | Acquisition payback / Scale |
| Validation Phase | CROSS_PHASE |
| Hypothesis | The model reaches acquisition payback and tolerable working-capital needs without relying on city-wide scale before one cluster works. |
| Why It Matters | A local marketplace may fail from cash burn even if long-run unit economics appear positive. |
| Current Classification | HYPOTHESIS |
| Current Evidence | The product specification leaves commercial terms configurable/TBD; no willingness-to-pay evidence was supplied. |
| Confidence | VERY LOW |
| Business Impact if False | HIGH |
| Validation Method | Cohort payback and cash-flow model with sensitivity for payout timing, refunds, reserves, seasonality and activation spend. |
| Evidence Needed | Payback by side, working-capital peak, cash conversion cycle, downside runway and scale dependencies. |
| Falsification Criterion | Payback or cash requirements exceed founder-approved limits under plausible downside conditions. |
| Montreal-Specific Consideration | Model one cluster first; Montreal-wide network effects are not an acceptable rescue assumption. |
| Priority | P1 |
| Status | UNTESTED |
| Primary Evidence Module | MI-09 + MI-11 |
| Version Control | v1.0; v1.0 priority P1; Source priority retained; interpreted within the assigned validation phase. |

#### ECO-013 — Unit economics / working capital

| Field | Registered control |
| --- | --- |
| ID | ECO-013 |
| Market Side | Unit economics / working capital |
| Validation Phase | PHASE_1_CHEF_KITCHEN |
| Hypothesis | Phase-1 booking settlement timing, deposits, refunds, disputes, damage exposure and reserves can be funded within the founder's cash constraint without hiding losses or transferring unacceptable risk. |
| Why It Matters | A transaction can appear contribution-positive yet fail because cash is held, refunded or exposed before operator payout and incident resolution. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal evidence has been collected; the record was added in v1.1 as a validation-control gap. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Map cash timing and responsibility for representative bookings; run downside scenarios using actual pilot terms and observed exceptions. |
| Evidence Needed | Cash-in/out timeline, deposit and reserve needs, payout timing, refund/dispute frequency, maximum plausible exposure and responsible party. |
| Falsification Criterion | Required float, reserves or unbounded incident exposure exceed the locked Phase-1 funding tolerance or cannot be contractually allocated on acceptable terms. |
| Montreal-Specific Consideration | Use Canadian-dollar cash timing and Quebec contract/tax assumptions only after primary-source or qualified review. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-09 Unit Economics |
| Version Control | v1.1 NEW; v1.0 priority N/A; New v1.1 record created to close a verified control gap. |

### 5.6 Monetization

**Domain rule:** Free activation can test behaviour but cannot prove continuing value or sustainable revenue.

#### MON-001 — Food transaction monetization

| Field | Registered control |
| --- | --- |
| ID | MON-001 |
| Market Side | Food transaction monetization |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | A food-order platform fee/take rate can fund the required service while preserving customer conversion and provider contribution. |
| Why It Matters | This is the most direct revenue source but creates a three-way price constraint. |
| Current Classification | HYPOTHESIS |
| Current Evidence | The product specification leaves commercial terms configurable/TBD; no willingness-to-pay evidence was supplied. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Triangulate customer checkout tests, provider fee-card tests and the MI-09 contribution model. |
| Evidence Needed | Customer conversion, provider margin/retention and platform contribution at each fee design. |
| Falsification Criterion | No fee design simultaneously supports customer acceptance, provider economics and platform contribution. |
| Montreal-Specific Consideration | Test all-in CAD checkout totals and disclose fees consistently in French/English. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-08 + MI-09 |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### MON-002 — Kitchen booking monetization

| Field | Registered control |
| --- | --- |
| ID | MON-002 |
| Market Side | Kitchen booking monetization |
| Validation Phase | PHASE_1_CHEF_KITCHEN |
| Hypothesis | A Kitchen-booking fee can be charged to the Chef, operator or split without driving transactions off-platform or breaking bilateral economics. |
| Why It Matters | The payer, value delivered and risk of disintermediation differ from food orders. |
| Current Classification | HYPOTHESIS |
| Current Evidence | The product specification leaves commercial terms configurable/TBD; no willingness-to-pay evidence was supplied. |
| Confidence | VERY LOW |
| Business Impact if False | HIGH |
| Validation Method | Bilateral fee experiments with off-platform risk questions and signed booking terms. |
| Evidence Needed | Fee acceptance, booking conversion, operator/ Chef contribution and disintermediation intent. |
| Falsification Criterion | Required monetization materially reduces bookings or causes repeat pairs to leave the platform. |
| Montreal-Specific Consideration | Use actual Montreal booking quotes and recurring-pair scenarios. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-07 + MI-08 + MI-09 |
| Version Control | v1.0; v1.0 priority P1; Priority changed for phase-relative control. |

#### MON-003 — Organization plans

| Field | Registered control |
| --- | --- |
| ID | MON-003 |
| Market Side | Organization plans |
| Validation Phase | LATER |
| Hypothesis | Organizations will pay an optional plan for multi-user, multi-location, reporting or operational tools beyond transaction access. |
| Why It Matters | SaaS revenue could diversify income, but premature plans add scope and sales friction. |
| Current Classification | HYPOTHESIS |
| Current Evidence | The product specification leaves commercial terms configurable/TBD; no willingness-to-pay evidence was supplied. |
| Confidence | VERY LOW |
| Business Impact if False | MEDIUM |
| Validation Method | Problem interviews and paid design-partner proposal; do not treat feature ranking as willingness to pay. |
| Evidence Needed | Decision-maker pain, budget, procurement, paid commitment and required features. |
| Falsification Criterion | Organizations value the tools but will not pay separately or require enterprise scope incompatible with launch. |
| Montreal-Specific Consideration | Test small local operators before assuming larger multi-location demand. |
| Priority | P2 |
| Status | UNTESTED |
| Primary Evidence Module | MI-06 + MI-07 + MI-08 |
| Version Control | v1.0; v1.0 priority P2; Source priority retained; interpreted within the assigned validation phase. |

#### MON-004 — Promoted visibility

| Field | Registered control |
| --- | --- |
| ID | MON-004 |
| Market Side | Promoted visibility |
| Validation Phase | LATER |
| Hypothesis | Providers will pay for promoted placement without materially reducing customer trust, relevance or fairness. |
| Why It Matters | Advertising revenue can conflict with marketplace quality and new-provider access. |
| Current Classification | HYPOTHESIS |
| Current Evidence | The product specification leaves commercial terms configurable/TBD; no willingness-to-pay evidence was supplied. |
| Confidence | VERY LOW |
| Business Impact if False | LOW |
| Validation Method | Concept test only after organic ranking value is demonstrated; include clearly labelled promotion treatments. |
| Evidence Needed | Provider willingness to pay, customer trust/comprehension, conversion quality and repeat. |
| Falsification Criterion | Paid placement reduces trust/relevance or merely reallocates orders among underutilized providers. |
| Montreal-Specific Consideration | Assess bilingual disclosure and small-market concentration risks within a launch cluster. |
| Priority | P2 |
| Status | UNTESTED |
| Primary Evidence Module | Post-core / MI-11 |
| Version Control | v1.0; v1.0 priority P2; Source priority retained; interpreted within the assigned validation phase. |

#### MON-005 — Analytics / tools

| Field | Registered control |
| --- | --- |
| ID | MON-005 |
| Market Side | Analytics / tools |
| Validation Phase | LATER |
| Hypothesis | Chefs and Kitchen operators will pay for decision tools or analytics that deliver measurable incremental value. |
| Why It Matters | Analytics is not a revenue source unless it changes decisions and has a budget owner. |
| Current Classification | HYPOTHESIS |
| Current Evidence | The product specification leaves commercial terms configurable/TBD; no willingness-to-pay evidence was supplied. |
| Confidence | VERY LOW |
| Business Impact if False | LOW |
| Validation Method | Prototype one decision workflow and seek a paid design-partner commitment. |
| Evidence Needed | Action taken, measured value, budget, frequency and willingness to pay. |
| Falsification Criterion | Users prefer basic included reporting or cannot attribute enough value to pay. |
| Montreal-Specific Consideration | Use locally relevant operational decisions; avoid building city-wide benchmarking before sufficient data exists. |
| Priority | P2 |
| Status | UNTESTED |
| Primary Evidence Module | Post-core / MI-11 |
| Version Control | v1.0; v1.0 priority P2; Source priority retained; interpreted within the assigned validation phase. |

#### MON-006 — Dietitian monetization

| Field | Registered control |
| --- | --- |
| ID | MON-006 |
| Market Side | Dietitian monetization |
| Validation Phase | LATER |
| Hypothesis | A professional-service fee can be charged on Dietitian appointments without weakening professional trust or provider economics. |
| Why It Matters | Professional services have different norms, liabilities and pricing than food transactions. |
| Current Classification | HYPOTHESIS |
| Current Evidence | The product specification leaves commercial terms configurable/TBD; no willingness-to-pay evidence was supplied. |
| Confidence | VERY LOW |
| Business Impact if False | HIGH |
| Validation Method | Customer and Dietitian fee-card tests plus professional/compliance review and a paid appointment pilot. |
| Evidence Needed | Booking conversion, Dietitian net revenue, customer trust, cancellation/refund and administrative cost. |
| Falsification Criterion | The fee required for platform contribution is unacceptable to customers/Dietitians or conflicts with applicable rules. |
| Montreal-Specific Consideration | Test Quebec-authorized professionals and Montreal customer segments only after scope verification. |
| Priority | P1 |
| Status | UNTESTED |
| Primary Evidence Module | MI-08 + regulatory workstream |
| Version Control | v1.0; v1.0 priority P1; Source priority retained; interpreted within the assigned validation phase. |

#### MON-007 — Monetization architecture

| Field | Registered control |
| --- | --- |
| ID | MON-007 |
| Market Side | Monetization architecture |
| Validation Phase | CROSS_PHASE |
| Hypothesis | Cheffy Bites can combine selected revenue sources without double-charging the same value, obscuring fees, or creating incentives that damage liquidity. |
| Why It Matters | Layered commissions, plans, promotion and service fees can make every side feel taxed and distort ranking. |
| Current Classification | HYPOTHESIS |
| Current Evidence | The product specification leaves commercial terms configurable/TBD; no willingness-to-pay evidence was supplied. |
| Confidence | VERY LOW |
| Business Impact if False | HIGH |
| Validation Method | End-to-end fee walkthroughs and incentive map for each wedge; test simplified alternatives. |
| Evidence Needed | Total effective take by side, fee comprehension, behavioural response, conflicts and off-platform incentives. |
| Falsification Criterion | The viable stack depends on overlapping charges or incentives that reduce trust, retention or participation. |
| Montreal-Specific Consideration | Evaluate Quebec consumer disclosure requirements in the later primary-source workstream. |
| Priority | P1 |
| Status | UNTESTED |
| Primary Evidence Module | MI-09 + MI-11 |
| Version Control | v1.0; v1.0 priority P1; Source priority retained; interpreted within the assigned validation phase. |

#### MON-008 — Monetization / disintermediation

| Field | Registered control |
| --- | --- |
| ID | MON-008 |
| Market Side | Monetization / disintermediation |
| Validation Phase | PHASE_1_CHEF_KITCHEN |
| Hypothesis | Cheffy Bites provides continuing value after a Chef and Kitchen meet, so repeat bookings remain on-platform at a fee structure both sides accept. |
| Why It Matters | If the platform only introduces parties, they can transact directly after the first booking and eliminate recurring revenue and observable liquidity. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal evidence has been collected; the record was added in v1.1 as a validation-control gap. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | After a completed manual booking, present realistic repeat terms and observe rebooking channel; interview both sides about bypass triggers and valued continuing services. |
| Evidence Needed | On-platform repeat intent and behaviour, fee tolerance, leakage reasons, value of payments, records, reliability, dispute handling and scheduling. |
| Falsification Criterion | Most viable pairs bypass the platform at the minimum monetization needed for sustainable economics, with no low-cost continuing value that changes behaviour. |
| Montreal-Specific Consideration | Existing Montreal operator networks and recurring direct agreements are explicit alternatives, not an afterthought. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-08 + MI-09 |
| Version Control | v1.1 NEW; v1.0 priority N/A; New v1.1 record created to close a verified control gap. |

### 5.7 Trust, food safety and quality

**Domain rule:** Mostly Phase 2; cross-phase responsibilities remain separately gated.

#### TRUST-001 — Customer trust

| Field | Registered control |
| --- | --- |
| ID | TRUST-001 |
| Market Side | Customer trust |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | Customers will purchase from unfamiliar independent Chefs when the marketplace presents credible accountability and fulfilment controls. |
| Why It Matters | Trust failure can prevent first purchase regardless of product-market need. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Trust-barrier interviews, listing experiments and a real first-purchase pilot with transparent controls. |
| Evidence Needed | First-order conversion, concern ranking, abandonment, support questions and repeat after satisfactory fulfilment. |
| Falsification Criterion | Target customers still refuse first purchase or demand a recognized restaurant/platform guarantee the model cannot provide. |
| Montreal-Specific Consideration | Recruit beyond founder networks and within several Montreal communities; measure trust by provider type. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-05 Customer Discovery |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### TRUST-002 — Trust evidence

| Field | Registered control |
| --- | --- |
| ID | TRUST-002 |
| Market Side | Trust evidence |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | A parsimonious set of trust signals - verified Kitchen status, transparent Chef identity, reviews, photos, certifications and clear remediation - materially increases purchase. |
| Why It Matters | Building every signal is expensive; the causal signals must be identified. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | HIGH |
| Validation Method | Factorial concept test followed by behavioural A/B tests on the highest-risk signals. |
| Evidence Needed | Incremental conversion/trust by signal, comprehension, false reassurance and cost to verify/maintain. |
| Falsification Criterion | No feasible signal bundle overcomes first-purchase resistance, or the required bundle is too costly to operate. |
| Montreal-Specific Consideration | Test terminology and evidence display in French/English and avoid implying unperformed verification. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-05 + MI-09 |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### TRUST-003 — Claims / Verification

| Field | Registered control |
| --- | --- |
| ID | TRUST-003 |
| Market Side | Claims / Verification |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | Cheffy Bites can present food-safety, dietary, nutrition and professional credentials with accurate provenance that users understand. |
| Why It Matters | Overstated claims create safety, legal and trust risk; overly weak claims may not support purchase. |
| Current Classification | ASSUMPTION |
| Current Evidence | The specification flags this as an unresolved launch gate; no authoritative Quebec or municipal verification has been completed. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Claim-language comprehension tests plus authoritative requirement review and verification-process costing. |
| Evidence Needed | Correct user interpretation, evidence source, verification status, update cadence, false-claim rate and operating cost. |
| Falsification Criterion | Users materially misinterpret self-attested claims as verified, or compliant verification is infeasible at launch. |
| Montreal-Specific Consideration | Use Quebec-specific credential and food-safety terminology after primary-source validation. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-05 + regulatory workstream |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### TRUST-004 — Quality / Retention

| Field | Registered control |
| --- | --- |
| ID | TRUST-004 |
| Market Side | Quality / Retention |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | Independent providers can deliver sufficiently consistent food quality, portion, packaging and timing for reviews and repeat purchase to stabilize. |
| Why It Matters | Marketplace variety is not valuable if experience variance destroys retention. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Standardized pilot scorecard, blind product checks where appropriate, review analysis and repeat tracking. |
| Evidence Needed | Quality variance, portion accuracy, temperature/packaging, timeliness, complaint rate, ratings and repeat. |
| Falsification Criterion | Experience variance remains above customer tolerance despite feasible controls and provider coaching. |
| Montreal-Specific Consideration | Test multiple Kitchens and service windows; avoid generalizing from a founder-supervised demonstration. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-05 + MI-06 + MI-10 |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### TRUST-005 — Multi-Chef accountability

| Field | Registered control |
| --- | --- |
| ID | TRUST-005 |
| Market Side | Multi-Chef accountability |
| Validation Phase | LATER |
| Hypothesis | Customers understand who is responsible when one Chef group fails inside a same-Kitchen multi-Chef order, and remediation preserves trust. |
| Why It Matters | Split preparation responsibility can create confusing partial failures and refunds. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | HIGH |
| Validation Method | Service blueprint and failure-scenario usability test; later simulate partial cancellation/refund in pilot operations. |
| Evidence Needed | Responsibility comprehension, preferred remedy, support contacts, refund expectation and repeat intent. |
| Falsification Criterion | Customers cannot identify an accountable party or reject partial fulfilment/remediation as unfair. |
| Montreal-Specific Consideration | Test bilingual communications and the actual commercial-provider model used in the Montreal pilot. |
| Priority | P1 |
| Status | UNTESTED |
| Primary Evidence Module | MI-05 + MI-11 |
| Version Control | v1.0; v1.0 priority P1; Source priority retained; interpreted within the assigned validation phase. |

#### TRUST-006 — Service recovery

| Field | Registered control |
| --- | --- |
| ID | TRUST-006 |
| Market Side | Service recovery |
| Validation Phase | CROSS_PHASE |
| Hypothesis | A clear complaint, refund and remediation process can restore trust at a cost the platform and providers can bear. |
| Why It Matters | Early incidents are inevitable; poor recovery accelerates churn and reputational harm. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | HIGH |
| Validation Method | Failure-scenario interviews and controlled pilot recovery with follow-up repeat measurement. |
| Evidence Needed | Resolution time, remedy preference/cost, satisfaction, repeat, chargeback and responsibility allocation. |
| Falsification Criterion | Credible recovery requires costs or guarantees that eliminate contribution or still fails to preserve repeat. |
| Montreal-Specific Consideration | Design Quebec consumer-facing disclosures only after primary-source verification; test both language journeys. |
| Priority | P1 |
| Status | UNTESTED |
| Primary Evidence Module | MI-05 + MI-09 |
| Version Control | v1.0; v1.0 priority P1; Source priority retained; interpreted within the assigned validation phase. |

### 5.8 Operating reliability

**Domain rule:** Track real failure, language and founder intervention—not only completed outcomes.

#### OPS-001 — Pickup behaviour

| Field | Registered control |
| --- | --- |
| ID | OPS-001 |
| Market Side | Pickup behaviour |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | Pickup-first fulfilment can attract enough customers to validate food demand while avoiding premature delivery subsidy. |
| Why It Matters | Pickup may be the cheapest launch mode, but convenience loss may shrink the addressable segment. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | HIGH |
| Validation Method | Offer pickup-only and pickup-versus-delivery choices in matched preorder tests and a real pilot. |
| Evidence Needed | Conversion, travel time, lateness, no-show, basket, satisfaction and repeat by mode. |
| Falsification Criterion | Pickup-only demand is too small or concentrated in a segment insufficient for cluster economics. |
| Montreal-Specific Consideration | Test walk, transit and car pickup contexts around actual candidate Kitchens, not hypothetical central points. |
| Priority | P1 |
| Status | UNTESTED |
| Primary Evidence Module | MI-05 + MI-10 |
| Version Control | v1.0; v1.0 priority P1; Source priority retained; interpreted within the assigned validation phase. |

#### OPS-002 — Delivery operations

| Field | Registered control |
| --- | --- |
| ID | OPS-002 |
| Market Side | Delivery operations |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | A delivery partner or controlled courier model can meet cost, coverage, food-handling, tracking and reliability needs. |
| Why It Matters | Provider availability does not guarantee workable marketplace terms or service quality. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Obtain indicative terms, run route pilots, and compare partner, courier, customer pickup and scheduled drop models. |
| Evidence Needed | Coverage, cost, SLA, integration/ops burden, claims, cancellation, tracking and food-handling constraints. |
| Falsification Criterion | No available model satisfies both economics and required service reliability for the launch cluster. |
| Montreal-Specific Consideration | Validate Montreal coverage and service windows using actual origins/destinations; retest seasonal conditions. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-09 + MI-10 |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### OPS-003 — Order operations

| Field | Registered control |
| --- | --- |
| ID | OPS-003 |
| Market Side | Order operations |
| Validation Phase | LATER |
| Hypothesis | A same-Kitchen multi-Chef order can be accepted, synchronized, staged and handed off without delay or error exceeding customer tolerance. |
| Why It Matters | The feature couples multiple independent preparation workflows to one fulfilment promise. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | HIGH |
| Validation Method | Timed tabletop simulation, then limited live pilot with exception injection. |
| Evidence Needed | Acceptance latency, readiness spread, staging time, missing items, handoff error, courier wait and support contacts. |
| Falsification Criterion | Coordination cost/failure materially exceeds single-Chef orders and cannot be reduced with simple process controls. |
| Montreal-Specific Consideration | Test within a real Montreal Kitchen layout and staffing model; do not infer from software workflow alone. |
| Priority | P1 |
| Status | UNTESTED |
| Primary Evidence Module | MI-10 + MI-11 |
| Version Control | v1.0; v1.0 priority P1; Source priority retained; interpreted within the assigned validation phase. |

#### OPS-004 — Reliability / Cancellation

| Field | Registered control |
| --- | --- |
| ID | OPS-004 |
| Market Side | Reliability / Cancellation |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | Chef, Kitchen and fulfilment cancellations can be kept low enough that customers and providers continue to participate. |
| Why It Matters | A three-layer supply chain multiplies cancellation points and remediation cost. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Pilot reliability ledger with reason codes and leading indicators; stress test late capacity loss. |
| Evidence Needed | Acceptance, provider cancellation, Kitchen cancellation, courier failure, customer cancellation, notice time and recovery. |
| Falsification Criterion | Combined cancellation/failure remains above the repeat and margin tolerance defined in MI-09/MI-11. |
| Montreal-Specific Consideration | Report by provider, Kitchen, cluster and daypart; do not hide correlated failures in averages. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-10 + MI-11 |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### OPS-005 — Capacity dependency

| Field | Registered control |
| --- | --- |
| ID | OPS-005 |
| Market Side | Capacity dependency |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | Food availability can be tied to confirmed or credibly controllable Kitchen capacity without creating unacceptable listing gaps or customer cancellations. |
| Why It Matters | The product vision permits future meal demand before capacity, but commercial fulfilment requires real space. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Operational dependency map and pilot scheduling with fallback capacity scenarios. |
| Evidence Needed | Listings backed by capacity, lead time, rebooking success, at-risk occurrences and customer remediation. |
| Falsification Criterion | Capacity uncertainty makes reliable food availability too sparse or creates frequent late cancellations. |
| Montreal-Specific Consideration | Use actual Kitchen booking horizons and travel-time-compatible fallbacks in the selected cluster. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-07 + MI-10 |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### OPS-006 — Platform operations

| Field | Registered control |
| --- | --- |
| ID | OPS-006 |
| Market Side | Platform operations |
| Validation Phase | CROSS_PHASE |
| Hypothesis | Manual onboarding, scheduling, support, verification, dispute and reconciliation work can be reduced to a scalable cost after learning. |
| Why It Matters | A concierge pilot can mask an operations-heavy business that software will not fully automate. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | HIGH |
| Validation Method | Time-driven activity costing and root-cause analysis through each pilot transaction type. |
| Evidence Needed | Staff minutes/case, rework, exception type, automation potential, quality risk and cost per successful transaction. |
| Falsification Criterion | Required human work remains too high or too judgment-sensitive for the viable platform take. |
| Montreal-Specific Consideration | Include bilingual support and Montreal operator/provider communication needs in workload measurement. |
| Priority | P1 |
| Status | UNTESTED |
| Primary Evidence Module | MI-09 + MI-11 |
| Version Control | v1.0; v1.0 priority P1; Source priority retained; interpreted within the assigned validation phase. |

#### OPS-007 — Seasonality / Resilience

| Field | Registered control |
| --- | --- |
| ID | OPS-007 |
| Market Side | Seasonality / Resilience |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | Demand, pickup/delivery behaviour and supply reliability remain workable across Montreal seasonal and weather conditions. |
| Why It Matters | A short fair-weather pilot may overstate pickup willingness, delivery performance or Chef reliability. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No primary evidence has been collected; this claim is untested. |
| Confidence | VERY LOW |
| Business Impact if False | HIGH |
| Validation Method | Plan longitudinal validation and scenario stress tests; compare actual cohorts as seasons change. |
| Evidence Needed | Conversion, mode share, ETA/cost, cancellations, no-show and repeat by season/weather band. |
| Falsification Criterion | The viable model disappears for a material part of the year without uneconomic subsidy or operational redesign. |
| Montreal-Specific Consideration | Do not declare this validated from a single-season pilot; adverse-weather evidence is required later. |
| Priority | P1 |
| Status | UNTESTED |
| Primary Evidence Module | MI-10 + MI-11 |
| Version Control | v1.0; v1.0 priority P1; Source priority retained; interpreted within the assigned validation phase. |

#### OPS-008 — Operating model / language

| Field | Registered control |
| --- | --- |
| ID | OPS-008 |
| Market Side | Operating model / language |
| Validation Phase | PHASE_1_CHEF_KITCHEN |
| Hypothesis | Phase-1 discovery, Kitchen listings, booking terms, support and incident handling can operate credibly in French for the selected Montreal participants. |
| Why It Matters | French-language failure can restrict recruitment, comprehension, trust and lawful operability even if the matching concept is otherwise viable. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal evidence has been collected; the record was added in v1.1 as a validation-control gap. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Audit every Phase-1 participant touchpoint; test French outreach and one end-to-end booking journey with native/qualified review before public launch. |
| Evidence Needed | French materials, comprehension/error log, response/conversion by language, support readiness and unresolved translation or legal-language dependencies. |
| Falsification Criterion | Material participant journeys or required notices cannot be delivered accurately within Phase-1 time/budget, or French execution causes disqualifying misunderstanding or exclusion. |
| Montreal-Specific Consideration | French is an operating requirement in Montreal; the exact legal obligations remain for authoritative primary-source verification. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-07 + regulatory screen |
| Version Control | v1.1 NEW; v1.0 priority N/A; New v1.1 record created to close a verified control gap. |

#### OPS-009 — Operating model / pilot exceptionalism

| Field | Registered control |
| --- | --- |
| ID | OPS-009 |
| Market Side | Operating model / pilot exceptionalism |
| Validation Phase | PHASE_1_CHEF_KITCHEN |
| Hypothesis | Phase-1 matching and booking can be repeated without exceptional founder relationships, unlogged manual rescue or labour that would make the apparent model uneconomic. |
| Why It Matters | A concierge pilot can manufacture success by concealing the work, persuasion and special access needed for every transaction. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal evidence has been collected; the record was added in v1.1 as a validation-control gap. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Log every founder action and minute by transaction, label exception/rescue work, apply a pre-registered shadow labour rate and repeat without relationship-specific concessions. |
| Evidence Needed | Founder minutes, touch count, exception category, dependency on personal relationship, shadow cost, automation candidate and booking outcome. |
| Falsification Criterion | Completed bookings require manual effort or exceptional access above the locked operating tolerance, and no credible lower-cost process or narrowed model resolves it. |
| Montreal-Specific Consideration | Travel, bilingual support and site-access coordination must be recorded rather than treated as free local-founder effort. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-09 + MI-10 |
| Version Control | v1.1 NEW; v1.0 priority N/A; New v1.1 record created to close a verified control gap. |

### 5.9 Dietitian professional services

**Domain rule:** Later, distinct professional-services market; not a Phase-1 blocker.

#### DIET-001 — Dietitian customer demand

| Field | Registered control |
| --- | --- |
| ID | DIET-001 |
| Market Side | Dietitian customer demand |
| Validation Phase | LATER |
| Hypothesis | A reachable Montreal customer segment has enough unmet need to pay for Dietitian consultations through a marketplace. |
| Why It Matters | Professional-service functionality has no launch value without distinct paid demand. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | HIGH |
| Validation Method | Problem interviews with recent service-seeking evidence, then a compliant booking/deposit test. |
| Evidence Needed | Need frequency, current alternatives, referral path, booking conversion, price and reason for trust/non-purchase. |
| Falsification Criterion | Target users lack a paid need or prefer established clinical/referral channels they will not switch from. |
| Montreal-Specific Consideration | Recruit only within validated professional scope and across relevant language journeys. |
| Priority | P1 |
| Status | UNTESTED |
| Primary Evidence Module | MI-05 + Dietitian discovery |
| Version Control | v1.0; v1.0 priority P1; Source priority retained; interpreted within the assigned validation phase. |

#### DIET-002 — Dietitian product fit

| Field | Registered control |
| --- | --- |
| ID | DIET-002 |
| Market Side | Dietitian product fit |
| Validation Phase | LATER |
| Hypothesis | Customers are willing to book professional consultations in the same product used to buy prepared food. |
| Why It Matters | A combined experience may create convenience or may reduce perceived clinical/professional trust. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | HIGH |
| Validation Method | Brand/flow concept comparison: integrated, endorsed referral, and separate professional-service experience. |
| Evidence Needed | Trust, comprehension, booking intent/action, perceived independence and cross-use. |
| Falsification Criterion | Integration materially lowers professional trust or does not improve acquisition/retention for either service. |
| Montreal-Specific Consideration | Test terminology and expectations in French/English; avoid implying medical care beyond scope. |
| Priority | P1 |
| Status | UNTESTED |
| Primary Evidence Module | MI-05 + Dietitian discovery |
| Version Control | v1.0; v1.0 priority P1; Source priority retained; interpreted within the assigned validation phase. |

#### DIET-003 — Dietitian supply

| Field | Registered control |
| --- | --- |
| ID | DIET-003 |
| Market Side | Dietitian supply |
| Validation Phase | LATER |
| Hypothesis | Qualified Dietitians can be acquired and activated at a cost and pace compatible with a Montreal launch. |
| Why It Matters | Professional supply may require credential checks, privacy processes and referral trust not shared with Chef acquisition. |
| Current Classification | ASSUMPTION |
| Current Evidence | The specification flags this as an unresolved launch gate; no authoritative Quebec or municipal verification has been completed. |
| Confidence | VERY LOW |
| Business Impact if False | HIGH |
| Validation Method | Professional interviews, compliant onboarding walkthrough, and signed pilot availability. |
| Evidence Needed | Qualified leads, completion, credential evidence, time/cost to activate, available appointment hours and objections. |
| Falsification Criterion | Too few eligible professionals commit or onboarding cost/time is incompatible with launch economics. |
| Montreal-Specific Consideration | Verify Quebec eligibility/title scope before recruiting or advertising services. |
| Priority | P1 |
| Status | UNTESTED |
| Primary Evidence Module | Dietitian discovery + regulatory workstream |
| Version Control | v1.0; v1.0 priority P1; Source priority retained; interpreted within the assigned validation phase. |

#### DIET-004 — Dietitian trust / Pricing

| Field | Registered control |
| --- | --- |
| ID | DIET-004 |
| Market Side | Dietitian trust / Pricing |
| Validation Phase | LATER |
| Hypothesis | Customers and Dietitians accept transparent consultation pricing and platform fees while maintaining professional trust. |
| Why It Matters | Price, fee presentation and platform intermediation can affect both demand and professional participation. |
| Current Classification | HYPOTHESIS |
| Current Evidence | The product specification leaves commercial terms configurable/TBD; no willingness-to-pay evidence was supplied. |
| Confidence | VERY LOW |
| Business Impact if False | HIGH |
| Validation Method | Separate customer WTP and professional fee-card tests, followed by paid appointments if compliant. |
| Evidence Needed | Booking conversion, net Dietitian revenue, fee comprehension, cancellation/no-show and repeat. |
| Falsification Criterion | No price/fee structure supports customer demand, Dietitian economics and platform cost. |
| Montreal-Specific Consideration | Use Montreal/Quebec professional context and CAD totals only after scope verification. |
| Priority | P1 |
| Status | UNTESTED |
| Primary Evidence Module | MI-08 + Dietitian discovery |
| Version Control | v1.0; v1.0 priority P1; Source priority retained; interpreted within the assigned validation phase. |

#### DIET-005 — Dietitian retention / Meal plans

| Field | Registered control |
| --- | --- |
| ID | DIET-005 |
| Market Side | Dietitian retention / Meal plans |
| Validation Phase | LATER |
| Hypothesis | Consultations and meal-plan guidance lead to repeat professional engagement or measurably improve relevant food discovery/retention. |
| Why It Matters | One-off consultations may not justify acquisition and privacy overhead; food linkage may be weak. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | MEDIUM |
| Validation Method | Small compliant longitudinal pilot measuring repeat consultation and customer-controlled use of structured meal requirements. |
| Evidence Needed | Repeat bookings, authorized requirement use, food search/purchase lift, satisfaction, outcomes claimed and privacy concerns. |
| Falsification Criterion | Professional engagement is predominantly one-off and produces no measurable benefit to the core food marketplace. |
| Montreal-Specific Consideration | Separate professional value from food-sale attribution; no Dietitian commission on Chef selection is assumed. |
| Priority | P2 |
| Status | UNTESTED |
| Primary Evidence Module | Dietitian pilot + MI-11 |
| Version Control | v1.0; v1.0 priority P2; Source priority retained; interpreted within the assigned validation phase. |

#### DIET-006 — Dietitian regulation / Privacy

| Field | Registered control |
| --- | --- |
| ID | DIET-006 |
| Market Side | Dietitian regulation / Privacy |
| Validation Phase | LATER |
| Hypothesis | Credential, consent, privacy, record-handling and professional-practice obligations can be met without turning the MVP into an inappropriate clinical-record system. |
| Why It Matters | The compliance surface may be disproportionate to uncertain demand. |
| Current Classification | ASSUMPTION |
| Current Evidence | The specification flags this as an unresolved launch gate; no authoritative Quebec or municipal verification has been completed. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Authoritative primary-source and qualified professional/legal/privacy review; data-flow minimization exercise. |
| Evidence Needed | Required data, consent, records, retention, access, storage, jurisdiction, professional liability and operating cost. |
| Falsification Criterion | Compliant service requires data/process scope, cost, risk or delay incompatible with the launch plan. |
| Montreal-Specific Consideration | Use Quebec-specific professional and privacy sources; do not infer from other provinces. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | Regulatory workstream before pilot |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### DIET-007 — Launch scope

| Field | Registered control |
| --- | --- |
| ID | DIET-007 |
| Market Side | Launch scope |
| Validation Phase | LATER |
| Hypothesis | Dietitian services strengthen the launch wedge more than they distract capital, trust design and operations from the food/Kitchen core. |
| Why It Matters | A valid future market can still be the wrong MVP scope. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal market evidence was supplied. The product specification describes intended capability only. |
| Confidence | VERY LOW |
| Business Impact if False | HIGH |
| Validation Method | Compare integrated and deferred scope using incremental demand, retention, economics, compliance effort and implementation cost. |
| Evidence Needed | Cross-sell/retention lift, unique acquisition, contribution, delay, risk and management bandwidth. |
| Falsification Criterion | Dietitian scope does not improve a P0 core metric enough to justify its incremental cost/risk. |
| Montreal-Specific Consideration | Make the decision for Montreal launch only; later expansion implications remain secondary. |
| Priority | P1 |
| Status | UNTESTED |
| Primary Evidence Module | MI-11 Business Model Stress Test |
| Version Control | v1.0; v1.0 priority P1; Source priority retained; interpreted within the assigned validation phase. |

### 5.10 Competitive hypotheses

**Domain rule:** Phase-1 alternatives include direct operator sites, listings, networks and direct agreements.

#### COMP-001 — Customer competition

| Field | Registered control |
| --- | --- |
| ID | COMP-001 |
| Market Side | Customer competition |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | For the chosen customer job, direct and indirect alternatives leave a material gap on at least one decisive dimension that Cheffy Bites can credibly own. |
| Why It Matters | A crowded category is survivable only if a specific switching gap exists. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No competitor evidence has been collected; the full study is intentionally deferred to MI-03. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | MI-03 occasion-based competitor mapping, mystery shopping and total-experience comparison; no full study in MI-01. |
| Evidence Needed | Price/fees, assortment, speed, trust, dietary fit, authenticity, customization, availability and repeat mechanisms. |
| Falsification Criterion | Existing alternatives meet the job as well or better on the attributes that actually drive purchase. |
| Montreal-Specific Consideration | Compare options available to the same Montreal cluster/customer, including informal/community supply where observable and lawful to assess. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-03 Competitor Intelligence |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### COMP-002 — Chef competition

| Field | Registered control |
| --- | --- |
| ID | COMP-002 |
| Market Side | Chef competition |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | Cheffy Bites offers qualified Chefs superior incremental economics or access versus direct social selling, catering, meal-prep channels, employment and other marketplaces. |
| Why It Matters | Chefs will multi-home or reject the platform if alternatives offer better demand with less control/fee burden. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No competitor evidence has been collected; the full study is intentionally deferred to MI-03. |
| Confidence | VERY LOW |
| Business Impact if False | HIGH |
| Validation Method | MI-03 supply-channel comparison plus Chef evidence on actual current channels. |
| Evidence Needed | Net contribution, acquisition effort, control, payment risk, customer ownership, compliance burden and volume. |
| Falsification Criterion | Cheffy Bites cannot beat or complement existing Chef channels on net value. |
| Montreal-Specific Consideration | Measure alternatives actually used by Montreal Chefs, not global platform feature lists. |
| Priority | P1 |
| Status | UNTESTED |
| Primary Evidence Module | MI-03 + MI-06 |
| Version Control | v1.0; v1.0 priority P1; Source priority retained; interpreted within the assigned validation phase. |

#### COMP-003 — Kitchen competition

| Field | Registered control |
| --- | --- |
| ID | COMP-003 |
| Market Side | Kitchen competition |
| Validation Phase | PHASE_1_CHEF_KITCHEN |
| Hypothesis | The Kitchen marketplace improves discovery, booking reliability or utilization economics versus brokers, shared-Kitchen operators and direct relationships. |
| Why It Matters | Existing direct or managed models may solve the problem without a marketplace layer. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No competitor evidence has been collected; the full study is intentionally deferred to MI-03. |
| Confidence | VERY LOW |
| Business Impact if False | HIGH |
| Validation Method | MI-03 operator/channel mapping and bilateral transaction walkthroughs. |
| Evidence Needed | Inventory transparency, price, contract length, flexibility, verification, booking success, admin and disintermediation. |
| Falsification Criterion | Existing channels already match supply/demand efficiently or repeat relationships predictably bypass Cheffy Bites. |
| Montreal-Specific Consideration | Focus on capacity accessible to the target Montreal Chef segment, not all commercial real estate. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-03 + MI-07 |
| Version Control | v1.0; v1.0 priority P1; Priority changed for phase-relative control. |

#### COMP-004 — Dietitian competition

| Field | Registered control |
| --- | --- |
| ID | COMP-004 |
| Market Side | Dietitian competition |
| Validation Phase | LATER |
| Hypothesis | The integrated marketplace can acquire and serve Dietitian customers better than referrals, clinics, directories, telehealth or professional booking tools. |
| Why It Matters | The professional-services extension may lack a credible acquisition or trust advantage. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No competitor evidence has been collected; the full study is intentionally deferred to MI-03. |
| Confidence | VERY LOW |
| Business Impact if False | MEDIUM |
| Validation Method | MI-03 journey comparison and later customer/professional discovery. |
| Evidence Needed | Referral source, trust, scope, price, wait time, continuity, privacy and booking conversion. |
| Falsification Criterion | Established professional channels dominate on trust/access and integration adds no measurable value. |
| Montreal-Specific Consideration | Compare Quebec/Montreal-available services and language access; do not infer from out-of-market platforms. |
| Priority | P2 |
| Status | UNTESTED |
| Primary Evidence Module | MI-03 + Dietitian discovery |
| Version Control | v1.0; v1.0 priority P2; Source priority retained; interpreted within the assigned validation phase. |

#### COMP-005 — Differentiation durability

| Field | Registered control |
| --- | --- |
| ID | COMP-005 |
| Market Side | Differentiation durability |
| Validation Phase | CROSS_PHASE |
| Hypothesis | Any validated advantage (Chef identity, shared-Kitchen assortment, specialization, community, recurring plan or supply access) is difficult enough to copy or is reinforced by local liquidity. |
| Why It Matters | A feature-only advantage can be copied before acquisition payback. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No competitor evidence has been collected; the full study is intentionally deferred to MI-03. |
| Confidence | VERY LOW |
| Business Impact if False | HIGH |
| Validation Method | MI-03 capability/response analysis and MI-11 strategic stress test. |
| Evidence Needed | Competitor capability, switching costs, exclusivity, network effects, local relationships and time-to-copy. |
| Falsification Criterion | The only valued differentiation is readily copied or already available without marketplace complexity. |
| Montreal-Specific Consideration | Assess defensibility at the cluster/community level before claiming Montreal-wide network effects. |
| Priority | P1 |
| Status | UNTESTED |
| Primary Evidence Module | MI-03 + MI-11 |
| Version Control | v1.0; v1.0 priority P1; Source priority retained; interpreted within the assigned validation phase. |

### 5.11 Regulatory, insurance and responsibility assumptions

**Domain rule:** Alerts only until authoritative verification; no legal conclusions.

#### REG-001 — Food regulation

| Field | Registered control |
| --- | --- |
| ID | REG-001 |
| Market Side | Food regulation |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | The proposed commercial preparation, listing and sale workflows can be operated lawfully in Montreal using qualified Kitchens and providers without prohibitive process or cost. |
| Why It Matters | The food marketplace cannot launch if the operating model conflicts with applicable requirements. |
| Current Classification | ASSUMPTION |
| Current Evidence | The specification flags this as an unresolved launch gate; no authoritative Quebec or municipal verification has been completed. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Authoritative Quebec and municipal primary-source review, then qualified legal/food-safety interpretation and operator confirmation. |
| Evidence Needed | Required permits/licences, inspections, training, location rules, responsibilities, claims, records and enforcement implications. |
| Falsification Criterion | The model is prohibited or required remediation makes target provider economics, supply or launch timing infeasible. |
| Montreal-Specific Consideration | Verify the exact Montreal/Quebec scope and the chosen fulfilment model; do not provide legal conclusions in MI-01. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-02 regulatory workstream |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### REG-002 — Kitchen regulation

| Field | Registered control |
| --- | --- |
| ID | REG-002 |
| Market Side | Kitchen regulation |
| Validation Phase | PHASE_1_CHEF_KITCHEN |
| Hypothesis | A licensed Kitchen operator can authorize multiple external Chefs and rentable spaces under a workable compliance and accountability structure. |
| Why It Matters | The core shared-Kitchen model depends on more than a facility licence. |
| Current Classification | ASSUMPTION |
| Current Evidence | The specification flags this as an unresolved launch gate; no authoritative Quebec or municipal verification has been completed. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Primary-source review plus facility-specific confirmation of operator authority, occupancy, sanitation and inspection requirements. |
| Evidence Needed | Operator/provider responsibilities, concurrency limits, records, access, cleaning, storage and enforcement treatment. |
| Falsification Criterion | External multi-Chef use is not permitted for enough facilities or requires controls that eliminate usable capacity. |
| Montreal-Specific Consideration | Verify per facility/use case in Montreal; a Kitchen existing or being licensed is not proof of rentable capacity. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-02 + MI-07 |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### REG-003 — Provider / Organization structure

| Field | Registered control |
| --- | --- |
| ID | REG-003 |
| Market Side | Provider / Organization structure |
| Validation Phase | CROSS_PHASE |
| Hypothesis | Independent vendor, Organization-affiliated Chef, commercial-provider and settlement arrangements can be structured without unacceptable worker-classification, authorization or payroll conflict. |
| Why It Matters | The specification distinguishes performer, provider and payee, but the lawful commercial relationship remains unverified. |
| Current Classification | ASSUMPTION |
| Current Evidence | The specification flags this as an unresolved launch gate; no authoritative Quebec or municipal verification has been completed. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Qualified Quebec legal/accounting review of proposed scenarios; validate actual operator/provider contracts later. |
| Evidence Needed | Worker status factors, contracting authority, payroll boundary, settlement beneficiary, tax/reporting and platform control. |
| Falsification Criterion | Core provider models create unacceptable classification, control, liability or settlement exposure. |
| Montreal-Specific Consideration | Analyze independent, Organization-operated and Cheffy-operated bootstrap scenarios separately in Quebec. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-02 regulatory workstream |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### REG-004 — Insurance / Liability

| Field | Registered control |
| --- | --- |
| ID | REG-004 |
| Market Side | Insurance / Liability |
| Validation Phase | CROSS_PHASE |
| Hypothesis | Cheffy Bites, Kitchens, providers and delivery parties can obtain and allocate required insurance/liability coverage at tolerable cost. |
| Why It Matters | Uninsured or ambiguous risk can block operator participation and threaten the company. |
| Current Classification | ASSUMPTION |
| Current Evidence | The specification flags this as an unresolved launch gate; no authoritative Quebec or municipal verification has been completed. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Broker quotes and qualified legal review using defined incident scenarios and party roles. |
| Evidence Needed | Coverage, exclusions, limits, additional insured requirements, claims process, premiums and indemnities. |
| Falsification Criterion | Essential coverage is unavailable, exclusions defeat the model, or cost breaks unit economics. |
| Montreal-Specific Consideration | Use Quebec policies and the exact Montreal operating model; include foodborne illness, property, professional and delivery scenarios as applicable. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-02 + MI-09 |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### REG-005 — Delivery regulation / Operations

| Field | Registered control |
| --- | --- |
| ID | REG-005 |
| Market Side | Delivery regulation / Operations |
| Validation Phase | PHASE_2_CUSTOMER_FOOD |
| Hypothesis | Pickup and delivery workflows can meet applicable food handling, consumer disclosure, contracting and incident responsibilities. |
| Why It Matters | Last-mile design changes the regulatory, quality and liability surface. |
| Current Classification | ASSUMPTION |
| Current Evidence | The specification flags this as an unresolved launch gate; no authoritative Quebec or municipal verification has been completed. |
| Confidence | VERY LOW |
| Business Impact if False | HIGH |
| Validation Method | Primary-source review and delivery-partner terms assessment before a delivery pilot. |
| Evidence Needed | Handling/temperature duties, labelling, custody, contractor roles, customer communication, claims and records. |
| Falsification Criterion | Compliant delivery requires cost/process incompatible with the viable basket and service window. |
| Montreal-Specific Consideration | Assess pickup and each delivery model separately for Montreal operations. |
| Priority | P1 |
| Status | UNTESTED |
| Primary Evidence Module | MI-02 + MI-10 |
| Version Control | v1.0; v1.0 priority P1; Source priority retained; interpreted within the assigned validation phase. |

#### REG-006 — Dietitian professional practice / Privacy

| Field | Registered control |
| --- | --- |
| ID | REG-006 |
| Market Side | Dietitian professional practice / Privacy |
| Validation Phase | LATER |
| Hypothesis | The proposed Dietitian marketplace can meet Quebec title, eligibility, consent, privacy, record and professional-service requirements within a narrow MVP. |
| Why It Matters | Professional and health-related data obligations may overwhelm the launch wedge. |
| Current Classification | ASSUMPTION |
| Current Evidence | The specification flags this as an unresolved launch gate; no authoritative Quebec or municipal verification has been completed. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Authoritative professional-order/privacy primary sources plus qualified professional/legal/privacy review. |
| Evidence Needed | Title use, jurisdiction, credential display, consent, records, retention, security, cross-border service and liability. |
| Falsification Criterion | Required scope, controls, risk or cost is incompatible with launch resources or the proposed product boundary. |
| Montreal-Specific Consideration | Quebec-specific verification is mandatory; no legal conclusion is made here. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-02 regulatory workstream |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### REG-007 — Consumer / Tax / Payment regulation

| Field | Registered control |
| --- | --- |
| ID | REG-007 |
| Market Side | Consumer / Tax / Payment regulation |
| Validation Phase | CROSS_PHASE |
| Hypothesis | Tax, consumer protection, fee disclosure, subscription, refund, chargeback, privacy and marketplace-payment responsibilities can be assigned and operated at tolerable cost. |
| Why It Matters | Unresolved Merchant-of-Record and consumer obligations can change prices, liability and operating scope. |
| Current Classification | ASSUMPTION |
| Current Evidence | The specification flags this as an unresolved launch gate; no authoritative Quebec or municipal verification has been completed. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Qualified Quebec/Canada legal, tax, accounting, privacy and payment-provider review of each transaction type. |
| Evidence Needed | Tax/remittance, seller identity, fee display, auto-renewal, refunds, reserves, disputes, data handling and settlement rules. |
| Falsification Criterion | Required legal/payment posture makes the viable fee model, cash flow or launch timing unacceptable. |
| Montreal-Specific Consideration | Review food orders, Kitchen bookings, subscriptions and professional services separately; do not assume one treatment. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | MI-02 + MI-09 |
| Version Control | v1.0; v1.0 priority P0; Source priority retained; interpreted within the assigned validation phase. |

#### REG-008 — Regulatory / contracting / responsibility

| Field | Registered control |
| --- | --- |
| ID | REG-008 |
| Market Side | Regulatory / contracting / responsibility |
| Validation Phase | PHASE_1_CHEF_KITCHEN |
| Hypothesis | Phase-1 can assign, communicate and operationalize responsibility for booking contracts, payment, cancellation, refund, damage, complaint handling, insurance evidence and incidents on acceptable terms. |
| Why It Matters | Ambiguous platform/operator/Chef responsibility can block bookings or create exposure that overwhelms a low-budget pilot. |
| Current Classification | HYPOTHESIS |
| Current Evidence | No attributable Montreal evidence has been collected; the record was added in v1.1 as a validation-control gap. |
| Confidence | VERY LOW |
| Business Impact if False | FATAL |
| Validation Method | Build a responsibility matrix from proposed booking flows; verify primary sources and obtain narrowly scoped qualified professional review before taking live payments where required. |
| Evidence Needed | Named contracting parties, merchant/payment role, cancellation/refund rules, damage process, complaint escalation, insurance evidence, incident owner and unresolved legal questions. |
| Falsification Criterion | Required roles cannot be assigned on terms accepted by operators and Chefs, or authoritative review identifies an exposure incompatible with the Phase-1 model and funding boundary. |
| Montreal-Specific Consideration | Separate Kitchen-booking requirements from later food-sale, delivery and consumer meal-remedy requirements; do not import Phase-2 conclusions into Phase 1. |
| Priority | P0 |
| Status | UNTESTED |
| Primary Evidence Module | Primary-source regulatory screen |
| Version Control | v1.1 NEW; v1.0 priority N/A; New v1.1 record created to close a verified control gap. |

## 6. Phase-1 P0 dashboard

The dashboard contains **27** P0 gates: records assigned to Phase 1 plus cross-phase records that can block a Phase-1 booking GO. Register distribution: CROSS_PHASE 11, LATER 17, PHASE_1_CHEF_KITCHEN 29, PHASE_2_CUSTOMER_FOOD 42.

| ID | Control question | Phase | Impact | Status | Evidence module |
| --- | --- | --- | --- | --- | --- |
| CHEF-001 | A sufficient pool of qualified independent Chefs has recurring difficulty obtaining compliant commercial Kitchen capacity at useful times. | PHASE_1_CHEF_KITCHEN | FATAL | UNTESTED | MI-06 Chef Discovery |
| CHEF-004 | Chefs will purchase Kitchen capacity in a workable format (hourly, recurring block, or subscription) and at a total cost compatible with menu economics. | PHASE_1_CHEF_KITCHEN | FATAL | UNTESTED | MI-06 + MI-08 |
| CHEF-005 | Chefs accept a platform commission or fee when total incremental contribution and service value exceed their direct-channel alternative. | PHASE_1_CHEF_KITCHEN | FATAL | UNTESTED | MI-08 + MI-09 |
| CHEF-010 | Qualified Montreal Chefs who complete an initial Kitchen booking make repeat bookings and remain active when capacity, terms, access and support meet their operating needs. | PHASE_1_CHEF_KITCHEN | FATAL | UNTESTED | MI-06 + MI-10 |
| COMP-003 | The Kitchen marketplace improves discovery, booking reliability or utilization economics versus brokers, shared-Kitchen operators and direct relationships. | PHASE_1_CHEF_KITCHEN | HIGH | UNTESTED | MI-03 + MI-07 |
| ECO-010 | Kitchen rental revenue exceeds incremental staffing, utilities, cleaning, wear, insurance, disruption and platform fees at achieved utilization. | PHASE_1_CHEF_KITCHEN | FATAL | UNTESTED | MI-07 + MI-09 |
| ECO-013 | Phase-1 booking settlement timing, deposits, refunds, disputes, damage exposure and reserves can be funded within the founder's cash constraint without hiding losses or transferring unacceptable risk. | PHASE_1_CHEF_KITCHEN | FATAL | UNTESTED | MI-09 Unit Economics |
| KIT-001 | Montreal has meaningful unused licensed commercial Kitchen space/unit capacity that is actually available for third-party commercial use. | PHASE_1_CHEF_KITCHEN | FATAL | UNTESTED | MI-07 Kitchen Operator Discovery |
| KIT-002 | Unused Kitchen capacity occurs at times that overlap Chef production needs and customer demand windows. | PHASE_1_CHEF_KITCHEN | FATAL | UNTESTED | MI-07 + MI-10 |
| KIT-003 | Authorized Kitchen operators are willing to host external independent and Organization-affiliated Chefs under standardized marketplace terms. | PHASE_1_CHEF_KITCHEN | FATAL | UNTESTED | MI-07 Kitchen Operator Discovery |
| KIT-004 | Hourly rates, minimum bookings, cleaning time/fees, deposits and equipment charges can satisfy operators while remaining viable for Chefs. | PHASE_1_CHEF_KITCHEN | FATAL | UNTESTED | MI-07 + MI-08 + MI-09 |
| KIT-005 | Available spaces have the equipment, storage, utilities, receiving, packaging and staging capabilities required by target Chef menus. | PHASE_1_CHEF_KITCHEN | HIGH | UNTESTED | MI-07 Kitchen Operator Discovery |
| KIT-006 | Insurance, liability allocation, food-safety controls and incident handling can be made acceptable to operators and providers at tolerable cost. | PHASE_1_CHEF_KITCHEN | FATAL | UNTESTED | MI-07 + regulatory workstream |
| KIT-009 | Advertised Kitchen availability remains confirmed, accessible and usable when a qualified Chef attempts to book it; reliability-adjusted supply is sufficient for Phase-1 matching. | PHASE_1_CHEF_KITCHEN | FATAL | UNTESTED | MI-07 + MI-10 |
| KIT-011 | Operators continue exposing genuine bookable capacity after initial onboarding and completed bookings under the proposed workload, economics and risk allocation. | PHASE_1_CHEF_KITCHEN | FATAL | UNTESTED | MI-07 + MI-10 |
| LIQ-003 | The launch cluster has enough compatible Kitchen spaces and hours to activate and retain its required Chef supply. | PHASE_1_CHEF_KITCHEN | FATAL | UNTESTED | MI-07 + MI-10 |
| LIQ-007 | Chefs can identify and book an acceptable space that matches equipment, schedule, price, location and compliance needs with low failure. | PHASE_1_CHEF_KITCHEN | HIGH | UNTESTED | MI-06 + MI-07 + MI-10 |
| LIQ-011 | Phase-1 liquidity exists within a practical service cell where Chef need, Kitchen, usable capacity, required equipment, daypart, geography, booking duration and compliance constraints overlap simultaneously. | PHASE_1_CHEF_KITCHEN | FATAL | UNTESTED | MI-10 Marketplace Liquidity |
| LIQ-012 | The no-result rate for qualified Phase-1 Chef searches is low enough for continued use, and reason codes reveal remediable rather than structural supply gaps. | PHASE_1_CHEF_KITCHEN | FATAL | UNTESTED | MI-10 Marketplace Liquidity |
| MON-002 | A Kitchen-booking fee can be charged to the Chef, operator or split without driving transactions off-platform or breaking bilateral economics. | PHASE_1_CHEF_KITCHEN | HIGH | UNTESTED | MI-07 + MI-08 + MI-09 |
| MON-008 | Cheffy Bites provides continuing value after a Chef and Kitchen meet, so repeat bookings remain on-platform at a fee structure both sides accept. | PHASE_1_CHEF_KITCHEN | FATAL | UNTESTED | MI-08 + MI-09 |
| OPS-008 | Phase-1 discovery, Kitchen listings, booking terms, support and incident handling can operate credibly in French for the selected Montreal participants. | PHASE_1_CHEF_KITCHEN | FATAL | UNTESTED | MI-07 + regulatory screen |
| OPS-009 | Phase-1 matching and booking can be repeated without exceptional founder relationships, unlogged manual rescue or labour that would make the apparent model uneconomic. | PHASE_1_CHEF_KITCHEN | FATAL | UNTESTED | MI-09 + MI-10 |
| REG-002 | A licensed Kitchen operator can authorize multiple external Chefs and rentable spaces under a workable compliance and accountability structure. | PHASE_1_CHEF_KITCHEN | FATAL | UNTESTED | MI-02 + MI-07 |
| REG-003 | Independent vendor, Organization-affiliated Chef, commercial-provider and settlement arrangements can be structured without unacceptable worker-classification, authorization or payroll conflict. | CROSS_PHASE | FATAL | UNTESTED | MI-02 regulatory workstream |
| REG-004 | Cheffy Bites, Kitchens, providers and delivery parties can obtain and allocate required insurance/liability coverage at tolerable cost. | CROSS_PHASE | FATAL | UNTESTED | MI-02 + MI-09 |
| REG-008 | Phase-1 can assign, communicate and operationalize responsibility for booking contracts, payment, cancellation, refund, damage, complaint handling, insurance evidence and incidents on acceptable terms. | PHASE_1_CHEF_KITCHEN | FATAL | UNTESTED | Primary-source regulatory screen |

A dashboard item remains open until its pre-registered criterion is addressed. A failed item triggers an immediate MODIFY/NO-GO review before further product spending.

## 7. Phase-1 fatal assumptions

### 7.1 Recurring Chef demand for external Kitchen capacity (CHEF-001, CHEF-004, CHEF-010)

| Control | Registered decision rule |
| --- | --- |
| Why existential | Without frequent, urgent need and repeat bookings, there is no durable transaction side to aggregate. |
| Evidence that invalidates | Qualified Chefs will not commit to realistic terms, complete bookings or repeat inside the locked window without uneconomic subsidy or founder persuasion. |
| Cheapest credible validation | Founder-led interviews followed by real service-cell offers, refundable commitments or small bookings and cohort follow-up. |
| Decision if invalidated | NO-GO for an open booking marketplace unless a narrower recurring-use segment supports MODIFY. |

### 7.2 Reliable, commercially usable Kitchen supply (KIT-001, KIT-002, KIT-003, KIT-005, KIT-009, KIT-011)

| Control | Registered decision rule |
| --- | --- |
| Why existential | Facilities and listings do not create supply unless capacity is compatible, confirmed, accessible and retained. |
| Evidence that invalidates | Reliability-adjusted slots within useful service cells remain insufficient or operators withdraw after initial participation. |
| Cheapest credible validation | Operator calls, site visits, slot-level inventory, reconfirmation and one or more real booking attempts. |
| Decision if invalidated | MODIFY to managed/anchored supply if one facility works; otherwise NO-GO for Phase 1. |

### 7.3 Bilateral price and Kitchen economics overlap (KIT-004, ECO-010)

| Control | Registered decision rule |
| --- | --- |
| Why existential | A match has no commercial value if the Chef ceiling is below the operator floor after real cleaning, access and staffing cost. |
| Evidence that invalidates | No material service cell closes the price/terms gap at positive operator economics and acceptable Chef economics. |
| Cheapest credible validation | Paired real-offer tests using disclosed slot terms; reconcile each completed or rejected offer to both parties' economics. |
| Decision if invalidated | MODIFY segment/duration/operating model; NO-GO if no credible overlap remains. |

### 7.4 Service-cell liquidity and diagnosable no-results (LIQ-003, LIQ-007, LIQ-011, LIQ-012)

| Control | Registered decision rule |
| --- | --- |
| Why existential | City-wide interest can coexist with near-zero matches once time, equipment, geography, duration and compliance are conjoined. |
| Evidence that invalidates | The locked usable-match/no-result criteria fail and dominant gaps cannot be corrected within constraints. |
| Cheapest credible validation | Spreadsheet inventory plus structured Chef requests and manual matching with reason codes. |
| Decision if invalidated | MODIFY to a smaller cell or managed inventory; NO-GO if viable cells cannot be found. |

### 7.5 Operable responsibility, insurance and regulatory path (KIT-006, REG-002, REG-003, REG-004, REG-008, OPS-008)

| Control | Registered decision rule |
| --- | --- |
| Why existential | Unclear contracting, liability, damage, language or facility rules can stop live bookings regardless of demand. |
| Evidence that invalidates | Authoritative or qualified review finds duties/exposure that cannot be assigned or met within the chosen model, budget and timeline. |
| Cheapest credible validation | Primary-source matrix, operator document review and narrowly scoped professional escalation only for unresolved fatal issues. |
| Decision if invalidated | MODIFY roles/payment/managed model if contained; otherwise NO-GO before taking live bookings. |

### 7.6 Continuing value and defensible monetization (CHEF-005, MON-002, MON-008, COMP-003)

| Control | Registered decision rule |
| --- | --- |
| Why existential | Free introductions may validate matching but not a business if repeat pairs bypass the platform or reject a viable fee. |
| Evidence that invalidates | Repeat pairs transact directly at the minimum sustainable fee and no low-cost continuing service changes that behaviour. |
| Cheapest credible validation | Present transparent repeat terms after a completed booking and observe the chosen rebooking channel. |
| Decision if invalidated | MODIFY to subscription/managed services or lead-generation economics; NO-GO if no viable continuing value exists. |

### 7.7 Cash exposure remains inside the funding boundary (ECO-013, REG-008)

| Control | Registered decision rule |
| --- | --- |
| Why existential | Deposits, payout timing, refunds, disputes and damage can consume more cash than the entire validation budget. |
| Evidence that invalidates | Required reserves or plausible incident exposure exceed the locked tolerance or remain unbounded. |
| Cheapest credible validation | Cash-timing map and downside scenarios using actual pilot terms before funds are handled. |
| Decision if invalidated | MODIFY payment/contract roles; NO-GO if safe transaction handling is unaffordable. |

### 7.8 Pilot success survives full founder-work accounting (OPS-009)

| Control | Registered decision rule |
| --- | --- |
| Why existential | Exceptional relationships and invisible labour can manufacture bookings that a marketplace cannot reproduce. |
| Evidence that invalidates | Bookings require founder effort, rescues or personal access above the locked tolerance with no credible process redesign. |
| Cheapest credible validation | Time-and-action log with shadow labour cost for every request and booking. |
| Decision if invalidated | MODIFY to a managed/high-touch model with matching economics, or NO-GO for a scalable marketplace claim. |

## 8. Long-term / Phase-2 fatal assumptions

| Fatal assumption | Linked IDs | Why it matters |
| --- | --- | --- |
| Repeat customer job | CUST-001, CUST-002, CUST-004 | A Montreal customer segment repeatedly buys for a specific job rather than trying a novelty. |
| All-in willingness to pay | CUST-005, ECO-001, ECO-009 | The checkout total supports customer conversion and Chef/provider economics. |
| Customer-food geographic density | CUST-009, LIQ-001, LIQ-002, LIQ-004, LIQ-009 | Demand, Chefs, Kitchens and fulfilment overlap in a practical cluster and service window. |
| Trust and quality | TRUST-001, TRUST-002, TRUST-003, TRUST-004 | Unfamiliar independent Chefs can earn repeat trust under observable accountability. |
| Fulfilment economics | LIQ-005, LIQ-008, ECO-007, OPS-001, OPS-002 | Pickup/delivery behaviour and density do not erase contribution or reliability. |
| Food marketplace contribution and payback | ECO-002, ECO-003, ECO-011, MON-001 | Repeat, acquisition and transaction contribution close without perpetual subsidy. |
| Food-sale regulatory operability | REG-001, REG-005, REG-007 | The chosen food sale, delivery, payment, tax and remedy model is operable. |
| Optional Dietitian extension | DIET-001 to DIET-007, REG-006 | This is fatal only to the Dietitian extension, not to Phase-1 or core Phase-2 food marketplace decisions. |

These risks are preserved but do not control Phase 1. Dietitian failure is fatal only to the Dietitian extension.

## 9. Phase-1 service-cell liquidity model

A Phase-1 match exists only when every required dimension overlaps for the same request. If any non-negotiable dimension fails, the result is **no usable match**, regardless of Montreal-wide totals.

`usable match = Chef need ∩ Kitchen ∩ capacity ∩ equipment ∩ daypart ∩ geography ∩ duration ∩ compliance`

| Dimension | Required interpretation |
| --- | --- |
| Chef need | Qualified use case, frequency, required certification/compliance and booking intent. |
| Kitchen | Specific licensed/eligible facility and operator willing to transact. |
| Capacity | Confirmed space and occupancy at the requested date/time. |
| Equipment | Required appliances, prep surfaces, storage, ventilation and utilities. |
| Daypart | Arrival, production, cleaning and exit window; not merely calendar availability. |
| Geography | Supply-led location plus observed Chef travel distance and time by daypart. The 30 km founder maximum is an outer test boundary, not the assumed radius; narrow the cell where behaviour requires. |
| Duration / booking unit | Actual operator/Chef overlap across hourly, multi-hour, half/full-day, recurring or other real formats, including setup/cleanup—not a preselected universal unit. |
| Compliance | Operator rules, documents, insurance, training and permitted activity. |

The service cell is supply-led. The founder's 30 km maximum is only an outer boundary for testing; actual travel time and distance determine the practical cell and may require a much narrower scope.

### 9.1 Liquidity metrics

| Metric | Definition |
| --- | --- |
| Qualified requests | Requests passing the pre-registered Chef/use-case eligibility screen. |
| Eligible-result rate | Qualified requests with at least one reliability-adjusted, constraint-complete match / qualified requests. |
| No-result rate | Qualified requests with zero eligible match / qualified requests; primary reason required. |
| Request-to-booking conversion | Completed or contractually committed bookings / qualified requests. |
| Reliability-adjusted capacity | Hours that remain confirmed, accessible and usable at the requested stage; not listed hours. |
| Time to usable result | Elapsed time from complete request to a bookable option, measured by service cell. |
| Completion reliability | Completed usable bookings / confirmed bookings, with cancellation/failure reasons. |

Every no-result receives a primary reason code: geography, time/daypart, price, duration, equipment, compliance, access, operator rejection, Chef withdrawal or other documented reason. Reliability-adjusted supply is recalculated at inquiry, confirmation and arrival.

## 10. Phase-1 cheapest credible test matrix

Maximum useful spend values are per-test stop/escalation caps, **not allocations and not additive**. Most records share the same interviews, inventory, booking and logs.

| Phase-1 P0 ID | Cheapest credible test | Maximum useful spend before stop/escalate |
| --- | --- | --- |
| CHEF-001 | Interview 8–12 Chefs already using rented commercial/shared/ghost Kitchens, then present actual available slots. | CAD $0–50 |
| CHEF-004 | Offer two or three real slot/price/term packages and request a reversible commitment. | CAD $0–100 |
| CHEF-005 | Show a transparent future fee/value proposition after a genuine match; record rejection point. | CAD $0–50 |
| CHEF-010 | Track every completed-booking Chef through the locked repeat window; no paid incentive. | CAD $0 |
| COMP-003 | Map direct search, operator websites, listings, networks and recurring direct agreements through interviews/public information. | CAD $0 |
| ECO-010 | Reconcile one or more real bookings to operator revenue and incremental cost. | CAD $0–100 |
| ECO-013 | Create cash-timing/downside scenarios from actual pilot terms before funds move. | CAD $0–100 |
| KIT-001 | Call mapped operators and obtain slot-level inventory, not expressions of interest. | CAD $0–50 |
| KIT-002 | Overlay operator slots with real Chef-request dayparts in a spreadsheet. | CAD $0 |
| KIT-003 | Ask an authorized operator to approve a named external-Chef booking under disclosed terms. | CAD $0–50 |
| KIT-004 | Pair Chef ceilings with operator floors for identical service cells and terms. | CAD $0 |
| KIT-005 | Use a requirements checklist during calls/site visits; verify one requested equipment bundle. | CAD $0–100 |
| KIT-006 | Review operator requirements and incident responsibilities; escalate only a fatal unresolved question. | CAD $0–250 |
| KIT-009 | Reconfirm and attempt real use of advertised slots; log loss at each stage. | CAD $0–100 |
| KIT-011 | Seek a second availability commitment after an initial booking; no special founder concession. | CAD $0 |
| LIQ-003 | Build a reliability-adjusted capacity sheet for the chosen cluster/dayparts. | CAD $0 |
| LIQ-007 | Manually match structured requests against the same inventory and record time/touches. | CAD $0 |
| LIQ-011 | Run constraint-complete matching across all service-cell dimensions. | CAD $0 |
| LIQ-012 | Apply controlled no-result reason codes to every qualified request. | CAD $0 |
| MON-002 | Test free activation separately from stated post-pilot booking monetization. | CAD $0–50 |
| MON-008 | Observe repeat-channel choice after a booking and test one continuing-value package. | CAD $0–50 |
| OPS-008 | Translation-assisted French audit plus one end-to-end French outreach/booking dry run; count any paid help as cash spend. | CAD $0–100 |
| OPS-009 | Log and shadow-price all founder actions for every request/booking; track against the 24-hour/week capacity separately from cash. | CAD $0 |
| REG-002 | Primary-source requirement matrix for the selected facilities and booking activity. | CAD $0–150 |
| REG-003 | Map provider/operator/platform roles against the exact Phase-1 flow; escalate only contradictions. | CAD $0–200 |
| REG-004 | Collect actual insurance requirements and exclusions from operators/insurer channels. | CAD $0–250 |
| REG-008 | Draft a responsibility matrix and sample booking flow; get targeted review only if unresolved before live payment. | CAD $0–300 |

## 11. Phase-1 budget-control framework

Working release ceiling: **CAD $1,900**, leaving external cash validation spend below CAD $2,000. No gate is automatically funded; unused amounts remain unspent. Paid translation counts as cash spend. Logged, shadow-priced founder labour is reported separately and does not consume the cash cap.

| Release gate | Maximum release | Control rule |
| --- | --- | --- |
| Gate 0 — scope/threshold lock | CAD $0 | No spend until one cluster, participant eligibility, service-cell schema, owners and decisive-variable lock dates are recorded. |
| Gate 1 — regulatory/role/French screen | Up to CAD $300 | Primary sources and free translation tools first; paid translator or professional-review spend only for a documented Phase-1 evidence need. |
| Gate 2 — Chef/operator discovery | Up to CAD $250 | Calls, email, translation, local travel/site visits, forms and simple materials; start with existing Kitchen users and stop if no credible commitments emerge. |
| Gate 3 — real booking attempts | Up to CAD $900 | Refundable deposits, narrowly bounded booking cost/support or incident buffer; no paid acquisition. |
| Gate 4 — repeat/economics/leakage | Up to CAD $250 | Follow-up and a small continuing-value test; no discount used to manufacture repeat. |
| Uncommitted reserve | CAD $200 | Held for a specific evidence gap approved in the decision log; not automatically spent. |

Prohibited by default: paid ads, new paid software, expensive consultants, paid datasets and broad participant incentives. A small real booking/deposit is permitted only as behavioural evidence with documented refund and exposure terms.

## 12. Phase-1 four-month validation roadmap

| Control window | Work | Kill / decision use |
| --- | --- | --- |
| Month 1 | Lock founder controls; map commercial/shared/ghost Kitchen supply and Phase-1 substitutes; begin operator discovery, primary-source role/regulatory screen and translation-assisted French operating-plan check. | Early kill: no plausible contracting/insurance/French path or no authorized operator willing to progress toward actual capacity. |
| Month 2 | Discover Chefs already renting Kitchens; collect real inventory and operator-defined booking formats; verify equipment, terms, price overlap and observed distance/time to select a supply-led service cell. | Early kill: no recurring Chef need/commitment, no genuine inventory or no workable price/format/travel overlap. |
| Month 3 | Manual/concierge matching, reason-coded no-results, real commitments, small safe/permitted bookings and founder workload logging against the 24-hour/week capacity. | Early kill: constraint-complete matching fails or every booking depends on exceptional access, labour or subsidy. |
| Month 4 | Chef repeat, operator inventory persistence, disintermediation, continuing-value/future-fee test, fully loaded economics, cash exposure and commercial decision. | Decision: Phase-1 commercial GO, MODIFY or NO-GO; no automatic move to Phase 2 and no decision about continued software development. |

The sequence may stop early. Calendar completion is not success; evidence against a fatal hypothesis is a valid result.

## 13. Competing Phase-1 operating models

The founder baseline is Chef↔Kitchen first. The operating form remains a competing hypothesis; no winner is selected in MI-01.

| Code | Operating model | Claim | Evidence for | Evidence against |
| --- | --- | --- | --- | --- |
| A | Open marketplace | Many Chefs search many operator-posted slots with light curation. | Works if standard terms, sufficient service-cell density, low no-results and low manual intervention are observed. | Fails if inventory is unreliable, heterogeneous or too thin. |
| B | Curated marketplace | Cheffy qualifies participants and manually/semiautomatically approves compatible matches. | Distinguished by higher completion/repeat that covers the added qualification and matching cost. | Fails if founder/manual work remains uneconomic or cannot be standardized. |
| C | One/few-Kitchen managed supply | Anchor Phase 1 around one or a few contracted facilities and a controlled Chef roster. | Favoured if concentrated supply creates repeatable bookings and clear operations while open supply does not. | Fails if anchor dependence, terms or capacity concentration are unacceptable. |
| D | Recurring block niche | Target Chefs needing predictable weekly/monthly production blocks. | Favoured by high repeat, lower matching cost and operator schedule fit. | Fails if most demand is episodic or blocks create unattractive commitment/risk. |
| E | Cuisine/equipment-specific niche | Begin with one compatible production/equipment need and a small facility set. | Favoured if narrower requirements sharply improve match and repeat. | Fails if the niche is too small or specialized equipment economics do not close. |

## 14. Phase-2 / later launch-wedge register

### 14.1 Separate Phase-2 provider paths

| Provider/control | Founder strategy | Later validation requirement |
| --- | --- | --- |
| Independent Chef provider | Qualified independent Chefs selling prepared food after applicable platform, food-safety, licensing/authorization, insurance and other validated requirements. | Customer job, Chef acquisition/activation, menu and provider economics, trust, fulfilment, repeat, fee tolerance and retention. |
| Restaurant / Organization provider | Existing Restaurants participate through the existing Organization business concept; no restaurant-specific architecture is introduced in MI-01. | West Island targeting assumption, onboarding, real menu/availability, integration friction, acquisition cost, platform economics, pickup/delivery, reliability and retention. |
| Separation rule | Do not aggregate the provider paths before analysis. | Measure provider acquisition, capacity, trust, commission tolerance, operations, delivery, repeat and contribution separately. Success of one path does not validate the other. |
| Incrementality rule | Restaurant marketplace orders must be separated from orders that would have occurred through direct website, phone, pickup, delivery, Google or social channels. | Measure genuinely incremental orders and value; migration of existing direct orders into a paid marketplace may create no Restaurant benefit. |

Restaurants participate through the existing **Organization** concept. Delivery/pickup is Phase 2 and Restaurant participation does not block Phase-1 GO. Restaurant economics, acquisition, reliability and retention remain separate from independent-Chef economics; incremental orders must be distinguished from cannibalized direct orders.

### 14.2 Competing food-market launch wedges

| Code | Candidate wedge | What later evidence must test |
| --- | --- | --- |
| P2-A | Independent Chef food marketplace | Qualified independent-Chef prepared-food supply; validate it separately from Restaurant/Organization supply. |
| P2-B | Cuisine/community-specific | Dense cultural/community occasion where substitutes and trust can be tested locally. |
| P2-C | Recurring meal-prep | Scheduled repeat meals intended to improve demand predictability and production planning. |
| P2-D | Dietary specialization | Specific dietary need with credible provider qualification and trust. |
| P2-E | Vertically managed food supply | Cheffy-operated/contracted initial food supply if open food-marketplace liquidity fails but demand persists. |
| P2-F | Restaurant / Organization food supply | Founder hypothesis: begin by challenging West Island Restaurant targeting; test onboarding, real menus/availability, pickup/delivery, incremental orders versus direct-order cannibalization, fee tolerance, reliability, repeat and separate economics. |
| LATER-A | Multi-Chef food basket | Only after co-located supply and single-Chef fulfilment are proven. |
| LATER-B | Dietitian services | Distinct professional-services extension; validate demand, trust, privacy and professional practice separately. |

Phase-2 wedges are candidates only after a Phase-1 GO. Multi-Chef ordering and Dietitian services remain later unless a genuinely cross-phase legal/privacy dependency is discovered.

## 15. Phase-1 GO / MODIFY / NO-GO criteria

> **Commercial/technical boundary:** These outcomes govern commercial validation and commercial claims. They do not determine whether the founder continues learning, architecture work or software development. A NO-GO cannot be ignored when describing the tested model's commercial validity, and no falsification standard is weakened by continued building.

### GO

- All Phase-1 fatal/P0 gates meet their pre-registered criteria with attributable Montreal evidence or are bounded by accepted, funded mitigations.
- At least one practical service cell produces real, safely completed bookings and evidence of repeat Chef demand/operator persistence.
- Reliability-adjusted supply, no-result reasons, price overlap, fully loaded work and cash exposure support a credible path to sustainable booking economics.
- A continuing-value/monetization path survives direct alternatives and leakage testing.
- GO authorizes commercial movement toward the tested Chef↔Kitchen MVP or an explicitly accepted bounded variation only; it does not validate food ordering or decide whether software creation continues.

### MODIFY

- A real booking job exists, but an open marketplace fails and a narrower cluster, recurring block, curated or managed-supply model plausibly resolves the identified constraint.
- The modified commercial model has explicit new hypotheses, thresholds, budget and stop rules; MODIFY is not permission to proceed unchanged and does not prohibit continued technical learning/building.

### NO-GO

- No qualified Chef segment commits and repeats at realistic terms, or no commercially usable operator supply persists.
- Service-cell matching/no-result performance remains structurally inadequate after credible narrowing.
- The bilateral price/economic gap, disintermediation, manual burden, cash exposure or regulatory/insurance friction cannot be resolved within a plausible business model.
- Multiple fatal hypotheses fail without one coherent, affordable redesign.
- NO-GO means the tested Chef↔Kitchen commercial model must not be represented as validated; it does not require deletion of architecture or cessation of software development.

Numeric success thresholds are intentionally not invented here. Section 17 controls when they must be locked.

## 16. Long-term GO / MODIFY / NO-GO criteria

| Decision | Preliminary long-term logic |
| --- | --- |
| GO | After a Phase-1 commercial GO, Phase 2 independently validates paid repeat customer demand and each provider path—Independent Chef and Restaurant/Organization—with separate acquisition, operations, fulfilment, contribution and retention evidence. Later extensions remain gated. |
| MODIFY | A customer food job or one provider path exists but requires a narrower segment, geography, pickup/delivery model, managed supply, pricing or deferred features; re-register and retest without aggregating unlike providers. |
| NO-GO | No Montreal segment/provider path shows adequate paid repeat at viable all-in economics, or trust, density, fulfilment, contribution or authoritative regulatory requirements remain structurally incompatible. This is a commercial conclusion, not a software-development prohibition. |

## 17. Threshold-lock register

For every decisive variable, the numerical threshold remains **TBD until the stated lock point**. Locking occurs before results are visible and records the rationale and any scenario range.

| Decisive variable | Metric and denominator | Observation window | Threshold-lock point | Source | Owner |
| --- | --- | --- | --- | --- | --- |
| Qualified Chef commitment | Existing commercial/shared/ghost-Kitchen users making the registered commitment / eligible existing users shown a real offer | Month 2 offer cycle | Before first real offer | Outreach/offer log | Founder / MI lead |
| Committed Kitchen supply | Reliability-screened bookable hours / operator hours proposed | Rolling 4 weeks | Before inventory collection | Operator inventory log | Founder / supply lead |
| Eligible-result rate | Qualified requests with ≥1 complete eligible result / qualified requests | Month 3 matching cycle | Before first request is matched | Service-cell match log | MI lead |
| No-result rate | Qualified requests with zero eligible result / qualified requests | Month 3 matching cycle | Before first request is matched | Reason-coded search log | MI lead |
| Request-to-booking conversion | Completed or binding bookings / qualified requests | Month 3–4 | Before first live offer | Booking ledger | Founder |
| Booking completion reliability | Usable completed bookings / confirmed bookings | All Phase-1 bookings | Before first confirmation | Booking/incident ledger | Operations owner |
| Chef repeat | Chefs with another booking / Chefs completing a first booking and observable for full window | Pre-set post-booking window | Before first completion | Cohort ledger | MI lead |
| Operator persistence | Operators retaining real inventory / activated operators observable for full window | Month 3–4 | Before activation | Inventory history | Supply lead |
| Bilateral price overlap | Eligible service cells where Chef ceiling ≥ operator floor plus required costs / tested cells | Month 2–3 | Before prices are solicited | Paired-offer model | Finance owner |
| On-platform repeat / leakage | Repeat pair bookings using registered channel / all repeat pair bookings observed | Month 4 | Before repeat terms are shown | Rebooking log | Founder / finance |
| Fully loaded contribution | Booking revenue less payment, support, remediation, acquisition allocation and shadow-priced labour | Per booking and cohort | Before first live booking | Unit-economics model | Finance owner |
| Founder manual burden | Logged founder minutes/touches and shadow cost / qualified request and completed booking; capacity tracked against 24 hours/week, cash accounted separately | All Month 3–4 transactions | Before first request | Time/action log | Founder |
| Supply concentration | Largest operator/facility share of reliability-adjusted hours and completed bookings | Rolling pilot and loss scenario | Before first operator activation | Inventory/booking ledger | MI lead |
| Failure/remedy burden | Cancellations, refunds, disputes, damage or complaints / confirmed bookings; cash exposure per event | All Phase-1 bookings | Before first confirmation | Incident/cash ledger | Operations / finance |

## 18. MI-01B reconciliation / change log

| MI-01B proposal / issue | Decision | v1.1 treatment |
| --- | --- | --- |
| Service-cell conjunction | ACCEPTED | Added LIQ-011; matching requires simultaneous need × Kitchen × capacity × equipment × daypart × geography × duration × compliance. |
| No-result rate and reason codes | ACCEPTED | Added LIQ-012 and Phase-1 metric/threshold controls. |
| Reliability-adjusted supply | ACCEPTED | Added KIT-009; listed hours are explicitly not counted as usable supply. |
| Operator/facility concentration risk | ACCEPTED | Added KIT-010; treated as Phase-1 P1 because a managed-anchor model may survive concentration. |
| Operator retention after activation | ACCEPTED WITH MODIFICATION | Added KIT-011 to make persistent participation testable. |
| Chef booking repeat | ACCEPTED WITH MODIFICATION | Added CHEF-010 because CHEF-009 concerns food-order flow, not Kitchen rebooking. |
| Disintermediation / continuing value | ACCEPTED | Added MON-008 and linked it to MON-002 and COMP-003. |
| Working capital / reserves | ACCEPTED | Added ECO-013 for settlement, deposits, refunds, disputes, damage and cash timing. |
| Contract/payment/cancellation/refund/damage/complaint/insurance responsibility | ACCEPTED | Added Phase-1-specific REG-008; later food-sale responsibilities remain separate. |
| French-language operability | ACCEPTED | Added OPS-008 as a real Phase-1 P0 operating concern; exact legal requirements remain unverified. |
| Pilot exceptionalism | ACCEPTED | Added OPS-009; all founder work and relationship-specific rescue must be logged and shadow-priced. |
| Split KIT-006 into narrower risks | ACCEPTED WITH MODIFICATION | Preserved authoritative v1.0 text/ID; REG-008 and the Phase-1 dashboard provide narrower operational control. |
| Split Phase-1 booking regulation from Phase-2 food-sale regulation | ACCEPTED WITH MODIFICATION | REG-002/003/004 retained; REG-008 added; REG-001/005 remain Phase 2 and REG-006 remains later. |
| Review regulatory statements as facts | REJECTED | MI-01B items are alerts/reference leads only until verified from authoritative primary sources and, where needed, qualified advice. |
| Merge distinct demand/economic records | REJECTED | Distinct falsifiable records and source IDs are preserved; fatal/dashboard roll-ups provide decision compression. |
| Set a target count of P0 records | REJECTED | Priority follows phase-relative existential impact, not a quota. |
| Downgrade MON-003/004/005 from P0 | INCORRECT SOURCE-ID MAPPING | All three are already P2 in v1.0; they are now LATER without a fictitious priority change. |
| Downgrade DIET-002/003 from P0 | INCORRECT SOURCE-ID MAPPING | Both are P1 in v1.0. Dietitian records are LATER and do not block Phase 1. |
| Treat LIQ-006 as multi-Chef basket | INCORRECT SOURCE-ID MAPPING | LIQ-006 is Chef-to-customer matching (v1.0 P1); LIQ-010 is the multi-Chef-basket record. |
| Treat CUST-004 as geographic density | INCORRECT SOURCE-ID MAPPING | CUST-004 is retention/repeat purchase; CUST-009 is geographic concentration. |
| Treat COMP-002 as v1.0 P0 | INCORRECT SOURCE-ID MAPPING | COMP-002 is v1.0 P1 and concerns Chef-side customer-acquisition alternatives. |
| Downgrade CHEF-004/005 and KIT-004 for Phase 1 | REJECTED | They test willingness to buy capacity, viable monetization and bilateral price overlap—existential to the Phase-1 booking model. |
| Downgrade CHEF-003 and OPS-006 for Phase 1 | ACCEPTED WITH MODIFICATION | Both become CROSS_PHASE/P1; narrower Phase-1 P0 controls are provided by REG-008 and OPS-009. |
| Treat CHEF-007, LIQ-004 and customer P0s as Phase-1 blockers | NOT APPLICABLE TO PHASE 1 | They retain P0 within PHASE_2_CUSTOMER_FOOD and do not control the Phase-1 GO. |
| Upgrade LIQ-007 | ACCEPTED | Kitchen-to-Chef matching is Phase-1 P0. |
| Retain multi-Chef food ordering in Phase 1 | NOT APPLICABLE TO PHASE 1 | CUST-007, LIQ-010, TRUST-005 and OPS-003 are LATER. |
| Make Dietitian functionality a Phase-1 gate | NOT APPLICABLE TO PHASE 1 | Dietitian hypotheses and REG-006 are LATER; failure affects only that extension. |
| Phase-1 alternatives include direct/operator channels | ALREADY PRESENT | COMP-003 already covers shared Kitchens, brokers, directories and direct relationships; v1.1 sharpens the operating-model and leakage tests. |

### 18.1 MI-01D founder-control completion

| Control | Status | Version 1.1 treatment |
| --- | --- | --- |
| Phase-1 Montreal geography | LOCKED | Supply-led; no neighbourhood preselected. Candidate service cells follow verified Kitchen supply and Chef evidence. |
| Chef travel | MATERIALLY NARROWED | 30 km is a founder maximum only. Actual travel distance, time, frequency and burden determine the practical cell. |
| Initial Phase-1 Chef segment | LOCKED | Chefs already using commercial/shared/ghost Kitchens; prior rental behaviour is not evidence they need Cheffy Bites. |
| Initial Kitchen supply | LOCKED | Existing commercial/shared/ghost Kitchen operators; no new capacity build for initial validation. |
| Booking format | LOCKED | Evidence-driven overlap across real operator formats; no universal duration/pricing model imposed. |
| Founder operating capacity | LOCKED | Up to 24 hours/week; all labour logged and shadow-priced separately from cash spend. |
| French execution | MATERIALLY NARROWED | Founder lacks sufficient personal capability; translation/translator assistance is tested and paid costs count against cash. |
| Commercial versus technical decision | LOCKED | MI-01 outcomes govern commercial claims/progression, not whether software learning/building continues; falsification remains unchanged. |
| Phase-2 provider strategy | LOCKED | Independent Chef and Restaurant/Organization food supply are separate provider hypotheses and economics. |
| Phase-2 Restaurant target | REGISTERED AS HYPOTHESIS | West Island Restaurants are a founder targeting preference, not evidence or a confirmed service cell. |
| Restaurant architecture | LOCKED | Restaurants use the existing Organization concept; MI-01 makes no technical redesign. |
| Advanced features | LOCKED | Dietitian and multi-Chef Customer ordering remain later; they do not block Phase 1. |
| Hypothesis register | UNCHANGED | 99 records retained; no ID, substantive hypothesis text, priority, phase, confidence or status changed. |

Version-control rule: future revisions retain invalidated and negative records. Do not delete a hypothesis because it challenges the business; append evidence, status and decision history.

## 19. Remaining founder questions

The following founder boundaries are resolved or materially narrowed. They are not market evidence:

| Boundary | Status | Locked position |
| --- | --- | --- |
| Initial geography | RESOLVED | Supply-led; no Montreal neighbourhood is predetermined. |
| Travel boundary | MATERIALLY NARROWED | 30 km maximum; actual time/distance/frequency/burden remain evidence variables. |
| Initial Chef segment | RESOLVED | Existing users of commercial/shared/ghost Kitchens. |
| Future Chef expansion | RESOLVED | Phase 2/later, subject to all applicable validated requirements; no generic certificate shortcut. |
| Initial Kitchen supply | RESOLVED | Existing commercial/shared/ghost Kitchen operators. |
| Booking format | RESOLVED | Follow actual Chef/operator overlap; no preselected universal unit. |
| Founder time | RESOLVED | Up to 24 hours/week. |
| Labour accounting | RESOLVED | Logged and shadow-priced outside the CAD $2,000 cash cap. |
| French capability | MATERIALLY NARROWED | Translation assistance planned and tested; paid assistance is cash spend. |
| Commercial/software boundary | RESOLVED | Commercial GO/MODIFY/NO-GO does not decide continuation of software development. |
| Phase-2 provider paths | RESOLVED | Independent Chef and Restaurant/Organization supply are distinct. |
| Restaurant target | MATERIALLY NARROWED | West Island is a founder hypothesis only. |
| Restaurant fulfilment | RESOLVED | Delivery/pickup belongs to Phase 2, not Phase 1. |
| Later scope | RESOLVED | Dietitian and multi-Chef Customer functionality remain later. |

### 19.1 Questions assigned to later validation gates

1. [Phase-1 finance/role gate] Will Cheffy collect or hold funds in the earliest live validation, or will payment flow directly between Chef and operator?
2. [Phase-1 regulatory/contracting gate] What exact legal contracting role can Cheffy take after authoritative and qualified review?
3. [Phase-1 insurance gate] How will insurance requirements, exclusions and liability be allocated among Chef, operator and platform?
4. [Phase-1 remedy gate] Who bears cancellation, refund, damage, complaint and incident responsibility under the selected booking flow?
5. [Phase-1 cash gate] What deposit size and maximum per-transaction cash exposure are acceptable after the downside model is built?
6. [Phase-1 economics gate] What shadow labour rate or scenario range will be pre-registered for founder work?
7. [Phase-1 monetization gate] What continuing service and future fee/fee split should be tested after free or minimal activation?
8. [Phase-1 operating-model gate] What locked evidence would trigger a shift from open marketplace to curated or managed supply?
9. [Phase-1 escalation gate] Which insurance, accounting, legal and qualified French-language resources are available for narrowly scoped escalation?
10. [Phase-2 Restaurant gate] What final Restaurant commission model and delivery/pickup provider model should be tested? These are not Phase-1 blockers.

## 20. Next-step research sequence

### 0. Freeze Phase-1 control

Founder boundary control is complete. Pre-register the remaining metric thresholds, denominators, windows, sources, owners, cash releases and shadow-labour rate before evidence collection.

### 1. Phase-1 regulatory/role/French screen

Separate Kitchen booking from later food sale; resolve facility, responsibility, insurance, payment and translation-assisted French fatal alerts using primary sources.

### 2. Supply-led operator and alternative map

Map commercial/shared/ghost Kitchen locations plus direct sites, Google, directories, listings, brokers, referrals and recurring agreements; obtain authorized slot-level, format/equipment-specific reliability-screened inventory.

### 3. Existing-Kitchen-user Chef discovery

Test discovery, booking, scheduling, backup, equipment/storage, price, travel and relationship pain with Chefs already renting capacity; seek real commitment without inferring Cheffy demand from prior rental behaviour.

### 4. Manual service-cell matching

Run structured requests against real inventory; record eligible results, no-results/reasons, time, founder touches and concentration.

### 5. Real booking and completion

Where the role/regulatory screen permits, complete small bookings and observe access, cancellation, incident and remedy handling.

### 6. Repeat, retention and leakage

Observe Chef repeat, operator inventory persistence, rebooking channel and continuing-value/future-fee acceptance without manufacturing behaviour through discounts.

### 7. Fully loaded economics and commercial decision

Keep external cash spend separate from shadow-priced founder labour; include acquisition, payment, translation, support, remediation and working capital; issue Phase-1 commercial GO/MODIFY/NO-GO only.

### 8. Phase 2 only after Phase-1 commercial GO

In a later controlled task, validate Independent Chef and Restaurant/Organization provider paths separately; do not infer either from Phase-1 results or from the other provider path.

> **Stop boundary:** this sequence defines the future Phase-1 commercial research program only. MI-02 has not begun. Founder technical/software development may continue outside this decision boundary, but no unvalidated commercial claim may be presented as validated.

---

**Canonical control note:** For every future evidence update, record source, date, sample/transaction definition, service cell, method, limitations, result against the locked criterion, classification/confidence/status change, owner and resulting decision. Preserve the prior state in the change history.
