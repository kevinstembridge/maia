package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityDetailViewDef

class EntityDetailViewComponentRenderer(
    private val entityDetailViewDef: EntityDetailViewDef
) : AbstractTypescriptRenderer() {


    private val hasDomainIdField = entityDetailViewDef.dtoDef.allFields.any { it.fieldType.isUuid }


    init {

        addImport("@angular/core", "Component")
        addImport("@angular/core", "effect")
        addImport("@angular/core", "inject")
        addImport("@angular/core", "input")
        addImport("@angular/common", "AsyncPipe")
        addImport("@angular/common", "DatePipe")
        addImport("rxjs", "Observable")
        addImport("@angular/material/progress-spinner", "MatProgressSpinner")
        if (hasDomainIdField) {
            addImport("@maia/maia-ui", "ShortId")
        }
        addImport(entityDetailViewDef.viewContentAngularComponentNames.serviceTypescriptImport)
        addImport(entityDetailViewDef.dtoDef.typescriptDtoImport)

    }


    override fun renderedFilePath(): String {

        return this.entityDetailViewDef.viewContentAngularComponentNames.componentRenderedFilePath

    }


    override fun renderSourceBody() {

        append("""
            |
            |
            |@Component({
            |    imports: [
            |        AsyncPipe,
            |        DatePipe,
            |        MatProgressSpinner${if (hasDomainIdField) ",\n            |        ShortId" else ""}
            |    ],
            |    host: {class: 'entity-detail-view'},
            |    selector: '${this.entityDetailViewDef.viewContentAngularComponentNames.componentSelector}',
            |    templateUrl: './${this.entityDetailViewDef.viewContentAngularComponentNames.htmlFileName}'
            |})
            |export class ${this.entityDetailViewDef.viewContentAngularComponentNames.componentName} {
            |
            |
            |    entityId = input.required<string>();
            |
            |
            |    detailDto$!: Observable<${this.entityDetailViewDef.dtoDef.uqcn}>;
            |
            |
            |    private readonly service = inject(${this.entityDetailViewDef.typescriptServiceName});
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
