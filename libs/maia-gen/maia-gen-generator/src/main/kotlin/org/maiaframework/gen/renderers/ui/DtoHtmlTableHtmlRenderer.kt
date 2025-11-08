package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.renderers.AbstractSourceFileRenderer
import org.maiaframework.gen.spec.definition.DtoHtmlTableDef
import org.maiaframework.gen.spec.definition.DtoHtmlTableActionColumnDef
import org.maiaframework.gen.spec.definition.DtoHtmlTableColumnDef

class DtoHtmlTableHtmlRenderer(private val dtoDef: DtoHtmlTableDef) : AbstractSourceFileRenderer() {


    override fun renderedFilePath(): String {

        return dtoDef.tableComponent.htmlRenderedFilePath

    }


    override fun renderSource(): String {

        appendLine("<form>")
        blankLine()

        if (this.dtoDef.addButtonDef != null) {
            appendLine("""
                |    @if (addButtonVisible) {
                |        <div class="col d-flex">
                |            <button (click)=\"onAddButtonClicked()\" mat-flat-button color=\"primary\" class=\"ms-auto\">Add</button>
                |        </div>
                |    }
            """.trimMargin())
        }

        blankLine()
        appendLine("  <table mat-table [dataSource]=\"rows$\" matSort>")

        this.dtoDef.dtoHtmlTableColumnDefs.forEach { fieldDef ->

            blankLine()
            when (fieldDef) {
                is DtoHtmlTableColumnDef -> {

                    val pipes = if (fieldDef.pipes.isEmpty()) {
                        ""
                    } else {
                        fieldDef.pipes.joinToString(prefix = " | ", separator = " | ")
                    }

                    appendLine("    <ng-container matColumnDef=\"${fieldDef.dtoFieldName}\">")
                    appendLine("      <th mat-header-cell *matHeaderCellDef${if (fieldDef.isSortable) " mat-sort-header" else ""}>${fieldDef.columnHeader ?: ""}</th>")
                    appendLine("      <td mat-cell *matCellDef=\"let row\">{{row.${fieldDef.dtoFieldName}$pipes}}</td>")
                    appendLine("    </ng-container>")
                    blankLine()
                    appendLine("    <ng-container matColumnDef=\"${fieldDef.dtoFieldName}Filter\">")
                    if (fieldDef.isFilterable) {
                        appendLine("      <th mat-header-cell *matHeaderCellDef><input name=\"${fieldDef.dtoFieldName}\" matInput placeholder=\"filter...\" [(ngModel)]=\"${fieldDef.dtoFieldName}\"></th>")
                    } else {
                        appendLine("      <th mat-header-cell *matHeaderCellDef></th>")
                    }
                    appendLine("    </ng-container>")

                }
                is DtoHtmlTableActionColumnDef -> {

                    appendLine("    <ng-container matColumnDef=\"${fieldDef.actionName}\">")
                    appendLine("      <th mat-header-cell *matHeaderCellDef>${fieldDef.columnHeader ?: ""}</th>")
                    appendLine("      <td mat-cell *matCellDef=\"let row\"><mat-icon (click)=\"on${fieldDef.actionName.firstToUpper()}(row)\" class=\"pointer-primary\">${fieldDef.icon}</mat-icon></td>")
                    appendLine("    </ng-container>")
                    blankLine()
                    appendLine("    <ng-container matColumnDef=\"${fieldDef.actionName}Filter\">")
                    appendLine("      <th mat-header-cell *matHeaderCellDef></th>")
                    appendLine("    </ng-container>")

                }
                else -> throw RuntimeException("Unknown type of field def: $fieldDef")
            }
        }

        blankLine()
        appendLine("    <tr mat-header-row *matHeaderRowDef=\"displayedColumns\"></tr>")
        appendLine("    <tr mat-header-row *matHeaderRowDef=\"displayedFilterColumns\"></tr>")

        if (this.dtoDef.clickableTableRowDef == null) {
            appendLine("    <tr mat-row *matRowDef=\"let row; columns: displayedColumns;\"></tr>")
        } else {
            appendLine("    <tr mat-row *matRowDef=\"let row; columns: displayedColumns;\" (click)=\"onRowClicked(row)\"></tr>")
        }

        blankLine()
        appendLine("  </table>")
        blankLine()
        appendLine("  <mat-paginator [length]=\"total$ | async\" [pageSize]=\"pageSize\" [pageSizeOptions]=\"[10, 20, 50, 100]\"></mat-paginator>")
        blankLine()
        appendLine("</form>")

        return sourceCode.toString()

    }


}
