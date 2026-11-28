package org.maiaframework.webapp

import org.maiaframework.common.BlotterTooLargeException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus


/**
 * Reports a Hazelcast-backed Blotter whose set exceeds its `maxRows` as a problem detail carrying the explicit
 * message, so the Blotter page can show it.
 */
@ControllerAdvice
class BlotterTooLargeExceptionHandler {


    @ExceptionHandler(BlotterTooLargeException::class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    @ResponseBody
    fun handle(e: BlotterTooLargeException): ProblemDetail {

        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, e.message!!)

    }


}
