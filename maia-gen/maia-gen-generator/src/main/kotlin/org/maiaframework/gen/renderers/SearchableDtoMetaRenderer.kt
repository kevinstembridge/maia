package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.SearchableDtoDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
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

        append("""
            |
            |
            |    fun fieldNameToColumnName(dtoFieldName: String): String {
            |
            |        return when(dtoFieldName) {
            |""".trimMargin())

        searchableDtoDef.nonManyToManyFields.forEach { field ->

            val columnExpression = if (field.entityAndField.entityDef.hasEffectiveTimestamps) {
                when (field.classFieldName) {
                    ClassFieldName.effectiveFrom -> "lower(${field.schemaAndTableName}.effective_range)"
                    ClassFieldName.effectiveTo -> "upper(${field.schemaAndTableName}.effective_range)"
                    else -> "${field.schemaAndTableName}.${field.databaseColumn}"
                }
            } else {
                "${field.schemaAndTableName}.${field.databaseColumn}"
            }

            appendLine("            \"${field.classFieldName}\" -> \"$columnExpression\"")

        }

        append($$"""
            |            else -> throw IllegalArgumentException("Unknown field name [$dtoFieldName]. Expected one of $${searchableDtoDef.nonManyToManyFields.map { it.classFieldName }}")
            |        }
            |
            |    }
            |""".trimMargin())

    }


    private fun `render function fieldNameToJdbcType`() {

        addImportFor<JdbcCompatibleType>()

        append("""
            |
            |
            |    fun fieldNameToJdbcType(dtoFieldName: String): JdbcCompatibleType {
            |
            |        return when(dtoFieldName) {
            |""".trimMargin())

        searchableDtoDef.nonManyToManyFields.forEach { field ->

            val jdbcTypeName = field.entityFieldDef.fieldType.jdbcCompatibleType!!.name
            appendLine("            \"${field.classFieldName}\" -> JdbcCompatibleType.$jdbcTypeName")

        }

        appendLine(
            $$"""
            |            else -> throw IllegalArgumentException("Unknown field name [$dtoFieldName]. Expected one of $${searchableDtoDef.nonManyToManyFields.map { it.classFieldName }}")
            |        }
            |
            |    }""".trimMargin())

    }


}
