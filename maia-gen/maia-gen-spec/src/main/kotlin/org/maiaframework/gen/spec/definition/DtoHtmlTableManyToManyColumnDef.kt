package org.maiaframework.gen.spec.definition

class DtoHtmlTableManyToManyColumnDef(
    val joinEntityDef: EntityDef,
    val rightEntityDef: EntityDef,
    columnHeader: String
) : AbstractDtoHtmlTableColumnDef(
    columnHeader = columnHeader,
    cellRenderer = AgGridCellRendererDefs.manyToManyChips
)
