package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityCreateApiDef


class EntityCreateDialogScssRenderer(private val apiDef: EntityCreateApiDef) : AbstractTypescriptRenderer() {


    override fun renderedFilePath(): String {

        return apiDef.angularDialogScssPath

    }


    override fun renderSourceBody() {

        blankLine()
        appendLine(".mat-mdc-form-field {")
        appendLine("  width: 100%;")
        appendLine("}")

    }


}
