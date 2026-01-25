package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.GeneratedTypescriptDir
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.EnumFieldType
import org.maiaframework.gen.spec.definition.lang.EsDocFieldType
import org.maiaframework.gen.spec.definition.lang.ListFieldType
import org.maiaframework.gen.spec.definition.lang.RequestDtoFieldType
import org.maiaframework.gen.spec.definition.lang.SimpleResponseDtoFieldType
import org.maiaframework.gen.spec.definition.lang.Uqcn

class TypescriptClassDtoRenderer(
    private val renderedFilePath: String,
    private val className: Uqcn,
    fields: List<ClassFieldDef>
) : AbstractTypescriptRenderer() {

    private val sortedFields = fields.sortedWith(compareBy<ClassFieldDef> { it.nullable }.thenBy { it.classFieldName })


    override fun renderedFilePath(): String {

        return this.renderedFilePath

    }


    override fun renderSourceBody() {

        renderImportStatements()
        blankLine()
        appendLine("export class ${this.className} {")
        blankLine()
        renderClassFields(";", indentSize = 4)
        blankLine()
        appendLine("    constructor(")
        renderClassFields(",", indentSize = 8)
        appendLine("    ) {")

        this.sortedFields.forEach { classFieldDef ->
            appendLine("        this.${classFieldDef.classFieldName} = ${classFieldDef.classFieldName};")
        }

        appendLine("    }")
        blankLine()
        appendLine("}")

    }


    private fun renderClassFields(separator: String, indentSize: Int) {

        val indent = "".padEnd(indentSize, ' ')

        this.sortedFields.forEach { fieldDef ->

            val nullableClause = if (fieldDef.nullable) "?" else ""

            val fieldType = fieldDef.fieldType

            if (fieldType is ListFieldType) {

                val listElementType = fieldType.parameterFieldType
                appendLine("$indent${fieldDef.classFieldName}${nullableClause}: ReadonlyArray<${listElementType.fqcn.uqcn}>$separator")

            } else if (fieldType is SimpleResponseDtoFieldType) {

                appendLine("$indent${fieldDef.classFieldName}${nullableClause}: ${fieldType.responseDtoDef.dtoDef.uqcn}$separator")

            } else if (fieldType is RequestDtoFieldType) {

                appendLine("$indent${fieldDef.classFieldName}${nullableClause}: ${fieldType.uqcn}$separator")

            } else if (fieldType is EnumFieldType) {

                appendLine("$indent${fieldDef.classFieldName}$nullableClause: ${fieldType.enumDef.uqcn}$separator")

            } else if (fieldType is EsDocFieldType) {

                appendLine("$indent${fieldDef.classFieldName}$nullableClause: ${fieldType.esDocDef.uqcn}$separator")

            } else {

                val type = fieldDef.fieldType.typescriptCompatibleType
                    ?: throw IllegalArgumentException("Expecting field to have a typescript-compatible type. dtoClassName = ${this.className}, fieldName = ${fieldDef.classFieldName}")

                appendLine("$indent${fieldDef.classFieldName}${nullableClause}: ${type}$separator")

            }

        }

    }


    init {

        val importStatements = mutableSetOf<String>()

        blankLine()

        this.sortedFields.forEach { fieldDef ->

            // TODO also need to cater for non-list types and lists of primitive types.
            val fieldType = fieldDef.fieldType

            if (fieldType is ListFieldType) {

                val listElementType = fieldType.parameterFieldType
                importStatements.add(
                    "import { ${listElementType.fqcn.uqcn} } from '@${
                        GeneratedTypescriptDir.forPackage(
                            listElementType.fqcn.packageName
                        )
                    }/${listElementType.fqcn.uqcn}';"
                )

            }

            if (fieldType is EnumFieldType) {
                importStatements.add("import { ${fieldType.enumDef.uqcn} } from '@${GeneratedTypescriptDir.forPackage(fieldType.enumDef.fqcn.packageName)}/${fieldType.enumDef.uqcn}';")
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

        }

        importStatements.forEach { appendLine(it) }

    }


}
