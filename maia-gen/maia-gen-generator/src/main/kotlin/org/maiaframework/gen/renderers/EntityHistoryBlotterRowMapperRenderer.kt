package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityFieldDef
import org.maiaframework.gen.spec.definition.EntityHistoryBlotterDef
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.BooleanFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanTypeFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.DataClassFieldType
import org.maiaframework.gen.spec.definition.lang.DomainIdFieldType
import org.maiaframework.gen.spec.definition.lang.DoubleFieldType
import org.maiaframework.gen.spec.definition.lang.EnumFieldType
import org.maiaframework.gen.spec.definition.lang.EsDocFieldType
import org.maiaframework.gen.spec.definition.lang.ForeignKeyFieldType
import org.maiaframework.gen.spec.definition.lang.FqcnFieldType
import org.maiaframework.gen.spec.definition.lang.JoinFetchDtoFieldType
import org.maiaframework.gen.spec.definition.lang.InstantFieldType
import org.maiaframework.gen.spec.definition.lang.IntFieldType
import org.maiaframework.gen.spec.definition.lang.IntTypeFieldType
import org.maiaframework.gen.spec.definition.lang.IntValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.ListFieldType
import org.maiaframework.gen.spec.definition.lang.LocalDateFieldType
import org.maiaframework.gen.spec.definition.lang.LongFieldType
import org.maiaframework.gen.spec.definition.lang.LongTypeFieldType
import org.maiaframework.gen.spec.definition.lang.MapFieldType
import org.maiaframework.gen.spec.definition.lang.ObjectIdFieldType
import org.maiaframework.gen.spec.definition.lang.PeriodFieldType
import org.maiaframework.gen.spec.definition.lang.PkAndNameFieldType
import org.maiaframework.gen.spec.definition.lang.RequestDtoFieldType
import org.maiaframework.gen.spec.definition.lang.SetFieldType
import org.maiaframework.gen.spec.definition.lang.SimpleResponseDtoFieldType
import org.maiaframework.gen.spec.definition.lang.StringFieldType
import org.maiaframework.gen.spec.definition.lang.StringTypeFieldType
import org.maiaframework.gen.spec.definition.lang.StringValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.UrlFieldType


