package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.AngularFormFieldDef
import org.maiaframework.gen.spec.definition.AsyncValidatorDef
import org.maiaframework.gen.spec.definition.flags.CreateOrEdit
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.validation.EmailConstraintDef
import org.maiaframework.gen.spec.definition.validation.NotBlankConstraintDef
import org.maiaframework.gen.spec.definition.validation.UrlConstraintDef

object FormControlRendererHelper {


    fun renderFormControlFor(
        angularFormFieldDef: AngularFormFieldDef,
        createOrEdit: CreateOrEdit?
    ): String {

        val classFieldDef = angularFormFieldDef.classFieldDef

        if (createOrEdit == CreateOrEdit.edit && classFieldDef.isEditableByUser.value == false) {
            return "new FormControl({value: dto.${classFieldDef.classFieldName}, disabled: true})"
        }

        val validators = determineValidatorsFor(classFieldDef)
        val asyncValidators = determineAsyncValidatorsFor(angularFormFieldDef.asyncValidatorDef)

        val optionFields = mutableMapOf<String, String>()
        optionFields["updateOn"] = "'change'"

        if (validators.isNotEmpty()) {
            optionFields["validators"] = "[${validators.joinToString(", ")}]"
        }

        if (asyncValidators.isNotEmpty()) {
            optionFields["asyncValidators"] = "[${asyncValidators.joinToString(", ")}]"
            // TODO This should probably be 'blur' but that results in a new value not being submitted
            //  to the back end if you edit the field and hit enter without first tabbing out of the field (blurring)
            optionFields["updateOn"] = "'change'"
        }

        val controlOptions = optionFields.entries.map { entry -> "${entry.key}: ${entry.value}" }.joinToString(prefix = "{ ", separator = ", ", postfix = " }")

        val initialValue = if (createOrEdit == CreateOrEdit.edit) {
            "dto.${classFieldDef.classFieldName}"
        } else {
            "''"
        }

        return "new FormControl($initialValue, $controlOptions)"

    }


    private fun determineValidatorsFor(classFieldDef: ClassFieldDef): List<String> {

        val validators = mutableListOf<String>()

        if (classFieldDef.hasValidationConstraint(NotBlankConstraintDef::class.java)) {
            validators.add("Validators.required")
        }

        if (classFieldDef.hasValidationConstraint(EmailConstraintDef::class.java)) {
            validators.add("Validators.email")
        }

        if (classFieldDef.hasValidationConstraint(UrlConstraintDef::class.java)) {
            validators.add("CustomValidators.url")
        }

        classFieldDef.lengthConstraint?.min?.let { minLength ->
            validators.add("Validators.minLength($minLength)")
        }

        classFieldDef.lengthConstraint?.max?.let { maxLength ->
            validators.add("Validators.maxLength($maxLength)")
        }

        return validators

    }


    private fun determineAsyncValidatorsFor(asyncValidatorDef: AsyncValidatorDef?): List<String> {

        return if (asyncValidatorDef != null) {
            listOf("this.${asyncValidatorDef.validatorFieldName}.validate.bind(this.${asyncValidatorDef.validatorFieldName})")
        } else {
            emptyList()
        }

    }


}
