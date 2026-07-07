package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.flags.GenerateFindById
import org.maiaframework.gen.spec.definition.flags.SearchModelType
import org.maiaframework.gen.spec.definition.flags.WithGeneratedDto
import org.maiaframework.gen.spec.definition.flags.WithGeneratedEndpoint
import org.maiaframework.gen.spec.definition.flags.WithGeneratedFindAllFunction
import org.maiaframework.gen.spec.definition.flags.WithGeneratedTypescriptService
import org.maiaframework.gen.spec.definition.flags.WithPreAuthorize
import org.maiaframework.gen.spec.definition.lang.Nullability
import org.maiaframework.gen.spec.definition.lang.PackageName


class BlotterDef(
    val dtoBaseName: DtoBaseName,
    val packageName: PackageName,
    val moduleName: ModuleName?,
    providedBlotterColumnDefs: List<AbstractBlotterColumnDef>,
    val entityCreatePageDef: EntityCreatePageDef?,
    val entityDetailViewDef: EntityDetailViewDef?,
    val entityEditPageDef: EntityEditPageDef?,
    val disableRendering: Boolean,
    val withGeneratedDto: WithGeneratedDto,
    val withPreAuthorize: WithPreAuthorize? = null,
    val blotterSourceDef: BlotterSourceDef,
    val clickableBlotterRowDef: ClickableTableRowDef?,
    withGeneratedEndpoint: WithGeneratedEndpoint,
    withGeneratedTypescriptService: WithGeneratedTypescriptService,
    withGeneratedFindAllFunction: WithGeneratedFindAllFunction,
    val searchModelType: SearchModelType
) {


    val angularDeleteDialogComponentNames = blotterSourceDef.deleteDialogComponentNames


    val checkForeignKeyReferencesDialogComponentNames = blotterSourceDef.checkForeignKeyReferencesDialogComponentNames


    val blotterColumnDefs = initBlotterColumnDefs(providedBlotterColumnDefs)



    private fun initBlotterColumnDefs(provided: List<AbstractBlotterColumnDef>): List<AbstractBlotterColumnDef> {

        val fields = mutableListOf<AbstractBlotterColumnDef>()

        when (blotterSourceDef) {
            is BlotterEsDocSourceDef -> TODO()
            is BlotterSearchableDtoSourceDef -> {

                if (blotterSourceDef.searchableDtoDef.dtoRootEntityDef.hasCompositePrimaryKey) {

                    fields.add(BlotterCompositePkColumnDef())

                } else if (blotterSourceDef.searchableDtoDef.dtoRootEntityDef.primaryKeyFields.size == 1) {

                    val pkField = blotterSourceDef.searchableDtoDef.dtoRootEntityDef.primaryKeyFields.first()

                    if (provided.none { it.colId == pkField.classFieldName.value }) {

                        val fieldPath = FieldPath.of(pkField.classFieldName.value)

                        val blotterColumnDef = BlotterColumnDef(
                            fieldPathInSourceData = fieldPath,
                            dtoFieldName = pkField.classFieldName.value,
                            columnHeader = "ID",
                            isSortable = false,
                            isFilterable = false,
                            fieldType = pkField.fieldType,
                            nullability = Nullability.NOT_NULLABLE,
                            hide = true,
                            providedAgGridCellDataType = null,
                            cellRenderer = null,
                            pipes = emptyList()
                        )

                        fields.add(blotterColumnDef)

                    }

                }

            }
        }

        return fields + provided

    }


    val blotterColumnFields = blotterColumnDefs.filterIsInstance<BlotterColumnDef>().sorted()


    val actionColumnFields = blotterColumnDefs.filterIsInstance<BlotterActionColumnDef>()


    val blotterComponent = AngularComponentNames(this.packageName, "${dtoBaseName}Blotter")


    val agGridDatasourceImportStatement = blotterComponent.agGridDatasourceImportStatement


    val agGridDatasourceClassName = blotterComponent.agGridDatasourceClassName


    val agGridDatasourceRenderedFilePath = blotterComponent.agGridDatasourceRenderedFilePath


    val angularBlotterServiceName = blotterComponent.serviceName


    val blotterServiceImportStatement = blotterComponent.serviceImportStatement


    private val ngMatTableComponentKebabCase = "${dtoBaseName.toKebabCase()}-blotter"


    val blotterScssPath = blotterComponent.componentScssRenderedFilePath


    val blotterComponentScssFileName = "${ngMatTableComponentKebabCase}.component.scss"


    val dtoClassFields = blotterColumnDefs
        .mapNotNull { when (it) {
            is BlotterActionColumnDef -> null
            is BlotterColumnDef -> it.classFieldDef
            is BlotterCompositePkColumnDef -> it.classFieldDef
        } }


    private val modulePath = if (moduleName == null) "" else "${moduleName.value}/"


    val hasViewActionColumn = actionColumnFields.any { it.actionName == ActionName.view }


    val hasEditActionColumn = actionColumnFields.any { it.actionName == ActionName.edit }


    val hasDeleteActionColumn = actionColumnFields.any { it.actionName == ActionName.delete }


    val hasDateTimeStringColumn = blotterColumnFields
        .any { it.agGridCellDateType == BlotterColumnDef.AgGridCellDataType.dateTimeString }


    val requiresCellClickedEvent: Boolean = clickableBlotterRowDef != null


    val searchDtoDef = SearchDtoDef(
        packageName,
        dtoBaseName,
        DtoSuffix("BlotterRowDto"),
        dtoClassFields,
        defaultSortModel = emptyList(),
        dataSourceType = blotterSourceDef.dataSourceType,
        searchModelType = searchModelType,
        searchApiUrl = "/api/$modulePath${dtoBaseName.toKebabCase()}-blotter/search",
        countApiUrl = "/api/$modulePath${dtoBaseName.toKebabCase()}-blotter/count",
        findByIdClientSideApiUrl = "/api/$modulePath${dtoBaseName.toKebabCase()}-blotter/",
        findByIdServerSideApiUrl = "/api/$modulePath${dtoBaseName.toKebabCase()}-blotter/{id}",
        findAllApiUrl = "/api/$modulePath${dtoBaseName.toKebabCase()}-blotter/find-all",
        withGeneratedFindAllFunction,
        GenerateFindById.FALSE,
        withGeneratedEndpoint,
        withGeneratedTypescriptService
    )


    val searchableDtoDef = when (blotterSourceDef) {
        is BlotterEsDocSourceDef -> null
        is BlotterSearchableDtoSourceDef ->
            blotterSourceDef.searchableDtoDef.let { searchableDtoDef ->

                val fields = blotterColumnFields.map { blotterColumnDef ->

                    val pathInSourceData = blotterColumnDef.fieldPathInSourceData
                    val fieldName = pathInSourceData.head()
                    searchableDtoDef.findSearchableDtoFieldByName(fieldName)
                        .copyWithFieldName(blotterColumnDef.dtoFieldName)

                }

                SearchableDtoDef(
                    searchableDtoDef.dtoRootEntityDef,
                    dtoBaseName.withSuffix("BlotterRow"),
                    moduleName,
                    packageName,
                    searchableDtoDef.tableName,
                    fields,
                    searchableDtoDef.withPreAuthorize,
                    withGeneratedEndpoint,
                    withGeneratedTypescriptService,
                    withGeneratedFindAllFunction,
                    withGeneratedDto,
                    GenerateFindById.FALSE,
                    searchModelType,
                    searchableDtoDef.withProvidedFieldConverter,
                    manyToManyJoinEntityDefs = searchableDtoDef.manyToManyJoinEntityDefs
                )

            }
    }


    val dtoDef = searchDtoDef.dtoDef


    val dtoImportStatement = this.dtoDef.typescriptDtoImportStatement


    val dtoUqcn = dtoDef.uqcn


}
