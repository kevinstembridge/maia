package org.maiaframework.gen.spec.definition

class BlotterActionColumnDef(
    val actionName: ActionName,
    columnHeader: String?,
    val icon: String?,
    cellRenderer: AgGridCellRendererDef?
): AbstractBlotterColumnDef(
    columnHeader,
    cellRenderer
)
