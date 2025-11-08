package org.maiaframework.webapp.domain

class ReduxFormValidationErrorsDto(globalErrors: List<GlobalError>, fieldErrors: List<FieldError>) {


    val validationErrors: Map<String, String>


    init {

        val globalErrorMessage = globalErrors.map { it.message }.joinToString(". ")
        this.validationErrors = fieldErrors.asSequence().plus(FieldError("_error", globalErrorMessage)).map { it.fieldName to it.message }.toList().toMap()

    }


    data class GlobalError(val message: String)


    data class FieldError(val fieldName: String, val message: String)


}
