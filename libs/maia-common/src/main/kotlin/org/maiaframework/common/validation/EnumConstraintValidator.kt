package org.maiaframework.common.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.util.*


class EnumConstraintValidator : ConstraintValidator<EnumConstraint, String> {


    private val enumNames = mutableSetOf<String>()
    private var ignoreCase: Boolean = false


    override fun initialize(enumConstraint: EnumConstraint) {

        this.ignoreCase = enumConstraint.ignoreCase
        val enumClass = enumConstraint.enumClass

        enumClass.java.enumConstants.forEach { o ->
            val enumName = if (enumConstraint.ignoreCase)
                o.name.uppercase(Locale.ENGLISH)
            else
                o.name

            this.enumNames.add(enumName)

        }

    }


    override fun isValid(value: String?, validatorContext: ConstraintValidatorContext): Boolean {

        if (value == null || "" == value.trim { it <= ' ' }) {
            return true
        }

        val normalisedValue = if (this.ignoreCase)
            value.uppercase(Locale.ENGLISH)
        else
            value

        val isValid = this.enumNames.contains(normalisedValue)

        if (isValid == false) {
            validatorContext.disableDefaultConstraintViolation()
            validatorContext.buildConstraintViolationWithTemplate("Invalid value [" + value + "]. Expected one of " + this.enumNames + ".").addConstraintViolation()
        }

        return isValid

    }


}
