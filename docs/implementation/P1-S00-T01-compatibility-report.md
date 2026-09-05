# P1-S00-T01 Compatibility Report

## Status
**Pre-scaffolding gate: passed on 2026-09-04.**

**Final checkpoint verdict: PASSED on 2026-09-05.**

This report records the version-discovery gate required by the approved
[`P1-S00-T01 bounded implementation plan`](../../plans/P1-S00-T01-bounded-implementation-plan.md:126).
All probes ran from `/tmp/cheffy-p1-s00-t01-probe`; no application source was
scaffolded before every required compatibility row passed.

The final checkpoint verdict is now complete: all repository manifests,
source shells, generated artifacts, and CI-equivalent checks are implemented
and passing.

## Repository baseline

| Item | Verified value |
|---|---|
| Branch | `feature/p1-foundation` |
| Implementation HEAD before scaffolding | `cc813c5c294e2cde35044388fa0f8671de53cd32` |
| Canonical architecture baseline | `d6fa1983ce77b10a9dbd574788f1de74004038be` |
| ADR-026 | Present in branch ancestry and accepted at [`ADR-026`](../adr/ADR-026-spring-boot-4-junit-6.md:28) |
| Pre-scaffolding worktree | Clean |

No commit or push was made during discovery.

## Selected compatibility matrix

### Runtime, backend, and data

| Component | Selected version/reference | Pin/evidence | Gate result |
|---|---:|---|---|
| Java | Eclipse Temurin `21.0.12+1` | Adoptium release API; macOS arm64 SHA-256 `3623232f33a9c3baadf304480b2535f9a3cba8a58d42ecbb438ba267315d9998`; Linux x64 SHA-256 `ce79869e1307ed8ee1e2baa86a412b1eb5b75d10a01006d788a6f968bcfaee94` | PASS |
| Gradle Wrapper | `9.7.1` | Official distribution SHA-256 `acd53f1edaf02f1a8ff99879f8a34b302661a057d9b063ae9e35b552f804d20a` | PASS |
| Spring Boot | `4.1.1` | Accepted [`ADR-026`](../adr/ADR-026-spring-boot-4-junit-6.md:28) | PASS |
| Spring Framework | `7.0.9` | Spring Boot dependency management | PASS |
| Spring Security | `7.1.1` | Spring Boot dependency management | PASS |
| Hibernate ORM | `7.4.5.Final` | Spring Boot dependency management | PASS |
| Flyway | `12.4.0` | Spring Boot dependency management; compatibility only, with no T01 migration | PASS |
| PostgreSQL JDBC | `42.7.13` | Spring Boot dependency management | PASS |
| Micrometer Tracing | `1.7.1` | Spring Boot dependency management | PASS |
| OpenTelemetry SDK | `1.62.0` | Spring Boot dependency management | PASS |
| JUnit Jupiter / Platform | `6.0.3` | Accepted [`ADR-026`](../adr/ADR-026-spring-boot-4-junit-6.md:28) and Spring Boot-managed module set | PASS |
| Testcontainers | `2.0.5` | Exact Testcontainers BOM plus PostgreSQL/JUnit Jupiter modules | PASS |
| UUIDv7 library | `com.github.f4b6a3:uuid-creator:6.1.1` | Exact Maven artifact; `UuidCreator.getTimeOrderedEpoch()` | PASS |
| PostgreSQL/PostGIS image | `postgis/postgis:16-3.5@sha256:94146ac37bc61e2322f88016056c5920729cb8c64c8542ed590af8fc2abdac07` | Docker Hub OCI index; PostgreSQL `16.9`, PostGIS `3.5.2`, `btree_gist` `1.7` observed at runtime | PASS |
| Mailpit image | `axllent/mailpit:v1.31.0@sha256:c96991d9bef73594c246d89ca81411d4e916f03e76a7d2d72fa2ab5dd3c9ce24` | Docker Hub multi-architecture OCI index | PASS |

The PostGIS index currently publishes a Linux amd64 runtime manifest only.
Docker Desktop successfully ran that manifest through Apple Silicon emulation;
Linux amd64 CI is native. This is a documented local-performance limitation,
not a database correctness failure.

### Web and contract tooling

