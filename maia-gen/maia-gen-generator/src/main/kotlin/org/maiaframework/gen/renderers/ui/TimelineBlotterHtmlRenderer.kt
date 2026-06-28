package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.renderers.AbstractSourceFileRenderer
import org.maiaframework.gen.spec.definition.TimelineBlotterDef


class TimelineBlotterHtmlRenderer(
    private val def: TimelineBlotterDef
) : AbstractSourceFileRenderer() {


    override fun renderedFilePath(): String {
        return def.blotterComponentNames.htmlRenderedFilePath
    }


    override fun renderSource(): String {

        append($$"""
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
            |    ></ag-grid-angular>
            |</div>
            |""".trimMargin())

        return sourceCode.toString()

    }


}
