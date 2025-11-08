package org.maiaframework.dynamic_regex

import org.slf4j.LoggerFactory
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

class DynamicRegexPattern(
    private val regexPatternSupplier: () -> String?
) {


    private val logger = LoggerFactory.getLogger(javaClass)


    private var pattern: Pattern?


    init {

        // Initialise the pattern early so that we fail on startup if it's not valid.
        val regexPattern = regexPatternFromSupplier
        pattern = if (regexPattern.isNullOrBlank()) null else Pattern.compile(regexPattern)

    }


    val currentPattern: Pattern?
        get() {

            val regexPatternFromProps = regexPatternFromSupplier

            if (changed(regexPatternFromProps)) {
                pattern = recompilePattern(regexPatternFromProps)
            }

            return pattern

        }


    private fun changed(filterExpressionFromProps: String?): Boolean {

        val currentFilterExpression = currentFilterExpression

        return if (currentFilterExpression == null) {

            filterExpressionFromProps != null

        } else {

            currentFilterExpression == filterExpressionFromProps == false

        }

    }


    private val currentFilterExpression: String?
        get() = pattern?.pattern()


    private fun recompilePattern(regexPattern: String?): Pattern? {

        logger.info("Regex pattern is being updated to \"{}\"", regexPattern)

        return if (regexPattern.isNullOrBlank()) {
            null
        } else try {
            Pattern.compile(regexPattern)
        } catch (e: PatternSyntaxException) {
            logger.warn("Invalid regex pattern:", e)
            pattern
        }

    }

    private val regexPatternFromSupplier: String?
        get() = regexPatternSupplier.invoke()


}
