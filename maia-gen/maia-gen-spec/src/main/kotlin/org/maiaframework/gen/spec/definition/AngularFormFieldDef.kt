package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.flags.TextCase
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.EnumFieldType
import org.maiaframework.gen.spec.definition.lang.TypescriptImport
import org.maiaframework.gen.spec.definition.validation.AbstractValidationConstraintDef
import org.maiaframework.gen.spec.definition.validation.NotEmptyConstraintDef
import org.maiaframework.gen.spec.definition.validation.NotNullConstraintDef


class AngularFormFieldDef(
    private val dtoBaseName: DtoBaseName,
    val classFieldDef: ClassFieldDef,
    val fieldLabel: FieldLabel?,
    val renderFieldLabel: Boolean,
    val placeholder: FormPlaceholderText?,
    val htmlInputType: HtmlInputType,
    val autoFocus: Boolean,
    val autocomplete: FormAutocompleteText?,
    val typeaheadRequiredValidatorFunctionName: String?,
    val typeaheadRequiredValidatorTypescriptImport: TypescriptImport?,
    val asyncValidatorDef: AsyncValidatorDef?
) {


    val isEnum = classFieldDef.isEnum


    val isTypeahead = classFieldDef.typeaheadDef != null


    val fieldType = classFieldDef.fieldType


    val fieldName = classFieldDef.classFieldName


    val enumDef: EnumDef? = when (this.classFieldDef.fieldType) {
        is EnumFieldType -> this.classFieldDef.fieldType.enumDef
        else -> null
    }


    val isRequired: Boolean
        get() = hasValidationConstraint(NotNullConstraintDef::class.java) || hasValidationConstraint(NotEmptyConstraintDef::class.java)


    val isEditable: Boolean = this.classFieldDef.isModifiableBySystem


    val fieldKey: String = this.dtoBaseName.toString() + "_" + fieldName


    val linksToAField = this.classFieldDef.fieldLinkedTo != null


    fun hasValidationConstraint(type: Class<out AbstractValidationConstraintDef>): Boolean {

        return this.classFieldDef.hasValidationConstraint(type)

    }


    fun hasAnyValidationConstraint(): Boolean {

        return this.classFieldDef.hasAnyValidationConstraint()

    }


    val inputEventText = when (classFieldDef.textCase) {
        TextCase.ORIGINAL -> null
        TextCase.LOWER -> "(input)=\"formGroup.controls['${classFieldDef.classFieldName}'].setValue(formGroup.controls['${classFieldDef.classFieldName}'].value.toLowerCase())\""
        TextCase.UPPER -> "(input)=\"formGroup.controls['${classFieldDef.classFieldName}'].setValue(formGroup.controls['${classFieldDef.classFieldName}'].value.toUpperCase())\""
    }


}
