# Cheffy Bites — API Contracts

> **Canonical ownership:** This document is the canonical human-readable API contract specification for Cheffy Bites.
> Accepted ADRs under `docs/adr/` govern architectural decisions; Proposed ADRs remain Proposed until explicitly accepted.
> [`02-detailed-architecture.md`](02-detailed-architecture.md) may summarize API behavior for integrated understanding, but it must not override the endpoints, request/response structures, validation rules, or API representation defined here.
> The machine-readable OpenAPI artifact must be generated and maintained consistently with this canonical contract.

---

# 22. API Contract Architecture

Base URL:

```text
/api/v1
```

Authentication:

```text
Authorization: Bearer <OIDC access token>
```

Mutation idempotency for payment-, reservation-, or externally-visible
side-effect APIs:

```text
Idempotency-Key: <unique-key>
```

For Kitchen request submission and transitions, the server scopes the key by
data scope, authenticated actor, and operation, compares a request hash, and
replays the completed safe response for an identical retry. Reusing a key with
a different request returns `409 IDEMPOTENCY_KEY_REUSED`. Raw keys and private
request content are not logged or copied into response receipts.

Correlation:

```text
X-Correlation-ID: <request-id>
```

Pilot localization:

```text
Accept-Language: en-CA | fr-CA
```

The request header overrides the stored preferred locale for that response;
otherwise the participant profile locale applies. Error/status/reason codes
are stable and language-neutral, while messages and safe notification
templates are localized. Changing locale never changes identifiers, amounts,
currencies, or instants, and participant-entered text is never machine-
translated or fabricated.

Timezone modeling follows accepted ADR-011. API fields representing real instants require RFC 3339 / ISO 8601 values containing `Z` or an explicit UTC offset, such as `2026-08-27T15:00:00Z` or `2026-08-27T11:00:00-04:00`. An offset-free value such as `2026-08-27T11:00:00` must not be silently interpreted as UTC for a real-instant field.

The owning Kitchen's IANA timezone controls server-side interpretation of Kitchen operating hours, recurring availability, and other Kitchen-based business-local rules. Browser, device, and customer timezones may control display but do not override Kitchen business-rule evaluation. When local input must be resolved to an instant, a nonexistent local time in a daylight-saving gap must not be silently shifted, and an ambiguous local time in a daylight-saving overlap must not be silently assigned an earlier or later offset. Sufficient information must identify the intended instant, or the request/materialization must be rejected.

Changing a Kitchen's configured timezone affects future business-local schedule interpretation only. It does not rewrite existing booking, order, financial, or materialized availability instants.

---

# 23. Standard API Response Rules

Success responses:

- `200 OK` — read/update action.
- `201 Created` — resource creation.
- `202 Accepted` — accepted asynchronous operation.
- `204 No Content` — successful deletion/action with no body.

Client errors:

- `400 Bad Request` — invalid request.
- `401 Unauthorized` — unauthenticated.
- `403 Forbidden` — authenticated but not authorized.
- `404 Not Found` — resource unavailable.
- `409 Conflict` — state/concurrency/business invariant conflict.
- `422 Unprocessable Entity` — semantic validation error.
- `429 Too Many Requests` — rate limit.

Server errors:

- `500 Internal Server Error`
- `502/503/504` where appropriate for provider/upstream dependency issues.

---

# 24. Standard Error Contract

```json
{
  "code": "KITCHEN_MISMATCH",
  "message": "All items in a cart must belong to the selected kitchen.",
  "traceId": "01H...",
  "details": {
    "cartId": "uuid",
    "existingKitchenId": "uuid",
    "requestedKitchenId": "uuid"
  }
}
```

Do not expose stack traces, SQL, provider secrets, internal hostnames, or infrastructure implementation details.

---

# 25. Core API Contracts — Identity and Organization

## 25.1 Current User

```http
GET /api/v1/me
```

Response:

```json
{
  "id": "uuid",
  "recordMode": "REAL",
  "displayName": "Jane Doe",
  "contactName": "Jane Doe",
  "contactEmail": "jane@example.ca",
  "contactEmailVerified": true,
  "preferredLocale": "en-CA",
  "chefProfileId": "uuid",
  "roles": ["OPERATOR_OWNER"],
  "organizations": [
    {
      "id": "uuid",
      "name": "ABC Kitchens",
      "type": "ENTREPRENEUR_BUSINESS",
      "permissions": ["organization:read", "kitchen:write", "kitchen:publish"]
    }
  ]
}
```

The token `sub` authenticates the User; Auth0 is not the participant-profile
database. The server derives `recordMode`, roles, memberships, permissions, and
resource access. Clients cannot select or change `recordMode`.

The bounded pilot role codes are `OPERATOR_OWNER`, `OPERATOR_MANAGER`, `CHEF`,
and `ADMIN`. Operator roles are Organization-membership scoped, Chef/Admin are
platform grants, and manager Kitchen assignment is explicit. They map to
granular permissions; a role alone never bypasses membership, resource
ownership, immutable data scope, or resource-state checks. One User may hold
more than one role.

## 25.2 Update Participant Profile

```http
PATCH /api/v1/me/profile
```

```json
{
  "displayName": "Jane Doe",
  "contactName": "Jane Doe",
  "preferredLocale": "fr-CA",
  "phoneE164": "+14165550123",
  "roleTitle": "Owner",
  "generalBusinessInfo": "Small commercial kitchen operator"
}
```

`preferredLocale` is `en-CA` or `fr-CA`. Email/phone verification claims are
not invented from editable profile input.

## 25.3 Create Organization

```http
POST /api/v1/organizations
GET  /api/v1/organizations/{organizationId}
PATCH /api/v1/organizations/{organizationId}
```

Request:

```json
{
  "type": "ENTREPRENEUR_BUSINESS",
  "name": "ABC Kitchens",
  "generalBusinessInformation": "Shared commercial kitchen operator"
}
```

Active Organization membership and explicit permission are required for reads
and writes. The general business field is private onboarding/profile content,
not public Kitchen listing copy.

## 25.4 Chef Profile and Business Categories

```http
GET   /api/v1/chef-business-categories
GET   /api/v1/me/chef-profile
PATCH /api/v1/me/chef-profile
```

```json
{
  "businessDisplayName": "Jane's Catering",
  "description": "Small-event caterer",
  "intendedActivity": "Batch preparation and catering",
  "generalOperatingArea": "East Toronto",
  "businessCategoryIds": ["uuid"],
  "otherBusinessCategory": null
}
```

