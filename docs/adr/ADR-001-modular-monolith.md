# ADR-001 — Modular Monolith First

## ADR-001 — Modular Monolith First

**Status:** Accepted

**Decision:** Start with one deployable Spring Boot modular monolith.

**Why:**

- Strong transactional coupling.
- Lower operational complexity.
- Easier local development.
- Easier AI-assisted cross-module changes.
- Clear extraction path later.

**Rejected:** Immediate microservices.

**Consequence:** Modules must be designed for future extraction even though they share one runtime/database initially.

---

