package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityDetailDtoDef

class EntityDetailDtoComponentRenderer(
    private val entityDetailDtoDef: EntityDetailDtoDef
) : AbstractTypescriptRenderer() {


    override fun renderedFilePath(): String {

        return this.entityDetailDtoDef.componentBaseName.componentRenderedFilePath

    }


    override fun renderSourceBody() {

        appendLine("""
            |import {Component, effect, input} from '@angular/core';
            |import {AsyncPipe, DatePipe} from '@angular/common';
            |import {Observable} from 'rxjs';
            |import {MatProgressSpinner} from '@angular/material/progress-spinner';
            |${entityDetailDtoDef.componentBaseName.serviceImportStatement}
            |${entityDetailDtoDef.dtoDef.typescriptDtoImportStatement}
            |
            |
            |@Component({
            |    imports: [
            |        AsyncPipe,
            |        DatePipe,
            |        MatProgressSpinner
            |    ],
            |    selector: '${this.entityDetailDtoDef.componentBaseName.componentSelector}',
            |    templateUrl: './${this.entityDetailDtoDef.componentBaseName.htmlFileName}'
            |})
            |export class ${this.entityDetailDtoDef.componentBaseName.componentName} {
            |
            |
            |    entityId = input.required<string>();
            |
            |
            |    detailDto$: Observable<${this.entityDetailDtoDef.dtoDef.uqcn}>;
            |
            |
            |    constructor(
            |        private service: ${this.entityDetailDtoDef.componentBaseName.serviceName}
            |    ) {
            |
            |        effect(() => {
            |            this.detailDto$ = this.service.fetch(this.entityId());
            |        });
            |
            |    }
            |
            |
            |}""".trimMargin())

    }


}
