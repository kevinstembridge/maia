package org.maiaframework.toggles

import org.maiaframework.toggles.fields.ContactPerson
import org.maiaframework.toggles.fields.Description
import org.maiaframework.toggles.fields.InfoLink
import org.maiaframework.toggles.fields.TicketKey
import java.time.LocalDate

abstract class Feature(
    val contactPerson: ContactPerson? = null,
    val ticketKey: TicketKey? = null,
    val description: Description? = null,
    val enabledByDefault: Boolean = false,
    val infoLink: InfoLink? = null,
    val reviewDate: LocalDate? = null,
    val attributes: Map<String, String>? = null,
) {


    val name: FeatureName = FeatureName(javaClass.simpleName)


}
