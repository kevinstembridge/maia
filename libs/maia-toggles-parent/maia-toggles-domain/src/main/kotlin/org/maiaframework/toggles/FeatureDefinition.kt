package org.maiaframework.toggles

import org.maiaframework.toggles.fields.ContactPerson
import org.maiaframework.toggles.fields.Description
import org.maiaframework.toggles.fields.InfoLink
import org.maiaframework.toggles.fields.TicketKey
import java.time.LocalDate

data class FeatureDefinition(
    val featureName: FeatureName,
    val attributes: Map<String, String>,
    val contactPerson: ContactPerson?,
    val description: Description?,
    val enabledByDefault: Boolean,
    val infoLink: InfoLink?,
    val reviewDate: LocalDate?,
    val ticketKey: TicketKey?
)
