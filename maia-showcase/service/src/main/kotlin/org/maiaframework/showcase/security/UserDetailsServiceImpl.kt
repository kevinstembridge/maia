package org.maiaframework.showcase.security

import org.maiaframework.domain.DomainId
import org.maiaframework.showcase.user.UserRepo
import org.maiaframework.webapp.domain.auth.MaiaUserDetails
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder


class UserDetailsServiceImpl(private val userRepo: UserRepo, private val passwordEncoder: PasswordEncoder) : UserDetailsService {


    private val usersByUsername: Map<String, () -> MaiaUserDetails> = mapOf(
        "user@maiaframework.org" to {
            MaiaUserDetails(
                "user@maiaframework.org",
                passwordEncoder.encode("password")!!,
                enabled = true,
                accountNonExpired = true,
                credentialsNonExpired = true,
                accountNonLocked = true,
                grantedAuthorities = setOf(SimpleGrantedAuthority("USER")).toSortedSet(Comparator.comparing { it.authority ?: "" }),
                userId = DomainId.newId(),
                firstName = "Norman",
                lastName = "User"
            )
        },
        "admin@maiaframework.org" to {
            MaiaUserDetails(
                "admin@maiaframework.org",
                passwordEncoder.encode("password")!!,
                enabled = true,
                accountNonExpired = true,
                credentialsNonExpired = true,
                accountNonLocked = true,
                grantedAuthorities = setOf(SimpleGrantedAuthority("SYS__ADMIN")).toSortedSet(Comparator.comparing { it.authority ?: "" }),
                userId = DomainId.newId(),
                firstName = "Admin",
                lastName = "User"
            )
        }
    )



    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails {

        val factory = usersByUsername[username] ?: throw UsernameNotFoundException("User not found: $username")
        return factory()

    }


}
