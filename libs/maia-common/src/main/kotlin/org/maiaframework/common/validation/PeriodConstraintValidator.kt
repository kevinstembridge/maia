package org.maiaframework.common.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.net.MalformedURLException
import java.time.Period


class PeriodConstraintValidator : ConstraintValidator<PeriodConstraint, String> {


    override fun isValid(value: String?, validatorContext: ConstraintValidatorContext): Boolean {

        if (value.isNullOrBlank()) {
            return true
        }

        try {
            Period.parse(value)
            return true
        } catch (_: MalformedURLException) {
            validatorContext.disableDefaultConstraintViolation()
            validatorContext.buildConstraintViolationWithTemplate("Invalid value [$value]. Expected a valid Period.").addConstraintViolation()
            return false
        }

    }


}
