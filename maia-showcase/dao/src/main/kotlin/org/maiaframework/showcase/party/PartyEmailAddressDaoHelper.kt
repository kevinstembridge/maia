package org.maiaframework.showcase.party

import org.maiaframework.domain.DomainId
import org.maiaframework.domain.contact.EmailAddress
import org.maiaframework.domain.contact.EmailAddressPurpose
import org.maiaframework.showcase.contact.EmailAddressDao
import org.maiaframework.showcase.contact.EmailAddressEntity
import org.maiaframework.showcase.party.contact.PartyEmailAddressDao
import org.maiaframework.showcase.party.contact.PartyEmailAddressEntity
import org.maiaframework.showcase.party.contact.PartyEmailAddressEntityFilters
import org.springframework.stereotype.Component
import java.time.Instant


@Component
class PartyEmailAddressDaoHelper(
    private val partyEmailAddressDao: PartyEmailAddressDao,
    private val emailAddressDao: EmailAddressDao
) {


    fun findLoginEmailAddress(emailAddressEntity: EmailAddressEntity): PartyEmailAddressEntity {

        val found = findBy(emailAddressEntity)

        if (found.size > 1) {
            throw RuntimeException("Only expecting to find one login email address for ${emailAddressEntity.emailAddress} but found $found")
        }

        return found.first()

    }


    fun findOneOrNullLoginEmailAddress(emailAddressEntity: EmailAddressEntity): PartyEmailAddressEntity? {

        val found = findBy(emailAddressEntity)

        return found.firstOrNull()

    }


    private fun findBy(emailAddressEntity: EmailAddressEntity): List<PartyEmailAddressEntity> {

        val filters = PartyEmailAddressEntityFilters()

        val filter = filters.and(
            filters.emailAddress eq emailAddressEntity.id,
            filters.purposes contains EmailAddressPurpose.USER_LOGIN,
            filters.effectiveFrom lte Instant.now(),
            filters.or(
                filters.effectiveTo.isNull(),
                filters.effectiveTo gte Instant.now(),
            )
        )

        return partyEmailAddressDao.findAllBy(filter)

    }


    fun findFirstEffectiveLoginEmailAddressForParty(partyId: DomainId): EmailAddressEntity? {

        return findEffectiveLoginEmailAddressesByParty(partyId)
            .firstOrNull()
            ?.let {
                this.emailAddressDao.findByPrimaryKeyOrNull(it.emailAddress)
            }

    }


    private fun findEffectiveLoginEmailAddressesByParty(partyId: DomainId): List<PartyEmailAddressEntity> {

        val filters = PartyEmailAddressEntityFilters()

        val filter = filters.and(
            filters.party eq partyId,
            filters.purposes contains EmailAddressPurpose.USER_LOGIN,
            filters.effectiveFrom lte Instant.now(),
            filters.or(
                filters.effectiveTo.isNull(),
                filters.effectiveTo gte Instant.now(),
            )
        )

        return partyEmailAddressDao.findAllBy(filter)

    }


    fun findPrimaryEmailAddress(partyId: DomainId): EmailAddress? {

        val filters = PartyEmailAddressEntityFilters()

        val filter = filters.and(
            filters.party eq partyId,
            filters.isPrimaryContact eq true,
            filters.effectiveFrom lte Instant.now(),
            filters.or(
                filters.effectiveTo.isNull(),
                filters.effectiveTo gte Instant.now(),
            )
        )

        val partyEmailAddressEntity = this.partyEmailAddressDao.findAllBy(filter).firstOrNull()

        return partyEmailAddressEntity?.let {
            this.emailAddressDao.findByPrimaryKeyOrNull(it.emailAddress)?.emailAddress
        }

    }


}
