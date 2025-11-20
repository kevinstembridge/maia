package org.maiaframework.toggles

import org.maiaframework.toggles.repo.ToggleRepo

class TogglesImpl(private val toggleRepo: ToggleRepo) {


    fun isActive(featureName: FeatureName) {

        TODO()

        // TODO get the current feature state, including any activation strategies

        val featureState = toggleRepo.getFeatureState(featureName)




    }


}
