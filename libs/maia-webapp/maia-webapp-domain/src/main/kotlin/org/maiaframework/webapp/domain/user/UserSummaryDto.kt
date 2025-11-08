package org.maiaframework.webapp.domain.user

import org.maiaframework.domain.DomainId
import org.maiaframework.domain.contact.EmailAddress
import java.util.*


data class UserSummaryDto(
    val firstName: String?,
    val lastName: String?,
    val emailAddress: EmailAddress,
    val grantedAuthorities: SortedSet<String>,
    val id: DomainId
)
