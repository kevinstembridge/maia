package org.maiaframework.toggles

import java.time.Instant


class ToggleSyncer(private val toggleDao: FeatureToggleDao) {


    fun sync(featureDefinition: Feature) {

        val existingFeatureEntity = this.toggleDao.findByPrimaryKeyOrNull(featureDefinition.name)

        if (existingFeatureEntity == null) {

            persistNewFeatureEntity(featureDefinition)

        } else {

            syncExistingFeatureState(existingFeatureEntity, featureDefinition)

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
            lastModifiedBy = "SYSTEM",
            reviewDate = feature.reviewDate,
            ticketKey = feature.ticketKey,
            infoLink = feature.infoLink,
        )

        this.toggleDao.insert(featureToggleEntity)

    }


    private fun syncExistingFeatureState(
        existingFeatureEntity: FeatureToggleEntity,
        feature: Feature
    ) {

        if (featureDefinitionHasChanged(feature, existingFeatureEntity)) {

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
                lastModifiedBy = "SYSTEM",
                reviewDate = feature.reviewDate,
                ticketKey = feature.ticketKey,
                version = existingFeatureEntity.version + 1
            )

            this.toggleDao.insert(updatedEntity)

        }

    }


    private fun featureDefinitionHasChanged(
        feature: Feature,
        featureToggleEntity: FeatureToggleEntity
    ): Boolean {

        return feature.description != featureToggleEntity.description
                || feature.attributes != featureToggleEntity.attributes
                || feature.reviewDate != featureToggleEntity.reviewDate
                || feature.ticketKey != featureToggleEntity.ticketKey
                || feature.contactPerson != featureToggleEntity.contactPerson
                || feature.infoLink != featureToggleEntity.infoLink
                || feature.ticketKey != featureToggleEntity.ticketKey

    }


}
