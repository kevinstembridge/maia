package org.maiaframework.toggles.sample

import org.maiaframework.toggles.Feature
import org.maiaframework.toggles.fields.ContactPerson


object SampleFeatureTwo : Feature(
    contactPerson = ContactPerson("Muriel"),
    enabledByDefault = true
)
