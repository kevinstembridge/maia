package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityHistoryBlotterDef
import org.maiaframework.gen.spec.definition.GeneratedTypescriptDir
import org.maiaframework.gen.spec.definition.lang.TypescriptImport


class EntityHistoryBlotterServiceRenderer(
    private val def: EntityHistoryBlotterDef
) : AbstractTypescriptRenderer() {


    private val genDir = GeneratedTypescriptDir.forPackage(def.packageName)


    init {
        addImport("@angular/core", "inject")
        addImport("@angular/core", "Injectable")
        addImport("@angular/common/http", "HttpClient")
        addImport("@angular/common/http", "HttpHeaders")
        addImport("rxjs", "Observable")
        addImport("@maia/maia-ui", "SearchResultPage")
        addImport(TypescriptImport(def.tsRowDtoClassName, "@$genDir/${def.tsRowDtoClassName}"))
    }


    override fun renderedFilePath(): String {
        return def.blotterComponentNames.serviceRenderedFilePath
    }


    override fun renderSourceBody() {

        blankLine()
        appendLine("@Injectable({providedIn: 'root'})")
        appendLine("export class ${def.serviceClassName} {")
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

        if (def.isJoinEntityHistory) {

            appendLine("    public search(searchModel: any): Observable<SearchResultPage<${def.tsRowDtoClassName}>> {")
            blankLine()
            appendLine("        return this.http.post<SearchResultPage<${def.tsRowDtoClassName}>>(")
            appendLine("                `${def.searchEndpointUrlForTypescript}`,")
            appendLine("                searchModel,")
            appendLine("                this.httpOptions);")
            blankLine()
            appendLine("    }")

        } else {

            appendLine("    public search(entityId: string, searchModel: any): Observable<SearchResultPage<${def.tsRowDtoClassName}>> {")
            blankLine()
            appendLine("        return this.http.post<SearchResultPage<${def.tsRowDtoClassName}>>(")
            appendLine("                `/api/${def.entityDef.entityBaseName.toKebabCase()}/\${entityId}/history/search`,")
            appendLine("                searchModel,")
            appendLine("                this.httpOptions);")
            blankLine()
            appendLine("    }")

        }

        blankLine()
        blankLine()
        appendLine("}")

    }


}
