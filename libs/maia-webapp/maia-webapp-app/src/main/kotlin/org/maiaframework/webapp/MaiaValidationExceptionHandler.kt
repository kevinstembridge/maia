package org.maiaframework.webapp

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import kotlin.collections.sorted


@ControllerAdvice
class MaiaValidationExceptionHandler : ResponseEntityExceptionHandler() {


    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {

        val fieldErrors: Map<String, List<String>> = ex.bindingResult.fieldErrors
            .groupBy { it.field }
            .mapValues { (_, errors) -> errors.mapNotNull { it.defaultMessage }.sorted() }

        val globalErrors: List<String> = ex.bindingResult.globalErrors
            .mapNotNull { globalError -> globalError.defaultMessage }
            .sorted()

        val body = ex.body.apply {
            setProperty("fieldErrors", fieldErrors)
            setProperty("globalErrors", globalErrors)
        }

        return ResponseEntity.status(status)
            .headers(headers)
            .body(body)

    }


}