Categories are controlled, active reference values. `otherBusinessCategory`
is accepted only when the controlled `OTHER` category is selected.
`businessDisplayName` is an optional trading/presentation label, not a second
authoritative Organization or ChefBusiness legal name.

---

# 26. Kitchen APIs

## Location Management

```http
POST  /api/v1/organizations/{organizationId}/locations
GET   /api/v1/organizations/{organizationId}/locations?cursor=...
GET   /api/v1/organizations/{organizationId}/locations/{locationId}
PATCH /api/v1/organizations/{organizationId}/locations/{locationId}
```

```json
{
  "name": "East Toronto Facility",
  "addressLine1": "123 Example Street",
  "city": "Toronto",
  "province": "ON",
  "postalCode": "M4M 1A1",
  "countryCode": "CA",
  "privatePoint": { "latitude": 43.000000, "longitude": -79.000000 },
  "publicAreaName": "East Toronto",
  "publicAreaPoint": { "latitude": 43.00, "longitude": -79.00 },
  "addressDisclosurePolicy": "CONFIRMED_PARTIES_ONLY",
  "accessInstructions": "Private instructions shown only after authorization."
}
```

Operator responses may contain the Organization's exact address and point only
for authorized management use. Discovery DTOs never reuse this private DTO.
The request separately captures exact address/point, `publicAreaName`, optional
coarse public point, `addressDisclosurePolicy`, and private access
instructions.
Phase 1 permits only `CONFIRMED_PARTIES_ONLY`; any additional disclosure policy
requires an explicit contract change. The server validates that
`publicAreaPoint` is intentionally coarse and does not echo the exact point.

## List/Search Kitchens

```http
GET /api/v1/kitchens?area=toronto-east&availableFrom=...&availableTo=...&equipment=...
```

This controlled-pilot discovery endpoint requires an authenticated,
stage-eligible Chef. It returns Space-centric results: each result identifies
one eligible KitchenSpace, its parent Kitchen safe summary, applicable
RentalOffers, and its honest compatibility classification.

Filters:

- Coarse public area or location-based search performed server-side.
- Date/time.
- Equipment.
- RentalOffer basis and, only when that basis is supplied, a comparable maximum
  price over offers with the same basis.
- Capacity.
- Safe published Space-equipment modes.

Discovery-safe results contain `publicAreaName` and, where approved, a rounded distance
band. They never return exact address, coordinates, postal code, access
instructions, operator contact information, DEMO rows in a REAL context, or a
Kitchen that fails current stage/publication/pilot-authorization gates.
No default service radius or validated-coverage claim is returned. A bounded
server search window used for performance is not presented as market coverage.

## Kitchen Management

```http
POST /api/v1/kitchens
GET  /api/v1/organizations/{organizationId}/kitchens?status=...&cursor=...
GET  /api/v1/kitchens/{kitchenId}
PATCH /api/v1/kitchens/{kitchenId}
GET  /api/v1/kitchens/{kitchenId}/preview
```

```json
{
  "locationId": "uuid",
  "name": "Hello Kitchen",
  "description": "Commercial kitchen facility",
  "facilityType": "SHARED_COMMERCIAL_KITCHEN",
  "intendedUseStatement": "Operator-entered permitted-use summary",
  "publicAccessibilitySummary": "Step-free main entrance",
  "loadingParkingSummary": "Loading bay available by prior arrangement",
  "storageSummary": "Dry and cold storage discussed per request",
  "facilityConstraints": "No nut processing in the facility",
  "visibilityLevel": "PILOT_AUTHENTICATED",
  "timezoneId": "America/Toronto"
}
```

`timezoneId` is the authoritative Kitchen IANA timezone. Abbreviations or fixed offsets such as `EST`, `EDT`, and `UTC-5` are not valid substitutes for the business timezone identity.

Loading/parking, storage, and facility-constraint fields are bounded
operator-authored listing summaries. The operating-hours summary is derived
from typed operating-hour rules. Private access/orientation instructions stay
on Location and are not accepted in these discovery fields.

Management GET/PATCH requires resource authority. Discovery GET uses a
separate safe projection and returns 404 when stage/scope/requestability gates
fail. Preview renders the safe Chef view without making the Kitchen
requestable.

## Operating Hours and Operator Requirements

```http
GET   /api/v1/kitchens/{kitchenId}/operating-hour-rules
POST  /api/v1/kitchens/{kitchenId}/operating-hour-rules
PATCH /api/v1/kitchen-operating-hour-rules/{ruleId}
GET   /api/v1/kitchens/{kitchenId}/operator-requirements
POST  /api/v1/kitchens/{kitchenId}/operator-requirements
PATCH /api/v1/kitchen-operator-requirements/{requirementId}
```

Operating-hour rules use ISO weekday, local start/end, effective dates,
`active`, and version. They constrain but never create Space availability.
Operator requirements contain a controlled code, bounded title/prompt,
`active`, and version; they request declarations and never request document
uploads.

## KitchenSpace Management

```http
POST /api/v1/kitchens/{kitchenId}/spaces
GET  /api/v1/kitchens/{kitchenId}/spaces?status=...&cursor=...
GET  /api/v1/kitchen-spaces/{spaceId}
PATCH /api/v1/kitchen-spaces/{spaceId}
```

```json
{
  "name": "Space 1",
  "description": "Baking and prep space",
  "capacity": 4,
  "size": { "value": 900, "unit": "SQUARE_FEET" },
  "publicAccessSummary": "Ground-floor prep area",
  "storageMode": "SHARED",
  "storageNote": "Dry shelving discussed per request",
  "operatingConstraints": "No nut processing in this Space",
  "exclusivityMode": "EXCLUSIVE_SPACE",
  "maximumBookingMinutes": 480,
  "cleaningMinutes": 60,
  "status": "ACTIVE"
}
```

KitchenSpace has no price or minimum-duration fields. Those belong only to a
RentalOffer.

`storageMode` is `NONE`, `AVAILABLE`, `SHARED`, or `DISCUSS`; Phase 1 permits
only `EXCLUSIVE_SPACE`. An `INACTIVE` Space accepts no new requests and remains
visible in authorized history. A deactivation that would conceal future
confirmed commitments is rejected.
`size` is optional and requires a positive value plus a controlled unit.
`maximumBookingMinutes` is optional and positive; it limits use duration and
is not a price or minimum commitment.

## Kitchen and Space Media

