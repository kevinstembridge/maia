package org.maiaframework.showcase.party

import org.maiaframework.domain.DomainId
import org.maiaframework.showcase.party.contact.EmailAddressVerificationDao
import org.maiaframework.showcase.party.contact.EmailAddressVerificationEntityFilters
import org.springframework.stereotype.Component

@Component
class EmailAddressVerificationRepoHelper(private val emailAddressVerificationDao: EmailAddressVerificationDao) {


    fun isEmailAddressVerified(emailAddressId: DomainId): Boolean {

        val filters = EmailAddressVerificationEntityFilters()

        val filter = filters.and(
            filters.emailAddressId eq emailAddressId,
            filters.isEffectiveNow(),
        )

        val found = this.emailAddressVerificationDao.findAllBy(filter)

        return found.isNotEmpty()

    }


}
