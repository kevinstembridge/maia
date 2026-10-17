package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.renderers.AbstractSourceFileRenderer
import org.maiaframework.gen.spec.definition.AngularFormFieldDef
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.flags.InlineFormOrDialog

abstract class AbstractCrudFormHtmlRenderer(
    protected val entityDef: EntityDef,
    private val inlineFormOrDialog: InlineFormOrDialog
) : AbstractSourceFileRenderer() {


    protected abstract val dialogTitle: String


    protected abstract val formFields: List<AngularFormFieldDef>


    protected open val withFetchForEditLoading: Boolean = false


    protected open val withCancelButton: Boolean = false


    protected open fun renderSingleFormField(formFieldDef: AngularFormFieldDef) {
        MatFormFieldRenderer.renderSignalFormField(formFieldDef, this)
    }


    // TODO MTM:
    protected open fun renderManyToManyChipFields() {}


    override fun renderSource(): String {

        val matDialogContentText = when (inlineFormOrDialog) {
            InlineFormOrDialog.DIALOG -> " mat-dialog-content"
            InlineFormOrDialog.INLINE_FORM -> ""
        }

        if (inlineFormOrDialog == InlineFormOrDialog.DIALOG) {
            appendLine("<h1 mat-dialog-title>${this.dialogTitle}</h1>")
        }

        if (this.withFetchForEditLoading) {
            append("""
                |
                |@if (loading()) {
                |    <div style="display: flex; justify-content: center; padding: 24px;">
                |        <mat-spinner diameter="40"></mat-spinner>
                |    </div>
                |} @else {
                |""".trimMargin())
        }

        appendLine($$"""
            |<form novalidate (submit)="onSubmit($event)">
            |    <div$$matDialogContentText>
            |        @if (problemDetail()) {
            |            <p class="alert alert-warning" role="alert">{{ problemDetail()!.title }}</p>
            |        }""".trimMargin())

        formFields.forEach { formFieldDef ->
            renderSingleFormField(formFieldDef)
        }

        // TODO MTM:
        renderManyToManyChipFields()

        val matDialogActionsText = when (inlineFormOrDialog) {
            InlineFormOrDialog.DIALOG -> " mat-dialog-actions"
            InlineFormOrDialog.INLINE_FORM -> ""
        }

        appendLine("    </div>")
        appendLine("    <div class=\"form-actions\"$matDialogActionsText>")
        appendLine("        <button mat-flat-button type=\"submit\" color=\"primary\">Submit</button>")

        if (inlineFormOrDialog == InlineFormOrDialog.DIALOG) {
            appendLine("        <button mat-flat-button type=\"button\" (click)=\"onCancel()\">Cancel</button>")
        } else if (withCancelButton) {
            appendLine("        <button mat-flat-button type=\"button\" (click)=\"onCancelClicked()\">Cancel</button>")
        }

        appendLine("    </div>")
        appendLine("</form>")

        if (this.withFetchForEditLoading) {
            appendLine("}")
        }

        return sourceCode.toString()

    }


}