```http
POST /api/v1/media/upload-requests
POST /api/v1/media/{mediaAssetId}/confirm
PUT  /api/v1/kitchens/{kitchenId}/media
PUT  /api/v1/kitchen-spaces/{spaceId}/media
```

The upload-request response is a short-lived presigned instruction, not a
permanent public URL. Confirmation validates owner, MIME, size, checksum, and
safe image status before an asset becomes `READY`. Association requests carry
ordered asset IDs, `DISCOVERY_SAFE|PRIVATE` visibility, and optional participant-
authored `en-CA`/`fr-CA` alt text/captions. Missing translations are not
fabricated; only safe `READY` `DISCOVERY_SAFE` assets enter authenticated pilot
discovery. The label does not make live inventory an unauthenticated LP asset.

## RentalOffer

```http
POST  /api/v1/kitchen-spaces/{spaceId}/rental-offers
PATCH /api/v1/rental-offers/{rentalOfferId}
DELETE /api/v1/rental-offers/{rentalOfferId}
GET   /api/v1/kitchen-spaces/{spaceId}/rental-offers
```

```json
{
  "rateBasis": "HOURLY",
  "title": "Hourly kitchen rental",
  "amountMinor": 2500,
  "currency": "CAD",
  "blockMinutes": null,
  "dayDefinitionCode": null,
  "dailyWindowStartLocalTime": null,
  "dailyWindowEndLocalTime": null,
  "includedQuantity": null,
  "includedUnit": null,
  "minimumDurationMinutes": 180,
  "minimumCommitmentCount": null,
  "minimumCommitmentUnit": null,
  "deposit": {
    "amountMinor": 5000,
    "currency": "CAD",
    "note": "Discussed directly; not collected by Cheffy Bites in Phase 1."
  },
  "additionalChargesNote": "Consumables discussed separately.",
  "termsNote": "Pilot request only.",
  "active": true
}
```

`rateBasis` is `HOURLY`, `FIXED_BLOCK`, `DAILY`, `RECURRING_HOURS`,
`MONTHLY_HOURS`, or `PRIVATE_LONG_TERM_INQUIRY`. Amount and currency are paired
and may be omitted only for `PRIVATE_LONG_TERM_INQUIRY`. The server enforces
basis-specific required fields. Deposit and additional-charge fields are
informational and create no financial resource.

`DAILY` uses `KITCHEN_LOCAL_CALENDAR_DAY` or
`OFFER_DEFINED_LOCAL_WINDOW`; an offer-defined window carries its local
start/end. `RECURRING_HOURS` uses `HOURS_PER_WEEK` and `WEEKS` commitment;
`MONTHLY_HOURS` uses `HOURS_PER_MONTH` and `MONTHS` commitment.

An estimate is `CALCULATED` only when no rounding or interpretation is needed:
HOURLY produces an exact integer value for `amountMinor * billableMinutes / 60`,
FIXED_BLOCK contains whole blocks, and DAILY contains whole configured local
day definitions. Otherwise it is `REQUIRES_CONFIRMATION`. Recurring/monthly
offers show the stated plan without one-off proration; inquiry-only offers have
`NOT_APPLICABLE` amount.

`billableMinutes` is the whole-minute requested use duration from `startAt` to
`endAt`; the cleaning extension to `occupancyEndAt` protects capacity but is
not silently billed. A `KITCHEN_LOCAL_CALENDAR_DAY` is a Kitchen-local civil
date, not a fixed 24-hour chunk across daylight-saving changes.

DELETE deactivates/version-controls the offer for new requests; it does not
hard-delete an offer referenced by historical snapshots.

## Space Availability Rules

```http
POST   /api/v1/kitchen-spaces/{spaceId}/availability-rules
PATCH  /api/v1/space-availability-rules/{ruleId}
DELETE /api/v1/space-availability-rules/{ruleId}
GET    /api/v1/kitchen-spaces/{spaceId}/availability-rules
```

One-time example:

```json
{
  "availabilityKind": "BLOCKED",
  "scheduleKind": "ONE_TIME",
  "localDate": "2026-10-12",
  "localStartTime": "09:00:00",
  "localEndTime": "13:00:00",
  "startOffset": "-04:00",
  "endOffset": "-04:00",
  "active": true
}
```

Weekly example:

```json
{
  "availabilityKind": "AVAILABLE",
  "scheduleKind": "WEEKLY",
  "weekdays": [1, 3, 5],
  "localStartTime": "08:00:00",
  "localEndTime": "16:00:00",
  "effectiveStartDate": "2026-09-01",
  "effectiveEndDate": "2026-12-31",
  "active": true
}
```

ISO weekdays are 1–7. Overnight rules are split at local midnight. Rules are
interpreted in the owning Kitchen timezone. For a one-time rule, offsets are
required only when needed to select an occurrence in a DST overlap; a gap
returns `INVALID_LOCAL_TIME` and an unresolved overlap returns
`AMBIGUOUS_LOCAL_TIME`. Weekly rules remain local-time expressions; bounded
preview returns a dated exception when a future gap or overlap cannot be
resolved and never silently shifts or guesses. Phase 1 does not persist
Space-availability occurrence rows.

DELETE idempotently sets the rule inactive for future evaluation and preserves
the versioned row as audit evidence; it never hard-deletes the rule or edits a
confirmed booking.
Creating, updating, or activating a BLOCKED rule that matches future
`HELD`/`CONFIRMED` occupancy returns `409 BLOCK_CONFLICTS_WITH_BOOKING`; the
operator must explicitly cancel the booking first. Pending requests stay
`REQUESTED` and their detail/dashboard shows the new incompatibility warning.

## Publish Kitchen

```http
POST /api/v1/kitchens/{kitchenId}/publication
```

```json
{
  "action": "PUBLISH",
  "expectedVersion": 3,
  "authorityAffirmed": true,
  "affirmationVersion": "pilot-authority-v1"
}
```

Operator publication does not grant platform pilot authorization. Publishing
emits `KitchenPublished.v1` only after commit.

`action` is `PUBLISH` or `UNPUBLISH`. PUBLISH revalidates completeness and, for
REAL records, requires authority affirmation evidence. UNPUBLISH blocks new
requests while preserving existing requests and confirmed bookings.
Completeness includes timezone, coarse public area, description/presentation
fields, at least one validated READY Kitchen image, one active Space, one
usable active RentalOffer, and future Space availability.

## Confirmed-Party Address

