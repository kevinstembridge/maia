package org.maiaframework.toggles


class ToggleRegistry(toggleProvider: FeatureToggleProvider) {


    val featuresByName: Map<FeatureName, FeatureDefinition>


    init {

        val map = mutableMapOf<FeatureName, FeatureDefinition>()

        toggleProvider.features.forEach { feature ->

            val featureDefinition = FeatureDefinition(
                feature.name,
                feature.attributes.orEmpty(),
                feature.contactPerson,
                feature.description,
                feature.enabledByDefault,
                feature.infoLink,
                feature.reviewDate,
                feature.ticketKey
            )

            val existing = map.put(feature.name, featureDefinition)

            if (existing != null) {
                throw IllegalArgumentException("Found a duplicate feature name: ${feature.name} on feature class $feature")
            }

        }

        this.featuresByName = map

    }


}
