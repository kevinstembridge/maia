package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityCreatePageDef


class EntityCreateFormScssRenderer(private val entityCreatePageDef: EntityCreatePageDef) : AbstractTypescriptRenderer() {


    override fun renderedFilePath(): String {

        return entityCreatePageDef.createFormAngularComponentNames.componentScssRenderedFilePath

    }


    override fun renderSourceBody() {

        blankLine()
        appendLine(".mat-mdc-form-field {")
        appendLine("  width: 100%;")
        appendLine("}")

    }


}