```http
GET /api/v1/kitchen-bookings/{bookingId}/location-details
```

Only an authorized operator or Chef party to a `CONFIRMED` booking may receive
exact address/access fields, subject to `addressDisclosurePolicy`. All other
callers receive 404 or 403 without leaking whether private data exists.

---

# 27. Equipment APIs

## Search Master Equipment

```http
GET /api/v1/equipment/catalog?q=tandoor&category=cooking
```

## Add or Update Space Equipment Offering

```http
GET /api/v1/kitchen-spaces/{spaceId}/equipment
PUT /api/v1/kitchen-spaces/{spaceId}/equipment/{equipmentCatalogItemId}
```

```json
{
  "displayQuantity": 1,
  "availabilityMode": "INCLUDED",
  "conditionNote": "Commercial convection oven",
  "operatorNote": null
}
```

`availabilityMode` is `INCLUDED`, `SHARED`, `EXTRA_DISCUSS`, or `UNAVAILABLE`.
It is descriptive request context in Phase 1.

## Future Paid Rental Equipment

```http
POST /api/v1/kitchen-spaces/{spaceId}/rental-equipment
```

```json
{
  "equipmentCatalogItemId": "uuid",
  "hourlyRateMinor": 1500,
  "currency": "CAD",
  "quantityAvailable": 2
}
```

The created EquipmentRental is an additional rentable inventory offer for this Kitchen Space. Its `quantityAvailable` is the authoritative finite reservable quantity; the catalog item is a reusable equipment type and is not reservation capacity. The current contract models EquipmentRental capacity per Kitchen Space. A shared Kitchen-wide resource across Spaces requires a separate approved business and architecture contract.

This endpoint and EquipmentRental capacity flow are not part of the Phase-1
pilot. `EXTRA_DISCUSS` never invokes them.

---

# 28. Kitchen Booking APIs

## Search Availability and Requestability

```http
GET /api/v1/kitchen-spaces/{spaceId}/availability?from=...&to=...
```

Availability results are informational snapshots only. They do not hold the Space or reserve EquipmentRental capacity; authoritative capacity is established only by a successfully committed transactional `HELD` or `CONFIRMED` booking reservation.

The response explains `requestable`, selected `rentalOfferId`, timezone,
matching available-rule window, any public-safe unavailable reason code, and an
estimate with status `CALCULATED`, `NOT_APPLICABLE`, or
`REQUIRES_CONFIRMATION`. Operating hours constrain but do not create
availability. A blocked rule or `HELD`/`CONFIRMED` overlap vetoes it.

The result classification is `MATCHES_REQUEST`,
`POSSIBLE_OPERATOR_CONFIRMATION_REQUIRED`, or `NO_MATCH`. Equipment/operating
declarations that need operator discussion produce
`POSSIBLE_OPERATOR_CONFIRMATION_REQUIRED`, not a false guarantee. Even
`MATCHES_REQUEST` remains informational and requires operator confirmation.

## Submit Booking Request

```http
POST /api/v1/kitchen-booking-requests
Idempotency-Key: request-unique-key
```

```json
{
  "rentalOfferId": "uuid",
  "startAt": "2026-09-01T10:00:00Z",
  "endAt": "2026-09-01T14:00:00Z",
  "activityCategory": "CATERING_PREP",
  "activityDescription": "Preparation for a 40-person event",
  "requirementDeclarations": [
    {
      "operatorRequirementId": "uuid",
      "declarationStatus": "DECLARED",
      "referenceText": "Food-handler training declared; no document uploaded"
    }
  ],
  "equipmentNeeds": [
    {
      "equipmentCatalogItemId": "uuid",
      "requestedQuantity": 1,
      "details": "Prefer shared mixer if available"
    }
  ],
  "storageNeeds": "One dry shelf if available",
  "setupNeeds": "Thirty minutes before use",
  "cleanupNeeds": "Standard one-hour operator cleaning period acknowledged",
  "messageToOperator": "Happy to discuss storage needs.",
  "acknowledgedDisclaimerVersion": "p1-estimate-v1"
}
```

Response:

```json
{
  "bookingId": "uuid",
  "status": "REQUESTED",
  "version": 1,
  "space": {
    "id": "uuid",
    "name": "Space 1",
    "timezoneId": "America/Toronto"
  },
  "occupancy": {
    "startAt": "2026-09-01T10:00:00Z",
    "endAt": "2026-09-01T14:00:00Z",
    "occupancyEndAt": "2026-09-01T15:00:00Z"
  },
  "offerSnapshot": {
    "rentalOfferId": "uuid",
    "rentalOfferVersion": 4,
    "rateBasis": "HOURLY",
    "amountMinor": 2500,
    "currency": "CAD"
  },
  "estimate": {
    "status": "CALCULATED",
    "amountMinor": 10000,
    "currency": "CAD",
    "disclaimerVersion": "p1-estimate-v1"
  }
}
```

Submission rechecks all current requestability gates, creates one
`KitchenBooking` in `REQUESTED`, immutable snapshots, normalized requirement
declarations, status history, and `KitchenBookingRequested.v1` in one local
transaction. It reserves no capacity and creates no financial record.

At submission, `declarationStatus` is exactly `NOT_PROVIDED` or `DECLARED`.
`REVIEWED_OUTSIDE_PLATFORM` is a derived current status produced only by an
append-only authorized operator/admin review action, not asserted by the Chef
or written over the submitted answer. None of these values means Cheffy
verification, and no credential or insurance document is accepted.
The request supplies exactly one declaration for every active requirement
presented by the selected Kitchen. The server snapshots each requirement's
version/code/title/prompt and each equipment need's displayed catalog name and
applicable Space offering mode, so later catalog/listing edits do not change
request detail.

## Read and List Requests

```http
GET /api/v1/kitchen-booking-requests/{bookingId}
GET /api/v1/me/kitchen-booking-requests?status=REQUESTED&cursor=...
GET /api/v1/operator/kitchen-booking-requests?status=REQUESTED&kitchenId=...&cursor=...
GET /api/v1/me/kitchen-bookings?view=pending|confirmed|declined|cancelled|past&cursor=...
GET /api/v1/operator/kitchen-bookings?view=upcoming|past&kitchenId=...&cursor=...
```

Chef list/detail is limited to the owning Chef. Operator list/detail requires
active Organization membership and Kitchen authority. Results enforce the
caller's immutable data scope. Operator detail may contain the snapshotted
request context but not identity-provider secrets or unrelated private data.

