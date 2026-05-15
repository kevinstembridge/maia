package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityDetailViewDef

class EntityDetailDtoServiceTypescriptRenderer(
    private val entityDetailViewDef: EntityDetailViewDef
) : AbstractTypescriptRenderer() {


    init {

        addImport("@angular/core", "inject")
        addImport("@angular/core", "Injectable")
        addImport("@angular/common/http", "HttpClient")
        addImport("@angular/common/http", "HttpHeaders")
        addImport("rxjs", "Observable")
        addImport(entityDetailViewDef.dtoDef.typescriptDtoImport)

    }


    override fun renderedFilePath(): String {

        return this.entityDetailViewDef.componentNames.serviceRenderedFilePath

    }


    override fun renderSourceBody() {

        append("""
            |
            |
            |@Injectable({providedIn: 'root'})
            |export class ${this.entityDetailViewDef.componentNames.serviceName} {
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
            |    public fetch(id: string): Observable<${entityDetailViewDef.dtoDef.uqcn}> {
            |
            |      return this.http.get<${entityDetailViewDef.dtoDef.uqcn}>(
            |          `${entityDetailViewDef.fetchApiUrlForTypescript}`,
            |          this.httpOptions
            |      );
            |
            |    }
            |
            |
            |}
            |""".trimMargin())

    }


}
