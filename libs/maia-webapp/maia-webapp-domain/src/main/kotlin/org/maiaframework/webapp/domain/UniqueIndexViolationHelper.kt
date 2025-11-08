package org.maiaframework.webapp.domain

import org.springframework.http.HttpStatus
import java.util.*

// TODO KS: Delete me after cooling off (2024-08-27)
object UniqueIndexViolationHelper {


    fun errorResponse(vararg fieldNameArray: String): ErrorResponseRuntimeException {

        val fieldNames = fieldNameArray.toSortedSet()

        if (fieldNames.size > 1) {

            val globalError = ReduxFormValidationErrorsDto.GlobalError("Unique key conflict.")
            val dto = ReduxFormValidationErrorsDto(listOf(globalError), fieldErrors(fieldNames, "This field is part of a unique key."))
            val errorResponseDto = ErrorResponseDto(ErrorCodes.VALIDATION_ERROR, "Unique constraint violation", dto)
            return ErrorResponseRuntimeException(HttpStatus.CONFLICT, errorResponseDto)

        } else {

            val dto = ReduxFormValidationErrorsDto(listOf(), fieldErrors(fieldNames, "This value must be unique."))
            val errorResponseDto = ErrorResponseDto(ErrorCodes.VALIDATION_ERROR, "Unique constraint violation", dto)
            return ErrorResponseRuntimeException(HttpStatus.CONFLICT, errorResponseDto)

        }

    }


    private fun fieldErrors(
        fieldNames: SortedSet<String>,
        message: String
    ): List<ReduxFormValidationErrorsDto.FieldError> {

        return fieldNames
            .map { fieldName -> ReduxFormValidationErrorsDto.FieldError(fieldName, message) }

    }


}
