package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.renderers.AbstractSourceFileRenderer
import org.maiaframework.gen.spec.definition.AngularFormFieldDef
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.flags.InlineFormOrDialog
import org.maiaframework.gen.spec.definition.lang.DoubleFieldType
import org.maiaframework.gen.spec.definition.lang.IntFieldType
import org.maiaframework.gen.spec.definition.lang.IntTypeFieldType
import org.maiaframework.gen.spec.definition.lang.IntValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.LongFieldType
import org.maiaframework.gen.spec.definition.lang.LongTypeFieldType
import org.maiaframework.lang.text.StringFunctions

abstract class AbstractCrudReactiveFormHtmlRenderer(
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
        MatFormFieldRenderer.renderFormField(formFieldDef, this)
    }


    protected open fun renderManyToManyTimestampedFields() {

        timestampedFields.forEach { field ->

            append("""
                |        <div class="join-entries">
                |            @for (join of ${field.joinsFieldName}; track join.entityId) {
                |                <div class="join-entry">
                |                    <span>{{ join.entityName }}</span>
                |""".trimMargin())

            if (!field.isManagedBySystem) {
                append("""
                |                    <mat-form-field appearance="outline">
                |                        <mat-label>Effective From Date</mat-label>
                |                        <input matInput class="join-effective-from-date" [matDatepicker]="effectiveFromDatePicker"
                |                            [(ngModel)]="join.effectiveFrom" [ngModelOptions]="{standalone: true}" />
                |                        <mat-datepicker-toggle matIconSuffix [for]="effectiveFromDatePicker"></mat-datepicker-toggle>
                |                        <mat-datepicker #effectiveFromDatePicker></mat-datepicker>
                |                    </mat-form-field>
                |                    <mat-form-field appearance="outline">
                |                        <mat-label>Effective From Time</mat-label>
                |                        <input matInput class="join-effective-from-time" [matTimepicker]="effectiveFromTimePicker"
                |                            [(ngModel)]="join.effectiveFrom" [ngModelOptions]="{standalone: true}" />
                |                        <mat-timepicker #effectiveFromTimePicker></mat-timepicker>
                |                        <mat-timepicker-toggle matSuffix [for]="effectiveFromTimePicker"></mat-timepicker-toggle>
                |                    </mat-form-field>
                |                    <mat-form-field appearance="outline">
                |                        <mat-label>Effective To Date</mat-label>
                |                        <input matInput class="join-effective-to-date" [matDatepicker]="effectiveToDatePicker"
                |                            [(ngModel)]="join.effectiveTo" [ngModelOptions]="{standalone: true}" />
                |                        <mat-datepicker-toggle matIconSuffix [for]="effectiveToDatePicker"></mat-datepicker-toggle>
                |                        <mat-datepicker #effectiveToDatePicker></mat-datepicker>
                |                    </mat-form-field>
                |                    <mat-form-field appearance="outline">
                |                        <mat-label>Effective To Time</mat-label>
                |                        <input matInput class="join-effective-to-time" [matTimepicker]="effectiveToTimePicker"
                |                            [(ngModel)]="join.effectiveTo" [ngModelOptions]="{standalone: true}" />
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
                |                <mat-form-field appearance="outline">
                |                    <mat-label>${field.labelText}</mat-label>
                |                    <input
                |                        matInput
                |                        [formControl]="${field.addEntityControlName}"
                |                        [matAutocomplete]="${field.autocompleteRefName}"
                |                        placeholder="${field.searchPlaceholder}"
                |                    />
                |                    <mat-autocomplete #${field.autocompleteRefName}="matAutocomplete" [displayWith]="${field.displayWithMethodName}">
                |                        @if (${field.filteredIsLoadingFieldName}()) {
                |                            <mat-option disabled>Loading...</mat-option>
                |                        }
                |                        @for (option of ${field.filteredFieldName}; track option.${field.esDocIdFieldName}) {
                |                            <mat-option [value]="option">{{ option.${field.searchTermFieldName} }}</mat-option>
                |                        }
                |                    </mat-autocomplete>
                |                </mat-form-field>
                |""".trimMargin())

            if (!field.isManagedBySystem) {
                append("""
                |                <mat-form-field appearance="outline">
                |                    <mat-label>Effective From Date</mat-label>
                |                    <input matInput [matDatepicker]="effectiveFromPicker${field.fieldName}"
                |                        [formControl]="${field.effectiveFromControlName}" />
                |                    <mat-datepicker-toggle matIconSuffix [for]="effectiveFromPicker${field.fieldName}"></mat-datepicker-toggle>
                |                    <mat-datepicker #effectiveFromPicker${field.fieldName}></mat-datepicker>
                |                </mat-form-field>
                |                <mat-form-field appearance="outline">
                |                    <mat-label>Effective From Time</mat-label>
                |                    <input matInput [matTimepicker]="effectiveFromTimepicker${field.fieldName}"
                |                        [formControl]="${field.effectiveFromControlName}" />
                |                    <mat-timepicker #effectiveFromTimepicker${field.fieldName}></mat-timepicker>
                |                    <mat-timepicker-toggle matSuffix [for]="effectiveFromTimepicker${field.fieldName}"></mat-timepicker-toggle>
                |                </mat-form-field>
                |                <mat-form-field appearance="outline">
                |                    <mat-label>Effective To Date</mat-label>
                |                    <input matInput [matDatepicker]="effectiveToPicker${field.fieldName}"
                |                        [formControl]="${field.effectiveToControlName}" />
                |                    <mat-datepicker-toggle matIconSuffix [for]="effectiveToPicker${field.fieldName}"></mat-datepicker-toggle>
                |                    <mat-datepicker #effectiveToPicker${field.fieldName}></mat-datepicker>
                |                </mat-form-field>
                |                <mat-form-field appearance="outline">
                |                    <mat-label>Effective To Time</mat-label>
                |                    <input matInput [matTimepicker]="effectiveToTimepicker${field.fieldName}"
                |                        [formControl]="${field.effectiveToControlName}" />
                |                    <mat-timepicker #effectiveToTimepicker${field.fieldName}></mat-timepicker>
                |                    <mat-timepicker-toggle matSuffix [for]="effectiveToTimepicker${field.fieldName}"></mat-timepicker-toggle>
                |                </mat-form-field>
                |""".trimMargin())
            }

            extraTimestampedJoinHtmlFields(field).forEach { extra ->
                append("""
                    |                <mat-form-field appearance="outline">
                    |                    <mat-label>${extra.label}</mat-label>
                    |                    <input matInput type="${extra.inputType}" [formControl]="${extra.controlName}" />
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


    private data class ExtraJoinHtmlField(val label: String, val controlName: String, val inputType: String)


    private fun extraTimestampedJoinHtmlFields(field: ManyToManyTimestampedFieldDef): List<ExtraJoinHtmlField> {

        val knownFieldNames = setOf("id", field.joinEntityIdFieldName, "effectiveFrom", "effectiveTo")
        val controlPrefix = field.effectiveFromControlName.removeSuffix("EffectiveFromControl")

        return field.joinRequestDtoDef.classFieldDefs
            .filterNot { it.classFieldName.value in knownFieldNames }
            .map { f ->
                val label = StringFunctions.toTitleCase(f.classFieldName.value)
                val controlName = "$controlPrefix${StringFunctions.firstToUpper(f.classFieldName.value)}Control"
                val inputType = when (f.fieldType) {
                    is IntFieldType, is IntTypeFieldType, is IntValueClassFieldType,
                    is LongFieldType, is LongTypeFieldType, is DoubleFieldType -> "number"
                    else -> "text"
                }
                ExtraJoinHtmlField(label, controlName, inputType)
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
            renderSingleFormField(formFieldDef)
        }

        renderManyToManyChipFields()
        renderManyToManyTimestampedFields()

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
