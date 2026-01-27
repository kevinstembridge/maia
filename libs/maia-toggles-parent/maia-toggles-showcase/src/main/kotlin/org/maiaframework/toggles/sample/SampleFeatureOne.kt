package org.maiaframework.toggles.sample

import org.maiaframework.toggles.Feature
import org.maiaframework.toggles.fields.ContactPerson


object SampleFeatureOne : Feature(
    contactPerson = ContactPerson("Muriel")
)
