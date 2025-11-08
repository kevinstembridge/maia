package org.maiaframework.webapp.domain.auth

import org.maiaframework.domain.DomainId
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User
import java.util.*

class MaiaUserDetails(
    username: String,
    password: String,
    enabled: Boolean,
    accountNonExpired: Boolean,
    credentialsNonExpired: Boolean,
    accountNonLocked: Boolean,
    val grantedAuthorities: SortedSet<GrantedAuthority>,
    val userId: DomainId,
    val firstName: String?,
    val lastName: String?
): User(
    username,
    password,
    enabled,
    accountNonExpired,
    credentialsNonExpired,
    accountNonLocked,
    grantedAuthorities
) {


    val grantedAuthoritiesSorted = grantedAuthorities.map { it.authority }.toSortedSet()

}
