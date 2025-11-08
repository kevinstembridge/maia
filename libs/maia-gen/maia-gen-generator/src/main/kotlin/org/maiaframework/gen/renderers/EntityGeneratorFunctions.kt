package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityFieldDef
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
import java.time.Period


fun renderWriteConversionForImplicitField(
    entityFieldDef: EntityFieldDef,
    indent: String = "    ",
    fieldName: String = entityFieldDef.classFieldDef.classFieldName.value,
    requiresCast: Boolean = false,
    renderer: AbstractKotlinRenderer
) {

    val fieldType = entityFieldDef.classFieldDef.fieldType
    val fullCastPrefix = if (requiresCast) "($fieldName as ${fieldType.unqualifiedToString})" else fieldName

    when (fieldType) {
        is BooleanFieldType -> renderer.appendLine("${indent}return $fieldName")
        is BooleanTypeFieldType -> `render for value field wrapper`(fieldType, renderer, indent)
        is BooleanValueClassFieldType -> renderer.appendLine("${indent}return $fieldName")
        is DataClassFieldType -> TODO("YAGNI?")
        is DomainIdFieldType -> renderer.appendLine("${indent}return (inputValue as DomainId).value")
        is DoubleFieldType -> TODO("YAGNI?")
        is EnumFieldType -> renderer.appendLine("${indent}return $fullCastPrefix.name")
        is EsDocFieldType -> TODO("YAGNI?")
        is ForeignKeyFieldType -> renderer.appendLine("${indent}return (inputValue as DomainId).value")
        is FqcnFieldType -> TODO("YAGNI?")
        is IdAndNameFieldType -> TODO("YAGNI?")
        is InstantFieldType -> renderer.appendLine("${indent}return $fieldName")
        is IntFieldType -> renderer.appendLine("${indent}return $fieldName")
        is IntTypeFieldType -> `render for value field wrapper`(fieldType, renderer, indent)
        is IntValueClassFieldType -> renderer.appendLine("${indent}return $fieldName")
        is ListFieldType -> `render for List field type`(fieldType, renderer, indent, fullCastPrefix, fieldName)
        is LocalDateFieldType -> renderer.appendLine("${indent}return $fieldName")
        is LongFieldType -> renderer.appendLine("${indent}return $fieldName")
        is LongTypeFieldType -> `render for value field wrapper`(fieldType, renderer, indent)
        is MapFieldType -> `render write for plain field`(renderer, indent, fieldName)
        is ObjectIdFieldType -> TODO("YAGNI?")
        is PeriodFieldType -> renderer.appendLine("${indent}return $fieldName.toString()")
        is RequestDtoFieldType -> TODO("YAGNI?")
        is SetFieldType -> TODO("YAGNI?")
        is SimpleResponseDtoFieldType -> renderer.appendLine("${indent}return inputValue")
        is StringFieldType -> renderer.appendLine("${indent}return $fieldName")
        is StringTypeFieldType -> `render for value field wrapper`(fieldType, renderer, indent)
        is StringValueClassFieldType -> `render for value field wrapper`(fieldType, renderer, indent)
        is UrlFieldType -> `render write for plain field`(renderer, indent, fieldName)
    }

}


private fun `render write for plain field`(renderer: AbstractKotlinRenderer, indent: String, fieldName: String) {
    renderer.appendLine("${indent}return $fieldName")
}


private fun `render for value field wrapper`(fieldType: FieldType, renderer: AbstractKotlinRenderer, indent: String) {

    renderer.appendLine("${indent}return (inputValue as ${fieldType.uqcn}).value")

}


private fun `render for List field type`(
    fieldType: ListFieldType,
    renderer: AbstractKotlinRenderer,
    indent: String,
    fullCastPrefix: String,
    fieldName: String
) {

    val listElementType = fieldType.parameterFieldType

    when (listElementType) {
        is BooleanFieldType -> renderer.appendLine("${indent}return inputValue")
        is BooleanTypeFieldType -> renderer.appendLine("${indent}return inputValue")
        is BooleanValueClassFieldType -> renderer.appendLine("${indent}return inputValue")
        is DataClassFieldType -> renderer.appendLine("${indent}return inputValue")
        is DomainIdFieldType -> renderer.appendLine("${indent}return inputValue")
        is DoubleFieldType -> renderer.appendLine("${indent}return inputValue")
        is EnumFieldType -> `render for List of Enums`(renderer, indent, fullCastPrefix, listElementType)
        is EsDocFieldType -> TODO("YAGNI?")
        is ForeignKeyFieldType -> renderer.appendLine("${indent}return inputValue")
        is FqcnFieldType -> renderer.appendLine("${indent}return inputValue")
        is IdAndNameFieldType -> TODO("YAGNI?")
        is InstantFieldType -> renderer.appendLine("${indent}return inputValue")
        is IntFieldType -> renderer.appendLine("${indent}return inputValue")
        is IntTypeFieldType -> renderer.appendLine("${indent}return inputValue")
        is IntValueClassFieldType -> renderer.appendLine("${indent}return inputValue")
        is ListFieldType -> renderer.appendLine("${indent}return inputValue")
        is LocalDateFieldType -> renderer.appendLine("${indent}return inputValue")
        is LongFieldType -> renderer.appendLine("${indent}return inputValue")
        is LongTypeFieldType -> renderer.appendLine("${indent}return inputValue")
        is MapFieldType -> renderer.appendLine("${indent}return inputValue")
        is ObjectIdFieldType -> renderer.appendLine("${indent}return inputValue")
        is PeriodFieldType -> `render for List of Periods`(renderer, indent, fullCastPrefix, listElementType)
        is RequestDtoFieldType -> TODO("YAGNI?")
        is SetFieldType -> renderer.appendLine("${indent}return inputValue")
        is SimpleResponseDtoFieldType -> TODO("YAGNI?")
        is StringFieldType -> renderer.appendLine("${indent}return inputValue")
        is StringTypeFieldType -> renderer.appendLine("${indent}return $fullCastPrefix.map { it.value }")
        is StringValueClassFieldType -> renderer.appendLine("${indent}return inputValue")
        is UrlFieldType -> renderer.appendLine("${indent}return inputValue")
    }

}

private fun `render for List of Enums`(
    renderer: AbstractKotlinRenderer,
    indent: String,
    fullCastPrefix: String,
    listElementType: EnumFieldType
) {

    renderer.appendLine("$indent{")
    renderer.appendLine("$indent    if (inputValue is List<*>) {")
    renderer.appendLine("$indent        return ${fullCastPrefix}.map { it.name }")
    renderer.appendLine("$indent    } else {")
    renderer.appendLine("$indent        return (inputValue as ${listElementType.unqualifiedToString}).name")
    renderer.appendLine("$indent    }")
    renderer.appendLine("$indent}")
}


private fun `render for List of Periods`(
    renderer: AbstractKotlinRenderer,
    indent: String,
    fullCastPrefix: String,
    listElementType: FieldType
) {

    renderer.addImportFor(Period::class.java)

    renderer.appendLine("$indent{")
    renderer.appendLine("$indent    if (inputValue is List<*>) {")
    renderer.appendLine("$indent        return ${fullCastPrefix}.map { it.toString() }")
    renderer.appendLine("$indent    } else {")
    renderer.appendLine("$indent        return (inputValue as ${listElementType.unqualifiedToString}).toString()")
    renderer.appendLine("$indent    }")
    renderer.appendLine("$indent}")

}

