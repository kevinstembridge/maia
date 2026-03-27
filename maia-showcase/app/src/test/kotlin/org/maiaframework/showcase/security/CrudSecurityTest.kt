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
    private val stubId = UUID.randomUUID().toString()

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
            mockMvc.delete().uri("/api/simple/$stubId")
                .with(csrf())
                .exchange()
        ).hasStatus(HttpStatus.FORBIDDEN)
    }

    @Test
    fun `simple delete - wrong authority - is forbidden`() {
        assertThat(
            mockMvc.delete().uri("/api/simple/$stubId")
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
            mockMvc.delete().uri("/api/all_field_types/$stubId")
                .with(csrf())
                .exchange()
        ).hasStatus(HttpStatus.FORBIDDEN)
    }

    @Test
    fun `all field types delete - wrong authority - is forbidden`() {
        assertThat(
            mockMvc.delete().uri("/api/all_field_types/$stubId")
                .with(csrf())
                .with(user("x").authorities(SimpleGrantedAuthority("READ")))
                .exchange()
        ).hasStatus(HttpStatus.FORBIDDEN)
    }

}
