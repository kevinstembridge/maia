package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.BooleanFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanTypeFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.DataClassFieldType
import org.maiaframework.gen.spec.definition.lang.DomainIdFieldType
import org.maiaframework.gen.spec.definition.lang.DoubleFieldType
import org.maiaframework.gen.spec.definition.lang.EnumFieldType
import org.maiaframework.gen.spec.definition.lang.EsDocFieldType
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.ForeignKeyFieldType
import org.maiaframework.gen.spec.definition.lang.Fqcn
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
import org.maiaframework.gen.spec.definition.lang.UrlFieldType

object RowMapperFunctions {


    fun renderRowMapperField(
        rowMapperFieldDef: RowMapperFieldDef,
        indentSize: Int = 12,
        orElseText: String = "",
        fqcnImporter: (Fqcn) -> Unit,
        lineAppender: (String) -> Unit
    ) {

        renderRowMapperField(
            rowMapperFieldDef.entityFieldDef,
            rowMapperFieldDef.resultSetFieldName,
            rowMapperFieldDef.nullability.nullable,
            indentSize,
            orElseText,
            fqcnImporter,
            lineAppender
        )

    }


    fun renderRowMapperField(
        entityFieldDef: EntityFieldDef,
        resultSetFieldName: String?,
        nullable: Boolean,
        indentSize: Int = 12,
        orElseText: String = "",
        fqcnImporter: (Fqcn) -> Unit,
        lineAppender: (String) -> Unit
    ) {

        val classFieldDef = entityFieldDef.classFieldDef

        val fieldType = classFieldDef.fieldType

        val indentStr = "".padEnd(indentSize, ' ')

        val rsaGetterFunctionName = classFieldDef.resultSetAdapterReadFunctionName(nullable)

        val resultSetColumnName = resultSetFieldName ?: entityFieldDef.tableColumnName

        val orElseClause = if (orElseText.isNotBlank()) " ?: \"$orElseText\"" else ""

        when (fieldType) {
            is BooleanFieldType -> renderForPlainFieldType(indentStr, rsaGetterFunctionName, resultSetColumnName, orElseClause, lineAppender)
            is BooleanTypeFieldType -> renderForValueWrapper(fieldType, indentStr, rsaGetterFunctionName, resultSetColumnName, fqcnImporter, lineAppender)
            is BooleanValueClassFieldType -> renderForValueWrapper(fieldType, indentStr, rsaGetterFunctionName, resultSetColumnName, fqcnImporter, lineAppender)
            is DataClassFieldType -> TODO()
            is DomainIdFieldType -> renderForPlainFieldType(indentStr, rsaGetterFunctionName, resultSetColumnName, orElseClause, lineAppender)
            is DoubleFieldType -> renderForPlainFieldType(indentStr, rsaGetterFunctionName, resultSetColumnName, orElseClause, lineAppender)
            is EnumFieldType -> renderForEnum(entityFieldDef, indentStr, rsaGetterFunctionName, resultSetColumnName, fqcnImporter, lineAppender)
            is EsDocFieldType -> TODO()
            is ForeignKeyFieldType -> renderForPlainFieldType(indentStr, rsaGetterFunctionName, resultSetColumnName, orElseClause, lineAppender)
            is FqcnFieldType -> TODO()
            is IdAndNameFieldType -> TODO()
            is InstantFieldType -> renderForPlainFieldType(indentStr, rsaGetterFunctionName, resultSetColumnName, orElseClause, lineAppender)
            is IntFieldType -> renderForPlainFieldType(indentStr, rsaGetterFunctionName, resultSetColumnName, orElseClause, lineAppender)
            is IntTypeFieldType -> renderForValueWrapper(fieldType, indentStr, rsaGetterFunctionName, resultSetColumnName, fqcnImporter, lineAppender)
            is IntValueClassFieldType -> renderForValueWrapper(fieldType, indentStr, rsaGetterFunctionName, resultSetColumnName, fqcnImporter, lineAppender)
            is ListFieldType -> renderForListField(entityFieldDef, fieldType, indentStr, resultSetColumnName, fqcnImporter, lineAppender)
            is LocalDateFieldType -> renderForPlainFieldType(indentStr, rsaGetterFunctionName, resultSetColumnName, orElseClause, lineAppender)
            is LongFieldType -> renderForPlainFieldType(indentStr, rsaGetterFunctionName, resultSetColumnName, orElseClause, lineAppender)
            is LongTypeFieldType -> renderForValueWrapper(fieldType, indentStr, rsaGetterFunctionName, resultSetColumnName, fqcnImporter, lineAppender)
            is MapFieldType -> renderForListSetOrMap(entityFieldDef, indentStr, resultSetColumnName, fqcnImporter, lineAppender)
            is ObjectIdFieldType -> renderForPlainFieldType(indentStr, rsaGetterFunctionName, resultSetColumnName, orElseClause, lineAppender)
            is PeriodFieldType -> renderForPlainFieldType(indentStr, rsaGetterFunctionName, resultSetColumnName, orElseClause, lineAppender)
            is RequestDtoFieldType -> TODO()
            is SetFieldType -> renderForListSetOrMap(entityFieldDef, indentStr, resultSetColumnName, fqcnImporter, lineAppender)
            is SimpleResponseDtoFieldType -> renderForDto(entityFieldDef, indentStr, fqcnImporter, lineAppender)
            is StringFieldType -> renderForPlainFieldType(indentStr, rsaGetterFunctionName, resultSetColumnName, orElseClause, lineAppender)
            is StringTypeFieldType -> renderForValueWrapper(fieldType, indentStr, rsaGetterFunctionName, resultSetColumnName, fqcnImporter, lineAppender)
            is StringValueClassFieldType -> renderForValueWrapper(fieldType, indentStr, rsaGetterFunctionName, resultSetColumnName, fqcnImporter, lineAppender)
            is UrlFieldType -> renderForValueWrapper(fieldType, indentStr, rsaGetterFunctionName, resultSetColumnName, fqcnImporter, lineAppender)
        }

    }