class EntityHistoryBlotterRowMapperRenderer(
    private val def: EntityHistoryBlotterDef
) : AbstractKotlinRenderer(
    def.rowMapperClassDef
) {


    init {

        if (def.requiresJsonMapper) {
            addConstructorArg(ClassFieldDef.aClassField("jsonMapper", Fqcns.JACKSON_JSON_MAPPER).privat().build())
        }

    }


    override fun renderFunctions() {

        addImportFor(Fqcns.MAIA_RESULT_SET_ADAPTER)
        addImportFor(def.rowDtoFqcn)

        append("""
            |
            |
            |    override fun mapRow(rsa: ResultSetAdapter): ${def.rowDtoUqcn} {
            |
            |""".trimMargin())

        val sortedColumns = def.blotterColumns.sortedBy { it.classFieldDef.classFieldName.value }

        sortedColumns.forEach { col ->
            renderRowMapperField(col)
        }

        blankLine()
        appendLine("        return ${def.rowDtoUqcn}(")

        sortedColumns.forEach { col ->
            appendLine("            ${col.classFieldDef.classFieldName.value},")
        }

        append("""
            |        )
            |
            |    }
            |""".trimMargin())

    }


    private fun renderRowMapperField(col: EntityFieldDef) {

        val fieldName = col.classFieldDef.classFieldName.value
        val columnName = fieldName
        val fieldType = col.classFieldDef.fieldType

        when (fieldType) {
            is EnumFieldType -> {
                addImportFor(fieldType.fqcn)
                appendLine("        val $fieldName = rsa.readEnum(\"$columnName\", ${fieldType.fqcn.uqcn}::class.java)")
            }
            is BooleanFieldType -> appendLine("        val $fieldName = rsa.readBoolean(\"$columnName\")")
            is BooleanTypeFieldType -> {
                addImportFor(fieldType.fqcn)
                appendLine("        val $fieldName = rsa.readBoolean(\"$columnName\") { ${fieldType.uqcn}(it) }")
            }
            is BooleanValueClassFieldType -> {
                addImportFor(fieldType.fqcn)
                appendLine("        val $fieldName = rsa.readBoolean(\"$columnName\") { ${fieldType.uqcn}(it) }")
            }
            is DataClassFieldType -> TODO()
            is DomainIdFieldType -> appendLine("        val $fieldName = rsa.readDomainId(\"$columnName\")")
            is DoubleFieldType -> appendLine("        val $fieldName = rsa.readDouble(\"$columnName\")")
            is EsDocFieldType -> TODO()
            is ForeignKeyFieldType -> when (val pkType = fieldType.pkFieldType) {
                is DomainIdFieldType -> appendLine("        val $fieldName = rsa.readDomainId(\"$columnName\")")
                is StringTypeFieldType -> {
                    addImportFor(pkType.fqcn)
                    appendLine("        val $fieldName = rsa.readString(\"$columnName\") { ${pkType.uqcn}(it) }")
                }
                else -> TODO("FK to non-UUID, non-String PK not yet supported")
            }
            is FqcnFieldType -> TODO()
            is JoinFetchDtoFieldType -> TODO("YAGNI?")
            is PkAndNameFieldType -> TODO()
            is InstantFieldType -> appendLine("        val $fieldName = rsa.readInstant(\"$columnName\")")
            is IntFieldType -> appendLine("        val $fieldName = rsa.readInt(\"$columnName\")")
            is IntTypeFieldType -> {
                addImportFor(fieldType.fqcn)
                appendLine("        val $fieldName = rsa.readInt(\"$columnName\") { ${fieldType.uqcn}(it) }")
            }
            is IntValueClassFieldType -> {
                addImportFor(fieldType.fqcn)
                appendLine("        val $fieldName = rsa.readInt(\"$columnName\") { ${fieldType.uqcn}(it) }")
            }
            is ListFieldType -> {
                val listElementFieldType = fieldType.parameterFieldType
                when (listElementFieldType) {
                    is DataClassFieldType -> {
                        addImportFor(Fqcns.JACKSON_TYPE_REFERENCE)
                        addImportFor(listElementFieldType.fqcn)
                        val nullableSuffix = if (col.nullable) "OrNull" else ""
                        appendLine("        val $fieldName = rsa.readString$nullableSuffix(\"$columnName\") { jsonMapper.readValue(it, object : TypeReference<${col.classFieldDef.unqualifiedToString}>() {}) }")
                    }
                    is EnumFieldType -> {
                        addImportFor(listElementFieldType.fqcn)
                        appendLine("        val $fieldName = rsa.readListOfStrings(\"$columnName\") { ${listElementFieldType.fqcn.uqcn}.valueOf(it) }")
                    }
                    is InstantFieldType -> appendLine("        val $fieldName = rsa.readListOfInstants(\"$columnName\")")
                    is LocalDateFieldType -> appendLine("        val $fieldName = rsa.readListOfLocalDates(\"$columnName\")")
                    is StringFieldType -> appendLine("        val $fieldName = rsa.readListOfStrings(\"$columnName\")")
                    is StringTypeFieldType -> {
                        addImportFor(listElementFieldType.fqcn)
                        appendLine("        val $fieldName = rsa.readListOfStrings(\"$columnName\") { ${listElementFieldType.fqcn.uqcn}(it) }")
                    }
                    is StringValueClassFieldType -> {
                        addImportFor(listElementFieldType.fqcn)
                        appendLine("        val $fieldName = rsa.readListOfStrings(\"$columnName\") { ${listElementFieldType.fqcn.uqcn}(it) }")
                    }
                    else -> throw NotImplementedError("No list row mapper implementation for element type ${listElementFieldType::class.simpleName} on field $fieldName")
                }
            }
            is LocalDateFieldType -> appendLine("        val $fieldName = rsa.readLocalDate(\"$columnName\")")
            is LongFieldType -> appendLine("        val $fieldName = rsa.readLong(\"$columnName\")")
            is LongTypeFieldType -> {
                addImportFor(fieldType.fqcn)
                appendLine("        val $fieldName = rsa.readLong(\"$columnName\") { ${fieldType.uqcn}(it) }")
            }
            is MapFieldType -> {
                addImportFor(Fqcns.JACKSON_TYPE_REFERENCE)
                val nullableSuffix = if (col.nullable) "OrNull" else ""
                appendLine("        val $fieldName = rsa.readString$nullableSuffix(\"$columnName\") { jsonMapper.readValue(it, object : TypeReference<${col.classFieldDef.unqualifiedToString}>() {}) }")
            }
            is ObjectIdFieldType -> appendLine("        val $fieldName = rsa.readObjectId(\"$columnName\")")
            is PeriodFieldType -> appendLine("        val $fieldName = rsa.readPeriod(\"$columnName\")")
            is RequestDtoFieldType -> TODO()
            is SetFieldType -> TODO()
            is SimpleResponseDtoFieldType -> TODO()
            is StringFieldType -> appendLine("        val $fieldName = rsa.readString(\"$columnName\")")
            is StringTypeFieldType -> {
                addImportFor(fieldType.fqcn)
                appendLine("        val $fieldName = rsa.readString(\"$columnName\") { ${fieldType.uqcn}(it) }")
            }
            is StringValueClassFieldType -> {
                addImportFor(fieldType.fqcn)
                appendLine("        val $fieldName = rsa.readString(\"$columnName\") { ${fieldType.uqcn}(it) }")
            }
            is UrlFieldType -> appendLine("        val $fieldName = rsa.readUrl(\"$columnName\")")
        }

    }


}
