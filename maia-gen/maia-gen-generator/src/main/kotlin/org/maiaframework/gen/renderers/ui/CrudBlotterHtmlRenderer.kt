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
            appendLine("  (addButtonClicked)=\"onAddButtonClicked()\"")
        }

        if (this.crudBlotterDef.entityCrudApiDef.deleteApiDef != null) {
            appendLine("  (delete)=\"onDelete(\$event)\"")
        }

        if (this.crudBlotterDef.entityCrudApiDef.updateApiDef != null) {
            appendLine("  (edit)=\"onEdit(\$event)\"")
        }

        appendLine("  >")
        appendLine("</${crudBlotterDef.dtoBlotterComponent.componentSelector}>")

        return sourceCode.toString()

    }


}
