package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.renderers.AbstractSourceRenderer
import org.maiaframework.gen.spec.definition.AngularFormFieldDef
import org.maiaframework.gen.spec.definition.lang.EnumFieldType
import org.maiaframework.gen.spec.definition.validation.EmailConstraintDef
import org.maiaframework.gen.spec.definition.validation.NotBlankConstraintDef
import org.maiaframework.gen.spec.definition.validation.NotNullConstraintDef
import org.maiaframework.gen.spec.definition.validation.UrlConstraintDef

object MatFormFieldRenderer {
    
    
    fun renderFormField(
        htmlFormField: AngularFormFieldDef,
        r: AbstractSourceRenderer,
        indentSize: Int = 8
    ) {

        val indent = " ".repeat(indentSize)

        if (htmlFormField.isTypeahead) {

            renderFieldWithTypeahead(htmlFormField, r, indent)

        } else if (htmlFormField.isEnum) {

            renderSelectFieldForEnum(htmlFormField, r, indent)

        } else {

            renderHtmlInputField(htmlFormField, r, indent)

        }
    
    }


    private fun renderHtmlInputField(
        htmlFormField: AngularFormFieldDef,
        r: AbstractSourceRenderer,
        indent: String
    ) {

        val classFieldDef = htmlFormField.classFieldDef
        val classFieldName = classFieldDef.classFieldName
        val fieldLabel = htmlFormField.fieldLabel

        r.appendLine("$indent<mat-form-field appearance=\"outline\">")

        if (htmlFormField.renderFieldLabel) {
            r.appendLine("$indent    <mat-label>${fieldLabel}</mat-label>")
        }

        r.appendLine("$indent    <input")
        r.appendLine("$indent        formControlName=\"$classFieldName\"")
        r.appendLine("$indent        name=\"$classFieldName\"")

        htmlFormField.placeholder?.let {
            r.appendLine("$indent        placeholder=\"${it}\"")
        }

        htmlFormField.inputEventText?.let {
            r.appendLine("$indent        $it")
        }

        r.appendLine("$indent        type=\"${htmlFormField.htmlInputType}\"")

        htmlFormField.autocomplete?.let {
            r.appendLine("$indent        autocomplete=\"$it\"")
        }

        if (htmlFormField.autoFocus) {
            r.appendLine("$indent        autoFocus")
        }

        r.appendLine("$indent        matInput")

        r.appendLine("$indent    />")

        if (classFieldDef.hasAnyValidationConstraints() || classFieldDef.isUnique) {

            if (classFieldDef.hasValidationConstraint(NotNullConstraintDef::class.java)
                || classFieldDef.hasValidationConstraint(NotBlankConstraintDef::class.java)
            ) {
                r.appendLine("$indent    @if (formGroup.controls['$classFieldName'].hasError('required')) {")
                r.appendLine("$indent        <mat-error>${fieldLabel} is required.</mat-error>")
                r.appendLine("$indent    }")
            }

            if (classFieldDef.hasValidationConstraint(EmailConstraintDef::class.java)) {
                r.appendLine("$indent    @if (formGroup.controls['$classFieldName'].hasError('email')) {")
                r.appendLine("$indent        <mat-error>${fieldLabel} must be valid.</mat-error>")
                r.appendLine("$indent    }")
            }

            if (classFieldDef.hasValidationConstraint(UrlConstraintDef::class.java)) {
                r.appendLine("$indent    @if (formGroup.controls['$classFieldName'].hasError('invalidUrl')) {")
                r.appendLine("$indent        <mat-error>${fieldLabel} must be a valid URL.</mat-error>")
                r.appendLine("$indent    }")
            }

            classFieldDef.minConstraint?.minValue?.let { min ->
                r.appendLine("$indent  @if (formGroup.controls['$classFieldName'].hasError('min')) {")
                r.appendLine("$indent      <mat-error>${fieldLabel} must be at least $min.</mat-error>")
                r.appendLine("$indent  }")
            }

            classFieldDef.maxConstraint?.maxValue?.let { max ->
                r.appendLine("$indent    @if (formGroup.controls['$classFieldName'].hasError('max')) {")
                r.appendLine("$indent        <mat-error>${fieldLabel} must not be more than $max.</mat-error>")
                r.appendLine("$indent    }")
            }

            classFieldDef.lengthConstraint?.min?.let { minLength ->
                r.appendLine("$indent    @if (formGroup.controls['$classFieldName'].hasError('minlength')) {")
                r.appendLine("$indent        <mat-error>${fieldLabel} must be at least $minLength characters.</mat-error>")
                r.appendLine("$indent    }")
            }

            classFieldDef.lengthConstraint?.max?.let { maxLength ->
                r.appendLine("$indent    @if (formGroup.controls['$classFieldName'].hasError('maxlength')) {")
                r.appendLine("$indent        <mat-error>${fieldLabel} must not be more than $maxLength characters.</mat-error>")
                r.appendLine("$indent    }")
            }

            if (classFieldDef.isUnique) {
                r.appendLine("$indent    @if (formGroup.controls['$classFieldName'].hasError('message')) {")
                r.appendLine("$indent        <mat-error>{{ formGroup.controls['$classFieldName']?.['errors']?.['message'] }}</mat-error>")
                r.appendLine("$indent    }")
            }

        }

        r.appendLine("$indent</mat-form-field>")
    }