| Component | Selected version | Compatibility constraint/evidence | Gate result |
|---|---:|---|---|
| Node.js | `24.20.0` LTS (`Krypton`) | Official release checksums: Darwin arm64 `40e5607e5ecb3db9192723776da2d75d966260fc74a7a9e731c1bd67dda96bc8`; Linux x64 `855d581f8a4eb1a8117e3426de25fe02770592febcfb31369aee1ffbfee9e8ec` | PASS |
| pnpm | `11.25.0` | Requires Node `>=22.13`; package-manager integrity is recorded in the root manifest | PASS |
| Next.js | `16.3.4` | Requires Node `>=20.9`; supports React 19 | PASS |
| React / React DOM | `19.2.8` | Matches Next.js and Auth0 peer ranges | PASS |
| TypeScript | `6.0.3` | Inside `typescript-eslint` range `>=4.8.4 <6.1.0` | PASS |
| Tailwind CSS / PostCSS adapter | `4.3.3` | Production build compiled the imported stylesheet | PASS |
| TanStack Query | `5.102.8` | Supports React 18 or 19; provider compiled and built | PASS |
| Zod | `4.5.4` | Runtime import compiled and built | PASS |
| ESLint / core configuration | `10.9.1` / `@eslint/js` `10.0.1` | ESLint 10 supports Node 24; no unsupported Next shareable-config peer graph is retained | PASS |
| typescript-eslint | `8.69.0` | Supports ESLint 10 and TypeScript below 6.1 | PASS |
| globals | `16.4.0` | Exact lint-environment metadata | PASS |
| Vitest (api-client) | `5.0.0` | Supports Node 24; package versions match exactly | PASS |
| Vitest (customer-web) | `4.1.11` | Bounded corrective probe passed under Node 24.20.0 / TypeScript 6.0.3 / Next.js 16.3.4 / Testing Library jest-dom 7.0.1 | PASS |
| @vitest/coverage-v8 (api-client) | `5.0.0` | Matches Vitest 5.0.0 | PASS |
| @vitest/coverage-v8 (customer-web) | `4.1.11` | Matches Vitest 4.1.11 | PASS |
| jsdom | `30.0.1` | Supports Node `^24.15.0`; DOM component test passed | PASS |
| Testing Library React / DOM / jest-dom | `16.3.3` / `10.4.1` / `7.0.1` | React 19 and Vitest-compatible peer ranges | PASS |
| Playwright | `1.62.1` | Supports Node 20+; Chromium production-server smoke passed | PASS |
| Redocly CLI | `2.51.1` | Supports Node `>=22.12`; OpenAPI lint passed without warnings | PASS |
| OpenAPI dialect | `3.0.3` | Avoids OpenAPI Generator's OpenAPI 3.1 beta path; sufficient for canonical common components | PASS |
| OpenAPI Generator CLI | `7.25.0` | Maven CLI JAR SHA-256 `41ce4f6b07f196676439d710759fa1ced7a08066d06ff1bf314681470289efae`; `typescript-fetch` is marked stable | PASS |
| Auth0 Next.js SDK probe | `4.28.0` | Peer range includes Next `^16.0.10` and React `^19.2.1` | PASS; probe only |

The Auth0 package is not retained by T01. Live tenant configuration and the
actual identity boundary remain deferred to P1-S01. The isolated App Router
probe compiled [`Auth0Client.getSession()`](../../apps/customer-web:1) usage,
fail-closed redirect behavior, and a dynamic/no-store protected route with
inert local placeholders and no Auth0 network call.

**Vitest 5.0.0 / @vitest/coverage-v8 5.0.0 for customer-web is recorded as a
FAILED CANDIDATE.** Under Node 24.20.0 / TypeScript 6.0.3 / Next.js 16.3.4 /
Testing Library jest-dom 7.0.1, customer-web produced declaration errors
including `@vitest/expect` resolution, `vitest/browser` `MarkOptions`, and
`Assertion` declaration conflicts. The bounded corrective probe to customer-web
Vitest 4.1.11 + @vitest/coverage-v8 4.1.11 passed. Vitest 5.0.0 remains the
selected and passing baseline for `packages/api-client`.

### Immutable CI actions and scanners

