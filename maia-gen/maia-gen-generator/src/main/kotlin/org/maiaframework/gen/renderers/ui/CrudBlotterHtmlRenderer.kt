package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.renderers.AbstractSourceFileRenderer
import org.maiaframework.gen.spec.definition.CrudBlotterDef

class CrudBlotterHtmlRenderer(private val crudBlotterDef: CrudBlotterDef): AbstractSourceFileRenderer() {


    override fun renderedFilePath(): String {

        return this.crudBlotterDef.htmlRenderedFilePath

    }


    override fun renderSource(): String {

        appendLine("<${crudBlotterDef.dtoBlotterComponent.componentSelector}")

        if (this.crudBlotterDef.blotterDef.addButtonDef != null) {
            appendLine("""    (addButtonClicked)="onAddButtonClicked()"""")
        }

        this.crudBlotterDef.blotterDef.actionColumnFields.sortedBy { it.actionName.value }.forEach { actionColumnDef ->
            appendLine($$"""    ($${actionColumnDef.actionName})="on$${actionColumnDef.actionName.firstToUpper()}($event)"""")
        }

        appendLine("/>")

        return sourceCode.toString()

    }


}
