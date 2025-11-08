package org.maiaframework.webapp.domain

import org.springframework.http.HttpStatus


open class ErrorResponseRuntimeException(
    val httpStatus: HttpStatus,
    val errorResponseDto: ErrorResponseDto
) : RuntimeException() {


    constructor(
        httpStatus: HttpStatus,
        errorCode: ErrorCode,
        errorMessage: String
    ) : this(
        httpStatus,
        ErrorResponseDto(errorCode, errorMessage, null)
    )


}
