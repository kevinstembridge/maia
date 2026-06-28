package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.TimelineBlotterDef
import org.maiaframework.jdbc.JdbcCompatibleType
import org.maiaframework.lang.text.StringFunctions


class TimelineBlotterRowDtoMetaRenderer(
    private val def: TimelineBlotterDef
) : AbstractKotlinRenderer(
    def.metaClassDef
) {


    override fun renderFunctions() {

        `render function fieldNameToColumnName`()
        `render function fieldNameToJdbcType`()

    }


    private fun `render function fieldNameToColumnName`() {

        val allFieldNames = buildAllFieldNames()

        append("""
            |
            |
            |    fun fieldNameToColumnName(dtoFieldName: String): String {
            |
            |        return when(dtoFieldName) {
            |""".trimMargin())

        appendLine("            \"eventTimestamp\" -> \"event_timestamp\"")
        appendLine("            \"eventType\" -> \"event_type\"")
        appendLine("            \"changeType\" -> \"change_type\"")
        appendLine("            \"version\" -> \"version\"")

        def.entityHistoryColumns.forEach { col ->
            val fieldName = col.classFieldDef.classFieldName.value
            val columnName = StringFunctions.toSnakeCase(fieldName)
            appendLine("            \"$fieldName\" -> \"$columnName\"")
        }

        def.joinDefs.forEach { joinDef ->
            appendLine("            \"${joinDef.rightFkDtoFieldName}\" -> \"${joinDef.rightFkSqlAlias}\"")
            appendLine("            \"${joinDef.displayFieldDtoFieldName}\" -> \"${joinDef.displayFieldSqlAlias}\"")
        }

        append("""
            |            else -> throw IllegalArgumentException("Unknown field name [${'$'}dtoFieldName]. Expected one of $allFieldNames")
            |        }
            |
            |    }
            |""".trimMargin())

    }


    private fun `render function fieldNameToJdbcType`() {

        addImportFor<JdbcCompatibleType>()

        val allFieldNames = buildAllFieldNames()

        append("""
            |
            |
            |    fun fieldNameToJdbcType(dtoFieldName: String): JdbcCompatibleType {
            |
            |        return when(dtoFieldName) {
            |""".trimMargin())

        appendLine("            \"eventTimestamp\" -> JdbcCompatibleType.timestamp")
        appendLine("            \"eventType\" -> JdbcCompatibleType.text")
        appendLine("            \"changeType\" -> JdbcCompatibleType.text")
        appendLine("            \"version\" -> JdbcCompatibleType.bigint")

        def.entityHistoryColumns.forEach { col ->
            val fieldName = col.classFieldDef.classFieldName.value
            val jdbcTypeName = col.classFieldDef.fieldType.jdbcCompatibleType.name
            appendLine("            \"$fieldName\" -> JdbcCompatibleType.$jdbcTypeName")
        }

        def.joinDefs.forEach { joinDef ->
            appendLine("            \"${joinDef.rightFkDtoFieldName}\" -> JdbcCompatibleType.uuid")
            appendLine("            \"${joinDef.displayFieldDtoFieldName}\" -> JdbcCompatibleType.text")
        }

        appendLine(
            $$"""
            |            else -> throw IllegalArgumentException("Unknown field name [$dtoFieldName]. Expected one of $$allFieldNames")
            |        }
            |
            |    }""".trimMargin())

    }


    private fun buildAllFieldNames(): List<String> {

        val names = mutableListOf("eventTimestamp", "eventType", "changeType", "version")
        def.entityHistoryColumns.forEach { names.add(it.classFieldDef.classFieldName.value) }
        def.joinDefs.forEach { joinDef ->
            names.add(joinDef.rightFkDtoFieldName)
            names.add(joinDef.displayFieldDtoFieldName)
        }

        return names

    }


}
