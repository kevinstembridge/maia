package org.maiaframework.props

import org.maiaframework.props.repo.PropsRepo
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.EnumerablePropertySource

class PropsManager(
    private val propsRepo: PropsRepo,
    private val environment: ConfigurableEnvironment
) {


    fun getAllProperties(): List<PropertyResponseDto> {

        val overridesByName = this.propsRepo.getAllProperties().associateBy { it.propertyName }
        val sourceNameByPropertyName = enumerateEnvironmentPropertyNames()

        val allPropertyNames = (sourceNameByPropertyName.keys + overridesByName.keys).toSortedSet()

        return allPropertyNames.map { propertyName ->
            toPropertyResponseDto(propertyName, overridesByName[propertyName], sourceNameByPropertyName[propertyName])
        }

    }


    private fun enumerateEnvironmentPropertyNames(): Map<String, String> {

        val sourceNameByPropertyName = mutableMapOf<String, String>()

        this.environment.propertySources
            .filterIsInstance<EnumerablePropertySource<*>>()
            .forEach { propertySource ->
                propertySource.propertyNames.forEach { propertyName ->
                    sourceNameByPropertyName.putIfAbsent(propertyName, propertySource.name)
                }
            }

        return sourceNameByPropertyName

    }


    private fun toPropertyResponseDto(
        propertyName: String,
        override: PropsEntity?,
        environmentSourceName: String?
    ): PropertyResponseDto {

        val environmentValue = this.environment.getProperty(propertyName)

        return if (override != null) {
            PropertyResponseDto(
                comment = override.comment,
                effectiveValue = override.propertyValue,
                environmentValue = environmentValue,
                isOverridden = true,
                lastModifiedByUsername = override.lastModifiedByUsername,
                lastModifiedTimestamp = override.lastModifiedTimestamp,
                propertyName = propertyName,
                sourceName = "DB override",
            )
        } else {
            PropertyResponseDto(
                comment = null,
                effectiveValue = environmentValue,
                environmentValue = environmentValue,
                isOverridden = false,
                lastModifiedByUsername = null,
                lastModifiedTimestamp = null,
                propertyName = propertyName,
                sourceName = environmentSourceName,
            )
        }

    }


    fun getPropertyHistory(propertyName: String): List<PropertyHistoryItemResponseDto> {

        return this.propsRepo.getPropertyHistory(propertyName).map {
            PropertyHistoryItemResponseDto(
                changeType = it.changeType,
                comment = it.comment,
                lastModifiedByUsername = it.lastModifiedByUsername,
                lastModifiedTimestamp = it.lastModifiedTimestamp,
                propertyName = it.propertyName,
                propertyValue = it.propertyValue,
                version = it.version,
            )
        }

    }


    fun setProperty(
        propertyName: String,
        propertyValue: String,
        username: String,
        comment: String?
    ): PropertyResponseDto {

        this.propsRepo.setPropertyOverride(
                propertyName,
                propertyValue,
                username,
                comment
        )

        val override = this.propsRepo.getPropertyOrNull(propertyName)
            ?: throw IllegalStateException("Expected a property override to exist for '$propertyName' immediately after setting it.")

        return toPropertyResponseDto(propertyName, override, null)

    }


    fun removeProperty(propertyName: String, username: String, comment: String?) {

        this.propsRepo.removePropertyOverride(
                propertyName,
                username,
                comment
        )

    }


}
