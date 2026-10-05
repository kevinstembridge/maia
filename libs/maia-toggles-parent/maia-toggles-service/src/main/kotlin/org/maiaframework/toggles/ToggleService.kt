package org.maiaframework.toggles


class ToggleService(
    private val toggleRepo: FeatureToggleRepo,
    private val toggles: Toggles,
    private val toggleRegistry: ToggleRegistry
) : SetFeatureToggleRequestDtoHandler {


    fun getAllFeatureToggles(): List<FeatureToggleResponseDto> {

        return this.toggleRepo.findAllAsSequence().map {
            FeatureToggleResponseDto(
                activationStrategies = it.activationStrategies,
                attributes = it.attributes,
                comment = it.comment,
                contactPerson = it.contactPerson,
                createdTimestamp = it.createdTimestamp,
                description = it.description,
                enabled = it.enabled,
                featureName = it.featureName,
                id = it.id,
                infoLink = it.infoLink,
                lastModifiedBy = it.lastModifiedByUsername,
                lastModifiedTimestamp = it.lastModifiedTimestamp,
                reviewDate = it.reviewDate,
                ticketKey = it.ticketKey
            )

        }.sortedBy { it.featureName.value }
        .toList()

    }


    override fun handleSetFeatureToggleRequestDto(requestDto: SetFeatureToggleRequestDto) {

        val entity = this.toggleRepo.findOneByFeatureName(requestDto.featureName)

        val updater = FeatureToggleEntityUpdater.forPrimaryKey(entity.id, requestDto.version) {
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
