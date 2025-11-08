package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityFieldDef
import org.maiaframework.gen.spec.definition.lang.BooleanFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanTypeFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
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
import org.maiaframework.gen.spec.definition.lang.UrlFieldType
import org.maiaframework.jdbc.JdbcCompatibleType

object SqlParamFunctions {


    fun sqlParamAddFunctionName(fieldType: FieldType): String {

        return when (fieldType) {
            is BooleanFieldType -> "addValue"
            is BooleanTypeFieldType -> "addValue"
            is BooleanValueClassFieldType -> "addValue"
            is DataClassFieldType -> "addValue"
            is DomainIdFieldType -> "addValue"
            is DoubleFieldType -> "addValue"
            is EnumFieldType -> "addValue"
            is EsDocFieldType -> "addValue"
            is ForeignKeyFieldType -> "addValue"
            is FqcnFieldType -> "addValue"
            is IdAndNameFieldType -> "addValue"
            is InstantFieldType -> "addValue"
            is IntFieldType -> "addValue"
            is IntTypeFieldType -> "addValue"
            is IntValueClassFieldType -> "addValue"
            is ListFieldType -> sqlParamAddFunctionForListField(fieldType)
            is LocalDateFieldType -> "addValue"
            is LongFieldType -> "addValue"
            is LongTypeFieldType -> "addValue"
            is MapFieldType -> "addJsonValue"
            is ObjectIdFieldType -> "addValue"
            is PeriodFieldType -> "addValue"
            is RequestDtoFieldType -> "addValue"
            is SetFieldType -> "addValue"
            is SimpleResponseDtoFieldType -> "addValue"
            is StringFieldType -> "addValue"
            is StringTypeFieldType -> "addValue"
            is StringValueClassFieldType -> "addValue"
            is UrlFieldType -> "addValue"
        }

    }


    private fun sqlParamAddFunctionForListField(listFieldType: ListFieldType): String {

        return when (listFieldType.parameterFieldType) {
            is BooleanFieldType -> "addValue"
            is BooleanTypeFieldType -> TODO()
            is BooleanValueClassFieldType -> TODO()
            is DataClassFieldType -> TODO()
            is DomainIdFieldType -> TODO()
            is DoubleFieldType -> TODO()
            is EnumFieldType -> "addListOfStrings"
            is EsDocFieldType -> TODO()
            is ForeignKeyFieldType -> TODO()
            is FqcnFieldType -> TODO()
            is IdAndNameFieldType -> TODO()
            is InstantFieldType -> "addListOfInstants"
            is IntFieldType -> TODO()
            is IntTypeFieldType -> TODO()
            is IntValueClassFieldType -> TODO()
            is ListFieldType -> TODO()
            is LocalDateFieldType -> TODO()
            is LongFieldType -> TODO()
            is LongTypeFieldType -> TODO()
            is MapFieldType -> TODO()
            is ObjectIdFieldType -> TODO()
            is PeriodFieldType -> "addListOfStrings"
            is RequestDtoFieldType -> TODO()
            is SetFieldType -> TODO()
            is SimpleResponseDtoFieldType -> TODO()
            is StringFieldType -> "addListOfStrings"
            is StringTypeFieldType -> "addListOfStrings"
            is StringValueClassFieldType -> TODO()
            is UrlFieldType -> TODO()
        }

    }


    fun sqlParamMapperFunction(fieldType: FieldType): String {

        return when (fieldType) {
            is BooleanFieldType -> ""
            is BooleanTypeFieldType -> ""
            is BooleanValueClassFieldType -> ""
            is DataClassFieldType -> ""
            is DomainIdFieldType -> ""
            is DoubleFieldType -> ""
            is EnumFieldType -> ""
            is EsDocFieldType -> ""
            is ForeignKeyFieldType -> ""
            is FqcnFieldType -> ""
            is IdAndNameFieldType -> ""
            is InstantFieldType -> ""
            is IntFieldType -> ""
            is IntTypeFieldType -> ""
            is IntValueClassFieldType -> ""
            is ListFieldType -> sqlParamMapperFunctionForListField(fieldType.parameterFieldType)
            is LocalDateFieldType -> ""
            is LongFieldType -> ""
            is LongTypeFieldType -> ""
            is MapFieldType -> ""
            is ObjectIdFieldType -> ""
            is PeriodFieldType -> ""
            is RequestDtoFieldType -> ""
            is SetFieldType -> ""
            is SimpleResponseDtoFieldType -> ""
            is StringFieldType -> ""
            is StringTypeFieldType -> ""
            is StringValueClassFieldType -> ""
            is UrlFieldType -> ""
        }

    }


    private fun sqlParamMapperFunctionForListField(listElementType: FieldType): String {

        return when (listElementType) {
            is BooleanFieldType -> ""
            is BooleanTypeFieldType -> ""
            is BooleanValueClassFieldType -> ""
            is DataClassFieldType -> ""
            is DomainIdFieldType -> ""
            is DoubleFieldType -> ""
            is EnumFieldType -> " { it.name }"
            is EsDocFieldType -> ""
            is ForeignKeyFieldType -> ""
            is FqcnFieldType -> ""
            is IdAndNameFieldType -> ""
            is InstantFieldType -> ""
            is IntFieldType -> ""
            is IntTypeFieldType -> ""
            is IntValueClassFieldType -> ""
            is ListFieldType -> ""
            is LocalDateFieldType -> ""
            is LongFieldType -> ""
            is LongTypeFieldType -> ""
            is MapFieldType -> ""
            is ObjectIdFieldType -> ""
            is PeriodFieldType -> ""
            is RequestDtoFieldType -> ""
            is SetFieldType -> ""
            is SimpleResponseDtoFieldType -> ""
            is StringFieldType -> ""
            is StringTypeFieldType -> ""
            is StringValueClassFieldType -> ""
            is UrlFieldType -> ""
        }

    }

