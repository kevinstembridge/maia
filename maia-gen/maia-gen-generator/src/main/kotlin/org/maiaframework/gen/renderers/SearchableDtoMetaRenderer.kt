package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.SearchableDtoDef
import org.maiaframework.jdbc.JdbcCompatibleType

class SearchableDtoMetaRenderer(
    private val searchableDtoDef: SearchableDtoDef
): AbstractKotlinRenderer(
    searchableDtoDef.metaClassDef
) {


    override fun renderFunctions() {

        `render function fieldNameToColumnName`()
        `render function fieldNameToJdbcType`()

    }


    private fun `render function fieldNameToColumnName`() {

        appendLine("""
            |
            |
            |    fun fieldNameToColumnName(dtoFieldName: String): String {
            |
            |        return when(dtoFieldName) {""".trimMargin())

        searchableDtoDef.allFields.forEach { field ->
            appendLine("            \"${field.classFieldName}\" -> \"${field.schemaAndTableName}.${field.databaseColumn}\"")
        }

        appendLine("""
            |            else -> throw IllegalArgumentException("Unknown field name [${'$'}dtoFieldName]. Expected one of ${searchableDtoDef.allFields.map { it.classFieldName }}")
            |        }
            |
            |    }
        """.trimMargin())

    }


    private fun `render function fieldNameToJdbcType`() {

        addImportFor<JdbcCompatibleType>()

        appendLine("""
            |
            |
            |    fun fieldNameToJdbcType(dtoFieldName: String): JdbcCompatibleType {
            |
            |        return when(dtoFieldName) {""".trimMargin())

        searchableDtoDef.allFields.forEach { field ->
            appendLine("            \"${field.classFieldName}\" -> JdbcCompatibleType.${field.entityFieldDef.fieldType.jdbcCompatibleType!!.name}")
        }

        appendLine("""
            |            else -> throw IllegalArgumentException("Unknown field name [${'$'}dtoFieldName]. Expected one of ${searchableDtoDef.allFields.map { it.classFieldName }}")
            |        }
            |
            |    }
        """.trimMargin())

    }


}
