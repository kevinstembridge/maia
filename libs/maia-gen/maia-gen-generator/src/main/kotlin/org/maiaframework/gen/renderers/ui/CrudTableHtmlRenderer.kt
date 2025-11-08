package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.renderers.AbstractSourceFileRenderer
import org.maiaframework.gen.spec.definition.CrudTableDef

class CrudTableHtmlRenderer(private val crudTableDef: CrudTableDef): AbstractSourceFileRenderer() {


    override fun renderedFilePath(): String {

        return this.crudTableDef.htmlRenderedFilePath

    }


    override fun renderSource(): String {

        appendLine("<${crudTableDef.dtoTableComponent.componentSelector}")

        if (this.crudTableDef.dtoHtmlTableDef.addButtonDef != null) {
            appendLine("  (addButtonClicked)=\"onAddButtonClicked()\"")
        }

        if (this.crudTableDef.entityCrudApiDef.deleteApiDef != null) {
            appendLine("  (delete)=\"onDelete(\$event)\"")
        }

        if (this.crudTableDef.entityCrudApiDef.updateApiDef != null) {
            appendLine("  (edit)=\"onEdit(\$event)\"")
        }

        appendLine("  >")
        appendLine("</${crudTableDef.dtoTableComponent.componentSelector}>")

        return sourceCode.toString()

    }


}
