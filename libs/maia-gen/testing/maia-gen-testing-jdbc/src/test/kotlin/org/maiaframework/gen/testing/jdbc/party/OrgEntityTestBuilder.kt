package org.maiaframework.gen.testing.jdbc.party

import org.maiaframework.domain.DomainId
import org.maiaframework.domain.contact.EmailAddress
import org.maiaframework.gen.testing.jdbc.sample.org.OrganizationEntity
import org.maiaframework.testing.domain.Anys
import org.maiaframework.testing.domain.Anys.anyDomainId
import org.maiaframework.testing.domain.Anys.anyEmailAddress
import org.maiaframework.testing.domain.Anys.anyInstant
import org.maiaframework.testing.domain.Anys.anyOrgName
import java.time.Instant

data class OrgEntityTestBuilder(
    val createdById: DomainId = Anys.defaultCreatedById,
    val createdTimestampUtc: Instant = anyInstant(),
    val emailAddress: EmailAddress = anyEmailAddress(),
    val id: DomainId = anyDomainId(),
    val lastModifiedById: DomainId = createdById,
    val lastModifiedTimestampUtc: Instant = createdTimestampUtc,
    val name: String = anyOrgName()
) {


    fun build(): OrganizationEntity {

        val displayName = name

        return OrganizationEntity(
            createdTimestampUtc,
            displayName,
            emailAddress,
            id,
            lastModifiedTimestampUtc,
            name
        )

    }

}
