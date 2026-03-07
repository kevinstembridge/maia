package org.maiaframework.showcase.config

import jakarta.servlet.DispatcherType
import org.maiaframework.showcase.security.UserDetailsServiceImpl
import org.maiaframework.webapp.security.ReportingAccessDeniedHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.web.AuthorizeHttpRequestsDsl
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.util.matcher.DispatcherTypeRequestMatcher
import org.springframework.security.web.util.matcher.RegexRequestMatcher.regexMatcher


@Configuration
class MaiaShowcaseSecurityConfiguration {


    private val passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()


    @Bean
    fun authenticationManager(userDetailsService: UserDetailsService): AuthenticationManager {

        return ProviderManager(
            listOf(
                initPasswordAuthenticationProvider(userDetailsService)
            )
        )

    }


    private fun initPasswordAuthenticationProvider(userDetailsService: UserDetailsService): DaoAuthenticationProvider {

        return DaoAuthenticationProvider(userDetailsService).apply {
            setPasswordEncoder(passwordEncoder)
        }

    }


    @Bean
    fun passwordEncoder(): PasswordEncoder {

        return this.passwordEncoder

    }


    @Bean
    fun accessDeniedHandler(): ReportingAccessDeniedHandler {

        return ReportingAccessDeniedHandler()

    }


    @Bean
    fun userDetailsService(): UserDetailsService {

        return UserDetailsServiceImpl(this.passwordEncoder)
    }


    @Bean
    fun authorizeHttpRequests(): AuthorizeHttpRequestsDsl.() -> Unit = {

        authorize(DispatcherTypeRequestMatcher(DispatcherType.FORWARD), permitAll)
        authorize(regexMatcher("/"), permitAll)
        authorize(regexMatcher("/index.html"), permitAll)
        authorize(regexMatcher("/assets/.*"), permitAll)
        authorize(regexMatcher("/csrf"), permitAll)
        authorize(regexMatcher("/css/.*"), permitAll)
        authorize(regexMatcher("/dist/.*"), permitAll)
        authorize(regexMatcher("/img/.*"), permitAll)
        authorize(regexMatcher("/robots.txt"), permitAll)
        authorize(regexMatcher("/search.*"), permitAll)
        authorize(regexMatcher("/login"), permitAll)
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
        authorize(regexMatcher(HttpMethod.GET, "/api/admin/.*"), hasRole("ADMIN"))
        authorize(regexMatcher(HttpMethod.GET, "/api/current_user"), permitAll)
        authorize(regexMatcher("/manage/.*"), hasRole("ADMIN"))
        authorize(regexMatcher(HttpMethod.POST, "/api/auth/refresh_token"), permitAll)
        authorize(regexMatcher(HttpMethod.POST, "/api/auth/revoke_token"), permitAll)
        authorize(regexMatcher(HttpMethod.POST, "/api/login"), permitAll)
        authorize(regexMatcher(HttpMethod.GET, "/api/search.*"), permitAll)
        authorize(regexMatcher(HttpMethod.POST, "/api/search.*"), permitAll)
        authorize(regexMatcher(HttpMethod.POST, "/api/.*/search"), permitAll)

    }


}
