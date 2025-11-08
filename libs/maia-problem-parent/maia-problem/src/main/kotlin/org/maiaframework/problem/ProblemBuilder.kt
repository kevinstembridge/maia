package org.maiaframework.problem

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.ErrorResponseException
import java.net.URI

object ProblemBuilder {


    fun errorResponse(
        type: URI,
        title: String,
        detail: String? = null,
        statusCode: HttpStatus,
        properties: Map<String, Any> = emptyMap(),
        cause: Throwable? = null
    ): ErrorResponseException {

        val problemDetail = ProblemDetail.forStatus(statusCode.value()).apply {
            this.type = type
            this.title = title
            this.detail = detail
        }

        properties.forEach { (key, value) -> problemDetail.setProperty(key, value) }

        return ErrorResponseException(
            statusCode,
            problemDetail,
            cause
        )

    }


}
