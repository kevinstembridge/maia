package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityFieldDef
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.RowMapperFieldDef
import org.maiaframework.gen.spec.definition.lang.BooleanFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanTypeFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.ClassDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.DataClassFieldType
import org.maiaframework.gen.spec.definition.lang.DomainIdFieldType
import org.maiaframework.gen.spec.definition.lang.DoubleFieldType
import org.maiaframework.gen.spec.definition.lang.EnumFieldType
import org.maiaframework.gen.spec.definition.lang.EsDocFieldType
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.ForeignKeyFieldType
import org.maiaframework.gen.spec.definition.lang.FqcnFieldType
import org.maiaframework.gen.spec.definition.lang.IdAndNameFieldType
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
import org.maiaframework.gen.spec.definition.lang.RequestDtoFieldType
import org.maiaframework.gen.spec.definition.lang.SetFieldType
import org.maiaframework.gen.spec.definition.lang.SimpleResponseDtoFieldType
import org.maiaframework.gen.spec.definition.lang.StringFieldType
import org.maiaframework.gen.spec.definition.lang.StringTypeFieldType
import org.maiaframework.gen.spec.definition.lang.StringValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.Uqcn
import org.maiaframework.gen.spec.definition.lang.UrlFieldType

class RowMapperRenderer(
    private val rowClassUqcn: Uqcn,
    private val rowMapperFieldDefs: List<RowMapperFieldDef>,
    rowMapperClassDef: ClassDef,
    private val isForEditDto: Boolean = false
): AbstractKotlinRenderer(
    rowMapperClassDef
) {


    init {

        if (rowMapperFieldDefs.any { it.entityFieldDef.classFieldDef.isMap }) {
            addConstructorArg(ClassFieldDef.aClassField("objectMapper", Fqcns.JACKSON_OBJECT_MAPPER).privat().build())
        }

    }


    override fun renderFunctions() {

        addImportFor(Fqcns.MAIA_RESULT_SET_ADAPTER)

        blankLine()
        blankLine()
        appendLine("    override fun mapRow(rsa: ResultSetAdapter): $rowClassUqcn {")
        blankLine()
        appendLine("        return $rowClassUqcn(")

        rowMapperFieldDefs.forEach { rowMapperFieldDef ->

            val entityFieldDef = rowMapperFieldDef.entityFieldDef
            val foreignKeyFieldDef = entityFieldDef.foreignKeyFieldDef

            if (foreignKeyFieldDef == null || isForEditDto == false) {

                renderRowMapperField(rowMapperFieldDef)

            } else {

                val idAndNameDef = foreignKeyFieldDef.foreignEntityDef.entityIdAndNameDef
                val idEntityFieldDef = idAndNameDef.idEntityFieldDef

                val idResultSetFieldName = "${foreignKeyFieldDef.foreignKeyFieldName}Id"
                val nameResultSetFieldName = "${foreignKeyFieldDef.foreignKeyFieldName}Name"

                addImportFor(idAndNameDef.dtoDef.fqcn)

                appendLine("            ${idAndNameDef.dtoUqcn}(")
                renderRowMapperField(idEntityFieldDef, idResultSetFieldName, nullable = false, indentSize = 16)
                renderRowMapperField(idAndNameDef.nameEntityFieldDef, nameResultSetFieldName, nullable = false, indentSize = 16, orElseText = "(blank)")
                appendLine("            ),")

            }

        }

        appendLine("        )")
        blankLine()
        appendLine("    }")

    }


    private fun renderRowMapperField(
        rowMapperFieldDef: RowMapperFieldDef,
        indentSize: Int = 12,
        orElseText: String = ""
    ) {

        renderRowMapperField(
            rowMapperFieldDef.entityFieldDef,
            rowMapperFieldDef.resultSetFieldName,
            rowMapperFieldDef.nullability.nullable,
            indentSize,
            orElseText
        )

    }


    private fun renderRowMapperField(
        entityFieldDef: EntityFieldDef,
        resultSetFieldName: String?,
        nullable: Boolean,
        indentSize: Int = 12,
        orElseText: String = ""
    ) {

        val fieldType = entityFieldDef.fieldType

        val indentStr = "".padEnd(indentSize, ' ')

        val rsaGetterFunctionName = entityFieldDef.resultSetAdapterReadFunctionName(nullable)

        val resultSetColumnName = resultSetFieldName ?: entityFieldDef.tableColumnName

        val orElseClause = if (orElseText.isNotBlank()) " ?: \"$orElseText\"" else ""

        when (fieldType) {
            is BooleanFieldType -> renderForPlainFieldType(indentStr, rsaGetterFunctionName, resultSetColumnName, orElseClause)
            is BooleanTypeFieldType -> renderForValueWrapper(fieldType, indentStr, rsaGetterFunctionName, resultSetColumnName)
            is BooleanValueClassFieldType -> renderForValueWrapper(fieldType, indentStr, rsaGetterFunctionName, resultSetColumnName)
            is DataClassFieldType -> TODO()
            is DomainIdFieldType -> renderForPlainFieldType(indentStr, rsaGetterFunctionName, resultSetColumnName, orElseClause)
            is DoubleFieldType -> renderForPlainFieldType(indentStr, rsaGetterFunctionName, resultSetColumnName, orElseClause)
            is EnumFieldType -> renderForEnum(entityFieldDef, indentStr, rsaGetterFunctionName, resultSetColumnName)
            is EsDocFieldType -> TODO()
            is ForeignKeyFieldType -> renderForPlainFieldType(indentStr, rsaGetterFunctionName, resultSetColumnName, orElseClause)
            is FqcnFieldType -> TODO()
            is IdAndNameFieldType -> TODO()
            is InstantFieldType -> renderForPlainFieldType(indentStr, rsaGetterFunctionName, resultSetColumnName, orElseClause)
            is IntFieldType -> renderForPlainFieldType(indentStr, rsaGetterFunctionName, resultSetColumnName, orElseClause)
            is IntTypeFieldType -> renderForValueWrapper(fieldType, indentStr, rsaGetterFunctionName, resultSetColumnName)
            is IntValueClassFieldType -> renderForValueWrapper(fieldType, indentStr, rsaGetterFunctionName, resultSetColumnName)
            is ListFieldType -> renderForListField(entityFieldDef, fieldType, indentStr, resultSetColumnName)
            is LocalDateFieldType -> renderForPlainFieldType(indentStr, rsaGetterFunctionName, resultSetColumnName, orElseClause)
            is LongFieldType -> renderForPlainFieldType(indentStr, rsaGetterFunctionName, resultSetColumnName, orElseClause)
            is LongTypeFieldType -> renderForValueWrapper(fieldType, indentStr, rsaGetterFunctionName, resultSetColumnName)
            is MapFieldType -> renderForListSetOrMap(entityFieldDef, indentStr, resultSetColumnName)
            is ObjectIdFieldType -> renderForPlainFieldType(indentStr, rsaGetterFunctionName, resultSetColumnName, orElseClause)
            is PeriodFieldType -> renderForPlainFieldType(indentStr, rsaGetterFunctionName, resultSetColumnName, orElseClause)
            is RequestDtoFieldType -> TODO()
            is SetFieldType -> renderForListSetOrMap(entityFieldDef, indentStr, resultSetColumnName)
            is SimpleResponseDtoFieldType -> renderForDto(entityFieldDef, indentStr)
            is StringFieldType -> renderForPlainFieldType(indentStr, rsaGetterFunctionName, resultSetColumnName, orElseClause)
            is StringTypeFieldType -> renderForValueWrapper(fieldType, indentStr, rsaGetterFunctionName, resultSetColumnName)
            is StringValueClassFieldType -> renderForValueWrapper(fieldType, indentStr, rsaGetterFunctionName, resultSetColumnName)
            is UrlFieldType -> renderForValueWrapper(fieldType, indentStr, rsaGetterFunctionName, resultSetColumnName)
        }

    }


    private fun renderForPlainFieldType(indentStr: String, rsaGetterFunctionName: String, resultSetColumnName: Any, orElseClause: String) {

        appendLine("${indentStr}rsa.$rsaGetterFunctionName(\"${resultSetColumnName}\")$orElseClause,")

    }


    private fun renderForEnum(
        entityFieldDef: EntityFieldDef,
        indentStr: String,
        rsaGetterFunctionName: String,
        resultSetColumnName: Any
    ) {

        addImportFor(entityFieldDef.fieldType)

        appendLine("${indentStr}rsa.${rsaGetterFunctionName}(\"${resultSetColumnName}\", ${entityFieldDef.fieldType.fqcn.uqcn}::class.java),")

    }


    private fun renderForListField(
        entityFieldDef: EntityFieldDef,
        fieldType: ListFieldType,
        indentStr: String,
        resultSetColumnName: Any
    ) {

        addImportFor(fieldType)

        val listElementFieldType = fieldType.parameterFieldType

        when (listElementFieldType) {
            is BooleanFieldType -> TODO()
            is BooleanTypeFieldType -> TODO()
            is BooleanValueClassFieldType -> TODO()
            is DataClassFieldType -> TODO()
            is DomainIdFieldType -> TODO()
            is DoubleFieldType -> TODO()
            is EnumFieldType -> appendLine("${indentStr}rsa.readListOfStrings(\"${resultSetColumnName}\") { ${listElementFieldType.fqcn.uqcn}.valueOf(it) },")
            is EsDocFieldType -> TODO()
            is ForeignKeyFieldType -> TODO()
            is FqcnFieldType -> TODO()
            is IdAndNameFieldType -> TODO()
            is InstantFieldType -> appendLine("${indentStr}rsa.readListOfInstants(\"${resultSetColumnName}\"),")
            is IntFieldType -> TODO()
            is IntTypeFieldType -> TODO()
            is IntValueClassFieldType -> TODO()
            is ListFieldType -> TODO()
            is LocalDateFieldType -> appendLine("${indentStr}rsa.readListOfLocalDates(\"${resultSetColumnName}\"),")
            is LongFieldType -> TODO()
            is LongTypeFieldType -> TODO()
            is MapFieldType -> appendLine("${indentStr}rsa.readString(\"$resultSetColumnName\") { objectMapper.readValue(it, object : TypeReference<${entityFieldDef.classFieldDef.unqualifiedToString}>() {}) },")
            is ObjectIdFieldType -> TODO()
            is PeriodFieldType -> appendLine("${indentStr}rsa.readListOfStrings(\"${resultSetColumnName}\") { Period.parse(it) },")
            is RequestDtoFieldType -> TODO()
            is SetFieldType -> TODO()
            is SimpleResponseDtoFieldType -> TODO()
            is StringFieldType -> appendLine("${indentStr}rsa.readListOfStrings(\"${resultSetColumnName}\"),")
            is StringTypeFieldType -> appendLine("${indentStr}rsa.readListOfStrings(\"${resultSetColumnName}\") { ${listElementFieldType.fqcn.uqcn}(it) },")
            is StringValueClassFieldType -> TODO()
            is UrlFieldType -> TODO()
        }

    }


    private fun renderForListSetOrMap(
        entityFieldDef: EntityFieldDef,
        indentStr: String,
        resultSetColumnName: Any
    ) {

        addImportFor(Fqcns.JACKSON_TYPE_REFERENCE)
        addImportFor(entityFieldDef.fieldType)

        appendLine("${indentStr}rsa.readString(\"$resultSetColumnName\") { objectMapper.readValue(it, object : TypeReference<${entityFieldDef.classFieldDef.unqualifiedToString}>() {}) },")

    }


    private fun renderForValueWrapper(
        fieldType: FieldType,
        indentStr: String,
        rsaGetterFunctionName: String,
        resultSetColumnName: Any
    ) {

        addImportFor(fieldType)

        appendLine("${indentStr}rsa.${rsaGetterFunctionName}(\"${resultSetColumnName}\") { ${fieldType.uqcn}(it) },")

    }


    private fun renderForDto(
        entityFieldDef: EntityFieldDef,
        indentStr: String
    ) {

        addImportFor(entityFieldDef.fieldType)

        val nullableSuffix = if (entityFieldDef.nullability.nullable) "OrNull" else ""

        appendLine("${indentStr}rsa.readString$nullableSuffix(\"${entityFieldDef.classFieldName}\") { objectMapper.readValue(it, ${entityFieldDef.classFieldDef.fqcn.uqcn}::class.java) },")

    }


}
