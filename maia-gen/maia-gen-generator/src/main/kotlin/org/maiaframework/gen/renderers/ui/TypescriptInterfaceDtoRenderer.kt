package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.AnyTypescriptType
import org.maiaframework.gen.spec.definition.BooleanTypescriptType
import org.maiaframework.gen.spec.definition.DtoCharacteristic
import org.maiaframework.gen.spec.definition.EnumTypescriptType
import org.maiaframework.gen.spec.definition.FieldTypeTypescriptCompatibleType
import org.maiaframework.gen.spec.definition.GeneratedTypescriptDir
import org.maiaframework.gen.spec.definition.NumberTypescriptType
import org.maiaframework.gen.spec.definition.ObjectTypescriptType
import org.maiaframework.gen.spec.definition.ReadonlyArrayTypescriptType
import org.maiaframework.gen.spec.definition.RecordTypescriptType
import org.maiaframework.gen.spec.definition.StringTypescriptType
import org.maiaframework.gen.spec.definition.TypescriptCompatibleTypes
import org.maiaframework.gen.spec.definition.lang.BooleanFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanTypeFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanValueClassFieldType
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

open class TypescriptInterfaceDtoRenderer(
    private val renderedFilePath: String,
    private val className: Uqcn,
    private val fields: List<ClassFieldDef>,
    private val dtoCharacteristics: Set<DtoCharacteristic>
) : AbstractTypescriptRenderer() {


    init {

        this.fields.sorted().forEach { fieldDef ->

            // TODO also need to cater for non-list types and lists of primitive types.

            val fieldType = fieldDef.fieldType

            if (fieldType is ListFieldType) {

                val listElementType = fieldType.parameterFieldType
                addImportsFor(listElementType)

                if (listElementType is ForeignKeyFieldType && dtoCharacteristics.contains(DtoCharacteristic.RESPONSE_DTO)) {
                    addImport(listElementType.foreignKeyFieldDef.foreignEntityDef.entityPkAndNameDef.pkAndNameDtoTypescriptImport)
                }

            }

            if (fieldType is MapFieldType) {
                addImportsFor(fieldType)
            }

            addImportsFor(fieldType)

            if (fieldType is ForeignKeyFieldType && dtoCharacteristics.contains(DtoCharacteristic.RESPONSE_DTO)) {
                addImport(fieldType.foreignKeyFieldDef.foreignEntityDef.entityPkAndNameDef.pkAndNameDtoTypescriptImport)
            }

        }

    }


    override fun renderedFilePath(): String {

        return this.renderedFilePath

    }


    override fun renderSourceBody() {

        blankLine()
        appendLine("export interface ${this.className} {")

        this.fields.sorted().forEach { fieldDef ->

            val nullableClause = if (fieldDef.nullable) "?" else ""

            val fieldType = fieldDef.fieldType

            when (fieldType) {
                is BooleanFieldType -> renderForPlainField(fieldDef, nullableClause)
                is BooleanTypeFieldType -> renderForPlainField(fieldDef, nullableClause)
                is BooleanValueClassFieldType -> renderForPlainField(fieldDef, nullableClause)
                is DataClassFieldType -> appendLine("    ${fieldDef.classFieldName}${nullableClause}: ${fieldType.uqcn};")
                is DomainIdFieldType -> renderForPlainField(fieldDef, nullableClause)
                is DoubleFieldType -> renderForPlainField(fieldDef, nullableClause)
                is EnumFieldType -> appendLine("    ${fieldDef.classFieldName}$nullableClause: ${fieldType.enumDef.uqcn};")
                is EsDocFieldType -> appendLine("    ${fieldDef.classFieldName}$nullableClause: ${fieldType.esDocDef.uqcn};")
                is ForeignKeyFieldType -> renderForForeignKeyField(fieldDef, nullableClause, fieldType)
                is FqcnFieldType -> renderForPlainField(fieldDef, nullableClause)
                is IdAndNameFieldType -> appendLine("    ${fieldDef.classFieldName}$nullableClause: ${fieldType.idAndNameDef.dtoUqcn};")
                is InstantFieldType -> renderForPlainField(fieldDef, nullableClause)
                is IntFieldType -> renderForPlainField(fieldDef, nullableClause)
                is IntTypeFieldType -> renderForPlainField(fieldDef, nullableClause)
                is IntValueClassFieldType -> renderForPlainField(fieldDef, nullableClause)
                is ListFieldType -> appendLine("    ${fieldDef.classFieldName}${nullableClause}: ReadonlyArray<${listElementType(fieldType)}>;")
                is LocalDateFieldType -> renderForPlainField(fieldDef, nullableClause)
                is LongFieldType -> renderForPlainField(fieldDef, nullableClause)
                is LongTypeFieldType -> renderForPlainField(fieldDef, nullableClause)
                is MapFieldType -> renderForMapFieldType(fieldDef, fieldType, nullableClause)
                is ObjectIdFieldType -> renderForPlainField(fieldDef, nullableClause)
                is PeriodFieldType -> renderForPlainField(fieldDef, nullableClause)
                is RequestDtoFieldType -> appendLine("    ${fieldDef.classFieldName}${nullableClause}: ${fieldType.requestDtoDef.uqcn};")
                is SetFieldType -> appendLine("    ${fieldDef.classFieldName}${nullableClause}: ReadonlyArray<${setElementType(fieldType)}>;")
                is SimpleResponseDtoFieldType -> appendLine("    ${fieldDef.classFieldName}${nullableClause}: ${fieldType.responseDtoDef.dtoDef.uqcn};")
                is StringFieldType -> renderForPlainField(fieldDef, nullableClause)
                is StringTypeFieldType -> renderForPlainField(fieldDef, nullableClause)
                is StringValueClassFieldType -> renderForPlainField(fieldDef, nullableClause)
                is UrlFieldType -> renderForPlainField(fieldDef, nullableClause)
            }

        }

        appendLine("}")

    }


    private fun listElementType(listFieldType: ListFieldType): String {

        return collectionElementFieldType(listFieldType.parameterFieldType)

    }


    private fun setElementType(fieldType: SetFieldType): String {

        return collectionElementFieldType(fieldType.parameterFieldType)

    }


    private fun collectionElementFieldType(parameterFieldType: FieldType): String {

        return when (parameterFieldType) {
            is BooleanFieldType -> "boolean"
            is BooleanTypeFieldType -> "boolean"
            is BooleanValueClassFieldType -> "boolean"
            is DomainIdFieldType -> "string"
            is DoubleFieldType -> "number"
            is EnumFieldType -> parameterFieldType.uqcn.value
            is EsDocFieldType -> TODO()
            is ForeignKeyFieldType -> TODO()
            is FqcnFieldType -> TODO()
            is IdAndNameFieldType -> TODO()
            is InstantFieldType -> "string"
            is IntFieldType -> "number"
            is IntTypeFieldType -> "number"
            is IntValueClassFieldType -> "number"
            is ListFieldType -> TODO()
            is LocalDateFieldType -> "string"
            is LongFieldType -> "number"
            is LongTypeFieldType -> "number"
            is MapFieldType -> TODO()
            is ObjectIdFieldType -> "string"
            is PeriodFieldType -> "string"
            is RequestDtoFieldType -> TODO()
            is SetFieldType -> TODO()
            is SimpleResponseDtoFieldType -> parameterFieldType.uqcn.value
            is StringFieldType -> "string"
            is StringTypeFieldType -> "string"
            is StringValueClassFieldType -> "string"
            else -> {
                val typescriptCompatibleType = parameterFieldType.typescriptCompatibleType
                when (typescriptCompatibleType) {
                    TypescriptCompatibleTypes.enum -> parameterFieldType.uqcn.value
                    null -> TODO()
                    else -> typescriptCompatibleType.value
                }
            }
        }

    }


    private fun addImportsFor(
        fieldType: FieldType,
        importStatements: MutableSet<String>
    ) {

        if (fieldType is EnumFieldType) {
            val enumDef = fieldType.enumDef
            importStatements.add("import { ${enumDef.uqcn} } from '@${GeneratedTypescriptDir.forPackage(enumDef.fqcn.packageName)}/${enumDef.uqcn}';")
        }

        if (fieldType is SimpleResponseDtoFieldType) {
            importStatements.add(fieldType.responseDtoDef.dtoDef.typescriptDtoImportStatement)
        }

        if (fieldType is RequestDtoFieldType) {
            importStatements.add(fieldType.requestDtoDef.typescriptFileImportStatement)
        }

        if (fieldType is EsDocFieldType) {
            importStatements.add(fieldType.esDocDef.dtoDef.typescriptDtoImportStatement)
        }

        if (fieldType is ForeignKeyFieldType && dtoCharacteristics.contains(DtoCharacteristic.RESPONSE_DTO)) {
            importStatements.add(fieldType.foreignKeyFieldDef.foreignEntityDef.entityPkAndNameDef.pkAndNameDtoImportStatement)
        }

        if (fieldType is IdAndNameFieldType) {
            importStatements.add(fieldType.idAndNameDef.pkAndNameDtoImportStatement)
        }

    }


    private fun renderForForeignKeyField(
        fieldDef: ClassFieldDef,
        nullableClause: String,
        fieldType: ForeignKeyFieldType
    ) {

        if (dtoCharacteristics.contains(DtoCharacteristic.RESPONSE_DTO)) {
            appendLine("    ${fieldDef.classFieldName}$nullableClause: ${fieldType.foreignKeyFieldDef.foreignEntityDef.entityPkAndNameDef.dtoUqcn};")
        } else {
            appendLine("    ${fieldDef.classFieldName}$nullableClause: string;")
        }

    }


    private fun renderForPlainField(fieldDef: ClassFieldDef, nullableClause: String) {

        val type = fieldDef.fieldType.typescriptCompatibleType
            ?: throw IllegalArgumentException("Expecting field to have a typescript-compatible type. dtoClassName = ${this.className}, fieldName = ${fieldDef.classFieldName}")

        appendLine("    ${fieldDef.classFieldName}${nullableClause}: ${type};")

    }


    private fun renderForMapFieldType(
        fieldDef: ClassFieldDef,
        fieldType: MapFieldType,
        nullableClause: String
    ) {

        val type = fieldType.typescriptCompatibleType
            ?: throw IllegalArgumentException("Expecting field to have a typescript-compatible type. dtoClassName = ${this.className}, fieldName = ${fieldDef.classFieldName}")

        when (type) {
            is AnyTypescriptType -> appendLine("    ${fieldDef.classFieldName}${nullableClause}: ${type};")
            is BooleanTypescriptType -> appendLine("    ${fieldDef.classFieldName}${nullableClause}: ${type};")
            is EnumTypescriptType -> appendLine("    ${fieldDef.classFieldName}${nullableClause}: ${type};")
            is NumberTypescriptType -> appendLine("    ${fieldDef.classFieldName}${nullableClause}: ${type};")
            is ObjectTypescriptType -> appendLine("    ${fieldDef.classFieldName}${nullableClause}: ${type};")
            is RecordTypescriptType -> {
                addImportsFor(fieldType)
                appendLine("    ${fieldDef.classFieldName}${nullableClause}: ${type};")
            }
            is StringTypescriptType -> appendLine("    ${fieldDef.classFieldName}${nullableClause}: ${type};")
            is ReadonlyArrayTypescriptType -> {
                addImportsFor(fieldType)
                appendLine("    ${fieldDef.classFieldName}${nullableClause}: ${type};")
            }
            is FieldTypeTypescriptCompatibleType -> appendLine("    ${fieldDef.classFieldName}${nullableClause}: ${type};")
        }

    }


}
