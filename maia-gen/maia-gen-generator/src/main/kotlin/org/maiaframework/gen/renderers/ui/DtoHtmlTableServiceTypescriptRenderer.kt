package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.DataSourceType
import org.maiaframework.gen.spec.definition.DtoHtmlTableDef

class DtoHtmlTableServiceTypescriptRenderer(private val dtoHtmlTableDef: DtoHtmlTableDef) : AbstractTypescriptRenderer() {

    private val searchResultType = when (dtoHtmlTableDef.dataSourceType) {
        DataSourceType.DATABASE -> "SearchResultPage"
        DataSourceType.ELASTIC_SEARCH -> "IndexSearchResult"
    }


    init {

        addImport("@angular/core", "Injectable")
        addImport("@angular/common/http", "HttpClient")
        addImport("@angular/common/http", "HttpHeaders")
        addImport("rxjs", "Observable")
        addImport("@maia/maia-ui", searchResultType)
        addImport(dtoHtmlTableDef.dtoDef.typescriptDtoImport)

    }


    override fun renderedFilePath(): String {

        return this.dtoHtmlTableDef.tableComponent.serviceRenderedFilePath

    }


    override fun renderSourceBody() {

        blankLine()
        appendLine("@Injectable({providedIn: 'root'})")
        appendLine("export class ${this.dtoHtmlTableDef.angularTableServiceName} {")
        blankLine()
        appendLine("    private httpOptions = {")
        appendLine("        headers: new HttpHeaders({")
        appendLine("            'Content-Type': 'application/json'")
        appendLine("        })")
        appendLine("    };")
        blankLine()
        blankLine()
        appendLine("    constructor(private http: HttpClient) {}")
        blankLine()
        blankLine()
        appendLine("    public search(searchModel: any): Observable<$searchResultType<${this.dtoHtmlTableDef.dtoUqcn}>> {")
        blankLine()
        appendLine("        return this.http.post<$searchResultType<${this.dtoHtmlTableDef.dtoUqcn}>>(")
        appendLine("                '${this.dtoHtmlTableDef.searchDtoDef.searchApiUrl}',")
        appendLine("                searchModel,")
        appendLine("                this.httpOptions);")
        blankLine()
        appendLine("    }")
        blankLine()
        blankLine()
        appendLine("}")

    }


}
