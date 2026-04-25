package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.BlotterDef
import org.maiaframework.gen.spec.definition.BlotterEsDocSourceDef
import org.maiaframework.gen.spec.definition.BlotterSearchableDtoSourceDef


class AgGridDatasourceRenderer(private val blotterDef: BlotterDef) : AbstractTypescriptRenderer() {


    private val searchResultUqcn = when (blotterDef.blotterSourceDef) {
        is BlotterEsDocSourceDef -> "IndexSearchResult"
        is BlotterSearchableDtoSourceDef -> "SearchResultPage"
    }


    private val hitsOrResults = when (blotterDef.blotterSourceDef) {
        is BlotterEsDocSourceDef -> "hits"
        is BlotterSearchableDtoSourceDef -> "results"
    }


    private val totalResultsOrTotalHitCount = when (blotterDef.blotterSourceDef) {
        is BlotterEsDocSourceDef -> "totalHits.count"
        is BlotterSearchableDtoSourceDef -> "totalResultCount"
    }


    init {

        addImport("@angular/core", "inject")
        addImport("@angular/core", "Injectable")
        addImport("@angular/common/http", "HttpClient")
        addImport("ag-grid-community", "IDatasource")
        addImport("ag-grid-community", "IGetRowsParams")
        addImport(blotterDef.dtoDef.typescriptDtoImport)
        addImport("@maia/maia-ui", searchResultUqcn)

    }


    override fun renderedFilePath(): String {

        return this.blotterDef.agGridDatasourceRenderedFilePath

    }


    override fun renderSourceBody() {

        appendLine("""
            |
            |
            |@Injectable({providedIn: 'root'})
            |export class ${blotterDef.agGridDatasourceClassName} implements IDatasource {
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
            |        this.http.post<$searchResultUqcn<${blotterDef.dtoUqcn}>>(
            |            '${blotterDef.searchDtoDef.searchApiUrl}',
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
