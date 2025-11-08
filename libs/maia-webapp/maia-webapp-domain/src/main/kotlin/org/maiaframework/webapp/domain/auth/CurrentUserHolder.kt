package org.maiaframework.webapp.domain.auth

import org.maiaframework.webapp.domain.user.UserSummaryDto
import org.maiaframework.domain.DomainId
import org.maiaframework.domain.contact.EmailAddress
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import java.util.SortedSet

object CurrentUserHolder {


    val userId: DomainId
        get() = userIdOrNull
            ?: throw IllegalStateException("userId not stored in current security context")


    val userIdOrNull: DomainId?
        get() = currentUserOrNull?.userId


    val currentUser: MaiaUserDetails
        get() = currentUserOrNull
            ?: throw IllegalStateException("current user is not a MahanaUserDetails instance")


    val currentUserOrNull: MaiaUserDetails?
        get() = SecurityContextHolder.getContext().authentication?.let { authentication ->

            val principal = authentication.principal

            if (principal is MaiaUserDetails) {
                principal
            } else {
                null
            }

        }


    val currentUsernameOrNull: String?
        get() = SecurityContextHolder.getContext().authentication?.let { authentication ->

            val principal = authentication.principal

            if (principal is User) {
                principal.username
            } else {
                null
            }

        }


    @JvmStatic
    val grantedAuthorities: SortedSet<String>
        get () {
            return SecurityContextHolder.getContext().authentication?.authorities?.map { it.authority }.orEmpty().toSortedSet()
        }


    fun currentUserSummary(): UserSummaryDto {

        val firstName = currentUser.firstName
        val lastName = currentUser.lastName
        val emailAddress = EmailAddress(currentUser.username)

        return UserSummaryDto(firstName, lastName, emailAddress, grantedAuthorities, currentUser.userId)

    }


    fun currentUserSummaryOrNull(): UserSummaryDto? {

        return currentUserOrNull?.let {

            val firstName = it.firstName
            val lastName = it.lastName
            val emailAddress = EmailAddress(it.username)

            UserSummaryDto(firstName, lastName, emailAddress, grantedAuthorities, it.userId)

        }

    }


}
