package org.maiaframework.webapp.security

import org.maiaframework.webapp.AngularRoutingFilter
import org.maiaframework.webapp.security.CsrfCookieFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.AuthorizeHttpRequestsDsl
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler
import org.springframework.security.web.context.SecurityContextHolderFilter
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import org.springframework.security.web.csrf.CsrfFilter
import org.springframework.security.web.csrf.CsrfTokenRequestHandler
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class MaiaSecurityFilterChainConfiguration {

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        authenticationManager: AuthenticationManager,
        accessDeniedHandler: AccessDeniedHandler,
        authorizeHttpRequests: AuthorizeHttpRequestsDsl.() -> Unit
    ): SecurityFilterChain {

        val delegate = XorCsrfTokenRequestAttributeHandler().apply {
            setCsrfRequestAttributeName("_csrf")
        }

        val csrfTokenRequestHandler = CsrfTokenRequestHandler(delegate::handle)

        http {

            csrf {
                this.csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse()
                this.csrfTokenRequestHandler = csrfTokenRequestHandler
            }

            logout {
                logoutSuccessHandler = HttpStatusReturningLogoutSuccessHandler()
            }

            this.authenticationManager = authenticationManager

            // The order of Spring's security filters can be found here:
            // https://docs.spring.io/spring-security/reference/servlet/architecture.html#servlet-security-filters
            addFilterBefore<SecurityContextHolderFilter>(AngularRoutingFilter())
            addFilterAfter<CsrfFilter>(CsrfCookieFilter())

            exceptionHandling { this.accessDeniedHandler = accessDeniedHandler }

            authorizeHttpRequests(authorizeHttpRequests)
        }

        return http.build()

    }


}