```http
POST /api/v1/kitchen-booking-requests/{bookingId}/requirements/{operatorRequirementId}/outside-review
Idempotency-Key: review-unique-key
If-Match: "<expected-version>"
```

This permissioned action may move that declaration to
derived `REVIEWED_OUTSIDE_PLATFORM` by appending bounded reference text and
audit actor/time while preserving the submitted declaration.
It never stores a document or labels the requirement verified by Cheffy Bites.

## Confirm, Decline, Withdraw, or Cancel

```http
POST /api/v1/kitchen-booking-requests/{bookingId}/confirm
POST /api/v1/kitchen-booking-requests/{bookingId}/decline
POST /api/v1/kitchen-booking-requests/{bookingId}/withdraw
POST /api/v1/kitchen-bookings/{bookingId}/cancel
Idempotency-Key: transition-unique-key
If-Match: "<expected-version>"
```

Confirm has an empty body and no financial payload.

Decline body:

```json
{
  "reasonCode": "REQUIREMENT_MISMATCH",
  "message": "This requirement cannot be supported for the requested time."
}
```

Withdraw body:

```json
{
  "reasonCode": "SCHEDULE_CHANGED",
  "message": "I no longer need this time."
}
```

Cancel body:

```json
{
  "reasonCode": "OPERATOR_UNAVAILABLE",
  "message": "The Space is unexpectedly unavailable.",
  "acknowledgedNoFinancialPolicyVersion": "p1-cancellation-v1"
}
```

Decline `reasonCode` is optional and is one of `NO_CAPACITY`,
`SCHEDULE_MISMATCH`, `EQUIPMENT_MISMATCH`, `REQUIREMENT_MISMATCH`,
`ACTIVITY_NOT_SUPPORTED`, `PRICE_OR_FORMAT_MISMATCH`, or `OTHER`. Withdraw
reason is optional and, when supplied, is `NO_LONGER_NEEDED`,
`SCHEDULE_CHANGED`, or `OTHER`. Cancel reason is required and is
`NO_LONGER_NEEDED`, `SCHEDULE_CHANGED`, `OPERATOR_UNAVAILABLE`, or `OTHER`.
Each message is optional and bounded. Free-text values are stored in history
but never emitted in integration events or ordinary logs.

Operator confirm/decline requires Kitchen authority. Chef withdraw requires
ownership and `REQUESTED`; cancel requires an authorized Chef or operator,
future `CONFIRMED` status, and the pilot no-financial-policy acknowledgement.
A past booking cannot use the ordinary cancellation action.

Confirmation locks/reloads the request and rechecks current data-scope, stage,
publication, pilot authorization, Space, offer, requirements, availability,
and cleaning-aware occupancy. It transitions to `CONFIRMED`, appends history,
and writes `KitchenBookingConfirmed.v1` in the same transaction. ADR-007's GiST
constraint is final authority. A competing acceptance returns:

```http
409 Conflict
```

```json
{
  "code": "BOOKING_CONFLICT",
  "message": "The requested space is no longer available for that occupancy window.",
  "traceId": "01H...",
  "details": { "bookingId": "uuid" }
}
```

The losing request remains `REQUESTED`. A decision/withdrawal race returns
`409 BOOKING_STATE_CONFLICT` to the losing transition. Decline and withdrawal
are non-reserving terminal states. Cancellation releases confirmed capacity
and preserves all history.

The Phase-1 contract has no `POST /kitchen-bookings/quote`, payment-coupled
`POST /kitchen-bookings`, pricing promotion, tax, checkout, or `clientSecret`.
A future paid workflow must use a separately versioned/approved checkout
contract such as `/api/v1/kitchen-booking-checkouts`; that schema is deferred.

# 28A. Pilot Feedback and Notification APIs

```http
POST /api/v1/feedback
GET  /api/v1/me/notifications?cursor=...
POST /api/v1/me/notifications/{notificationId}/read
```

Feedback request:

```json
{
  "roleContext": "CHEF",
  "category": "BOOKING_REQUEST",
  "feedbackText": "The equipment questions were clear.",
  "locale": "en-CA",
  "routeContext": "/app/chef/requests/new",
  "relatedResource": {
    "kind": "KITCHEN_BOOKING",
    "id": "uuid"
  }
}
```

The server verifies the typed related resource is visible to the submitter.
Text and route lengths are bounded. Feedback is private internal triage input,
not a public review, chat message, event payload, or analytics attribute.
`roleContext` is `OPERATOR|CHEF`; category is `AVAILABILITY`, `PRICING`,
`KITCHEN_LISTING`, `BOOKING_REQUEST`, `OPERATOR_WORKFLOW`, `CHEF_WORKFLOW`,
`REQUIREMENTS_COMPLIANCE`, or `OTHER`.

Notification responses contain a safe type, localized title/body, booking ID,
created/read state, and route target. They do not contain exact address,
request free text, access instructions, or private contact details. Opening a
notification reloads the authoritative booking state.

The web-first P0 contract uses in-app and email delivery. SMS is outside the
pilot. Push remains a later native P1 channel using the same booking events;
device registration and push-delivery APIs are deferred to that native slice
and are not invented here.

# 28B. Pilot Administration APIs

```http
GET  /api/v1/admin/pilot/stage
PUT  /api/v1/admin/pilot/stage
GET  /api/v1/admin/pilot/users?recordMode=...&status=...&cursor=...
POST /api/v1/admin/pilot/users/{userId}/status
GET  /api/v1/admin/pilot/kitchens?recordMode=...&cursor=...
GET  /api/v1/admin/pilot/bookings?status=...&cursor=...
GET  /api/v1/admin/pilot/feedback?triageStatus=...&cursor=...
GET  /api/v1/admin/pilot/operations/failures?kind=notification|outbox&cursor=...
POST /api/v1/admin/kitchens/{kitchenId}/pilot-authorization
POST /api/v1/admin/kitchens/{kitchenId}/pilot-authorization/revoke
POST /api/v1/admin/kitchens/{kitchenId}/emergency-unpublish
POST /api/v1/admin/demo-scopes/{dataScopeId}/reset
```

All require dedicated permissions, expected-version/idempotency protection,
actor/reason audit, and an explicit data scope. Demo reset rejects a REAL or
non-resettable scope before any deletion. Platform authorization never changes
operator publication state. Emergency unpublish records the admin actor and
reason and cannot be represented as an operator action.
Reset deletes/rebuilds only rows enumerated by the DEMO fixture manifest; it
preserves the DataScope, reset-run record, admin identity, and audit history.

