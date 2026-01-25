package org.maiaframework.gen.spec.definition

class DtoHtmlTableActionColumnDef(
    val actionName: ActionName,
    columnHeader: String?,
    val icon: String?,
    cellRenderer: AgGridCellRendererDef?
): AbstractDtoHtmlTableColumnDef(
    columnHeader,
    cellRenderer
)
