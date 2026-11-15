package org.maiaframework.gen.renderers.ui

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BigDecimalFormPatternTest {


    @Test
    fun `accepts up to precision minus scale integer digits and up to scale decimal digits`() {

        val pattern = BigDecimalFormPattern.regexFor(precision = 19, scale = 4).toRegex()

        assertThat(listOf("0", "-1", "123456789012345", "123456789012345.1234", "0.5", "-0.0001"))
            .allSatisfy { assertThat(pattern.matches(it)).describedAs(it).isTrue() }

        assertThat(listOf("", "1234567890123456", "1.12345", "abc", "1.", ".5", "1e3", "1,5", " 1"))
            .allSatisfy { assertThat(pattern.matches(it)).describedAs(it).isFalse() }

    }


    @Test
    fun `uses each field's own precision and scale`() {

        val pattern = BigDecimalFormPattern.regexFor(precision = 5, scale = 2).toRegex()

        assertThat(pattern.matches("123.45")).isTrue()
        assertThat(pattern.matches("1234.5")).isFalse()
        assertThat(pattern.matches("1.234")).isFalse()

    }


    @Test
    fun `scale zero allows no decimal part`() {

        val pattern = BigDecimalFormPattern.regexFor(precision = 3, scale = 0).toRegex()

        assertThat(pattern.matches("123")).isTrue()
        assertThat(pattern.matches("1.5")).isFalse()

    }


    @Test
    fun `scale equal to precision allows only a zero integer part`() {

        val pattern = BigDecimalFormPattern.regexFor(precision = 3, scale = 3).toRegex()

        assertThat(pattern.matches("0.123")).isTrue()
        assertThat(pattern.matches("1.123")).isFalse()

    }


}
