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


    protected open val withFetchForEditLoading: Boolean = false


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
                |    <div mat-dialog-content style="display: flex; justify-content: center; padding: 24px;">
                |        <mat-spinner diameter="40"></mat-spinner>
                |    </div>
                |} @else {
                |""".trimMargin())
        }

        appendLine("""
            |<form [formGroup]="formGroup" novalidate (ngSubmit)="onSubmit()">
            |    <div$matDialogContentText>
            |        @if (problemDetail()) {
            |            <p class="alert alert-warning" role="alert">{{ problemDetail()!.title }}</p>
            |        }""".trimMargin())

        if (this.entityDef.multiFieldUniqueIndexDefs.isNotEmpty()) {
            appendLine("""
                |        @if (formGroup.errors?.message && (formGroup.touched || formGroup.dirty)) {
                |            {{ formGroup.errors.message }}
                |        }""".trimMargin())
        }

        formFields.forEach { formFieldDef ->
            MatFormFieldRenderer.renderFormField(formFieldDef, this)
        }

        renderManyToManyChipFields()

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

        if (this.withFetchForEditLoading) {
            appendLine("}")
        }

        return sourceCode.toString()

    }


}