---

# 29. Food / Chef APIs

## Search Master Food

```http
GET /api/v1/catalog/foods?q=palak%20paneer&cuisine=indian
```

## Create Food Listing

```http
POST /api/v1/food-listings
```

```json
{
  "masterFoodId": "uuid",
  "name": "Chef Raj's Palak Paneer",
  "description": "Fresh spinach and paneer",
  "priceMinor": 1800,
  "currency": "CAD",
  "preparationMinutes": 25,
  "recipe": null,
  "youtubeUrl": "https://youtube.com/watch?v=..."
}
```

## Create Recurring Food Availability Rule

```http
POST /api/v1/food-listings/{foodListingId}/availability-rules
```

```json
{
  "daysOfWeek": ["FRIDAY", "SATURDAY"],
  "localStartTime": "12:00:00",
  "localEndTime": "15:00:00",
  "recurrenceRule": "WEEKLY",
  "cutoffMinutes": 60
}
```

This operation creates a recurring business-local rule, not an already-resolved instant interval. `localStartTime` and `localEndTime` are interpreted using the authoritative IANA timezone of the Food Listing's Kitchen; callers must not convert the recurring rule to fixed UTC timestamps. The Kitchen timezone is obtained from the owning Kitchen rather than supplied or overridden by the browser, device, or customer.

When the server resolves a rule for a specific date, it must not silently shift a nonexistent local time during a DST gap or guess an offset during a DST overlap. Materialization requires sufficient information to identify the intended instant or fails semantic validation.

## Create Materialized Food Availability Occurrence

```http
POST /api/v1/food-listings/{foodListingId}/availability-occurrences
```

```json
{
  "availabilityRuleId": "uuid",
  "startAt": "2026-08-27T11:00:00-04:00",
  "endAt": "2026-08-27T15:00:00-04:00",
  "cutoffMinutes": 60
}
```

This operation creates a concrete occurrence for a specific interval. `startAt` and `endAt` are real instants and must contain `Z` or an explicit UTC offset. Offset-free instant values are rejected rather than silently treated as UTC. `availabilityRuleId` is nullable when the occurrence was supplied directly rather than materialized from a recurring rule.

Previously materialized occurrences retain their original real instants if the Kitchen's configured timezone later changes. The changed timezone applies when interpreting future business-local schedule occurrences.

---

# 30. Customer Discovery APIs

```http
GET /api/v1/discovery/foods
GET /api/v1/discovery/chefs
GET /api/v1/discovery/cuisines
```

Example filters:

```text
cuisine=indian
food=palak-paneer
chefId=uuid
lat=45.5
lng=-73.6
radiusMeters=10000
vegetarian=true
availableAt=...
```

The backend may query PostgreSQL/PostGIS directly for MVP. Do not introduce OpenSearch until justified by search requirements.

---

# 31. Cart APIs

## Create Cart

```http
POST /api/v1/carts
```

```json
{
  "kitchenId": "uuid"
}
```

## Add Item

```http
POST /api/v1/carts/{cartId}/items
```

```json
{
  "foodListingId": "uuid",
  "quantity": 2
}
```

If the item belongs to a different Kitchen:

```text
409 KITCHEN_MISMATCH
```

## Remove Item

```http
DELETE /api/v1/carts/{cartId}/items/{cartItemId}
```

## Get Cart

```http
GET /api/v1/carts/{cartId}
```

---

# 32. Pricing / Promotion APIs

Promotion ownership and food-order calculation scope are separate API concepts.

Allowed `ownerType` values:

```text
CHEF
PLATFORM
ENTREPRENEUR
```

Allowed food-order `scope` values:

```text
ITEM
CHEF_ORDER_GROUP
DELIVERY
ORDER
```

`PLATFORM` is an ownership domain, not a calculation scope. `ENTREPRENEUR` is an ownership domain, not a food-order calculation scope. Entrepreneur booking and equipment promotions remain in the booking/rental promotion domain unless a future approved cross-domain decision changes that boundary.

Chef-owned promotions are evaluated within the relevant ChefOrderGroup. Chef A and Chef B are independent promotion domains within the same Order. Item-level and ChefOrderGroup-level promotions may coexist when their monetary scopes do not overlap and compatibility rules allow it.

Compatibility is determined through `priority`, `qualifyingBasis`, `compatibilityGroup`, `exclusivityGroup`, targets, customer savings, and deterministic tie-breakers. The API does not use a global `stackable`, `isStackable`, or `canStack` flag.

## Checkout Quote

```http
POST /api/v1/carts/{cartId}/checkout/quote
```

Returns a non-authoritative pricing preview.

The pricing response must expose promotion applications, rejected promotions, rejection reasons, qualifying basis, eligible/excluded item IDs, discount amounts, snapshot references, and allocation preview data.

## Validate Promo Code

```http
POST /api/v1/carts/{cartId}/promo-code/validate
```

```json
{
  "promoCode": "WELCOME10"
}
```

## Create Chef Promotion

```http
POST /api/v1/promotions
```

```json
{
  "ownerType": "CHEF",
  "scope": "CHEF_ORDER_GROUP",
  "promotionType": "PERCENTAGE",
  "name": "20% off 2 or more items",
  "priority": 100,
  "validFrom": "2026-09-01T00:00:00Z",
  "validTo": "2026-09-30T23:59:59Z",
  "qualifyingBasis": "NON_DISCOUNTED_ELIGIBLE_ITEMS",
  "conditions": {
    "minimumQuantity": 2
  },
  "targets": [
    {
      "type": "MENU",
      "id": "uuid"
    }
  ],
  "discount": {
    "percent": 20
  },
  "compatibilityGroup": "GROUP_DEFAULT",
  "exclusivityGroup": "GROUP_DEFAULT"
}
```

The API must validate that a Chef-owned promotion does not target another Chef's resources.

---

# 33. Order APIs

## Price Checkout

```http
POST /api/v1/carts/{cartId}/checkout/quote
```

## Create Order

```http
POST /api/v1/orders
Idempotency-Key: order-unique-key
```

Request:

`fulfillmentType` is required and allows exactly:

```text
PICKUP
DELIVERY
```

It is selected before Order creation and is immutable after the Order is created.

```json
{
  "cartId": "uuid",
  "fulfillmentType": "DELIVERY",
  "deliveryAddressId": "uuid",
  "promoCode": "WELCOME10"
}
```