| Tool/action | Human-readable release | Immutable pin | Gate result |
|---|---:|---|---|
| `actions/checkout` | `v5.0.0` | `08c6903cd8c0fde910a37f88322edcfb5dd907a8` | Tag resolved independently; PASS |
| `actions/setup-java` | `v5.0.0` | `dded0888837ed1f317902acf8a20df0ad188d165` | Tag resolved independently; PASS |
| `actions/setup-node` | `v5.0.0` | `a0853c24544627f65ddf259abe73b1d18a591444` | Tag resolved independently; PASS |
| `pnpm/action-setup` | `v6.0.10` | `0977fd99725f1db4007ccb2928dbb4e90d06cc86` | Package-manager field is authoritative; correction pending remote re-run |
| `google/osv-scanner-action` | `v2.5.1` | `6e4298ebc4db23e847df9b2e2de2939d6f066c67` | Tag resolved independently; pnpm lock scan passed |
| `gitleaks/gitleaks-action` | `v3.0.0` | `e0c47f4f8be36e29cdc102c57e68cb5cbf0e8d1e` | Correction pending remote re-run |
| Gitleaks CLI | `8.30.1` | Official release checksum verified | Clean Git-history scan passed |
| OSV-Scanner CLI | `2.5.1` | Darwin arm64 SHA-256 `75c44d6332f892a1e56286f4105a98ed751ae28d215ca0a8b65cc00d84103054` | pnpm lock scan found no issues |

## Compatibility proofs

### Java and Spring

- Gradle 9.7.1 started with Temurin Java 21 and compiled the Kotlin DSL build.
- Spring Boot 4.1.1 compiled MVC, Validation, Security/OAuth2 Resource Server,
  JPA/Hibernate, Actuator, Flyway/PostgreSQL, and OpenTelemetry starters.
- A Spring application context loaded under JUnit Jupiter 6.0.3 with one test,
  zero failures, and zero errors.
- Dependency resolution selected JUnit Jupiter and Platform 6.0.3. A
  Testcontainers constraint requested a JUnit 5-era API coordinate, but Gradle
  selected 6.0.3 through the authoritative Spring Boot constraints; no JUnit 5
  API or engine is present in the resolved runtime.

### Testcontainers, PostGIS, GiST, and UUIDv7

An actual JUnit 6 test used Testcontainers 2.0.5 and the exact selected PostGIS
digest through the non-deprecated
`org.testcontainers.postgresql.PostgreSQLContainer` API. It proved:

- JDBC startup and connection;
- PostgreSQL 16, PostGIS 3.5, and available `btree_gist` 1.7 versions;
- `postgis` and `btree_gist` extension availability;
- database acceptance of adjacent half-open `[)` timestamp ranges;
- SQLSTATE `23P01` for a conflicting overlap under a GiST exclusion;
- RFC 9562 UUID version 7, RFC variant 2, and lossless PostgreSQL UUID
  round-trip.

A separate 10,000-value UUIDv7 sample found no duplicate and no invalid
version/variant value.

### Containers

- The pinned PostGIS image passed health, SQL, extension, and GiST probes.
- The pinned Mailpit image passed health, accepted SMTP mail, and returned the
  message through its HTTP API.
- No real mail account or credential was used.

### Web, Auth0 boundary, and browser

- Node 24.20.0 and pnpm 11.25.0 installed the exact graph and reproduced it
  using a frozen lockfile.
- Peer validation reported no issue in the final web graph.
- ESLint 10 typed lint, strict TypeScript 6.0.3 compilation, a Testing Library
  component test, V8 coverage, and a Next.js 16.3.4 production build passed.
- Tailwind CSS, TanStack Query, and Zod were imported by executable probe code,
  not merely installed.
- The public route was statically generated and served by `next start`.
- Playwright 1.62.1 launched its pinned Chromium revision and verified the
  production page and `en-CA` document language.
- Auth0 4.28.0 compiled with a protected App Router route. Next classified the
  protected route as dynamic while the public route remained static.
- customer-web uses `@testing-library/jest-dom/vitest` typings for Vitest 4.1.11
  compatibility.
- Shared library baseline uses ES2024; JavaScript target remains ES2022.

### OpenAPI and generated client

- OpenAPI 3.0.3 lint passed under Redocly 2.51.1 without warnings.
- The final endpoint-free baseline keeps `paths: {}` and intentionally disables
  only Redocly's unused-component rule because common components precede the
  first canonical business operation.
- OpenAPI Generator 7.25.0 generated `typescript-fetch` output twice.
- After removing generator bookkeeping, both trees were byte-for-byte equal.
- The generated error model and runtime compiled under strict TypeScript 6.0.3.
- No runtime Spring documentation dependency was selected because the
  checkpoint exposes no application endpoint and the machine-readable contract
  is maintained directly.

## Candidates rejected or adjusted

