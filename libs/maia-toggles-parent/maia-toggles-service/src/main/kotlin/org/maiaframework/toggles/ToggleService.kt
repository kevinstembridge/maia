package org.maiaframework.toggles



class ToggleService(private val toggleDao: FeatureToggleDao) {


    fun getAllFeatureToggles(): List<FeatureToggleResponseDto> {

        return this.toggleDao.findAll().map {
            FeatureToggleResponseDto(
                it.featureName
            )
        }

    }


}
