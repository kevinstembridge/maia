package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.AngularFormDef


class FormScssRenderer(private val formDef: AngularFormDef) : AbstractTypescriptRenderer() {


    override fun renderedFilePath(): String {

        return formDef.scssRenderedFilePath

    }


    override fun renderSourceBody() {

        blankLine()
        appendLine(".mat-mdc-form-field {")
        appendLine("  width: 100%;")
        appendLine("}")

    }


}
