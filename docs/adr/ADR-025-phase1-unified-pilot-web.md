# ADR-025 — Phase-1 Unified Pilot Web Topology

## Status

Accepted

Accepted during `P1-ARCH-01` architecture reconciliation on 2026-09-01.

## Context

The long-term product describes distinct Entrepreneur, professional, and
Customer web experiences. The Phase-1 pilot, however, has one public
credibility surface plus a small authenticated operator and Chef workflow. The
repository contains the intended application directories but no implemented
web application whose separation must be preserved. Deploying and operating
three independent pilot web applications would add release, authentication,
design, and hosting overhead before those boundaries have independent scale or
team ownership.

ADR-003 already accepts Next.js App Router and shared web packages. This ADR
chooses the bounded deployment topology for the pilot without changing the
long-term experience boundaries.

## Decision

Phase 1 uses one deployed Next.js application in `apps/customer-web`:

```text
public credibility surface       /
operator workspace               /app/operator/*
Chef workspace                   /app/chef/*
```

The public routes implement the LP-01 credibility surface. Protected routes
use role-aware navigation, server-enforced authorization, resource ownership,
and cache boundaries. Hiding a route in navigation is not authorization.
Authenticated responses must not be shared through public caches.

`apps/business-web` and `apps/chef-web` remain reserved for the long-term
separate deployments. Phase 1 does not independently deploy them and does not
move their future domain ownership into `customer-web`. Web code continues to
use the generated OpenAPI client, design tokens, and genuinely shared UI
packages. Mobile applications use the same backend API and are not coupled to
this web deployment choice.

The topology must be reviewed after controlled-pilot evidence or before a
broader release. Independent apps become justified by demonstrated team
ownership, security isolation, deployment cadence, scale, regulatory, or
operational requirements—not simply by the existence of distinct roles.

## Consequences

- The pilot has one deployment, authentication integration, and design shell.
- Public SEO and the two protected workflows can be delivered incrementally.
- Backend module, authorization, and product-experience boundaries remain
  independent of the deployment count.
- Later extraction remains straightforward because routes and feature code are
  role-scoped and share contracts rather than private cross-route state.

## Alternatives Considered

### Deploy Separate Business and Chef Applications Immediately

Rejected for Phase 1 because there is no existing implementation to protect
and the operational cost is disproportionate to the bounded pilot surface.

### Create a New Pilot-Only Application Directory

Rejected because it would introduce temporary repository topology and a later
migration without a capability unavailable in the existing public web app.

### Treat One Application as One Authorization Boundary

Rejected. Deployment topology does not replace backend role, permission,
membership, data-scope, or resource-ownership enforcement.

## References

- ADR-003 — Next.js for Web
- `docs/product/LP-01-public-credibility-surface-spec.md`
- `docs/product/P1-MVP-01-chef-kitchen-pilot-marketplace-spec.md`
