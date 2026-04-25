package org.maiaframework.gen.spec.definition.builders


import org.maiaframework.gen.spec.definition.ActionName
import org.maiaframework.gen.spec.definition.AgGridCellRendererDef
import org.maiaframework.gen.spec.definition.BlotterActionColumnDef


@MaiaDslMarker
class BlotterActionColumnDefBuilder(
    private val actionName: ActionName,
    private val cellRenderer: AgGridCellRendererDef
) : DefBuilder<BlotterActionColumnDef> {

    private var columnHeader: String? = null
    private var icon: String? = null


    override fun build(): BlotterActionColumnDef {

        return BlotterActionColumnDef(
            this.actionName,
            this.columnHeader,
            this.icon,
            this.cellRenderer
        )

    }


    fun header(columnHeader: String): BlotterActionColumnDefBuilder {

        this.columnHeader = columnHeader
        return this

    }


    fun icon(icon: String): BlotterActionColumnDefBuilder {

        this.icon = icon
        return this

    }


}
