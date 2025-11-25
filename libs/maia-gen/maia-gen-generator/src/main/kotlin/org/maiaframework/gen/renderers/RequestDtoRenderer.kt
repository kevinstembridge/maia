package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.RequestDtoDef
import org.maiaframework.gen.spec.definition.lang.AnnotationUsageSite
import org.maiaframework.gen.spec.definition.lang.BooleanFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanTypeFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.ConstructorArg
import org.maiaframework.gen.spec.definition.lang.DataClassFieldType
import org.maiaframework.gen.spec.definition.lang.DomainIdFieldType
import org.maiaframework.gen.spec.definition.lang.DoubleFieldType
import org.maiaframework.gen.spec.definition.lang.EnumFieldType
import org.maiaframework.gen.spec.definition.lang.EsDocFieldType
import org.maiaframework.gen.spec.definition.lang.FieldTypes.isValueFieldWrapper
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


class RequestDtoRenderer(private val requestDtoDef: RequestDtoDef) : AbstractKotlinRenderer(requestDtoDef.classDef) {


    init {

        this.requestDtoDef.classFieldDefs.forEach { classField ->
            addConstructorArg(ConstructorArg(classField, classField.validationAnnotations))
            addImportFor(classField.fieldType)
        }

    }


    override fun renderConstructorArgs(args: List<ConstructorArg>) {

        val argCount = args.size

        args.forEachIndexed { index, constructorArg ->

            val commaOrNot = if (index + 1 == argCount) "" else ","

            val classField = constructorArg.classFieldDef

            val fieldIsNotNullable = classField.nullable == false

            val fieldRequiresJsonPropertyAnnotation = doesFieldRequireJsonPropertyAnnotation(classField)

            if (fieldRequiresJsonPropertyAnnotation) {
                addImportFor(Fqcns.JACKSON_JSON_PROPERTY)
            }

            val fieldName = classField.classFieldName
            val isEnum = classField.fieldType is EnumFieldType
            val isUrl = classField.fieldType is UrlFieldType
            val isValueFieldWrapper = classField.fieldType.isValueFieldWrapper()

            val usageSite = if (
                isEnum
                || isUrl
                || classField.fieldType is DomainIdFieldType
                || classField.fieldType is ForeignKeyFieldType
            ) {
                null
            } else {
                AnnotationUsageSite.param
            }

            val annotationStrings = constructorArg.annotationDefs.map { "    ${it.toStringInKotlin(usageSite)} " }
            val jsonPropertyAnnotation = if (fieldRequiresJsonPropertyAnnotation) "@param:JsonProperty(\"$fieldName\", access = JsonProperty.Access.READ_WRITE) " else ""
            val visibility = if (fieldIsNotNullable) "private " else ""

            val variableType = if (isEnum || isValueFieldWrapper) {
                if (fieldIsNotNullable) {
                    "val "
                } else {
                    ""
                }
            } else {
                "val "
            }

            val constructorArgName = if (fieldIsNotNullable || isUrl) "${fieldName}_raw" else fieldName
            val unwrappedFieldType = classField.unWrapIfComplexType()
            addImportFor(unwrappedFieldType.fieldType)

            annotationStrings.forEach { appendLine(it) }

            appendLine("    $jsonPropertyAnnotation")
            appendLine("    $visibility$variableType$constructorArgName: ${unwrappedFieldType.convertToNullable().unqualifiedToString}$commaOrNot")

        }

    }


    private fun doesFieldRequireJsonPropertyAnnotation(classField: ClassFieldDef): Boolean {

        return classField.nullable == false || classField.fieldType is UrlFieldType

    }


    override fun renderFunctions() {

        renderGetters()
        renderFunction_toString()

    }


