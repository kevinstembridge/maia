package org.maiaframework.webapp.security

import org.maiaframework.webapp.AngularRoutingFilter
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
import org.springframework.security.web.csrf.CsrfFilter

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

        http.csrf { it.spa() }

        http {

            logout {
                logoutSuccessHandler = HttpStatusReturningLogoutSuccessHandler()
            }

            this.authenticationManager = authenticationManager

            // The order of Spring's security filters can be found here:
            // https://docs.spring.io/spring-security/reference/servlet/architecture.html#servlet-security-filters
            addFilterBefore<SecurityContextHolderFilter>(AngularRoutingFilter())

            exceptionHandling { this.accessDeniedHandler = accessDeniedHandler }

            authorizeHttpRequests(authorizeHttpRequests)
        }

        return http.build()

    }


}
