package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityHistoryBlotterDef
import org.maiaframework.jdbc.JdbcCompatibleType


class EntityHistoryBlotterRowDtoMetaRenderer(
    private val def: EntityHistoryBlotterDef
) : AbstractKotlinRenderer(
    def.metaClassDef
) {


    override fun renderFunctions() {

        `render function fieldNameToColumnName`()
        `render function fieldNameToJdbcType`()

    }


    private fun `render function fieldNameToColumnName`() {

        val fieldNames = def.blotterColumns.map { it.classFieldDef.classFieldName.value }

        append("""
            |
            |
            |    fun fieldNameToColumnName(dtoFieldName: String): String {
            |
            |        return when(dtoFieldName) {
            |""".trimMargin())

        def.blotterColumns.forEach { col ->
            val fieldName = col.classFieldDef.classFieldName.value
            val columnName = col.tableColumnName.value
            appendLine("            \"$fieldName\" -> \"${def.historyTableSchemaAndTable}.$columnName\"")
        }

        append("""
            |            else -> throw IllegalArgumentException("Unknown field name [${'$'}dtoFieldName]. Expected one of $fieldNames")
            |        }
            |
            |    }
            |""".trimMargin())

    }


    private fun `render function fieldNameToJdbcType`() {

        addImportFor<JdbcCompatibleType>()

        val fieldNames = def.blotterColumns.map { it.classFieldDef.classFieldName.value }

        append("""
            |
            |
            |    fun fieldNameToJdbcType(dtoFieldName: String): JdbcCompatibleType {
            |
            |        return when(dtoFieldName) {
            |""".trimMargin())

        def.blotterColumns.forEach { col ->
            val fieldName = col.classFieldDef.classFieldName.value
            val jdbcTypeName = col.classFieldDef.fieldType.jdbcCompatibleType.name
            appendLine("            \"$fieldName\" -> JdbcCompatibleType.$jdbcTypeName")
        }

        appendLine(
            $$"""
            |            else -> throw IllegalArgumentException("Unknown field name [$dtoFieldName]. Expected one of $${fieldNames}")
            |        }
            |
            |    }""".trimMargin())

    }


}