| Candidate | Finding | Resolution |
|---|---|---|
| TypeScript `7.0.2` | Outside `typescript-eslint` 8.69.0 support (`<6.1.0`) | Selected stable TypeScript `6.0.3` |
| ESLint `9.39.5` plus `eslint-config-next` `16.3.4` | Next's plugin graph accepted ESLint 9 but npm marked that patch unsupported after ESLint 10 adoption | Selected ESLint 10.9.1 with official core rules and typed TypeScript rules; Next type-check/build remain separate gates |
| ESLint `10.9.1` plus `eslint-config-next` `16.3.4` | Several transitive Next lint plugins did not declare ESLint 10 peers | Omitted the incompatible shareable config instead of forcing peers |
| OpenAPI `3.1.1` | Redocly accepted it, but OpenAPI Generator 7.25.0 labels 3.1 support beta | Selected OpenAPI `3.0.3` |
| OpenAPI Generator npm wrapper `2.41.0` | Its install script conflicted with pnpm 11's explicit build policy | Use the exact Maven CLI artifact through a dedicated Gradle configuration |
| PostGIS tag without `docker exec -i` | SQL was not piped to `psql`, producing an invalid initial probe | Corrected command and asserted extension versions, row count, and SQLSTATE explicitly |
| Testcontainers legacy generic PostgreSQL class | Compiled with a deprecation warning | Selected the Testcontainers 2.x non-generic PostgreSQL module class |
| System Corepack `0.30.0` | Stale signing-key metadata failed before pnpm activation | Final repository uses the exact pnpm action/package-manager pin; scratch probes executed the already-verified pnpm 11.25.0 archive under Node 24.20.0 |

## Supply-chain notes

- pnpm 11's build-script policy explicitly denies the unnecessary
  `unrs-resolver` postinstall; its signed platform package is present and lint
  works without executing the script.
- Frozen installation and pnpm peer checks passed.
- The local production-license inventory contained only `0BSD`, `Apache-2.0`,
  `BSD-3-Clause`, `CC-BY-4.0`, `ISC`, `LGPL-3.0-or-later`, and `MIT` identifiers.
- The npm advisory POST endpoint timed out repeatedly even though ordinary npm
  registry GET requests returned HTTP 200. This was an upstream endpoint/network
  failure rather than a reported vulnerability. OSV-Scanner therefore provides
  the reproducible dependency scan. Final CI still must scan the generated
  backend lock/SBOM after repository dependency locking exists.
- Gitleaks scanned the existing Git history without credentials and reported no
  leak.

## Deferred risks and non-claims

- Local PostGIS success does not prove future AWS RDS extension availability;
  the selected PostgreSQL/PostGIS major remains subject to infrastructure-stage
  RDS validation.
- Live Auth0 tenant configuration, tokens, callback routes, rolling sessions,
  and backend authorization are not implemented by this checkpoint.
- No Flyway migration, application schema, entity, repository, business API,
  outbox runtime, event, domain behavior, or adjacent application was probed as
  implemented.
- Turborepo remains the long-term architecture decision but is not part of T01.
- The complete T01 verdict may be marked passed only after all repository and CI
  checks described by the bounded plan pass against checked-in manifests and
  generated output.

## Final verification results

### Backend verification (executed 2026-09-04)

| Command | Result |
|---|---|
| `./gradlew clean test` | PASS |
| `./gradlew integrationTest` | PASS |
| `./gradlew verifyResolvedJUnit6` | PASS |

All backend tests pass. The integration test source set is correctly configured
at `src/integrationTest/java` with `PostgisContainerCompatibilityIT` and
`UuidV7CompatibilityIT`. The Gradle dependency lock file (`gradle.lockfile`)
was generated and verified.

**verifyResolvedJUnit6 details:**
- Verifies resolved JUnit Jupiter/Platform artifacts are major 6
- Runs with configuration cache enabled
- First run stores configuration cache
- Second run reuses configuration cache
- No `--no-configuration-cache` workaround is required

### Dependency verification

- `backend/gradle/verification-metadata.xml` — generated with SHA-256 metadata
  using `./gradlew --write-verification-metadata sha256`
- Final file exists and final backend graph passes with dependency
  verification enabled
- `backend/gradle.lockfile` is the Gradle 9.7.1 lock artifact used by this
  checkpoint

### Frontend verification (executed 2026-09-04)

| Command | Result |
|---|---|
| `pnpm install --frozen-lockfile` | PASS |
| `pnpm lint` | PASS |
| `pnpm type-check` | PASS |
| `pnpm test` | PASS |
| `pnpm build` | PASS |
| `pnpm test:e2e` | PASS (3/3) |

