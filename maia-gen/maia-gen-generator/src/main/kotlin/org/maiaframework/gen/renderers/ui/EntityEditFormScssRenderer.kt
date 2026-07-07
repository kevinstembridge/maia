package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityEditPageDef


class EntityEditFormScssRenderer(
    private val entityEditPageDef: EntityEditPageDef
) : AbstractTypescriptRenderer() {


    override fun renderedFilePath(): String {

        return entityEditPageDef.editFormAngularComponentNames.componentScssRenderedFilePath

    }


    override fun renderSourceBody() {

        blankLine()
        appendLine(".mat-mdc-form-field {")
        appendLine("  width: 100%;")
        appendLine("}")

        blankLine()
        appendLine("button + button {")
        appendLine("  margin-left: 8px;")
        appendLine("}")

    }


}
