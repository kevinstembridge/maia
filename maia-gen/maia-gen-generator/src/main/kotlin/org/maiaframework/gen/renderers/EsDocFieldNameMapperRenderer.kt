package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.BlotterColumnDef
import org.maiaframework.gen.spec.definition.BlotterDef
import org.maiaframework.gen.spec.definition.BlotterEsDocSourceDef
import org.maiaframework.gen.spec.definition.TextAndKeywordEsDocMappingType

class EsDocFieldNameMapperRenderer(
    private val blotterDef: BlotterDef
) : AbstractKotlinRenderer(
    blotterDef.searchDtoDef.fieldNameMapperClassDef
) {


    private val esDocDef = (blotterDef.blotterSourceDef as BlotterEsDocSourceDef).esDocDef


    override fun renderPreClassFields() {

        blankLine()
        blankLine()
        appendLine("    val mappingFunction: (String) -> String = {")
        appendLine("        when (it) {")

        blotterDef.blotterColumnFields.forEach { dtoField ->
            appendLine("            \"${dtoField.dtoFieldName}\" -> \"${filterAndSortFieldPathFor(dtoField)}\"")
        }

        appendLine("            else -> throw IllegalArgumentException(\"Unknown field name: \$it\")")
        appendLine("        }")
        appendLine("    }")

    }


    /**
     * A `textAndKeyword` field is mapped as an analyzed `text` field (tokenised, so exact `term` filters do not
     * match and it cannot be sorted) with a `keyword` sub-field named `raw`, so filters and sorts must use
     * `<field>.raw`. Only direct fields of the EsDoc are considered: a nested or unknown path is left unchanged.
     */
    private fun filterAndSortFieldPathFor(blotterColumnDef: BlotterColumnDef): String {

        val fieldPath = blotterColumnDef.fieldPathInSourceData

        if (fieldPath.isJustOneField().not()) {
            return fieldPath.toString()
        }

        val esDocFieldDef = esDocDef.fields.firstOrNull { it.classFieldDef.classFieldName.value == fieldPath.head() }

        return if (esDocFieldDef?.mappingType is TextAndKeywordEsDocMappingType) {
            "$fieldPath.raw"
        } else {
            fieldPath.toString()
        }

    }


}
