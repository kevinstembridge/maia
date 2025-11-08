package org.maiaframework.toggles

import java.time.Instant


class ToggleSyncer(private val toggleDao: FeatureToggleDao) {


    fun sync(featureDefinition: FeatureDefinition) {

        val existingFeatureEntity = this.toggleDao.findOneOrNullByFeatureName(featureDefinition.featureName)

        if (existingFeatureEntity == null) {

            persistNewFeatureEntity(featureDefinition)

        } else {

            syncExistingFeatureState(existingFeatureEntity, featureDefinition)

        }

    }


    private fun persistNewFeatureEntity(featureDefinition: FeatureDefinition) {

        val featureToggleEntity = FeatureToggleEntity.newInstance(
            activationStrategies = emptyList(),
            attributes = featureDefinition.attributes,
            comment = "Initial creation by system",
            contactPerson = featureDefinition.contactPerson,
            description = featureDefinition.description,
            enabled = featureDefinition.enabledByDefault,
            featureName = featureDefinition.featureName,
            lastModifiedBy = "SYSTEM",
            reviewDate = featureDefinition.reviewDate,
            ticketKey = featureDefinition.ticketKey,
            infoLink = featureDefinition.infoLink,
        )

        this.toggleDao.insert(featureToggleEntity)

    }


    private fun syncExistingFeatureState(
        existingFeatureEntity: FeatureToggleEntity,
        featureDefinition: FeatureDefinition
    ) {

        if (featureDefinitionHasChanged(featureDefinition, existingFeatureEntity)) {

            val updatedEntity = FeatureToggleEntity(
                activationStrategies = existingFeatureEntity.activationStrategies,
                attributes = featureDefinition.attributes,
                comment = "Updated by system",
                contactPerson = featureDefinition.contactPerson,
                createdTimestampUtc = existingFeatureEntity.createdTimestampUtc,
                description = featureDefinition.description,
                enabled = existingFeatureEntity.enabled,
                featureName = featureDefinition.featureName,
                id = existingFeatureEntity.id,
                infoLink = featureDefinition.infoLink,
                lastModifiedTimestampUtc = Instant.now(),
                lastModifiedBy = "SYSTEM",
                reviewDate = featureDefinition.reviewDate,
                ticketKey = featureDefinition.ticketKey,
                version = existingFeatureEntity.version + 1
            )

            this.toggleDao.insert(updatedEntity)

        }

    }


    private fun featureDefinitionHasChanged(
        featureDefinition: FeatureDefinition,
        featureToggleEntity: FeatureToggleEntity
    ): Boolean {

        return featureDefinition.description != featureToggleEntity.description
                || featureDefinition.attributes != featureToggleEntity.attributes
                || featureDefinition.reviewDate != featureToggleEntity.reviewDate
                || featureDefinition.ticketKey != featureToggleEntity.ticketKey
                || featureDefinition.contactPerson != featureToggleEntity.contactPerson
                || featureDefinition.infoLink != featureToggleEntity.infoLink
                || featureDefinition.ticketKey != featureToggleEntity.ticketKey

    }


}
