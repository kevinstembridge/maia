package org.maiaframework.showcase.security

import org.maiaframework.domain.contact.EmailAddress
import org.maiaframework.showcase.contact.EmailAddressRepo
import org.maiaframework.showcase.party.EmailAddressVerificationRepoHelper
import org.maiaframework.showcase.party.PartyEmailAddressRepoHelper
import org.maiaframework.showcase.user.UserEntity
import org.maiaframework.showcase.user.UserGroupEntity
import org.maiaframework.showcase.user.UserGroupEntityFilters
import org.maiaframework.showcase.user.UserGroupMembershipRepo
import org.maiaframework.showcase.user.UserGroupRepo
import org.maiaframework.showcase.user.UserRepo
import org.maiaframework.webapp.domain.auth.MaiaUserDetails
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import java.util.SortedSet


@Service
class UserDetailsServiceImpl(
    private val userRepo: UserRepo,
    private val partyEmailAddressRepoHelper: PartyEmailAddressRepoHelper,
    private val emailAddressVerificationRepoHelper: EmailAddressVerificationRepoHelper,
    private val emailAddressRepo: EmailAddressRepo,
    private val userGroupMembershipRepo: UserGroupMembershipRepo,
    private val userGroupRepo: UserGroupRepo
) : UserDetailsService {


    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails {

        val emailAddressEntity = this.emailAddressRepo.findOneOrNullByEmailAddress(EmailAddress(username))
            ?: throw UsernameNotFoundException.fromUsername(username)

        val loginPartyEmailAddressEntity = this.partyEmailAddressRepoHelper.findLoginEmailAddressByUsername(username)
            ?: throw UsernameNotFoundException.fromUsername(username)

        val userEntity = this.userRepo.findByPrimaryKey(loginPartyEmailAddressEntity.party)
        val loginEmailVerified = this.emailAddressVerificationRepoHelper.isEmailAddressVerified(emailAddressEntity.id)
        val userGrantedAuthorities = getGrantedAuthoritiesFor(userEntity)
        val userGroupGrantedAuthorities = getAuthoritiesForUserGroups(userEntity)
        val allGrantedAuthorities = userGrantedAuthorities
            .union(userGroupGrantedAuthorities)
            .toSortedSet(java.util.Comparator.comparing { it.authority ?: "" })

        return MaiaUserDetails(
            emailAddressEntity.emailAddress.value,
            userEntity.encryptedPassword,
            enabled = true,
            accountNonExpired = true,
            credentialsNonExpired = true,
            accountNonLocked = loginEmailVerified,
            grantedAuthorities = allGrantedAuthorities,
            userId = userEntity.id,
            userEntity.firstName?.value,
            userEntity.lastName.value
        )

    }


    private fun getGrantedAuthoritiesFor(
        userEntity: UserEntity
    ): SortedSet<GrantedAuthority> {

        return userEntity
            .authorities
            .map { SimpleGrantedAuthority(it.name) }
            .toSortedSet(java.util.Comparator.comparing { it.authority ?: "" })

    }


    private fun getAuthoritiesForUserGroups(userEntity: UserEntity): Set<GrantedAuthority> {

        val userGroupIds = this.userGroupMembershipRepo.findByUser(userEntity.id).map { it.userGroup }

        if (userGroupIds.isEmpty()) {
            return emptySet()
        }

        val userGroupFilter = UserGroupEntityFilters().id  `in` userGroupIds

        val userGroups: List<UserGroupEntity> = this.userGroupRepo.findAllBy(userGroupFilter)

        return userGroups.flatMap { it.authorities }.map { SimpleGrantedAuthority(it.name) }.toSet()

    }


}
