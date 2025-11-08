package org.maiaframework.webapp

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandlerImpl
import org.springframework.security.web.csrf.InvalidCsrfTokenException
import org.springframework.security.web.csrf.MissingCsrfTokenException
import org.springframework.stereotype.Component

@Component
class ReportingAccessDeniedHandler : AccessDeniedHandlerImpl() {

    // TODO KNS replace with Micrometer
    //    private static final Counter exceptionCounter = Counter.build()
    //            .name("access_denied_exception_count")
    //            .help("The total count of AccessDeniedExceptions")
    //            .labelNames("type")
    //            .register();


    override fun handle(request: HttpServletRequest, response: HttpServletResponse, accessDeniedException: AccessDeniedException) {

        if (accessDeniedException is InvalidCsrfTokenException) {
            incrementCounter("invalid_csrf")
        } else if (accessDeniedException is MissingCsrfTokenException) {
            incrementCounter("missing_csrf")
        } else {
            incrementCounter("access_denied")
        }

        super.handle(request, response, accessDeniedException)

    }


    private fun incrementCounter(labelValue: String) {

        //        exceptionCounter.labels(labelValue).inc();

    }


}
