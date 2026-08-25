# ADR-002 — Event-Driven Integration Through Outbox

## ADR-002 — Event-Driven Integration Through Outbox

**Status:** Accepted

**Decision:** Use transactional outbox + SQS/EventBridge for asynchronous integration.

**Why:** Notifications, delivery integration, analytics, and search indexing do not need to block the primary transaction.

**Rejected:** Kafka for MVP.

**Consequence:** Consumers must be idempotent and eventually consistent where applicable.

---

