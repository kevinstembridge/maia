package org.maiaframework.props

import org.maiaframework.common.logging.getLogger
import org.maiaframework.props.repo.PropsRepo
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.EnumerablePropertySource
import java.time.LocalDate


class PropsManager(
    private val propsRepo: PropsRepo,
    private val environment: ConfigurableEnvironment
) {


    private val logger = getLogger<PropsManager>()


    private fun isSensitivePropertyName(propertyName: String): Boolean =
        SENSITIVE_NAME_FRAGMENTS.any { propertyName.contains(it, ignoreCase = true) }


    private fun maskIfSensitive(value: String?, isSensitive: Boolean): String? =
        if (isSensitive && value != null) MASKED_VALUE else value


    fun getAllProperties(): List<PropertyResponseDto> {

        val overridesByName = this.propsRepo.getAllProperties().associateBy { it.propertyName }
        val sourceNameByPropertyName = enumerateEnvironmentPropertyNames()

        val allPropertyNames = (sourceNameByPropertyName.keys + overridesByName.keys).toSortedSet()

        return allPropertyNames.map { propertyName ->
            toPropertyResponseDto(propertyName, overridesByName[propertyName], sourceNameByPropertyName[propertyName])
        }

    }


    // Only walks EnumerablePropertySources, so name discovery and sourceName attribution are incomplete: a
    // property supplied solely by a non-enumerable PropertySource (with no DB override) won't appear here at
    // all, and if a non-enumerable higher-priority source supplies a different value, sourceName could name the
    // wrong source. effectiveValue/environmentValue are unaffected — they come from environment.getProperty(),
    // which resolves across all sources.
    private fun enumerateEnvironmentPropertyNames(): Map<String, String> {

        val sourceNameByPropertyName = mutableMapOf<String, String>()

        this.environment.propertySources
            .filterIsInstance<EnumerablePropertySource<*>>()
            .forEach { propertySource ->
                propertySource.propertyNames.forEach { propertyName ->
                    // putIfAbsent (not put): first property source wins, matching Spring's own
                    // property-source precedence order. Do not simplify to put.
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

        val environmentValue = `get property value from Spring Environment`(propertyName)
        val isSensitive = isSensitivePropertyName(propertyName)

        return if (override != null) {
            PropertyResponseDto(
                comment = override.comment,
                effectiveValue = maskIfSensitive(override.propertyValue, isSensitive),
                environmentValue = maskIfSensitive(environmentValue, isSensitive),
                isOverridden = true,
                isRedundant = override.propertyValue == environmentValue,
                isSensitive = isSensitive,
                lastModifiedByUsername = override.lastModifiedByUsername,
                lastModifiedTimestamp = override.lastModifiedTimestamp,
                propertyName = propertyName,
                reviewDate = override.reviewDate,
                sourceName = "DB override",
            )
        } else {
            PropertyResponseDto(
                comment = null,
                effectiveValue = maskIfSensitive(environmentValue, isSensitive),
                environmentValue = maskIfSensitive(environmentValue, isSensitive),
                isOverridden = false,
                isRedundant = false,
                isSensitive = isSensitive,
                lastModifiedByUsername = null,
                lastModifiedTimestamp = null,
                propertyName = propertyName,
                reviewDate = null,
                sourceName = environmentSourceName,
            )
        }

    }


    private fun `get property value from Spring Environment`(propertyName: String): String? {

        try {
            return this.environment.getProperty(propertyName)
        } catch (e: Exception) {
            logger.warn("Failed to get property value from Spring Environment: $propertyName", e)
            return null
        }

    }


    fun getPropertyHistory(propertyName: String): List<PropertyHistoryItemResponseDto> {

        val isSensitive = isSensitivePropertyName(propertyName)

        return this.propsRepo.getPropertyHistory(propertyName).map {
            PropertyHistoryItemResponseDto(
                changeType = it.changeType,
                comment = it.comment,
                lastModifiedByUsername = it.lastModifiedByUsername,
                lastModifiedTimestamp = it.lastModifiedTimestamp,
                propertyName = it.propertyName,
                propertyValue = if (isSensitive) MASKED_VALUE else it.propertyValue,
                reviewDate = it.reviewDate,
                version = it.version,
            )
        }

    }


    fun setProperty(
        propertyName: String,
        propertyValue: String,
        username: String,
        comment: String?,
        reviewDate: LocalDate?
    ): PropertyResponseDto {

        this.propsRepo.setPropertyOverride(
                propertyName,
                propertyValue,
                username,
                comment,
                reviewDate
        )

        // Both PropsRepo implementations refresh synchronously before setPropertyOverride returns, so this is
        // not normally reachable — but a concurrent removeProperty racing in the window between the write and
        // this re-read can trigger it. Not dead defensive code.
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


    companion object {
        private const val MASKED_VALUE = "******"

        // Broad substring match, not a precise identifier match — deliberately errs toward over-masking
        // (e.g. "hotkey.enabled" gets caught too) rather than ever under-masking a real secret. Same
        // tradeoff Spring Boot Actuator's own Sanitizer makes with its default key list.
        private val SENSITIVE_NAME_FRAGMENTS = listOf("password", "secret", "key", "token", "credential")
    }


}