### OpenAPI verification (executed 2026-09-04)

| Command | Result |
|---|---|
| `pnpm openapi:validate` | PASS |
| `pnpm api-client:generate` | PASS |
| `pnpm api-client:drift` | PASS |

**OpenAPI drift checker correction:** The drift checker initially had an
implementation defect: it deleted generated `.openapi-generator-ignore` before
comparing against the accepted generated directory. The checker was corrected
to compare that generated file consistently. Final API client drift check
passed (7 files). This is not OpenAPI generator nondeterminism.

### Foundation verification (executed 2026-09-04)

| Command | Result |
|---|---|
| `pnpm verify:metadata` | PASS |
| `git diff --check` | PASS |

### Protected Next.js routes (T01 fail-closed boundary)

- `/app/operator` fails closed with HTTP 404
- `/app/chef` fails closed with HTTP 404
- Both are private/no-store
- Production build and Playwright E2E pass
- T01 temporary request-level fail-closed boundary uses Next.js proxy
  before S01 Auth0 implementation

## GitHub Actions PR run (2026-09-05)

### Run identifier
GitHub Actions PR run **33971999943** on branch `feature/p1-foundation`.

### Diff Check
**PASS** — `git diff --check` passed.

### Initial CI wiring failures (5 jobs)

| Job | Failure | Root cause |
|---|---|---|
| Backend | `gradle/actions/setup-gradle@417ae3ccd767c254f566b4a1e5b39f4e13a7a0e0` could not be resolved | Invalid immutable SHA for v4.3.1 |
| Web | `actions/setup-node` with `cache: pnpm` executed before `pnpm/action-setup` installed pnpm | `Unable to locate executable file: pnpm` |
| Contract | Same pnpm cache-before-install ordering | `Unable to locate executable file: pnpm` |
| Security | Same pnpm cache-before-install ordering; additionally `google/osv-scanner-action@6e4298ebc4db23e847df9b2e2de2939d6f066c67` (repository root) is not a runnable action | `Top level 'runs:' section is required` |
| Containers | `sendmail: command not found` on Ubuntu runner | Ubuntu runner lacks `sendmail` binary |

### Corrected action evidence (CORRECTION PENDING REMOTE RE-RUN)

| Tool/action | Human-readable release | Corrected immutable pin | Status |
|---|---:|---|---|
| `gradle/actions/setup-gradle` | `v4.3.1` | `06832c7b30a0129d7fb559bcc6e43d26f6374244` | Corrected in `.github/workflows/ci.yml` |
| `pnpm/action-setup` | `v6.0.10` | `0977fd99725f1db4007ccb2928dbb4e90d06cc86` | Package-manager field is authoritative; reordered before `actions/setup-node` in Web, Contract, Security |
| `google/osv-scanner-action/osv-scanner-action` | `v2.5.1` | `6e4298ebc4db23e847df9b2e2de2939d6f066c67` | Corrected subdirectory path in Security job |
| Mailpit SMTP probe | — | Python 3 `smtplib` replacement | Replaced `sendmail` dependency in Containers job |
| `gitleaks/gitleaks-action` | `v3.0.0` | `e0c47f4f8be36e29cdc102c57e68cb5cbf0e8d1e` | Removed legacy args input and added `GITHUB_TOKEN` |

The corrected workflow has not been executed remotely yet. The next PR push will trigger a re-run.
### Second GitHub Actions PR run — 33971999943

The first correction commit was pushed and GitHub Actions PR run
**33971999943** executed on `feature/p1-foundation`.

**Diff Check: PASS**

The first-round wiring corrections were effective:

- Backend progressed past `gradle/actions/setup-gradle` setup and reached the Gradle build.
- Security progressed past the OSV scanner action-path failure.
- Containers progressed past the missing `sendmail` failure.
- Web and Contract progressed to `pnpm/action-setup`.

The run then exposed the following clean-run issues:

| Job | Failure | Root cause / correction |
|---|---|---|
| Backend | Dependency verification failed for additional plugin/classpath metadata artifacts | `backend/gradle/verification-metadata.xml` was regenerated using a clean temporary `GRADLE_USER_HOME` with `--write-verification-metadata sha256`. The resulting diff added 118 lines and removed 0 existing entries. |
| Web | `Multiple versions of pnpm specified` | Removed the explicit action `version:` input and retained the integrity-pinned `packageManager` field as authoritative. |
| Contract | Same duplicate pnpm version failure | Same correction as Web. |
| Security | Gitleaks required `GITHUB_TOKEN`; legacy `args` input was not accepted | Upgraded to `gitleaks/gitleaks-action` v3.0.0, removed legacy `args`, and supplied `${{ secrets.GITHUB_TOKEN }}`. |
| Containers | PostgreSQL was shutting down during extension verification | Readiness now waits for `PostgreSQL init process complete; ready for start up.` before final `pg_isready`, avoiding the temporary initialization server race. |

