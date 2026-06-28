package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.GeneratedTypescriptDir
import org.maiaframework.gen.spec.definition.TimelineBlotterDef
import org.maiaframework.gen.spec.definition.lang.TypescriptImport


class TimelineBlotterAgGridDatasourceRenderer(
    private val def: TimelineBlotterDef
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

        blankLine()
        blankLine()
        appendLine("@Injectable()")
        appendLine("export class ${def.datasourceClassName} implements IDatasource {")
        blankLine()
        blankLine()
        appendLine("    rowCount?: number = undefined;")
        blankLine()
        blankLine()
        appendLine("    private entityId!: string;")
        blankLine()
        blankLine()
        appendLine("    private readonly http = inject(HttpClient);")
        blankLine()
        blankLine()
        appendLine("    setEntityId(id: string): void {")
        appendLine("        this.entityId = id;")
        appendLine("    }")
        blankLine()
        blankLine()
        appendLine("    getRows(params: IGetRowsParams): void {")
        blankLine()
        appendLine("        this.http.post<SearchResultPage<${def.tsRowDtoClassName}>>(")
        appendLine("            `${def.searchEndpointUrlForTypescript}`,")
        appendLine("            params")
        appendLine("        ).subscribe({")
        appendLine("           next: searchResultPage => params.successCallback(searchResultPage.results, searchResultPage.totalResultCount)")
        appendLine("        });")
        blankLine()
        appendLine("    }")
        blankLine()
        blankLine()
        appendLine("}")

    }


}
