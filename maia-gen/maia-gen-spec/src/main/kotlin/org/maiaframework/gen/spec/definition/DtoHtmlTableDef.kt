package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.flags.GenerateFindById
import org.maiaframework.gen.spec.definition.flags.WithGeneratedDto
import org.maiaframework.gen.spec.definition.flags.WithGeneratedEndpoint
import org.maiaframework.gen.spec.definition.flags.WithGeneratedFindAllFunction
import org.maiaframework.gen.spec.definition.flags.WithGeneratedTypescriptService
import org.maiaframework.gen.spec.definition.flags.WithPreAuthorize
import org.maiaframework.gen.spec.definition.lang.*


class DtoHtmlTableDef(
    val dtoBaseName: DtoBaseName,
    val packageName: PackageName,
    val moduleName: ModuleName?,
    val dtoHtmlTableColumnDefs: List<AbstractDtoHtmlTableColumnDef>,
    val addButtonDef: AddButtonDef?,
    val disableRendering: Boolean,
    val withGeneratedDto: WithGeneratedDto,
    val withPreAuthorize: WithPreAuthorize? = null,
    val dtoHtmlTableSourceDef: DtoHtmlTableSourceDef,
    val clickableTableRowDef: ClickableTableRowDef?,
    withGeneratedEndpoint: WithGeneratedEndpoint,
    withGeneratedTypescriptService: WithGeneratedTypescriptService,
    withGeneratedFindAllFunction: WithGeneratedFindAllFunction,
    val searchModelType: SearchModelType
) {


    val dtoHtmlTableColumnFields = dtoHtmlTableColumnDefs.filterIsInstance<DtoHtmlTableColumnDef>().sorted()


    val actionColumnFields = dtoHtmlTableColumnDefs.filterIsInstance<DtoHtmlTableActionColumnDef>()


    val tableComponent = AngularComponentNames(this.packageName, "${dtoBaseName}Table")


    val agGridDatasourceImportStatement = tableComponent.agGridDatasourceImportStatement


    val agGridDatasourceClassName = tableComponent.agGridDatasourceClassName


    val agGridDatasourceRenderedFilePath = tableComponent.agGridDatasourceRenderedFilePath


    val angularTableServiceName = tableComponent.serviceName


    val tableServiceImportStatement = tableComponent.serviceImportStatement


    private val ngMatTableComponentKebabCase = "${dtoBaseName.toKebabCase()}-table"


    val tableScssPath = tableComponent.componentScssRenderedFilePath


    val tableComponentScssFileName = "${ngMatTableComponentKebabCase}.component.scss"


    private val dtoClassFields = dtoHtmlTableColumnFields.map { it.classFieldDef }


    private val modulePath = if (moduleName == null) "" else "${moduleName.value}/"


    val requiresCellClickedEvent: Boolean = clickableTableRowDef != null


    val searchDtoDef = SearchDtoDef(
        packageName,
        dtoBaseName,
        DtoSuffix("TableDto"),
        dtoClassFields,
        defaultSortModel = emptyList(),
        dataSourceType = dtoHtmlTableSourceDef.dataSourceType,
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


    val searchableDtoDef = when (dtoHtmlTableSourceDef) {
        is DtoHtmlTableEsDocSourceDef -> null
        is DtoHtmlTableSearchableDtoSourceDef ->
            dtoHtmlTableSourceDef.searchableDtoDef.let { searchableDtoDef ->

                val fields = dtoHtmlTableColumnFields.map { dtoHtmlTableColumnDef ->

                    val pathInSourceData = dtoHtmlTableColumnDef.fieldPathInSourceData
                    val fieldName = pathInSourceData.head()
                    searchableDtoDef.findSearchableDtoFieldByName(fieldName)
                        .copyWithFieldName(dtoHtmlTableColumnDef.dtoFieldName)

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
