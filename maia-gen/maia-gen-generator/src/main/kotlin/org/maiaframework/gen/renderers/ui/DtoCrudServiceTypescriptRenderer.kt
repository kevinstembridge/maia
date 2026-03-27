package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityCrudApiDef
import org.maiaframework.gen.spec.definition.lang.IntFieldType
import org.maiaframework.gen.spec.definition.lang.LongFieldType

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

        val indexDefsWithExistsEndpoint = this.entityCrudApiDef.entityDef.uniqueIndexDefs.filter { it.withExistsEndpoint }
        if (indexDefsWithExistsEndpoint.isNotEmpty()) {
            indexDefsWithExistsEndpoint.forEach { entityIndexDef ->
                addImport(entityIndexDef.asyncValidator.asyncValidationDtoTypescriptImport)
            }
            addImport("@maia/maia-ui", "FormValidationResponseDto")
        }

    }


    override fun renderedFilePath(): String {

        return this.entityCrudApiDef.entityDef.crudAngularComponentNames.serviceRenderedFilePath

    }


    override fun renderSourceBody() {

        append("""
            |
            |@Injectable({providedIn: 'root'})
            |export class ${this.entityCrudApiDef.entityDef.crudAngularComponentNames.serviceName} {
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
            |""".trimMargin())

        this.entityCrudApiDef.createApiDef?.let { apiDef ->

            val createRequestDtoDef = apiDef.requestDtoDef

            append("""
                |    public create(requestDto: ${createRequestDtoDef.uqcn}): Observable<void> {
                |
                |        return this.http.post<void>(
                |                '${apiDef.endpointUrl}',
                |                requestDto,
                |                this.httpOptions);
                |
                |    }
                |""".trimMargin())
        }

        this.entityCrudApiDef.entityDef.uniqueIndexDefs.filter { it.withExistsEndpoint }.forEach { entityIndexDef ->

            append("""
                |
                |
                |    public ${entityIndexDef.existsByFunctionName}(requestBody: ${entityIndexDef.asyncValidator.asyncValidationDtoName}): Observable<FormValidationResponseDto> {
                |
                |        return this.http.post<FormValidationResponseDto>('${entityIndexDef.existsByUrl}', requestBody, this.httpOptions);
                |
                |    }
                |""".trimMargin())

        }

        this.entityCrudApiDef.updateApiDef?.let { apiDef ->

            val requestDtoDef = apiDef.requestDtoDef

            append("""
                |
                |
                |    public edit(dto: ${requestDtoDef.uqcn}): Observable<void> {
                |
                |        return this.http.put<void>(
                |                '${apiDef.endpointUrl}',
                |                dto,
                |                this.httpOptions);
                |
                |    }
                |""".trimMargin())

        }

        this.entityCrudApiDef.entityDef.fetchForEditDtoDef?.let { fetchForEditDtoDef ->

            val entityDef = this.entityCrudApiDef.entityDef
            if (entityDef.hasCompositePrimaryKey) {

                val pkType = entityDef.primaryKeyClassFields.joinToString(", ") { field ->
                    val tsType = if (field.fieldType is IntFieldType || field.fieldType is LongFieldType) "number" else "string"
                    "${field.classFieldName.value}: $tsType"
                }

                append("""
                    |
                    |
                    |    public fetchForEdit(pk: {$pkType}): Observable<${fetchForEditDtoDef.uqcn}> {
                    |
                    |        return this.http.post<${fetchForEditDtoDef.uqcn}>(
                    |                '${fetchForEditDtoDef.endpointUrl}',
                    |                pk,
                    |                this.httpOptions);
                    |
                    |    }
                    |""".trimMargin())

            } else {

                append("""
                    |
                    |
                    |    public fetchForEdit(id: string): Observable<${fetchForEditDtoDef.uqcn}> {
                    |
                    |        return this.http.get<${fetchForEditDtoDef.uqcn}>('${fetchForEditDtoDef.endpointUrl}/' + id, this.httpOptions);
                    |
                    |    }
                    |""".trimMargin())

            }

        }

        this.entityCrudApiDef.deleteApiDef?.let { apiDef ->

            val entityDef = this.entityCrudApiDef.entityDef
            if (entityDef.hasCompositePrimaryKey) {

                val pkType = entityDef.primaryKeyClassFields.joinToString(", ") { field ->
                    val tsType = if (field.fieldType is IntFieldType || field.fieldType is LongFieldType) "number" else "string"
                    "${field.classFieldName.value}: $tsType"
                }
                val deleteBaseUrl = apiDef.endpointUrl.trimEnd('/')

                append("""
                    |
                    |
                    |    public delete(pk: {$pkType}): Observable<any> {
                    |
                    |        return this.http.delete('$deleteBaseUrl', {
                    |            headers: new HttpHeaders({'Content-Type': 'application/json'}),
                    |            body: pk
                    |        });
                    |
                    |    }
                    |""".trimMargin())

            } else {

                append("""
                    |
                    |
                    |    public delete(id: string): Observable<any> {
                    |
                    |        return this.http.delete('${apiDef.endpointUrl}' + id, this.httpOptions);
                    |
                    |    }
                    |""".trimMargin())

            }
        }

        append("""
            |
            |
            |}
            |""".trimMargin())

    }


}
