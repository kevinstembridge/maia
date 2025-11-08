package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.ResponseDtoDef


class ResponseDtoCsvHelperRenderer(private val responseDtoDef: ResponseDtoDef) : AbstractKotlinRenderer(responseDtoDef.csvHelperClassDef) {


    override fun renderFunctions() {

        renderMethod_getHeaderNames()
        renderMethod_getColumnsFrom()

    }


    private fun renderMethod_getHeaderNames() {

        blankLine()
        blankLine()
        appendLine("    override fun getHeaderNames(): Array<String> {")
        blankLine()
        appendLine("        return arrayOf(")

        appendLines(
                this.responseDtoDef.allFields.map { fd -> { append("                \"${fd.classFieldDef.classFieldName}\"") } }
        ) { appendLine(",") }

        appendLine("\n        )")
        blankLine()
        appendLine("    }")

    }


    private fun renderMethod_getColumnsFrom() {

        blankLine()
        blankLine()
        appendLine("    override fun getColumnsFrom(dto: ${this.responseDtoDef.dtoDef.uqcn}): List<Any?> {")
        blankLine()
        appendLine("        return listOf(")
        appendLines(this.responseDtoDef.allClassFields.map { {append("            dto.${it.classFieldName}")}}, {appendLine(",")})
        appendLine("        )")
        blankLine()
        appendLine("    }")

    }


}
