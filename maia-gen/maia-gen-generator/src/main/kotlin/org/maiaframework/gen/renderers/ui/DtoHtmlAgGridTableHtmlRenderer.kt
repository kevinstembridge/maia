package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.renderers.AbstractSourceFileRenderer
import org.maiaframework.gen.spec.definition.DtoHtmlTableDef

class DtoHtmlAgGridTableHtmlRenderer(private val dtoDef: DtoHtmlTableDef) : AbstractSourceFileRenderer() {


    override fun renderedFilePath(): String {

        return dtoDef.tableComponent.htmlRenderedFilePath

    }


    override fun renderSource(): String {

        if (this.dtoDef.addButtonDef != null) {
            appendLine("""
                 |@if (addButtonVisible) {
                 |    <div class="flex">
                 |        <button (click)="onAddButtonClicked()" mat-flat-button color="primary" class="ms-auto">Add</button>
                 |    </div>
                 |}""".trimMargin())
        }

        append(
            $$"""
            |
            |<div class="h-screen">
            |    <ag-grid-angular
            |        style="width: 100%; height: 100%;"
            |        [columnDefs]="columnDefs"
            |        [defaultColDef]="defaultColDef"
            |        [rowBuffer]="rowBuffer"
            |        [rowSelection]="rowSelection"
            |        [rowModelType]="rowModelType"
            |        [cacheBlockSize]="cacheBlockSize"
            |        [cacheOverflowSize]="cacheOverflowSize"
            |        [maxConcurrentDatasourceRequests]="maxConcurrentDatasourceRequests"
            |        [infiniteInitialRowCount]="infiniteInitialRowCount"
            |        [maxBlocksInCache]="maxBlocksInCache"
            |        [rowData]="rowData"
            |        [theme]="agGridTheme"
            |        (gridReady)="onGridReady($event)"
            |""".trimMargin()
        )

        if (dtoDef.requiresCellClickedEvent) {
            appendLine($$"        (cellClicked)=\"onCellClicked($event)\"")
        }

        append("""
            |    ></ag-grid-angular>
            |</div>
            |""".trimMargin()
        )

        return sourceCode.toString()

    }


}
