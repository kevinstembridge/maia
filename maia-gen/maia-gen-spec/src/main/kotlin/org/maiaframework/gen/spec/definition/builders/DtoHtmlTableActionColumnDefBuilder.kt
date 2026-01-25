package org.maiaframework.gen.spec.definition.builders


import org.maiaframework.gen.spec.definition.ActionName
import org.maiaframework.gen.spec.definition.AgGridCellRendererDef
import org.maiaframework.gen.spec.definition.DtoHtmlTableActionColumnDef


class DtoHtmlTableActionColumnDefBuilder(
    private val actionName: ActionName,
    private val cellRenderer: AgGridCellRendererDef
) : DefBuilder<DtoHtmlTableActionColumnDef> {

    private var columnHeader: String? = null
    private var icon: String? = null


    override fun build(): DtoHtmlTableActionColumnDef {

        return DtoHtmlTableActionColumnDef(
            this.actionName,
            this.columnHeader,
            this.icon,
            this.cellRenderer
        )

    }


    fun header(columnHeader: String): DtoHtmlTableActionColumnDefBuilder {

        this.columnHeader = columnHeader
        return this

    }


    fun icon(icon: String): DtoHtmlTableActionColumnDefBuilder {

        this.icon = icon
        return this

    }


}
