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
    ): String {

        return renderRowMapperField(
            rowMapperFieldDef.entityFieldDef,
            rowMapperFieldDef.resultSetFieldName,
            rowMapperFieldDef.nullability.nullable,
            indentSize,
            orElseText,
            fqcnImporter,
        )

    }


    fun renderRowMapperField(
        entityFieldDef: EntityFieldDef,
        resultSetFieldName: String?,
        nullable: Boolean,
        indentSize: Int = 12,
        orElseText: String = "",
        fqcnImporter: (Fqcn) -> Unit,
    ): String {

        val classFieldDef = entityFieldDef.classFieldDef

        val fieldType = classFieldDef.fieldType

        val indentStr = "".padEnd(indentSize, ' ')

        val rsaGetterFunctionName = classFieldDef.resultSetAdapterReadFunctionName(nullable)

        val resultSetColumnName = resultSetFieldName ?: entityFieldDef.tableColumnName

        val orElseClause = if (orElseText.isNotBlank()) " ?: \"$orElseText\"" else ""

        return when (fieldType) {
            is BooleanFieldType -> renderForPlainFieldType(indentStr, rsaGetterFunctionName, resultSetColumnName, orElseClause)
            is BooleanTypeFieldType -> renderForValueWrapper(fieldType, indentStr, rsaGetterFunctionName, resultSetColumnName, fqcnImporter)
            is BooleanValueClassFieldType -> renderForValueWrapper(fieldType, indentStr, rsaGetterFunctionName, resultSetColumnName, fqcnImporter)
            is DataClassFieldType -> TODO()
            is DomainIdFieldType -> renderForPlainFieldType(indentStr, rsaGetterFunctionName, resultSetColumnName, orElseClause)
            is DoubleFieldType -> renderForPlainFieldType(indentStr, rsaGetterFunctionName, resultSetColumnName, orElseClause)
            is EnumFieldType -> renderForEnum(entityFieldDef, indentStr, rsaGetterFunctionName, resultSetColumnName, fqcnImporter)
            is EsDocFieldType -> TODO()
            is ForeignKeyFieldType -> renderForPlainFieldType(indentStr, rsaGetterFunctionName, resultSetColumnName, orElseClause)
            is FqcnFieldType -> TODO()
            is IdAndNameFieldType -> TODO()
            is InstantFieldType -> renderForPlainFieldType(indentStr, rsaGetterFunctionName, resultSetColumnName, orElseClause)
            is IntFieldType -> renderForPlainFieldType(indentStr, rsaGetterFunctionName, resultSetColumnName, orElseClause)
            is IntTypeFieldType -> renderForValueWrapper(fieldType, indentStr, rsaGetterFunctionName, resultSetColumnName, fqcnImporter)
            is IntValueClassFieldType -> renderForValueWrapper(fieldType, indentStr, rsaGetterFunctionName, resultSetColumnName, fqcnImporter)
            is ListFieldType -> renderForListField(entityFieldDef, fieldType, indentStr, resultSetColumnName, fqcnImporter)
            is LocalDateFieldType -> renderForPlainFieldType(indentStr, rsaGetterFunctionName, resultSetColumnName, orElseClause)
            is LongFieldType -> renderForPlainFieldType(indentStr, rsaGetterFunctionName, resultSetColumnName, orElseClause)
            is LongTypeFieldType -> renderForValueWrapper(fieldType, indentStr, rsaGetterFunctionName, resultSetColumnName, fqcnImporter)
            is MapFieldType -> renderForListSetOrMap(entityFieldDef, indentStr, resultSetColumnName, fqcnImporter)
            is ObjectIdFieldType -> renderForPlainFieldType(indentStr, rsaGetterFunctionName, resultSetColumnName, orElseClause)
            is PeriodFieldType -> renderForPlainFieldType(indentStr, rsaGetterFunctionName, resultSetColumnName, orElseClause)
            is RequestDtoFieldType -> TODO()
            is SetFieldType -> renderForListSetOrMap(entityFieldDef, indentStr, resultSetColumnName, fqcnImporter)
            is SimpleResponseDtoFieldType -> renderForDto(entityFieldDef, indentStr, fqcnImporter)
            is StringFieldType -> renderForPlainFieldType(indentStr, rsaGetterFunctionName, resultSetColumnName, orElseClause)
            is StringTypeFieldType -> renderForValueWrapper(fieldType, indentStr, rsaGetterFunctionName, resultSetColumnName, fqcnImporter)
            is StringValueClassFieldType -> renderForValueWrapper(fieldType, indentStr, rsaGetterFunctionName, resultSetColumnName, fqcnImporter)
            is UrlFieldType -> renderForValueWrapper(fieldType, indentStr, rsaGetterFunctionName, resultSetColumnName, fqcnImporter)
        }

    }


    private fun renderForPlainFieldType(
        indentStr: String,
        rsaGetterFunctionName: String,
        resultSetColumnName: Any,
        orElseClause: String,
    ): String {

        return "${indentStr}rsa.$rsaGetterFunctionName(\"${resultSetColumnName}\")$orElseClause"

    }


    private fun renderForEnum(
        entityFieldDef: EntityFieldDef,
        indentStr: String,
        rsaGetterFunctionName: String,
        resultSetColumnName: Any,
        fqcnImporter: (Fqcn) -> Unit,
    ): String {

        fqcnImporter.invoke(entityFieldDef.fieldType.fqcn)

        return "${indentStr}rsa.${rsaGetterFunctionName}(\"${resultSetColumnName}\", ${entityFieldDef.fieldType.fqcn.uqcn}::class.java)"

    }


    private fun renderForListField(
        entityFieldDef: EntityFieldDef,
        fieldType: ListFieldType,
        indentStr: String,
        resultSetColumnName: Any,
        fqcnImporter: (Fqcn) -> Unit,
    ): String {

        fqcnImporter.invoke(fieldType.fqcn)

        val listElementFieldType = fieldType.parameterFieldType

        fqcnImporter.invoke(listElementFieldType.fqcn)

        return when (listElementFieldType) {
            is BooleanFieldType -> TODO()
            is BooleanTypeFieldType -> TODO()
            is BooleanValueClassFieldType -> TODO()
            is DataClassFieldType -> TODO()
            is DomainIdFieldType -> TODO()
            is DoubleFieldType -> TODO()
            is EnumFieldType -> "${indentStr}rsa.readListOfStrings(\"${resultSetColumnName}\") { ${listElementFieldType.fqcn.uqcn}.valueOf(it) }"
            is EsDocFieldType -> TODO()
            is ForeignKeyFieldType -> TODO()
            is FqcnFieldType -> TODO()
            is IdAndNameFieldType -> TODO()
            is InstantFieldType -> "${indentStr}rsa.readListOfInstants(\"${resultSetColumnName}\")"
            is IntFieldType -> TODO()
            is IntTypeFieldType -> TODO()
            is IntValueClassFieldType -> TODO()
            is ListFieldType -> TODO()
            is LocalDateFieldType -> "${indentStr}rsa.readListOfLocalDates(\"${resultSetColumnName}\")"
            is LongFieldType -> TODO()
            is LongTypeFieldType -> TODO()
            is MapFieldType -> "${indentStr}rsa.readString(\"$resultSetColumnName\") { objectMapper.readValue(it, object : TypeReference<${entityFieldDef.classFieldDef.unqualifiedToString}>() {}) }"
            is ObjectIdFieldType -> TODO()
            is PeriodFieldType -> "${indentStr}rsa.readListOfStrings(\"${resultSetColumnName}\") { Period.parse(it) }"
            is RequestDtoFieldType -> TODO()
            is SetFieldType -> TODO()
            is SimpleResponseDtoFieldType -> TODO()
            is StringFieldType -> "${indentStr}rsa.readListOfStrings(\"${resultSetColumnName}\")"
            is StringTypeFieldType -> "${indentStr}rsa.readListOfStrings(\"${resultSetColumnName}\") { ${listElementFieldType.fqcn.uqcn}(it) }"
            is StringValueClassFieldType -> TODO()
            is UrlFieldType -> TODO()
        }

    }


    private fun renderForListSetOrMap(
        entityFieldDef: EntityFieldDef,
        indentStr: String,
        resultSetColumnName: Any,
        fqcnImporter: (Fqcn) -> Unit,
    ): String {

        fqcnImporter.invoke(Fqcns.JACKSON_TYPE_REFERENCE)
        fqcnImporter.invoke(entityFieldDef.fieldType.fqcn)

        return "${indentStr}rsa.readString(\"$resultSetColumnName\") { objectMapper.readValue(it, object : TypeReference<${entityFieldDef.classFieldDef.unqualifiedToString}>() {}) }"

    }


    private fun renderForValueWrapper(
        fieldType: FieldType,
        indentStr: String,
        rsaGetterFunctionName: String,
        resultSetColumnName: Any,
        fieldTypeImporter: (Fqcn) -> Unit,
    ): String {

        fieldTypeImporter.invoke(fieldType.fqcn)

        return "${indentStr}rsa.${rsaGetterFunctionName}(\"${resultSetColumnName}\") { ${fieldType.uqcn}(it) }"

    }


    private fun renderForDto(
        entityFieldDef: EntityFieldDef,
        indentStr: String,
        fqcnImporter: (Fqcn) -> Unit,
    ): String {

        fqcnImporter.invoke(entityFieldDef.fieldType.fqcn)

        val nullableSuffix = if (entityFieldDef.nullability.nullable) "OrNull" else ""

        return "${indentStr}rsa.readString$nullableSuffix(\"${entityFieldDef.classFieldName}\") { objectMapper.readValue(it, ${entityFieldDef.classFieldDef.fqcn.uqcn}::class.java) }"

    }



}
