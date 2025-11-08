package org.maiaframework.dynamic_filter

import org.maiaframework.dynamic_regex.DynamicRegexPattern
import org.slf4j.LoggerFactory
import java.util.regex.Pattern

class DynamicStringFilter(
    val includePatternPropertyName: String,
    val excludePatternPropertyName: String,
    val displayName: String? = null,
    val description: String?,
    private val propertySupplier: (String) -> String?
) {


    private val logger = LoggerFactory.getLogger(javaClass)


    private val includeRegex = DynamicRegexPattern { this.propertySupplier.invoke(this.includePatternPropertyName) }


    private val excludeRegex = DynamicRegexPattern { this.propertySupplier.invoke(this.excludePatternPropertyName) }


    fun accepts(input: String): Boolean {

        return input.isIncluded() && input.isNotExcluded()

    }


    private fun String.isIncluded(): Boolean {

        val pattern = includeRegex.currentPattern
            ?: return true

        if (this.isBlank()) return true

        return matches(pattern, this)

    }


    private fun String.isNotExcluded(): Boolean {

        val pattern = excludeRegex.currentPattern
            ?: return true

        if (this.isBlank()) return true

        return matches(pattern, this) == false

    }


    private fun matches(pattern: Pattern, input: String): Boolean {

        val matcher = pattern.matcher(input)
        val matches = matcher.matches()

        logger.debug("input='{}', filter={}, matches={}", input, pattern, matches)
        return matches

    }


    fun rejects(input: String): Boolean {

        return accepts(input) == false

    }


    fun currentPatternDescription(): String {

        return "include='${includeRegex.currentPattern}', exclude='${excludeRegex.currentPattern}'"

    }


    fun currentIncludePattern(): String? {

        return includeRegex.currentPattern?.pattern()

    }


    fun currentExcludePattern(): String? {

        return excludeRegex.currentPattern?.pattern()

    }


}
