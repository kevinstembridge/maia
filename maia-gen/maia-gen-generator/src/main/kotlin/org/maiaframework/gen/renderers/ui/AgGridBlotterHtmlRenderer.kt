package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.renderers.AbstractSourceFileRenderer
import org.maiaframework.gen.spec.definition.BlotterDef
import org.maiaframework.gen.spec.definition.BlotterHazelcastSourceDef

class AgGridBlotterHtmlRenderer(private val dtoDef: BlotterDef) : AbstractSourceFileRenderer() {


    override fun renderedFilePath(): String {

        return dtoDef.blotterComponent.htmlRenderedFilePath

    }


    override fun renderSource(): String {

        if (this.dtoDef.entityCreatePageDef != null) {
            append("""
                 |@if (addButtonVisible) {
                 |    <div class="flex">
                 |        <button (click)="onAddButtonClicked()" mat-flat-button color="primary" class="ms-auto">Add</button>
                 |    </div>
                 |}
                 |""".trimMargin())
        }

        val isClientSide = dtoDef.blotterSourceDef is BlotterHazelcastSourceDef

        if (isClientSide) {
            append(
                """
                |
                |<div class="flex">
                |    <button (click)="refresh()" mat-icon-button class="ms-auto" aria-label="Refresh" data-testid="refresh-button">
                |        <mat-icon>refresh</mat-icon>
                |    </button>
                |</div>
                |""".trimMargin()
            )
        }

        append(
            $$"""
            |
            |<div class="h-screen">
            |    <ag-grid-angular
            |        style="width: 100%; height: 100%;"
            |        [columnDefs]="columnDefs"
            |        [defaultColDef]="defaultColDef"
            |""".trimMargin()
        )

        if (dtoDef.hasDateTimeStringColumn) {
            appendLine("        [dataTypeDefinitions]=\"dataTypeDefinitions\"")
        }

        append(
            $$"""
            |        [rowBuffer]="rowBuffer"
            |        [rowSelection]="rowSelection"
            |        [rowModelType]="rowModelType"
            |""".trimMargin()
        )

        if (!isClientSide) {
            append(
                $$"""
                |        [cacheBlockSize]="cacheBlockSize"
                |        [cacheOverflowSize]="cacheOverflowSize"
                |        [maxConcurrentDatasourceRequests]="maxConcurrentDatasourceRequests"
                |        [infiniteInitialRowCount]="infiniteInitialRowCount"
                |        [maxBlocksInCache]="maxBlocksInCache"
                |        [pagination]="pagination"
                |        [paginationPageSize]="paginationPageSize"
                |        [paginationPageSizeSelector]="paginationPageSizeSelector"
                |""".trimMargin()
            )
        }

        append(
            $$"""
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
