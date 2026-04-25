package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.BlotterDef
import org.maiaframework.gen.spec.definition.BlotterEsDocSourceDef
import org.maiaframework.gen.spec.definition.BlotterSearchableDtoSourceDef

class BlotterServiceTypescriptRenderer(private val blotterDef: BlotterDef) : AbstractTypescriptRenderer() {


    private val searchResultType = when (blotterDef.blotterSourceDef) {
        is BlotterEsDocSourceDef -> "IndexSearchResult"
        is BlotterSearchableDtoSourceDef -> "SearchResultPage"
    }


    init {

        addImport("@angular/core", "inject")
        addImport("@angular/core", "Injectable")
        addImport("@angular/common/http", "HttpClient")
        addImport("@angular/common/http", "HttpHeaders")
        addImport("rxjs", "Observable")
        addImport("@maia/maia-ui", searchResultType)
        addImport(blotterDef.dtoDef.typescriptDtoImport)

    }


    override fun renderedFilePath(): String {

        return this.blotterDef.blotterComponent.serviceRenderedFilePath

    }


    override fun renderSourceBody() {

        blankLine()
        appendLine("@Injectable({providedIn: 'root'})")
        appendLine("export class ${this.blotterDef.angularBlotterServiceName} {")
        blankLine()
        appendLine("    private httpOptions = {")
        appendLine("        headers: new HttpHeaders({")
        appendLine("            'Content-Type': 'application/json'")
        appendLine("        })")
        appendLine("    };")
        blankLine()
        blankLine()
        appendLine("    private readonly http = inject(HttpClient);")
        blankLine()
        blankLine()
        appendLine("    public search(searchModel: any): Observable<$searchResultType<${this.blotterDef.dtoUqcn}>> {")
        blankLine()
        appendLine("        return this.http.post<$searchResultType<${this.blotterDef.dtoUqcn}>>(")
        appendLine("                '${this.blotterDef.searchDtoDef.searchApiUrl}',")
        appendLine("                searchModel,")
        appendLine("                this.httpOptions);")
        blankLine()
        appendLine("    }")
        blankLine()
        blankLine()
        appendLine("}")

    }


}
