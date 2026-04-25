package org.maiaframework.gen.spec.definition.builders


import org.maiaframework.gen.spec.definition.AgGridCellRendererDef
import org.maiaframework.gen.spec.definition.BlotterColumnDef
import org.maiaframework.gen.spec.definition.FieldPath
import org.maiaframework.gen.spec.definition.Pipes
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.InstantFieldType
import java.util.SortedSet


@MaiaDslMarker
class BlotterColumnDefBuilder(
    private val fieldPathInSourceData: FieldPath,
    private val dtoFieldName: String,
    private val classFieldDef: ClassFieldDef,
    private val isSortable: Boolean,
) : DefBuilder<BlotterColumnDef> {


    private var columnHeader: String? = null


    private val pipes: SortedSet<String> = sortedSetOf()


    private val fieldType = this.classFieldDef.fieldType


    var isFilterable: Boolean = true


    var cellDataType: BlotterColumnDef.AgGridCellDataType? = null


    var agGridCellRendererDef: AgGridCellRendererDef? = null


    init {

        if (fieldType is InstantFieldType) {
            this.pipes.add(Pipes.INSTANT_PIPE)
        }

    }


    override fun build(): BlotterColumnDef {

        return BlotterColumnDef(
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


    fun header(columnHeader: String): BlotterColumnDefBuilder {

        this.columnHeader = columnHeader
        return this

    }


    fun pipes(vararg pipes: String): BlotterColumnDefBuilder {

        this.pipes.addAll(pipes)
        return this

    }


}
