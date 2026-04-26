package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.BlotterDef
import org.maiaframework.gen.spec.definition.BlotterEsDocSourceDef


class EsDocBlotterRowDtoMapperRenderer(
    private val blotterDef: BlotterDef
): AbstractKotlinRenderer(
    blotterDef.searchDtoDef.esDocMapperClassDef
) {


    private val blotterRowDtoUqcn = blotterDef.searchDtoDef.uqcn


    private val esDocDef = (blotterDef.blotterSourceDef as BlotterEsDocSourceDef).esDocDef


    override fun renderFunctions() {

        renderFunction_mapEsDoc()

    }


    private fun renderFunction_mapEsDoc() {

        append("""            |
            |
            |    fun mapEsDoc(esDoc: ${esDocDef.uqcn}): ${this.blotterRowDtoUqcn} {
            |
            |        return ${this.blotterRowDtoUqcn}(
            |""".trimMargin())

        this.blotterDef.blotterColumnFields.forEach { blotterColumnDef ->
            appendLine("            esDoc.${blotterColumnDef.fieldPathInSourceData},")
        }

        appendLine("        )")
        blankLine()
        appendLine("    }")

    }


}
