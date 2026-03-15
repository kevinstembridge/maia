package org.maiaframework.showcase.security

import org.maiaframework.domain.DomainId
import org.maiaframework.domain.contact.EmailAddress
import org.maiaframework.showcase.auth.Authority
import org.maiaframework.showcase.contact.EmailAddressRepo
import org.maiaframework.showcase.party.EmailAddressVerificationRepoHelper
import org.maiaframework.showcase.party.PartyEmailAddressRepoHelper
import org.maiaframework.showcase.user.UserEntity
import org.maiaframework.showcase.user.UserGroupMembershipRepo
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
    private val userGroupMembershipRepo: UserGroupMembershipRepo
) : UserDetailsService {


    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails {

        val emailAddressEntity = this.emailAddressRepo.findOneOrNullByEmailAddress(EmailAddress(username))
            ?: throw UsernameNotFoundException.fromUsername(username)

        val loginPartyEmailAddressEntity = this.partyEmailAddressRepoHelper.findLoginEmailAddressByUsername(username)
            ?: throw UsernameNotFoundException.fromUsername(username)

        val userEntity = this.userRepo.findByPrimaryKey(loginPartyEmailAddressEntity.partyId)
        val loginEmailVerified = this.emailAddressVerificationRepoHelper.isEmailAddressVerified(emailAddressEntity.id)
        val grantedAuthorities = getGrantedAuthoritiesFor(userEntity)

        return MaiaUserDetails(
            emailAddressEntity.emailAddress.value,
            userEntity.encryptedPassword,
            enabled = true,
            accountNonExpired = true,
            credentialsNonExpired = true,
            accountNonLocked = loginEmailVerified,
            grantedAuthorities,
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


    private fun getAuthoritiesByUserGroup(userEntity: UserEntity): Map<DomainId, Set<Authority>> {

        val orgUserGroupIds = this.userGroupMembershipRepo.findByUserId(userEntity.id).map { it.orgUserGroupId }

        if (orgUserGroupIds.isEmpty()) {
            return emptyMap()
        }

        val orgUserGroupFilter = OrgUserGroupEntityFilters().id  `in` orgUserGroupIds

        val orgUserGroups: List<OrgUserGroupEntity> = this.orgUserGroupDao.findAllBy(orgUserGroupFilter)

        return orgUserGroups.associate { it.orgId to it.authorities.toSet() }

    }


}
