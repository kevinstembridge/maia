package org.maiaframework.gen.testing.jdbc.party

import org.maiaframework.domain.DomainId
import org.maiaframework.domain.contact.EmailAddress
import org.maiaframework.domain.party.FirstName
import org.maiaframework.domain.party.LastName
import org.maiaframework.gen.testing.jdbc.sample.person.PersonEntity
import org.maiaframework.testing.domain.Anys
import org.maiaframework.testing.domain.Anys.anyDomainId
import org.maiaframework.testing.domain.Anys.anyEmailAddress
import org.maiaframework.testing.domain.Anys.anyFirstName
import org.maiaframework.testing.domain.Anys.anyInstant
import org.maiaframework.testing.domain.Anys.anyLastName
import java.time.Instant

data class PersonEntityTestBuilder(
    val createdById: DomainId = Anys.defaultCreatedById,
    val createdTimestampUtc: Instant = anyInstant(),
    val emailAddress: EmailAddress = anyEmailAddress(),
    val firstName: FirstName = anyFirstName(),
    val id: DomainId = anyDomainId(),
    val lastModifiedById: DomainId = createdById,
    val lastModifiedTimestampUtc: Instant = createdTimestampUtc,
    val lastName: LastName = anyLastName()
) {


    fun build(): PersonEntity {

        val displayName = "$firstName $lastName"

        return PersonEntity(
            createdTimestampUtc,
            displayName,
            emailAddress,
            firstName,
            id,
            lastModifiedTimestampUtc,
            lastName
        )

    }

}
