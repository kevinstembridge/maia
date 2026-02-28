package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.AngularFormDef

class AngularFormServiceRenderer(private val angularFormDef: AngularFormDef) : AbstractTypescriptRenderer() {


    init {

        addImport("@angular/core", "inject")
        addImport("@angular/core", "Injectable")
        addImport("@angular/common/http", "HttpClient")
        addImport("@angular/common/http", "HttpHeaders")
        addImport("rxjs", "Observable")
        addImport("rxjs", "of")
        addImport(angularFormDef.requestDtoDef.typescriptImport)

    }


    override fun renderedFilePath(): String {

        return this.angularFormDef.angularServiceRenderedFilePath

    }


    override fun renderSourceBody() {

        blankLine()
        appendLine("@Injectable({providedIn: 'root'})")
        appendLine("export class ${this.angularFormDef.formServiceClassName} {")
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
        appendLine("    public sendRequest(requestDto: ${this.angularFormDef.requestDtoDef.uqcn}): Observable<void> {")
        blankLine()
        appendLine("        return this.http.post<void>(")
        appendLine("                '${this.angularFormDef.requestDtoDef.requestMappingPath}',")
        appendLine("                requestDto,")
        appendLine("                this.httpOptions);")
        blankLine()
        appendLine("    }")
        blankLine()
        blankLine()
        appendLine("}")

    }


}
