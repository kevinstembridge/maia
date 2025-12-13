package org.maiaframework.gen.testing.mongo.sample.person

import org.maiaframework.domain.DomainId
import org.maiaframework.gen.testing.mongo.sample.contact.EmailAddress
import org.maiaframework.gen.testing.mongo.sample.types.FirstName
import org.maiaframework.gen.testing.mongo.sample.types.LastName
import org.maiaframework.testing.domain.Anys
import org.maiaframework.testing.domain.Anys.anyInstant
import org.maiaframework.testing.domain.Anys.anyString
import org.maiaframework.testing.domain.Anys.randomiseCase
import java.time.Instant

data class PersonEntityTestBuilder(
    val amount: Double? = null,
    val createdTimestampUtc: Instant = anyInstant(),
    val emailAddress: EmailAddress = EmailAddress("bogus@bogus.com"),
    val id: DomainId = DomainId.newId(),
    val firstName: FirstName? = FirstName(randomiseCase(Anys.anyFirstName().toString())),
    val lastName: LastName = LastName(anyString())
) {


    fun build(): PersonEntity {

        return PersonEntity(
            this.amount,
            this.createdTimestampUtc,
            this.emailAddress,
            this.firstName,
            this.id,
            this.lastName
        )

    }


}
