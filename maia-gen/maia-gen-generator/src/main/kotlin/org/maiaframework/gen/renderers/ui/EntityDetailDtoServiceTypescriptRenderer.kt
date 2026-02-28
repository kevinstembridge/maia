package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityDetailDtoDef

class EntityDetailDtoServiceTypescriptRenderer(
    private val entityDetailDtoDef: EntityDetailDtoDef
) : AbstractTypescriptRenderer() {


    init {

        addImport("@angular/core", "inject")
        addImport("@angular/core", "Injectable")
        addImport("@angular/common/http", "HttpClient")
        addImport("@angular/common/http", "HttpHeaders")
        addImport("rxjs", "Observable")
        addImport(entityDetailDtoDef.dtoDef.typescriptDtoImport)

    }


    override fun renderedFilePath(): String {

        return this.entityDetailDtoDef.componentBaseName.serviceRenderedFilePath

    }


    override fun renderSourceBody() {

        appendLine("""
            |
            |
            |@Injectable({providedIn: 'root'})
            |export class ${this.entityDetailDtoDef.componentBaseName.serviceName} {
            |
            |
            |    private httpOptions = {
            |        headers: new HttpHeaders({
            |            'Content-Type': 'application/json'
            |        })
            |    };
            |
            |
            |    private readonly http = inject(HttpClient);
            |
            |
            |    public fetch(id: string): Observable<${entityDetailDtoDef.dtoDef.uqcn}> {
            |
            |      return this.http.get<${entityDetailDtoDef.dtoDef.uqcn}>(
            |          `${entityDetailDtoDef.fetchApiUrlForTypescript}`,
            |          this.httpOptions
            |      );
            |
            |    }
            |
            |
            |}""".trimMargin())

    }


}
