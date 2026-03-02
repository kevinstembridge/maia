package org.maiaframework.webapp

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import org.maiaframework.domain.auth.EmailAndPassword
import org.maiaframework.webapp.domain.user.UserSummaryDto
import org.maiaframework.webapp.service.LoginService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException


@RestController
class LoginEndpoint(private val loginService: LoginService) {


    @PostMapping("/api/login", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun loginUser(
        @RequestBody emailAndPassword: EmailAndPassword,
        request: HttpServletRequest
    ): UserSummaryDto {

        try {

            val userSummaryDto = loginService.login(emailAndPassword)

            `store Authentication in session`(request)

            return userSummaryDto

        } catch (_: LockedException) {
            throw ResponseStatusException(HttpStatus.LOCKED)
        } catch (_: BadCredentialsException) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        }

    }


    private fun `store Authentication in session`(request: HttpServletRequest) {

        val securityContext = SecurityContextHolder.getContext()
        val session: HttpSession = request.getSession(true)
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext)

    }


}