Response:

```json
{
  "orderId": "uuid",
  "status": "PAYMENT_PENDING",
  "kitchenId": "uuid",
  "fulfillmentType": "DELIVERY",
  "chefOrderGroups": [
    {
      "id": "uuid",
      "chefBusinessId": "uuid",
      "status": "PENDING_ACCEPTANCE",
      "subtotalMinor": 5000,
      "discountMinor": 1000,
      "netMinor": 4000,
      "promotionSnapshotId": "uuid",
      "pricingSnapshotId": "uuid",
      "paymentAllocationIds": ["uuid"],
      "refundAllocationIds": [],
      "payoutLineIds": ["uuid"],
      "items": [
        {
          "orderItemId": "uuid",
          "chefOrderGroupId": "uuid",
          "foodListingId": "uuid",
          "quantity": 2,
          "unitPriceMinor": 2500,
          "currency": "CAD"
        }
      ]
    }
  ],
  "pricing": {
    "subtotalMinor": 11200,
    "discountMinor": 1500,
    "deliveryFeeMinor": 700,
    "feeMinor": 300,
    "taxMinor": 500,
    "totalMinor": 11200,
    "currency": "CAD"
  },
  "payment": {
    "paymentId": "uuid",
    "provider": "STRIPE"
  }
}
```

`pricingSnapshotId` identifies the canonical immutable Pricing-owned commercial calculation evidence captured for the Order or applicable ChefOrderGroup scope. It is not a Payment, PaymentAllocation, settlement, ledger, or other Financial-domain aggregate reference.

Every food OrderItem belongs to exactly one ChefOrderGroup and exposes that membership through `chefOrderGroupId`. An Order belongs to exactly one Kitchen; multiple ChefOrderGroups are permitted only when all represented Chefs operate from that same Kitchen.

## Parent Order Fulfillment Contract

The parent Order owns final customer pickup and delivery fulfillment.

For `PICKUP`:

```text
PAID
→ PENDING_CHEF_ACCEPTANCE
→ ACCEPTED
→ PREPARING
→ READY_FOR_FULFILLMENT
→ PICKED_UP
→ COMPLETED
```

For `DELIVERY`:

```text
PAID
→ PENDING_CHEF_ACCEPTANCE
→ ACCEPTED
→ PREPARING
→ READY_FOR_FULFILLMENT
→ DELIVERY_REQUESTED
→ DRIVER_ASSIGNED
→ DRIVER_PICKED_UP
→ OUT_FOR_DELIVERY
→ DELIVERED
→ COMPLETED
```

`PICKED_UP` means completed handoff to the customer or the customer's authorized pickup party only. `DRIVER_PICKED_UP` means delivery-driver possession only. Pickup and delivery transitions must not cross lanes. Existing rejection, cancellation, payment-failure, and refund semantics remain governed by their approved contracts.

## ChefOrderGroup List

```http
GET /api/v1/chef-order-groups?chefBusinessId=uuid&page=0&size=20
```

Returns paginated ChefOrderGroups for the authenticated Chef.

## ChefOrderGroup Detail

```http
GET /api/v1/chef-order-groups/{chefOrderGroupId}
```

Returns the authenticated Chef's ChefOrderGroup details, including its preparation status and OrderItems. Every returned food OrderItem includes the owning `chefOrderGroupId`.

## ChefOrderGroup Financials

```http
GET /api/v1/chef-order-groups/{chefOrderGroupId}/financials
```

Returns immutable promotion and referenced payment, refund, payout, and ledger evidence for ChefOrderGroup traceability. These are financial-domain records that reference the ChefOrderGroup; the ChefOrderGroup does not own the Payment, Refund, Payout, or Ledger aggregates.

ChefOrderGroup is the Chef operational boundary, Chef promotion evaluation boundary, financial allocation/reference boundary, refund traceability boundary, payout traceability boundary, and reporting boundary. It does not own the Delivery aggregate or final fulfillment; those remain parent Order/fulfillment concerns.

## ChefOrderGroup Preparation State Contract

```text
PENDING_ACCEPTANCE → ACCEPTED
PENDING_ACCEPTANCE → REJECTED
ACCEPTED → PREPARING
ACCEPTED → CANCELLED
PREPARING → READY
PREPARING → CANCELLED
```

ChefOrderGroup owns Chef preparation responsibility only. It never enters `PICKED_UP`, `DRIVER_PICKED_UP`, `OUT_FOR_DELIVERY`, `DELIVERED`, or `COMPLETED`. Each ChefOrderGroup reaches `READY` independently; parent Order coordination determines when the Order reaches `READY_FOR_FULFILLMENT`.

## ChefOrderGroup Actions

```http
POST /api/v1/chef-order-groups/{chefOrderGroupId}/accept
POST /api/v1/chef-order-groups/{chefOrderGroupId}/reject
POST /api/v1/chef-order-groups/{chefOrderGroupId}/preparing
POST /api/v1/chef-order-groups/{chefOrderGroupId}/ready
POST /api/v1/chef-order-groups/{chefOrderGroupId}/cancel
```

The canonical command-to-transition mapping is:

| Command | Permitted source state | Resulting state |
|---------|------------------------|-----------------|
| `accept` | `PENDING_ACCEPTANCE` | `ACCEPTED` |
| `reject` | `PENDING_ACCEPTANCE` | `REJECTED` |
| `preparing` | `ACCEPTED` | `PREPARING` |
| `ready` | `PREPARING` | `READY` |
| `cancel` | `ACCEPTED` | `CANCELLED` |
| `cancel` | `PREPARING` | `CANCELLED` |

Successful ChefOrderGroup transitions map to canonical events as follows:

| Command | Canonical event |
|---------|-----------------|
| `accept` | `ChefOrderGroupAccepted.v1` |
| `reject` | `ChefOrderGroupRejected.v1` |
| `preparing` | `ChefOrderGroupPreparing.v1` |
| `ready` | `ChefOrderGroupReady.v1` |
| `cancel` | `ChefOrderGroupCancelled.v1` |

State transitions must be validated server-side and rejected with `409 Conflict` on invalid transitions. In particular, `cancel` is not valid from `PENDING_ACCEPTANCE`, `REJECTED`, `READY`, or `CANCELLED`.

