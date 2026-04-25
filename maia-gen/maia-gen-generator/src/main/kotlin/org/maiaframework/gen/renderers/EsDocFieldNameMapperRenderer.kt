package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.BlotterDef

class EsDocFieldNameMapperRenderer(
    private val blotterDef: BlotterDef
) : AbstractKotlinRenderer(
    blotterDef.searchDtoDef.fieldNameMapperClassDef
) {


    override fun renderPreClassFields() {

        blankLine()
        blankLine()
        appendLine("    val mappingFunction: (String) -> String = {")
        appendLine("        when (it) {")

        blotterDef.blotterColumnFields.forEach { dtoField ->
            appendLine("            \"${dtoField.dtoFieldName}\" -> \"${dtoField.fieldPathInSourceData}\"")
        }

        appendLine("            else -> throw IllegalArgumentException(\"Unknown field name: \$it\")")
        appendLine("        }")
        appendLine("    }")

    }


}
