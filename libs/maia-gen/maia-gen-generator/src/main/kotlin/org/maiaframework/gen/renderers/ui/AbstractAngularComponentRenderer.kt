package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.AngularComponentNames

abstract class AbstractAngularComponentRenderer(
    private val angularComponentNames: AngularComponentNames
) : AbstractTypescriptRenderer() {


    val className = angularComponentNames.componentName


    override fun renderedFilePath(): String {

        return this.angularComponentNames.componentRenderedFilePath

    }


    override fun renderSourceBody() {

        renderComponentDecorator()
        renderComponentSource()

    }


    protected fun renderComponentDecorator() {

        append("""
            |
            |
            |@Component({
            |""".trimMargin()
        )

        renderComponentImportArray()

        append("""
            |    selector: '${this.angularComponentNames.componentSelector}',
            |    styleUrls: ['./${this.angularComponentNames.componentScssFileName}'],
            |    templateUrl: './${this.angularComponentNames.htmlFileName}'
            |})
            |""".trimMargin()
        )

    }


    protected fun renderComponentImportArray() {

        appendLine("    imports: [")

        forEachModuleImport { import ->
            appendLine("        ${import.name},")
        }

        appendLine("    ],")

    }


    protected abstract fun renderComponentSource()


}
