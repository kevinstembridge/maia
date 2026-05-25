package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.AngularComponentNames

abstract class AbstractAngularComponentRenderer(
    protected val angularComponentNames: AngularComponentNames,
    protected val providerServices: List<String>
) : AbstractTypescriptRenderer() {


    val className = angularComponentNames.componentName


    override fun renderedFilePath(): String {

        return this.angularComponentNames.componentRenderedFilePath

    }


    override fun renderSourceBody() {

        renderComponentDecorator()
        renderComponentSource()

    }


    protected open fun renderComponentDecorator() {

        append("""
            |
            |
            |@Component({
            |""".trimMargin()
        )

        renderComponentImportArray()

        if (providerServices.isNotEmpty()) {
            appendLine("    providers: [")
            providerServices.distinct().forEach { serviceName ->
                appendLine("        $serviceName,")
            }
            appendLine("    ],")
        }

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
