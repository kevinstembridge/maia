package org.maiaframework.toggles

import java.time.Instant


class ToggleSyncer(private val toggleRepo: FeatureToggleRepo) {


    fun sync(feature: Feature) {

        val existingFeatureEntity = this.toggleRepo.findByPrimaryKeyOrNull(feature.name)

        if (existingFeatureEntity == null) {

            persistNewFeatureEntity(feature)

        } else {

            syncExistingFeatureState(existingFeatureEntity, feature)

        }

    }


    private fun persistNewFeatureEntity(feature: Feature) {

        val featureToggleEntity = FeatureToggleEntity.newInstance(
            activationStrategies = emptyList(),
            attributes = feature.attributes,
            comment = "Initial creation by system",
            contactPerson = feature.contactPerson,
            description = feature.description,
            enabled = feature.enabledByDefault,
            featureName = feature.name,
            lastModifiedByUsername = "SYSTEM",
            reviewDate = feature.reviewDate,
            ticketKey = feature.ticketKey,
            infoLink = feature.infoLink,
        )

        this.toggleRepo.insert(featureToggleEntity)

    }


    private fun syncExistingFeatureState(
        existingFeatureEntity: FeatureToggleEntity,
        feature: Feature
    ) {

        if (feature.isDifferentThan(existingFeatureEntity)) {

            val updatedEntity = FeatureToggleEntity(
                activationStrategies = existingFeatureEntity.activationStrategies,
                attributes = feature.attributes,
                comment = "Updated by system",
                contactPerson = feature.contactPerson,
                createdTimestampUtc = existingFeatureEntity.createdTimestampUtc,
                description = feature.description,
                enabled = existingFeatureEntity.enabled,
                featureName = feature.name,
                infoLink = feature.infoLink,
                lastModifiedTimestampUtc = Instant.now(),
                lastModifiedByUsername = "SYSTEM",
                reviewDate = feature.reviewDate,
                ticketKey = feature.ticketKey,
                version = existingFeatureEntity.version + 1
            )

            this.toggleRepo.upsertByFeatureName(updatedEntity)

        }

    }


    private fun Feature.isDifferentThan(featureToggleEntity: FeatureToggleEntity): Boolean {

        return description != featureToggleEntity.description
                || attributes != featureToggleEntity.attributes
                || reviewDate != featureToggleEntity.reviewDate
                || ticketKey != featureToggleEntity.ticketKey
                || contactPerson != featureToggleEntity.contactPerson
                || infoLink != featureToggleEntity.infoLink
                || ticketKey != featureToggleEntity.ticketKey

    }


}
