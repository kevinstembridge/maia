package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityDetailDtoDef

class EntityDetailDtoServiceTypescriptRenderer(
    private val entityDetailDtoDef: EntityDetailDtoDef
) : AbstractTypescriptRenderer() {


    override fun renderedFilePath(): String {

        return this.entityDetailDtoDef.componentBaseName.serviceRenderedFilePath

    }


    override fun renderSourceBody() {

        appendLine("""
            |import {Injectable} from '@angular/core';
            |import {HttpClient, HttpHeaders} from '@angular/common/http';
            |import {Observable} from 'rxjs';
            |${entityDetailDtoDef.dtoDef.typescriptDtoImportStatement}
            |
            |
            |@Injectable({providedIn: 'root'})
            |export class ${this.entityDetailDtoDef.componentBaseName.serviceName} {
            |
            |
            |    private httpOptions = {
            |        headers: new HttpHeaders({
            |            'Content-Type': 'application/json'
            |        })
            |    };
            |
            |
            |    constructor(
            |        private http: HttpClient
            |    ) {}
            |
            |
            |    public fetch(id: string): Observable<${entityDetailDtoDef.dtoDef.uqcn}> {
            |
            |      return this.http.get<${entityDetailDtoDef.dtoDef.uqcn}>(
            |          `${entityDetailDtoDef.fetchApiUrlForTypescript}`,
            |          this.httpOptions
            |      );
            |
            |    }
            |
            |
            |}""".trimMargin())

    }


}
