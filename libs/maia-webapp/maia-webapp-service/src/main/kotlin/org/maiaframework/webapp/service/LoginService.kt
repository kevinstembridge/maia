package org.maiaframework.webapp.service

import org.maiaframework.common.logging.getLogger
import org.maiaframework.domain.auth.EmailAndPassword
import org.maiaframework.domain.contact.EmailAddress
import org.maiaframework.webapp.domain.auth.MaiaUserDetails
import org.maiaframework.webapp.domain.user.UserSummaryDto
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component

@Component
class LoginService(
    private val authenticationManager: AuthenticationManager,
    private val userDetailsService: UserDetailsService
) {


    private val logger = getLogger<LoginService>()


    fun login(emailAndPassword: EmailAndPassword): UserSummaryDto {

        val loginEmailAddress = emailAndPassword.emailAddressObj
        logger.info("BEGIN: login($loginEmailAddress)")

        authenticateUser(emailAndPassword)

        return buildUserSummaryDto(loginEmailAddress)

    }


    private fun authenticateUser(emailAndPassword: EmailAndPassword) {

        val authentication = this.authenticationManager.authenticate(emailAndPassword.asUsernamePasswordAuthenticationToken)
        SecurityContextHolder.getContext().authentication = authentication

    }


    private fun buildUserSummaryDto(loginEmailAddress: EmailAddress): UserSummaryDto {

        val userDetails = this.userDetailsService.loadUserByUsername(loginEmailAddress.value) as MaiaUserDetails

        return UserSummaryDto(
            emailAddress = userDetails.usernameAsEmailAddress,
            firstName = userDetails.firstName,
            grantedAuthorities = userDetails.grantedAuthoritiesSorted,
            id = userDetails.userId,
            lastName = userDetails.lastName
        )

    }


    private val EmailAndPassword.asUsernamePasswordAuthenticationToken
        get() = UsernamePasswordAuthenticationToken(emailAddressRaw, password)


    private val MaiaUserDetails.usernameAsEmailAddress
        get() = EmailAddress(username)


}
