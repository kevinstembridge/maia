package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.*
import org.maiaframework.gen.spec.definition.flags.WithGeneratedDto
import org.maiaframework.gen.spec.definition.flags.WithGeneratedEndpoint
import org.maiaframework.gen.spec.definition.flags.WithGeneratedFindAllFunction
import org.maiaframework.gen.spec.definition.flags.WithPreAuthorize
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.PackageName

class DtoHtmlTableDefBuilder(
    private val packageName: PackageName,
    private val dtoBaseName: DtoBaseName,
    private val fieldSupplier: (String) -> ClassFieldDef,
    private val addButtonDef: AddButtonDef? = null,
    private val disableRendering: Boolean,
    private val dataSourceType: DataSourceType,
    private val withGeneratedDto: WithGeneratedDto,
    private val withGeneratedEndpoint: WithGeneratedEndpoint,
    private val withGeneratedFindAllFunction: WithGeneratedFindAllFunction,
    private val withPreAuthorize: WithPreAuthorize?,
    private val dtoHtmlTableSourceDef: DtoHtmlTableSourceDef,
    private val searchModelType: SearchModelType
) {


    private val columnBuilders = mutableListOf<DefBuilder<out AbstractDtoHtmlTableColumnDef>>()
    private var clickableRowBuilder: ClickableRowBuilder? = null
    private var moduleName: ModuleName? = null


    fun build(): DtoHtmlTableDef {

        val fieldDefs = buildFieldDefs()
        val clickableTableRowDef = this.clickableRowBuilder?.build()

        return DtoHtmlTableDef(
            this.dtoBaseName,
            this.packageName,
            this.moduleName,
            fieldDefs,
            this.addButtonDef,
            this.disableRendering,
            this.dataSourceType,
            this.withGeneratedDto,
            this.withPreAuthorize,
            this.dtoHtmlTableSourceDef,
            clickableTableRowDef,
            withGeneratedEndpoint,
            withGeneratedFindAllFunction,
            this.searchModelType
        )

    }


    private fun buildFieldDefs(): List<AbstractDtoHtmlTableColumnDef> {

        return this.columnBuilders.asSequence().map { it.build() }.toList()

    }


    fun columnFromDto(
        fieldPathInSourceData: String,
        dtoFieldName: String? = null,
        sortable: Boolean = true,
        init: (DtoHtmlTableColumnDefBuilder.() -> Unit)? = null
    ) {

        val dtoFieldNameToUse = dtoFieldName ?: fieldPathInSourceData

        val classFieldDef = this.fieldSupplier.invoke(fieldPathInSourceData)

        val builder = DtoHtmlTableColumnDefBuilder(
            fieldPathInSourceData,
            dtoFieldNameToUse,
            classFieldDef,
            sortable
        )

        this.columnBuilders.add(builder)
        init?.invoke(builder)

    }


    fun actionColumn(
        actionName: String,
        cellRendererDef: AgGridCellRendererDef
    ): DtoHtmlTableActionColumnDefBuilder {

        val builder = DtoHtmlTableActionColumnDefBuilder(ActionName(actionName), cellRendererDef)
        this.columnBuilders.add(builder)
        return builder

    }


    fun editActionColumn(): DtoHtmlTableActionColumnDefBuilder {

        val builder = DtoHtmlTableActionColumnDefBuilder(ActionName.edit, AgGridCellRendererDefs.editAction).icon("edit").header("")
        this.columnBuilders.add(builder)
        return builder

    }


    fun deleteActionColumn(): DtoHtmlTableActionColumnDefBuilder {

        val builder = DtoHtmlTableActionColumnDefBuilder(ActionName.delete, AgGridCellRendererDefs.deleteAction).icon("delete").header("")
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
