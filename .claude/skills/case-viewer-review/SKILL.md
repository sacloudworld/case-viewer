---
name: case-viewer-review
description: Review the case-viewer Spring Boot app (Case/Activity domain, JWT auth, dual JPA/MyBatis data access) against its known risk areas. Use when asked to review, audit, or get context on this app, or before making changes to auth, the Case API, or logging config.
---

# Case Viewer review checklist

`case-viewer` is a Spring Boot 4 / Java 21 REST service (originally an "Order
Service" template) whose domain is `Case` → `Activity` (one-to-many). Use this
checklist when reviewing the app or before touching the areas it covers.

## Project shape

- Auth: `POST /api/auth/register`, `POST /api/auth/login` (public) issue a JWT.
  `JwtAuthenticationFilter` validates the token on every request via
  `CustomUserDetailsService` → `UserCacheService` (Redis-backed via Spring Cache).
- Case API: `GET /api/cases/{caseNumber}` (JWT-protected) returns a case with its
  activities via JPA (`CaseRepository`). A `Semaphore(5)` in
  `DatabaseConcurrencyConfig` throttles concurrent DB access and throws
  `DatabaseCapacityException` (503) when exhausted.
- A second, parallel data path exists: `CaseController.findCase` /
  `CaseService.findCase` uses a MyBatis mapper (`resources/mapper/CaseMapper.xml`,
  `mybatis.entity.Case`) to query the same `cases` table directly with raw SQL,
  independent of the JPA path. Treat JPA (`entity.Case`) and MyBatis
  (`mybatis.entity.Case`) as two distinct models of the same table — changes to one
  do not automatically apply to the other.
- Config: `application.properties` (shared) + `application-dev.properties` /
  `application-prod.properties` (profile-specific). Prod pulls secrets from AWS
  Secrets Manager / Parameter Store (`spring.config.import`); dev has plaintext
  local Postgres/Redis credentials and a placeholder `jwt.secret`.

## Known risk areas to re-check on every review

1. **Credential/token logging.** Nothing in the auth path (`JwtAuthenticationFilter`,
   `CustomUserDetailsService`, `UserCacheService`) should log raw JWTs or password
   hashes, or use `System.out.println` — use the class `Logger` at `DEBUG`, and never
   include the token or password field. See `docs/adr/0001-remove-credential-exposure-in-auth-path.md`.
2. **In-memory retention of user records.** Watch for any collection (static or
   otherwise) that accumulates `CachedUser`/`User` objects across requests without
   being bounded or cleared — this previously happened via a `leakedUsers` static
   list in `CustomUserDetailsService`.
3. **Security DEBUG logging scope.** `logging.level.org.springframework.security`
   should only be `DEBUG` in `application-dev.properties`, never in the shared
   `application.properties` or in `application-prod.properties`.
4. **Consistency between the two Case data paths.** When one of
   `CaseService.getCase` (JPA) or `CaseService.findCase` (MyBatis) changes error
   handling, concurrency guarding (the `databaseSemaphore`), or response shape, check
   whether the other path needs the same change. `findCase` currently returns `null`
   (200 + empty body) on a miss instead of a 404 — confirm this is intentional before
   extending that pattern elsewhere.
5. **Authorization scope on write endpoints.** `POST /api/cases/create` only
   requires *any* authenticated user (`/api/cases/**` → `.authenticated()` in
   `SecurityConfig`) with no ownership/tenant check. The `tenant-id` header read in
   `CaseController.getCase` is currently unused — if tenant scoping is added, wire it
   through `CaseService`, not just the controller signature.
6. **Dead code left from the Order→Case migration.** `ActivityController` is fully
   commented out; large commented-out blocks remain in `CaseController`. Confirm
   before deleting whether they're placeholders for planned endpoints or should be
   removed outright.

## When reviewing

Read `SecurityConfig`, `JwtAuthenticationFilter`, `JwtService`, `AuthService`,
`CustomUserDetailsService`, `UserCacheService`, `CaseController`, `CaseService`,
`CaseMapper.xml`, and the three `application*.properties` files — that set covers
essentially all request-path security and data-access behavior in this app.
