package org.maiaframework.gen.schemacheck

/**
 * Normalizes the type representation reported by `information_schema.columns`
 * (`data_type` / `udt_name` / `character_maximum_length`) into the same string
 * space used by `ExpectedColumnDef.postgresType` (i.e. `JdbcCompatibleType.postgresDataType`,
 * with a `(length)` suffix for `varchar`).
 */
object PostgresActualTypeNormalizer {

    fun normalize(dataType: String, udtName: String, characterMaximumLength: Int?): String {

        val baseType = when (dataType) {
            "ARRAY" -> normalizeArrayElementType(udtName)
            "character varying" -> "varchar"
            "character" -> "varchar"
            "text" -> "text"
            "boolean" -> "boolean"
            "integer" -> "integer"
            "smallint" -> "smallint"
            "bigint" -> "bigint"
            "numeric" -> "decimal"
            "date" -> "date"
            "jsonb" -> "jsonb"
            "uuid" -> "uuid"
            "timestamp without time zone" -> "timestamp"
            "timestamp with time zone" -> "timestamp(3) with time zone"
            else -> throw IllegalArgumentException("Unrecognized Postgres data_type '$dataType' (udt_name '$udtName') — add a mapping in PostgresActualTypeNormalizer.")
        }

        return if (baseType == "varchar" && characterMaximumLength != null) {
            "varchar($characterMaximumLength)"
        } else {
            baseType
        }

    }

    private fun normalizeArrayElementType(udtName: String): String {

        val elementType = when (udtName) {
            "_bool" -> "boolean"
            "_int2" -> "smallint"
            "_int4" -> "integer"
            "_int8" -> "bigint"
            "_numeric" -> "decimal"
            "_text", "_varchar" -> "text"
            "_timestamp" -> "timestamp"
            "_uuid" -> "uuid"
            else -> throw IllegalArgumentException("Unrecognized Postgres array udt_name '$udtName' — add a mapping in PostgresActualTypeNormalizer.")
        }

        return "$elementType[]"

    }

}
