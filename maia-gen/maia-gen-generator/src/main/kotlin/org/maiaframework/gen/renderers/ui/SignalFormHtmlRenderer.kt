package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.renderers.AbstractSourceFileRenderer
import org.maiaframework.gen.spec.definition.AngularFormDef
import org.maiaframework.gen.spec.definition.flags.InlineFormOrDialog

/**
 * Renders the HTML of a standalone form, i.e. one that is not generated for an entity, as a signal form.
 */
class SignalFormHtmlRenderer(private val angularFormDef: AngularFormDef) : AbstractSourceFileRenderer() {


    override fun renderedFilePath(): String {

        return this.angularFormDef.formHtmlFilePath

    }


    override fun renderSource(): String {

        if (this.angularFormDef.inlineFormOrDialog == InlineFormOrDialog.DIALOG) {
            appendLine("<h1 mat-dialog-title>${this.angularFormDef.dialogTitle}</h1>")
        }

        appendLine("<form novalidate (submit)=\"onSubmit(\$event)\">")

        if (this.angularFormDef.inlineFormOrDialog == InlineFormOrDialog.DIALOG) {

            append("""
                |    <div mat-dialog-content>
                |
                |        @if (problemDetail()) {
                |            <p class="alert alert-warning" role="alert">{{ problemDetail()!.title }}</p>
                |        }
                |
                |""".trimMargin())

        }

        this.angularFormDef.htmlFormFields.forEach { htmlFormField ->

            if (htmlFormField.classFieldDef.fieldLinkedTo != null) {

                blankLine()
                appendLine("        @if (${htmlFormField.fieldName}IsVisible()) {")
                blankLine()
                MatFormFieldRenderer.renderSignalFormField(htmlFormField, this, indentSize = 12)
                blankLine()
                appendLine("        }")
                blankLine()

            } else {
                MatFormFieldRenderer.renderSignalFormField(htmlFormField, this)
            }

        }

        when (this.angularFormDef.inlineFormOrDialog) {

            InlineFormOrDialog.INLINE_FORM -> {
                appendLine("""    <button mat-flat-button color="primary" type="submit" name="submit${this.angularFormDef.htmlFormName}Btn">${this.angularFormDef.submitButtonText ?: "Submit"}</button>""")
            }

            InlineFormOrDialog.DIALOG -> {

                append("""
                    |    </div>
                    |    <div class="form-actions" mat-dialog-actions>
                    |        <button mat-flat-button color="primary" type="submit" name="submit${this.angularFormDef.htmlFormName}Btn">${this.angularFormDef.submitButtonText ?: "Submit"}</button>
                    |        <button mat-flat-button type="button" (click)="onCancel()">Cancel</button>
                    |    </div>
                    |""".trimMargin())

            }

        }

        appendLine("</form>")

        return sourceCode.toString()

    }


}
