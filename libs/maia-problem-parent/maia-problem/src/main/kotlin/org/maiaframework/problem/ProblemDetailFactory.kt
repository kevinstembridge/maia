package org.maiaframework.problem


import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail


class ProblemDetailFactory {


    fun businessRuleViolation(ex: ConstraintViolationException) {

        val problemDetail = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_CONTENT).apply {

//            type = ProblemDetailConstants.ProblemType.BUSINESS_RULE_VIOLATION

        }




    }


    private fun getObjectErrorsFrom(ex: ConstraintViolationException): List<String> {

        return ex.constraintViolations
            .filter { it.propertyPath == null }
            .map { it.message }

    }


    private fun getFieldErrorsFrom(ex: ConstraintViolationException): Map<String, List<String>> {

        val fieldErrors = mutableMapOf<String, List<String>>()

        ex.constraintViolations
            .filter { it.propertyPath != null }
            .forEach { huh ->

                val fieldName = huh.propertyPath.toString()
                val errorMessage = huh.message

                fieldErrors.compute(fieldName) { _, existingErrors ->
                    existingErrors?.plus(errorMessage)?.sorted() ?: listOf(errorMessage)
                }

            }

        return fieldErrors

    }


}
