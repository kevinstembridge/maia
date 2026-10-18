package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.renderers.AbstractSourceFileRenderer
import org.maiaframework.gen.spec.definition.AngularFormFieldDef
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.flags.InlineFormOrDialog

abstract class AbstractCrudFormHtmlRenderer(
    protected val entityDef: EntityDef,
    private val inlineFormOrDialog: InlineFormOrDialog,
    protected open val chipFields: List<ManyToManyChipFieldDef> = emptyList(),
    protected open val timestampedFields: List<ManyToManyTimestampedFieldDef> = emptyList()
) : AbstractSourceFileRenderer() {


    protected abstract val dialogTitle: String


    protected abstract val formFields: List<AngularFormFieldDef>


    protected open val withFetchForEditLoading: Boolean = false


    protected open val withCancelButton: Boolean = false


    protected open fun renderSingleFormField(formFieldDef: AngularFormFieldDef) {
        MatFormFieldRenderer.renderSignalFormField(formFieldDef, this)
    }


    protected open fun renderManyToManyChipFields() {

        chipFields.forEach { chip ->

            append("""
                |        <${chip.typeaheadDef.angularComponentSelector}
                |            chips
                |            label="${chip.labelText}"
                |            placeholder="${chip.searchPlaceholder}"
                |            [selected]="${chip.selectedFieldName}()"
                |            (added)="${chip.addMethodName}(${'$'}event)"
                |            (removed)="${chip.removeMethodName}(${'$'}event)"
                |        ></${chip.typeaheadDef.angularComponentSelector}>
                |""".trimMargin())

        }

    }


    protected open fun renderManyToManyTimestampedFields() {

        timestampedFields.forEach { field ->

            append("""
                |        <div class="join-entries">
                |            @for (join of ${field.joinsFormFieldName}; track join) {
                |                <div class="join-entry">
                |                    <span>{{ join().value().entityName }}</span>
                |""".trimMargin())

            if (!field.isManagedBySystem) {
                append("""
                |                    <mat-form-field appearance="outline">
                |                        <mat-label>Effective From Date</mat-label>
                |                        <input matInput class="join-effective-from-date" [matDatepicker]="effectiveFromDatePicker"
                |                            [formField]="join.effectiveFrom" />
                |                        <mat-datepicker-toggle matIconSuffix [for]="effectiveFromDatePicker"></mat-datepicker-toggle>
                |                        <mat-datepicker #effectiveFromDatePicker></mat-datepicker>
                |                    </mat-form-field>
                |                    <mat-form-field appearance="outline">
                |                        <mat-label>Effective From Time</mat-label>
                |                        <input matInput class="join-effective-from-time" [matTimepicker]="effectiveFromTimePicker"
                |                            [formField]="join.effectiveFrom" />
                |                        <mat-timepicker #effectiveFromTimePicker></mat-timepicker>
                |                        <mat-timepicker-toggle matSuffix [for]="effectiveFromTimePicker"></mat-timepicker-toggle>
                |                    </mat-form-field>
                |                    <mat-form-field appearance="outline">
                |                        <mat-label>Effective To Date</mat-label>
                |                        <input matInput class="join-effective-to-date" [matDatepicker]="effectiveToDatePicker"
                |                            [formField]="join.effectiveTo" />
                |                        <mat-datepicker-toggle matIconSuffix [for]="effectiveToDatePicker"></mat-datepicker-toggle>
                |                        <mat-datepicker #effectiveToDatePicker></mat-datepicker>
                |                    </mat-form-field>
                |                    <mat-form-field appearance="outline">
                |                        <mat-label>Effective To Time</mat-label>
                |                        <input matInput class="join-effective-to-time" [matTimepicker]="effectiveToTimePicker"
                |                            [formField]="join.effectiveTo" />
                |                        <mat-timepicker #effectiveToTimePicker></mat-timepicker>
                |                        <mat-timepicker-toggle matSuffix [for]="effectiveToTimePicker"></mat-timepicker-toggle>
                |                    </mat-form-field>
                |""".trimMargin())
            }

            append("""
                |                    <button mat-icon-button type="button" class="join-remove-button" (click)="${field.removeMethodName}(${'$'}index)">
                |                        <mat-icon>delete</mat-icon>
                |                    </button>
                |                </div>
                |            }
                |        </div>
                |        <button mat-stroked-button type="button" (click)="${field.showFormSignalName}.set(true)">
                |            <mat-icon>add</mat-icon> Add ${field.labelText}
                |        </button>
                |        @if (${field.showFormSignalName}()) {
                |            <div class="join-mini-form">
                |                <${field.typeaheadDef.angularComponentSelector}
                |                    [formField]="${field.addFormFieldName}.entity"
                |                    label="${field.labelText}"
                |                    placeholder="${field.searchPlaceholder}"
                |                ></${field.typeaheadDef.angularComponentSelector}>
                |""".trimMargin())

            if (!field.isManagedBySystem) {
                append("""
                |                <mat-form-field appearance="outline">
                |                    <mat-label>Effective From Date</mat-label>
                |                    <input matInput [matDatepicker]="effectiveFromPicker${field.fieldName}"
                |                        [formField]="${field.addFormFieldName}.effectiveFrom" />
                |                    <mat-datepicker-toggle matIconSuffix [for]="effectiveFromPicker${field.fieldName}"></mat-datepicker-toggle>
                |                    <mat-datepicker #effectiveFromPicker${field.fieldName}></mat-datepicker>
                |                </mat-form-field>
                |                <mat-form-field appearance="outline">
                |                    <mat-label>Effective From Time</mat-label>
                |                    <input matInput [matTimepicker]="effectiveFromTimepicker${field.fieldName}"
                |                        [formField]="${field.addFormFieldName}.effectiveFrom" />
                |                    <mat-timepicker #effectiveFromTimepicker${field.fieldName}></mat-timepicker>
                |                    <mat-timepicker-toggle matSuffix [for]="effectiveFromTimepicker${field.fieldName}"></mat-timepicker-toggle>
                |                </mat-form-field>
                |                <mat-form-field appearance="outline">
                |                    <mat-label>Effective To Date</mat-label>
                |                    <input matInput [matDatepicker]="effectiveToPicker${field.fieldName}"
                |                        [formField]="${field.addFormFieldName}.effectiveTo" />
                |                    <mat-datepicker-toggle matIconSuffix [for]="effectiveToPicker${field.fieldName}"></mat-datepicker-toggle>
                |                    <mat-datepicker #effectiveToPicker${field.fieldName}></mat-datepicker>
                |                </mat-form-field>
                |                <mat-form-field appearance="outline">
                |                    <mat-label>Effective To Time</mat-label>
                |                    <input matInput [matTimepicker]="effectiveToTimepicker${field.fieldName}"
                |                        [formField]="${field.addFormFieldName}.effectiveTo" />
                |                    <mat-timepicker #effectiveToTimepicker${field.fieldName}></mat-timepicker>
                |                    <mat-timepicker-toggle matSuffix [for]="effectiveToTimepicker${field.fieldName}"></mat-timepicker-toggle>
                |                </mat-form-field>
                |""".trimMargin())
            }

            field.extraJoinFields.forEach { extra ->
                append("""
                    |                <mat-form-field appearance="outline">
                    |                    <mat-label>${extra.label}</mat-label>
                    |                    <input matInput type="${extra.htmlInputType}" [formField]="${field.addFormFieldName}.${extra.name}" />
                    |                </mat-form-field>
                    |""".trimMargin())
            }

            append("""
                |                <button mat-flat-button type="button" (click)="${field.confirmMethodName}()">Add</button>
                |                <button mat-flat-button type="button" (click)="${field.cancelMethodName}()">Cancel</button>
                |            </div>
                |        }
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

        renderManyToManyChipFields()
        renderManyToManyTimestampedFields()

        val matDialogActionsText = when (inlineFormOrDialog) {
            InlineFormOrDialog.DIALOG -> " mat-dialog-actions"
            InlineFormOrDialog.INLINE_FORM -> ""
        }

        appendLine($$"""
            |        <!-- TEMPORARY: form validation state, for debugging -->
            |        <pre class="form-debug" style="font-size: 12px; background: #f5f5f5; padding: 8px;">valid: {{ dialogForm().valid() }}
            |invalid: {{ dialogForm().invalid() }}
            |pending: {{ dialogForm().pending() }}
            |touched: {{ dialogForm().touched() }}
            |@for (error of dialogForm().errorSummary(); track $index) {
            |{{ error.fieldTree().name() }}: {{ error.kind }}{{ error.message ? ' - ' + error.message : '' }}
            |}</pre>""".trimMargin())
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
