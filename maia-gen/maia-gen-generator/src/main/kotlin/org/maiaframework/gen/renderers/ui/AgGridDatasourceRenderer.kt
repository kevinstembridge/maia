package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.DtoHtmlTableDef
import org.maiaframework.gen.spec.definition.DtoHtmlTableEsDocSourceDef
import org.maiaframework.gen.spec.definition.DtoHtmlTableSearchableDtoSourceDef


class AgGridDatasourceRenderer(private val dtoHtmlTableDef: DtoHtmlTableDef) : AbstractTypescriptRenderer() {


    private val searchResultUqcn = when (dtoHtmlTableDef.dtoHtmlTableSourceDef) {
        is DtoHtmlTableEsDocSourceDef -> "IndexSearchResult"
        is DtoHtmlTableSearchableDtoSourceDef -> "SearchResultPage"
    }


    private val hitsOrResults = when (dtoHtmlTableDef.dtoHtmlTableSourceDef) {
        is DtoHtmlTableEsDocSourceDef -> "hits"
        is DtoHtmlTableSearchableDtoSourceDef -> "results"
    }


    private val totalResultsOrTotalHitCount = when (dtoHtmlTableDef.dtoHtmlTableSourceDef) {
        is DtoHtmlTableEsDocSourceDef -> "totalHits.count"
        is DtoHtmlTableSearchableDtoSourceDef -> "totalResultCount"
    }


    init {

        addImport("@angular/core", "inject")
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
            |    private readonly http = inject(HttpClient);
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
