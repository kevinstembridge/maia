package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityUpdateApiDef


class EntityEditDialogScssRenderer(private val apiDef: EntityUpdateApiDef) : AbstractTypescriptRenderer() {


    override fun renderedFilePath(): String {

        return apiDef.angularDialogComponentNames.componentScssRenderedFilePath

    }


    override fun renderSourceBody() {

        blankLine()
        appendLine(".mat-mdc-form-field {")
        appendLine("  width: 100%;")
        appendLine("}")

    }


}
