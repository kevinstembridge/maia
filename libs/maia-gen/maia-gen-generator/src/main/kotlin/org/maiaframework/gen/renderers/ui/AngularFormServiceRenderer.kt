package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.AngularFormDef

class AngularFormServiceRenderer(private val angularFormDef: AngularFormDef) : AbstractTypescriptRenderer() {


    override fun renderedFilePath(): String {

        return this.angularFormDef.angularServiceRenderedFilePath

    }


    override fun renderSourceBody() {

        appendLine("import {Injectable} from '@angular/core';")
        appendLine("import {HttpClient, HttpHeaders} from '@angular/common/http';")
        appendLine("import {Observable, of} from 'rxjs';")
        appendLine(this.angularFormDef.requestDtoDef.typescriptFileImportStatement)
        blankLine()
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
        appendLine("    constructor(")
        appendLine("        private http: HttpClient,")
        appendLine("    ) {}")
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
