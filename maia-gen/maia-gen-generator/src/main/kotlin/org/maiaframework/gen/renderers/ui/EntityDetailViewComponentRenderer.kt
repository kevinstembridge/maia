package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityDetailViewDef

class EntityDetailViewComponentRenderer(
    private val entityDetailViewDef: EntityDetailViewDef
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
        addImport(entityDetailViewDef.angularComponentNames.serviceTypescriptImport)
        addImport(entityDetailViewDef.dtoDef.typescriptDtoImport)

    }


    override fun renderedFilePath(): String {

        return this.entityDetailViewDef.angularComponentNames.componentRenderedFilePath

    }


    override fun renderSourceBody() {

        append("""
            |
            |
            |@Component({
            |    imports: [
            |        AsyncPipe,
            |        DatePipe,
            |        MatProgressSpinner
            |    ],
            |    selector: '${this.entityDetailViewDef.angularComponentNames.componentSelector}',
            |    templateUrl: './${this.entityDetailViewDef.angularComponentNames.htmlFileName}'
            |})
            |export class ${this.entityDetailViewDef.angularComponentNames.componentName} {
            |
            |
            |    entityId = input.required<string>();
            |
            |
            |    detailDto$!: Observable<${this.entityDetailViewDef.dtoDef.uqcn}>;
            |
            |
            |    private readonly service = inject(${this.entityDetailViewDef.angularComponentNames.serviceName});
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
            |}
            |""".trimMargin())

    }


}
