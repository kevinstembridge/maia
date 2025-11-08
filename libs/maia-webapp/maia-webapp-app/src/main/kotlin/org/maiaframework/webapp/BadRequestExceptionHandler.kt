package org.maiaframework.webapp

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus


//@ControllerAdvice
class BadRequestExceptionHandler {

    private val logger = LoggerFactory.getLogger(BadRequestExceptionHandler::class.java)

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handle(e: HttpMessageNotReadableException?) {
        logger.warn("Returning HTTP 400 Bad Request", e)
        throw e!!
    }

}
