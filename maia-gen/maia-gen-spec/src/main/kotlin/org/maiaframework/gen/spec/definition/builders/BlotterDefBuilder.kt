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
    private val addButtonDef: AddButtonDef? = null,
    private val disableRendering: Boolean,
    private val withGeneratedDto: WithGeneratedDto,
    private val withGeneratedEndpoint: WithGeneratedEndpoint,
    private val withGeneratedTypescriptService: WithGeneratedTypescriptService,
    private val withGeneratedFindAllFunction: WithGeneratedFindAllFunction,
    private val withPreAuthorize: WithPreAuthorize?,
    private val blotterSourceDef: BlotterSourceDef,
    private val searchModelType: SearchModelType
) {


    private val columnBuilders = mutableListOf<DefBuilder<out AbstractBlotterColumnDef>>()
    private var clickableRowBuilder: ClickableRowBuilder? = null
    private var moduleName: ModuleName? = null


    fun build(): BlotterDef {

        val fieldDefs = buildFieldDefs()
        val clickableTableRowDef = this.clickableRowBuilder?.build()

        return BlotterDef(
            this.dtoBaseName,
            this.packageName,
            this.moduleName,
            fieldDefs,
            this.addButtonDef,
            this.disableRendering,
            this.withGeneratedDto,
            this.withPreAuthorize,
            this.blotterSourceDef,
            clickableTableRowDef,
            withGeneratedEndpoint,
            withGeneratedTypescriptService,
            withGeneratedFindAllFunction,
            this.searchModelType
        )

    }


    private fun buildFieldDefs(): List<AbstractBlotterColumnDef> {

        return this.columnBuilders.asSequence().map { it.build() }.toList()

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

        this.columnBuilders.add(builder)
        init?.invoke(builder)

    }


    fun actionColumn(
        actionName: String,
        cellRendererDef: AgGridCellRendererDef
    ): BlotterActionColumnDefBuilder {

        val builder = BlotterActionColumnDefBuilder(ActionName(actionName), cellRendererDef)
        this.columnBuilders.add(builder)
        return builder

    }


    fun editActionColumn(): BlotterActionColumnDefBuilder {

        val builder = BlotterActionColumnDefBuilder(ActionName.edit, AgGridCellRendererDefs.editAction).icon("edit").header("")
        this.columnBuilders.add(builder)
        return builder

    }


    fun deleteActionColumn(): BlotterActionColumnDefBuilder {

        val builder = BlotterActionColumnDefBuilder(ActionName.delete, AgGridCellRendererDefs.deleteAction).icon("delete").header("")
        this.columnBuilders.add(builder)
        return builder

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
