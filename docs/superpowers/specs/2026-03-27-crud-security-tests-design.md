# CRUD Security Tests Design

## Overview

Write MockMvc tests verifying that unauthenticated and insufficiently-authorised users cannot call the secured CRUD API endpoints.

## Background

The generator now emits `@PreAuthorize("hasAuthority('...')")` on all write endpoints (create, update, inline update, delete). `@EnableMethodSecurity` is active. The test needs to confirm the security is wired correctly end-to-end.

## Test Class

**File:** `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/security/CrudSecurityTest.kt`

Extends `AbstractBlackBoxTest` (Spring Boot test with real DB via Testcontainers, MockMvc with `springSecurity()` applied).

Does NOT use `AbstractBlackBoxTest.assertThat_POST` — that helper unconditionally adds an ADMIN user. Tests call `mockMvc` directly.

## Entities Under Test

| Entity | Required authority | Create URL | Update URL | Delete URL |
|---|---|---|---|---|
| `Simple` | `SYS__ADMIN` | `POST /api/simple/create` | `PUT /api/simple/update` | `DELETE /api/simple/{id}` |
| `AllFieldTypes` | `WRITE` | `POST /api/all_field_types/create` | `PUT /api/all_field_types/update` | `DELETE /api/all_field_types/{id}` |

## Security Scenarios

For each operation:

1. **Unauthenticated** — no `user()` post-processor → Spring Security's `Http403ForbiddenEntryPoint` returns `403 Forbidden`
2. **Wrong authority** — `.with(user("x").authorities("READ"))` → `@PreAuthorize` denies → `AccessDeniedHandler` returns `403 Forbidden`

Request bodies are empty JSON objects (`{}`). The security check fires before bean validation, so the body content is irrelevant.

Delete URL uses a random `DomainId` string — the security check fires before the service is invoked.

## Test Matrix

12 tests total:

| Operation | Scenario | Expected |
|---|---|---|
| `POST /api/simple/create` | unauthenticated | 403 |
| `POST /api/simple/create` | wrong authority | 403 |
| `PUT /api/simple/update` | unauthenticated | 403 |
| `PUT /api/simple/update` | wrong authority | 403 |
| `DELETE /api/simple/{id}` | unauthenticated | 403 |
| `DELETE /api/simple/{id}` | wrong authority | 403 |
| `POST /api/all_field_types/create` | unauthenticated | 403 |
| `POST /api/all_field_types/create` | wrong authority | 403 |
| `PUT /api/all_field_types/update` | unauthenticated | 403 |
| `PUT /api/all_field_types/update` | wrong authority | 403 |
| `DELETE /api/all_field_types/{id}` | unauthenticated | 403 |
| `DELETE /api/all_field_types/{id}` | wrong authority | 403 |

## CSRF Handling

Write operations pass through the CSRF filter before reaching the security annotation. Tests must include a valid CSRF token — otherwise the 403 would come from the CSRF filter, not `@PreAuthorize`, giving a false pass.

Use `SecurityMockMvcRequestPostProcessors.csrf()` as a request post-processor. This is simpler than fetching the `/csrf` cookie and avoids a round-trip GET.
