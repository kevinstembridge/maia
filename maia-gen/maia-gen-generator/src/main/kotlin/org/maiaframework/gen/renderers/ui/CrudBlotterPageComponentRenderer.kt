package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.CrudBlotterPageDef

class CrudBlotterPageComponentRenderer(
    private val crudBlotterPageDef: CrudBlotterPageDef
) : AbstractTypescriptRenderer() {


    init {
        addImport("@angular/core", "ChangeDetectionStrategy")
        addImport("@angular/core", "Component")
        addImport("@app/components/page-layout/page-layout", "PageLayout")
        addImport(crudBlotterPageDef.crudBlotterComponentTypescriptImport)
    }


    override fun renderedFilePath(): String {

        return crudBlotterPageDef.pageAngularComponentNames.componentRenderedFilePath

    }


    override fun renderSourceBody() {

        append("""
            |
            |@Component({
            |    changeDetection: ChangeDetectionStrategy.OnPush,
            |    imports: [
            |        PageLayout,
            |        ${crudBlotterPageDef.crudBlotterComponentClassName}
            |    ],
            |    selector: '${crudBlotterPageDef.pageAngularComponentNames.componentSelector}',
            |    templateUrl: './${crudBlotterPageDef.pageAngularComponentNames.htmlFileName}'
            |})
            |export class ${crudBlotterPageDef.pageAngularComponentNames.componentName} {}
            |""".trimMargin())

    }


}
