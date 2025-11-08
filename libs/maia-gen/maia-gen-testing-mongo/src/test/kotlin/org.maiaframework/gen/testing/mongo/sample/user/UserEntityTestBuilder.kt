package org.maiaframework.gen.testing.mongo.sample.user

import org.maiaframework.domain.DomainId
import org.maiaframework.gen.testing.mongo.sample.contact.EmailAddress
import org.maiaframework.gen.testing.mongo.sample.types.FirstName
import org.maiaframework.gen.testing.mongo.sample.types.LastName
import org.maiaframework.testing.domain.Anys.anyInstant
import org.maiaframework.testing.domain.Anys.anyString
import org.maiaframework.testing.domain.Anys.randomiseCase
import java.time.Instant

data class UserEntityTestBuilder(
    val amount: Double? = null,
    val createdTimestampUtc: Instant = anyInstant(),
    val emailAddress: EmailAddress = EmailAddress("bogus@bogus.com"),
    val encryptedPassword: String = anyString(),
    val firstName: FirstName? = FirstName(randomiseCase(anyString())),
    val id: DomainId = DomainId.newId(),
    val lastName: LastName = LastName(anyString())
) {


    fun build(): UserEntity {

        return UserEntity(
            this.amount,
            this.createdTimestampUtc,
            this.emailAddress,
            this.encryptedPassword,
            this.firstName,
            this.id,
            this.lastName
        )

    }


}
