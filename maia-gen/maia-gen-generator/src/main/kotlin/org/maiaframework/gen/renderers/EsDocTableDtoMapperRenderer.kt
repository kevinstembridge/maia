package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.DtoHtmlTableDef
import org.maiaframework.gen.spec.definition.DtoHtmlTableEsDocSourceDef


class EsDocTableDtoMapperRenderer(
    private val dtoHtmlTableDef: DtoHtmlTableDef
): AbstractKotlinRenderer(
    dtoHtmlTableDef.searchDtoDef.esDocMapperClassDef
) {


    private val tableDtoUqcn = dtoHtmlTableDef.searchDtoDef.uqcn


    private val esDocDef = (dtoHtmlTableDef.dtoHtmlTableSourceDef as DtoHtmlTableEsDocSourceDef).esDocDef


    override fun renderFunctions() {

        renderFunction_mapEsDoc()

    }


    private fun renderFunction_mapEsDoc() {

        append("""            |
            |
            |    fun mapEsDoc(esDoc: ${esDocDef.uqcn}): ${this.tableDtoUqcn} {
            |
            |        return ${this.tableDtoUqcn}(
            |""".trimMargin())

        this.dtoHtmlTableDef.dtoHtmlTableColumnFields.forEach { dtoHtmlTableColumnDef ->
            appendLine("            esDoc.${dtoHtmlTableColumnDef.fieldPathInSourceData},")
        }

        appendLine("        )")
        blankLine()
        appendLine("    }")

    }


}
