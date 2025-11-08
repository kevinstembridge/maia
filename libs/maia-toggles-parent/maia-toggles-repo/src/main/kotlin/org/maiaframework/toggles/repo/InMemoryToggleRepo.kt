package org.maiaframework.toggles.repo

import org.maiaframework.toggles.FeatureName
import org.maiaframework.toggles.FeatureState

class InMemoryToggleRepo: ToggleRepo {


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
