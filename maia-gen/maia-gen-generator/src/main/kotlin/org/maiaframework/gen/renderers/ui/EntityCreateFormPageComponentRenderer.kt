package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.BlotterPageDef
import org.maiaframework.gen.spec.definition.EntityCreatePageDef


class EntityCreateFormPageComponentRenderer(
    private val entityCreatePageDef: EntityCreatePageDef,
    private val blotterPageDef: BlotterPageDef? = null,
) : AbstractTypescriptRenderer() {


    init {

        addImport("@angular/core", "ChangeDetectionStrategy")
        addImport("@angular/core", "Component")
        addImport("@angular/core", "inject")
        addImport("@angular/router", "Router")
        addImport("@app/components/page-layout/page-layout", "PageLayout")
        addImport(entityCreatePageDef.createFormAngularComponentNames.componentTypescriptImport)

    }


    override fun renderedFilePath(): String {

        return entityCreatePageDef.createPageAngularComponentNames.componentRenderedFilePath

    }


    override fun renderSourceBody() {

        append("""
            |
            |
            |@Component({
            |    changeDetection: ChangeDetectionStrategy.OnPush,
            |    imports: [
            |        PageLayout,
            |        ${entityCreatePageDef.createFormAngularComponentNames.componentName}
            |    ],
            |    selector: '${entityCreatePageDef.createPageAngularComponentNames.componentSelector}',
            |    templateUrl: './${entityCreatePageDef.createPageAngularComponentNames.htmlFileName}'
            |})
            |export class ${entityCreatePageDef.createPageAngularComponentNames.componentName} {
            |
            |
            |    private readonly router = inject(Router);
            |
            |
            |    onSaveClicked(): void {
            |        this.router.navigate(['..']);
            |    }
            |
            |
            |    onCancelClicked(): void {
            |        ${if (blotterPageDef != null) "this.router.navigate(['/${blotterPageDef.routePath}']);" else "this.router.navigate(['..']);"}
            |    }
            |
            |
            |}
            |""".trimMargin())

    }


}
