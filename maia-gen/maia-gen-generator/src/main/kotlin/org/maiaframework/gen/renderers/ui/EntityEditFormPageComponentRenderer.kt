package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityEditPageDef


class EntityEditFormPageComponentRenderer(
    private val entityEditPageDef: EntityEditPageDef
) : AbstractTypescriptRenderer() {


    init {

        addImport("@angular/core", "ChangeDetectionStrategy")
        addImport("@angular/core", "Component")
        addImport("@angular/core", "inject")
        addImport("@angular/core/rxjs-interop", "toSignal")
        addImport("@angular/router", "ActivatedRoute")
        addImport("@angular/router", "Router")
        addImport("rxjs", "map")
        addImport("@app/components/page-layout/page-layout", "PageLayout")
        addImport(entityEditPageDef.editFormAngularComponentNames.componentTypescriptImport)

    }


    override fun renderedFilePath(): String {

        return entityEditPageDef.editFormPageAngularComponentNames.componentRenderedFilePath

    }


    override fun renderSourceBody() {

        append("""
            |
            |
            |@Component({
            |    changeDetection: ChangeDetectionStrategy.OnPush,
            |    imports: [
            |        PageLayout,
            |        ${entityEditPageDef.editFormAngularComponentNames.componentName}
            |    ],
            |    selector: '${entityEditPageDef.editFormPageAngularComponentNames.componentSelector}',
            |    templateUrl: './${entityEditPageDef.editFormPageAngularComponentNames.htmlFileName}'
            |})
            |export class ${entityEditPageDef.editFormPageAngularComponentNames.componentName} {
            |
            |
            |    private readonly route = inject(ActivatedRoute);
            |
            |
            |    private readonly router = inject(Router);
            |
            |
            |    protected readonly entityId = toSignal(
            |        this.route.paramMap.pipe(
            |            map(p => p.get('id'))
            |        )
            |    );
            |
            |
            |    onSaveClicked(): void {
            |        const id = this.entityId();
            |        if (id) {
            |            this.router.navigate(['${entityEditPageDef.viewPageUrl}', id]);
            |        }
            |    }
            |
            |
            |    onCancelClicked(): void {
            |        const id = this.entityId();
            |        if (id) {
            |            this.router.navigate(['${entityEditPageDef.viewPageUrl}', id]);
            |        }
            |    }
            |
            |
            |}
            |""".trimMargin())

    }


}
