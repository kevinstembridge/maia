package org.maiaframework.gen.testing.mongo.sample.org

import org.maiaframework.domain.DomainId
import org.maiaframework.gen.testing.mongo.sample.contact.EmailAddress
import org.maiaframework.testing.domain.Anys.anyInstant
import org.maiaframework.testing.domain.Anys.anyString
import java.time.Instant

data class OrganizationEntityTestBuilder(
    val createdTimestampUtc: Instant = anyInstant(),
    val emailAddress: EmailAddress = EmailAddress("bogus@bogus.com"),
    val id: DomainId = DomainId.newId(),
    val name: String = anyString()
) {


    fun build(): OrganizationEntity {

        return OrganizationEntity(
            this.createdTimestampUtc,
            this.emailAddress,
            this.id,
            this.name
        )

    }


}
