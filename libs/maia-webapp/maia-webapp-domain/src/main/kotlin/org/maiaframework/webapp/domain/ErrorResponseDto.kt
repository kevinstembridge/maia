package org.maiaframework.webapp.domain

class ErrorResponseDto(
    code: ErrorCode,
    message: String,
    context: Any?
) {

    val error: Body = Body(code, message, context)


    data class Body(val code: ErrorCode, val message: String, val context: Any?)


}
