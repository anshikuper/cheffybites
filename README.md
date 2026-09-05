# Cheffy Bites

This repository currently implements the P1-S00-T01 checkpoint-1 foundation:
a backend-local Spring Boot build, one unified Phase-1 Next.js shell,
deterministic OpenAPI client generation, and reproducible local dependencies.

## Prerequisites

- Eclipse Temurin Java 21
- Node.js 24.20.0 (see `.nvmrc`)
- Corepack with pnpm 11.25.0
- Docker Desktop or a compatible Docker Engine with Compose

The Gradle root is `backend/`; there is intentionally no root Gradle
multiproject build.

## Install and verify

```bash
corepack pnpm install --frozen-lockfile
corepack pnpm verify
JAVA_HOME=/path/to/java-21 backend/gradlew -p backend clean check
```

Run the workspaces independently when needed:

```bash
corepack pnpm --filter @cheffybites/customer-web build
corepack pnpm --filter @cheffybites/api-client type-check
```

## Local infrastructure

Start digest-pinned PostGIS and Mailpit:

```bash
corepack pnpm infra:up
```

Stop services while preserving their local named volumes:

```bash
corepack pnpm infra:down
```

The following command is intentionally destructive and removes only the local
Compose project's volumes. It is not the future application DEMO reset:

```bash
corepack pnpm infra:reset:local
```

Default endpoints are PostgreSQL on `127.0.0.1:5432`, Mailpit SMTP on
`127.0.0.1:1025`, and Mailpit HTTP on `http://127.0.0.1:8025`.

Run the backend shell with the local profile:

```bash
JAVA_HOME=/path/to/java-21 backend/gradlew -p backend bootRun --args='--spring.profiles.active=local'
```

Run the web shell:

```bash
corepack pnpm --filter @cheffybites/customer-web dev
```

## Checkpoint boundaries

This checkpoint intentionally contains no Flyway migration, application-owned
schema, seed, outbox runtime, business endpoint, domain behavior, live Auth0
integration, Turborepo implementation, or adjacent web/mobile application.
Protected `/app/operator/*` and `/app/chef/*` routes fail closed until the
approved identity slice supplies a real backend-enforced session boundary.

See the compatibility evidence in
`docs/implementation/P1-S00-T01-compatibility-report.md`.
