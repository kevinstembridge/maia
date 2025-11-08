package org.maiaframework.toggles.repo

import org.maiaframework.toggles.FeatureName
import org.maiaframework.toggles.FeatureState

interface ToggleRepo {


    fun getFeatureState(featureName: FeatureName): FeatureState


    fun getFeatureStateOrNull(featureName: FeatureName): FeatureState?


    fun save(updatedFeatureState: FeatureState)


}
