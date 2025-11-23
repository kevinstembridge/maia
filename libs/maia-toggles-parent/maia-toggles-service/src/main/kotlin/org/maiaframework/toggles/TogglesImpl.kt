package org.maiaframework.toggles

import org.maiaframework.toggles.activation.ActivationStrategyRegistry

class TogglesImpl(
    private val toggleRepo: FeatureToggleRepo,
    private val activationStrategyRegistry: ActivationStrategyRegistry
) : Toggles {


    override fun isActive(feature: Feature): Boolean {

        val featureToggleEntity = toggleRepo.findByPrimaryKey(feature.name)

        if (featureToggleEntity.enabled == false) {
            return false
        }

        val activationStrategies = activationStrategyRegistry.getStrategiesFor(featureToggleEntity.activationStrategies)

        return activationStrategies.all { it.invoke() }

    }


}