    private fun renderSelectFieldForEnum(
        htmlFormField: AngularFormFieldDef,
        r: AbstractSourceRenderer,
        indent: String
    ) {

        val label = htmlFormField.fieldLabel
        val classFieldDef = htmlFormField.classFieldDef
        val classFieldName = classFieldDef.classFieldName
        val enumFieldType = classFieldDef.fieldType as EnumFieldType
        val enumDef = enumFieldType.enumDef

        r.append("""
            |$indent<mat-form-field appearance="outline">
            |$indent    <mat-label>$label</mat-label>
            |$indent    <mat-select formControlName="$classFieldName">
            |$indent        @for ($classFieldName of ${enumDef.selectOptionsUqcn}; track $classFieldName.name) {
            |$indent            <div [matTooltip]="$classFieldName.description" matTooltipShowDelay="1000">
            |$indent                <mat-option [value]="$classFieldName.name">{{$classFieldName.displayName}}</mat-option>
            |$indent            </div>
            |$indent        }
            |$indent    </mat-select>
            |""".trimMargin())

        if (classFieldDef.hasValidationConstraint(NotNullConstraintDef::class.java)
            || classFieldDef.hasValidationConstraint(NotBlankConstraintDef::class.java)) {
            r.appendLine("$indent    @if (formGroup.controls['$classFieldName'].hasError('required')) {")
            r.appendLine("$indent        <mat-error>$label is required.</mat-error>")
            r.appendLine("$indent    }")
        }

        if (classFieldDef.isUnique) {
            r.appendLine("$indent    @if (formGroup.controls['$classFieldName'].hasError('notUnique')) {")
            r.appendLine("$indent        <mat-error>{{ formGroup.controls['$classFieldName'].errors.message }}</mat-error>")
            r.appendLine("$indent    }")
        }

        r.appendLine("$indent</mat-form-field>")

    }


    private fun renderFieldWithTypeahead(
        htmlFormField: AngularFormFieldDef,
        r: AbstractSourceRenderer,
        indent: String
    ) {

        val classFieldDef = htmlFormField.classFieldDef
        val typeaheadDef = classFieldDef.typeaheadDef!!
        val classFieldName = classFieldDef.classFieldName

        val formGroupFieldName = typeaheadDef.typeaheadName.firstToLower()

        r.appendLine("$indent<mat-form-field appearance=\"outline\">")
        r.appendLine("$indent    <mat-autocomplete #${classFieldName}Auto=\"matAutocomplete\" [displayWith]=\"${formGroupFieldName}ResultFormatter\">")
        r.appendLine("$indent        @if (filtered${typeaheadDef.typeaheadName.firstToUpper()}IsLoading()) {")
        r.appendLine("$indent            <mat-option>Loading...</mat-option>")
        r.appendLine("$indent        }")
        r.appendLine("$indent        @if (!filtered${typeaheadDef.typeaheadName.firstToUpper()}IsLoading()) {")
        r.blankLine()
        r.appendLine("$indent            @for (option of filtered${typeaheadDef.typeaheadName.firstToUpper()}; track option) {")
        r.appendLine("$indent                <mat-option [value]=\"option\">{{ option.${typeaheadDef.searchTermFieldName} }}</mat-option>")
        r.appendLine("$indent            }")
        r.blankLine()
        r.appendLine("$indent        }")
        r.appendLine("$indent    </mat-autocomplete>")
        r.appendLine("$indent    <mat-label>${classFieldDef.displayName}</mat-label>")
        r.appendLine("$indent    <input")
        r.appendLine("$indent        formControlName=\"$formGroupFieldName\"")
        r.appendLine("$indent        matInput")
        r.appendLine("$indent        type=\"text\"")
        r.appendLine("$indent        [matAutocomplete]=\"${classFieldName}Auto\"")

        htmlFormField.placeholder?.let {
            r.appendLine("$indent        placeholder=\"${classFieldDef.formPlaceholderText}\"")
        }

        r.appendLine("$indent    />")

        if (classFieldDef.nullable == false) {
            r.appendLine("$indent    @if (formGroup.controls['$formGroupFieldName'].hasError('required')) {")
            r.appendLine("$indent        <mat-error>${classFieldDef.displayName} is required.</mat-error>")
            r.appendLine("$indent    }")
        }

        r.appendLine("$indent</mat-form-field>")

    }


}
