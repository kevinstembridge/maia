package org.maiaframework.toggles


class ToggleService(
    private val toggleRepo: FeatureToggleRepo,
    private val toggles: Toggles,
    private val toggleRegistry: ToggleRegistry
) : SetFeatureToggleRequestDtoHandler {


    fun getAllFeatureToggles(): List<FeatureToggleResponseDto> {

        return this.toggleRepo.findAllAsSequence().map {
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

        }.sortedBy { it.featureName.value }
        .toList()

    }


    override fun handleSetFeatureToggleRequestDto(requestDto: SetFeatureToggleRequestDto) {

        val updater = FeatureToggleEntityUpdater.forPrimaryKey(requestDto.featureName, requestDto.version) {
            enabled(requestDto.enabled)
            comment(requestDto.comment)
        }

        this.toggleRepo.setFields(updater)

    }


    fun isActive(featureName: FeatureName): FeatureToggleIsActiveResponseDto {

        val feature = this.toggleRegistry.getFeature(featureName)
        val isActive = this.toggles.isActive(feature)

        return FeatureToggleIsActiveResponseDto(isActive)

    }


}
