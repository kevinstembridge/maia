package org.maiaframework.toggles

class TogglesImpl(private val toggleRepo: FeatureToggleRepo) : Toggles {


    override fun isActive(feature: Feature): Boolean {

        val featureToggleEntity = toggleRepo.findByPrimaryKey(feature.name)

        if (featureToggleEntity.enabled == false) {
            return false
        }

        // TODO check activation strategies

        return featureToggleEntity.enabled

    }


}
