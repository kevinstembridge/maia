package org.maiaframework.webapp

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

class CsrfCookieFilter : OncePerRequestFilter() {

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
            request: HttpServletRequest,
            response: HttpServletResponse,
            filterChain: FilterChain
    ) {

        val csrfToken: CsrfToken = request.getAttribute(CsrfToken::class.java.name) as CsrfToken
        response.setHeader(csrfToken.headerName, csrfToken.token)
        filterChain.doFilter(request, response)

    }

}
