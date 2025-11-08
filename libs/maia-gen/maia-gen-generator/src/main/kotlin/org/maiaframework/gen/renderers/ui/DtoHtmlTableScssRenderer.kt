package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.DtoHtmlTableDef


class DtoHtmlTableScssRenderer(private val dtoHtmlTableDef: DtoHtmlTableDef) : AbstractTypescriptRenderer() {


    override fun renderedFilePath(): String {

        return dtoHtmlTableDef.tableScssPath

    }


    override fun renderSourceBody() {

        blankLine()
        appendLine("table {")
        appendLine("  width: 100%;")
        appendLine("}")

        this.dtoHtmlTableDef.clickableTableRowDef?.let {

            blankLine()
            appendLine(".mat-mdc-row .mat-mdc-cell {")
            appendLine("  border-bottom: 1px solid transparent;")
            appendLine("  border-top: 1px solid transparent;")
            appendLine("  cursor: pointer;")
            appendLine("}")
            blankLine()
            appendLine(".mat-mdc-row:hover .mat-mdc-cell {")
            appendLine("  border-color: currentColor;")
            appendLine("}")

        }

    }


}
