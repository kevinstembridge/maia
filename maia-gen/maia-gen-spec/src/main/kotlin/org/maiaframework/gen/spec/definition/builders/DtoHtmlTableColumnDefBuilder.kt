package org.maiaframework.gen.spec.definition.builders


import org.maiaframework.gen.spec.definition.AgGridCellRendererDef
import org.maiaframework.gen.spec.definition.DtoHtmlTableColumnDef
import org.maiaframework.gen.spec.definition.Pipes
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.InstantFieldType
import java.util.SortedSet


class DtoHtmlTableColumnDefBuilder(
    private val fieldPathInSourceData: String,
    private val dtoFieldName: String,
    private val classFieldDef: ClassFieldDef,
    private val isSortable: Boolean,
) : DefBuilder<DtoHtmlTableColumnDef> {


    private var columnHeader: String? = null


    private val pipes: SortedSet<String> = sortedSetOf()


    private val fieldType = this.classFieldDef.fieldType


    var isFilterable: Boolean = true


    var cellDataType: DtoHtmlTableColumnDef.AgGridCellDataType? = null


    var agGridCellRendererDef: AgGridCellRendererDef? = null


    init {

        if (fieldType is InstantFieldType) {
            this.pipes.add(Pipes.INSTANT_PIPE)
        }

    }


    override fun build(): DtoHtmlTableColumnDef {

        return DtoHtmlTableColumnDef(
            this.fieldPathInSourceData,
            this.dtoFieldName,
            this.columnHeader,
            this.isSortable,
            this.isFilterable,
            this.fieldType,
            this.classFieldDef.nullability,
            this.cellDataType,
            this.agGridCellRendererDef,
            this.pipes.toList()
        )

    }


    fun header(columnHeader: String): DtoHtmlTableColumnDefBuilder {

        this.columnHeader = columnHeader
        return this

    }


    fun pipes(vararg pipes: String): DtoHtmlTableColumnDefBuilder {

        this.pipes.addAll(pipes)
        return this

    }


}