    fun renderSqlParamAddValueFor(
        entityFieldDef: EntityFieldDef,
        indent: String,
        entityParameterName: String?,
        indentSize: Int,
        lineAppender: (String) -> Unit
    ) {
        val fieldType = entityFieldDef.classFieldDef.fieldType
        val fieldName = entityFieldDef.classFieldName

        val entityNamePrefix = if (entityParameterName != null) "$entityParameterName." else ""

        if (fieldType is ListFieldType) {

            when (fieldType.parameterFieldType) {
                is BooleanFieldType -> TODO("YAGNI")
                is BooleanTypeFieldType -> TODO("YAGNI")
                is BooleanValueClassFieldType -> TODO("YAGNI")
                is DataClassFieldType -> renderAddJsonValue(entityFieldDef, lineAppender, indent, fieldName, entityNamePrefix)
                is DomainIdFieldType -> TODO("YAGNI")
                is DoubleFieldType -> TODO("YAGNI")
                is EnumFieldType -> lineAppender("$indent    addListOfStrings(\"$fieldName\", $entityNamePrefix$fieldName.map { it.name })")
                is EsDocFieldType -> TODO("YAGNI")
                is ForeignKeyFieldType -> TODO("YAGNI")
                is FqcnFieldType -> TODO("YAGNI")
                is IdAndNameFieldType -> TODO("YAGNI")
                is InstantFieldType -> lineAppender("$indent    addListOfInstants(\"$fieldName\", $entityNamePrefix$fieldName)")
                is IntFieldType -> TODO("YAGNI")
                is IntTypeFieldType -> TODO("YAGNI")
                is IntValueClassFieldType -> TODO("YAGNI")
                is ListFieldType -> TODO("YAGNI")
                is LocalDateFieldType -> lineAppender("$indent    addListOfLocalDates(\"$fieldName\", $entityNamePrefix$fieldName)")
                is LongFieldType -> TODO("YAGNI")
                is LongTypeFieldType -> TODO("YAGNI")
                is MapFieldType -> TODO("YAGNI")
                is ObjectIdFieldType -> TODO("YAGNI")
                is PeriodFieldType -> lineAppender("$indent    addListOfStrings(\"$fieldName\", $entityNamePrefix$fieldName.map { it.toString() })")
                is RequestDtoFieldType -> TODO("YAGNI")
                is SetFieldType -> TODO("YAGNI")
                is SimpleResponseDtoFieldType -> TODO("YAGNI")
                is StringFieldType -> lineAppender("$indent    addListOfStrings(\"$fieldName\", $entityNamePrefix$fieldName)")
                is StringTypeFieldType -> lineAppender("$indent    addListOfStrings(\"$fieldName\", $entityNamePrefix$fieldName.map { it.value })")
                is StringValueClassFieldType -> TODO("YAGNI")
                is UrlFieldType -> TODO("YAGNI")
            }

        } else if (entityFieldDef.classFieldDef.fieldType.jdbcCompatibleType == JdbcCompatibleType.jsonb) {

            renderAddJsonValue(entityFieldDef, lineAppender, indent, fieldName, entityNamePrefix)

        } else if (entityFieldDef.classFieldDef.isMap) {

            renderSqlParamForCollectionField(entityNamePrefix, indentSize, entityFieldDef, lineAppender)

        } else if (entityFieldDef.classFieldDef.isValueClass) {

            val fieldName = entityFieldDef.classFieldName
            val nullSafeOperator = if (entityFieldDef.nullable) "?" else ""
            lineAppender("$indent    addValue(\"$fieldName\", $entityNamePrefix$fieldName$nullSafeOperator.value)")

        } else {

            val fieldName = entityFieldDef.classFieldName
            lineAppender("$indent    addValue(\"$fieldName\", $entityNamePrefix$fieldName)")

        }

    }


    private fun renderAddJsonValue(
        entityFieldDef: EntityFieldDef,
        lineAppender: (String) -> Unit,
        indent: String,
        fieldName: ClassFieldName,
        entityNamePrefix: String
    ) {

        if (entityFieldDef.nullable) {

            lineAppender("$indent    addJsonValue(\"$fieldName\", $entityNamePrefix$fieldName?.let { objectMapper.writeValueAsString(it) })")

        } else {

            lineAppender("$indent    addJsonValue(\"$fieldName\", objectMapper.writeValueAsString($entityNamePrefix$fieldName))")

        }

    }


    private fun renderSqlParamForCollectionField(
        entityNamePrefix: String,
        indentSize: Int,
        entityFieldDef: EntityFieldDef,
        lineAppender: (String) -> Unit
    ) {

        val indent = "".padEnd(indentSize, ' ')
        val fieldName = entityFieldDef.classFieldName

        if (entityFieldDef.classFieldDef.isEnumList) {

            lineAppender("$indent    addListOfStrings(\"$fieldName\", $entityNamePrefix$fieldName.map { it.name })")

        } else if (entityFieldDef.classFieldDef.isMap) {

            lineAppender("$indent    addJsonValue(\"$fieldName\", jsonFacade.writeValueAsString($entityNamePrefix$fieldName))")

        } else {

            lineAppender("$indent    addJsonValue(\"$fieldName\", jsonFacade.writeValueAsString($entityNamePrefix$fieldName.map { it.toString() }))")

        }


    }



}
