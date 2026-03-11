package org.maiaframework.showcase.party

import org.maiaframework.domain.DomainId
import org.maiaframework.domain.LifecycleState
import org.maiaframework.domain.auth.EncryptedPassword
import org.maiaframework.domain.party.FirstName
import org.maiaframework.domain.party.LastName
import org.maiaframework.showcase.auth.Authority
import org.maiaframework.showcase.user.UserEntity
import org.maiaframework.testing.domain.Anys
import org.maiaframework.testing.domain.Anys.anyDomainId
import org.maiaframework.testing.domain.Anys.anyEncryptedPassword
import org.maiaframework.testing.domain.Anys.anyFirstName
import org.maiaframework.testing.domain.Anys.anyLastName
import java.time.Instant


data class UserEntityTestBuilder(
    val authorities: List<Authority> = emptyList(),
    val createdById: DomainId = Anys.defaultCreatedById,
    val createdTimestampUtc: Instant = Anys.anyInstant(),
    val firstName: FirstName = anyFirstName(),
    val id: DomainId = anyDomainId(),
    val encryptedPassword: EncryptedPassword = anyEncryptedPassword(),
    val lastModifiedById: DomainId = createdById,
    val lastModifiedTimestampUtc: Instant = createdTimestampUtc,
    val lastName: LastName = anyLastName(),
    val lifecycleState: LifecycleState = LifecycleState.ACTIVE,
    val version: Long = 1L
) {


    fun build(): UserEntity {

        val displayName = "$firstName $lastName"

        return UserEntity(
            authorities,
            createdById,
            createdTimestampUtc,
            displayName,
            encryptedPassword.value,
            firstName,
            id,
            lastModifiedById,
            lastModifiedTimestampUtc,
            lastName,
            lifecycleState,
            version
        )

    }

}
