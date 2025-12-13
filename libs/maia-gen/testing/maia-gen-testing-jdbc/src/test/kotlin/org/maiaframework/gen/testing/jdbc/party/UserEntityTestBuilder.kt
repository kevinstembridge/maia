package org.maiaframework.gen.testing.jdbc.party

import org.maiaframework.domain.DomainId
import org.maiaframework.domain.LifecycleState
import org.maiaframework.domain.auth.EncryptedPassword
import org.maiaframework.domain.contact.EmailAddress
import org.maiaframework.domain.party.FirstName
import org.maiaframework.domain.party.LastName
import org.maiaframework.gen.testing.sample.user.UserEntity
import org.maiaframework.testing.domain.Anys
import org.maiaframework.testing.domain.Anys.anyDomainId
import org.maiaframework.testing.domain.Anys.anyEmailAddress
import org.maiaframework.testing.domain.Anys.anyEncryptedPassword
import org.maiaframework.testing.domain.Anys.anyFirstName
import org.maiaframework.testing.domain.Anys.anyLastName
import java.time.Instant

data class UserEntityTestBuilder(
    val createdById: DomainId = Anys.defaultCreatedById,
    val createdTimestampUtc: Instant = Anys.anyInstant(),
    val emailAddress: EmailAddress = anyEmailAddress(),
    val firstName: FirstName = anyFirstName(),
    val id: DomainId = anyDomainId(),
    val encryptedPassword: EncryptedPassword = anyEncryptedPassword(),
    val lastModifiedById: DomainId = createdById,
    val lastModifiedTimestampUtc: Instant = createdTimestampUtc,
    val lastName: LastName = anyLastName(),
    val lifecycleState: LifecycleState = LifecycleState.ACTIVE,
    val someStrings: List<String> = emptyList(),
    val version: Long = 1L
) {


    fun build(): UserEntity {

        val displayName = "$firstName $lastName"

        return UserEntity(
            createdTimestampUtc,
            displayName,
            emailAddress,
            encryptedPassword.value,
            firstName,
            id,
            lastModifiedTimestampUtc,
            lastName,
            lifecycleState,
            someStrings,
            version
        )

    }

}
