package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.DtoHtmlTableDef

class EsDocFieldNameMapperRenderer(
    private val dtoHtmlTableDef: DtoHtmlTableDef
) : AbstractKotlinRenderer(
    dtoHtmlTableDef.searchDtoDef.fieldNameMapperClassDef
) {


    override fun renderPreClassFields() {

        blankLine()
        blankLine()
        appendLine("    val mappingFunction: (String) -> String = {")
        appendLine("        when (it) {")

        dtoHtmlTableDef.dtoHtmlTableColumnFields.forEach { dtoField ->
            appendLine("            \"${dtoField.dtoFieldName}\" -> \"${dtoField.fieldPathInSourceData}\"")
        }

        appendLine("            else -> throw IllegalArgumentException(\"Unknown field name: \$it\")")
        appendLine("        }")
        appendLine("    }")

    }


}
