package org.maiaframework.gen.schemacheck

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.maiaframework.jdbc.JdbcCompatibleType

class PostgresActualTypeNormalizerTest {

    data class Case(
        val jdbcCompatibleType: JdbcCompatibleType,
        val dataType: String,
        val udtName: String,
        val characterMaximumLength: Int? = null,
    )

    companion object {

        // One case per JdbcCompatibleType value, using the information_schema
        // data_type/udt_name representation Postgres actually reports for it.
        @JvmStatic
        fun cases(): List<Case> = listOf(
            Case(JdbcCompatibleType.bigint, "bigint", "int8"),
            Case(JdbcCompatibleType.boolean, "boolean", "bool"),
            Case(JdbcCompatibleType.date, "date", "date"),
            Case(JdbcCompatibleType.decimal, "numeric", "numeric"),
            Case(JdbcCompatibleType.integer, "integer", "int4"),
            Case(JdbcCompatibleType.jsonb, "jsonb", "jsonb"),
            Case(JdbcCompatibleType.smallint, "smallint", "int2"),
            Case(JdbcCompatibleType.text, "text", "text"),
            Case(JdbcCompatibleType.timestamp, "timestamp without time zone", "timestamp"),
            Case(JdbcCompatibleType.timestamp_with_time_zone, "timestamp with time zone", "timestamptz"),
            Case(JdbcCompatibleType.uuid, "uuid", "uuid"),
            Case(JdbcCompatibleType.varchar, "character varying", "varchar", characterMaximumLength = 100),
            Case(JdbcCompatibleType.boolean_array, "ARRAY", "_bool"),
            Case(JdbcCompatibleType.decimal_array, "ARRAY", "_numeric"),
            Case(JdbcCompatibleType.integer_array, "ARRAY", "_int4"),
            Case(JdbcCompatibleType.smallint_array, "ARRAY", "_int2"),
            Case(JdbcCompatibleType.text_array, "ARRAY", "_text"),
            Case(JdbcCompatibleType.timestamp_array, "ARRAY", "_timestamp"),
            Case(JdbcCompatibleType.uuid_array, "ARRAY", "_uuid"),
        )

    }

    @Test
    fun `test cases cover every JdbcCompatibleType value`() {

        val coveredTypes = cases().map { it.jdbcCompatibleType }

        assertThat(coveredTypes).containsExactlyInAnyOrderElementsOf(JdbcCompatibleType.entries)

    }

    @ParameterizedTest
    @MethodSource("cases")
    fun `normalizes information_schema types to the matching JdbcCompatibleType postgresDataType`(case: Case) {

        val actual = PostgresActualTypeNormalizer.normalize(case.dataType, case.udtName, case.characterMaximumLength)

        val expected = if (case.characterMaximumLength != null) {
            "${case.jdbcCompatibleType.postgresDataType}(${case.characterMaximumLength})"
        } else {
            case.jdbcCompatibleType.postgresDataType
        }

        assertThat(actual).isEqualTo(expected)

    }

    @Test
    fun `throws on an unrecognized data_type`() {

        assertThatThrownBy { PostgresActualTypeNormalizer.normalize("money", "money", null) }
            .isInstanceOf(IllegalArgumentException::class.java)

    }

    @Test
    fun `throws on an unrecognized array udt_name`() {

        assertThatThrownBy { PostgresActualTypeNormalizer.normalize("ARRAY", "_money", null) }
            .isInstanceOf(IllegalArgumentException::class.java)

    }

}
