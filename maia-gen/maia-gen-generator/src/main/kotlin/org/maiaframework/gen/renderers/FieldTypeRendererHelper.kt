package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.ListFieldType
import org.maiaframework.gen.spec.definition.lang.SetFieldType
import org.maiaframework.jdbc.JdbcCompatibleType

object FieldTypeRendererHelper {


    fun determineSqlDataType(fieldType: FieldType): String {

        val arraySuffix = if (fieldType is ListFieldType || fieldType is SetFieldType) {
            "[]"
        } else {
            ""
        }

        val jdbcCompatibleType = fieldType.jdbcCompatibleType

        return when (jdbcCompatibleType) {
            JdbcCompatibleType.jsonb -> jdbcCompatibleType.postgresDataType
            else -> "${jdbcCompatibleType!!.postgresDataType}$arraySuffix"
        }

    }


}
