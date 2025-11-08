package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityCreateApiDef


class EntityCreateFormScssRenderer(private val apiDef: EntityCreateApiDef) : AbstractTypescriptRenderer() {


    override fun renderedFilePath(): String {

        return apiDef.angularEntityFormScssPath

    }


    override fun renderSourceBody() {

        blankLine()
        appendLine(".mat-mdc-form-field {")
        appendLine("  width: 100%;")
        appendLine("}")

    }


}
