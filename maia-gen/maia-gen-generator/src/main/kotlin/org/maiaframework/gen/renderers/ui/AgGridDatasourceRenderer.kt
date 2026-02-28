package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.DataSourceType
import org.maiaframework.gen.spec.definition.DtoHtmlTableDef


class AgGridDatasourceRenderer(private val dtoHtmlTableDef: DtoHtmlTableDef) : AbstractTypescriptRenderer() {


    private val searchResultUqcn = when (dtoHtmlTableDef.dataSourceType) {
        DataSourceType.ELASTIC_SEARCH -> "IndexSearchResult"
        DataSourceType.DATABASE -> "SearchResultPage"
    }

    private val hitsOrResults = when (dtoHtmlTableDef.dataSourceType) {
        DataSourceType.ELASTIC_SEARCH -> "hits"
        DataSourceType.DATABASE -> "results"
    }

    private val totalResultsOrTotalHitCount = when (dtoHtmlTableDef.dataSourceType) {
        DataSourceType.ELASTIC_SEARCH -> "totalHits.count"
        DataSourceType.DATABASE -> "totalResultCount"
    }


    init {

        addImport("@angular/core", "Injectable")
        addImport("@angular/common/http", "HttpClient")
        addImport("ag-grid-community", "IDatasource")
        addImport("ag-grid-community", "IGetRowsParams")
        addImport(dtoHtmlTableDef.dtoDef.typescriptDtoImport)
        addImport("@maia/maia-ui", searchResultUqcn)

    }


    override fun renderedFilePath(): String {

        return this.dtoHtmlTableDef.agGridDatasourceRenderedFilePath

    }


    override fun renderSourceBody() {

        appendLine("""
            |
            |
            |@Injectable({providedIn: 'root'})
            |export class ${dtoHtmlTableDef.agGridDatasourceClassName} implements IDatasource {
            |
            |
            |    rowCount?: number = undefined;
            |
            |
            |    constructor(
            |        private http: HttpClient
            |    ) { }
            |
            |
            |    getRows(params: IGetRowsParams): void {
            |
            |        this.http.post<$searchResultUqcn<${dtoHtmlTableDef.dtoUqcn}>>(
            |            '${dtoHtmlTableDef.searchDtoDef.searchApiUrl}',
            |            params
            |        ).subscribe({
            |           next: searchResultPage => params.successCallback(searchResultPage.$hitsOrResults, searchResultPage.$totalResultsOrTotalHitCount)
            |        });
            |
            |    }
            |
            |
            |}""".trimMargin())

    }


}
