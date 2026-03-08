package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityCrudApiDef

class DtoCrudServiceTypescriptRenderer(
    private val entityCrudApiDef: EntityCrudApiDef
) : AbstractTypescriptRenderer() {


    init {

        addImport("@angular/core", "inject")
        addImport("@angular/core", "Injectable")
        addImport("@angular/common/http", "HttpClient")
        addImport("@angular/common/http", "HttpHeaders")
        addImport("rxjs", "Observable")

        this.entityCrudApiDef.createApiDef?.let { addImport(it.requestDtoDef.typescriptImport) }
        this.entityCrudApiDef.updateApiDef?.let { addImport(it.requestDtoDef.typescriptImport) }
        this.entityCrudApiDef.entityDef.fetchForEditDtoDef?.let { addImport(it.typescriptImport) }

        if (this.entityCrudApiDef.entityDef.databaseIndexDefs.isNotEmpty()) {
            this.entityCrudApiDef.entityDef.uniqueIndexDefs.forEach { entityIndexDef ->
                addImport(entityIndexDef.asyncValidator.asyncValidationDtoTypescriptImport)
            }
            addImport("@maia/maia-ui", "FormValidationResponseDto")
        }

    }


    override fun renderedFilePath(): String {

        return this.entityCrudApiDef.entityDef.crudAngularComponentNames.serviceRenderedFilePath

    }


    override fun renderSourceBody() {

        blankLine()
        appendLine("@Injectable({providedIn: 'root'})")
        appendLine("export class ${this.entityCrudApiDef.entityDef.crudAngularComponentNames.serviceName} {")
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

        this.entityCrudApiDef.createApiDef?.let { apiDef ->

            val createRequestDtoDef = apiDef.requestDtoDef

            appendLine("    public create(requestDto: ${createRequestDtoDef.uqcn}): Observable<void> {")
            blankLine()
            appendLine("        return this.http.post<void>(")
            appendLine("                '${apiDef.endpointUrl}',")
            appendLine("                requestDto,")
            appendLine("                this.httpOptions);")
            blankLine()
            appendLine("    }")
        }

        this.entityCrudApiDef.entityDef.uniqueIndexDefs.filter { it.withExistsEndpoint }.forEach { entityIndexDef ->

            blankLine()
            blankLine()
            appendLine("    public ${entityIndexDef.existsByFunctionName}(requestBody: ${entityIndexDef.asyncValidator.asyncValidationDtoName}): Observable<FormValidationResponseDto> {")
            blankLine()
            appendLine("        return this.http.post<FormValidationResponseDto>('${entityIndexDef.existsByUrl}', requestBody, this.httpOptions);")
            blankLine()
            appendLine("    }")

        }

        this.entityCrudApiDef.updateApiDef?.let { apiDef ->

            val requestDtoDef = apiDef.requestDtoDef

            blankLine()
            blankLine()
            appendLine("    public edit(dto: ${requestDtoDef.uqcn}): Observable<void> {")
            blankLine()
            appendLine("        return this.http.put<void>(")
            appendLine("                '${apiDef.endpointUrl}',")
            appendLine("                dto,")
            appendLine("                this.httpOptions);")
            blankLine()
            appendLine("    }")

        }

        this.entityCrudApiDef.entityDef.fetchForEditDtoDef?.let { fetchForEditDtoDef ->

            blankLine()
            blankLine()
            appendLine("    public fetchForEdit(id: string): Observable<${fetchForEditDtoDef.uqcn}> {")
            blankLine()
            appendLine("        return this.http.get<${fetchForEditDtoDef.uqcn}>('${fetchForEditDtoDef.endpointUrl}/' + id, this.httpOptions);")
            blankLine()
            appendLine("    }")

        }

        this.entityCrudApiDef.deleteApiDef?.let { apiDef ->

            blankLine()
            blankLine()
            appendLine("    public delete(id: string): Observable<any> {")
            blankLine()
            appendLine("        return this.http.delete('${apiDef.endpointUrl}' + id, this.httpOptions);")
            blankLine()
            appendLine("    }")
        }

        blankLine()
        blankLine()
        appendLine("}")

    }


}
