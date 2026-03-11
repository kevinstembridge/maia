package org.maiaframework.showcase.party

import org.maiaframework.domain.DomainId
import org.maiaframework.domain.contact.EmailAddressPurpose
import org.maiaframework.showcase.contact.EmailAddressEntity
import org.maiaframework.showcase.party.contact.PartyEmailAddressEntity
import org.maiaframework.testing.domain.Anys.defaultCreatedById
import java.time.Instant


data class PartyEmailAddressEntityTestBuilder(
    val createdById: DomainId = defaultCreatedById,
    val createdTimestampUtc: Instant = Instant.now(),
    val effectiveFrom: Instant = Instant.now(),
    val effectiveTo: Instant? = null,
    val emailAddressId: DomainId = PartyEmailAddressEntity.newId(),
    val id: DomainId = PartyEmailAddressEntity.newId(),
    val isPrimaryContact: Boolean = true,
    val lastModifiedById: DomainId = defaultCreatedById,
    val lastModifiedTimestampUtc: Instant = Instant.now(),
    val partyId: DomainId = PartyEmailAddressEntity.newId(),
    val purposes: List<EmailAddressPurpose> = emptyList(),
    val version: Long = 1
) {


    fun build(): PartyEmailAddressEntity {

        return PartyEmailAddressEntity(
            createdById,
            createdTimestampUtc,
            effectiveFrom,
            effectiveTo,
            emailAddressId,
            id,
            isPrimaryContact,
            lastModifiedById,
            lastModifiedTimestampUtc,
            partyId,
            purposes.toList(),
            version
        )

    }


    companion object {


        fun forEmailAddress(emailAddressEntity: EmailAddressEntity): PartyEmailAddressEntityTestBuilder {

            return PartyEmailAddressEntityTestBuilder(
                emailAddressId = emailAddressEntity.id
            )

        }


        fun partyEmailAddress(emailAddressEntity: EmailAddressEntity, partyEntity: PartyEntity): PartyEmailAddressEntityTestBuilder {

            return PartyEmailAddressEntityTestBuilder(
                emailAddressId = emailAddressEntity.id,
                partyId = partyEntity.id
            )

        }


    }


}
