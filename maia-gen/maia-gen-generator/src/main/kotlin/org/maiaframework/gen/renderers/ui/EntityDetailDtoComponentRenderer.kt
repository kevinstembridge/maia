package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityDetailDtoDef

class EntityDetailDtoComponentRenderer(
    private val entityDetailDtoDef: EntityDetailDtoDef
) : AbstractTypescriptRenderer() {


    init {

        addImport("@angular/core", "Component")
        addImport("@angular/core", "effect")
        addImport("@angular/core", "inject")
        addImport("@angular/core", "input")
        addImport("@angular/common", "AsyncPipe")
        addImport("@angular/common", "DatePipe")
        addImport("rxjs", "Observable")
        addImport("@angular/material/progress-spinner", "MatProgressSpinner")
        addImport(entityDetailDtoDef.componentBaseName.serviceTypescriptImport)
        addImport(entityDetailDtoDef.dtoDef.typescriptDtoImport)

    }


    override fun renderedFilePath(): String {

        return this.entityDetailDtoDef.componentBaseName.componentRenderedFilePath

    }


    override fun renderSourceBody() {

        appendLine("""
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
            |    private readonly service = inject(${this.entityDetailDtoDef.componentBaseName.serviceName});
            |
            |
            |    constructor() {
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