### Clean-cache backend verification

After regenerating Gradle verification metadata, the backend gate was executed from a fresh temporary Gradle user home:

```text
./gradlew clean test integrationTest verifyResolvedJUnit6

BUILD SUCCESSFUL
Configuration cache entry stored.
backend_clean_cache_exit=0
```

This confirms the regenerated dependency-verification metadata supports a clean dependency resolution while the application runtime baseline remains JUnit Jupiter / Platform 6.0.3.

### Second correction status

**CORRECTION PENDING REMOTE RE-RUN**

The current second-round corrections include:

- `pnpm/action-setup` v6.0.10 at `0977fd99725f1db4007ccb2928dbb4e90d06cc86`
- `gitleaks/gitleaks-action` v3.0.0 at `e0c47f4f8be36e29cdc102c57e68cb5cbf0e8d1e`
- PostgreSQL final-server readiness gate
- backend CI gate aligned to `clean test integrationTest verifyResolvedJUnit6`
- regenerated Gradle dependency-verification metadata

Remote CI has not yet validated this second correction set.

## Gate verdict

All mandatory pre-scaffolding compatibility rows have a supported exact
selection and a passing isolated proof. Repository scaffolding may proceed
without an architecture change.

## GitHub Actions PR run — 33979919846

Run **33979919846** executed on branch `feature/p1-foundation-ci-reconcile`.

### Passing jobs

- Contract — PASS
- Security — PASS
- Containers — PASS
- Diff Check — PASS

### Backend

Setup, PostgreSQL readiness, backend tests, and `verifyResolvedJUnit6` all
passed. The final Backend failure was only the invalid Gradle
`--verify-locks` command. Corrected CI uses normal dependency resolution with
the committed lock state and does not rewrite locks.

The corrected dependency-lock step is:

```text
./gradlew dependencies
```

### Web

pnpm setup, dependency installation, and lint passed. Type-check failed in the
clean checkout because Next-generated `.next/types` files were absent.
Customer-web type-check now runs:

```text
next typegen && tsc --noEmit
```

### Current reconciliation status

**CORRECTION PENDING REMOTE RE-RUN**

**Final T01 checkpoint: PENDING REMOTE CI** — Local verification passes, but T01 is not complete until the corrected GitHub Actions PR run passes.
bounded scope have been executed and the results recorded above. The
integration test source set is correctly placed, the CI workflow uses immutable
action SHAs, the Gradle dependency lock file exists, and the compatibility
report is finalized with actual command outcomes.

## GitHub Actions PR run — 33980525427

Run **33980525427** executed on branch `feature/p1-foundation-ci-reconcile`.

### Passing jobs

- Backend — PASS
- Containers — PASS
- Security — PASS
- Contract — PASS
- Diff Check — PASS

### Web

Install, lint, type-check, tests, and build all passed. E2E reached Playwright
successfully; the protected `/app/operator` and `/app/chef` tests passed. The
public-shell browser test failed only because Chromium had not been installed
on the clean runner.

CI now explicitly installs the Playwright Chromium browser and Linux
dependencies before E2E:

```text
pnpm --filter @cheffybites/customer-web exec playwright install --with-deps chromium
```

### Current reconciliation status

**CORRECTION PENDING REMOTE RE-RUN**

## GitHub Actions PR run — 33981303962

Run **33981303962** completed successfully on branch
`feature/p1-foundation-ci-reconcile` and is the acceptance evidence for the
P1-S00-T01 checkpoint.

### Job results

- Backend — PASS
- Web — PASS
- Containers — PASS
- Contract — PASS
- Security — PASS
- Diff Check — PASS

### Backend

PostgreSQL/PostGIS startup and readiness passed. Backend tests passed, and
dependency-lock verification passed.

### Web

The complete clean-run pipeline passed:

- Install dependencies
- Lint
- Type-check
- Tests
- Build
- Install Playwright Chromium
- E2E

### Final checkpoint status

**Final T01 checkpoint: PASSED**

Successful remote CI run **33981303962** is the acceptance evidence.
