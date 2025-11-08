package org.maiaframework.webapp

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter


class AngularRoutingFilter : OncePerRequestFilter() {


    override fun doFilterInternal(
            request: HttpServletRequest,
            response: HttpServletResponse,
            filterChain: FilterChain
    ) {

        val mode = request.getHeader("Sec-Fetch-Mode")

        when (mode) {
            "navigate" -> {
                val rd = request.getRequestDispatcher("/")
                rd.forward(request, response)
            }
            else -> filterChain.doFilter(request, response)
        }

    }


}
