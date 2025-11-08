package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.lang.ListFieldType


class EntityFieldConverterRenderer(private val entityDef: EntityDef) : AbstractKotlinRenderer(entityDef.entityFieldConverterClassDef) {


    init {

        entityDef.allEntityFieldsSorted.forEach { fieldDef ->

            fieldDef.fieldReaderClassField?.let { addConstructorArg(it) }
            fieldDef.fieldWriterClassField?.let { addConstructorArg(it) }

        }

    }


    override fun renderFunctions() {

        blankLine()
        blankLine()
        appendLine("    override fun convert(tableColumnName: String, inputValue: Any?): Any? {")
        blankLine()
        appendLine("        when (tableColumnName) {")
        blankLine()

        this.entityDef.allEntityFieldsSorted.forEach { fieldDef ->

            addImportFor(fieldDef.classFieldDef.fieldType)

            appendLine("            \"${fieldDef.dbColumnFieldDef.tableColumnName}\" -> // ${fieldDef.classFieldDef.classFieldName}")

            val fieldWriterClassField = fieldDef.fieldWriterClassField

            if (fieldWriterClassField != null) {

                val fieldWriterClassFieldName = fieldWriterClassField.classFieldName
                val fieldType = fieldDef.classFieldDef.fieldType

                when (fieldType) {
                    is ListFieldType -> appendLine("                return (inputValue as List<${fieldType.parameterFieldType.unqualifiedToString}>).map($fieldWriterClassFieldName::writeField)")
                    else -> appendLine("                return $fieldWriterClassFieldName.writeField(inputValue as ${fieldDef.classFieldDef.unqualifiedToString})")
                }

            } else {

                renderWriteConversionForImplicitField(fieldDef, indent = "                ", "inputValue", requiresCast = true, renderer = this)

            }

        }

        appendLine("             else -> throw RuntimeException(\"Unknown tableColumnName [\$tableColumnName]\")")
        appendLine("        }")
        blankLine()
        appendLine("    }")

    }


}
