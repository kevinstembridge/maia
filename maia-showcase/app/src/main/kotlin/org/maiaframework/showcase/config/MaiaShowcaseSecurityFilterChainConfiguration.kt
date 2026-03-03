package org.maiaframework.showcase.config

import org.maiaframework.webapp.AngularRoutingFilter
import org.maiaframework.webapp.CsrfCookieFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
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
import org.springframework.security.web.util.matcher.RegexRequestMatcher.regexMatcher


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class MaiaShowcaseSecurityFilterChainConfiguration {


    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        authenticationManager: AuthenticationManager,
        accessDeniedHandler: AccessDeniedHandler
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

            authorizeHttpRequests {

                authorize(regexMatcher("/"), permitAll)
                authorize(regexMatcher("/index.html"), permitAll)
                authorize(regexMatcher("/assets/.*"), permitAll)
                authorize(regexMatcher("/aircraft/.*"), permitAll)
                authorize(regexMatcher("/csrf"), permitAll)
                authorize(regexMatcher("/css/.*"), permitAll)
                authorize(regexMatcher("/dist/.*"), permitAll)
                authorize(regexMatcher("/img/.*"), permitAll)
                authorize(regexMatcher("/robots.txt"), permitAll)
                authorize(regexMatcher("/search.*"), permitAll)
                authorize(regexMatcher("/login"), permitAll)
                authorize(regexMatcher("/login_or_register"), permitAll)
                authorize(regexMatcher("/register/verify_email_address/.*"), permitAll)
                authorize(regexMatcher("/register/request_email_address_token"), permitAll)
                authorize(regexMatcher("/.*\\.css$"), permitAll)
                authorize(regexMatcher("/.*\\.eot$"), permitAll)
                authorize(regexMatcher("/.*\\.ico$"), permitAll)
                authorize(regexMatcher("/.*\\.jpeg.*"), permitAll)
                authorize(regexMatcher("/.*\\.jpg$"), permitAll)
                authorize(regexMatcher("/.*\\.js$"), permitAll)
                authorize(regexMatcher("/.*\\.png$"), permitAll)
                authorize(regexMatcher("/.*\\.svg$"), permitAll)
                authorize(regexMatcher("/.*\\.ttf$"), permitAll)
                authorize(regexMatcher("/.*\\.woff.*$"), permitAll)
                authorize(regexMatcher(HttpMethod.POST, "/e2e_testing/.*"), permitAll)
                authorize(regexMatcher(HttpMethod.GET, "/api/admin/.*"), hasRole("ADMIN"))
                authorize(regexMatcher(HttpMethod.GET, "/api/current_user"), permitAll)
                authorize(regexMatcher("/manage/.*"), hasRole("ADMIN"))
                authorize(regexMatcher(HttpMethod.GET, "/api/aircraft/aircraft_owned_by_current_user"), authenticated)
                authorize(regexMatcher(HttpMethod.GET, "/api/aircraft/detail/.*"), permitAll)
                authorize(regexMatcher(HttpMethod.GET, "/api/aircraft/find_ownership_claims_with_same_registrant_as/.*"), authenticated)
                authorize(regexMatcher(HttpMethod.GET, "/api/my/aircraft/ownership_claim/fetch_my_aircraft_eligible_for_postal_verification"), authenticated)
                authorize(regexMatcher(HttpMethod.GET, "/api/aircraft/ownership_claim/orgs_for_current_user"), authenticated)
                authorize(regexMatcher(HttpMethod.GET, "/api/aircraft/ownership_claims.*"), authenticated)
                authorize(regexMatcher(HttpMethod.POST, "/api/aircraft/submit_ownership_claim"), authenticated)
                authorize(regexMatcher(HttpMethod.POST, "/api/auth/refresh_token"), permitAll)
                authorize(regexMatcher(HttpMethod.POST, "/api/auth/revoke_token"), permitAll)
                authorize(regexMatcher(HttpMethod.GET, "/api/my/aircraft.*"), authenticated)
                authorize(regexMatcher(HttpMethod.POST, "/api/my/aircraft.*"), authenticated)
                authorize(regexMatcher(HttpMethod.POST, "/api/ops/org/create"), authenticated)
                authorize(regexMatcher(HttpMethod.POST, "/api/ops/website_url/exists_by_url"), authenticated)
                authorize(regexMatcher(HttpMethod.DELETE, "/api/ops/.*"), hasAuthority(Authority.SYS__OPS.name))
                authorize(regexMatcher(HttpMethod.GET, "/api/ops/.*"), hasAuthority(Authority.SYS__OPS.name))
                authorize(regexMatcher(HttpMethod.POST, "/api/ops/.*"), hasAuthority(Authority.SYS__OPS.name))
                authorize(regexMatcher(HttpMethod.PUT, "/api/ops/.*"), hasAuthority(Authority.SYS__OPS.name))
                authorize(regexMatcher(HttpMethod.POST, "/api/org/custom_create.*"), authenticated)
                authorize(regexMatcher(HttpMethod.GET, "/api/search.*"), permitAll)
                authorize(regexMatcher(HttpMethod.POST, "/api/search.*"), permitAll)
                authorize(regexMatcher(HttpMethod.POST, "/api/login"), permitAll)
                authorize(regexMatcher(HttpMethod.POST, "/api/register"), permitAll)

            }
        }

        return http.build()

    }


}
