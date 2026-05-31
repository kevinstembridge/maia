package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.renderers.AbstractSourceFileRenderer
import org.maiaframework.gen.spec.definition.AngularFormFieldDef
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.flags.InlineFormOrDialog

abstract class AbstractCrudReactiveFormHtmlRenderer(
    protected val entityDef: EntityDef,
    private val inlineFormOrDialog: InlineFormOrDialog,
    protected open val chipFields: List<ManyToManyChipFieldDef> = emptyList()
) : AbstractSourceFileRenderer() {


    protected abstract val dialogTitle: String


    protected abstract val formFields: List<AngularFormFieldDef>


    protected open val withFetchForEditLoading: Boolean = false


    protected open val withCancelButton: Boolean = false


    protected open fun renderManyToManyChipFields() {

        chipFields.forEach { chip ->

            val chipGridRefName = "${chip.fieldName}EntityChipGrid"

            append("""
                |        <mat-form-field appearance="outline">
                |            <mat-label>${chip.labelText}</mat-label>
                |            <mat-chip-grid #$chipGridRefName>
                |                @for (entity of ${chip.selectedFieldName}; track entity.${chip.esDocIdFieldName}) {
                |                    <mat-chip-row (removed)="${chip.removeMethodName}(entity)">
                |                        {{ entity.${chip.searchTermFieldName} }}
                |                        <button matChipRemove type="button"><mat-icon>cancel</mat-icon></button>
                |                    </mat-chip-row>
                |                }
                |            </mat-chip-grid>
                |            <input
                |                #${chip.inputRefName}
                |                placeholder="${chip.searchPlaceholder}"
                |                [formControl]="${chip.searchControlFieldName}"
                |                [matChipInputFor]="$chipGridRefName"
                |                [matAutocomplete]="${chip.autocompleteRefName}"
                |            />
                |            <mat-autocomplete #${chip.autocompleteRefName}="matAutocomplete" (optionSelected)="${chip.addMethodName}(${'$'}event)">
                |                @if (${chip.filteredIsLoadingFieldName}()) {
                |                    <mat-option disabled>Loading...</mat-option>
                |                }
                |                @for (option of ${chip.filteredFieldName}; track option.${chip.esDocIdFieldName}) {
                |                    <mat-option [value]="option">{{ option.${chip.searchTermFieldName} }}</mat-option>
                |                }
                |            </mat-autocomplete>
                |        </mat-form-field>
                |""".trimMargin())

        }

    }


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

        append("""
            |<form [formGroup]="formGroup" novalidate (ngSubmit)="onSubmit()">
            |    <div$matDialogContentText>
            |        @if (problemDetail()) {
            |            <p class="alert alert-warning" role="alert">{{ problemDetail()!.title }}</p>
            |        }
            |""".trimMargin())

        if (this.entityDef.multiFieldUniqueIndexDefs.isNotEmpty()) {
            append("""
                |        @if (formGroup.errors?.message && (formGroup.touched || formGroup.dirty)) {
                |            {{ formGroup.errors.message }}
                |        }
                |""".trimMargin())
        }

        formFields.forEach { formFieldDef ->
            MatFormFieldRenderer.renderFormField(formFieldDef, this)
        }

        renderManyToManyChipFields()

        appendLine("    </div>")

        val matDialogActionsText = when (inlineFormOrDialog) {
            InlineFormOrDialog.DIALOG -> " mat-dialog-actions"
            InlineFormOrDialog.INLINE_FORM -> ""
        }

        appendLine("    <div$matDialogActionsText>")
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