    private fun renderForPlainFieldType(
        indentStr: String,
        rsaGetterFunctionName: String,
        resultSetColumnName: Any,
        orElseClause: String,
        lineAppender: (String) -> Unit
    ) {

        lineAppender.invoke("${indentStr}rsa.$rsaGetterFunctionName(\"${resultSetColumnName}\")$orElseClause,")

    }


    private fun renderForEnum(
        entityFieldDef: EntityFieldDef,
        indentStr: String,
        rsaGetterFunctionName: String,
        resultSetColumnName: Any,
        fqcnImporter: (Fqcn) -> Unit,
        lineAppender: (String) -> Unit
    ) {

        fqcnImporter.invoke(entityFieldDef.fieldType.fqcn)

        lineAppender.invoke("${indentStr}rsa.${rsaGetterFunctionName}(\"${resultSetColumnName}\", ${entityFieldDef.fieldType.fqcn.uqcn}::class.java),")

    }


    private fun renderForListField(
        entityFieldDef: EntityFieldDef,
        fieldType: ListFieldType,
        indentStr: String,
        resultSetColumnName: Any,
        fqcnImporter: (Fqcn) -> Unit,
        lineAppender: (String) -> Unit
    ) {

        fqcnImporter.invoke(fieldType.fqcn)

        val listElementFieldType = fieldType.parameterFieldType

        when (listElementFieldType) {
            is BooleanFieldType -> TODO()
            is BooleanTypeFieldType -> TODO()
            is BooleanValueClassFieldType -> TODO()
            is DataClassFieldType -> TODO()
            is DomainIdFieldType -> TODO()
            is DoubleFieldType -> TODO()
            is EnumFieldType -> lineAppender.invoke("${indentStr}rsa.readListOfStrings(\"${resultSetColumnName}\") { ${listElementFieldType.fqcn.uqcn}.valueOf(it) },")
            is EsDocFieldType -> TODO()
            is ForeignKeyFieldType -> TODO()
            is FqcnFieldType -> TODO()
            is IdAndNameFieldType -> TODO()
            is InstantFieldType -> lineAppender.invoke("${indentStr}rsa.readListOfInstants(\"${resultSetColumnName}\"),")
            is IntFieldType -> TODO()
            is IntTypeFieldType -> TODO()
            is IntValueClassFieldType -> TODO()
            is ListFieldType -> TODO()
            is LocalDateFieldType -> lineAppender.invoke("${indentStr}rsa.readListOfLocalDates(\"${resultSetColumnName}\"),")
            is LongFieldType -> TODO()
            is LongTypeFieldType -> TODO()
            is MapFieldType -> lineAppender.invoke("${indentStr}rsa.readString(\"$resultSetColumnName\") { objectMapper.readValue(it, object : TypeReference<${entityFieldDef.classFieldDef.unqualifiedToString}>() {}) },")
            is ObjectIdFieldType -> TODO()
            is PeriodFieldType -> lineAppender.invoke("${indentStr}rsa.readListOfStrings(\"${resultSetColumnName}\") { Period.parse(it) },")
            is RequestDtoFieldType -> TODO()
            is SetFieldType -> TODO()
            is SimpleResponseDtoFieldType -> TODO()
            is StringFieldType -> lineAppender.invoke("${indentStr}rsa.readListOfStrings(\"${resultSetColumnName}\"),")
            is StringTypeFieldType -> lineAppender.invoke("${indentStr}rsa.readListOfStrings(\"${resultSetColumnName}\") { ${listElementFieldType.fqcn.uqcn}(it) },")
            is StringValueClassFieldType -> TODO()
            is UrlFieldType -> TODO()
        }

    }


    private fun renderForListSetOrMap(
        entityFieldDef: EntityFieldDef,
        indentStr: String,
        resultSetColumnName: Any,
        fqcnImporter: (Fqcn) -> Unit,
        lineAppender: (String) -> Unit
    ) {

        fqcnImporter.invoke(Fqcns.JACKSON_TYPE_REFERENCE)
        fqcnImporter.invoke(entityFieldDef.fieldType.fqcn)

        lineAppender.invoke("${indentStr}rsa.readString(\"$resultSetColumnName\") { objectMapper.readValue(it, object : TypeReference<${entityFieldDef.classFieldDef.unqualifiedToString}>() {}) },")

    }


    private fun renderForValueWrapper(
        fieldType: FieldType,
        indentStr: String,
        rsaGetterFunctionName: String,
        resultSetColumnName: Any,
        fieldTypeImporter: (Fqcn) -> Unit,
        lineAppender: (String) -> Unit
    ) {

        fieldTypeImporter.invoke(fieldType.fqcn)

        lineAppender.invoke("${indentStr}rsa.${rsaGetterFunctionName}(\"${resultSetColumnName}\") { ${fieldType.uqcn}(it) },")

    }


    private fun renderForDto(
        entityFieldDef: EntityFieldDef,
        indentStr: String,
        fqcnImporter: (Fqcn) -> Unit,
        lineAppender: (String) -> Unit
    ) {

        fqcnImporter.invoke(entityFieldDef.fieldType.fqcn)

        val nullableSuffix = if (entityFieldDef.nullability.nullable) "OrNull" else ""

        lineAppender.invoke("${indentStr}rsa.readString$nullableSuffix(\"${entityFieldDef.classFieldName}\") { objectMapper.readValue(it, ${entityFieldDef.classFieldDef.fqcn.uqcn}::class.java) },")

    }



}
