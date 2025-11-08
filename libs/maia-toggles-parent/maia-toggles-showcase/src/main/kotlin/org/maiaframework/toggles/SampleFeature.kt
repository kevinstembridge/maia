package org.maiaframework.toggles

import org.maiaframework.toggles.fields.ContactPerson

object SampleFeature : Feature() {

    override val contactPerson = ContactPerson("Muriel")

}
