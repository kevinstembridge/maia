package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.flags.GenerateFindById
import org.maiaframework.gen.spec.definition.flags.WithGeneratedDto
import org.maiaframework.gen.spec.definition.flags.WithGeneratedEndpoint
import org.maiaframework.gen.spec.definition.flags.WithGeneratedFindAllFunction
import org.maiaframework.gen.spec.definition.flags.WithGeneratedTypescriptService
import org.maiaframework.gen.spec.definition.flags.WithPreAuthorize
import org.maiaframework.gen.spec.definition.lang.*


class BlotterDef(
    val dtoBaseName: DtoBaseName,
    val packageName: PackageName,
    val moduleName: ModuleName?,
    val blotterColumnDefs: List<AbstractBlotterColumnDef>,
    val addButtonDef: AddButtonDef?,
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


    val blotterColumnFields = blotterColumnDefs.filterIsInstance<BlotterColumnDef>().sorted()


    val actionColumnFields = blotterColumnDefs.filterIsInstance<BlotterActionColumnDef>()


    val blotterComponent = AngularComponentNames(this.packageName, "${dtoBaseName}Blotter")


    val agGridDatasourceImportStatement = blotterComponent.agGridDatasourceImportStatement


    val agGridDatasourceClassName = blotterComponent.agGridDatasourceClassName


    val agGridDatasourceRenderedFilePath = blotterComponent.agGridDatasourceRenderedFilePath


    val angularBlotterServiceName = blotterComponent.serviceName


    val blotterServiceImportStatement = blotterComponent.serviceImportStatement


    private val ngMatTableComponentKebabCase = "${dtoBaseName.toKebabCase()}-table"


    val blotterScssPath = blotterComponent.componentScssRenderedFilePath


    val blotterComponentScssFileName = "${ngMatTableComponentKebabCase}.component.scss"


    private val dtoClassFields = blotterColumnFields.map { it.classFieldDef }


    private val modulePath = if (moduleName == null) "" else "${moduleName.value}/"


    val requiresCellClickedEvent: Boolean = clickableBlotterRowDef != null


    val searchDtoDef = SearchDtoDef(
        packageName,
        dtoBaseName,
        DtoSuffix("TableDto"),
        dtoClassFields,
        defaultSortModel = emptyList(),
        dataSourceType = blotterSourceDef.dataSourceType,
        searchModelType = searchModelType,
        searchApiUrl = "/api/$modulePath${dtoBaseName.toSnakeCase()}_table/search",
        countApiUrl = "/api/$modulePath${dtoBaseName.toSnakeCase()}_table/count",
        findByIdClientSideApiUrl = "/api/$modulePath${dtoBaseName.toSnakeCase()}_table/",
        findByIdServerSideApiUrl = "/api/$modulePath${dtoBaseName.toSnakeCase()}_table/{id}",
        findAllApiUrl = "/api/$modulePath${dtoBaseName.toSnakeCase()}_table/find_all",
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
                    dtoBaseName.withSuffix("Table"),
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
