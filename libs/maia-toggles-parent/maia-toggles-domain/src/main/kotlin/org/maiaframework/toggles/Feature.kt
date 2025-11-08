package org.maiaframework.toggles

import org.maiaframework.toggles.fields.ContactPerson
import org.maiaframework.toggles.fields.Description
import org.maiaframework.toggles.fields.InfoLink
import org.maiaframework.toggles.fields.TicketKey
import java.time.LocalDate

abstract class Feature {


    val name: FeatureName = FeatureName(javaClass.simpleName)


    open val contactPerson: ContactPerson? = null


    open val ticketKey: TicketKey? = null


    open val description: Description? = null


    open val enabledByDefault: Boolean = false


    open val infoLink: InfoLink? = null


    open val reviewDate: LocalDate? = null


    open val attributes: Map<String, String>? = null


}
