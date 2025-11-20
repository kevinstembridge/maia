package org.maiaframework.toggles


class ToggleRegistry(toggleProvider: FeatureToggleProvider) {


    val featuresByName: Map<FeatureName, Feature>


    init {

        val map = mutableMapOf<FeatureName, Feature>()

        toggleProvider.features.forEach { feature ->

            val existing = map.put(feature.name, feature)

            if (existing != null) {
                throw IllegalArgumentException("Found a duplicate feature name: ${feature.name} on feature class $feature")
            }

        }

        this.featuresByName = map

    }


}
