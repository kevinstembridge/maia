package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityHistoryBlotterDef
import org.maiaframework.gen.spec.definition.GeneratedTypescriptDir
import org.maiaframework.gen.spec.definition.lang.TypescriptImport


class EntityHistoryBlotterAgGridDatasourceRenderer(
    private val def: EntityHistoryBlotterDef
) : AbstractTypescriptRenderer() {


    private val genDir = GeneratedTypescriptDir.forPackage(def.packageName)


    init {
        addImport("@angular/core", "inject")
        addImport("@angular/core", "Injectable")
        addImport("@angular/common/http", "HttpClient")
        addImport("ag-grid-community", "IDatasource")
        addImport("ag-grid-community", "IGetRowsParams")
        addImport("@maia/maia-ui", "SearchResultPage")
        addImport(TypescriptImport(def.tsRowDtoClassName, "@$genDir/${def.tsRowDtoClassName}"))
    }


    override fun renderedFilePath(): String {
        return "$genDir/${def.datasourceClassName}.ts"
    }


    override fun renderSourceBody() {

        append("""
            |
            |
            |@Injectable()
            |export class ${def.datasourceClassName} implements IDatasource {
            |
            |
            |    rowCount?: number = undefined;
            |
            |
            |    private entityId!: string;
            |
            |
            |    private readonly http = inject(HttpClient);
            |
            |
            |    setEntityId(id: string): void {
            |        this.entityId = id;
            |    }
            |
            |
            |    getRows(params: IGetRowsParams): void {
            |
            |        this.http.post<SearchResultPage<${def.tsRowDtoClassName}>>(
            |            `${def.searchEndpointUrlForTypescript}`,
            |            params
            |        ).subscribe({
            |           next: searchResultPage => params.successCallback(searchResultPage.results, searchResultPage.totalResultCount)
            |        });
            |
            |    }
            |
            |
            |}
            |""".trimMargin())

    }


}
