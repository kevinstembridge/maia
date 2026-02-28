package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.SearchDtoDef

class SearchDtoServiceTypescriptRenderer(private val searchDtoDef: SearchDtoDef) : AbstractTypescriptRenderer() {


    init {

        addImport("@angular/core", "Injectable")
        addImport("@angular/common/http", "HttpClient")
        addImport("@angular/common/http", "HttpHeaders")
        addImport("rxjs", "Observable")
        addImport(searchDtoDef.dtoDef.typescriptDtoImport)

    }


    override fun renderedFilePath(): String {

        return this.searchDtoDef.angularComponentNames.serviceRenderedFilePath

    }


    override fun renderSourceBody() {

        appendLine("""
            |
            |
            |@Injectable({providedIn: 'root'})
            |export class ${this.searchDtoDef.angularComponentNames.serviceName} {
            |
            |
            |    private httpOptions = {
            |        headers: new HttpHeaders({
            |            'Content-Type': 'application/json'
            |        })
            |    };
            |
            |
            |    constructor(private http: HttpClient) {}
            |
            |
            |    public findById(id: string): Observable<${this.searchDtoDef.uqcn}> {
            |
            |        return this.http.get<${this.searchDtoDef.uqcn}>(
            |            '${this.searchDtoDef.findByIdClientSideApiUrl}' + id,
            |            this.httpOptions
            |        );
            |
            |    }
            |
            |
            |}""".trimMargin())

    }


}
