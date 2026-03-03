package org.maiaframework.showcase.config

import org.maiaframework.webapp.ReportingAccessDeniedHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder


@Configuration
class MaiaShowcaseSecurityConfiguration {


    private val passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()


    @Bean
    fun authenticationManager(): AuthenticationManager {

        return ProviderManager(
            listOf(
                initPasswordAuthenticationProvider()
            )
        )

    }


    private fun initPasswordAuthenticationProvider(): DaoAuthenticationProvider {

        return DaoAuthenticationProvider(UserDetailsServiceImpl(userCustomRepo)).apply {
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


}
