package org.maiaframework.showcase.all_field_types

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractBlackBoxTest
import org.maiaframework.webapp.domain.auth.MaiaUserDetails
import org.springframework.security.core.GrantedAuthority
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import java.util.UUID

class AllFieldTypesBigDecimalRestTest : AbstractBlackBoxTest() {


    private val createdIds = mutableListOf<String>()


    @AfterEach
    fun deleteCreatedRows() {

        // The create endpoint commits, so remove our rows to avoid polluting other tests (e.g. Playwright CRUD).
        createdIds.forEach { id ->
            jdbcOps.update("delete from ${AllFieldTypesEntityMeta.SCHEMA_AND_TABLE_NAME} where id = '$id'")
        }

        createdIds.clear()

    }


    @Test
    fun `big decimal values round trip through create, update and read as exact JSON strings`() {

        val id = create(
            someBigDecimal = "\"12.34\"",
            someBigDecimalModifiable = "\"5.6\"",
            someBigDecimalNullable = "\"7\""
        )

        val created = fetchForEdit(id)
        created.bodyJson().extractingPath("$.someBigDecimal").isEqualTo("12.3400")
        created.bodyJson().extractingPath("$.someBigDecimalModifiable").isEqualTo("5.60")
        created.bodyJson().extractingPath("$.someBigDecimalNullable").isEqualTo("7.0000")

        update(id, someBigDecimalModifiable = "\"99999999.99\"", someBigDecimalNullable = "null")

        val updated = fetchForEdit(id)
        updated.bodyJson().extractingPath("$.someBigDecimalModifiable").isEqualTo("99999999.99")
        updated.bodyJson().extractingPath("$.someBigDecimalNullable").isNull()

    }


    @Test
    fun `big decimal values sent as JSON strings keep full precision`() {

        val id = create(
            someBigDecimal = "\"123456789012345.1234\"",
            someBigDecimalModifiable = "\"1.00\"",
            someBigDecimalNullable = "null"
        )

        fetchForEdit(id)
            .bodyJson()
            .extractingPath("$.someBigDecimal").isEqualTo("123456789012345.1234")

    }


    private fun create(
        someBigDecimal: String,
        someBigDecimalModifiable: String,
        someBigDecimalNullable: String
    ): String {

        val json = """
            {
                "someBigDecimal": $someBigDecimal,
                "someBigDecimalModifiable": $someBigDecimalModifiable,
                "someBigDecimalNullable": $someBigDecimalNullable,
                "someBoolean": true,
                "someBooleanType": true,
                "someBooleanTypeProvided": true,
                "someEnum": "OK",
                "someInstant": "2024-01-01T00:00:00Z",
                "someInstantModifiable": "2024-01-01T00:00:00Z",
                "someInt": 1,
                "someIntModifiable": 1,
                "someIntType": ${(1..Int.MAX_VALUE).random()},
                "someIntTypeProvided": 1,
                "someListOfStrings": ["item"],
                "someLocalDateModifiable": "2024-01-01",
                "someLongType": ${(1..Int.MAX_VALUE).random()},
                "someLongTypeProvided": 1,
                "somePeriodModifiable": "P1D",
                "someProvidedStringType": "abc",
                "someString": "${UUID.randomUUID()}",
                "someStringModifiable": "abc",
                "someStringType": "${UUID.randomUUID()}"
            }
        """.trimIndent()

        val result = mockMvc.post().uri("/api/all-field-types/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .with(csrf())
            .with(writer())
            .exchange()

        assertThat(result).hasStatus(HttpStatus.CREATED)

        return jsonMapperRead(result.response.contentAsString).also { createdIds.add(it) }

    }


    private fun jsonMapperRead(body: String): String =
        Regex("\"id\"\\s*:\\s*\"([^\"]+)\"").find(body)!!.groupValues[1]


    private fun update(id: String, someBigDecimalModifiable: String, someBigDecimalNullable: String) {

        val json = """
            {
                "id": "$id",
                "someBigDecimalModifiable": $someBigDecimalModifiable,
                "someBigDecimalNullable": $someBigDecimalNullable,
                "someInstantModifiable": "2024-01-01T00:00:00Z",
                "someIntModifiable": 1,
                "someListOfStrings": ["item"],
                "someLocalDateModifiable": "2024-01-01",
                "somePeriodModifiable": "P1D",
                "someStringModifiable": "abc"
            }
        """.trimIndent()

        assertThat(
            mockMvc.put().uri("/api/all-field-types/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .with(csrf())
                .with(writer())
                .exchange()
        ).hasStatusOk()

    }


    private fun fetchForEdit(id: String) =
        assertThat(
            mockMvc.get().uri("/api/all-field-types/fetch-for-edit/$id")
                .with(writer())
                .exchange()
        ).hasStatusOk()


    private fun writer() =
        user(
            MaiaUserDetails(
                "nigel",
                "password",
                enabled = true,
                accountNonExpired = true,
                credentialsNonExpired = true,
                accountNonLocked = true,
                grantedAuthorities = sortedSetOf<GrantedAuthority>(
                    Comparator.comparing { it.authority ?: "" },
                    SimpleGrantedAuthority("ROLE_ADMIN"),
                    SimpleGrantedAuthority("WRITE")
                ),
                userId = defaultUser.id,
                firstName = "Nigel",
                lastName = "Nigelson"
            )
        )


}
