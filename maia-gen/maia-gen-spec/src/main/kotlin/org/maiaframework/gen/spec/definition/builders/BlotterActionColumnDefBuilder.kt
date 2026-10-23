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
    private var enabledWhenField: String? = null


    override fun build(): BlotterActionColumnDef {

        return BlotterActionColumnDef(
            this.actionName,
            this.columnHeader,
            this.icon,
            this.cellRenderer,
            this.enabledWhenField
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


    /**
     * The action is enabled for a row only when the named boolean field of the row's DTO is true.
     * Otherwise the icon is rendered greyed out and clicking it does nothing.
     */
    fun enabledWhen(booleanRowField: String): BlotterActionColumnDefBuilder {

        require(booleanRowField.isNotBlank()) { "booleanRowField must not be blank" }
        this.enabledWhenField = booleanRowField
        return this

    }


}
