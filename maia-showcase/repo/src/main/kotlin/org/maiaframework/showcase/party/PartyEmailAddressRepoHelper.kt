package org.maiaframework.showcase.party

import org.maiaframework.domain.contact.EmailAddress
import org.maiaframework.showcase.contact.EmailAddressRepo
import org.maiaframework.showcase.party.contact.PartyEmailAddressEntity
import org.springframework.stereotype.Repository

@Repository
class PartyEmailAddressRepoHelper(
    private val emailAddressRepo: EmailAddressRepo,
    private val partyEmailAddressDaoHelper: PartyEmailAddressDaoHelper
) {


    fun findLoginEmailAddressByUsername(username: String): PartyEmailAddressEntity? {

        val emailAddressEntity = this.emailAddressRepo.findOneOrNullByEmailAddress(EmailAddress(username))
            ?: return null

        return this.partyEmailAddressDaoHelper.findOneOrNullLoginEmailAddress(emailAddressEntity)

    }


}
