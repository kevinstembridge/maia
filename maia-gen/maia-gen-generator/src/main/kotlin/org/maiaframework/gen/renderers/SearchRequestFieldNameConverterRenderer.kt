package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.ResponseDtoFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassDef


class SearchRequestFieldNameConverterRenderer(
        classDef: ClassDef,
        private val fieldDefs: List<ResponseDtoFieldDef>
): AbstractKotlinRenderer(classDef) {


    override fun renderFunctions() {

        renderMethod_toTableColumnName()

    }


    private fun renderMethod_toTableColumnName() {

        blankLine()
        blankLine()
        appendLine("    override fun convertFieldName(fieldName: String): String {")
        blankLine()
        appendLine("        return when(fieldName) {")

        this.fieldDefs.forEach { fieldDef ->
            appendLine("            \"${fieldDef.classFieldDef.classFieldName}\" -> \"${fieldDef.collectionFieldDef.tableColumnName}\"")
        }

        appendLine("            else -> throw IllegalArgumentException(\"Unknown dtoFieldName [\$fieldName]\")")
        appendLine("        }")
        blankLine()
        appendLine("    }")

    }


}
