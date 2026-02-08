package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityCrudApiDef

class DtoCrudServiceTypescriptRenderer(
    private val entityCrudApiDef: EntityCrudApiDef
) : AbstractTypescriptRenderer() {


    override fun renderedFilePath(): String {

        return this.entityCrudApiDef.entityDef.crudAngularComponentNames.serviceRenderedFilePath

    }


    override fun renderSourceBody() {

        appendLine("import { Injectable } from '@angular/core';")
        appendLine("import { HttpClient, HttpHeaders } from '@angular/common/http';")
        appendLine("import { Observable } from 'rxjs';")

        this.entityCrudApiDef.createApiDef?.let { apiDef ->
            appendLine(apiDef.requestDtoDef.typescriptFileImportStatement)
        }

        this.entityCrudApiDef.updateApiDef?.let { apiDef ->
            appendLine(apiDef.requestDtoDef.typescriptFileImportStatement)
        }

        if (this.entityCrudApiDef.entityDef.databaseIndexDefs.isNotEmpty()) {

            this.entityCrudApiDef.entityDef.uniqueIndexDefs.forEach { entityIndexDef ->
                appendLine(entityIndexDef.asyncValidator.asyncValidationDtoImportStatement)
            }

            appendLine("import { FormValidationResponseDto } from '@app/gen-components/common/model/FormValidationResponseDto';")

        }

        blankLine()
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
        appendLine("    constructor(")
        appendLine("        private http: HttpClient")
        appendLine("    ) {}")
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
