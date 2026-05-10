package org.maiaframework.gen.spec.definition

sealed class AbstractBlotterColumnDef(
    val columnHeader: String?,
    val cellRenderer: AgGridCellRendererDef?,
    val colId: String,
    val hide: Boolean
)