    private fun renderGetters() {

        this.requestDtoDef.dtoFieldDefs.map { it.classFieldDef }.forEach { field ->

            val fieldType = field.fieldType

            when (fieldType) {
                is BooleanFieldType -> renderGetterIfNonNullableField(field)
                is BooleanTypeFieldType -> renderGetterForValueWrapper(field)
                is BooleanValueClassFieldType -> renderGetterForValueWrapper(field)
                is DataClassFieldType -> TODO()
                is DomainIdFieldType -> renderGetterIfNonNullableField(field)
                is DoubleFieldType -> renderGetterIfNonNullableField(field)
                is EnumFieldType -> renderGetterForEnum(field)
                is EsDocFieldType -> renderGetterIfNonNullableField(field)
                is ForeignKeyFieldType -> renderGetterIfNonNullableField(field)
                is FqcnFieldType -> renderGetterIfNonNullableField(field)
                is IdAndNameFieldType -> renderGetterIfNonNullableField(field)
                is InstantFieldType -> renderGetterIfNonNullableField(field)
                is IntFieldType -> renderGetterIfNonNullableField(field)
                is IntTypeFieldType -> renderGetterForValueWrapper(field)
                is IntValueClassFieldType -> renderGetterForValueWrapper(field)
                is ListFieldType -> renderGetterIfNonNullableField(field)
                is LocalDateFieldType -> renderGetterIfNonNullableField(field)
                is LongFieldType -> renderGetterIfNonNullableField(field)
                is LongTypeFieldType -> renderGetterForValueWrapper(field)
                is MapFieldType -> renderGetterIfNonNullableField(field)
                is ObjectIdFieldType -> renderGetterIfNonNullableField(field)
                is PeriodFieldType -> renderGetterIfNonNullableField(field)
                is RequestDtoFieldType -> renderGetterIfNonNullableField(field)
                is SetFieldType -> renderGetterIfNonNullableField(field)
                is SimpleResponseDtoFieldType -> renderGetterIfNonNullableField(field)
                is StringFieldType -> renderGetterIfNonNullableField(field)
                is StringTypeFieldType -> renderGetterForValueWrapper(field)
                is StringValueClassFieldType -> renderGetterForValueWrapper(field)
                is UrlFieldType -> renderGetterForUrl(field)
            }

        }

    }


    private fun renderGetterForValueWrapper(fieldDef: ClassFieldDef) {

        addImportFor(Fqcns.JACKSON_JSON_IGNORE)

        blankLine()
        blankLine()
        appendLine("    @get:JsonIgnore")

        if (fieldDef.nullable) {
            appendLine("    val ${fieldDef.classFieldName} = ${fieldDef.classFieldName}?.let { ${fieldDef.fieldType.unqualifiedToString}(it) }")
        } else {
            appendLine("    val ${fieldDef.classFieldName}")
            appendLine("        get() = ${fieldDef.fieldType.uqcn}(${fieldDef.classFieldName}_raw!!)")
        }

    }


    private fun renderGetterForUrl(fieldDef: ClassFieldDef) {

        addImportFor(Fqcns.JACKSON_JSON_IGNORE)

        blankLine()
        blankLine()
        appendLine("    @get:JsonIgnore")
        appendLine("    val ${fieldDef.classFieldName}")

        if (fieldDef.nullable) {
            appendLine("        get() = ${fieldDef.classFieldName}_raw?.let { ${fieldDef.fieldType.unqualifiedToString}(it) }")
        } else {
            appendLine("        get() = ${fieldDef.fieldType.uqcn}(${fieldDef.classFieldName}_raw!!)")
        }

    }


    private fun renderGetterForEnum(fieldDef: ClassFieldDef) {

        addImportFor(Fqcns.JACKSON_JSON_IGNORE)

        blankLine()
        blankLine()
        appendLine("    @get:JsonIgnore")

        if (fieldDef.nullable) {
            appendLine("    val ${fieldDef.classFieldName} = ${fieldDef.classFieldName}?.let { ${fieldDef.fieldType.unqualifiedToString}.valueOf(it) }")
        } else {
            appendLine("    val ${fieldDef.classFieldName}")
            appendLine("        get() = ${fieldDef.unqualifiedToString}.valueOf(${fieldDef.classFieldName}_raw!!)")
        }

    }


    private fun renderFunction_toString() {

        blankLine()
        blankLine()
        appendLine("    override fun toString(): String {")
        blankLine()
        appendLine("        return \"${this.requestDtoDef.uqcn}{\" +")
        append("                ")

        val lines: String = this.requestDtoDef.classFieldDefs
            .map { fd ->

                if (fd.isMasked) {
                    "\"${fd.classFieldName} = 'MASKED'\" +"
                } else {
                    "\"${fd.classFieldName} = '\" + this.${fd.classFieldName} + '\\'' +"
                }

            }.joinToString(" \", \" + \n                ")

        append(lines)

        appendLine("\n                \"}\"")
        blankLine()
        appendLine("    }")

    }


    private fun renderGetterIfNonNullableField(fieldDef: ClassFieldDef) {

        if (fieldDef.nullable) {
            return
        }

        addImportFor(Fqcns.JACKSON_JSON_IGNORE)

        blankLine()
        blankLine()
        appendLine("    @get:JsonIgnore")
        appendLine("    val ${fieldDef.classFieldName}")
        appendLine("        get() = ${fieldDef.classFieldName}_raw!!")

    }


}
