package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.renderers.AbstractSourceFileRenderer
import org.maiaframework.gen.spec.definition.AngularFormDef
import org.maiaframework.gen.spec.definition.flags.InlineFormOrDialog

class FormHtmlRenderer(private val angularFormDef: AngularFormDef) : AbstractSourceFileRenderer() {


    override fun renderedFilePath(): String {

        return this.angularFormDef.formHtmlFilePath

    }


    override fun renderSource(): String {

        if (this.angularFormDef.inlineFormOrDialog == InlineFormOrDialog.DIALOG) {
            appendLine("<h1 mat-dialog-title>${this.angularFormDef.dialogTitle}</h1>")
        }

        appendLine("<form [formGroup]=\"formGroup\" (ngSubmit)=\"onSubmit()\">")

        if (this.angularFormDef.inlineFormOrDialog == InlineFormOrDialog.DIALOG) {

            append("""
                |    <div mat-dialog-content>
                |
                |        @if (problemDetail()) {
                |            <mat-error>
                |                <p class="alert alert-warning">{{ problemDetail().title }}</p>
                |            </mat-error>
                |        }
                |
                |        @if (formGroup.errors?.message && (formGroup.touched || formGroup.dirty)) {
                |            <mat-error>
                |                {{ formGroup.errors.message }}
                |            </mat-error>
                |        }
                |
                |""".trimMargin())

        }

        // TODO we might need some way of defining form-level and cross-field validators
//        if (this.angularFormDef.entityDef.entityIndexDefs.isNotEmpty()) {
//            appendLine("      <div *ngIf=\"formGroup.errors?.message && (formGroup.touched || formGroup.dirty)\" class=\"form-level-error-message alert alert-danger\">")
//            appendLine("        {{formGroup.errors.message}}")
//            appendLine("      </div>")
//        }

        this.angularFormDef.htmlFormFields.forEach { htmlFormField ->

            if (htmlFormField.classFieldDef.fieldLinkedTo != null) {

                blankLine()
                appendLine("        @if (${htmlFormField.fieldName}IsVisible()) {")
                blankLine()
                MatFormFieldRenderer.renderFormField(htmlFormField, this, indentSize = 12)
                blankLine()
                appendLine("        }")
                blankLine()

            } else {
                MatFormFieldRenderer.renderFormField(htmlFormField, this)
            }

        }

        when (this.angularFormDef.inlineFormOrDialog) {

            InlineFormOrDialog.INLINE_FORM -> {
                appendLine("""    <button mat-flat-button color="primary" type="submit" name="submit${this.angularFormDef.htmlFormName}Btn">${this.angularFormDef.submitButtonText ?: "Submit"}</button>""")
            }

            InlineFormOrDialog.DIALOG -> {

                append("""
                    |    </div>
                    |    <div mat-dialog-actions>
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
