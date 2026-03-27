# CRUD Security Tests Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Write MockMvc tests that verify unauthenticated and insufficiently-authorised requests are rejected (HTTP 403) on the secured CRUD endpoints.

**Architecture:** A single new test class `CrudSecurityTest` extends `AbstractBlackBoxTest` (full Spring Boot context + Testcontainers Postgres + MockMvc with `springSecurity()`). Tests call `mockMvc` directly rather than through `AbstractBlackBoxTest.assertThat_POST`, which unconditionally adds an ADMIN user. Two entities are tested — `Simple` (requires `SYS__ADMIN`) and `AllFieldTypes` (requires `WRITE`) — with two negative scenarios each for create, update, and delete. The security annotations are already in place, so all tests are expected to pass on first run.

**Tech Stack:** Kotlin, JUnit 5, Spring Boot Test, Spring Security Test (`SecurityMockMvcRequestPostProcessors.csrf()`, `user().authorities()`), MockMvcTester (assertj), Testcontainers.

---

### Task 1: Write `CrudSecurityTest`

**Files:**
- Create: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/security/CrudSecurityTest.kt`

**Key facts:**
- `AbstractBlackBoxTest.mockMvc` is `protected` — accessible directly in the subclass.
- `csrf()` injects a valid CSRF token via the app's `CsrfTokenRepository`, avoiding false positives from the CSRF filter.
- `user("x").authorities(SimpleGrantedAuthority("READ"))` creates an authenticated user with only the `READ` authority — insufficient for either `SYS__ADMIN` or `WRITE`.
- Request bodies are `{}` — the security check fires before bean validation.
- Delete uses a random UUID string — the security check fires before the service is invoked.
- Both unauthenticated and wrong-authority requests expect `403 Forbidden`. Spring Security returns 403 in this configuration (no form login configured, `Http403ForbiddenEntryPoint` is the default entry point).

- [ ] **Step 1: Create the test file**

Create `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/security/CrudSecurityTest.kt` with this content:

```kotlin
package org.maiaframework.showcase.security

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractBlackBoxTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import java.util.UUID

class CrudSecurityTest : AbstractBlackBoxTest() {

    private val emptyJson = "{}"
    private val randomId = UUID.randomUUID().toString()

    // ─── Simple entity (requires SYS__ADMIN) ──────────────────────────────────

    @Test
    fun `simple create - unauthenticated - is forbidden`() {
        assertThat(
            mockMvc.post().uri("/api/simple/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyJson)
                .with(csrf())
                .exchange()
        ).hasStatus(HttpStatus.FORBIDDEN)
    }

    @Test
    fun `simple create - wrong authority - is forbidden`() {
        assertThat(
            mockMvc.post().uri("/api/simple/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyJson)
                .with(csrf())
                .with(user("x").authorities(SimpleGrantedAuthority("READ")))
                .exchange()
        ).hasStatus(HttpStatus.FORBIDDEN)
    }

    @Test
    fun `simple update - unauthenticated - is forbidden`() {
        assertThat(
            mockMvc.put().uri("/api/simple/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyJson)
                .with(csrf())
                .exchange()
        ).hasStatus(HttpStatus.FORBIDDEN)
    }

    @Test
    fun `simple update - wrong authority - is forbidden`() {
        assertThat(
            mockMvc.put().uri("/api/simple/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyJson)
                .with(csrf())
                .with(user("x").authorities(SimpleGrantedAuthority("READ")))
                .exchange()
        ).hasStatus(HttpStatus.FORBIDDEN)
    }

    @Test
    fun `simple delete - unauthenticated - is forbidden`() {
        assertThat(
            mockMvc.delete().uri("/api/simple/$randomId")
                .with(csrf())
                .exchange()
        ).hasStatus(HttpStatus.FORBIDDEN)
    }

    @Test
    fun `simple delete - wrong authority - is forbidden`() {
        assertThat(
            mockMvc.delete().uri("/api/simple/$randomId")
                .with(csrf())
                .with(user("x").authorities(SimpleGrantedAuthority("READ")))
                .exchange()
        ).hasStatus(HttpStatus.FORBIDDEN)
    }

    // ─── AllFieldTypes entity (requires WRITE) ────────────────────────────────

    @Test
    fun `all field types create - unauthenticated - is forbidden`() {
        assertThat(
            mockMvc.post().uri("/api/all_field_types/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyJson)
                .with(csrf())
                .exchange()
        ).hasStatus(HttpStatus.FORBIDDEN)
    }

    @Test
    fun `all field types create - wrong authority - is forbidden`() {
        assertThat(
            mockMvc.post().uri("/api/all_field_types/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyJson)
                .with(csrf())
                .with(user("x").authorities(SimpleGrantedAuthority("READ")))
                .exchange()
        ).hasStatus(HttpStatus.FORBIDDEN)
    }

    @Test
    fun `all field types update - unauthenticated - is forbidden`() {
        assertThat(
            mockMvc.put().uri("/api/all_field_types/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyJson)
                .with(csrf())
                .exchange()
        ).hasStatus(HttpStatus.FORBIDDEN)
    }

    @Test
    fun `all field types update - wrong authority - is forbidden`() {
        assertThat(
            mockMvc.put().uri("/api/all_field_types/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyJson)
                .with(csrf())
                .with(user("x").authorities(SimpleGrantedAuthority("READ")))
                .exchange()
        ).hasStatus(HttpStatus.FORBIDDEN)
    }

    @Test
    fun `all field types delete - unauthenticated - is forbidden`() {
        assertThat(
            mockMvc.delete().uri("/api/all_field_types/$randomId")
                .with(csrf())
                .exchange()
        ).hasStatus(HttpStatus.FORBIDDEN)
    }

    @Test
    fun `all field types delete - wrong authority - is forbidden`() {
        assertThat(
            mockMvc.delete().uri("/api/all_field_types/$randomId")
                .with(csrf())
                .with(user("x").authorities(SimpleGrantedAuthority("READ")))
                .exchange()
        ).hasStatus(HttpStatus.FORBIDDEN)
    }

}
```

- [ ] **Step 2: Run the tests**

```bash
./gradlew :maia-showcase:app:test --tests "org.maiaframework.showcase.security.CrudSecurityTest"
```

Expected: `BUILD SUCCESSFUL`, all 12 tests pass.

If any test fails with a status other than 403 (e.g. 401), update the assertion in that test to match the actual status Spring Security returns in this configuration. If a test passes when it should fail (i.e. a 2xx is returned), that means the security annotation is not being applied — investigate whether `@EnableMethodSecurity` is active and the correct `SecurityFilterChain` bean is being loaded.

- [ ] **Step 3: Commit**

```bash
git add maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/security/CrudSecurityTest.kt
git commit -m "test: verify unauthenticated and unauthorised requests are rejected on secured CRUD endpoints"
```
