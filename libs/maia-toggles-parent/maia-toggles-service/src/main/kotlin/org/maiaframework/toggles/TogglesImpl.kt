package org.maiaframework.toggles

import org.maiaframework.domain.DomainId
import org.maiaframework.toggles.activation.ActivationStrategyRegistry
import java.util.concurrent.ConcurrentHashMap

class TogglesImpl(
    private val toggleRepo: FeatureToggleRepo,
    private val activationStrategyRegistry: ActivationStrategyRegistry
) : Toggles {


    /**
     * The repo caches entities by primary key, so we remember the id for each feature name
     * to avoid a database lookup by name on every call to [isActive].
     */
    private val idsByFeatureName = ConcurrentHashMap<FeatureName, DomainId>()


    override fun isActive(feature: Feature): Boolean {

        val featureToggleEntity = findEntity(feature.name)

        if (featureToggleEntity.enabled == false) {
            return false
        }

        val activationStrategies = activationStrategyRegistry.getStrategiesFor(featureToggleEntity.activationStrategies)

        return activationStrategies.all { it.invoke() }

    }


    private fun findEntity(featureName: FeatureName): FeatureToggleEntity {

        val knownId = this.idsByFeatureName[featureName]

        if (knownId != null) {

            val entity = this.toggleRepo.findByPrimaryKeyOrNull(knownId)

            if (entity != null) {
                return entity
            }

            // The row was deleted and possibly recreated with a new id.
            this.idsByFeatureName.remove(featureName, knownId)

        }

        val entity = this.toggleRepo.findOneByFeatureName(featureName)
        this.idsByFeatureName[featureName] = entity.id
        return entity

    }


}
