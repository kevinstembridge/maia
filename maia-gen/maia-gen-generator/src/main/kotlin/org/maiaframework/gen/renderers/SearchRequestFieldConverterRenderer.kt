package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.ResponseDtoFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassDef


class SearchRequestFieldConverterRenderer(
        classDef: ClassDef,
        private val fieldDefs: List<ResponseDtoFieldDef>
) : AbstractKotlinRenderer(classDef) {


    override fun renderFunctions() {

        renderMethod_toTableColumnName()

    }


    private fun renderMethod_toTableColumnName() {

        blankLine()
        blankLine()
        appendLine("    override fun convertValue(tableColumnPath: String, inputValue: Any?): Any? {")
        blankLine()
        appendLine("        when(tableColumnPath) {")

        this.fieldDefs.forEach { fieldDef ->
            appendLine("            \"${fieldDef.collectionFieldDef.tableColumnName}\" -> // ${fieldDef.classFieldDef.classFieldName}")
            appendLine("                return inputValue") // TODO need to figure out how to do conversion
        }

        appendLine("            else -> throw IllegalArgumentException(\"Unknown tableColumnPath [\$tableColumnPath]\")")
        appendLine("        }")
        blankLine()
        appendLine("    }")

    }


}
