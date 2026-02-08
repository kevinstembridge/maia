package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.renderers.AbstractSourceFileRenderer
import org.maiaframework.gen.spec.definition.AngularFormFieldDef
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.flags.InlineFormOrDialog

abstract class AbstractCrudReactiveFormHtmlRenderer(
    protected val entityDef: EntityDef,
    private val inlineFormOrDialog: InlineFormOrDialog
) : AbstractSourceFileRenderer() {


    protected abstract val dialogTitle: String


    protected abstract val formFields: List<AngularFormFieldDef>


    override fun renderSource(): String {

        val matDialogContentText = when (inlineFormOrDialog) {
            InlineFormOrDialog.DIALOG -> " mat-dialog-content"
            InlineFormOrDialog.INLINE_FORM -> ""
        }

        if (inlineFormOrDialog == InlineFormOrDialog.DIALOG) {
            appendLine("<h1 mat-dialog-title>${this.dialogTitle}</h1>")
        }

        appendLine("""
            |<form [formGroup]="formGroup" novalidate (ngSubmit)="onSubmit()">
            |    <div$matDialogContentText>
            |        @if (problemDetail()) {
            |            <mat-error>
            |                <p class="alert alert-warning">{{ problemDetail()!.title }}</p>
            |            </mat-error>
            |        }""".trimMargin())

        if (this.entityDef.multiFieldUniqueIndexDefs.isNotEmpty()) {
            appendLine("""
                |        @if (formGroup.errors?.message && (formGroup.touched || formGroup.dirty)) {
                |            <mat-error>
                |                {{ formGroup.errors.message }}
                |            </mat-error>
                |        }""".trimMargin())
        }

        formFields.forEach { formFieldDef ->
            MatFormFieldRenderer.renderFormField(formFieldDef, this)
        }

        val matDialogActionsText = when (inlineFormOrDialog) {
            InlineFormOrDialog.DIALOG -> " mat-dialog-actions"
            InlineFormOrDialog.INLINE_FORM -> ""
        }

        appendLine("    </div>")
        appendLine("    <div$matDialogActionsText>")
        appendLine("        <button mat-flat-button type=\"submit\" color=\"primary\">Submit</button>")

        if (inlineFormOrDialog == InlineFormOrDialog.DIALOG) {
            appendLine("        <button mat-flat-button type=\"button\" (click)=\"onCancel()\">Cancel</button>")
        }

        appendLine("    </div>")
        appendLine("</form>")

        return sourceCode.toString()

    }


}
