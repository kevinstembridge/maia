package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.*
import org.maiaframework.gen.spec.definition.flags.WithGeneratedDto
import org.maiaframework.gen.spec.definition.flags.WithGeneratedEndpoint
import org.maiaframework.gen.spec.definition.flags.WithGeneratedFindAllFunction
import org.maiaframework.gen.spec.definition.flags.WithGeneratedTypescriptService
import org.maiaframework.gen.spec.definition.flags.WithPreAuthorize
import org.maiaframework.gen.spec.definition.lang.PackageName

@MaiaDslMarker
class BlotterDefBuilder(
    private val packageName: PackageName,
    private val dtoBaseName: DtoBaseName,
    private val entityCreatePageDef: EntityCreatePageDef?,
    private val entityEditPageDef: EntityEditPageDef?,
    private val entityDetailViewDef: EntityDetailViewDef?,
    private val disableRendering: Boolean,
    private val withGeneratedDto: WithGeneratedDto,
    private val withGeneratedEndpoint: WithGeneratedEndpoint,
    private val withGeneratedTypescriptService: WithGeneratedTypescriptService,
    private val withGeneratedFindAllFunction: WithGeneratedFindAllFunction,
    private val withPreAuthorize: WithPreAuthorize?,
    private val blotterSourceDef: BlotterSourceDef,
    private val searchModelType: SearchModelType
) {


    private val columnDefs = mutableListOf<AbstractBlotterColumnDef>()


    private var clickableRowBuilder: ClickableRowBuilder? = null


    private var moduleName: ModuleName? = null


    fun build(): BlotterDef {

        val clickableTableRowDef = this.clickableRowBuilder?.build()

        return BlotterDef(
            this.dtoBaseName,
            this.packageName,
            this.moduleName,
            this.columnDefs,
            this.entityCreatePageDef,
            this.entityDetailViewDef,
            this.entityEditPageDef,
            this.disableRendering,
            this.withGeneratedDto,
            this.withPreAuthorize,
            this.blotterSourceDef,
            clickableTableRowDef,
            this.withGeneratedEndpoint,
            this.withGeneratedTypescriptService,
            this.withGeneratedFindAllFunction,
            this.searchModelType
        )

    }


    fun columnFromDto(
        fieldPathInSourceData: String,
        dtoFieldName: String? = null,
        sortable: Boolean = true,
        init: (BlotterColumnDefBuilder.() -> Unit)? = null
    ) {

        val fieldPath = FieldPath.of(fieldPathInSourceData)

        val dtoFieldNameToUse = dtoFieldName ?: fieldPathInSourceData

        val classFieldDef = this.blotterSourceDef.findFieldByPath(fieldPath)

        val builder = BlotterColumnDefBuilder(
            fieldPath,
            dtoFieldNameToUse,
            classFieldDef,
            sortable
        )

        classFieldDef.displayName?.let { builder.header(it.value) }

        init?.invoke(builder)
        this.columnDefs.add(builder.build())

    }


    fun actionColumn(
        actionName: String,
        cellRendererDef: AgGridCellRendererDef
    ) {

        val builder = BlotterActionColumnDefBuilder(ActionName(actionName), cellRendererDef)
        this.columnDefs.add(builder.build())

    }


    fun editActionColumn(): BlotterActionColumnDefBuilder {

        val builder = BlotterActionColumnDefBuilder(ActionName.edit, AgGridCellRendererDefs.editAction).icon("edit").header("")
        this.columnDefs.add(builder.build())
        return builder

    }


    fun viewActionColumn() {

        val builder = BlotterActionColumnDefBuilder(ActionName.view, AgGridCellRendererDefs.viewAction).icon("visibility").header("")
        this.columnDefs.add(builder.build())

    }


    fun deleteActionColumn() {

        val builder = BlotterActionColumnDefBuilder(ActionName.delete, AgGridCellRendererDefs.deleteAction).icon("delete").header("")
        this.columnDefs.add(builder.build())

    }


    fun moduleName(moduleName: String) {

        this.moduleName = ModuleName.of(moduleName)

    }


    fun clickableRow(configure: ClickableRowBuilder.() -> Unit) {

        if (this.clickableRowBuilder != null) {
            throw IllegalStateException("clickableRowBuilder has already been initialized")
        }

        val clickableRowBuilder = ClickableRowBuilder()
        clickableRowBuilder.configure()
        this.clickableRowBuilder = clickableRowBuilder

    }


    @MaiaDslMarker
    class ClickableRowBuilder {


        private var routerNavigationArgs: List<String>? = null


        fun routerNavigate(vararg navigationArgs: String) {

            this.routerNavigationArgs = navigationArgs.toList()

        }


        fun build(): ClickableTableRowDef {

            return ClickableTableRowDef(this.routerNavigationArgs)

        }


    }


}
