package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.TimelineBlotterDef
import org.maiaframework.gen.spec.definition.lang.DomainIdFieldType
import org.maiaframework.gen.spec.definition.lang.EnumFieldType
import org.maiaframework.gen.spec.definition.lang.InstantFieldType
import org.maiaframework.gen.spec.definition.lang.IntFieldType
import org.maiaframework.gen.spec.definition.lang.IntTypeFieldType
import org.maiaframework.gen.spec.definition.lang.IntValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.LongFieldType
import org.maiaframework.gen.spec.definition.lang.LongTypeFieldType
import org.maiaframework.gen.spec.definition.lang.StringFieldType
import org.maiaframework.gen.spec.definition.lang.StringTypeFieldType
import org.maiaframework.gen.spec.definition.lang.StringValueClassFieldType


class TimelineBlotterRowMapperRenderer(
    private val def: TimelineBlotterDef
) : AbstractKotlinRenderer(
    def.rowMapperClassDef
) {


    override fun renderFunctions() {

        addImportFor(Fqcns.MAIA_RESULT_SET_ADAPTER)
        addImportFor(def.rowDtoFqcn)
        addImportFor(Fqcns.MAIA_DOMAIN_ID)
        addImportFor(Fqcns.MAIA_CHANGE_TYPE)

        append("""
            |
            |
            |    override fun mapRow(rsa: ResultSetAdapter): ${def.rowDtoUqcn} {
            |
            |        val eventTimestamp = rsa.readInstant("event_timestamp")
            |        val eventType = rsa.readString("event_type")
            |        val changeType = rsa.readEnumOrNull("change_type", ChangeType::class.java)
            |        val version = rsa.readLongOrNull("version")
            |""".trimMargin())

        def.entityHistoryColumns.forEach { col ->
            val fieldName = col.classFieldDef.classFieldName.value
            val columnAlias = org.maiaframework.lang.text.StringFunctions.toSnakeCase(fieldName)
            renderNullableField(fieldName, columnAlias, col.classFieldDef.fieldType)
        }

        def.joinDefs.forEach { joinDef ->
            appendLine("        val ${joinDef.rightFkDtoFieldName} = rsa.readDomainIdOrNull(\"${joinDef.rightFkSqlAlias}\")")
            appendLine("        val ${joinDef.displayFieldDtoFieldName} = rsa.readStringOrNull(\"${joinDef.displayFieldSqlAlias}\")")
        }

        blankLine()
        appendLine("        return ${def.rowDtoUqcn}(")
        appendLine("            eventTimestamp,")
        appendLine("            eventType,")
        appendLine("            changeType,")
        appendLine("            version,")

        def.entityHistoryColumns.forEach { col ->
            appendLine("            ${col.classFieldDef.classFieldName.value},")
        }

        def.joinDefs.forEach { joinDef ->
            appendLine("            ${joinDef.rightFkDtoFieldName},")
            appendLine("            ${joinDef.displayFieldDtoFieldName},")
        }

        append("""
            |        )
            |
            |    }
            |""".trimMargin())

    }


    private fun renderNullableField(fieldName: String, columnAlias: String, fieldType: org.maiaframework.gen.spec.definition.lang.FieldType) {
        when (fieldType) {
            is EnumFieldType -> {
                addImportFor(fieldType.fqcn)
                appendLine("        val $fieldName = rsa.readEnumOrNull(\"$columnAlias\", ${fieldType.fqcn.uqcn}::class.java)")
            }
            is InstantFieldType -> appendLine("        val $fieldName = rsa.readInstantOrNull(\"$columnAlias\")")
            is IntFieldType -> appendLine("        val $fieldName = rsa.readIntOrNull(\"$columnAlias\")")
            is IntTypeFieldType -> {
                addImportFor(fieldType.fqcn)
                appendLine("        val $fieldName = rsa.readIntOrNull(\"$columnAlias\")?.let { ${fieldType.uqcn}(it) }")
            }
            is IntValueClassFieldType -> {
                addImportFor(fieldType.fqcn)
                appendLine("        val $fieldName = rsa.readIntOrNull(\"$columnAlias\")?.let { ${fieldType.uqcn}(it) }")
            }
            is LongFieldType -> appendLine("        val $fieldName = rsa.readLongOrNull(\"$columnAlias\")")
            is LongTypeFieldType -> {
                addImportFor(fieldType.fqcn)
                appendLine("        val $fieldName = rsa.readLongOrNull(\"$columnAlias\")?.let { ${fieldType.uqcn}(it) }")
            }
            is DomainIdFieldType -> appendLine("        val $fieldName = rsa.readDomainIdOrNull(\"$columnAlias\")")
            is StringFieldType -> appendLine("        val $fieldName = rsa.readStringOrNull(\"$columnAlias\")")
            is StringTypeFieldType -> {
                addImportFor(fieldType.fqcn)
                appendLine("        val $fieldName = rsa.readStringOrNull(\"$columnAlias\")?.let { ${fieldType.uqcn}(it) }")
            }
            is StringValueClassFieldType -> {
                addImportFor(fieldType.fqcn)
                appendLine("        val $fieldName = rsa.readStringOrNull(\"$columnAlias\")?.let { ${fieldType.uqcn}(it) }")
            }
            else -> appendLine("        val $fieldName = rsa.readStringOrNull(\"$columnAlias\")")
        }
    }


}
