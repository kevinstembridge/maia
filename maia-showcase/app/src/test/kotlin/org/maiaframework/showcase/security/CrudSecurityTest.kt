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


    private val validSimpleCreateJson = """{"someString": "abc"}"""


    private val validSimpleUpdateJson = """{"id": "$stubId", "someString": "abc"}"""


    private val validAllFieldTypesCreateJson = """
        {
            "someBoolean": true,
            "someBooleanType": true,
            "someBooleanTypeProvided": true,
            "someEnum": "OK",
            "someInstant": "2024-01-01T00:00:00Z",
            "someInstantModifiable": "2024-01-01T00:00:00Z",
            "someInt": 1,
            "someIntModifiable": 1,
            "someIntType": 1,
            "someIntTypeProvided": 1,
            "someListOfStrings": ["item"],
            "someLocalDateModifiable": "2024-01-01",
            "someLongType": 1,
            "someLongTypeProvided": 1,
            "somePeriodModifiable": "P1D",
            "someProvidedStringType": "abc",
            "someString": "abc",
            "someStringModifiable": "abc",
            "someStringType": "abc"
        }
    """.trimIndent()


    private val validAllFieldTypesUpdateJson = """
        {
            "id": "$stubId",
            "someInstantModifiable": "2024-01-01T00:00:00Z",
            "someIntModifiable": 1,
            "someListOfStrings": ["item"],
            "someLocalDateModifiable": "2024-01-01",
            "somePeriodModifiable": "P1D",
            "someStringModifiable": "abc"
        }
    """.trimIndent()


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
                .content(validSimpleCreateJson)
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
                .content(validSimpleUpdateJson)
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
                .content(validAllFieldTypesCreateJson)
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
                .content(validAllFieldTypesUpdateJson)
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
