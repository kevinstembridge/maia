package org.maiaframework.common.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.net.MalformedURLException
import java.net.URL


class UrlConstraintValidator : ConstraintValidator<UrlConstraint, String> {


    override fun isValid(value: String?, validatorContext: ConstraintValidatorContext): Boolean {

        if (value == null || "" == value.trim { it <= ' ' }) {
            return true
        }

        try {
            URL(value)
            return true
        } catch (e: MalformedURLException) {
            validatorContext.disableDefaultConstraintViolation()
            validatorContext.buildConstraintViolationWithTemplate("Invalid value [$value]. Expected a valid URL.").addConstraintViolation()
            return false
        }

    }


}
