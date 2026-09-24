# 1. Remove credential exposure in the authentication path

## Status

Accepted

## Context

A review of the authentication path (`CustomUserDetailsService`, `UserCacheService`,
`JwtAuthenticationFilter`) found several places where sensitive data — password
hashes and JWTs — is retained or emitted beyond what the request needs:

- `CustomUserDetailsService` keeps a `static List<CachedUser> leakedUsers` that every
  call to `loadUserByUsername` appends to. The list is never read and never cleared,
  so it grows for the lifetime of the JVM, holding every authenticated user's
  username, password hash, role, and enabled flag in memory indefinitely and making
  that data trivially recoverable from a heap dump.
- `JwtAuthenticationFilter.doFilterInternal` writes the raw JWT, the extracted
  username, the loaded `UserDetails`, and the resulting `Authentication` object to
  stdout via `System.out.println` on every authenticated request.
- `CustomUserDetailsService.loadUserByUsername` logs username, role, and enabled
  status at `INFO` on every lookup.
- `application.properties` (loaded in all profiles, not just `dev`) sets
  `logging.level.org.springframework.security=DEBUG`, which is broad enough to
  surface additional security-internal detail (including from Spring Security's own
  authentication machinery) in any environment that doesn't override it.

None of this data needs to persist past the single request/response cycle it
supports, and printing JWTs/password hashes to stdout or INFO logs means they end up
in log aggregation and shell history in plaintext.

## Decision

- Delete the `leakedUsers` field and the code in `CustomUserDetailsService` that
  appends to it. `loadUserByUsername` will only build and return the `UserDetails`
  it needs for the current request.
- Remove the `System.out.println` calls from `JwtAuthenticationFilter`. If
  request-scoped tracing is needed, use the class's `Logger` at `DEBUG` (never
  logging the raw token or password) instead of stdout.
- Drop the per-request `INFO` log of username/role/enabled in
  `CustomUserDetailsService`; if lookup auditing is needed, log at `DEBUG` without
  the role/enabled fields, which are not needed to diagnose lookup failures.
- Scope `logging.level.org.springframework.security=DEBUG` to
  `application-dev.properties` only, and remove it from the shared
  `application.properties` so production and any other profile default to `INFO`.

## Consequences

- Password hashes and JWTs are no longer retained in application memory or written
  to stdout/logs outside of the request that legitimately needs them.
- Heap dumps and log exports from this service no longer double as a credential
  store.
- Security-subsystem DEBUG logging becomes opt-in per profile instead of a global
  default, reducing log volume and information disclosure risk in production.
- Anyone debugging auth locally still has `dev` profile DEBUG logging available;
  they lose the removed `println` output but gain equivalent detail through the
  logger if it's added back deliberately and scoped.
