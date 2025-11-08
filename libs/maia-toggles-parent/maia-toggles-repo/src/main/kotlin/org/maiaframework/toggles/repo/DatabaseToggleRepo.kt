package org.maiaframework.toggles.repo

import org.maiaframework.toggles.FeatureName
import org.maiaframework.toggles.FeatureState
import org.maiaframework.toggles.FeatureToggleDao
import org.maiaframework.toggles.FeatureToggleHistoryDao
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener


class DatabaseToggleRepo(
    private val featureToggleDao: FeatureToggleDao,
    private val featureToggleHistoryDao: FeatureToggleHistoryDao
) : ToggleRepo {



    @EventListener
    fun handleContextRefreshEvent(event: ContextRefreshedEvent) {
        // TODO refresh()
    }


    override fun getFeatureState(featureName: FeatureName): FeatureState {

        TODO("Not yet implemented")

    }


    override fun getFeatureStateOrNull(featureName: FeatureName): FeatureState? {
        TODO("Not yet implemented")
    }


    override fun save(updatedFeatureState: FeatureState) {
        TODO("Not yet implemented")
    }


}
