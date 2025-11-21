package org.maiaframework.toggles


class ToggleService(private val toggleDao: FeatureToggleDao) {


    fun getAllFeatureToggles(): List<FeatureToggleResponseDto> {

        return this.toggleDao.findAll().map {
            FeatureToggleResponseDto(
                it.activationStrategies,
                it.attributes,
                it.comment,
                it.contactPerson,
                it.createdTimestampUtc,
                it.description,
                it.enabled,
                it.featureName,
                it.infoLink,
                it.lastModifiedBy,
                it.lastModifiedTimestampUtc,
                it.reviewDate,
                it.ticketKey
            )

        }

    }


}
