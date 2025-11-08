package org.maiaframework.dynamic_regex

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.util.regex.PatternSyntaxException

class DynamicRegexPatternTest {


    @Test
    fun `constructor should not throw exception if pattern is valid`() {

        assertDoesNotThrow {
            DynamicRegexPattern { "abc.*" }
        }

    }


    @Test
    fun `constructor should throw exception if pattern is invalid`() {

        assertThatThrownBy {
            DynamicRegexPattern { "\\invalid pattern" }
        }.isInstanceOf(PatternSyntaxException::class.java)

    }


    @Test
    fun `should return null current pattern if regex is blank`() {

        val dynamicRegexPattern = DynamicRegexPattern { "" }
        assertThat(dynamicRegexPattern.currentPattern).isNull()

    }


    @Test
    fun `should retain previous pattern if updated to an invalid pattern`() {

        var currentRegex = "abc.*"
        val dynamicRegexPattern = DynamicRegexPattern { currentRegex }

        assertThat(dynamicRegexPattern.currentPattern!!.pattern()).isEqualTo("abc.*")

        currentRegex = "\\x invalid pattern"

        assertThat(dynamicRegexPattern.currentPattern!!.pattern()).isEqualTo("abc.*")

    }


    @Test
    fun `should return null if updated to a blank pattern`() {

        var currentRegex = "abc.*"
        val dynamicRegexPattern = DynamicRegexPattern { currentRegex }

        assertThat(dynamicRegexPattern.currentPattern!!.pattern()).isEqualTo("abc.*")

        currentRegex = ""

        assertThat(dynamicRegexPattern.currentPattern).isNull()

    }


}