An authenticated Chef actor may invoke a ChefOrderGroup mutation only for a ChefOrderGroup belonging to that actor's Chef / Chef Business and must pass the repository's normal role, permission, and resource-ownership authorization rules. A Chef must never mutate another Chef's ChefOrderGroup. An authorized platform/system workflow may invoke a transition where an existing business process requires it. This contract does not grant a Customer a direct ChefOrderGroup mutation or cancellation right.

## Cancel ChefOrderGroup

```http
POST /api/v1/chef-order-groups/{chefOrderGroupId}/cancel
Idempotency-Key: chef-order-group-cancellation-key
```

Request:

```json
{
  "reason": "Unable to complete preparation"
}
```

`reason` records the operational cancellation reason. The command contains no payment, refund, payout, fee, compensation, penalty, delivery, or parent-Order mutation fields.

Response: `200 OK` with the existing ChefOrderGroup detail representation updated to `CANCELLED`. For example:

```json
{
  "id": "uuid",
  "orderId": "uuid",
  "chefBusinessId": "uuid",
  "status": "CANCELLED",
  "items": []
}
```

This command changes only ChefOrderGroup operational preparation state. Customer cancellation, parent Order cancellation, refund policy, payment adjustment, payout adjustment, delivery effects, and coordination with other ChefOrderGroups remain separate authorized orchestration or financial workflows. Where applicable, financial-domain records may reference `chefOrderGroupId`; the ChefOrderGroup cancellation command does not directly mutate or assume an outcome for Payment, PaymentAllocation, Refund, RefundLine, Payout, PayoutLine, or Ledger aggregates.

## Get Order

```http
GET /api/v1/orders/{orderId}
```

## Cancel Order

```http
POST /api/v1/orders/{orderId}/cancellation
```

```json
{
  "reasonCode": "CUSTOMER_CHANGED_MIND"
}
```

## Chef Accept/Reject

```http
POST /api/v1/chef-order-groups/{chefOrderGroupId}/accept
POST /api/v1/chef-order-groups/{chefOrderGroupId}/reject
```

## Chef Preparation

```http
POST /api/v1/chef-order-groups/{chefOrderGroupId}/preparing
POST /api/v1/chef-order-groups/{chefOrderGroupId}/ready
```

---

# 34. Payment APIs

The financial API remains provider-neutral and supports one logical customer payment with multiple internal allocations. Merchant-of-Record, legal, tax-remittance, chargeback, and refund-liability responsibilities remain unresolved and are not asserted by this contract.

## Create Payment Attempt

```http
POST /api/v1/orders/{orderId}/payments
Idempotency-Key: payment-attempt-key
```

Response must include provider payment identifiers, payment allocation references, and the authoritative payment status.

## Payment Provider Webhook

```http
POST /api/v1/webhooks/stripe
```

This endpoint is unauthenticated at the normal user level but is protected by provider signature verification and replay protection.

Webhook processing must be idempotent and deduplicated by provider event ID.

Never trust the customer browser's redirect/success page as payment proof.

---

# 35. Refund APIs

## Request Refund

```http
POST /api/v1/orders/{orderId}/refunds
Idempotency-Key: refund-key
```

```json
{
  "type": "ITEM_REFUND",
  "items": [
    {
      "orderItemId": "uuid",
      "quantity": 1
    }
  ],
  "reasonCode": "ITEM_UNAVAILABLE"
}
```

Response:

```json
{
  "refundId": "uuid",
  "status": "REFUND_PENDING",
  "requestedAmountMinor": 2000,
  "currency": "CAD",
  "refundAllocationIds": ["uuid"],
  "promotionAdjustmentIds": ["uuid"]
}
```

The financial engine re-evaluates promotion validity after the refund scope is known.

Refund APIs must support item-scoped, ChefOrderGroup-scoped, and delivery-scoped refunds where the business workflow permits.

---

# 36. Delivery APIs

## Create Delivery

```http
POST /api/v1/orders/{orderId}/delivery
```

## Get Delivery

```http
GET /api/v1/orders/{orderId}/delivery
```

## Provider Webhook

```http
POST /api/v1/webhooks/delivery/{provider}
```

The webhook maps provider lifecycle to internal Delivery/Order states.

---

# 37. Food Request APIs

## Create Food Request

```http
POST /api/v1/food-requests
```

```json
{
  "requestedName": "Mysore Masala Dosa",
  "masterFoodId": "uuid",
  "description": "Authentic Mysore-style dosa",
  "referenceUrl": "https://youtube.com/...",
  "location": {
    "lat": 45.5,
    "lng": -73.6
  },
  "notifyWhenAvailable": true
}
```

## Nearby Requests for Chef

```http
GET /api/v1/food-requests/nearby?lat=45.5&lng=-73.6&radiusMeters=10000
```

## Add Existing Request to Wishlist

```http
POST /api/v1/food-requests/{requestId}/interest
```

## Subscribe

```http
POST /api/v1/food-requests/{requestId}/subscription
```

## Chef Responds

```http
POST /api/v1/food-requests/{requestId}/responses
```

```json
{
  "responseType": "ADDING_TO_MENU",
  "foodListingId": "uuid"
}
```

The response must not grant unrestricted customer messaging automatically.

---

# 38. Chat APIs

## Conversation History

```http
GET /api/v1/chat/conversations/{conversationId}/messages?cursor=...
```

## Send Message

```http
POST /api/v1/chat/conversations/{conversationId}/messages
```

```json
{
  "message": "Your order is almost ready."
}
```

Real-time delivery may use WebSocket.

Authorization must validate the conversation relationship on every send/read operation.

---

# 39. Review APIs

```http
POST /api/v1/orders/{orderId}/food-reviews
POST /api/v1/orders/{orderId}/chef-review
GET /api/v1/food-listings/{foodListingId}/reviews
GET /api/v1/chef-businesses/{chefBusinessId}/reviews
```

Review creation must verify the customer actually purchased the relevant item/ordered from the Chef.

---

# 40. Administration APIs

Examples:

```http
GET    /api/v1/admin/users
GET    /api/v1/admin/orders
GET    /api/v1/admin/payments
GET    /api/v1/admin/refunds
GET    /api/v1/admin/payouts
GET    /api/v1/admin/payouts/{payoutId}
POST   /api/v1/admin/catalog/foods
POST   /api/v1/admin/catalog/equipment
POST   /api/v1/admin/promotions
POST   /api/v1/admin/delivery/providers
```

Admin APIs require dedicated permission checks such as:

```text
admin:users:read
admin:orders:read
admin:finance:read
admin:finance:write
admin:catalog:write
```

---
