package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.BlotterPageDef

class CrudBlotterPageComponentRenderer(
    private val blotterPageDef: BlotterPageDef
) : AbstractTypescriptRenderer() {


    init {
        addImport("@angular/core", "ChangeDetectionStrategy")
        addImport("@angular/core", "Component")
        addImport("@app/components/page-layout/page-layout", "PageLayout")
        addImport(blotterPageDef.blotterComponentTypescriptImport)
    }


    override fun renderedFilePath(): String {

        return blotterPageDef.pageAngularComponentNames.componentRenderedFilePath

    }


    override fun renderSourceBody() {

        append("""
            |
            |@Component({
            |    changeDetection: ChangeDetectionStrategy.OnPush,
            |    imports: [
            |        PageLayout,
            |        ${blotterPageDef.blotterComponentClassName}
            |    ],
            |    selector: '${blotterPageDef.pageAngularComponentNames.componentSelector}',
            |    templateUrl: './${blotterPageDef.pageAngularComponentNames.htmlFileName}'
            |})
            |export class ${blotterPageDef.pageAngularComponentNames.componentName} {}
            |""".trimMargin())

    }


}
