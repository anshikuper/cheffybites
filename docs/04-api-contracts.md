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

Mutation idempotency for payment/reservation-sensitive APIs:

```text
Idempotency-Key: <unique-key>
```

Correlation:

```text
X-Correlation-ID: <request-id>
```

Timezone modeling follows the repository ADR-011 decision while ADR-011 remains Proposed until explicitly accepted. API fields representing real instants require RFC 3339 / ISO 8601 values containing `Z` or an explicit UTC offset, such as `2026-08-27T15:00:00Z` or `2026-08-27T11:00:00-04:00`. An offset-free value such as `2026-08-27T11:00:00` must not be silently interpreted as UTC for a real-instant field.

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
  "displayName": "Jane Doe",
  "roles": ["CHEF_OWNER"],
  "organizations": [
    {
      "id": "uuid",
      "name": "Jane Foods",
      "type": "CHEF_BUSINESS",
      "permissions": ["food:write", "order:read"]
    }
  ]
}
```

Booking creation and confirmation must be idempotent, and booking concurrency must rely on PostgreSQL range constraints rather than advisory locks.

## 25.2 Create Organization

```http
POST /api/v1/organizations
```

Request:

```json
{
  "type": "ENTREPRENEUR_BUSINESS",
  "name": "ABC Kitchens"
}
```

---

# 26. Kitchen APIs

## List/Search Kitchens

```http
GET /api/v1/kitchens?lat=45.5&lng=-73.6&radiusMeters=10000&availableFrom=...&availableTo=...
```

Filters:

- Latitude/longitude.
- Radius.
- Date/time.
- Equipment.
- Price range.
- Capacity.
- Amenities.

## Create Kitchen

```http
POST /api/v1/kitchens
```

```json
{
  "locationId": "uuid",
  "name": "Hello Kitchen",
  "description": "Commercial kitchen facility",
  "timezoneId": "America/Toronto"
}
```

`timezoneId` is the authoritative Kitchen IANA timezone. Abbreviations or fixed offsets such as `EST`, `EDT`, and `UTC-5` are not valid substitutes for the business timezone identity.

## Create Space

```http
POST /api/v1/kitchens/{kitchenId}/spaces
```

```json
{
  "name": "Space 1",
  "description": "Baking and prep space",
  "capacity": 4,
  "hourlyRateMinor": 2500,
  "currency": "CAD",
  "minimumBookingMinutes": 180,
  "cleaningMinutes": 60
}
```

## Publish Kitchen

```http
POST /api/v1/kitchens/{kitchenId}/publication
```

---

# 27. Equipment APIs

## Search Master Equipment

```http
GET /api/v1/equipment/catalog?q=tandoor&category=cooking
```

## Add Included Equipment

```http
POST /api/v1/kitchen-spaces/{spaceId}/equipment
```

```json
{
  "equipmentCatalogItemId": "uuid",
  "quantity": 1,
  "included": true
}
```

## Add Rental Equipment

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

---

# 28. Kitchen Booking APIs

## Search Availability

```http
GET /api/v1/kitchen-spaces/{spaceId}/availability?from=...&to=...
```

Availability results are informational snapshots only. They do not hold the Space or reserve EquipmentRental capacity; authoritative capacity is established only by a successfully committed transactional `HELD` or `CONFIRMED` booking reservation.

## Quote Booking

```http
POST /api/v1/kitchen-bookings/quote
```

```json
{
  "spaceId": "uuid",
  "startAt": "2026-09-01T10:00:00Z",
  "endAt": "2026-09-01T14:00:00Z",
  "equipment": [
    {
      "equipmentRentalId": "uuid",
      "quantity": 1
    }
  ],
  "promoCode": "WELCOME10"
}
```

Each `equipmentRentalId` identifies an EquipmentRental offered by the requested `spaceId`; a rental belonging to another Kitchen Space is invalid for the quote. Equipment availability and pricing in this response are an informational snapshot only. A quote does not hold the Space or reserve equipment and does not guarantee that capacity remains available when booking reservation is attempted.

## Create Booking

```http
POST /api/v1/kitchen-bookings
Idempotency-Key: booking-unique-key
```

Request:

```json
{
  "spaceId": "uuid",
  "startAt": "2026-09-01T10:00:00Z",
  "endAt": "2026-09-01T14:00:00Z",
  "equipment": [
    {
      "equipmentRentalId": "uuid",
      "quantity": 1
    }
  ],
  "promoCode": "WELCOME10"
}
```

Each `equipmentRentalId` must belong to the selected `spaceId` and be active/reservable. The backend transactionally revalidates every requested rental and its capacity; no client-side availability result or earlier quote is authoritative.

Equipment capacity is guaranteed only after a successful transactional booking reservation reaches `HELD` or `CONFIRMED`. That transaction validates all requested rentals and commits all EquipmentBookings, EquipmentAllocations, and the capacity-reserving Kitchen Booking state together. If any rental lacks capacity, the complete reservation attempt fails with `409 Conflict`; no partial equipment reservation or capacity-reserving booking transition commits.

The `Idempotency-Key` applies to booking and equipment-reservation side effects. Repeating the same operation under its idempotency semantics must not create duplicate EquipmentBookings or EquipmentAllocations. PostgreSQL lock waiting, lock ordering, and any bounded internal deadlock retry are server concerns and require no client-managed database retry behavior.

Response:

```json
{
  "bookingId": "uuid",
  "status": "PAYMENT_PENDING",
  "pricing": {
    "subtotalMinor": 10000,
    "discountMinor": 1000,
    "feeMinor": 500,
    "taxMinor": 1400,
    "totalMinor": 10900,
    "currency": "CAD"
  },
  "payment": {
    "paymentId": "uuid",
    "clientSecret": "provider-generated client secret"
  }
}
```

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
