package org.maiaframework.toggles


class ToggleRegistry(toggleProvider: FeatureToggleProvider) {


    private val featuresByName: Map<FeatureName, Feature>


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


    val features: Collection<Feature>
        get() = featuresByName.values


    fun getFeature(featureName: FeatureName): Feature {

        return featuresByName[featureName]
            ?: throw IllegalArgumentException("No feature exists with the name '$featureName'")

    }


}
