package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.SearchDtoDef

class SearchDtoServiceTypescriptRenderer(private val searchDtoDef: SearchDtoDef) : AbstractTypescriptRenderer() {


    override fun renderedFilePath(): String {

        return this.searchDtoDef.angularComponentNames.serviceRenderedFilePath

    }


    override fun renderSourceBody() {

        appendLine("""
            |import { Injectable } from '@angular/core';
            |import { HttpClient, HttpHeaders } from '@angular/common/http';
            |import { Observable } from 'rxjs';
            |import { ${this.searchDtoDef.uqcn} } from './${this.searchDtoDef.uqcn}';
            |
            |
            |@Injectable({providedIn: 'root'})
            |export class ${this.searchDtoDef.angularComponentNames.serviceName} {
            |
            |
            |    private httpOptions = {
            |        headers: new HttpHeaders({
            |            'Content-Type': 'application/json'
            |        })
            |    };
            |
            |
            |    constructor(private http: HttpClient) {}
            |
            |
            |    public findById(id: string): Observable<${this.searchDtoDef.uqcn}> {
            |
            |        return this.http.get<${this.searchDtoDef.uqcn}>(
            |            '${this.searchDtoDef.findByIdClientSideApiUrl}' + id,
            |            this.httpOptions
            |        );
            |
            |    }
            |
            |
            |}""".trimMargin())

    }


}
