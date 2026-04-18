package org.maiaframework.webapp

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter


class AngularRoutingFilter(
    private val excludedPathPrefixes: Set<String> = emptySet()
) : OncePerRequestFilter() {


    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {

        val mode = request.getHeader("Sec-Fetch-Mode")
        val uri = request.requestURI

        if (mode == "navigate" && uri.notExcluded()) {
            val requestDispatcher = request.getRequestDispatcher("/")
            requestDispatcher.forward(request, response)
        } else {
            filterChain.doFilter(request, response)
        }

    }


    private fun String.notExcluded(): Boolean {

        return excludedPathPrefixes.none { this.startsWith(it) }

    }


}
