# Cheffy Bites — API Contracts

> **Source of Truth:** This document is **subsidiary** to [`02-detailed-architecture.md`](docs/02-detailed-architecture.md) Sections 22–40.
> All API contract changes must be made in `02-detailed-architecture.md` first.
> This document exists for convenient reference and will be regenerated from `02` during CI.
> The authoritative machine-readable contract is the OpenAPI document generated from the backend.

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
  "description": "Commercial kitchen facility"
}
```

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

---

# 28. Kitchen Booking APIs

## Search Availability

```http
GET /api/v1/kitchen-spaces/{spaceId}/availability?from=...&to=...
```

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

## Schedule Availability

```http
POST /api/v1/food-listings/{foodListingId}/availability
```

```json
{
  "startAt": "2026-09-05T12:00:00-04:00",
  "endAt": "2026-09-05T15:00:00-04:00",
  "cutoffMinutes": 60
}
```

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
  "chefOrderGroups": [
    {
      "id": "uuid",
      "chefBusinessId": "uuid",
      "status": "PENDING_ACCEPTANCE",
      "subtotalMinor": 5000,
      "discountMinor": 1000,
      "netMinor": 4000,
      "promotionSnapshotId": "uuid",
      "financialSnapshotId": "uuid",
      "paymentAllocationIds": ["uuid"],
      "refundAllocationIds": [],
      "payoutLineIds": ["uuid"]
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

## ChefOrderGroup List

```http
GET /api/v1/chef-order-groups?chefBusinessId=uuid&page=0&size=20
```

Returns paginated ChefOrderGroups for the authenticated Chef.

## ChefOrderGroup Detail

```http
GET /api/v1/chef-order-groups/{chefOrderGroupId}
```

Returns the authenticated Chef's ChefOrderGroup details.

## ChefOrderGroup Financials

```http
GET /api/v1/chef-order-groups/{chefOrderGroupId}/financials
```

Returns immutable promotion, payment, refund, payout, and ledger evidence for the ChefOrderGroup.

## ChefOrderGroup Actions

```http
POST /api/v1/chef-order-groups/{chefOrderGroupId}/accept
POST /api/v1/chef-order-groups/{chefOrderGroupId}/reject
POST /api/v1/chef-order-groups/{chefOrderGroupId}/preparing
POST /api/v1/chef-order-groups/{chefOrderGroupId}/ready
```

State transitions must be validated server-side and rejected with `409 Conflict` on invalid transitions.

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
