package org.maiaframework.webapp

import org.springframework.security.web.csrf.CsrfToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Spring Security defers the resolution of CSRF tokens in order
 * to avoid accessing the HTTP session unnecessarily. This
 * doesn't work so well with Angular apps because they submit
 * POST requests without first having retrieved the page via
 * a GET request. This endpoint allows us to eagerly resolve
 * the deferred CSRF token, especially useful in tests.
 */
@RestController
class CsrfEndpoint {


    @GetMapping("/csrf")
    fun csrf(csrfToken: CsrfToken): CsrfToken {

        return csrfToken

    }


}
